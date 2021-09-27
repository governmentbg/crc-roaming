package bg.crc.roamingapp.activity

import android.Manifest
import android.app.NotificationManager
import android.content.*
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.service.notification.StatusBarNotification
import android.telephony.PhoneStateListener
import android.telephony.ServiceState
import android.telephony.TelephonyManager
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.plusAssign
import androidx.navigation.ui.setupWithNavController
import bg.crc.roamingapp.R
import bg.crc.roamingapp.app.RoamingProtect
import bg.crc.roamingapp.constant.AppUtils
import bg.crc.roamingapp.constant.Constants
import bg.crc.roamingapp.databinding.ActivityHomeBinding
import bg.crc.roamingapp.debug.MyDebug
import bg.crc.roamingapp.dialog.CustomDialog
import bg.crc.roamingapp.helper.LocationTrackerHelper
import bg.crc.roamingapp.helper.TelecomsHelper
import bg.crc.roamingapp.service.ForegroundLocationService
import bg.crc.roamingapp.service.LocationBroadcastReceiver
import bg.crc.roamingapp.util.KeepStateNavigator
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_menu.*
import kotlinx.android.synthetic.main.dialogue_unintentional_roaming.*
import kotlinx.android.synthetic.main.home_header.*
import kotlinx.android.synthetic.main.internet_connectivity_view.*

/**
 * This screen display Home UI.
 * User can redirect from here to setting Screen and list of telecoms screen.
 */
class HomeActivity : BaseActivity(), SharedPreferences.OnSharedPreferenceChangeListener{
    private val REQUEST_CODE = 1 // request for permissions

    private lateinit var sharedPreferences: SharedPreferences
    private val locationBroadcastReceiver = LocationBroadcastReceiver()

    private lateinit var binding: ActivityHomeBinding

