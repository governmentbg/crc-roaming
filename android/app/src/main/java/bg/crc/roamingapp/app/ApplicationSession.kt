package bg.crc.roamingapp.app

import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import bg.crc.roamingapp.constant.Constants.PREF_SECRET_KEY
import bg.crc.roamingapp.constant.Constants.PREF_USER_ID
import bg.crc.roamingapp.debug.MyDebug
import bg.crc.roamingapp.models.BlockingEntry
import bg.crc.roamingapp.models.reportromaningzone.RealRoamingZone
import com.google.gson.Gson

/**
 *
 * This class store the user preferences and manage preference data.
 * This class can also store other information(value), assigning with its new unique key-name.
 *
 * */
object ApplicationSession {
    private const val TAG = "RoamingProtect" //javaClass.simpleName
    private const val PREF_NAME = "RoamingApp" //Preference name

    // login types
    const val LOGIN_TYPE_NOT_LOGGED_IN = 0
    const val LOGIN_TYPE_EMAIL = 1
    const val LOGIN_TYPE_FACEBOOK = 2
    const val LOGIN_TYPE_GOOGLE = 3

    // current login state
    private const val PREF_LOGIN_TYPE = "login_type"
    private const val PREF_LOGIN_ID = "login_id"

    // prefix for user specific keys based on login id
    private const val PREF_PREFIX_LOGIN_TYPE_EMAIL = "EM$"
    private const val PREF_PREFIX_LOGIN_TYPE_FACEBOOK = "FB$"
    private const val PREF_PREFIX_LOGIN_TYPE_GOOGLE = "GG$"

    // settings for the current user
    const val PREF_IS_NOTIFICATION_ON = "is_notification_on"
    const val PREF_TELECOMS_VERSION = "telecoms_version"
    const val PREF_IS_UPDATE_TELECOMS = "is_update_telecoms"
    const val PREF_IS_SECOND_TIME_TELECOMES = "is_second_time_login"
    const val PREF_BLOCK_TELECOMS = "block_telecoms"
    const val PREF_BLOCK_COUNTRY = "block_country"
    const val PREF_LIST_OF_TELECOMS = "list_of_telecoms"
    const val PREF_ROAMING_DATA = "roaming_data"
    const val PREF_BLOCKING_DATA = "blocking_data"

    private lateinit var mPreferences: SharedPreferences

    var mMediaPlayer: MediaPlayer? = null


    /**
     * This function is initialized preference object in private mode.
     * @param context use to get sharedPreference.
     * */
    fun setPreference(context: Context) {
        mPreferences = context.getSharedPreferences(
                PREF_NAME, Context.MODE_PRIVATE)
    }


    /**
     * This function store information in preference.
     *<p>
     *This function get key name as string and value as Any.
     * This will be store the value according to the value type(Boolean, String, Int, Long or Float).
     * if the value is out of the above types, then it will be convert value into string and store it.
     * </p>
     *
     *@param keyName store value using this key
     *@param value   is "what you want to store?" into the preference.
     *
     *Note : if you want to  store-some object then override that object  with toString function.
     *        either, it will return value is prefix by the object-name and postfix by the runtime value reference number.
     *
     *       if the value is null, it will be not store any value and return.
     * */

    fun putData(keyName: String, value: Any?) {
        if (value == null) {
            return
        }
        val editor = mPreferences.edit()

        when (value) {
            is Boolean -> editor.putBoolean(keyName, value)
            is String -> editor.putString(keyName, value)
            is Int -> editor.putInt(keyName, value)
            is Long -> editor.putLong(keyName, value)
            is Float -> editor.putFloat(keyName, value)
            else -> editor.putString(keyName, value.toString())
        }
        editor.apply()
        editor.commit()
    }


    /**
     * This function store mutable strings set.
     * @param keyName   store value using this key
     * @param value     which is stored in preference
     * */
    fun putStringsData(keyName: String, value: MutableSet<String>) {
        val editor = mPreferences.edit()
        editor.putStringSet(keyName, value)
        editor.apply()
        editor.commit()
    }

