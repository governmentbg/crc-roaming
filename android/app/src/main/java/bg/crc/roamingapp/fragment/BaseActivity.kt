package bg.crc.roamingapp.fragment

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import bg.crc.roamingapp.R
import bg.crc.roamingapp.activity.ChangePasswordActivity
import bg.crc.roamingapp.activity.LoginTypeActivity
import bg.crc.roamingapp.app.ApplicationSession
import bg.crc.roamingapp.constant.AppUtils
import bg.crc.roamingapp.constant.Constants
import bg.crc.roamingapp.constant.ErrorHandler
import bg.crc.roamingapp.debug.MyDebug
import bg.crc.roamingapp.dialog.CustomProgressDialog
import bg.crc.roamingapp.helper.LocationTrackerHelper
import bg.crc.roamingapp.server.ApiClient
import bg.crc.roamingapp.server.ConnectionDetector
import bg.crc.roamingapp.server.RequestInterface
import bg.crc.roamingapp.server.RequestParameters
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

abstract class BaseActivity : Fragment(), PopupMenu.OnMenuItemClickListener {

    private lateinit var fragmentBinding: ViewDataBinding
    var isInternetAvailable = false

    abstract val layoutResId: Int
    protected inline fun <reified T : ViewDataBinding> bindingLazy(): BindingLazy<T> = BindingLazy()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        context?.let { isInternetAvailable = ConnectionDetector.isConnectedToInternet(it) }
        return if (layoutResId == 0) {
            throw IllegalArgumentException("Please setup layoutResId")
        } else {
            fragmentBinding = DataBindingUtil.inflate(inflater, layoutResId, container, false)
            fragmentBinding.root
        }
    }

    fun showPopup(v: View?) {
        val popup = PopupMenu(requireContext(), v)
        popup.setOnMenuItemClickListener(this)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.profile_menu, popup.menu)

        if(ApplicationSession.getLoginType() != ApplicationSession.LOGIN_TYPE_EMAIL) {
            popup.menu.removeItem(R.id.menu_change_password)
        }

        popup.show()
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        item?.let {
            when (it.itemId) {
                R.id.menu_change_password -> {
                    startActivity(Intent(requireContext(), ChangePasswordActivity::class.java))
                    return true
                }
                R.id.menu_logout -> {
                    val alert = AlertDialog.Builder(requireActivity()).setTitle(getString(R.string.logout))
                        .setMessage(getString(R.string.txt_want_to_logout))
                        .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                            dialog.dismiss()
                        }
                        .setPositiveButton(getString(R.string.logout)) { dialog, _ ->
                            dialog.dismiss()
                            apiCallForLogout(requireContext())
                        }
                    alert.show()

                    return true
                }
                else -> return false
            }
        }

        return false
    }

    private val broadcastReceiverConnectionChanged = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            isInternetAvailable = ConnectionDetector.isConnectedToInternet(context)
            onInternetConnectivityChange()
        }
    }

    open fun onInternetConnectivityChange() {}

    @Suppress("UNCHECKED_CAST")
    protected inner class BindingLazy<out T : ViewDataBinding> : Lazy<T> {

        override val value: T
            get() = fragmentBinding as T

        override fun isInitialized() = ::fragmentBinding.isInitialized
    }

    /**
     * This function request to server for logout.
     * After success, clear some stored data in session, stop location service and redirect to [LoginTypeActivity].
     **/
    private fun apiCallForLogout(context: Context) {
        val logoutUserProgressDialog = CustomProgressDialog(context)
        if (ConnectionDetector.isConnectedToInternet(context)) {
            try {
                AppUtils.showAndHideDialog(logoutUserProgressDialog, true)
                val parameter = RequestParameters.getLogoutParameter(
                    ApplicationSession.getString(Constants.PREF_SECRET_KEY),
                    ApplicationSession.getInt(Constants.PREF_USER_ID).toString())
                val apiClient = ApiClient.getClient(context).create(RequestInterface::class.java)
                AppUtils.compositeDisposable.add(
                    apiClient.logout(parameter)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({
                            when (it.ec) {
                                ErrorHandler.OK -> {
                                    AppUtils.showAndHideDialog(logoutUserProgressDialog, false)
                                    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                                    nm.cancel(Constants.TRACKING_NOTIFICATION_ID)
                                    nm.cancel(Constants.GPS_ALERT_NOTIFICATION_ID)
                                    LocationTrackerHelper.stopLocationService()
                                    ApplicationSession.clearLoginData()
                                    val intent = Intent(context, LoginTypeActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    context.startActivity(intent)
//                                            closeApp()
                                }
                                else -> {
                                    AppUtils.showAndHideDialog(logoutUserProgressDialog, false)
                                    ErrorHandler.serverHandleError(context, it.ec, null, true)
                                }
                            }
                        }, { error ->
                            AppUtils.showAndHideDialog(logoutUserProgressDialog, false)
                            if (MyDebug.IS_DEBUG) MyDebug.showLog("error", error.message)
                        })
                )
            } catch (e: Exception) {
                AppUtils.showAndHideDialog(logoutUserProgressDialog, false)
                if (MyDebug.IS_DEBUG) MyDebug.showLog("catch", e.message)
            }
        } else {
            AppUtils.showAndHideDialog(logoutUserProgressDialog, false)
            AppUtils.showInternetDialog(context)
        }
    }
}