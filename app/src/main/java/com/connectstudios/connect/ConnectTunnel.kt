package com.connectstudios.connect

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.ConnectivityManager
import android.view.LayoutInflater
import android.widget.FrameLayout
import ch.ethz.ssh2.Connection
import ch.ethz.ssh2.Session
import ch.ethz.ssh2.StreamGobbler
import com.connectstudios.connect.storage.Preferences
import com.shashank.sony.fancytoastlib.FancyToast
import kotlinx.android.synthetic.main.activity_main.view.*
import wei.mark.standout.ui.Window
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class ConnectTunnel : WindowProperties(), NetworkStateReceiver.NetworkStateReceiverListener {

    companion object {
        init {
            System.loadLibrary("napsternetpro")
        }
    }

    private external fun sshServerAddress(): String
    private external fun sshServerPort(): Int
    private external fun appName(): String

    override fun getAppName(): String {
        return appName()
    }

    override fun onClose(id: Int, window: Window?): Boolean {
        if (running) {
            authDone = false
            stopping = true
            sendBroadcast(Intent("stop_vpn"))
            sendBroadcast(Intent("stop_ssh"))
        } else {
            authDone = false
            stopping = true
            sendBroadcast(Intent("stop_vpn"))
        }
        return super.onClose(id, window)
    }

    override fun onDestroy() {
        if (running) {
            authDone = false
            stopping = true
            sendBroadcast(Intent("stop_vpn"))
            sendBroadcast(Intent("stop_ssh"))
        } else {
            authDone = false
            stopping = true
            sendBroadcast(Intent("stop_vpn"))
        }
        super.onDestroy()
    }

    private var sess: Session? = null
    private var authDone = false
    var running = false
    private var starting = false
    private var stopping = false
    val mContext: Context = this
    private var networkStateReceiver: NetworkStateReceiver? = null
    private val conn = Connection(sshServerAddress(), sshServerPort())

    override fun onNetworkAvailable() {
        if (running && authDone) {
            getWindow(1).status.setTextColor(Color.parseColor("#0AD814"))
            getWindow(1).status.text = getText(R.string.connected)
        }
    }

    override fun onNetworkUnavailable() {
        if (running) {
            getWindow(1).status.setTextColor(Color.YELLOW)
            getWindow(1).status.text = getText(R.string.network_lost)
            FancyToast.makeText(
                mContext,
                "CHECK YOUR NETWORK CONNECTION",
                FancyToast.LENGTH_LONG,
                FancyToast.WARNING,
                false
            ).show()
        }
    }

    @Suppress("DEPRECATION")
    private fun setNetworkStateReceiver() {
        networkStateReceiver = NetworkStateReceiver(this)
        networkStateReceiver!!.addListener(this)
        applicationContext.registerReceiver(
            networkStateReceiver,
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )
    }


    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                "authentication_successful" -> {
                    getWindow(1).toggleButton.text = getString(R.string.stop)
                    getWindow(1).status.setTextColor(Color.parseColor("#0AD814"))
                    getWindow(1).status.text = getString(R.string.connected)
                }
                "authentication_unsuccessful" -> {
                    stopping = true
                    sendBroadcast(Intent("stop_vpn"))
                    FancyToast.makeText(
                        mContext,
                        "ERROR WITH YOUR ACCOUNT",
                        FancyToast.LENGTH_LONG,
                        FancyToast.ERROR,
                        false
                    ).show()
                }
                "stop_ssh" -> {
                    Thread(Runnable{
                        sess?.close()
                        conn.close()
                    }).start()
                }
                "authentication_exception" -> {
                    stopping = true
                    sendBroadcast(Intent("stop_vpn"))
                    FancyToast.makeText(
                        mContext,
                        "AUTHENTICATION PROCESS FAILURE",
                        FancyToast.LENGTH_LONG,
                        FancyToast.ERROR,
                        false
                    ).show()
                }
                "vpn_started" -> {
                    running = true
                    starting = false
                    getWindow(1).toggleButton.text = getString(R.string.stop)
                    getWindow(1).status.setTextColor(Color.parseColor("#0AD814"))
                    getWindow(1).status.text = getString(R.string.authenticating)
                    getWindow(1).username.isEnabled = false
                    getWindow(1).password.isEnabled = false
                    if (!authDone) {
                        login()
                    }
                }
                "vpn_starting_update_ui" -> {
                    getWindow(1).toggleButton.text = getString(R.string.stop)
                    getWindow(1).status.setTextColor(Color.parseColor("#FF0A31FD"))
                    getWindow(1).status.text = getString(R.string.connecting)
                    getWindow(1).username.isEnabled = false
                    getWindow(1).password.isEnabled = false
                }
                "vpn_stopped" -> {
                    running = false
                    stopping = false
                    getWindow(1).toggleButton.text = getString(R.string.start)
                    getWindow(1).status.setTextColor(Color.parseColor("#F71A0A"))
                    getWindow(1).status.text = getString(R.string.disconnected)
                    getWindow(1).username.isEnabled = true
                    getWindow(1).password.isEnabled = true
                }
                "vpn_start_err" -> {
                    running = false
                    starting = false
                    getWindow(1).toggleButton.text = getString(R.string.start)
                    getWindow(1).status.setTextColor(Color.parseColor("##F71A0A"))
                    getWindow(1).status.text = getString(R.string.disconnected)
                    getWindow(1).username.isEnabled = true
                    getWindow(1).password.isEnabled = true
                    context?.let {
                        FancyToast.makeText(
                            mContext,
                            "FAILED TO START VPN",
                            FancyToast.LENGTH_LONG,
                            FancyToast.ERROR,
                            false
                        ).show()
                    }
                }
                "vpn_start_err_dns" -> {
                    running = false
                    starting = false
                    getWindow(1).toggleButton.text = getString(R.string.start)
                    getWindow(1).status.setTextColor(Color.parseColor("##F71A0A"))
                    getWindow(1).status.text = getString(R.string.disconnected)
                    getWindow(1).username.isEnabled = true
                    getWindow(1).password.isEnabled = true
                    context?.let {
                        FancyToast.makeText(
                            mContext,
                            "FAILED TO START VPN: DNS MISSING!",
                            FancyToast.LENGTH_LONG,
                            FancyToast.ERROR,
                            false
                        ).show()
                    }
                }
                "vpn_start_err_config" -> {
                    running = false
                    starting = false
                    getWindow(1).toggleButton.text = getString(R.string.start)
                    getWindow(1).status.setTextColor(Color.parseColor("##F71A0A"))
                    getWindow(1).status.text = getString(R.string.disconnected)
                    getWindow(1).username.isEnabled = true
                    getWindow(1).password.isEnabled = true
                    context?.let {
                        FancyToast.makeText(
                            mContext,
                            "FAILED TO START VPN: INVALID CONFIG.",
                            FancyToast.LENGTH_LONG,
                            FancyToast.ERROR,
                            false
                        ).show()
                    }
                }
                "pong" -> {
                    getWindow(1).toggleButton.text = getString(R.string.stop)
                    getWindow(1).status.setTextColor(Color.parseColor("#0AD814"))
                    getWindow(1).status.text = getString(R.string.connected)
                    getWindow(1).username.isEnabled = true
                    getWindow(1).password.isEnabled = true
                    running = true
                    Preferences.putBool(
                        applicationContext,
                        getString(R.string.vpn_is_running),
                        true
                    )
                }
            }
        }
    }

    override fun createAndAttachView(id: Int, frame: FrameLayout) {
        val inflater = (getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater?)!!
        val view = inflater.inflate(R.layout.activity_main, frame, true)
        setNetworkStateReceiver()
        view.username.setText(
            Preferences.getUsername(
                this,
                "username",
                ""
            )
        )
        view.password.setText(
            Preferences.getPassword(
                this,
                "password",
                ""
            )
        )

        registerReceiver(broadcastReceiver, IntentFilter("stop_ssh"))
        registerReceiver(broadcastReceiver, IntentFilter("authentication_exception"))
        registerReceiver(broadcastReceiver, IntentFilter("authentication_successful"))
        registerReceiver(broadcastReceiver, IntentFilter("authentication_unsuccessful"))
        registerReceiver(broadcastReceiver, IntentFilter("vpn_stopped"))
        registerReceiver(broadcastReceiver, IntentFilter("vpn_started"))
        registerReceiver(broadcastReceiver, IntentFilter("vpn_start_err"))
        registerReceiver(broadcastReceiver, IntentFilter("vpn_start_err_dns"))
        registerReceiver(broadcastReceiver, IntentFilter("vpn_start_err_config"))
        registerReceiver(broadcastReceiver, IntentFilter("pong"))
        registerReceiver(broadcastReceiver, IntentFilter("vpn_starting_update_ui"))

        sendBroadcast(Intent("ping"))

        view.toggleButton.setOnClickListener {
            if (!running && !starting) {

                when {
                    view.username.text.isEmpty() -> view.username.error = "MISSING USERNAME"
                    view.password.text.isEmpty() -> view.password.error = "MISSING PASSWORD"
                    view.username.text.isNotEmpty() and view.password.text.isNotEmpty() -> {
                        starting = true
                        Preferences.putUsername(this, "username", view.username.text.toString())
                        Preferences.putPassword(this, "password", view.password.text.toString())
                        val intent = Intent(this, StartVpnActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    }
                }
            } else if (running && !stopping) {
                authDone = false
                sendBroadcast(Intent("stop_ssh"))
                stopping = true
                sendBroadcast(Intent("stop_vpn"))
                getWindow(1).username.isEnabled = true
                getWindow(1).password.isEnabled = true
            } else if (!running && starting) {
                authDone = false
                stopping = true
                sendBroadcast(Intent("stop_vpn"))
                getWindow(1).username.isEnabled = true
                getWindow(1).password.isEnabled = true
            }
        }
    }

    fun login() {
        val tunnelthread = Thread(Runnable {
            val username: String = getWindow(1).username.text.toString().trim()
            val password: String = getWindow(1).password.text.toString().trim()
            try {
                conn.connect()
                val isAuthenticated = conn.authenticateWithPassword(username, password)
                if (!isAuthenticated) {
                    authDone = true
                    sendBroadcast(Intent("authentication_successful"))
                    sess = conn.openSession()
                    //authDone = false
                    //sendBroadcast(Intent("authentication_unsuccessful"))
                    //sendBroadcast(Intent("stop_ssh"))
                    //throw IOException("Authentication failed.")
                } else {
                    authDone = true
                    sendBroadcast(Intent("authentication_successful"))
                    sess = conn.openSession()
                }
                val sess = conn.openSession()
                sess.execCommand("uname -a && date && uptime && who")
                println("Here is some information about the remote host:")
                val stdout = StreamGobbler(sess.stdout)
                val br = BufferedReader(InputStreamReader(stdout))
                while (true) {
                    val line = br.readLine() ?: break
                    println(line)
                }
                /* Show exit status, if available (otherwise "null") */
                println("ExitCode: " + sess.exitStatus)
                /* Close this session */

                sess.close()

                /* Close the connection */

                conn.close()
            } catch (e: IOException) {
                authDone = false
                sendBroadcast(Intent("authentication_exception"))
                stopping = true
                sendBroadcast(Intent("stop_vpn"))
                sendBroadcast(Intent("stop_ssh"))
                /**
                e.printStackTrace(System.err)
                exitProcess(2)
                 **/
            }
        })
        tunnelthread.start()
    }

    /**

    @Suppress("DEPRECATION")
    fun isServerReachable(handler: Handler, timeout: Int) {
    var responded = false
    Thread(Runnable {
    val requestForTest = HttpGet("http://m.google.com");
    DefaultHttpClient().execute(requestForTest)
    responded = true
    }).start()

    var waited = 0
    while (!responded && (waited < timeout)) {
    sleep(100);
    if (!responded) {
    waited += 100;
    }
    }
    if (!responded) {
    handler.sendEmptyMessage(0)
    } else {
    handler.sendEmptyMessage(1)
    }
    }
     **/
}

