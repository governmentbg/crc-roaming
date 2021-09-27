package bg.crc.roamingapp.constant

import bg.crc.roamingapp.app.ApplicationSession
import bg.crc.roamingapp.app.RoamingProtect
import bg.crc.roamingapp.constant.WarningRoamingZone.apiCallForGetRoamingZones
import bg.crc.roamingapp.debug.MyDebug
import bg.crc.roamingapp.models.reportromaningzone.ReportRoamingZoneDbgRequest
import bg.crc.roamingapp.models.reportromaningzone.ReportRoamingZoneDbgRequestItem
import bg.crc.roamingapp.server.ApiClient
import bg.crc.roamingapp.server.ConnectionDetector
import bg.crc.roamingapp.server.RequestInterface
import bg.crc.roamingapp.server.RequestParameters
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.lang.reflect.Type
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.max
import kotlin.math.min

/**
 * This object check user in Roaming Zone or not.
 * This object have [apiCallForGetRoamingZones] and notify to user about roaming zone.
 **/
object WarningRoamingZone {
//    val context = RoamingProtect.context
    var isInRoamingZone = false
    var zoneList = ArrayList<ArrayList<LatLng>>()
    var compositeDisposable = CompositeDisposable()
    var createZoneApiCallingCount = 0
    var timer = Timer()

    /**
     * Using user's current location check that, Is user in Roaming zone or not?
     **/
    fun checkIsUserInZone(lat: Double, lng: Double): Boolean {
        val zones = ApplicationSession.getString(Constants.PREF_ZONE_LIST)
        if (zoneList.isEmpty() && !AppUtils.appValidateString(zones)) {
            apiCallForGetRoamingZones()
        } else {
            val type: Type = object : TypeToken<ArrayList<ArrayList<LatLng>>>() {}.type
            zoneList = Gson().fromJson(zones, type)
        }

        for (i in 0 until zoneList.size) {
            val zVa = zoneList[i].get(0)
            val zVb = zoneList[i].get(1)
            val isUserInZone = (lng >= min(zVa.longitude, zVb.longitude)) &&
                        (lng <= max(zVa.longitude, zVb.longitude)) &&
                        (lat >= min(zVa.latitude, zVb.latitude)) &&
                        (lat <= max(zVa.latitude, zVb.latitude))

            if (isUserInZone) {
                isInRoamingZone = true
                if (MyDebug.IS_DEBUG) {
                    val va = arrayListOf(zoneList[i].get(0).latitude, zoneList[i].get(0).longitude)
                    val vb = arrayListOf(zoneList[i].get(1).latitude, zoneList[i].get(1).longitude)
                    val reportsdbg = ReportRoamingZoneDbgRequest()
                    reportsdbg.add(ReportRoamingZoneDbgRequestItem(va, vb))
                    apiCallForReportDbgRoamingZone(lng, lat, reportsdbg.joinToString(",") { Gson().toJson(it) })
                }
                break
            } else {
                isInRoamingZone = false
//                ApplicationSession.putData(Constants.PREF_IS_SECOND_TIME_ROAMING_ZONE_WARNING, false)
            }
        }
        return isInRoamingZone
    }

