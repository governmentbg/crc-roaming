package bg.crc.roamingapp.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import bg.crc.roamingapp.R
import bg.crc.roamingapp.constant.Constants.KEY_NOTIFICATION_ID
import bg.crc.roamingapp.constant.Constants.ROAMING_CHANNEL_ID
import bg.crc.roamingapp.constant.Constants.ROAMING_CHANNEL_NAME
import bg.crc.roamingapp.constant.Constants.ROAMING_NOTIFICATION_ID
import bg.crc.roamingapp.activity.SettingsActivity
import bg.crc.roamingapp.service.MyBroadcastReceiver

/**
 * This object is for 'device in real roaming' notification.
 */
object NotificationForRoaming {
    private const val channelId = ROAMING_CHANNEL_ID
    private const val channelName = ROAMING_CHANNEL_NAME
    private const val channelImportance = NotificationManager.IMPORTANCE_HIGH
    private const val notificationId = ROAMING_NOTIFICATION_ID

    /**
     * This function creates a notification channel for Android 8 (API 26) and above
     */
    fun createNotificationChannel(context: Context) {
        val notificationChannel = NotificationChannel(channelId, channelName, channelImportance)
        notificationChannel.description = context.getString(R.string.notification_channel)

        // register it
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(notificationChannel)
    }

    /**
     * This function creates notification, notification style and handles notification click.
     * This notification fires when the user connects to real roaming.
     */
    fun createRoamingNotification(context: Context, title: String?, subtitle: String) {
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

        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.main_logo)
                .setContentTitle(title)
                .setStyle(NotificationCompat.BigTextStyle().bigText(subtitle))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(resultPendingIntent)
                // Set the intent that will fire when the user dismisses the notification
                .addAction(R.drawable.ic_cancel, context.getString(R.string.dismiss), deletePendingIntent)
                // automatically remove the notification when the user taps it
                .setAutoCancel(true)
                .setOngoing(false)
                .setSound(uri)
                .build()
        val notificationManagerCompat = NotificationManagerCompat.from(context)
        notificationManagerCompat.notify(notificationId, notification)
    }

    fun deleteNotification(context: Context) {
        // cancel a previously shown notification
        NotificationManagerCompat.from(context).cancel(null, notificationId)
    }
}