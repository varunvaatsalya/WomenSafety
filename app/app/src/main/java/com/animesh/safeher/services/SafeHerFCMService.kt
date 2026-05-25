package com.animesh.safeher.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.animesh.safeher.MainActivity
import com.animesh.safeher.data.repository.FirebaseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SafeHerFCMService : FirebaseMessagingService() {

    private val repo by lazy { FirebaseRepository() }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Update token in Firestore whenever it refreshes
        CoroutineScope(Dispatchers.IO).launch {
            repo.updateFcmToken(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.notification?.title ?: message.data["title"] ?: "SafeHer Alert"
        val body = message.notification?.body ?: message.data["body"] ?: ""
        val type = message.data["type"] ?: ""
        val mapsLink = message.data["mapsLink"] ?: ""

        showNotification(title, body, mapsLink)
    }

    private fun showNotification(title: String, body: String, mapsLink: String) {
        val channelId = "safeher_sos"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create channel (Android 8+)
        val channel = NotificationChannel(
            channelId, "SOS Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Emergency SOS alerts from nearby users"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 500, 250, 500, 250, 500)
        }
        notificationManager.createNotificationChannel(channel)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("mapsLink", mapsLink)
            putExtra("openScreen", "map")
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
            .setVibrate(longArrayOf(0, 500, 250, 500, 250, 500))
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}