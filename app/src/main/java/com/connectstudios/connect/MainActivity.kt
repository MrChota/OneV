package com.connectstudios.connect

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import wei.mark.standout.StandOutWindow


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StandOutWindow.closeAll(this, ConnectTunnel::class.java)
        StandOutWindow.show(this, ConnectTunnel::class.java, 1)
        //prepareVpn()
        finish()
    }
/*
    private fun prepareVpn() {
        val intent = VpnService.prepare(applicationContext)
        if (intent != null) {
            startActivityForResult(intent, 0)
        } else {
            onActivityResult(0, Activity.RESULT_OK, null)
        }
    }
*/
}
