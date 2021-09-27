package bg.crc.roamingapp.server

import android.content.Context
import android.provider.Settings
import bg.crc.roamingapp.app.ApplicationSession
import bg.crc.roamingapp.app.RoamingProtect

/**
 * This object is provided request parameters according to it's request.
 * That centralize to request parameter and it's key name.
 * This objects hold all the required parameter which are necessary for particular request.
 * */
object RequestParameters {
    private fun getDeviceID(context: Context) =
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

    private fun getAppVersion() = "1" //BuildConfig.VERSION_NAME

    fun getRegistrationViaEmailParameter(
        email: String,
        password: String
    ): HashMap<String, String> {
        val params = HashMap<String, String>()
        params["a"] = "register"
        params["em"] = email
        params["pw"] = password
        params["vn"] = getAppVersion()
        return params
    }

    fun getLoginViaEmailParameter(email: String, password: String): HashMap<String, String> {
        val params = HashMap<String, String>()
        params["a"] = "login"
        params["deviceOS"] = "android"
        params["em"] = email
        params["pw"] = password
        params["dev_id"] = getDeviceID(RoamingProtect.context)
        params["vn"] = getAppVersion()
        return params
    }

    fun getChangePasswordParameter(id: String, secretKey: String, oldPassword: String, newPassword: String): HashMap<String, String> {
        val params = HashMap<String, String>()
        params["a"] = "change_passwd"
        params["id"] = id
        params["key"] = secretKey
        params["pw"] = oldPassword
        params["newPw"] = newPassword
        return params
    }

    fun getLoginViaFacebookParameter(
        email: String,
        fbUserId: String,
        fbToken: String,
    ): HashMap<String, String> {
        val params = HashMap<String, String>()
        params["a"] = "login_facebook"
        params["deviceOS"] = "android"
        params["em"] = email
        params["fb_user_id"] = fbUserId
        params["t"] = fbToken
        params["dev_id"] = getDeviceID(RoamingProtect.context)
        params["vn"] = getAppVersion()
        return params
    }

    fun getLoginViaGoogleParameter(token: String): HashMap<String, String> {
        val params = HashMap<String, String>()
        params["a"] = "login_google"
        params["deviceOS"] = "android"
        params["t"] = token
        params["dev_id"] = getDeviceID(RoamingProtect.context)
        params["vn"] = getAppVersion()
        return params
    }

    fun getLogoutParameter(secretKey: String, id: String): HashMap<String, String> {
        val params = HashMap<String, String>()
        params["a"] = "logout"
        params["id"] = id
        params["key"] = secretKey
        params["vn"] = getAppVersion()
        return params
    }

    fun getForgottenPasswordParameter(email: String): HashMap<String, String> {
        val params = HashMap<String, String>()
        params["a"] = "generate_password"
        params["em"] = email
        params["vn"] = getAppVersion()
        return params
    }

    fun getTelecomsListParameter(key: String, id: String): HashMap<String, String> {
        val params = HashMap<String, String>()
        params["a"] = "get_telecoms"
        params["id"] = id
        params["key"] = key
        params["vn"] = getAppVersion()
        return params
    }

    fun getRoamingZonesParameter(key: String, id: String): HashMap<String, String> {
        val params = HashMap<String, String>()
        params["a"] = "get_zones"
        params["id"] = id
        params["key"] = key
        params["vn"] = getAppVersion()
        return params
    }

    fun getRoamingZonesParameterEx(key: String, id: String): HashMap<String, String> {
        val params = HashMap<String, String>()
        params["a"] = "get_zones_ex"
        params["id"] = id
        params["key"] = key
        params["vn"] = getAppVersion()
        return params
    }

    fun getReportRoamingZoneParameter(
        roamingData: String,
        opMcc: String?,
        opMnc: String?,
        opName: String?,
        key: String,
        id: String
    ): HashMap<String, String> {
        val params = HashMap<String, String>()
        params["a"] = "add_roaming"
        params["z"] = roamingData
        params["srcTelecomMnc"] = opMnc ?: ""
        params["srcTelecomMcc"] = opMcc ?: ""
        params["srcTelecomName"] = opName ?: ""
        params["id"] = id
        params["key"] = key
        params["vn"] = getAppVersion()
        return params
    }

    fun getReportBlockingParameter(
        id: String,
        key: String,
        mnc: String,
        mcc: String
    ): HashMap<String, String> {
        val params = HashMap<String, String>()
        params["a"] = "add_blocking"
        params["id"] = id
        params["key"] = key
        params["mnc"] = mnc
        params["mcc"] = mcc
        return params
    }

    fun getdbgZoneParameter(
        timestamp: String,
        logitude: String,
        latitude: String,
        zone: String,
        key: String,
        id: String
    ): HashMap<String, String> {
        val params = HashMap<String, String>()
        params["a"] = "dbg_set_zone_alarm"
        params["ts"] = timestamp
        params["lon"] = logitude
        params["lat"] = latitude
        params["zone"] = zone
        params["id"] = id
        params["key"] = key
        params["vn"] = getAppVersion()
        return params
    }

    fun getHistoryParameter(key: String, id: String): HashMap<String, String> {
        val params = HashMap<String, String>()
        params["a"] = "get_history"
        params["id"] = id
        params["key"] = key
        params["vn"] = getAppVersion()
        return params
    }
}