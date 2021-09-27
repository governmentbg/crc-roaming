package bg.crc.roamingapp.dialog

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import bg.crc.roamingapp.R
import bg.crc.roamingapp.activity.*
import bg.crc.roamingapp.app.ApplicationSession
import bg.crc.roamingapp.constant.AppUtils
import bg.crc.roamingapp.constant.Constants.GPS_ALERT_NOTIFICATION_ID
import bg.crc.roamingapp.constant.Constants.PREF_SECRET_KEY
import bg.crc.roamingapp.constant.Constants.PREF_USER_ID
import bg.crc.roamingapp.constant.Constants.TRACKING_NOTIFICATION_ID
import bg.crc.roamingapp.constant.ErrorHandler
import bg.crc.roamingapp.debug.MyDebug
import bg.crc.roamingapp.helper.LocationTrackerHelper
import bg.crc.roamingapp.server.ApiClient
import bg.crc.roamingapp.server.ConnectionDetector
import bg.crc.roamingapp.server.RequestInterface
import bg.crc.roamingapp.server.RequestParameters
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_menu.*


/**
 * Show Bottom Sheet Dialog.
 **/
class BottomSheetFragment : BottomSheetDialogFragment() {
    private var fragmentView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentView = inflater.inflate(R.layout.activity_menu, container, false)
        return fragmentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    /**
     * This function set listeners.
     * This function also set basic layout. We set the transparent background color.
     **/
    private fun initView() {
        dialog?.setOnShowListener { dialog ->
            val d = dialog as BottomSheetDialog
            val bottomSheetInternal = d.findViewById<View>(R.id.design_bottom_sheet)
            bottomSheetInternal?.setBackgroundColor(Color.TRANSPARENT)
//            BottomSheetBehavior.from(bottomSheetInternal!!).peekHeight =
//                ((context?.resources?.displayMetrics?.heightPixels ?: 0) * 0.90).toInt()
        }

        btnHome.setOnClickListener {
            dialog?.dismiss()
            val intent = Intent(context, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            context?.startActivity(intent)
        }

        btnLogout.setOnClickListener {
            val alert = AlertDialog.Builder(requireActivity()).setTitle(getString(R.string.logout))
                .setMessage(getString(R.string.txt_want_to_logout))
                .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                    dialog.dismiss()
                }
                .setPositiveButton(getString(R.string.logout)) { dialog, _ ->
                    dialog.dismiss()
                    apiCallForLogout(requireActivity())
                }
            alert.show()
        }
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
                                ApplicationSession.getString(PREF_SECRET_KEY),
                                ApplicationSession.getInt(PREF_USER_ID).toString())
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
                                            nm.cancel(TRACKING_NOTIFICATION_ID)
                                            nm.cancel(GPS_ALERT_NOTIFICATION_ID)
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

//    private fun closeApp() {
//        activity?.finish()
//    }
}