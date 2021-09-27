package bg.crc.roamingapp.app

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.telephony.PhoneStateListener
import android.telephony.ServiceState
import android.telephony.TelephonyManager
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import bg.crc.roamingapp.constant.AppUtils
import bg.crc.roamingapp.constant.Constants
import bg.crc.roamingapp.debug.MyDebug
import bg.crc.roamingapp.helper.LocationTrackerHelper
import bg.crc.roamingapp.helper.TelecomsHelper
import bg.crc.roamingapp.models.BlockingEntry
import bg.crc.roamingapp.models.reportromaningzone.RealRoamingZone
import bg.crc.roamingapp.notifications.NotificationForRoaming
import bg.crc.roamingapp.notifications.NotificationForTracking
import bg.crc.roamingapp.notifications.NotificationForZone
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger


/**
 * This class is the application class.
 * This class help in some centralization and also help in managing application level uses objects.
 * When the application is create we initialize shared preference [@see bg.crc.roamingapp.app.ApplicationSession].
 * so we can access this class over application without initialize every time.
 **/
class RoamingProtect : Application(), LifecycleObserver, LocationListener {

    companion object {
        private val TAG = "RoamingProtect"
        lateinit var context: Context
        lateinit var phoneStateListener: PhoneStateListener
        var isNotificationRunning = false
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        ApplicationSession.setPreference(this)

        AppEventsLogger.activateApp(this)
        //LifecycleOwner.getK=.lifecycle.addObserver(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        // create notification channels
        NotificationForTracking.createNotificationChannel(this)
        NotificationForZone.createNotificationChannel(this)
        NotificationForRoaming.createNotificationChannel(this)

        phoneStateListener = object : PhoneStateListener() {
            override fun onServiceStateChanged(serviceState: ServiceState?) {
                super.onServiceStateChanged(serviceState)

                if (ActivityCompat.checkSelfPermission(
                        this@RoamingProtect,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                        this@RoamingProtect,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    val locationManager =
                        getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    val isGPSEnabled =
                        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                    val isNetworkEnabled =
                        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                    if (!isGPSEnabled && !isNetworkEnabled) {
                        return
                    } else {
                        var lat = 0.0
                        var long = 0.0
                        var location: Location? = null

                        if (isNetworkEnabled) {
                            locationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                Constants.LOCATION_INTERVAL,
                                Constants.LOCATION_DISTANCE_INTERVAL, this@RoamingProtect
                            )
                            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            lat = location?.latitude ?: 0.0
                            long = location?.longitude?: 0.0
                        }

                        if (isGPSEnabled) {
                            if (location == null) {
                                locationManager.requestLocationUpdates(
                                    LocationManager.GPS_PROVIDER,
                                    Constants.LOCATION_INTERVAL,
                                    Constants.LOCATION_DISTANCE_INTERVAL, this@RoamingProtect
                                )

                                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                lat = location?.latitude ?: 0.0
                                long = location?.longitude?: 0.0
                            }
                        }

                        AppUtils.notificationForRoamingZone(
                            this@RoamingProtect,
                            lat, long
                        )
                    }
                }
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun killApp() {
        LocationTrackerHelper.stopLocationService()
    }

    override fun onProviderEnabled(provider: String) {
        // TODO: not implemented
    }

    override fun onProviderDisabled(provider: String) {
        // TODO: not implemented
    }

    override fun onLocationChanged(location: Location) {
        // TODO: not implemented
    }
}