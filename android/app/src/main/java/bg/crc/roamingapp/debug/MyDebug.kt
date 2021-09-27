package bg.crc.roamingapp.debug

import android.util.Log
import bg.crc.roamingapp.BuildConfig

/**
 * This object class mange and centralize debug mode.
 * this class show variant option and debug functionality.
 * */

object MyDebug {

    private var isDebugMode = "debug".equals(BuildConfig.BUILD_TYPE, true)

    private var IS_LOG_ENABLE = isDebugMode// for show log
    var IS_SERVER_LOG_ENABLE = isDebugMode// for show server log
    var IS_DEBUG = isDebugMode  // isDebug mode on
    var IS_MCC_MNC_EMULATION = false && isDebugMode // change MCC/MNC on the emulator when in roaming

    /**
     * if want to show log in different type
     * */
    enum class LogType {
        V, I, W, E, D
    }

    /**
     * this function shows log. if [IS_LOG_ENABLE] is true
     * */
    fun showLog(tag: String, message: String?) {
        if (IS_LOG_ENABLE) {
            Log.e(tag, message ?: "")
        }
    }

    /**
     * this function shows log with specified log type. if [IS_LOG_ENABLE] is true
     * */
    fun showLog(tag: String, message: String?, logType: LogType) {
        if (IS_LOG_ENABLE) {
            when (logType) {
                LogType.V -> Log.v(tag, message ?: "")
                LogType.I -> Log.i(tag, message ?: "")
                LogType.W -> Log.w(tag, message ?: "")
                LogType.E -> Log.e(tag, message ?: "")
                LogType.D -> Log.d(tag, message ?: "")
            }
        }

    }

    /**
     * this function shows log with specified log type. if [IS_SERVER_LOG_ENABLE] and [IS_LOG_ENABLE] is true
     * */
    fun showServerLog(tag: String, message: String?, logType: LogType) {
        if (IS_LOG_ENABLE && IS_SERVER_LOG_ENABLE) {

            when (logType) {
                LogType.V -> Log.v(tag, message ?: "")
                LogType.I -> Log.i(tag, message ?: "")
                LogType.W -> Log.w(tag, message ?: "")
                LogType.E -> Log.e(tag, message ?: "")
                LogType.D -> Log.d(tag, message ?: "")
            }

        }
    }

}