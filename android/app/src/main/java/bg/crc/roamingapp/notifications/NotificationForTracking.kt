package bg.crc.roamingapp.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import bg.crc.roamingapp.R
import bg.crc.roamingapp.constant.Constants.TRACKING_CHANNEL_ID
import bg.crc.roamingapp.constant.Constants.TRACKING_CHANNEL_NAME
import bg.crc.roamingapp.constant.Constants.TRACKING_NOTIFICATION_ID

/**
 * This object is for 'location tracking' notification.
 */
object NotificationForTracking {
    private const val channelId = TRACKING_CHANNEL_ID
    private const val channelName = TRACKING_CHANNEL_NAME
    private const val channelImportance = NotificationManager.IMPORTANCE_DEFAULT
    private const val notificationId = TRACKING_NOTIFICATION_ID

    /**
     * This function creates a notification channel for Android 8 (API 26) and above
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(channelId, channelName, channelImportance)
            notificationChannel.description = context.getString(R.string.notification_channel)
            notificationChannel.setShowBadge(false)

            // register it
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    /**
     * This function creates notification, notification style and handles notification click.
     * This notification fires when the location tracking service is started.
     */
/*
    fun createNotification(title: String, subtitle: String) {
        // cancel a previously shown notification
        NotificationManagerCompat.from(context).cancel(null, Constants.ROAMING_NOTIFICATION_ID)

        val intent = Intent(context, SettingsActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingContentIntent = PendingIntent.getActivity(context, 0, intent, 0)

        val intent2 = Intent(context, MyBroadcastReceiver::class.java)
        intent2.putExtra(Constants.KEY_NOTIFICATION_ID, Constants.ROAMING_NOTIFICATION_ID)
        val pendingDeleteIntent = PendingIntent.getBroadcast(context, 0, intent2, 0)

        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notification = NotificationCompat.Builder(context, Constants.CHANNEL_ID)
                .setSmallIcon(R.drawable.main_logo)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setOngoing(false)
                .setSound(uri)
                .setStyle(NotificationCompat.BigTextStyle().bigText(subtitle))
                .addAction(R.drawable.ic_cancel, context.getString(R.string.dismiss), deletePendingIntent)
                .setContentIntent(pendingIntent)
                .build()
        val notificationManagerCompat = NotificationManagerCompat.from(context)
        notificationManagerCompat.notify(Constants.ROAMING_NOTIFICATION_ID, notification)
    }
*/

    fun generateNotification(context: Context): Notification {
        val title = context.getString(R.string.app_name)
        val text = context.getString(R.string.roaming_sticky_notification)
        val bigTextStyle = NotificationCompat.BigTextStyle().bigText(text)
        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.main_logo)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(bigTextStyle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setNumber(0)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }
}