package `in`.testpress.testpress.viewmodels

import `in`.testpress.testpress.BuildConfig
import `in`.testpress.testpress.TestpressApplication
import `in`.testpress.testpress.authenticator.RegisterActivity
import `in`.testpress.testpress.models.DaoSession
import `in`.testpress.testpress.models.InstituteSettings
import `in`.testpress.testpress.models.InstituteSettingsDao
import android.content.Context
import androidx.lifecycle.ViewModel

class RegisterViewModel(val context: Context) : ViewModel() {

    companion object {
        const val MOBILE = "M"
    }

    val daoSession: DaoSession = TestpressApplication.getDaoSession()
    val instituteSettingsDao: InstituteSettingsDao = daoSession.instituteSettingsDao
    val instituteSettingsList: MutableList<InstituteSettings> = instituteSettingsDao.queryBuilder()
            .where(InstituteSettingsDao.Properties.BaseUrl.eq(BuildConfig.BASE_URL))
            .list()

    var verificationMethod =
            if (instituteSettingsList[0].verificationMethod == MOBILE) {
                RegisterActivity.VerificationMethod.MOBILE
            } else {
                RegisterActivity.VerificationMethod.EMAIL
            }

    val isTwilioEnabled: Boolean = instituteSettingsList[0].twilioEnabled

}