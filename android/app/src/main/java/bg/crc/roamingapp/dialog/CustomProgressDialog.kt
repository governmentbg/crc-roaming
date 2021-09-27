package bg.crc.roamingapp.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import bg.crc.roamingapp.R

/**
 * Custom progress dialog.
 **/
class CustomProgressDialog(context: Context) : Dialog(context) {

    /**
     * set dialog view.
     **/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        setCancelable(false)
        setContentView(R.layout.dialog_progress_bar)
    }
}