package `in`.testpress.testpress.util

import `in`.testpress.testpress.authenticator.RegisterActivity
import `in`.testpress.testpress.models.InstituteSettings
import `in`.testpress.testpress.authenticator.RegisterActivity.VerificationMethod.EMAIL
import `in`.testpress.testpress.authenticator.RegisterActivity.VerificationMethod.MOBILE
import `in`.testpress.testpress.authenticator.RegisterActivity.VerificationMethod.NONE

class VerificationType {
    fun get(instituteSettings: InstituteSettings): RegisterActivity.VerificationMethod {
        return when(instituteSettings.verificationMethod) {
            "NV" -> NONE
            "M" -> MOBILE
            else -> EMAIL
        }
    }
}