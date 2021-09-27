/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package bg.crc.roamingapp.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.location.Location
import android.os.Binder
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import bg.crc.roamingapp.BuildConfig
import bg.crc.roamingapp.R
import bg.crc.roamingapp.activity.HomeActivity
import bg.crc.roamingapp.constant.*
import bg.crc.roamingapp.constant.Constants.GPS_ALERT_NOTIFICATION_ID
import bg.crc.roamingapp.constant.Constants.ROAMING_NOTIFICATION_ID
import bg.crc.roamingapp.constant.Constants.SERVICE
import bg.crc.roamingapp.constant.Constants.TRACKING_NOTIFICATION_ID
import bg.crc.roamingapp.constant.Constants.ZONE_NOTIFICATION_ID
import bg.crc.roamingapp.debug.MyDebug
import bg.crc.roamingapp.helper.LocationTrackerHelper.stopLocationService
import com.google.android.gms.location.*
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * Service tracks location when requested and updates Activity via binding. If Activity is
 * stopped/unbinds and tracking is enabled, the service promotes itself to a foreground service to
 * insure location updates aren't interrupted.
 */
class ForegroundLocationService : Service() {
    private var configurationChange = false

    private var serviceRunningInBackground = false

    private val localBinder = LocalBinder()

    private lateinit var notificationManager: NotificationManager

    // FusedLocationProviderClient - Main class for receiving location updates.
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    // LocationRequest - Requirements for the location updates, i.e., how often you should receive
    // updates, the priority, etc.
    private lateinit var locationRequest: LocationRequest

    // LocationCallback - Called when FusedLocationProviderClient has a new Location.
    private lateinit var locationCallback: LocationCallback

    // Used only for local storage of the last known location.
    private var currentLocation: Location? = null

    val TAG = SERVICE