    /**
     * Send a request to server for getting the roaming zones.
     * The roaming zones are defined as rectangles.
     * Store all zone data in Preference.
     **/
    private fun apiCallForGetRoamingZones() {
        if (ConnectionDetector.isConnectedToInternet(RoamingProtect.context)) {
            try {
                val parameter = RequestParameters.getRoamingZonesParameter(
                    ApplicationSession.getString(Constants.PREF_SECRET_KEY),
                    ApplicationSession.getInt(Constants.PREF_USER_ID).toString()
                )
                val apiClient =
                    ApiClient.getClient(RoamingProtect.context)
                        .create(RequestInterface::class.java)
                compositeDisposable.add(
                    apiClient.getRoamingZones(parameter)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({
                            when (it.ec) {
                                ErrorHandler.OK -> {
                                    if (it.zones != null) {
                                        zoneList.clear()
                                        it.zones.forEach { zone ->
                                            val latlongVa = LatLng(
                                                (zone.va?.get(0) ?: 0.0),
                                                (zone.va?.get(1) ?: 0.0)
                                            )
                                            val latlongVb = LatLng(
                                                (zone.vb?.get(0) ?: 0.0),
                                                (zone.vb?.get(1) ?: 0.0)
                                            )
                                            AppUtils.getArrayOfLatLong(latlongVa, latlongVb)
                                            zoneList.add(
                                                AppUtils.getArrayOfLatLong(
                                                    latlongVa,
                                                    latlongVb
                                                )
                                            )
                                        }
                                        ApplicationSession.putData(Constants.PREF_ZONE_LIST, Gson().toJson(zoneList))
                                    }
                                }
                                else -> {
                                    ErrorHandler.serverHandleError(RoamingProtect.context, it.ec, null, true)
                                }
                            }
                        }, { error ->
                            if (MyDebug.IS_DEBUG) MyDebug.showLog("error", error.message)
                        })
                )
            } catch (e: Exception) {
                if (MyDebug.IS_DEBUG) MyDebug.showLog("catch", e.message)
            }
        } else {
            AppUtils.showInternetDialog(RoamingProtect.context)
        }

    }

    /**
     * This API for testing purpose.
     * This function request to server for report about roaming zone.
     **/
    private fun apiCallForReportDbgRoamingZone(long: Double, lat: Double, roamingdbgdata: String) {
        if (MyDebug.IS_DEBUG) {
            val timestamp = (Calendar.getInstance().timeInMillis / 1000).toString()
            if (ConnectionDetector.isConnectedToInternet(RoamingProtect.context)) {
                try {
                    val parameter = RequestParameters.getdbgZoneParameter(
                        timestamp,
                        long.toString(),
                        lat.toString(),
                        roamingdbgdata,
                        ApplicationSession.getString(Constants.PREF_SECRET_KEY),
                        ApplicationSession.getInt(Constants.PREF_USER_ID).toString()
                    )
                    val apiClient =
                        ApiClient.getClient(RoamingProtect.context).create(RequestInterface::class.java)
                    compositeDisposable.add(
                        apiClient.reportRoamingDbgZone(parameter)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe({
                                when (it.ec) {
                                    ErrorHandler.OK -> {
                                        if (MyDebug.IS_DEBUG) MyDebug.showLog(
                                            "serverResponse",
                                            it.ec.toString()
                                        )
                                    }
                                    else -> {
                                        ErrorHandler.serverHandleError(RoamingProtect.context, it.ec, null, true)
                                    }
                                }
                            }, { error ->
                                if (MyDebug.IS_DEBUG) MyDebug.showLog("error", error.message)
                            })
                    )
                } catch (e: Exception) {
                    if (MyDebug.IS_DEBUG) MyDebug.showLog("catch", e.message)
                }
            } else {
                AppUtils.showInternetDialog(RoamingProtect.context)
            }
        }
    }


    /**
     * Timer scheduling for 24 hours interval.
     **/
    internal class RoamingZonesTimer {
        fun scheduleApiCall(date: Calendar) {
            timer.scheduleAtFixedRate(
                ApiCallingTask(),
                date.time,
                Constants.GET_ROAMING_ZONE_API_CALLING_PERIOD
            )
        }
    }

    /**
     * This internal class for request to server [apiCallForGetRoamingZones] for getting roaming zone on 1 day interval.
     * Request to server[apiCallForGetRoamingZones] for getting roaming zone not sent,if user continuously in Roaming zone for 2 days.
     * If user not in Roaming then every day request sent to the server[apiCallForGetRoamingZones] for getting roaming zone.
     **/
    internal class ApiCallingTask : TimerTask() {
        override fun run() {
            if (isInRoamingZone) {
                createZoneApiCallingCount++
                if (createZoneApiCallingCount == 3) {
                    createZoneApiCallingCount = 0
                    apiCallForGetRoamingZones()
                }
            } else {
                apiCallForGetRoamingZones()
            }
        }
    }
}