package bg.crc.roamingapp.customview

import android.content.Context
import android.graphics.Rect
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import bg.crc.roamingapp.R
import com.google.android.material.textfield.TextInputEditText


class CustomTextInputEditText : TextInputEditText {
    private var mContext: Context
    private var attrs: AttributeSet? = null

    constructor(context: Context) : super(context) {
        mContext = context
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        mContext = context
        this.attrs = attrs
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        mContext = context
        this.attrs = attrs
    }

    fun setAsPassword() {
        isPassword = true
    }

    private var isPassword: Boolean = false


    /**
     * This override method check and set edit text focus.
     * This function also change input type of password.
     */
    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        if (focused) {
            if (this.text.toString() == " ") {
                this.setText("")
            }
            if (isPassword) {
                inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
                transformationMethod = PasswordTransformationMethod.getInstance()
            }
        } else {
            if (this.text.toString().isEmpty()) {
                this.setText(" ")
            }
            if (this.text.toString() == " ") {
                if (isPassword) {
                    inputType = InputType.TYPE_CLASS_TEXT
                    transformationMethod = null
                }
            }
        }
    }


    init {
        try {
            val a = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.OpenEdittext,
                0,
                0
            )
            isPassword = a.getBoolean(R.styleable.OpenEdittext_isPassword, false)
        } catch (e: Exception) {

        }

        setText(" ")
    }
}