    // Listens for location broadcasts from ForegroundOnlyLocationService.
    private lateinit var foregroundBroadcastReceiver: ForegroundBroadcastReceiver

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        when (intent?.extras?.get(FRAGMENT_TO_START_BUNDLE_KEY) ?: HomeNavigationTarget.HOME_FRAGMENT_TO_SHOW) {
            HomeNavigationTarget.HOME_FRAGMENT_TO_SHOW -> binding.navView.selectedItemId = R.id.navigation_home
            HomeNavigationTarget.TELECOMS_FRAGMENT_TO_SHOW -> binding.navView.selectedItemId = R.id.navigation_telecoms
            HomeNavigationTarget.HISTORY_FRAGMENT_TO_SHOW -> binding.navView.selectedItemId = R.id.navigation_history
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = bindContentView(R.layout.activity_home)

        foregroundBroadcastReceiver = ForegroundBroadcastReceiver()
        sharedPreferences = getSharedPreferences(Constants.PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
//	    setListeners()
        init(savedInstanceState)

        registerReceiver(locationBroadcastReceiver, IntentFilter("android.location.PROVIDERS_CHANGED"))


        val telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        telephonyManager.listen(RoamingProtect.phoneStateListener, PhoneStateListener.LISTEN_SERVICE_STATE)
    }

    /**
     * Check for permission.
     * If permission approved then start location service [LocationTrackerHelper.startLocationService]
     * If not then request for permission.
     **/
    private fun init(savedInstanceState: Bundle?) {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController
        val navigator = KeepStateNavigator(this, navHostFragment.childFragmentManager, R.id.navHostFragment)
        navController.navigatorProvider += navigator
        navController.setGraph(R.navigation.mobile_navigation)
        binding.navView.setupWithNavController(navController)

        if (savedInstanceState == null) {
            when (intent.extras?.get(FRAGMENT_TO_START_BUNDLE_KEY) ?: HomeNavigationTarget.HOME_FRAGMENT_TO_SHOW) {
                HomeNavigationTarget.HOME_FRAGMENT_TO_SHOW -> binding.navView.selectedItemId = R.id.navigation_home
                HomeNavigationTarget.TELECOMS_FRAGMENT_TO_SHOW -> binding.navView.selectedItemId = R.id.navigation_telecoms
                HomeNavigationTarget.HISTORY_FRAGMENT_TO_SHOW -> binding.navView.selectedItemId = R.id.navigation_history
            }
        }

        val locationPermission: Int = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        val phoneStatePermission: Int = checkSelfPermission(Manifest.permission.READ_PHONE_STATE)
        if (locationPermission == PackageManager.PERMISSION_GRANTED && phoneStatePermission == PackageManager.PERMISSION_GRANTED) {
            if (MyDebug.IS_DEBUG) MyDebug.showLog("LocationService", "Start from init")
            LocationTrackerHelper.startLocationService()

            if(!AppUtils.isBulgarianSIM())
                showBulgarianSIMRequiredDialog()
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE),
                    REQUEST_CODE)
        }
    }

    /**
     * This override function handles Home Screen back press.
     **/
    override fun onBackPressed() {
        LocationTrackerHelper.stopLocationService()
        val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val statusBarNotifications: Array<StatusBarNotification> = mNotificationManager.activeNotifications
        if (statusBarNotifications.isNotEmpty()) {
            statusBarNotifications.forEach { notification ->
                if (notification.id == Constants.TRACKING_NOTIFICATION_ID) {
                    mNotificationManager.cancel(Constants.TRACKING_NOTIFICATION_ID)
                }
                if (notification.id == Constants.ZONE_NOTIFICATION_ID) {
                    mNotificationManager.cancel(Constants.ZONE_NOTIFICATION_ID)
                }
                if (notification.id == Constants.GPS_ALERT_NOTIFICATION_ID) {
                    mNotificationManager.cancel(Constants.GPS_ALERT_NOTIFICATION_ID)
                }
            }
        }
        super.onBackPressed()
    }

    /**
     *This override function notify to user about internet connectivity.
     * */
    override fun onInternetConnectivityChange() {
        tvInternetConnection?.visibility = if (isInternetAvailable) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        // Updates button states if new location is added to SharedPreferences.
        if (key == Constants.KEY_FOREGROUND_ENABLED) {
            sharedPreferences.getBoolean(Constants.KEY_FOREGROUND_ENABLED, false)
        }
    }

    override fun onStop() {
        //Remove from foreground service
        LocationTrackerHelper.unbindServiceFromLocationTrack(this)
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onStop()
    }

    override fun onPause() {
        // remove broadcast receiver when activity stops
        LocalBroadcastManager.getInstance(this).unregisterReceiver(
            foregroundBroadcastReceiver
        )
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        // register broadcast receiver after starting activity
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            LocationBroadcastReceiver.showStickyNotification(notificationManager, this@HomeActivity)
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(
            foregroundBroadcastReceiver,
            IntentFilter(ForegroundLocationService.ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST)
        )
    }

    override fun onStart() {
        super.onStart()
        /**
         * This store data about application in foreground
         * and start [ForegroundLocationService]
         **/
        sharedPreferences.getBoolean(Constants.KEY_FOREGROUND_ENABLED, false)
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        LocationTrackerHelper.bindServiceForLocationTrack(this)
    }

    /**
     * Receiver for location broadcasts from [ForegroundLocationService].
     */
    private inner class ForegroundBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            /*val location = intent.getParcelableExtra<Location>(
                ForegroundLocationService.EXTRA_LOCATION
            )*/
        }
    }

    /**
     * Check granted and not granted permission and redirect according to permission.
     * On cancel permission request show dialog[showPermissionRequestDialog].
     * On permission approved start location service[LocationTrackerHelper.startLocationService].
     * */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            /**
             * If location permission is not granted then show dialog for permission request.
             * */
            REQUEST_CODE -> {
                if (!(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                    showPermissionRequestDialog()
                } else {
                    if (MyDebug.IS_DEBUG) MyDebug.showLog("LocationService", "Start from onRequestPermissionsResult")
                    LocationTrackerHelper.startLocationService()
                    if(!AppUtils.isBulgarianSIM())
                        showBulgarianSIMRequiredDialog()
                }
                return
            }
        }
    }

    /**
     * Show dialog for permission request.
     * */
    private fun showPermissionRequestDialog() {
        CustomDialog.Builder(this)
            .hasPositiveButton(false)
            .setTitle(getString(R.string.permission))
            .build(getString(R.string.permission_message), null).show()
    }

    private fun showBulgarianSIMRequiredDialog() {
        CustomDialog.Builder(this)
            .hasPositiveButton(false)
            .setTitle(getString(R.string.bulgarian_sim_required))
            .build(getString(R.string.bulgarian_sim_required_message), null).show()
    }

    companion object {
        enum class HomeNavigationTarget {
            HOME_FRAGMENT_TO_SHOW,
            TELECOMS_FRAGMENT_TO_SHOW,
            HISTORY_FRAGMENT_TO_SHOW,
        }

        const val FRAGMENT_TO_START_BUNDLE_KEY = "FRAGMENT_TO_START_BUNDLE_KEY"

        fun start(context: Context) = context.startActivity(Intent(context, HomeActivity::class.java))
    }
}
