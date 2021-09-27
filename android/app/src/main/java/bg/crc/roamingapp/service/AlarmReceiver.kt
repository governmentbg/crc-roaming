package bg.crc.roamingapp.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import bg.crc.roamingapp.R
import bg.crc.roamingapp.app.ApplicationSession
import bg.crc.roamingapp.constant.Constants
import bg.crc.roamingapp.activity.SettingsActivity

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if(!ApplicationSession.getUserSettingNotification()) {
            createNotificationChannel(context)
            createRoamingNotification(
                context,
                context.getString(R.string.notification_alert_message)
            )
        }
    }

    private fun createRoamingNotification(context: Context, subtitle: String) {
        // intent that will fire when the user taps the notification
        val resultIntent = Intent(context, SettingsActivity::class.java)
        val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(resultIntent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        // intent that will fire when the user dismisses the notification
        val intent2 = Intent(context, MyBroadcastReceiver::class.java)
        intent2.putExtra(Constants.KEY_NOTIFICATION_ID, Constants.NOTIFICATION_ALERT_NOTIFICATION_ID)
        val deletePendingIntent = PendingIntent.getBroadcast(context, 0, intent2, 0)

        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notification = NotificationCompat.Builder(context, Constants.NOTIFICATION_ALERT_CHANNEL_ID)
            .setSmallIcon(R.drawable.main_logo)
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
        notificationManagerCompat.notify(Constants.NOTIFICATION_ALERT_NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel(context: Context) {
        val notificationChannel = NotificationChannel(Constants.NOTIFICATION_ALERT_CHANNEL_ID, Constants.NOTIFICATION_ALERT_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
        notificationChannel.description = context.getString(R.string.notification_channel)

        // register it
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(notificationChannel)
    }
}