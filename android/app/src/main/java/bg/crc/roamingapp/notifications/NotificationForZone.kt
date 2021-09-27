package bg.crc.roamingapp.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import bg.crc.roamingapp.R
import bg.crc.roamingapp.constant.Constants.KEY_NOTIFICATION_ID
import bg.crc.roamingapp.constant.Constants.ZONE_CHANNEL_ID
import bg.crc.roamingapp.constant.Constants.ZONE_CHANNEL_NAME
import bg.crc.roamingapp.constant.Constants.ZONE_NOTIFICATION_ID
import bg.crc.roamingapp.activity.SettingsActivity
import bg.crc.roamingapp.service.MyBroadcastReceiver

/**
 * This object is for 'roaming zone' notification.
 */
object NotificationForZone {
    private val TAG = "RoamingZone"
    private const val channelId = ZONE_CHANNEL_ID
    private const val channelName = ZONE_CHANNEL_NAME
    private const val channelImportance = NotificationManager.IMPORTANCE_HIGH
    private const val notificationId = ZONE_NOTIFICATION_ID

    /**
     * This function creates a notification channel for Android 8 (API 26) and above
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(channelId, channelName, channelImportance)
            notificationChannel.description = context.getString(R.string.notification_channel)

            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()
            notificationChannel.setSound(soundUri, audioAttributes)

            // register it
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    /**
     * This function creates notification, notification style and handles notification click.
     * This notification fires when the user is in a roaming zone.
     */
    fun createNotification(context: Context, title: String, subtitle: String) {
        // cancel a previously shown notification
        deleteNotification(context)

        // intent that will fire when the user taps the notification
        val resultIntent = Intent(context, SettingsActivity::class.java)
        val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(resultIntent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        // intent that will fire when the user dismisses the notification
        val intent2 = Intent(context, MyBroadcastReceiver::class.java)
        intent2.putExtra(KEY_NOTIFICATION_ID, notificationId)
        val deletePendingIntent = PendingIntent.getBroadcast(context, 0, intent2, 0)

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.main_logo)
                .setContentTitle(title)
                .setStyle(NotificationCompat.BigTextStyle().bigText(subtitle))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(resultPendingIntent)
                // Set the intent that will fire when the user dismisses the notification
                .addAction(R.drawable.ic_cancel, context.getString(R.string.dismiss), deletePendingIntent)
                // automatically remove the notification when the user taps it
                .setAutoCancel(true)
                .setOngoing(false)
                .setSound(soundUri)
                .build()
        val notificationManagerCompat = NotificationManagerCompat.from(context)
        notificationManagerCompat.notify(notificationId, notification)
    }

    fun deleteNotification(context: Context) {
        // cancel a previously shown notification
        NotificationManagerCompat.from(context).cancel(null, notificationId)
    }
}


