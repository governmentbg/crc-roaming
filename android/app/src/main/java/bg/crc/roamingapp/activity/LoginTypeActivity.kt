package bg.crc.roamingapp.activity

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.text.Spannable
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.text.style.UnderlineSpan
import android.view.View
import bg.crc.roamingapp.R
import bg.crc.roamingapp.app.ApplicationSession
import bg.crc.roamingapp.app.ApplicationSession.LOGIN_TYPE_FACEBOOK
import bg.crc.roamingapp.app.ApplicationSession.LOGIN_TYPE_GOOGLE
import bg.crc.roamingapp.constant.AppUtils
import bg.crc.roamingapp.constant.AppUtils.isLoginWithFacebook
import bg.crc.roamingapp.constant.Constants
import bg.crc.roamingapp.constant.Constants.EMAIL
import bg.crc.roamingapp.constant.Constants.PUBLIC_PROFILE
import bg.crc.roamingapp.constant.ErrorHandler
import bg.crc.roamingapp.debug.MyDebug
import bg.crc.roamingapp.dialog.CustomDialog
import bg.crc.roamingapp.helper.TelecomsHelper
import bg.crc.roamingapp.server.ApiClient
import bg.crc.roamingapp.server.ConnectionDetector
import bg.crc.roamingapp.server.RequestInterface
import bg.crc.roamingapp.server.RequestParameters
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_login_type.*
import kotlinx.android.synthetic.main.dialogue_permission_usage.*
import kotlinx.android.synthetic.main.internet_connectivity_view.*
import java.util.*


/**
 * This screen display Login Type UI.
 * User can login into application using this screen by facebook, google and email.
 */
class LoginTypeActivity : BaseActivity() {
    private var compositeDisposable = CompositeDisposable()

    // Api Calling common
    private val RC_SIGN_IN = 234
//    companion object {
//        private const val RC_SIGN_IN = 234
//    }

