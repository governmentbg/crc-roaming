package bg.crc.roamingapp.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import bg.crc.roamingapp.R
import bg.crc.roamingapp.app.ApplicationSession
import bg.crc.roamingapp.constant.AppUtils
import bg.crc.roamingapp.constant.Constants
import bg.crc.roamingapp.constant.ErrorHandler
import bg.crc.roamingapp.debug.MyDebug
import bg.crc.roamingapp.dialog.CustomDialog
import bg.crc.roamingapp.server.ApiClient
import bg.crc.roamingapp.server.ConnectionDetector
import bg.crc.roamingapp.server.RequestInterface
import bg.crc.roamingapp.server.RequestParameters
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_change_password.*
import kotlinx.android.synthetic.main.header.*
import kotlinx.android.synthetic.main.header.view.*
import kotlinx.android.synthetic.main.internet_connectivity_view.*

class ChangePasswordActivity : BaseActivity() {

    private var compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        header_include.title.text = getString(R.string.title_change_password)
        setListeners()
        init()
    }

    /**
     *This function is initialized view and set password input type.
     * */
    private fun init() {
        ivBack.visibility = View.VISIBLE
        ivSettings.visibility = View.VISIBLE

        oldPassword.setAsPassword()
        newPassword.setAsPassword()
        etChangeRepeatPassword.setAsPassword()
    }

    /**
     *set listeners
     **/
    private fun setListeners() {
        btnChangePassword.setOnClickListener {
            if (!isValidUserInput()) {
                return@setOnClickListener
            }
            apiCallForChangePassword()
        }

        ivBack.setOnClickListener {
            HomeActivity.start(this)
            finish()
        }

        ivSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    /**
     *This function check for valid user input.
     * */
    private fun isValidUserInput(): Boolean {
        if (!isValidPassword()) return false
        if (!isValidRepeatPassword()) return false
        return true
    }

    /**
     *This function check for valid password.
     * */
    private fun isValidPassword(): Boolean {
        val strPassword: String = newPassword.text?.trim().toString()
        if (strPassword.isEmpty()) {
            newPassword.error = getString(R.string.error_password_required)
            newPassword.requestFocus()
            return false
        }

        if ((strPassword).length < Constants.PASSWORD_MIN_LENGTH) {
            newPassword.error =
                getString(R.string.error_password_length_pre) + " " + Constants.PASSWORD_MIN_LENGTH + " " + getString(
                    R.string.error_password_length_post
                )
            newPassword.requestFocus()
            return false
        }

        return true
    }

    /**
     *This function check for valid repeat password.
     * */
    private fun isValidRepeatPassword(): Boolean {
        val strPassword: String = newPassword.text?.trim().toString()
        val strRepeatPassword: String = etChangeRepeatPassword.text?.trim().toString()
        if (strRepeatPassword.isEmpty()) {
            etChangeRepeatPassword.requestFocus()
            etChangeRepeatPassword.error = getString(R.string.error_repeat_password_required)
            return false
        }

        if (!strPassword.equals(strRepeatPassword, false)) {
            etChangeRepeatPassword.requestFocus()
            etChangeRepeatPassword.error = getString(R.string.error_password_not_match)
            return false
        }
        return true
    }

    /**
     *This override function notify to user about internet connectivity.
     * */
    override fun onInternetConnectivityChange() {
        tvInternetConnection?.visibility = if (isInternetAvailable) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    override fun onBackPressed() {
        HomeActivity.start(this@ChangePasswordActivity)
        finish()
    }

    /**
     * This function sends change password request to the server.
     * Redirect to [SettingsActivity].
     * */
    private fun apiCallForChangePassword() {
        val oldPassword = oldPassword.text?.trim().toString()
        val newPassword = newPassword.text?.trim().toString()

        if (ConnectionDetector.isConnectedToInternet(this)) {
            try {
                pbChangePassword.visibility = View.VISIBLE

                val parameter = RequestParameters.getChangePasswordParameter(
                    ApplicationSession.getInt(Constants.PREF_USER_ID).toString(),
                    ApplicationSession.getString(Constants.PREF_SECRET_KEY),
                    oldPassword, newPassword)
                val apiClient = ApiClient.getClient(this).create(RequestInterface::class.java)
                compositeDisposable.add(
                    apiClient.changePassword(parameter)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({
                            pbChangePassword.visibility = View.GONE
                            when (it.ec) {
                                ErrorHandler.OK -> {
                                    CustomDialog.Builder(this)
                                        .setPositiveButton(getString(R.string.ok))
                                        .build(
                                            getString(R.string.password_change_success),
                                            object : CustomDialog.DialogCallBack() {
                                                override fun onPositiveButtonClick(dialog: CustomDialog) {
                                                    dialog.dismiss()
                                                    HomeActivity.start(this@ChangePasswordActivity)
                                                    finish()
                                                }
                                            }).show()
                                }
                                else -> {
                                    ErrorHandler.serverHandleError(this, it.ec, null, true)
                                }
                            }
                        }, { error ->
                            pbChangePassword.visibility = View.GONE
                            if (MyDebug.IS_DEBUG) MyDebug.showLog("error", error.message)
                        })
                )
            } catch (e: Exception) {
                pbChangePassword.visibility = View.GONE
                if (MyDebug.IS_DEBUG) MyDebug.showLog("catch", e.message)
            }
        } else {
            AppUtils.showInternetDialog(this)
        }
    }
}