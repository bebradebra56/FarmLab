package com.farmlab.labfarmis.ut.presentation.notificiation

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.os.bundleOf
import com.farmlab.labfarmis.FarmLabActivity
import com.farmlab.labfarmis.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.farmlab.labfarmis.ut.presentation.app.FarmLabApplication

private const val FARM_LAB_CHANNEL_ID = "farm_lab_notifications"
private const val FARM_LAB_CHANNEL_NAME = "FarmLab Notifications"
private const val FARM_LAB_NOT_TAG = "FarmLab"

class FarmLabPushService : FirebaseMessagingService(){
    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Обработка notification payload
        remoteMessage.notification?.let {
            if (remoteMessage.data.contains("url")) {
                farmLabShowNotification(it.title ?: FARM_LAB_NOT_TAG, it.body ?: "", data = remoteMessage.data["url"])
            } else {
                farmLabShowNotification(it.title ?: FARM_LAB_NOT_TAG, it.body ?: "", data = null)
            }
        }

        // Обработка data payload
        if (remoteMessage.data.isNotEmpty()) {
            farmLabHandleDataPayload(remoteMessage.data)
        }
    }

    private fun farmLabShowNotification(title: String, message: String, data: String?) {
        val farmLabNotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Создаем канал уведомлений для Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                FARM_LAB_CHANNEL_ID,
                FARM_LAB_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            farmLabNotificationManager.createNotificationChannel(channel)
        }

        val farmLabIntent = Intent(this, FarmLabActivity::class.java).apply {
            putExtras(bundleOf(
                "url" to data
            ))
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val farmLabPendingIntent = PendingIntent.getActivity(
            this,
            0,
            farmLabIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val farmLabNotification = NotificationCompat.Builder(this, FARM_LAB_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.farm_lab_noti_ic)
            .setAutoCancel(true)
            .setContentIntent(farmLabPendingIntent)
            .build()

        farmLabNotificationManager.notify(System.currentTimeMillis().toInt(), farmLabNotification)
    }

    private fun farmLabHandleDataPayload(data: Map<String, String>) {
        data.forEach { (key, value) ->
            Log.d(FarmLabApplication.FARM_LAB_MAIN_TAG, "Data key=$key value=$value")
        }
    }
}