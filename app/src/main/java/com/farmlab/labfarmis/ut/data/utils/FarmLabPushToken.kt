package com.farmlab.labfarmis.ut.data.utils

import android.util.Log
import com.farmlab.labfarmis.ut.presentation.app.FarmLabApplication
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class FarmLabPushToken {

    suspend fun farmLabGetToken(
        farmLabMaxAttempts: Int = 3,
        farmLabDelayMs: Long = 1500
    ): String {

        repeat(farmLabMaxAttempts - 1) {
            try {
                val farmLabToken = FirebaseMessaging.getInstance().token.await()
                return farmLabToken
            } catch (e: Exception) {
                Log.e(FarmLabApplication.FARM_LAB_MAIN_TAG, "Token error (attempt ${it + 1}): ${e.message}")
                delay(farmLabDelayMs)
            }
        }

        return try {
            FirebaseMessaging.getInstance().token.await()
        } catch (e: Exception) {
            Log.e(FarmLabApplication.FARM_LAB_MAIN_TAG, "Token error final: ${e.message}")
            "null"
        }
    }


}