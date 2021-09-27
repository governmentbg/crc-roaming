package bg.crc.roamingapp.constant

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import bg.crc.roamingapp.R
import bg.crc.roamingapp.activity.LoginTypeActivity
import bg.crc.roamingapp.dialog.CustomDialog
import kotlin.math.log

/**
 * This object handle server error.
 * */
object ErrorHandler {
    const val OK = 0
    private const val ERROR_UNKNOWN_ACTION = 1
    private const val ERROR_FAILED_ACTION = 3
    private const val ERROR_SQL_ERROR = 5
    private const val ERROR_MISSING_REQUIRED_PARAM = 18
    private const val ERROR_EMAIL_SENDING_FAILED = 29
    private const val ERROR_INVALID_SQUARE = 30
    private const val ERROR_USER_LOGGED_FROM_DEVICE = 31
    private const val ERROR_USER_UNAUTHORIZED = 401
    private const val ERROR_EMAIL_ALREADY_REGISTERED = 32
    private const val ERROR_WRONG_PASSWORD = 34

    fun serverHandleError(
        context: Context,
        errorCode: Int?,
        message: String?,
        isShowDialog: Boolean,
    ) {

        val showMessage: String = when {
            AppUtils.appValidateString(message) -> {
                message!!
            }
            errorCode != null -> {
                getErrorReason(errorCode, context)
            }
            else -> {
                context.getString(R.string.server_error)
            }
        }

        if ((errorCode == ERROR_USER_UNAUTHORIZED) && context !is LoginTypeActivity) {
            val dialog =
                CustomDialog.Builder(context).setTitle(context.getString(R.string.error))
                    .setPositiveButton(context.getString(R.string.go_to_login))
                    .build(
                        context.getString(R.string.unauthorized_user),
                        object : CustomDialog.DialogCallBack() {
                            override fun onPositiveButtonClick(dialog: CustomDialog) {
                                dialog.dismiss()
                                context.startActivity(
                                    Intent(
                                        context,
                                        LoginTypeActivity::class.java
                                    )
                                )
                                if (context is Activity) {
                                    context.finish()
                                }
                            }
                        })
            dialog.setCancelable(false)
            dialog.show()
        } else if (isShowDialog) {
            CustomDialog.Builder(context).setTitle(context.getString(R.string.error))
                .build(showMessage, null).show()
        } else {
            Toast.makeText(context, showMessage, Toast.LENGTH_LONG).show()
        }
    }

    private fun getErrorReason(errorCode: Int, context: Context): String {
        return when (errorCode) {
            ERROR_UNKNOWN_ACTION -> context.getString(R.string.unknown_action)
            ERROR_FAILED_ACTION -> context.getString(R.string.failed_action)
            ERROR_SQL_ERROR -> context.getString(R.string.sql_error)
            ERROR_MISSING_REQUIRED_PARAM -> context.getString(R.string.param_missing)
            ERROR_EMAIL_SENDING_FAILED -> context.getString(R.string.email_sending_fail)
            ERROR_WRONG_PASSWORD -> context.getString(R.string.wrong_password)
            ERROR_INVALID_SQUARE -> context.getString(R.string.invalid_square)
            ERROR_USER_LOGGED_FROM_DEVICE -> context.getString(R.string.already_login)
            ERROR_USER_UNAUTHORIZED -> context.getString(R.string.unauthorized_user)
            ERROR_EMAIL_ALREADY_REGISTERED -> context.getString(R.string.email_already_registered)
            else -> context.getString(R.string.server_error)
        }
    }
}