package iot.doom.com.doomPortal

import android.app.PendingIntent
import android.content.Intent
import android.preference.PreferenceManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "FirebaseMessaging"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        super.onMessageReceived(remoteMessage)
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.from)

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: " + remoteMessage.data)
            sendNotification(remoteMessage.data["url"])
            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
                // scheduleJob()
            } else {
                // Handle message within 10 seconds
                // handleNow()
            }

        }

        // Check if message contains a notification payload.
        if (remoteMessage.notification != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.data.toProperties())
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    private fun sendNotification(url: String?) {
        val notifyIntent = Intent(this, PhotoDetailActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        notifyIntent.putExtra("url", url)

        val notifyPendingIntent = PendingIntent.getActivity(
            this, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBuilder = NotificationCompat.Builder(this, "test")
            .setSmallIcon(R.drawable.pairing_shape)
            .setContentTitle("Une personne est à votre porte !")
            .setContentText("Cliquez pour gérer l'accès")
            .setContentIntent(notifyPendingIntent)
        with(NotificationManagerCompat.from(this)) {
            notify(0, notificationBuilder.build())
        }
    }

    override fun onNewToken(refreshedToken: String) {
        // Get updated InstanceID token.
        Log.d(TAG, "Refreshed token: $refreshedToken")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        PreferenceManager.getDefaultSharedPreferences(this).edit()
            .putString("fcm_token", refreshedToken).apply()
    }
}
