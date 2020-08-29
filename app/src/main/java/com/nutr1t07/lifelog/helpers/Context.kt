package com.nutr1t07.lifelog.helpers

import android.content.Context
import androidx.preference.PreferenceManager
import com.nutr1t07.lifelog.data.EventDao
import com.nutr1t07.lifelog.data.EventDatabase
import com.nutr1t07.lifelog.fragment.LoginFragment

val Context.contextEventDB: EventDao
    get() = EventDatabase.getInstance(applicationContext).eventDao()

var Context.username: String?
    get() = PreferenceManager.getDefaultSharedPreferences(this)
        .getString(LoginFragment.USER_NAME, null)
    set(value) = PreferenceManager.getDefaultSharedPreferences(this).edit()
        .putString(LoginFragment.USER_NAME, value)
        .apply()

var Context.lastSyncTime: Long
    get() = PreferenceManager.getDefaultSharedPreferences(this)
        .getLong(LoginFragment.LAST_SYNC_TIME, 0L)
    set(value) = PreferenceManager.getDefaultSharedPreferences(this).edit()
        .putLong(LoginFragment.LAST_SYNC_TIME, value)
        .apply()

var Context.userSession: String?
    get() = PreferenceManager.getDefaultSharedPreferences(this)
        .getString(LoginFragment.USER_SESSION, null)
    set(value) = PreferenceManager.getDefaultSharedPreferences(this).edit()
        .putString(LoginFragment.USER_SESSION, value)
        .apply()

fun Context.logoutUser() {
    PreferenceManager.getDefaultSharedPreferences(this).edit()
        .putString(LoginFragment.USER_NAME, null)
        .putString(LoginFragment.LAST_SYNC_TIME, null)
        .putString(LoginFragment.USER_SESSION, null)
        .apply()
}