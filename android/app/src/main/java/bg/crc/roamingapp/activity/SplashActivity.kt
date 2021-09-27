package bg.crc.roamingapp.activity

import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentSender.SendIntentException
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import bg.crc.roamingapp.R
import bg.crc.roamingapp.app.ApplicationSession
import bg.crc.roamingapp.app.ApplicationSession.LOGIN_TYPE_NOT_LOGGED_IN
import bg.crc.roamingapp.constant.AppUtils
import bg.crc.roamingapp.constant.Constants
import bg.crc.roamingapp.debug.MyDebug
import bg.crc.roamingapp.dialog.CustomDialog
import bg.crc.roamingapp.models.telecomslist.Country
import bg.crc.roamingapp.models.telecomslist.Telco
import bg.crc.roamingapp.server.ConnectionDetector
import bg.crc.roamingapp.service.LocationBroadcastReceiver
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_splash.*
import java.util.*


/**
 *
 * This screen show the progress and check configurations.
 * */
class SplashActivity : AppCompatActivity() {
    private val TAG = "RoamingProtect"

    private var handler = Handler(Looper.getMainLooper())

    private var runnable = Runnable {
        if (Constants.isRedirect) {
            redirectToActivity()
        } else {
            Constants.isRedirect = true
        }
    }

    private var loginProcess = 50f  //progress After screen loading

    init {
        Constants.appDateFormatMultiline.timeZone = TimeZone.getTimeZone("GMT")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        loadDefaultTelecomList()
        gpsPermission()
        subcribeToTopicAll()

    }

    private fun init() {
        if (ConnectionDetector.isConnectedToInternet(this)) {
            crpLoading.percent = 50f //initial progress
            /**
             * start handler. [handler] will hold screen to [resources.getInteger(R.integer.splash_screen_timeout)] millisecond.
             */
            handler.postDelayed(
                runnable,
                resources.getInteger(R.integer.splash_screen_timeout).toLong()
            )
        } else {
            AppUtils.showInternetDialog(this,
                object : CustomDialog.DialogCallBack() {
                    override fun onPositiveButtonClick(dialog: CustomDialog) {
                        super.onPositiveButtonClick(dialog)
                        finish()
                    }
                }
            )
        }
    }

    /**
     *  This function check loading progress and which activity should be open.
     */
    private fun redirectToActivity() {
        crpLoading.percent = crpLoading.percent + loginProcess
        if (crpLoading.percent != 100f) {
            return
        }

        val isLoggedIn = ApplicationSession.getString(Constants.PREF_SECRET_KEY) != ""
        if (isLoggedIn) {
            this.startActivity(Intent(this@SplashActivity, HomeActivity::class.java))
        } else {
            this.startActivity(Intent(this, LoginTypeActivity::class.java))
        }
        this.finish()
    }
    /**
     * Subscribes the device to the FCM topic "android", i.e. all android devices will receive
     * a push notification sent to that topic.
     */
    private fun subcribeToTopicAll() {
        FirebaseMessaging.getInstance().subscribeToTopic(getString(R.string.FCM_topic_android))
            .addOnCompleteListener { task ->
                if (MyDebug.IS_DEBUG) MyDebug.showLog(TAG,
                    "Subscription to FCM topic " + getString(R.string.FCM_topic_android) + " isSuccessful=" + task.isSuccessful)
            }
    }

    /**
     * This function for gps permission request to get accurate location.
     */
    private fun gpsPermission() {
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)


        val result: Task<LocationSettingsResponse> =
            LocationServices.getSettingsClient(this).checkLocationSettings(builder.build())

        result.addOnCompleteListener { task ->
            try {
                val response = task.getResult(ApiException::class.java)
                // All location settings are satisfied. The client can initialize location
                // requests here.
                init()
            } catch (exception: ApiException) {
                when (exception.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
                        // Location settings are not satisfied.
                        try {
                            // Cast to a resolvable exception.
                            val resolvable = exception as ResolvableApiException
                            resolvable.startResolutionForResult(
                                this@SplashActivity,
                                LocationRequest.PRIORITY_HIGH_ACCURACY
                            )
                        } catch (e: SendIntentException) {
                            // Ignore the error.
                        } catch (e: ClassCastException) {
                            // Ignore, should be an impossible error.
                        }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                        //Settings change not available
                    }
                }
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            LocationRequest.PRIORITY_HIGH_ACCURACY -> when (resultCode) {
                Activity.RESULT_OK ->
                    init()
                Activity.RESULT_CANCELED ->
                    needGPSPermisionDialog()
                else ->
                    init()
            }
        }
    }

    /**
     * In this function dialog for compulsory gps permission to access roaming functionality.
     */
    private fun needGPSPermisionDialog() {
        val dialog = CustomDialog(
            this,
            getString(R.string.permission_message),
            object : CustomDialog.DialogCallBack() {
                override fun onPositiveButtonClick(dialog: CustomDialog) {
                    super.onPositiveButtonClick(dialog)
                    gpsPermission()
                }
            })
        dialog.setTitle(getString(R.string.permission))
        dialog.setPositiveButton(getString(R.string.ok))
        dialog.setCancelable(false)
        dialog.show()
    }

    private fun loadDefaultTelecomList() {
        ApplicationSession.putUserData(ApplicationSession.PREF_IS_UPDATE_TELECOMS, true)
    }
}