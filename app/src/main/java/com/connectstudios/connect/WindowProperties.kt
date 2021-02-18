package com.connectstudios.connect

import android.content.Intent
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import wei.mark.standout.StandOutWindow
import wei.mark.standout.constants.StandOutFlags
import wei.mark.standout.ui.Window

open class WindowProperties : StandOutWindow() {

    override fun getAppName(): String {
        return getString(R.string.app_name)
    }

    override fun getAppIcon(): Int {
        return R.drawable.napsterneticon
    }

    override fun getTitle(id: Int): String {
        return appName
    }

    override fun createAndAttachView(id: Int, frame: FrameLayout) {

    }

    override fun getThemeStyle(): Int {
        return R.style.AppTheme
    }


    override fun getParams(id: Int, window: Window): StandOutLayoutParams? {
        val width = resources.getDimension(R.dimen.width).toInt()
        val height = resources.getDimension(R.dimen.height).toInt()
        return StandOutLayoutParams(
            id, width, height,
            StandOutLayoutParams.CENTER, StandOutLayoutParams.CENTER
        )
    }


    override fun getFlags(id: Int): Int {
        return (StandOutFlags.FLAG_BODY_MOVE_ENABLE
                or StandOutFlags.FLAG_WINDOW_HIDE_ENABLE
                or StandOutFlags.FLAG_WINDOW_BRING_TO_FRONT_ON_TAP
                or StandOutFlags.FLAG_WINDOW_EDGE_LIMITS_ENABLE
                or StandOutFlags.FLAG_DECORATION_RESIZE_DISABLE
                or StandOutFlags.FLAG_DECORATION_MAXIMIZE_DISABLE)

    }

    override fun getPersistentNotificationTitle(id: Int): String {
        return appName
    }

    override fun getPersistentNotificationMessage(id: Int): String {
        return "successfully connected"
    }

    // return an Intent that creates a new MultiWindow
    override fun getPersistentNotificationIntent(id: Int): Intent? {
        return null
    }

    override fun getHiddenIcon(): Int {
        return R.drawable.napsterneticon
    }

    override fun getHiddenNotificationTitle(id: Int): String {
        return appName
    }

    override fun getHiddenNotificationMessage(id: Int): String {
        return "tap to reopen"
    }

    // return an Intent that restores the MultiWindow
    override fun getHiddenNotificationIntent(id: Int): Intent? {
        return getShowIntent(this, javaClass, id)
    }

    override fun getShowAnimation(id: Int): Animation {
        return if (isExistingId(id)) {
            // restore
            AnimationUtils.loadAnimation(
                this,
                android.R.anim.slide_in_left
            )
        } else {
            // show
            super.getShowAnimation(id)
        }
    }

    override fun getHideAnimation(id: Int): Animation {
        return AnimationUtils.loadAnimation(
            this,
            android.R.anim.slide_out_right
        )
    }

}