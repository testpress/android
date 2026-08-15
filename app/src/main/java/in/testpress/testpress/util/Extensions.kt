package `in`.testpress.testpress.util

import android.text.TextUtils
import android.widget.TextView


fun TextView.isEmpty(): Boolean {
    return TextUtils.isEmpty(this.text)
}

fun TextView.isNotEmpty(): Boolean {
    return !this.isEmpty()
}