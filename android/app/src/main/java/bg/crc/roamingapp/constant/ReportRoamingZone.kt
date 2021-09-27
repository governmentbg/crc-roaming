package bg.crc.roamingapp.constant

import android.content.Context
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.VibrationEffect
import android.os.Vibrator
import bg.crc.roamingapp.R
import bg.crc.roamingapp.app.ApplicationSession
import bg.crc.roamingapp.app.ApplicationSession.PREF_BLOCK_TELECOMS
import bg.crc.roamingapp.app.RoamingProtect
import bg.crc.roamingapp.constant.AppUtils.getCountryName
import bg.crc.roamingapp.constant.AppUtils.getVaAndVbInArray
import bg.crc.roamingapp.constant.Constants.PREF_IN_ROAMING_PENDING_DATA
import bg.crc.roamingapp.constant.Constants.PREF_IS_SECOND_TIME_REAL_ROAMING_NOTIFICATION
import bg.crc.roamingapp.constant.Constants.PREF_USER_MCC_MNC_SIM1
import bg.crc.roamingapp.constant.Constants.PREF_USER_MCC_MNC_SIM2
import bg.crc.roamingapp.constant.Constants.PREF_USER_OP_MCC_MNC_SIM1
import bg.crc.roamingapp.constant.Constants.PREF_USER_OP_MCC_MNC_SIM2
import bg.crc.roamingapp.constant.Constants.PREF_USER_OP_NAME_SIM1
import bg.crc.roamingapp.constant.Constants.PREF_USER_OP_NAME_SIM2
import bg.crc.roamingapp.constant.Constants.ROAMING_NOTIFICATION_ID
import bg.crc.roamingapp.constant.ReportRoamingZone.apiCallForReportRoamingZone
import bg.crc.roamingapp.debug.MyDebug
import bg.crc.roamingapp.models.BlockingEntry
import bg.crc.roamingapp.models.reportromaningzone.RealRoamingZone
import bg.crc.roamingapp.models.reportromaningzone.ReportRoamingZoneRequest
import bg.crc.roamingapp.models.reportromaningzone.ReportRoamingZoneRequestItem
import bg.crc.roamingapp.notifications.NotificationForRoaming
import bg.crc.roamingapp.server.ApiClient
import bg.crc.roamingapp.server.ConnectionDetector
import bg.crc.roamingapp.server.RequestInterface
import bg.crc.roamingapp.server.RequestParameters
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*


/**
 * This object checks for Roaming area.
 * This object notifies to server [apiCallForReportRoamingZone] about roaming area.
 **/
object ReportRoamingZone {
    private const val TAG = "RoamingProtect"
//    val context = RoamingProtect.context
    private var compositeDisposable = CompositeDisposable()
//    private var timer=Timer()