    private var callbackManager: CallbackManager = CallbackManager.Factory.create()

    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_type)

        val language = if(Locale.getDefault().language == "bg") "bg" else "en"
        val content = getString(R.string.privacy_policy_link)
        val s = Html.fromHtml("<a href=\"https://roaming.crc.bg/privacy-policy?lang=$language\">$content</a>", Html.FROM_HTML_MODE_COMPACT) as Spannable
        for (u in s.getSpans(0, s.length, URLSpan::class.java)) {
            s.setSpan(object : UnderlineSpan() {
                override fun updateDrawState(tp: TextPaint) {
                    tp.isUnderlineText = false
                    tp.color = resources.getColor(R.color.white, theme)
                }
            }, s.getSpanStart(u), s.getSpanEnd(u), 0)
        }
        tv_privacy_policy_login.text = s
        tv_privacy_policy_login.movementMethod = LinkMovementMethod.getInstance()

        init()
        setListeners()
    }

    private fun init() {
        initFacebookManager()
        initGoogle()

        displayPermissionUsage()
    }

    /**
     *display permission usage dialogue if first run
     **/
    private fun displayPermissionUsage() {
        val prefs = super.getSharedPreferences("bg.crc.roamingapp", MODE_PRIVATE)
        if(!prefs.getBoolean("hasRun", false)) {
            // DISPLAY PERMISSION USAGE
            permissionDialogueInclude.visibility = View.VISIBLE
            prefs.edit().putBoolean("hasRun", true).apply()
        }
    }

    /**
     *set listeners
     **/
    private fun setListeners() {
        btnEmailLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        btnOk.setOnClickListener {
            permissionDialogueInclude.visibility = View.GONE
        }

        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegistrationTypeActivity::class.java))
            finish()
        }

        btnGoogleLogin.setOnClickListener {
            if (ConnectionDetector.isConnectedToInternet(this)) {
                signInWithGoogle()
            } else {
                AppUtils.showInternetDialog(this)
            }
        }

        btnFacebookLogin.setOnClickListener {
            if (ConnectionDetector.isConnectedToInternet(this)) {
                LoginManager.getInstance().logInWithReadPermissions(
                    this,
                    listOf(PUBLIC_PROFILE, EMAIL)
                )
            } else {
                AppUtils.showInternetDialog(this)
            }
        }
    }

    /**
     * This function for facebook login.
     * Get email, token, userid after successfully login with facebook
     * After success redirect to [apiCallForFacebookLogin] for request to server.
     * */
    private fun initFacebookManager() {
        if (isLoginWithFacebook()) {
            AppUtils.logoutFacebook()
        }

        LoginManager.getInstance().registerCallback(callbackManager, object :
            FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                val accessToken = AccessToken.getCurrentAccessToken()
                val request: GraphRequest =
                    GraphRequest.newMeRequest(accessToken) { objectResponse, response ->
                        if (response.error == null) {
                            val email = objectResponse.optString("email")
                            val fbUserId = accessToken.userId
                            val fbToken = AccessToken.getCurrentAccessToken().token
                            apiCallForFacebookLogin(email, fbUserId, fbToken)
                        }
                    }
                val parameters = Bundle()
                parameters.putString("fields", "id,name,email")
                request.parameters = parameters
                request.executeAsync()
            }

            override fun onCancel() {
                if (MyDebug.IS_DEBUG) MyDebug.showLog("Facebook login", "canceled")
            }

            override fun onError(exception: FacebookException) {
                AppUtils.showToast(this@LoginTypeActivity, getString(R.string.failed_facebook_login))
                if (MyDebug.IS_DEBUG) MyDebug.showLog("Facebook login", exception.toString())
            }
        })
    }

    /**
     *This function for Configure Google Sign In.
     * */
    private fun initGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestId()
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        //Initialize firebase auth
        auth = Firebase.auth

        //sign out if user already sign in
        googleSignOut()
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching Intent
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful
                val account = task.getResult(ApiException::class.java)
                if (MyDebug.IS_DEBUG) MyDebug.showLog("Token", account.idToken)
                pbLoginType.visibility = View.VISIBLE
                firebaseAuthWithGoogle(account.id!!, account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed
                if (MyDebug.IS_DEBUG) MyDebug.showLog("catch", e.message)
            }
        }
    }



    /**
     *   This function authenticate credential with Firebase
     *   After success redirect to [apiCallForGoogleLogin] for request to server.
     **/
    private fun firebaseAuthWithGoogle(gglUserId: String, idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    //sign in successfully
                    apiCallForGoogleLogin(gglUserId, idToken)
                } else {
                    // If sign in fails
                    pbLoginType.visibility = View.GONE
                    AppUtils.showToast(this, getString(R.string.failed_google_login))
                    if (MyDebug.IS_DEBUG) MyDebug.showLog("catch", task.exception.toString())
                }
            }
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
     * This function request to server for login via facebook.
     * This function also store [Constants.PREF_USERID],[Constants.PREF_SECRET_KEY] and [Constants.PREF_SIGN_IN_WITH_SOCIAL_LOG_IN] in session after success.
     * After successfully response getting from server, check that any update in telecoms list version or not.
     * After success redirect to [HomeActivity].
     **/
    private fun apiCallForFacebookLogin(email: String, fbUserId: String, fbToken: String) {
        if (ConnectionDetector.isConnectedToInternet(this)) {
            try {
                pbLoginType.visibility = View.VISIBLE
                ApplicationSession.clearLoginData()

                val parameter = RequestParameters.getLoginViaFacebookParameter(
                    email,
                    fbUserId,
                    fbToken
                )
                val apiClient = ApiClient.getClient(this).create(RequestInterface::class.java)
                compositeDisposable.add(
                    apiClient.loginViaFacebook(parameter)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({
                            pbLoginType.visibility = View.GONE
                            when (it.ec) {
                                ErrorHandler.OK -> {
                                    ApplicationSession.putLoginData(LOGIN_TYPE_FACEBOOK, fbUserId, it.id, it.key)
                                    ApplicationSession.setUserTelecomsUpdate(it.telcos_vn)
                                    TelecomsHelper.initUserTelecomSelection(false);
                                    HomeActivity.start(this@LoginTypeActivity)
                                    finish()
                                }
                                else -> {
                                    ErrorHandler.serverHandleError(this, it.ec, null, true)
                                }
                            }
                        }, { error ->
                            pbLoginType.visibility = View.GONE
                            if (MyDebug.IS_DEBUG) MyDebug.showLog("error", error.message)
                        })
                )
            } catch (e: Exception) {
                pbLoginType.visibility = View.GONE
                if (MyDebug.IS_DEBUG) MyDebug.showLog("catch", e.message)
            }
        } else {
            AppUtils.showInternetDialog(this)
        }
    }

    /**
     * This function request to server for login via google.
     * This function also store [Constants.PREF_USERID],[Constants.PREF_SECRET_KEY] and [Constants.PREF_SIGN_IN_WITH_SOCIAL_LOG_IN] after success.
     * After successfully response getting from server, check that any update in telecoms list version or not.
     * After success redirect to [HomeActivity].
     * */
    private fun apiCallForGoogleLogin(gglUserId: String, token: String) {
        if (ConnectionDetector.isConnectedToInternet(this)) {
            try {
                pbLoginType.visibility = View.VISIBLE
                ApplicationSession.clearLoginData()

                val parameter = RequestParameters.getLoginViaGoogleParameter(token)
                val apiClient = ApiClient.getClient(this).create(RequestInterface::class.java)
                compositeDisposable.add(
                    apiClient.loginViaGoogle(parameter)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({
                            pbLoginType.visibility = View.GONE
                            when (it.ec) {
                                ErrorHandler.OK -> {
                                    ApplicationSession.putLoginData(LOGIN_TYPE_GOOGLE, gglUserId, it.id, it.key)
                                    ApplicationSession.setUserTelecomsUpdate(it.telcos_vn)
                                    TelecomsHelper.initUserTelecomSelection(false)
                                    HomeActivity.start(this@LoginTypeActivity)
                                    finish()
                                }
                                else -> {
                                    ErrorHandler.serverHandleError(this, it.ec, null, true)
                                }
                            }
                        }, { error ->
                            pbLoginType.visibility = View.GONE
                            if (MyDebug.IS_DEBUG) MyDebug.showLog("error", error.message)
                        })
                )
            } catch (e: Exception) {
                pbLoginType.visibility = View.GONE
                if (MyDebug.IS_DEBUG) MyDebug.showLog("catch", e.message)
            }
        } else {
            AppUtils.showInternetDialog(this)
        }
    }

    /**
    * In this function sign out from Firebase and Google
    **/
    private fun googleSignOut() {
        // Firebase sign out
        auth.signOut()
        // Google sign out
        googleSignInClient.signOut().addOnCompleteListener(this) {

        }
    }
}