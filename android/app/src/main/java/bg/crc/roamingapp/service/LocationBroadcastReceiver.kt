package bg.crc.roamingapp.service

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.location.LocationManager
import androidx.core.app.NotificationCompat
import bg.crc.roamingapp.R
import bg.crc.roamingapp.activity.HomeActivity
import bg.crc.roamingapp.activity.SplashActivity
import bg.crc.roamingapp.constant.Constants


class LocationBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val locationManager = context!!.getSystemService(LOCATION_SERVICE) as LocationManager
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showStickyNotification(notificationManager, context)
        } else {
            hideStickyNotification(notificationManager)
        }
    }

    companion object {
        fun showStickyNotification(
            notificationManager: NotificationManager,
            context: Context
        ) {
            val sbn = notificationManager.activeNotifications
            if (sbn.isNotEmpty()) sbn.forEach { n -> if (n.id == Constants.GPS_ALERT_NOTIFICATION_ID) return }
            notificationManager.notify(
                Constants.GPS_ALERT_NOTIFICATION_ID,
                generateNotification(notificationManager, context)
            )
        }

        private fun hideStickyNotification(notificationManager: NotificationManager) {
            notificationManager.cancel(Constants.GPS_ALERT_NOTIFICATION_ID)
        }

        private fun generateNotification(
            notificationManager: NotificationManager,
            context: Context
        ): Notification {
            /**
             * Create Notification Channel for O+ and beyond devices (26+).
             */
            val notificationChannel = NotificationChannel(
                Constants.GPS_ALERT_CHANNEL_ID,
                Constants.GPS_ALERT_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationChannel.setShowBadge(false)
            notificationManager.createNotificationChannel(notificationChannel)

            /**
             * Notification Channel Id is ignored for Android pre O (26).
             */
            val resultIntent = Intent(context, HomeActivity::class.java)
            val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(context).run {
                addNextIntentWithParentStack(resultIntent)
                getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
            }

            val bigTextStyle = NotificationCompat.BigTextStyle()
                .bigText(context.getString(R.string.gps_alert_message))
            val notificationCompatBuilder =
                NotificationCompat.Builder(context, Constants.GPS_ALERT_CHANNEL_ID)
            return notificationCompatBuilder
                .setStyle(bigTextStyle)
                .setContentTitle(context.getString(R.string.gps_alert_title))
                .setContentText(context.getString(R.string.gps_alert_message))
                .setContentIntent(resultPendingIntent)
                .setSmallIcon(R.drawable.main_logo)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setNumber(0)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build()
        }
    }
}