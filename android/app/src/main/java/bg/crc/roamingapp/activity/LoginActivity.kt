package bg.crc.roamingapp.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import bg.crc.roamingapp.R
import bg.crc.roamingapp.app.ApplicationSession
import bg.crc.roamingapp.app.ApplicationSession.LOGIN_TYPE_EMAIL
import bg.crc.roamingapp.constant.AppUtils
import bg.crc.roamingapp.constant.Constants
import bg.crc.roamingapp.constant.ErrorHandler
import bg.crc.roamingapp.debug.MyDebug
import bg.crc.roamingapp.helper.TelecomsHelper
import bg.crc.roamingapp.server.ApiClient
import bg.crc.roamingapp.server.ConnectionDetector
import bg.crc.roamingapp.server.RequestInterface
import bg.crc.roamingapp.server.RequestParameters
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.header.*
import kotlinx.android.synthetic.main.header.view.*
import kotlinx.android.synthetic.main.internet_connectivity_view.*

/**
 * This screen display Login UI.
 * User can login into application using this screen by entering email and password.
 */
class LoginActivity : BaseActivity() {
    private var compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        header_include.title.text = getString(R.string.title_login)
        header_include.ivSettings.visibility = View.GONE
        setListeners()
        init()
    }

    /**
     *set listeners
     **/
    private fun setListeners() {
        btnLogin.setOnClickListener {
            if (!isValidUserInput()) {
                return@setOnClickListener
            }
            apiCallForEmailLogin()
        }
        tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgottenPasswordActivity::class.java))
        }
        ivBack.setOnClickListener {
            startActivity(Intent(this, LoginTypeActivity::class.java))
        }
        ivSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    /**
     *This function Set view visibility and change password input type.
     * */
    private fun init() {
        etPassword.setAsPassword()
        ivBack.visibility = View.VISIBLE
    }

    /**
     *This function check for valid user input.
     * */
    private fun isValidUserInput(): Boolean {
        if (!isValidEmail()) return false
        if (!isValidPassword()) return false
        return true
    }

    /**
     *This function for email validation.
     **/
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
     *This function for password validation.
     **/
    private fun isValidPassword(): Boolean {
        val strPassword: String = etPassword.text?.trim().toString()
        if (strPassword.isEmpty()) {
            etPassword.error = getString(R.string.error_password_required)
            etPassword.requestFocus()
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
     * This function request to server for login via email
     * This function also store [Constants.PREF_USERID],[Constants.PREF_SECRET_KEY] and [Constants.PREF_IS_LOGIN] after getting success response.
     * After successfully response getting from server, check that any update in telecoms list version or not.
     * Redirect to [HomeActivity].
     * */
    private fun apiCallForEmailLogin() {
        val email = etEmail.text?.trim().toString()
        val password = etPassword.text?.trim().toString()

        if (ConnectionDetector.isConnectedToInternet(this)) {
            try {
                pbLogin.visibility = View.VISIBLE
                ApplicationSession.clearLoginData()

                val parameter = RequestParameters.getLoginViaEmailParameter(email, password)
                val apiClient = ApiClient.getClient(this).create(RequestInterface::class.java)
                compositeDisposable.add(
                    apiClient.loginViaEmail(parameter)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({
                            pbLogin.visibility = View.GONE
                            when (it.ec) {
                                ErrorHandler.OK -> {
                                    ApplicationSession.putLoginData(LOGIN_TYPE_EMAIL, email, it.id, it.key)
                                    ApplicationSession.setUserTelecomsUpdate(it.telcos_vn)
                                    TelecomsHelper.initUserTelecomSelection(false);
                                    HomeActivity.start(this)
                                    finish()
                                }
                                else -> {
                                    ErrorHandler.serverHandleError(this, it.ec, null, true)
                                }
                            }
                        }, { error ->
                            pbLogin.visibility = View.GONE
                            if (MyDebug.IS_DEBUG) MyDebug.showLog("error", error.message)
                        })
                )
            } catch (e: Exception) {
                pbLogin.visibility = View.GONE
                if (MyDebug.IS_DEBUG) MyDebug.showLog("catch", e.message)
            }
        } else {
            AppUtils.showInternetDialog(this)
        }
    }
}