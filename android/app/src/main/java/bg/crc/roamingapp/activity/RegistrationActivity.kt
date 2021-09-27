package bg.crc.roamingapp.activity

import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.Toast
import bg.crc.roamingapp.R
import bg.crc.roamingapp.app.ApplicationSession
import bg.crc.roamingapp.constant.AppUtils
import bg.crc.roamingapp.constant.Constants
import bg.crc.roamingapp.constant.ErrorHandler
import bg.crc.roamingapp.constant.ErrorHandler.serverHandleError
import bg.crc.roamingapp.debug.MyDebug
import bg.crc.roamingapp.dialog.CustomDialog
import bg.crc.roamingapp.server.ApiClient
import bg.crc.roamingapp.server.ConnectionDetector
import bg.crc.roamingapp.server.RequestInterface
import bg.crc.roamingapp.server.RequestParameters
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_registration.*
import kotlinx.android.synthetic.main.activity_registration.etEmail
import kotlinx.android.synthetic.main.activity_registration.etPassword
import kotlinx.android.synthetic.main.activity_registration.header_include
import kotlinx.android.synthetic.main.header.*
import kotlinx.android.synthetic.main.header.view.*
import kotlinx.android.synthetic.main.internet_connectivity_view.*

/**
 * This screen display Registration UI.
 * User can register into application using this screen by entering username,email,phone and password.
 */
class RegistrationActivity : BaseActivity() {

    private var compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        header_include.title.text = getString(R.string.title_registration)
        header_include.ivSettings.visibility = View.GONE
        setListeners()
        init()
    }

    /**
     *This function is initialized view and set password input type.
     * */
    private fun init() {
        ivBack.visibility = View.VISIBLE
        etPassword.setAsPassword()
        etRepeatPassword.setAsPassword()
    }

    /**
     *set listeners
     **/
    private fun setListeners() {
        btnRegister.setOnClickListener {
            if (!isValidUserInput()) {
                return@setOnClickListener
            }
            apiCallForEmailRegistration()
        }


        ivBack.setOnClickListener {
            startActivity(Intent(this, RegistrationTypeActivity::class.java))
            finish()
        }
    }


    /**
     *This function check for valid user input.
     * */
    private fun isValidUserInput(): Boolean {
        if (!isValidEmail()) return false
        if (!isValidPassword()) return false
        if (!isValidRepeatPassword()) return false
        return true
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
     *This function check for valid password.
     * */
    private fun isValidPassword(): Boolean {
        val strPassword: String = etPassword.text?.trim().toString()
        if (strPassword.isEmpty()) {
            etPassword.error = getString(R.string.error_password_required)
            etPassword.requestFocus()
            return false
        }

        if ((strPassword).length < Constants.PASSWORD_MIN_LENGTH) {
            etPassword.error =
                getString(R.string.error_password_length_pre) + " " + Constants.PASSWORD_MIN_LENGTH + " " + getString(
                    R.string.error_password_length_post
                )
            etPassword.requestFocus()
            return false
        }

        return true
    }

    /**
     *This function check for valid repeat password.
     * */
    private fun isValidRepeatPassword(): Boolean {
        val strPassword: String = etPassword.text?.trim().toString()
        val strRepeatPassword: String = etRepeatPassword.text?.trim().toString()
        if (strRepeatPassword.isEmpty()) {
            etRepeatPassword.requestFocus()
            etRepeatPassword.error = getString(R.string.error_repeat_password_required)
            return false
        }

        if (!strPassword.equals(strRepeatPassword, false)) {
            etRepeatPassword.requestFocus()
            etRepeatPassword.error = getString(R.string.error_password_not_match)
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
        startActivity(Intent(this, LoginTypeActivity::class.java))
        finish()
    }

    /**
     * This function request to server for user registration.
     * After success redirect to [LoginActivity].
     **/
    private fun apiCallForEmailRegistration() {
        val email = etEmail.text?.trim().toString()
        val password = etPassword.text?.trim().toString()

        if (ConnectionDetector.isConnectedToInternet(this)) {
            try {
                pbRegistration.visibility = View.VISIBLE
                ApplicationSession.clearLoginData()

                val parameter = RequestParameters.getRegistrationViaEmailParameter(email, password)
                val apiClient = ApiClient.getClient(this).create(RequestInterface::class.java)
                compositeDisposable.add(
                    apiClient.registrationViaEmail(parameter)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({
                            pbRegistration.visibility = View.GONE
                            when (it.ec) {
                                ErrorHandler.OK -> {
                                    CustomDialog.Builder(this)
                                        .setTitle(getString(R.string.go_to_login))
                                        .setPositiveButton(getString(R.string.ok))
                                        .build(
                                            getString(R.string.registered_successfully),
                                            object : CustomDialog.DialogCallBack() {
                                                override fun onPositiveButtonClick(dialog: CustomDialog) {
                                                    dialog.dismiss()
                                                    startActivity(Intent(this@RegistrationActivity, LoginActivity::class.java))
                                                    finish()
                                                }
                                            }).show()
                                }
                                else -> {
                                    serverHandleError(this, it.ec, null, true)
                                }
                            }
                        }, { error ->
                            pbRegistration.visibility = View.GONE
                            if (MyDebug.IS_DEBUG) MyDebug.showLog("error", error.message)
                        })
                )
            } catch (e: Exception) {
                pbRegistration.visibility = View.GONE
                if (MyDebug.IS_DEBUG) MyDebug.showLog("catch", e.message)
            }
        } else {
            AppUtils.showInternetDialog(this)
        }
    }
}