package bg.crc.roamingapp.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import bg.crc.roamingapp.constant.Constants.KEY_NOTIFICATION_ID

/**
 * Service clear notification after click
 **/
class MyBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.apply {
            val notificationId = getIntExtra(KEY_NOTIFICATION_ID, 0)
            context?.apply {
                // Remove the notification programmatically on button click
                NotificationManagerCompat.from(this).cancel(notificationId)
            }
        }
    }
}