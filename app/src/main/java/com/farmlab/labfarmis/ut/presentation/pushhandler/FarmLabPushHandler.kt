package com.farmlab.labfarmis.ut.presentation.pushhandler

import android.os.Bundle
import android.util.Log
import com.farmlab.labfarmis.ut.presentation.app.FarmLabApplication

class FarmLabPushHandler {
    fun farmLabHandlePush(extras: Bundle?) {
        Log.d(FarmLabApplication.FARM_LAB_MAIN_TAG, "Extras from Push = ${extras?.keySet()}")
        if (extras != null) {
            val map = farmLabBundleToMap(extras)
            Log.d(FarmLabApplication.FARM_LAB_MAIN_TAG, "Map from Push = $map")
            map?.let {
                if (map.containsKey("url")) {
                    FarmLabApplication.FARM_LAB_FB_LI = map["url"]
                    Log.d(FarmLabApplication.FARM_LAB_MAIN_TAG, "UrlFromActivity = $map")
                }
            }
        } else {
            Log.d(FarmLabApplication.FARM_LAB_MAIN_TAG, "Push data no!")
        }
    }

    private fun farmLabBundleToMap(extras: Bundle): Map<String, String?>? {
        val map: MutableMap<String, String?> = HashMap()
        val ks = extras.keySet()
        val iterator: Iterator<String> = ks.iterator()
        while (iterator.hasNext()) {
            val key = iterator.next()
            map[key] = extras.getString(key)
        }
        return map
    }

}