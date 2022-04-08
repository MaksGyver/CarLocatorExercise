package com.example.android.car_locator.repository.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.android.car_locator.constants.CONSTANTS.DATE_FORMAT
import java.util.*

const val SHARED_PREFERENCES_NAME = "SETTINGS"
const val LAST_FETCH_DATE_OF_USERS_NAME = "LAST_FETCH_DATE_OF_USERS"
const val LAST_FETCH_DATE_OF_USERS_DEFAULT = 0L

class AppPreferences(context: Context) {

    companion object {
        const val TAG = "AppPreferences"
    }

    private val preferences: SharedPreferences =
        context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun getLastUsersFetchDate() =
        preferences.getLong(
            LAST_FETCH_DATE_OF_USERS_NAME,
            LAST_FETCH_DATE_OF_USERS_DEFAULT
        )

    fun updateLastUsersFetchDate() {
        val editor: SharedPreferences.Editor = preferences.edit()
        editor.apply {
            putLong(LAST_FETCH_DATE_OF_USERS_NAME, Date().time)
        }
        editor.apply()
        editor.commit()
    }

}