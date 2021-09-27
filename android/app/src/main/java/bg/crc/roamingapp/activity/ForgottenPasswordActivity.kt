package bg.crc.roamingapp.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import bg.crc.roamingapp.R
import bg.crc.roamingapp.constant.AppUtils
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
import kotlinx.android.synthetic.main.activity_forgotten_password.*
import kotlinx.android.synthetic.main.header.*
import kotlinx.android.synthetic.main.internet_connectivity_view.*

/**
 * This screen display forgot password UI.
 * User can reset password using email.
 */
class ForgottenPasswordActivity : BaseActivity() {

    private var compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgotten_password)
        setListeners()
        init()
    }

    /**
     *set listeners
     **/
    private fun setListeners() {
        btnReset.setOnClickListener {
            if (!isValidEmail()) return@setOnClickListener
            apiCallForForgottenPassword()
        }

        ivBack.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    /**
     *This function for view visibility.
     * */
    private fun init() {
        ivBack.visibility = View.VISIBLE
    }

    /**
     *This function check for valid email.
     * */
    private fun isValidEmail(): Boolean {
        val strEmail: String = etEmail.text?.trim().toString()
        if (strEmail.isEmpty()) {
            etEmail.error = getString(R.string.error_email_required)
            etEmail.requestFocus()
            return false
        }
        if (!AppUtils.isValidEmail(strEmail.trim())) {
            etEmail.error = getString(R.string.error_invalid_email)
            etEmail.requestFocus()
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


    /**
     * This function request to server for generate password.
     * After success redirect to [LoginActivity].
     **/
    private fun apiCallForForgottenPassword() {
        val email = etEmail.text?.trim().toString()

        if (ConnectionDetector.isConnectedToInternet(this)) {
            try {
                pbForgottenPass.visibility = View.VISIBLE

                val parameter = RequestParameters.getForgottenPasswordParameter(email)
                val apiClient = ApiClient.getClient(this).create(RequestInterface::class.java)
                compositeDisposable.add(
                    apiClient.forgottenPassword(parameter)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({
                            pbForgottenPass.visibility = View.GONE
                            when (it.ec) {
                                ErrorHandler.OK -> {
                                    CustomDialog.Builder(this)
                                            .setTitle(getString(R.string.go_to_login))
                                            .setPositiveButton(getString(R.string.ok))
                                            .build(
                                                    getString(R.string.password_reset_successfully),
                                                    object : CustomDialog.DialogCallBack() {
                                                        override fun onPositiveButtonClick(dialog: CustomDialog) {
                                                            dialog.dismiss()
                                                            startActivity(Intent(this@ForgottenPasswordActivity, LoginActivity::class.java))
                                                            finish()
                                                        }
                                                    }).show()
//                                    startActivity(Intent(this, LoginActivity::class.java))
//                                    finish()
                                }
                                else -> {
                                    ErrorHandler.serverHandleError(this, it.ec, null, true)
                                }
                            }
                        }, { error ->
                            pbForgottenPass.visibility = View.GONE
                            if (MyDebug.IS_DEBUG) MyDebug.showLog("error", error.message)
                        })
                )
            } catch (e: Exception) {
                pbForgottenPass.visibility = View.GONE
                if (MyDebug.IS_DEBUG) MyDebug.showLog("catch", e.message)
            }
        } else {
            AppUtils.showInternetDialog(this)
        }
    }
}