    /**
     * This function checks if the user is in roaming or not.
     * If the user is in roaming then notify the user, depending on user settings.
     * Schedule a task to send the collected coordinates to the server [apiCallForReportRoamingZone].
     */
    fun checkRoamingNetwork(context: Context, lat: Double, long: Double) {
        if (isDeviceInRoaming(context)) {
            if(!AppUtils.isMiUi() && !AppUtils.isDataRoamingOn())
                return

            // the device IS in roaming
            val mccMncSim1 = ApplicationSession.getString(PREF_USER_MCC_MNC_SIM1)
            val opMccAndMncSim1 = ApplicationSession.getString(PREF_USER_OP_MCC_MNC_SIM1)
            val opNameSim1 = ApplicationSession.getString(PREF_USER_OP_NAME_SIM1)

            val mccMncSim2 = ApplicationSession.getString(PREF_USER_MCC_MNC_SIM2)
            val opMccAndMncSim2 = ApplicationSession.getString(PREF_USER_OP_MCC_MNC_SIM2)
            val opNameSim2 = ApplicationSession.getString(PREF_USER_OP_NAME_SIM2)
            // save the device location
            if (mccMncSim1.isNotEmpty()) { // is SIM1 in roaming?
                saveVaAndVb(mccMncSim1, opMccAndMncSim1, opNameSim1, lat, long)
            }
            if (mccMncSim2.isNotEmpty()) { // is SIM2 in roaming?
                saveVaAndVb(mccMncSim2, opMccAndMncSim2, opNameSim2, lat, long)
            }
            // get current user settings
            val isNotificationOn = ApplicationSession.getUserSettingNotification()
            // must send a notification?
            var sendNotification = false
            val isSecondTimeRealRoamingNotification = ApplicationSession.getBoolean(PREF_IS_SECOND_TIME_REAL_ROAMING_NOTIFICATION)
            //if (MyDebug.IS_DEBUG) MyDebug.showLog(TAG, "ReportRoamingZone" + " in roaming, isSecondTimeRealRoamingNotification="+isSecondTimeRealRoamingNotification)
            if (!isSecondTimeRealRoamingNotification) {
                // the user just went into roaming
                ApplicationSession.putData(PREF_IS_SECOND_TIME_REAL_ROAMING_NOTIFICATION, true)
                ApplicationSession.putData(PREF_IN_ROAMING_PENDING_DATA, false)
                // send the first notification? yes, if the user settings allows for
                sendNotification = isNotificationOn
                //if (MyDebug.IS_DEBUG) MyDebug.showLog(TAG, "ReportRoamingZone" + " the user went into roaming, sendNotification="+sendNotification)
            }
            else {
                // the user is still in roaming
                if (isNotificationOn) {
                    // notifications are ON, send a notification? yes, if first was not shown
                    sendNotification = !AppUtils.isNotificationShown(context, ROAMING_NOTIFICATION_ID)
                }
                else {
                    // notifications are OFF
                    NotificationForRoaming.deleteNotification(context)
                }
                //if (MyDebug.IS_DEBUG) MyDebug.showLog(TAG, "ReportRoamingZone" + " the user is still in roaming, sendNotification="+sendNotification)
            }
            // check if the user has selected this network(s) for tracking
            val isSelectedMccMncSim1 = checkMccMnc(mccMncSim1)
            val isSelectedMccMncSim2 = checkMccMnc(mccMncSim2)
            if (sendNotification) {
                sendNotification = isSelectedMccMncSim1 || isSelectedMccMncSim2
            }
            if (sendNotification) {
                val splitMccAndMncSim1 = mccMncSim1.split(" ")
                val splitMccAndMncSim2 = mccMncSim2.split(" ")
                val countryName1 = getCountryName(splitMccAndMncSim1[0]) // 0: MCC, 1: MNC
                val countryName2 = getCountryName(splitMccAndMncSim2[0]) // 0: MCC, 1: MNC
                var countryNames = ""
                // we are sure that at least one SIM card is in roaming, i.e. at least
                // one country name is non empty string
                if (isSelectedMccMncSim1 && countryName1.isNotEmpty()) {
                    countryNames = countryName1
                }
                if (isSelectedMccMncSim2 && countryName2.isNotEmpty()) {
                    countryNames = if (countryNames.isEmpty()) countryName2
                    else "$countryNames / $countryName2"
                }

                RoamingProtect.isNotificationRunning = true
                NotificationForRoaming.createRoamingNotification(context,
                    context.getString(R.string.roaming_alert_title),
                    String.format("%s %s", context.getString(R.string.roaming_alert_message), countryNames)
                )

                val alert: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ApplicationSession.mMediaPlayer?.stop()
                ApplicationSession.mMediaPlayer = MediaPlayer.create(context, alert)
                ApplicationSession.mMediaPlayer?.isLooping = true
                ApplicationSession.mMediaPlayer?.start()

                val v = context.applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
                val pattern = longArrayOf(0, 2500)
                val amps = intArrayOf(0, 255)
                v?.vibrate(VibrationEffect.createWaveform(pattern, amps,1))
            }
        } else {
            // the device is NOT in roaming
            endOfRoaming(context)
//            if (MyDebug.IS_DEBUG) {
//                val isSecondTimeRealRoamingNotification = ApplicationSession.getBoolean(PREF_IS_SECOND_TIME_REAL_ROAMING_NOTIFICATION)
//                MyDebug.showLog(TAG, "ReportRoamingZone" + " the user is NOT in roaming, isSecondTimeRealRoamingNotification="+isSecondTimeRealRoamingNotification)
//            }
        }
    }

