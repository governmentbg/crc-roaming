package bg.crc.roamingapp.dialog

import android.app.Dialog
import android.content.Context
import android.opengl.Visibility
import android.os.Bundle
import android.view.View
import android.view.Window
import bg.crc.roamingapp.R
import bg.crc.roamingapp.constant.AppUtils
import bg.crc.roamingapp.constant.AppUtils.convertIntoHtmlText
import kotlinx.android.synthetic.main.custome_dialog.*
import kotlinx.android.synthetic.main.custome_dialog.view.*

/**
 * show the app general dialog with custom design.
 *  These dialog contain Title, Description, Positive and Negative buttons.
 *  These dialog also create by Builder pattern.
 * */
class CustomDialog(
    context: Context, private var descriptipn: String, private val callback: DialogCallBack?
) : Dialog(context, R.style.DialogStyle) {

    /**
     * work as intermediates between  positive and negative button clicks.
     **/
    abstract class DialogCallBack {
        /**
         * intermediate to positive button. as well as close the dialog.
         **/
        open fun onPositiveButtonClick(dialog: CustomDialog) {
            dialog.dismiss()
        }

        /**
         * intermediate to negative button. as well as close the dialog.
         **/
        open fun onNegativeButtonClick(dialog: CustomDialog) {
            dialog.dismiss()
        }
    }

    private var title: String? = null  // Title string of the dialog
    private var hasPositiveButton: Boolean = true
    private var positiveButtonName: String? = null // Positive button name of the dialog
    private var negativeButtonName: String? = null // Negative button name of the dialog.

    /**
     * set dialog view.
     * */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        setCancelable(true)
        setContentView(R.layout.custome_dialog)

        /*val displayMetrics = DisplayMetrics()
        window!!.windowManager.defaultDisplay.getMetrics(displayMetrics)

        window!!.setLayout((displayMetrics.widthPixels * 0.90).toInt(), RelativeLayout.LayoutParams.WRAP_CONTENT)*/

        setData()
    }

    private fun setData() {
        /**
         * make title visible and and set text if title is valid.
         **/
        if (AppUtils.appValidateString(title)) {
            tvTitle.text = title
            tvTitle.visibility = View.VISIBLE
        }

        /**
         * set description of dialog, also support html content.
         **/
        tvDescription.text = descriptipn.convertIntoHtmlText()
        tvPositiveButton.visibility = if (hasPositiveButton) View.VISIBLE else View.GONE

        /**
         * set text of positive button if text is valid.
         **/
        if (AppUtils.appValidateString(positiveButtonName)) {
            tvPositiveButton.text = positiveButtonName
        }

        /**
         * if [negativeButtonName] is valid then, set into the negative button and make it visible.
         **/
        if (AppUtils.appValidateString(negativeButtonName)) {
            tvNegativeButton.text = negativeButtonName
            tvNegativeButton.visibility = View.VISIBLE
        }

        /**
         * set listener for positive button. onclick of button it will fire event into the callback
         **/
        tvPositiveButton.setOnClickListener {
            callback?.onPositiveButtonClick(this) ?: dismiss()
        }

        /**
         * set listener for negative button. onclick of button it will fire event into the callback
         **/
        tvNegativeButton.setOnClickListener {
            callback?.onNegativeButtonClick(this) ?: dismiss()
        }
    }

    /**
     * set title text
     **/
    fun setTitle(title: String?) {
        this.title = title
        if (this.isShowing) {
            tvTitle.text = title
        }
    }

    fun changeDescription(description: String) {
        this.descriptipn = description
        if (this.isShowing) {
            descriptipn.convertIntoHtmlText()
        }
    }

    fun hasPositiveButton(hasPositiveButton: Boolean) {
        this.hasPositiveButton = hasPositiveButton
        if(this.isShowing)
            tvPositiveButton.visibility = if (hasPositiveButton) View.VISIBLE else View.GONE
    }

    /**
     * set positive button text
     **/
    fun setPositiveButton(positiveButtonName: String) {
        this.positiveButtonName = positiveButtonName
        if (this.isShowing) {
            tvPositiveButton.text = positiveButtonName
        }
    }

    /**
     * set negative button text
     **/
    fun setNegativeButton(negativeButtonName: String?) {
        this.negativeButtonName = negativeButtonName
        if (this.isShowing) {
            tvNegativeButton.text = negativeButtonName
        }
    }

    /**
     * Help to build custom dialog.
     * Builder can set title, "positive button" name and "negative button" name.
     **/
    class Builder(private val context: Context) {
        private var title: String? = null
        private var hasPositiveButton: Boolean = true
        private var positiveButtonName: String? = null
        private var negativeButtonName: String? = null


        fun setTitle(title: String?) = apply { this.title = title }
        fun hasPositiveButton(hasPositiveButton: Boolean) =
            apply { this.hasPositiveButton = hasPositiveButton }

        fun setPositiveButton(positiveButtonName: String) =
            apply { this.positiveButtonName = positiveButtonName }

        fun setNegativeButton(negativeButtonName: String) =
            apply { this.negativeButtonName = negativeButtonName }


        /**
         * build dialog.
         * @param  description is  necessary to in dialog.
         * @param callback
         *
         * */
        fun build(description: String, callback: DialogCallBack?): CustomDialog {
            val customDialog = CustomDialog(context, description, callback)
            if(!hasPositiveButton)
                customDialog.hasPositiveButton(false)

            if (positiveButtonName != null) {
                customDialog.setPositiveButton(positiveButtonName!!)
            }

            if (negativeButtonName != null) {
                customDialog.setNegativeButton(negativeButtonName!!)
            }

            if (title != null) {
                customDialog.setTitle(title)
            }

            return customDialog
        }

    }
}
