package bg.crc.roamingapp.constant

import bg.crc.roamingapp.app.ApplicationSession
import bg.crc.roamingapp.app.RoamingProtect
import bg.crc.roamingapp.constant.WarningRoamingZoneEx.apiCallForGetRoamingZones
import bg.crc.roamingapp.debug.MyDebug
import bg.crc.roamingapp.models.reportromaningzone.Bulgaria
import bg.crc.roamingapp.models.roamingzones.Polygon
import bg.crc.roamingapp.server.ApiClient
import bg.crc.roamingapp.server.ConnectionDetector
import bg.crc.roamingapp.server.RequestInterface
import bg.crc.roamingapp.server.RequestParameters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.lang.reflect.Type
import java.util.*
import kotlin.collections.ArrayList

/**
 * This object check user in Roaming Zone or not.
 * This object have [apiCallForGetRoamingZones] and notify to user about roaming zone.
 **/
object WarningRoamingZoneEx {
    private val TAG = "RoamingProtect"
//    val context = RoamingProtect.context
    var isInRoamingZone = false
//    @SuppressLint("StaticFieldLeak")
    var zoneList = ArrayList<Polygon>()
    var compositeDisposable = CompositeDisposable()
    var createZoneApiCallingCount = 0
    var timer = Timer()

    /**
     * Using user's current location check that, Is user in Roaming zone or not?
     **/
    fun checkIsUserInZone(lat: Double, lng: Double): Boolean {
        val zones = ApplicationSession.getString(Constants.PREF_ZONE_LIST_EX)
        if (zoneList.isEmpty() && !AppUtils.appValidateString(zones)) {
            apiCallForGetRoamingZones()
        } else {
            val type: Type = object : TypeToken<ArrayList<Polygon>>() {}.type
            zoneList = Gson().fromJson(zones, type)
//if (MyDebug.IS_DEBUG) {
//    MyDebug.showLog(TAG, "read zones: zone count=" + (zoneList.size ?: 0))
//    if (zoneList != null) {
//        zoneList.forEach{z ->
//            MyDebug.showLog(TAG, "read zones: polygon(lat) vertex count=" + (z.lat ?: 0))
//            MyDebug.showLog(TAG, "read zones: polygon(lng) vertex count=" + (z.lng ?: 0))
//        }
//    }
//}
        }

        if (isPointInBulgaria(lat, lng)) {
            if (MyDebug.IS_DEBUG) MyDebug.showLog(TAG, "checkIsUserInZone, the point IS in Bulgaria ------")
            for (i in 0 until zoneList.size) {
                val isUserInZone = isPointInPolygon(lat, lng, zoneList.get(i))
                if (isUserInZone) {
                    isInRoamingZone = true
                    break
                } else {
                    isInRoamingZone = false
//                ApplicationSession.putData(Constants.PREF_IS_SECOND_TIME_ROAMING_ZONE_WARNING, false)
                }
            }
            if (MyDebug.IS_DEBUG) MyDebug.showLog(TAG, "checkIsUserInZone, the point is in roaming zone=" + isInRoamingZone)
        } else {
            // we don't collect points outside of Bulgaria
            if (MyDebug.IS_DEBUG) MyDebug.showLog(TAG, "checkIsUserInZone, the point is NOT in Bulgaria ------")
            isInRoamingZone = false
        }
        return isInRoamingZone
    }

    /**
     * Send a request to server for getting the roaming zones.
     * The roaming zones are defined as polygons.
     * Store all zone data in Preference.
     **/
    private fun apiCallForGetRoamingZones() {
        if (ConnectionDetector.isConnectedToInternet(RoamingProtect.context)) {
            try {
                val parameter = RequestParameters.getRoamingZonesParameterEx(
                    ApplicationSession.getString(Constants.PREF_SECRET_KEY),
                    ApplicationSession.getInt(Constants.PREF_USER_ID).toString()
                )
                val apiClient = ApiClient.getClient(RoamingProtect.context).create(RequestInterface::class.java)
                compositeDisposable.add(
                    apiClient.getRoamingZonesEx(parameter)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({
                            when (it.ec) {
                                ErrorHandler.OK -> {
                                    if (it.zones != null) {
                                        zoneList.clear()
                                        it.zones.forEach { zone ->
                                            if (zone.lat != null && zone.lng != null && zone.lat.size == zone.lng.size) {
                                                val lat = ArrayList<Double>()
                                                zone.lat.forEach{l -> lat.add(l)}
                                                val lng = ArrayList<Double>()
                                                zone.lng.forEach{l -> lng.add(l)}
                                                val polygon = Polygon(lat, lng)
                                                zoneList.add(polygon)
                                            }
                                        }
                                        if (MyDebug.IS_DEBUG) MyDebug.showLog(TAG, "save zones: " + Gson().toJson(zoneList))
                                        ApplicationSession.putData(Constants.PREF_ZONE_LIST_EX, Gson().toJson(zoneList))
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

    private fun isPointInBulgaria(lat: Double, lng: Double): Boolean {
        var collisionCount = 0;
        for (i in 0 until Bulgaria.lat.size-1) {
            val vertA = Vertex(Bulgaria.lat[i], Bulgaria.lng[i])
            val vertB = Vertex(Bulgaria.lat[i+1], Bulgaria.lng[i+1])
            if (raycastCollision(lat, lng, vertA, vertB)) {
                collisionCount++;
            }
        }
        return ((collisionCount % 2) == 1); // odd = inside, even = outside;
    }

    private fun isPointInPolygon(lat: Double, lng: Double, polygon: Polygon): Boolean {
        var collisionCount = 0;
        for (i in 0 until polygon.lat.size-1) {
            val vertA = Vertex(polygon.lat[i], polygon.lng[i])
            val vertB = Vertex(polygon.lat[i+1], polygon.lng[i+1])
            if (raycastCollision(lat, lng, vertA, vertB)) {
                collisionCount++;
            }
        }
        return ((collisionCount % 2) == 1); // odd = inside, even = outside;
    }

    private fun raycastCollision(pointLat: Double, pointLng: Double, vertA: Vertex, vertB: Vertex): Boolean {
        val aY = vertA.lat; // lat
        val aX = vertA.lng; // lng
        val bY = vertB.lat; // lat
        val bX = vertB.lng; // lng
        val pY = pointLat;
        val pX = pointLng;

        if ((aY > pY && bY > pY) || (aY < pY && bY < pY) || (aX < pX && bX < pX)) {
            return false;
        }

        if (aX == bX) { // vertical segment?
            // for a horizontal ray from the point to [right] infinity:
            // - if the point is to the left of the segment the ray crosses the segment
            // - if the point is to the right of the segment the ray doesn't cross the segment
            return pX < aX; //
        }

        val m = (aY - bY) / (aX - bX);
        val bee = (-aX) * m + aY;
        val x = (pY - bee) / m;

        return x > pX;
    }

    internal class Vertex(
            val lat: Double,
            val lng: Double
    )
}