    /**
     *This function is use to get boolean from preference.
     * @param keyName    is use to get the stored value from preference with same key.
     * @return Boolean   if the value is not stored then return false
     * */
    fun getBoolean(keyName: String) = mPreferences.getBoolean(keyName, false)

    /**
     *This function is use to get String from preference.
     * @param keyName   is use to get the stored value from preference with same key.
     * @return String   if the value is not stored then return empty string
     * */
    fun getString(keyName: String) = mPreferences.getString(keyName, "")!!

    /**
     * get string from preference
     * @param keyName   is use to get the stored value from preference with same key.
     * @param default   return default value if preference value is not set.
     * @return String   if the value is not stored then return empty string
     * */
    fun getString(keyName: String,default :String?) = mPreferences.getString(keyName, default)

    /**
     *This function is use to get float value from preference.
     * @param keyName   is use to get the stored value from preference with same key.
     * @return String   if the value is not stored then return zero
     * */
    fun getFloat(keyName: String) = mPreferences.getFloat(keyName, 0f)

    /**
     *This function is use to get integer value from preference.
     * @param keyName   is use to get the stored value from preference with same key.
     * @return Int      if the value is not stored then return zero
     * */
    fun getInt(keyName: String) = mPreferences.getInt(keyName, 0)

    /**
     *This function is use to get integer value from preference.
     * @param keyName   is use to get the stored value from preference with same key.
     * @param default return default value if value is not stored in preference.
     * @return Int      if the value is not stored then return zero
     * */
    fun getInt(keyName: String,default :Int) = mPreferences.getInt(keyName, default)

    /**
     *This function is use to get long value from preference.
     * @param keyName   is use to get the stored value from preference with same key.
     * @return Int      if the value is not stored then return zero
     * */
    fun getLong(keyName: String) = mPreferences.getLong(keyName, 0)

    /**
     *This function is use to get set of strings value from preference.
     * @param keyName   is use to get the stored value from preference with same key.
     * @return Int      if the value is not stored then return null
     * */
    fun getStringSet(keyName: String): MutableSet<String>? = mPreferences.getStringSet(keyName, null)

    /**
     * This function clear all data from preference.
     * */
    fun clearAllData() = mPreferences.edit().clear().commit()

    /**
     * This function clear preference detail which is stored with same key.
     * @param keyName   remove preference information which is store with same key.
     * @return  true    if this remove from preference
     * */
    fun clearData(keyName: String) = mPreferences.edit().remove(keyName).commit()

    /**
     * This function is use to get all preference information.
     * @return this will return all information in collection (Map<String,Any>)
     * */
    fun getAllData(): MutableMap<String, *> = mPreferences.all

    /**
     * This function is use to  clear  multiple information.
     * @param keys  passes all keys will be clear from preference.
     * */
    fun clearArrayString(keys: ArrayList<String>) {
        val editor = mPreferences.edit()
        keys.forEach {key ->
            editor.remove(key)
        }
        editor.apply()

        /*keys.forEach {
            mPreferences.edit().remove(it).apply()
        }*/

    }


    // --------------------------------------------------------------------------------------------
    fun getLoginType() : Int {
        return mPreferences.getInt(PREF_LOGIN_TYPE, LOGIN_TYPE_NOT_LOGGED_IN)
    }

    /**
     * Clears login related data from preferences.
     */
    fun clearLoginData() {
        val editor = mPreferences.edit()
        editor.remove(PREF_LOGIN_TYPE)
        editor.remove(PREF_LOGIN_ID)
        editor.remove(PREF_USER_ID)
        editor.remove(PREF_SECRET_KEY)
        editor.apply()
    }

    /**
     * Saves login related data into preferences.
     */
    fun putLoginData(userLoginType: Int, userLoginId: String?, userId: Int?, userKey: String?) {
        if (userLoginId == null || userId == null || userKey == null) {
            if (MyDebug.IS_DEBUG) MyDebug.showLog(TAG,"param is null")
            return
        }
        if (MyDebug.IS_DEBUG) {
            MyDebug.showLog(TAG,"put login data:" +
                    " userLoginType=" + userLoginType +
                    " userLoginId=" + userLoginId
            )
        }
        val editor = mPreferences.edit()
        editor.putInt(PREF_LOGIN_TYPE, userLoginType)
        editor.putString(PREF_LOGIN_ID, userLoginId)
        editor.putInt(PREF_USER_ID, userId)
        editor.putString(PREF_SECRET_KEY, userKey)
        editor.apply()
    }

