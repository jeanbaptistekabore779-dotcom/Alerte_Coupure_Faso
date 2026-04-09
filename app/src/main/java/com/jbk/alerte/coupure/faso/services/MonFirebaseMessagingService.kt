package com.jbk.alerte.coupure.faso.services

import android.R
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
// CORRECTION : Importe TA MainActivity (vérifie bien le package)
import com.jbk.alerte.coupure.faso.ui.MainActivity

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class MonFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Logique pour extraire le titre et le message de la notification Firebase
        val titre = remoteMessage.notification?.title ?: "Alerte Coupure"
        val corps = remoteMessage.notification?.body ?: "Nouvelle mise à jour"

        afficherNotification(titre, corps)
    }

    private fun afficherNotification(titre: String, message: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        // CORRECTION : Utilisation de FLAG_IMMUTABLE (obligatoire sur Android 12+)
        val flags =
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, flags)

        val channelId = "alerte_channel"

        // CORRECTION : Remplace android.R.drawable par une icône de ton projet
        // Si tu n'as pas d'icône, utilise : android.R.drawable.ic_dialog_info
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.stat_sys_warning)
            .setContentTitle(titre)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Alertes Faso",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}