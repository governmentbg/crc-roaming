package bg.crc.roamingapp.constant

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.provider.Settings
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.text.Html
import android.text.Spanned
import android.text.TextUtils
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import bg.crc.roamingapp.R
import bg.crc.roamingapp.app.ApplicationSession
import bg.crc.roamingapp.app.ApplicationSession.PREF_LIST_OF_TELECOMS
import bg.crc.roamingapp.app.RoamingProtect
import bg.crc.roamingapp.constant.Constants.KEY_FOREGROUND_ENABLED
import bg.crc.roamingapp.constant.Constants.PREFERENCE_FILE_KEY
import bg.crc.roamingapp.constant.Constants.PREF_IS_SECOND_TIME_ROAMING_ZONE_WARNING
import bg.crc.roamingapp.constant.Constants.ROAMING_NOTIFICATION_ID
import bg.crc.roamingapp.constant.Constants.TRACKING_NOTIFICATION_ID
import bg.crc.roamingapp.constant.Constants.ZONE_NOTIFICATION_ID
import bg.crc.roamingapp.debug.MyDebug
import bg.crc.roamingapp.dialog.CustomDialog
import bg.crc.roamingapp.dialog.CustomProgressDialog
import bg.crc.roamingapp.models.telecomslist.Country
import bg.crc.roamingapp.notifications.NotificationForZone
import com.facebook.AccessToken
import com.facebook.login.LoginManager
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.disposables.CompositeDisposable
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.reflect.Type
import java.util.*
import kotlin.collections.ArrayList


/**
 * This object holds application basic utility methods.
 **/
object AppUtils {
    private val TAG = "RoamingProtect"

//    val context = RoamingProtect.context
    var compositeDisposable = CompositeDisposable()

