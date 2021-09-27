package bg.crc.roamingapp.helper

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import bg.crc.roamingapp.debug.MyDebug
import bg.crc.roamingapp.service.ForegroundLocationService

object LocationTrackerHelper {

    // Provides location updates.
    private var foregroundLocationService: ForegroundLocationService? = null

    private var foregroundLocationServiceBound = false

    private val foregroundOnlyServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            if (MyDebug.IS_DEBUG) MyDebug.showLog("LocationService", "connected")
            val binder = service as ForegroundLocationService.LocalBinder
            foregroundLocationService = binder.service
            foregroundLocationServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            foregroundLocationService = null
            foregroundLocationServiceBound = false
        }
    }

    /**
    * Start location service
    **/
    fun startLocationService() {
        if (MyDebug.IS_DEBUG) MyDebug.showLog(
            "LocationService",
            "startLocationService $foregroundLocationService"
        )
        Handler(Looper.getMainLooper()).postDelayed({
            foregroundLocationService?.subscribeToLocationUpdates()
        }, 200)
    }

    /**
     * Stop location service
     **/
    fun stopLocationService() {
        foregroundLocationService?.unsubscribeToLocationUpdates()
    }

    /**
     * Binding location service, to track current location.
     * */
    fun bindServiceForLocationTrack(context: Context) {
        val serviceIntent = Intent(context, ForegroundLocationService::class.java)
        context.bindService(
            serviceIntent,
            foregroundOnlyServiceConnection,
            Context.BIND_AUTO_CREATE
        )
    }

    /**
     * Unbind location service, to track current location.
     * */
    fun unbindServiceFromLocationTrack(context: Context) {
        if (foregroundLocationServiceBound) {
            context.unbindService(foregroundOnlyServiceConnection)
            foregroundLocationServiceBound = false

        }
    }
}