    fun endOfRoaming(context: Context) {
        // end of the 'device in roaming' process,
        // stop the task of sending reports to the server
        // and flush the collected data (if any)
        try {
//            timer.cancel()
            // force data flushing
            val v = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
            v?.cancel()
            ApplicationSession.putData(PREF_IN_ROAMING_PENDING_DATA, true)
            sendRoamingDataToServer(context)
            NotificationForRoaming.deleteNotification(context)
            ApplicationSession.putData(PREF_IS_SECOND_TIME_REAL_ROAMING_NOTIFICATION, false)
        } catch (t: Throwable) {
            println(t)
        }
    }

    private fun isDeviceInRoaming(context: Context): Boolean {
        // the device might be a dual SIM (even tri SIM - not supported yet)
        ApplicationSession.clearData(PREF_USER_MCC_MNC_SIM1)
        ApplicationSession.clearData(PREF_USER_OP_MCC_MNC_SIM1)
        ApplicationSession.clearData(PREF_USER_OP_NAME_SIM1)
        ApplicationSession.clearData(PREF_USER_MCC_MNC_SIM2)
        ApplicationSession.clearData(PREF_USER_OP_MCC_MNC_SIM2)
        ApplicationSession.clearData(PREF_USER_OP_NAME_SIM2)

        val roamingStatus = ConnectionDetector.isDeviceInRoaming(context)
        if (roamingStatus.isRoamingSim1) {
            val mccSpaceMnc = roamingStatus.mccSim1 + " " + roamingStatus.mncSim1
            val opMccSpaceMnc = roamingStatus.opMccSim1 + " " + roamingStatus.opMncSim1
            val opName = roamingStatus.opNameSim1

            ApplicationSession.putData(PREF_USER_MCC_MNC_SIM1, mccSpaceMnc)
            ApplicationSession.putData(PREF_USER_OP_MCC_MNC_SIM1, opMccSpaceMnc)
            ApplicationSession.putData(PREF_USER_OP_NAME_SIM1, opName)

            if (MyDebug.IS_DEBUG) MyDebug.showLog(TAG, "ReportRoamingZone.isDeviceInRoaming sim1" +
                    " mcc=" + roamingStatus.mccSim1 + " mnc=" + roamingStatus.mncSim1 + " MccMnc=" + mccSpaceMnc)
        }
        if (roamingStatus.isRoamingSim2) {
            val mccSpaceMnc = roamingStatus.mccSim2 + " " + roamingStatus.mncSim2
            val opMccSpaceMnc = roamingStatus.opMccSim2 + " " + roamingStatus.opMncSim2
            val opName = roamingStatus.opNameSim2

            ApplicationSession.putData(PREF_USER_MCC_MNC_SIM2, mccSpaceMnc)
            ApplicationSession.putData(PREF_USER_OP_MCC_MNC_SIM2, opMccSpaceMnc)
            ApplicationSession.putData(PREF_USER_OP_NAME_SIM2, opName)

            if (MyDebug.IS_DEBUG) MyDebug.showLog(TAG, "ReportRoamingZone.isDeviceInRoaming sim2" +
                    " mcc=" + roamingStatus.mccSim2 + " mnc=" + roamingStatus.mncSim2 + " MccMnc=" + mccSpaceMnc)
        }
        return roamingStatus.isInRoaming
    }

    fun isSim1InRoaming(context: Context): Pair<Boolean, Pair<String?, String?>?> {
        ApplicationSession.clearData(PREF_USER_MCC_MNC_SIM1)
        ApplicationSession.clearData(PREF_USER_OP_MCC_MNC_SIM1)
        ApplicationSession.clearData(PREF_USER_OP_NAME_SIM1)

        val roamingStatus = ConnectionDetector.isDeviceInRoaming(context)
        if (roamingStatus.isRoamingSim1)
            return Pair(true, Pair(roamingStatus.mncSim1, roamingStatus.mccSim1))

        return Pair(false, null)
    }

