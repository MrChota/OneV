package com.connectstudios.connect.storage

import android.content.Context
import com.connectstudios.connect.R

open class Preferences {
    companion object {

        fun getString(context: Context, k: String, default: String?): String? {
            val sharedPref = context.getSharedPreferences(
                context.getString(R.string.config_preference), Context.MODE_PRIVATE
            )
            return if (default != null) {
                sharedPref.getString(k, default)
            } else {
                sharedPref.getString(k, "")
            }
        }

        fun putUsername(context: Context, k: String, v: String) {
            val sharedPref = context.getSharedPreferences(
                context.getString(R.string.username_preference),
                Context.MODE_PRIVATE
            )
            with(sharedPref.edit()) {
                putString(k, v)
                commit()
            }
        }


        fun getUsername(context: Context, k: String, default: String?): String? {
            val sharedPref = context.getSharedPreferences(
                context.getString(R.string.username_preference), Context.MODE_PRIVATE
            )
            return if (default != null) {
                sharedPref.getString(k, default)
            } else {
                sharedPref.getString(k, "")
            }
        }

        fun putPassword(context: Context, k: String, v: String) {
            val sharedPref = context.getSharedPreferences(
                context.getString(R.string.password_preference),
                Context.MODE_PRIVATE
            )
            with(sharedPref.edit()) {
                putString(k, v)
                commit()
            }
        }


        fun getPassword(context: Context, k: String, default: String?): String? {
            val sharedPref = context.getSharedPreferences(
                context.getString(R.string.password_preference), Context.MODE_PRIVATE
            )
            return if (default != null) {
                sharedPref.getString(k, default)
            } else {
                sharedPref.getString(k, "")
            }
        }

        fun putBool(context: Context, k: String, v: Boolean) {
            val sharedPref = context.getSharedPreferences(
                context.getString(R.string.config_preference),
                Context.MODE_PRIVATE
            )
            with(sharedPref.edit()) {
                putBoolean(k, v)
                commit()
            }
        }

        fun getBool(context: Context, k: String, default: Boolean?): Boolean {
            val sharedPref = context.getSharedPreferences(
                context.getString(R.string.config_preference), Context.MODE_PRIVATE
            )
            return if (default != null) {
                sharedPref.getBoolean(k, default)
            } else {
                sharedPref.getBoolean(k, false)
            }
        }

    }
}
