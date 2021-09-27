package bg.crc.roamingapp.constant

import bg.crc.roamingapp.BuildConfig
import java.text.SimpleDateFormat
import java.util.*

/**
 * This object contains important and static information related to app.
 * */
object Constants {
    //Base Url
//    const val BASE_URL = "https://roaming.crc.bg/api/" // Infosys' site
    const val BASE_URL = "https://crc.infosys.bg/roaming/api/" // Infosys' site
//    const val BASE_URL = "http://192.168.40.108:8080/crc-roaming-ws/api/" // Infosys' site
    const val GET_PART = "mobile" // Infosys' site

    const val SERVER_REQUEST_TIMEOUT_IN_SECOND = 60

    var isRedirect = true  //splash screen redirection flag

    //Preference keys

//    const val PREF_IS_LOGIN = "is_email_login"
//    const val PREF_SIGN_IN_WITH_SOCIAL_LOG_IN = "social_login"

    // constants for current login type
    // current login state
    const val PREF_USER_ID = "user_id"
    const val PREF_SECRET_KEY = "secret_key"

    const val PREF_ZONE_LIST: String = "pref_zone_list" // Store Zone list in string format
    const val PREF_ZONE_LIST_EX: String = "pref_zone_list_ex" // Store Zone list in string format

    //Key for store Location. where [PREF_VA] and [PREF_VB] is store lat-longs.
    const val PREF_IN_ROAMING_PENDING_DATA = "in_roaming_pending_data"

    //store users MCC and MNC code.
    const val PREF_USER_MCC_MNC_SIM1 = "user_mcc_mnc_sim1"
    const val PREF_USER_OP_MCC_MNC_SIM1 = "user_op_mcc_mnc_sim1"
    const val PREF_USER_OP_NAME_SIM1 = "user_op_name_sim1"
    const val PREF_USER_MCC_MNC_SIM2 = "user_mcc_mnc_sim2"
    const val PREF_USER_OP_MCC_MNC_SIM2 = "user_op_mcc_mnc_sim2"
    const val PREF_USER_OP_NAME_SIM2 = "user_op_name_sim2"

    /**
     * To store the roaming zone notification status.
     * Note :- to help of this flag, check the user is already get the notification for same roaming zone.
     * true : before that show the notification of same roaming zone.
     */
    const val PREF_IS_SECOND_TIME_ROAMING_ZONE_WARNING = "is_second_time_roaming_zone_warning"
    const val PREF_IS_SECOND_TIME_REAL_ROAMING_NOTIFICATION = "is_second_time_real_roaming_notification"


    // password length
    var PASSWORD_MIN_LENGTH = 6

    // Service name for location
    const val SERVICE = "LocationService"


    // notification "Warning zone with possible roaming"
    const val ZONE_NOTIFICATION_ID = 1
    const val ZONE_CHANNEL_ID = "channel_zone_with_roaming"
    const val ZONE_CHANNEL_NAME = "A zone with possible roaming"
    // notification "Warning device in roaming"
    const val ROAMING_NOTIFICATION_ID = 2
    const val ROAMING_CHANNEL_ID = "channel_device_in_roaming"
    const val ROAMING_CHANNEL_NAME = "The device is in roaming"
    // notification "Warning tracking your location"
    const val TRACKING_NOTIFICATION_ID = 3
    const val TRACKING_CHANNEL_ID = "channel_tracking_location"
    const val TRACKING_CHANNEL_NAME = "Tracking user's location"
    // notification "GPS Alert"
    const val GPS_ALERT_NOTIFICATION_ID = 4
    const val GPS_ALERT_CHANNEL_ID = "channel_gps_alert"
    const val GPS_ALERT_CHANNEL_NAME = "GPS is disabled"
    // notification "Notification Alert"
    const val NOTIFICATION_ALERT_NOTIFICATION_ID = 5
    const val NOTIFICATION_ALERT_CHANNEL_ID = "channel_notification_alert"
    const val NOTIFICATION_ALERT_CHANNEL_NAME = "Notifications disabed alert"
    // key for removing a notification
    const val KEY_NOTIFICATION_ID = "notificationId"


    //Facebook permissions
    const val PUBLIC_PROFILE = "public_profile"
    const val EMAIL = "email"

    const val KEY_FOREGROUND_ENABLED = "tracking_foreground_location"
    var appDateFormatMultiline = SimpleDateFormat("yyyy-MM-dd\nHH:mm:ss", Locale.getDefault())

    // Set the 'location update' interval in seconds
    // this value is not exact, the update may come earlier or later
    const val LOCATION_INTERVAL: Long = 5
    // Set the 'location update' distance interval
    const val LOCATION_DISTANCE_INTERVAL: Float = 5f;

    // Set the "get_zones" API calling timer in Millisecond
    const val GET_ROAMING_ZONE_API_CALLING_PERIOD: Long = 86400000

    const val PREFERENCE_FILE_KEY = "${BuildConfig.APPLICATION_ID}.PREFERENCE_FILE_KEY"
}