    fun isSim2InRoaming(context: Context): Pair<Boolean, Pair<String?, String?>?> {
        ApplicationSession.clearData(PREF_USER_MCC_MNC_SIM2)
        ApplicationSession.clearData(PREF_USER_OP_MCC_MNC_SIM2)
        ApplicationSession.clearData(PREF_USER_OP_NAME_SIM2)

        val roamingStatus = ConnectionDetector.isDeviceInRoaming(context)
        if (roamingStatus.isRoamingSim2)
            return Pair(true, Pair(roamingStatus.mncSim2, roamingStatus.mccSim2))

        return Pair(false, null)
    }

    /**
     * Add data into[ReportRoamingZoneRequest] ArrayList
     * API calling request[apiCallForReportRoamingZone]
     */
    private fun sendRoamingDataToServer(context: Context) {
        if (!ApplicationSession.getBoolean(PREF_IN_ROAMING_PENDING_DATA)) {
            //if (MyDebug.IS_DEBUG) MyDebug.showLog(TAG, "sendRoamingDataToServer can NOT send data now")
            return
        }

        val realRoamingZones = ApplicationSession.getRoamingData()
        if (realRoamingZones.isEmpty()) {
            //if (MyDebug.IS_DEBUG) MyDebug.showLog(TAG, "sendRoamingDataToServer NO data, empty list, exit")
            return
        }

        var opMcc: String? = null
        var opMnc: String? = null
        var opName: String? = null
        val reports = ReportRoamingZoneRequest()
        for (zone in realRoamingZones) {
//            if (MyDebug.IS_DEBUG) MyDebug.showLog(TAG, "sendRoamingDataToServer" +
//                    " ts=" + zone.ts + " mcc=" + zone.mcc + " mnc=" + zone.mnc +
//                    " zone (" + zone.latVa + ", " + zone.lngVa +") (" + zone.latVb + ", " + zone.lngVb + ")")

            if(opMcc == null || opMnc == null || opName == null) {
                opMcc = zone.opMcc
                opMnc = zone.opMnc
                opName = zone.opName
            }
            reports.add(
                ReportRoamingZoneRequestItem(
                        zone.ts,
                        zone.mcc,
                        zone.mnc,
                        getVaAndVbInArray(zone.latVa, zone.lngVa),
                        getVaAndVbInArray(zone.latVb, zone.lngVb))
                )
        }

        apiCallForReportRoamingZone(context, Gson().toJson(reports), opMcc, opMnc, opName)
        ApplicationSession.getBlockingData()?.let { apiCallForAddBlocking(context, it) }
    }

    /**
     * Store Va and Vb for report to the server. Where Va and Vb is lat-longs.
     */
    private fun saveVaAndVb(mccAndMnc: String, opMccAndMnc: String, opName: String, lat: Double, lng: Double) {
        if (MyDebug.IS_DEBUG) MyDebug.showLog(TAG,
            "saveVaAndVb MccMnc=$mccAndMnc lat=$lat lng=$lng"
        )
        if (mccAndMnc.isEmpty()) return

        val splitMCCAndMNC = mccAndMnc.split(" ")
        val mcc = splitMCCAndMNC[0]
        val mnc = splitMCCAndMNC[1]
        val splitOpMccAndMnc = opMccAndMnc.split(" ")
        val opMcc = splitOpMccAndMnc[0]
        val opMnc = splitOpMccAndMnc[1]

        val ts = Calendar.getInstance().timeInMillis / 1000 // timestamp in seconds
        // add new entry, Va and Vb are the same
        val zone = RealRoamingZone(ts, mcc, mnc, opMcc, opMnc, opName, lat, lng, lat, lng)
        ApplicationSession.putRoamingData(zone)
        if (MyDebug.IS_DEBUG) MyDebug.showLog(TAG, "saveVaAndVb, create zone" +
                " ts=" + ts + " mcc=" + mcc + " mnc=" + mnc +
                " (" + zone.latVa +", " + zone.lngVa + ") (" + zone.latVb +", " + zone.lngVb + ")")
    }

