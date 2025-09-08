package com.example.kodomo

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object ShouldLogout {

    private const val PREF_NAME = "logout_pref"
    private const val KEY_SHOULD_LOGOUT = "should_logout"
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun set(value: Boolean) {
        prefs.edit { putBoolean(KEY_SHOULD_LOGOUT, value) }
    }

    fun get(): Boolean {
        return prefs.getBoolean(KEY_SHOULD_LOGOUT, false)
    }
}