    override fun onCreate() {
        if (MyDebug.IS_DEBUG) MyDebug.showLog(TAG, "onCreate()")
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Review the FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // Create a LocationRequest.
        locationRequest = LocationRequest.create()
            .setInterval(TimeUnit.SECONDS.toMillis(Constants.LOCATION_INTERVAL))
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        /**
         *Initialize the LocationCallback.
         **/
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (MyDebug.IS_DEBUG) MyDebug.showLog("ForegroundLocationService", "onLocationResult" + TimeUnit.MILLISECONDS.toSeconds(Date().time))
                super.onLocationResult(locationResult)
                locationResult.lastLocation.let {
                    currentLocation = it
                    val intent = Intent(ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST)
                    intent.putExtra(EXTRA_LOCATION, currentLocation)
                    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)

                    if (MyDebug.IS_DEBUG) {
                        MyDebug.showLog(TAG, "LocationCallback" +
                                " lat=" + currentLocation?.latitude.toString() +
                                " lng=" + currentLocation?.longitude.toString() +
                                " $locationCallback")
                    }

                    showStickyNotification()
                    if (currentLocation != null) {
                        AppUtils.notificationForRoamingZone(this@ForegroundLocationService,
                            currentLocation!!.latitude, currentLocation!!.longitude)
                    }
                }
            }
        }
    }

    /**
     * Check for sticky notification is visible or not.
     * If not then generate.
     */
    private fun showStickyNotification() {
        if (MyDebug.IS_DEBUG) MyDebug.showLog(TAG, "showStickyNotification()")
        val sbn = notificationManager.activeNotifications
        if (sbn.isNotEmpty()) sbn.forEach { n -> if (n.id == TRACKING_NOTIFICATION_ID) return }
        notificationManager.notify(TRACKING_NOTIFICATION_ID, generateNotification())
/* the original code
        var alreadyVisible = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val statusBarNotifications: Array<StatusBarNotification> = notificationManager.activeNotifications
            if (statusBarNotifications.isNotEmpty()) {
                statusBarNotifications.forEach { notification ->
                    if (notification.id == Constants.LOCATION_NOTIFICATION_ID) {
                        alreadyVisible = true
                    }
                }
                if (!alreadyVisible) {
                    notificationManager.notify(Constants.LOCATION_NOTIFICATION_ID, generateNotification())
                }
            } else {
                notificationManager.notify(Constants.LOCATION_NOTIFICATION_ID, generateNotification())
            }
        } else {
            notificationManager.notify(Constants.LOCATION_NOTIFICATION_ID, generateNotification())
        }
 */
    }

    /**
    * START_NOT_STICKY are used for services that should only remain running while processing any commands sent to them.
    */
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (MyDebug.IS_DEBUG) MyDebug.showLog(TAG, "onStartCommand()")
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        if (MyDebug.IS_DEBUG) MyDebug.showLog(TAG, "onBind()")
        stopForeground(false)
        serviceRunningInBackground = false
        configurationChange = false

        /**
        * Timer for calling API get Roaming zone.
        **/
        val date = Calendar.getInstance()
        val apiCallingTimer = WarningRoamingZoneEx.RoamingZonesTimer()
        date[Calendar.HOUR] = 1
        date[Calendar.MINUTE] = 0
        date[Calendar.SECOND] = 0
        date[Calendar.MILLISECOND] = 0
        apiCallingTimer.scheduleApiCall(date)
        return localBinder
    }

    override fun onRebind(intent: Intent) {
        if (MyDebug.IS_DEBUG) MyDebug.showLog(TAG, "onRebind()")
        stopForeground(false)
        serviceRunningInBackground = false
        configurationChange = false
        super.onRebind(intent)
    }


    override fun onUnbind(intent: Intent): Boolean {
        if (MyDebug.IS_DEBUG) MyDebug.showLog(TAG, "onUnbind()")
        stopForeground(false)
//        ReportRoamingZone.endOfRoaming(this)
        if (!configurationChange && AppUtils.SharedPreferenceUtil.getLocationTrackingPref(this)) {
            val notification = generateNotification()
            startForeground(Constants.TRACKING_NOTIFICATION_ID, notification)
            serviceRunningInBackground = false
        }
        return true
    }

    override fun onDestroy() {
        if (MyDebug.IS_DEBUG) MyDebug.showLog(TAG, "onDestroy")
        stopForeground(true)
        ReportRoamingZone.endOfRoaming(this)
        removeNotifications()
        /**
         *clearing objects
        **/
        super.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        configurationChange = true
    }

    /**
     * This is called if the service is currently running and the user has to stop.
     */
    override fun onTaskRemoved(rootIntent: Intent) {
        if (MyDebug.IS_DEBUG) MyDebug.showLog(TAG, "onTaskRemoved()")
        removeNotifications()
        stopLocationService()
    }

    private fun removeNotifications() {
        val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.cancel(TRACKING_NOTIFICATION_ID)
        mNotificationManager.cancel(ZONE_NOTIFICATION_ID)
        mNotificationManager.cancel(GPS_ALERT_NOTIFICATION_ID)
    }

    /**
     * Subscribe to location changes.
     */
    fun subscribeToLocationUpdates() {
        if (MyDebug.IS_DEBUG) MyDebug.showLog(TAG, "subscribeToLocationUpdates()")

        AppUtils.SharedPreferenceUtil.saveLocationTrackingPref(this, true)

        /**
         *start fetching location service.
         **/
        startService(Intent(this, ForegroundLocationService::class.java))

        try {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } catch (unlikely: SecurityException) {
            AppUtils.SharedPreferenceUtil.saveLocationTrackingPref(this, false)
            if (MyDebug.IS_DEBUG) MyDebug.showLog(
                "service", "Lost location permissions. Couldn't remove updates. $unlikely"
            )
        }
    }

    /**
     * Unsubscribe from location changes.
     */
    fun unsubscribeToLocationUpdates() {
        if (MyDebug.IS_DEBUG) MyDebug.showLog(TAG, "unsubscribeToLocationUpdates()")
        try {
            fusedLocationProviderClient?.flushLocations()
            val removeTask = fusedLocationProviderClient?.removeLocationUpdates(locationCallback)
            removeTask?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (MyDebug.IS_DEBUG) MyDebug.showLog("remove", "successfully")
                    stopForeground(true)
                    stopSelf()
                }
            }
            AppUtils.SharedPreferenceUtil.saveLocationTrackingPref(this, false)
        } catch (unlikely: SecurityException) {
            AppUtils.SharedPreferenceUtil.saveLocationTrackingPref(this, true)
            if (MyDebug.IS_DEBUG) MyDebug.showLog(
                TAG,
                "Lost location permissions. Couldn't remove updates. $unlikely"
            )
        }
    }

    /*
     * Show a notification while this service is running.
     * Notify the user about location tracking.
     * This notification is shown when the application is in foreground/background.
     * When the application is closed this notification is removed.
     */
    private fun generateNotification(): Notification {
        if (MyDebug.IS_DEBUG) MyDebug.showLog(TAG, "generateNotification()")
        val titleText = getString(R.string.app_name)

        /**
         * Create Notification Channel for O+ and beyond devices (26+).
         */
        val notificationChannel = NotificationChannel(
            Constants.TRACKING_CHANNEL_ID,
            titleText,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationChannel.setShowBadge(false)
        notificationManager.createNotificationChannel(notificationChannel)
        /**
         * Notification Channel Id is ignored for Android pre O (26).
         */


        val resultIntent = Intent(this, HomeActivity::class.java)
        val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(resultIntent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val bigTextStyle = NotificationCompat.BigTextStyle().bigText(getString(R.string.roaming_sticky_notification))

        val notificationCompatBuilder = NotificationCompat.Builder(applicationContext, Constants.TRACKING_CHANNEL_ID)
        return notificationCompatBuilder
            .setStyle(bigTextStyle)
            .setContentTitle(titleText)
            .setContentText(getString(R.string.roaming_sticky_notification))
            .setContentIntent(resultPendingIntent)
            .setSmallIcon(R.drawable.main_logo)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setNumber(0)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    /**
     * Class used for the client Binder.Since this service runs in the same process as its
     * clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        internal val service: ForegroundLocationService
            get() = this@ForegroundLocationService
    }

    companion object {
        internal const val ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST =
            "${BuildConfig.APPLICATION_ID}.action.FOREGROUND_ONLY_LOCATION_BROADCAST"

        internal const val EXTRA_LOCATION = "${BuildConfig.APPLICATION_ID}.extra.LOCATION"
    }
}


