@file:Suppress("DEPRECATION")

package bg.crc.roamingapp.server

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import bg.crc.roamingapp.debug.MyDebug
import bg.crc.roamingapp.models.roamingzones.RoamingStatus

object ConnectionDetector {
    private val TAG = "RoamingProtect"

    /**
     * This function checks if the device is connected to the internet or not.
     * The internet connection can be via WiFi or via GSM.
     *
     * @param context use to get system service object of ConnectivityManager
     */
    fun isConnectedToInternet(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        if (connectivityManager != null) {
            @Suppress("DEPRECATION")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val network = connectivityManager.activeNetwork
                if (network != null) {
                    val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
                    if (networkCapabilities != null) {
                        return (
//                                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_ROAMING) && // needs API 28
                                (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                                 networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))
                                )
                    }
                }
            } else {
                val info = connectivityManager.allNetworkInfo
                for (networkInfo in info) {
                    if (networkInfo.state == NetworkInfo.State.CONNECTED) {
                        return true
                    }
                }
            }
        }
        return false
    }

    fun isConnectedToHomeInternet(context: Context): Boolean {
        val hasInternet = isConnectedToInternet(context)
        val roamingStatus = isDeviceInRoaming(context)
        return hasInternet && !roamingStatus.isInRoaming
    }

    fun isDeviceInRoaming(context: Context): RoamingStatus {
        // the device might be a dual SIM (even tri SIM - not supported yet)
        var isInRoaming = false
        var inRoamingSim1 = false
        var inRoamingSim2 = false
        var mncSim1:String? = null
        var opMncSim1:String? = null
        var mccSim1:String? = null
        var opMccSim1:String? = null
        var opNameSim1:String? = null
        var mncSim2:String? = null
        var opMncSim2:String? = null
        var mccSim2:String? = null
        var opMccSim2:String? = null
        var opNameSim2:String? = null
        val permission: String = Manifest.permission.READ_PHONE_STATE
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
            val activeSubscriptionInfoList = subscriptionManager.activeSubscriptionInfoList
            if (activeSubscriptionInfoList != null && activeSubscriptionInfoList.isNotEmpty()) {
                var sim = 0
                for (subscriptionInfo in activeSubscriptionInfoList) {
                    sim++
                    val id = subscriptionInfo.subscriptionId
                    val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager?
                    if (telephonyManager != null) {
                        val tm = telephonyManager.createForSubscriptionId(id)
                        if (tm.isNetworkRoaming) {
                            if (MyDebug.IS_DEBUG) MyDebug.showLog(TAG, "isDeviceInRoaming" +
                                    " the device is in roaming, MccMnc=" + tm.networkOperator + " sim=" + sim)
                            // tm.simOperator returns <mcc as 3 digits><mnc as 2 or 3 digits>

                            var mcc = tm.networkOperator.substring(0, 3)
                            var mnc = tm.networkOperator.substring(3)
                            val opMcc = tm.simOperator.substring(0, 3)
                            val opMnc = tm.simOperator.substring(3)
                            val opName = tm.simOperatorName
                            if (MyDebug.IS_MCC_MNC_EMULATION) {
                                // emulate roaming with neighbour country
                                if (sim == 1) { mcc = "220"; mnc = "03" } // mcc='Serbia', mnc='MTS/Telekom Srbija'
                                if (sim == 2) { mcc = "226"; mnc = "03" } // mcc='Romania', mnc='Cosmote' */
                                MyDebug.showLog(TAG, "isDeviceInRoaming" + " emulate MCC/MNC, MccMnc=" +
                                        tm.networkOperator + " sim=" + sim)
                            }
                            if (sim == 1) {
                                inRoamingSim1 = true
                                mncSim1 = mnc
                                opMncSim1 = opMnc
                                mccSim1 = mcc
                                opMccSim1 = opMcc
                                opNameSim1 = opName
                            }
                            if (sim == 2) {
                                inRoamingSim2 = true
                                mncSim2 = mnc
                                opMncSim2 = opMnc
                                mccSim2 = mcc
                                opMccSim2 = opMcc
                                opNameSim2 = opName
                            }
                        }
                    }
                }
                isInRoaming = inRoamingSim1 || inRoamingSim2
            }
        }
        if (!isInRoaming) {
            if (MyDebug.IS_DEBUG) MyDebug.showLog(TAG, "isDeviceInRoaming the device is NOT in roaming")
        }
        return RoamingStatus(isInRoaming, inRoamingSim1, inRoamingSim2,
            mccSim1, opMccSim1, mncSim1, opMncSim1, opNameSim1, mccSim2, opMccSim2, mncSim2, opMncSim2, opNameSim2)
    }
}