    fun putRoamingData(roamingZone: RealRoamingZone) {
        val zonesSet = HashSet<String> (mPreferences.getStringSet(PREF_ROAMING_DATA, mutableSetOf())).toMutableSet()
        zonesSet += Gson().toJson(roamingZone)

        val editor = mPreferences.edit()
        editor.putStringSet(PREF_ROAMING_DATA, zonesSet)
        editor.apply()
    }

    fun getRoamingData(): List<RealRoamingZone> {
        val zonesSet = mPreferences.getStringSet(PREF_ROAMING_DATA, mutableSetOf()) as MutableSet<String>
        val gson = Gson()
        return zonesSet.map { gson.fromJson(it, RealRoamingZone::class.java) }
    }

    fun clearRoamingData() {
        val editor = mPreferences.edit()
        editor.remove(PREF_ROAMING_DATA)
        editor.apply()
    }

    fun putBlockingData(blockingData: BlockingEntry) {
        val editor = mPreferences.edit()
        editor.putString(PREF_BLOCKING_DATA, Gson().toJson(blockingData))
        editor.apply()
    }

    fun getBlockingData(): BlockingEntry? {
        val blockingDataStr = mPreferences.getString(PREF_BLOCKING_DATA, null)
        if(blockingDataStr != null)
            return Gson().fromJson(blockingDataStr, BlockingEntry::class.java)

        return null
    }

    fun clearBlockingData() {
        val editor = mPreferences.edit()
        editor.remove(PREF_BLOCKING_DATA)
        editor.apply()
    }

    fun getUserSettingNotification() : Boolean {
        return getUserBoolean(PREF_IS_NOTIFICATION_ON)
    }

    fun setUserSettingNotification(value: Boolean) {
        if (MyDebug.IS_DEBUG) MyDebug.showLog(TAG,"setUserSettingNotification="  + value)
        putUserData(PREF_IS_NOTIFICATION_ON, value)
    }

    fun getUserTelecomsVersion(): Int? {
        return getUserInt(PREF_TELECOMS_VERSION, 1)
    }

    fun setUserTelecomsUpdate(version: Int?) {
        version?.let {
            val currentVersion = getUserInt(PREF_TELECOMS_VERSION, 0)
            if (currentVersion < it) {
                putUserData(PREF_IS_UPDATE_TELECOMS, true)
                putUserData(PREF_TELECOMS_VERSION, version)
            } else {
                putUserData(PREF_IS_UPDATE_TELECOMS, false)
            }
        }
    }

    fun putUserData(keyName: String, value: Boolean?) {
        if (value == null) return
        val prefix: String
        when (getInt(PREF_LOGIN_TYPE)) {
            LOGIN_TYPE_EMAIL -> prefix = PREF_PREFIX_LOGIN_TYPE_EMAIL
            LOGIN_TYPE_FACEBOOK -> prefix = PREF_PREFIX_LOGIN_TYPE_FACEBOOK
            LOGIN_TYPE_GOOGLE -> prefix = PREF_PREFIX_LOGIN_TYPE_GOOGLE
            else -> {
                if (MyDebug.IS_DEBUG) MyDebug.showLog(TAG,"unknown type, putUserData, key=" + keyName + " value=" + value)
                return
            }
        }
        val userLoginId = getString(PREF_LOGIN_ID) 
        val userKeyName = userLoginId + prefix + keyName
        val editor = mPreferences.edit()
        editor.putBoolean(userKeyName, value)
        editor.apply()
    }

