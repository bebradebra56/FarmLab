package com.farmlab.labfarmis.ut.data.shar

import android.content.Context
import androidx.core.content.edit

class FarmLabSharedPreference(context: Context) {
    private val farmLabPrefs = context.getSharedPreferences("farmLabSharedPrefsAb", Context.MODE_PRIVATE)

    var farmLabSavedUrl: String
        get() = farmLabPrefs.getString(FARM_LAB_SAVED_URL, "") ?: ""
        set(value) = farmLabPrefs.edit { putString(FARM_LAB_SAVED_URL, value) }

    var farmLabExpired : Long
        get() = farmLabPrefs.getLong(FARM_LAB_EXPIRED, 0L)
        set(value) = farmLabPrefs.edit { putLong(FARM_LAB_EXPIRED, value) }

    var farmLabAppState: Int
        get() = farmLabPrefs.getInt(FARM_LAB_APPLICATION_STATE, 0)
        set(value) = farmLabPrefs.edit { putInt(FARM_LAB_APPLICATION_STATE, value) }

    var farmLabNotificationRequest: Long
        get() = farmLabPrefs.getLong(FARM_LAB_NOTIFICAITON_REQUEST, 0L)
        set(value) = farmLabPrefs.edit { putLong(FARM_LAB_NOTIFICAITON_REQUEST, value) }


    var farmLabNotificationState:Int
        get() = farmLabPrefs.getInt(FARM_LAB_NOTIFICATION_STATE, 0)
        set(value) = farmLabPrefs.edit { putInt(FARM_LAB_NOTIFICATION_STATE, value) }

    companion object {
        private const val FARM_LAB_NOTIFICATION_STATE = "farmLabNotificationState"
        private const val FARM_LAB_SAVED_URL = "farmLabSavedUrl"
        private const val FARM_LAB_EXPIRED = "farmLabExpired"
        private const val FARM_LAB_APPLICATION_STATE = "farmLabApplicationState"
        private const val FARM_LAB_NOTIFICAITON_REQUEST = "farmLabNotificationRequest"
    }
}