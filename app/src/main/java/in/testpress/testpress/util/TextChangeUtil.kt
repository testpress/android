package `in`.testpress.testpress.util

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.google.android.material.textfield.TextInputLayout

object TextChangeUtil {

    fun hideErrorMessageOnTextChange(editText: EditText, errorText: TextView) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                errorText.visibility = View.GONE
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
    }

    fun showPasswordToggleOnTextChange(textView: TextView, errorText: TextView, inputLayout: TextInputLayout) {
        textView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable) {
                errorText.visibility = View.GONE
                inputLayout.isPasswordVisibilityToggleEnabled = s.isNotEmpty()
            }
        })

    }
}