    fun putUserData(keyName: String, value: Int?) {
        if (value == null) return
        val prefix: String
        when (getInt(PREF_LOGIN_TYPE)) {
            LOGIN_TYPE_EMAIL -> prefix = PREF_PREFIX_LOGIN_TYPE_EMAIL
            LOGIN_TYPE_FACEBOOK -> prefix = PREF_PREFIX_LOGIN_TYPE_FACEBOOK
            LOGIN_TYPE_GOOGLE -> prefix = PREF_PREFIX_LOGIN_TYPE_GOOGLE
            else -> {
                if (MyDebug.IS_DEBUG) MyDebug.showLog(TAG,"unknown type, putUserData, key=" + keyName + " value=" + value)
                return
            }
        }
        val userLoginId = getString(PREF_LOGIN_ID)
        val userKeyName = userLoginId + prefix + keyName
        val editor = mPreferences.edit()
        editor.putInt(userKeyName, value)
        editor.apply()
    }

    fun putUserData(keyName: String, value: String?) {
        if (value == null) return
        val prefix: String
        when (getInt(PREF_LOGIN_TYPE)) {
            LOGIN_TYPE_EMAIL -> prefix = PREF_PREFIX_LOGIN_TYPE_EMAIL
            LOGIN_TYPE_FACEBOOK -> prefix = PREF_PREFIX_LOGIN_TYPE_FACEBOOK
            LOGIN_TYPE_GOOGLE -> prefix = PREF_PREFIX_LOGIN_TYPE_GOOGLE
            else -> {
                if (MyDebug.IS_DEBUG) MyDebug.showLog(TAG,"unknown type, putUserData, key=" + keyName + " value=" + value)
                return
            }
        }
        val userLoginId = getString(PREF_LOGIN_ID)
        val userKeyName = userLoginId + prefix + keyName
        val editor = mPreferences.edit()
        editor.putString(userKeyName, value)
        editor.apply()
    }

    fun getUserBoolean(keyName: String): Boolean {
        val prefix: String
        when (getInt(PREF_LOGIN_TYPE)) {
            LOGIN_TYPE_EMAIL -> prefix = PREF_PREFIX_LOGIN_TYPE_EMAIL
            LOGIN_TYPE_FACEBOOK -> prefix = PREF_PREFIX_LOGIN_TYPE_FACEBOOK
            LOGIN_TYPE_GOOGLE -> prefix = PREF_PREFIX_LOGIN_TYPE_GOOGLE
            else -> {
                if (MyDebug.IS_DEBUG) MyDebug.showLog(TAG,"getUserBoolean, unknown type, key="  + keyName)
                return false
            }
        }
        val userLoginId = getString(PREF_LOGIN_ID)
        val userKeyName = userLoginId + prefix + keyName
        return mPreferences.getBoolean(userKeyName, false)
    }

    fun getUserInt(keyName: String, defValue: Int): Int {
        val prefix: String
        when (getInt(PREF_LOGIN_TYPE)) {
            LOGIN_TYPE_EMAIL -> prefix = PREF_PREFIX_LOGIN_TYPE_EMAIL
            LOGIN_TYPE_FACEBOOK -> prefix = PREF_PREFIX_LOGIN_TYPE_FACEBOOK
            LOGIN_TYPE_GOOGLE -> prefix = PREF_PREFIX_LOGIN_TYPE_GOOGLE
            else -> {
                if (MyDebug.IS_DEBUG) MyDebug.showLog(TAG,"getUserInt, unknown type, key="  + keyName)
                return defValue
            }
        }
        val userLoginId = getString(PREF_LOGIN_ID)
        val userKeyName = userLoginId + prefix + keyName
        return mPreferences.getInt(userKeyName, defValue)
    }

    fun getUserString(keyName: String, defValue: String?): String? {
        val prefix: String
        when (getInt(PREF_LOGIN_TYPE)) {
            LOGIN_TYPE_EMAIL -> prefix = PREF_PREFIX_LOGIN_TYPE_EMAIL
            LOGIN_TYPE_FACEBOOK -> prefix = PREF_PREFIX_LOGIN_TYPE_FACEBOOK
            LOGIN_TYPE_GOOGLE -> prefix = PREF_PREFIX_LOGIN_TYPE_GOOGLE
            else -> {
                if (MyDebug.IS_DEBUG) MyDebug.showLog(TAG,"getUserString, unknown type, key="  + keyName)
                return defValue
            }
        }
        val userLoginId = getString(PREF_LOGIN_ID)
        val userKeyName = userLoginId + prefix + keyName
        return mPreferences.getString(userKeyName, defValue)
    }
}