    /**
     * This function check where email-address is valid or not.
     *
     * @param target        targeted email address.
     * @return Boolean
     */
    fun isValidEmail(target: String): Boolean {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target)
            .matches()
    }

    /**
     * This function check where string is valid or not.
     * This function check string is null empty or text with null.
     * <P>
     *    some time server response with "null" text. so prevent that we check null validation.
     * </P>
     *
     * @param target    operation performed on that string.
     * @return Boolean  true - if target string is valid.
     *
     */
    fun appValidateString(target: String?): Boolean {
        if (target == null || target.isEmpty() || target.equals("null", true)) {
            return false
        }
        return true
    }

    /**
     * This function shows message.
     * If the message is less then {@integer toast_text_max_length) characters then it's show toast either alert box.
     *
     * if the message is empty or null then it will be not show message.
     *
     * @param context  use to show toast or alert dialog.
     * @param message  the text to show to the user.
     *
     */
    fun showToast(context: Context, message: String?) {
        if (message != null && message.isNotEmpty()) {
            if (message.length < 250) {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            } else {
                val alert = androidx.appcompat.app.AlertDialog.Builder(context)
                    .setMessage(message)
                    .setPositiveButton(context.getString(R.string.done)) { dialog, _ ->
                        dialog.dismiss()
                    }
                alert.show()

            }
        }
    }

    /**
     * Returns the 'location' object as a human readable string.
     */
    fun Location?.toText(): String {
        return if (this != null) {
            "($latitude, $longitude)"
        } else {
            "Unknown location"
        }
    }

    /**
     * Provides access to SharedPreferences for location to Activities and Services.
     */
    internal object SharedPreferenceUtil {
        /**
         * Returns true if requesting location updates, otherwise returns false.
         *
         * @param context The [Context].
         */
        fun getLocationTrackingPref(context: Context): Boolean =
            context.getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
                .getBoolean(KEY_FOREGROUND_ENABLED, false)

        /**
         * Stores the location updates state in SharedPreferences.
         * @param requestingLocationUpdates The location updates state.
         */
        fun saveLocationTrackingPref(context: Context, requestingLocationUpdates: Boolean) =
            context.getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
                    .edit { putBoolean(KEY_FOREGROUND_ENABLED, requestingLocationUpdates) }
    }


    /**
     * This function is use to get Selected Telecoms data in Array list from preference.
     * @param key  -> is use to get the stored value from preference with same key.
     */
    fun getUserSelectedTelecomsArrayList(key: String): java.util.ArrayList<String?>? {
        //val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val json = ApplicationSession.getUserString(key, "")
        val type: Type = object : TypeToken<java.util.ArrayList<String?>?>() {}.type
        return Gson().fromJson(json, type)
    }

    /**
     * This function store Selected Telecoms data Array list.
     * @param key  -> store value using this key
     * @param list  ->  which is stored in preference
     */
    fun saveUserSelectedTelecomsArrayList(list: ArrayList<String>, key: String) {
        /*val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor: SharedPreferences.Editor = prefs.edit()*/
        val json: String = Gson().toJson(list)
        ApplicationSession.putUserData(key, json)
    }

    /**
     * This function store Telecoms data in Array list.
     * @param key  -> store value using this key
     * @param list  ->  which is stored in preference
     **/
    fun saveTelecomsDataArrayList(list: ArrayList<Country>, key: String) {
        val json: String = Gson().toJson(list)
//        ApplicationSession.putUserData(key, json)
        ApplicationSession.putData(key, json)
    }

    /**
     * This function is use to get Telecoms data in Array list from preference.
     * @param key  -> is use to get the stored value from preference with same key.
     */
    fun getTelecomsDataArrayList(): java.util.ArrayList<Country?>? {
//        val json: String? = ApplicationSession.getUserString(PREF_LIST_OF_TELECOMS, null)
        val json: String? = ApplicationSession.getString(PREF_LIST_OF_TELECOMS, null)
        val type: Type = object : TypeToken<java.util.ArrayList<Country?>?>() {}.type
        return Gson().fromJson(json, type)
    }

    /**
     * This function store Va and Vb data in Array list.
     * @param key  -> store value using this key
     * @param list  ->  which is stored in preference
     */
    fun saveVaAndVbArrayListInPref(list: ArrayList<Double>, key: String) {
        ApplicationSession.putData(key, Gson().toJson(list))
    }

    /**
     *This function is use to get Va and Vb data in Array list from preference.
     * @param key -> is use to get the stored value from preference with key.
     **/
    fun getPrefVaAndVbArrayList(key: String): java.util.ArrayList<Double> {
        val json = ApplicationSession.getString(key)
        val type: Type = object : TypeToken<java.util.ArrayList<Double?>?>() {}.type
        return Gson().fromJson(json, type)
    }

    /**
     *MCC and MNC code with version control
     **/
    @SuppressLint("NewApi")
    fun SubscriptionInfo?.getMCCAndMNC(): String {
        if (this == null) {
            return ""
        }
        return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            String.format("%s %02d", this.mccString, this.mncString?.toInt())
        } else {
            @Suppress("DEPRECATION")
            String.format("%s %02d", this.mcc.toString(), this.mnc)
        }
    }

    /**
     * Show dialog for internet connection.
     **/
    fun showInternetDialog(context: Context) {
        CustomDialog.Builder(context).setTitle(context.getString(R.string.internet_connection))
            .build(context.getString(R.string.internet_not_connection), null).show()
    }

    fun showTelecomsNoInternetDialog(context: Context) {
        CustomDialog.Builder(context).setTitle(context.getString(R.string.internet_connection))
            .build(context.getString(R.string.telecom_no_internet), null).show()
    }

    fun showTelecomsNoUpdateDialog(context: Context) {
        CustomDialog.Builder(context).setTitle(context.getString(R.string.internet_connection))
            .build(context.getString(R.string.telecom_no_update), null).show()
    }

    fun showHistoryNoInternetDialog(context: Context) {
        CustomDialog.Builder(context).setTitle(context.getString(R.string.internet_connection))
            .build(context.getString(R.string.history_no_internet), null).show()
    }


    /**
     * Show dialog for internet connection.
     **/
    fun showInternetDialog(context: Context,  callback: CustomDialog.DialogCallBack?) {
        CustomDialog.Builder(context).setTitle(context.getString(R.string.internet_connection))
                .build(context.getString(R.string.internet_not_connection), callback).show()
    }

    /**
     *This function convert string to html format
     **/
    fun String?.convertIntoHtmlText(): Spanned? {
        if (this == null) {
            return null
        }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
        } else {
            @Suppress("DEPRECATION") Html.fromHtml(this)
        }
    }

    /**
     * This function checks for 'Mobile data' on/off
     */
    fun isMobileDataOn(): Boolean {
        var isMobileDataEnabled = false // Assume disabled
        val cm = RoamingProtect.context.getSystemService(Context.CONNECTIVITY_SERVICE)
        try {
            val cmClass = Class.forName(cm.javaClass.name)
            val method = cmClass.getDeclaredMethod("getMobileDataEnabled")
            method.isAccessible = true // Make the method callable
            // get the setting for "mobile data"
            isMobileDataEnabled = method.invoke(cm) as Boolean
        } catch (e: Exception) {
            if (MyDebug.IS_DEBUG) MyDebug.showLog("Exe", e.message)
        }
        return isMobileDataEnabled
    }

    fun isMiUi(): Boolean {
        return !TextUtils.isEmpty(getSystemProperty("ro.miui.ui.version.name"))
    }

    private fun getSystemProperty(propName: String): String? {
        val line: String
        var input: BufferedReader? = null
        try {
            val p = Runtime.getRuntime().exec("getprop $propName")
            input = BufferedReader(InputStreamReader(p.inputStream), 1024)
            line = input.readLine()
            input.close()
        } catch (ex: IOException) {
            return null
        } finally {
            if (input != null) {
                try {
                    input.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return line
    }

    /**m
     * This function check for 'Data roaming' on/off
     */
    fun isDataRoamingOn(): Boolean {
        // return true if data roaming is enabled, false if not
        val subManager = RoamingProtect.context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        val id = SubscriptionManager.getDefaultDataSubscriptionId()
        if (ActivityCompat.checkSelfPermission(
                RoamingProtect.context, Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }

        val ino = subManager.getActiveSubscriptionInfo(id) ?: return false
        return ino.dataRoaming == 1
    }

    fun isBulgarianSIM(): Boolean {
        val subManager = RoamingProtect.context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        val id = SubscriptionManager.getDefaultDataSubscriptionId()
        if (ActivityCompat.checkSelfPermission(
                RoamingProtect.context, Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }

        val ino = subManager.getActiveSubscriptionInfo(id) ?: return false
        return ino.mccString.equals("284")
    }

    /**
     * lat-long array.
     */
    fun getArrayOfLatLong(firstPoint: LatLng, secondPoint: LatLng): ArrayList<LatLng> {
        return arrayListOf(firstPoint, secondPoint)
    }

    /**
     * Va and Vb arraylist in double
     */
    fun getVaAndVbInArray(firstPoint: Double, secondPoint: Double): ArrayList<Double> {
        return arrayListOf(firstPoint, secondPoint)
    }

    /**
     * This function returns user "is login with facebook or not".
     * if user is login with facebook then it will be used facebook current access token.
     * @return boolean - true  if current access token is not NULL
     */
    fun isLoginWithFacebook(): Boolean {
        return AccessToken.getCurrentAccessToken() != null
    }

    /**
     * This function will be logged out user form facebook.
     */
    fun logoutFacebook() {
        LoginManager.getInstance().logOut()
    }

    /**
     * This function return country name from mcc code.
     * If no match returns an empty string
     */
    fun getCountryName(mcc: String): String {
        var countryName: String = ""
        when (mcc) {
            RoamingProtect.context.getString(R.string.macedonia_mcc) -> {
                countryName = RoamingProtect.context.getString(R.string.macedonia_prefix)
            }
            RoamingProtect.context.getString(R.string.serbia_mcc) -> {
                countryName = RoamingProtect.context.getString(R.string.serbia_prefix)
            }
            RoamingProtect.context.getString(R.string.turkey_mcc) -> {
                countryName = RoamingProtect.context.getString(R.string.turkey_prefix)
            }
            RoamingProtect.context.getString(R.string.romania_mcc) -> {
                countryName = RoamingProtect.context.getString(R.string.romania_prefix)
            }
            RoamingProtect.context.getString(R.string.greece_mcc) -> {
                countryName = RoamingProtect.context.getString(R.string.greece_prefix)
            }
        }
        return countryName
    }

    /**
     * Hide and show progress dialog.
     */
    fun showAndHideDialog(dialog: CustomProgressDialog?, isShow: Boolean) {
        if (isShow && dialog != null && !dialog.isShowing) {
            dialog.show()
        } else if (!isShow && dialog != null && dialog.isShowing) {
            dialog.dismiss()
        }
    }

    /**
     * Dialog for compulsory permission to access roaming functionality.
     */
    fun showPermissionRequestDialog(
        context: Context
    ) {
        val dialog = CustomDialog(
            context,
            context.getString(R.string.permission_message), null
        )
        dialog.setTitle(context.getString(R.string.permission))
        dialog.setCancelable(false)
        dialog.hasPositiveButton(false)
        dialog.show()
    }

    /**
     * Set country flag, name, code, selected country from mcc code .
     */
    fun setCountryValueUsingCode(country: Country, oldCountry: Country?, telecomsData: java.util.ArrayList<String?>? ) {
        country.telcos?.forEach { telco ->
            if(oldCountry?.telcos?.any { t -> t.c.equals(telco.c) } == true) {
                telco.isCheckTelecoms =
                    telecomsData?.contains("${country.mcc} ${telco.c}") ?: false
            } else telco.isCheckTelecoms = true
        }
        country.isCheckCountry = country.telcos?.all { telco -> telco.isCheckTelecoms == true }

        when (country.mcc) {
            RoamingProtect.context.getString(R.string.macedonia_mcc) -> {
                country.countryFlag = R.drawable.ic_macedonia_flag
            }
            RoamingProtect.context.getString(R.string.serbia_mcc) -> {
                country.countryFlag = R.drawable.ic_serbia_flag
            }
            RoamingProtect.context.getString(R.string.turkey_mcc) -> {
                country.countryFlag = R.drawable.ic_turkey_flag
            }
            RoamingProtect.context.getString(R.string.romania_mcc) -> {
                country.countryFlag = R.drawable.ic_romania_flag
            }
            RoamingProtect.context.getString(R.string.greece_mcc) -> {
                country.countryFlag = R.drawable.ic_greece_flag
            }
        }
    }

    /**
     * This function checks if the user is in a roaming zone or not.
     * If notification is on, a notification is fired.
     * Only one notification is fired, the first one when the user enters the roaming zone.
     **/
    fun notificationForRoamingZone(context: Context, lat: Double, long: Double) {
        checkForDeviceInRoamingZone(lat, long)
        checkForDeviceInRealRoaming(context, lat, long)
    }

    /**
     * This function checks if the user is in a roaming zone or not.
     * If notification is on, a notification is fired.
     * Only one notification is fired, the first one when the user enters the roaming zone.
     * If notification is off or the user is not in a roaming zone, a previously shown notification is removed.
     **/
    private fun checkForDeviceInRoamingZone(lat: Double, long: Double) {
//        val isUserInRoamingZone = WarningRoamingZone.checkIsUserInZone(lat, long)
        val isUserInRoamingZone = WarningRoamingZoneEx.checkIsUserInZone(lat, long)
        if (isUserInRoamingZone) {
            // the user is in the roaming zone
            // get current user settings
            val isNotificationOn = ApplicationSession.getUserSettingNotification()
            // must send a notification?
            var sendNotification = false
            val isSecondTimeRoamingZoneWarning = ApplicationSession.getBoolean(PREF_IS_SECOND_TIME_ROAMING_ZONE_WARNING)
            if (MyDebug.IS_DEBUG) {
                MyDebug.showLog(TAG, "checkForDeviceInRoamingZone" + " in the roaming zone, isSecondTimeRoamingZoneWarning="+isSecondTimeRoamingZoneWarning)
            }
            if (!isSecondTimeRoamingZoneWarning) {
                // the user entered into the roaming zone
                ApplicationSession.putData(PREF_IS_SECOND_TIME_ROAMING_ZONE_WARNING, true)
                // send the first notification? yes, if the user settings allows for
                sendNotification = isNotificationOn
                if (MyDebug.IS_DEBUG) {
                    MyDebug.showLog(TAG, "checkForDeviceInRoamingZone" + " the user entered into the roaming zone, sendNotification="+sendNotification)
                }
            }
            else {
                // the user is still in the roaming zone
                if (isNotificationOn) {
                    // notifications are ON, send a notification? yes, if first was not shown
                    sendNotification = !AppUtils.isNotificationShown(RoamingProtect.context, ZONE_NOTIFICATION_ID)
                }
                else {
                    // notifications are OFF
                    NotificationForZone.deleteNotification(RoamingProtect.context)
                }
                if (MyDebug.IS_DEBUG) {
                    MyDebug.showLog(TAG, "checkForDeviceInRoamingZone" + " the user is still in the roaming zone, sendNotification="+sendNotification)
                }
            }
            if (sendNotification) {
                NotificationForZone.createNotification(RoamingProtect.context,
                        RoamingProtect.context.getString(R.string.warning_roaming_zone_title),
                        RoamingProtect.context.getString(R.string.warning_roaming_zone_subtitle)
                )
            }
        }
        else {
            // the user left the roaming zone
            NotificationForZone.deleteNotification(RoamingProtect.context)
            ApplicationSession.putData(PREF_IS_SECOND_TIME_ROAMING_ZONE_WARNING, false)
            if (MyDebug.IS_DEBUG) {
                val isSecondTimeRoamingZoneWarning = ApplicationSession.getBoolean(PREF_IS_SECOND_TIME_ROAMING_ZONE_WARNING)
                MyDebug.showLog(TAG, "checkForDeviceInRoamingZone" + " the user left the roaming zone, isSecondTimeRoamingZoneWarning="+isSecondTimeRoamingZoneWarning)
            }
        }
    }

    /**
     * This function checks if the user is in roaming (i.e. connected to a foreign network) or not.
     **/
    private fun checkForDeviceInRealRoaming(context: Context, lat: Double, long: Double) {
        ReportRoamingZone.checkRoamingNetwork(context, lat, long)
    }

    fun isNotificationShown(context: Context, notificationId: Int): Boolean {
        var isShown = false
        when (notificationId) {
            TRACKING_NOTIFICATION_ID,
            ZONE_NOTIFICATION_ID,
            ROAMING_NOTIFICATION_ID ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
                    if (notificationManager != null) {
                        val sbn = notificationManager.activeNotifications
                        if (sbn.isNotEmpty()) sbn.forEach { n -> if (n.id == notificationId) isShown = true }
                    }
                }
        }
        return isShown
    }
}