    /**
     * This function checks user's MCC-MNC code against the user's list of selected telecoms.
     * Returns true if match has been found.
     */
    private fun checkMccMnc(userMccMNc: String): Boolean {
        var isMatchMCCMNC = false
        val selectedTelecoms = AppUtils.getUserSelectedTelecomsArrayList(PREF_BLOCK_TELECOMS)
        if (selectedTelecoms != null && userMccMNc.isNotEmpty()) {
            for (i in 0 until selectedTelecoms.size) {
                if (selectedTelecoms[i].equals(userMccMNc)) {
                    isMatchMCCMNC = true
                    break
                }
            }
        }
        return isMatchMCCMNC
    }

    /**
     * This function request to server for report about roaming zone.
     */
    private fun apiCallForReportRoamingZone(context: Context, roamingData: String, opMcc: String?, opMnc: String?, opName: String?) {
        if (MyDebug.IS_DEBUG) MyDebug.showLog(TAG,
            "apiCallForReportRoamingZone, data: $roamingData, op: $opMcc;$opMnc;$opName"
        )
        try {
            val parameter = RequestParameters.getReportRoamingZoneParameter(
                roamingData,
                opMcc,
                opMnc,
                opName,
                ApplicationSession.getString(Constants.PREF_SECRET_KEY),
                ApplicationSession.getInt(Constants.PREF_USER_ID).toString()
            )

            if (MyDebug.IS_DEBUG) MyDebug.showLog(TAG,
                "apiCallForReportRoamingZone, data: $roamingData"
            )
            val apiClient = ApiClient.getClient(context).create(RequestInterface::class.java)
            compositeDisposable.add(
                apiClient.reportRoamingZone(parameter)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({
                        when (it.ec) {
                            ErrorHandler.OK -> {
                                ApplicationSession.putData(PREF_IN_ROAMING_PENDING_DATA, false)
                                ApplicationSession.clearRoamingData()
                                if (MyDebug.IS_DEBUG) MyDebug.showLog(TAG, "apiCallForReportRoamingZone, data sent successfully")
                            }
                            else -> {
                                if (MyDebug.IS_DEBUG) MyDebug.showLog(TAG, "apiCallForReportRoamingZone, Server Error, ec=" + it.ec)
//                                    ErrorHandler.serverHandleError(context,
//                                        it.ec, null, true)
                            }
                        }
                    }, { error ->
                        if (MyDebug.IS_DEBUG) MyDebug.showLog(TAG, "apiCallForReportRoamingZone, Error\n" + error.message)
                    })
            )
        } catch (e: Exception) {
            if (MyDebug.IS_DEBUG) MyDebug.showLog(TAG, "apiCallForReportRoamingZone, Exception\n" + e.message)
        }
    }

    private fun apiCallForAddBlocking(context: Context, blockingData: BlockingEntry) {
        val parameter = RequestParameters.getReportBlockingParameter(
            ApplicationSession.getInt(Constants.PREF_USER_ID).toString(),
            ApplicationSession.getString(Constants.PREF_SECRET_KEY),
            blockingData.mnc,
            blockingData.mcc
        )
        val apiClient = ApiClient.getClient(context).create(RequestInterface::class.java)
        compositeDisposable.add(
            apiClient.reportBlocking(parameter)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    when(it.ec) {
                        ErrorHandler.OK -> {
                            ApplicationSession.clearBlockingData()
                            if (MyDebug.IS_DEBUG) MyDebug.showLog("RoamingProtect", "setRoamingDataOnOff, data sent successfully")
                        }
                        else -> {
                            if (MyDebug.IS_DEBUG) MyDebug.showLog("RoamingProtect", "setRoamingDataOnOff, Server Error, ec=" + it.ec)
                        }
                    }
                }, { error ->
                    if (MyDebug.IS_DEBUG) MyDebug.showLog("RoamingProtect", "setRoamingDataOnOff, Error\n" + error.message)
                })
        )
    }
}

