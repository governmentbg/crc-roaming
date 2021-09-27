package bg.crc.roamingapp.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import bg.crc.roamingapp.debug.MyDebug
import bg.crc.roamingapp.server.ConnectionDetector


abstract class BaseActivity : AppCompatActivity() {
    private val TAG = "RoamingProtect" //javaClass.simpleName
    private lateinit var SUBCLASS:String

    var isInternetAvailable = false

    //    companion object {
//        var isInternetAvailable = false
//    }
    override fun onCreate(savedInstanceState: Bundle?) {
        SUBCLASS = javaClass.simpleName
        if (MyDebug.IS_DEBUG) MyDebug.showLog(TAG, SUBCLASS + ".onCreate")
        super.onCreate(savedInstanceState)
        isInternetAvailable = ConnectionDetector.isConnectedToInternet(this)
    }

    fun <T : ViewDataBinding> bindContentView(@LayoutRes layoutRes: Int): T =
        DataBindingUtil.setContentView(this, layoutRes)

    override fun onResume() {
        super.onResume()
        /**
         * register a broadcast receiver to listen for connectivity changes
         */
        val intentFilter = IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
        registerReceiver(broadcastReceiverConnectionChanged, intentFilter)
    }

    override fun onPause() {
        /**
         * remove the broadcast receiver listening for connectivity changes
         */
        unregisterReceiver(broadcastReceiverConnectionChanged)
        super.onPause()
    }

    override fun onDestroy() {
        if (MyDebug.IS_DEBUG) MyDebug.showLog(TAG, SUBCLASS + ".onDestroy")
        super.onDestroy()
    }

    /**
     * Broadcast receiver for internet connectivity.
     */
    private val broadcastReceiverConnectionChanged = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            isInternetAvailable = ConnectionDetector.isConnectedToInternet(this@BaseActivity)
            onInternetConnectivityChange()
        }
    }

    open fun onInternetConnectivityChange() {}
}