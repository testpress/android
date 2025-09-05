package `in`.testpress.testpress.authenticator

import `in`.testpress.core.TestpressSDKDatabase
import `in`.testpress.course.services.VideoDownloadService
import `in`.testpress.database.TestpressDatabase.Companion.invoke
import `in`.testpress.testpress.TestpressApplication
import `in`.testpress.testpress.R
import `in`.testpress.testpress.authenticator.LoginActivity.REQUEST_CODE_REGISTER_USER
import `in`.testpress.testpress.core.TestpressService
import `in`.testpress.testpress.enums.VerificationMethod
import `in`.testpress.testpress.models.InstituteSettings
import `in`.testpress.testpress.ui.fragments.AutoLoginFragment
import `in`.testpress.testpress.ui.fragments.EmailVerificationFragment
import `in`.testpress.testpress.ui.fragments.PhoneVerificationFragment
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.offline.DownloadService
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Inject

open class RegisterActivity : AppCompatActivity() {

    @Inject
    lateinit var testPressService: TestpressService

    private val instituteSettings = InstituteSettings.getInstance()

    private val verificationMethod = instituteSettings.verificationType

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TestpressApplication.getAppComponent().inject(this)
        setContentView(R.layout.fragment_layout)
        deleteDownloadedVideos(this)
        TestpressSDKDatabase.clearDatabase(this)
        initFragment()
    }

    private fun deleteDownloadedVideos(activity: Activity) {
        val executor: Executor = Executors.newSingleThreadExecutor()
        executor.execute { invoke(activity).clearAllTables() }
        DownloadService.sendRemoveAllDownloads(activity, VideoDownloadService::class.java, false)
    }

    private fun initFragment() {
        when (verificationMethod) {
            VerificationMethod.MOBILE -> navigateToPhoneVerificationFragment()
            VerificationMethod.EMAIL -> navigateToEmailVerificationFragment()
            VerificationMethod.NONE -> navigateToAutoLoginFragment()
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun navigateToPhoneVerificationFragment() {
        supportFragmentManager.beginTransaction().apply {
            add(R.id.container, PhoneVerificationFragment(), "PhoneVerificationFragment")
            commit()
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun navigateToEmailVerificationFragment() {
        supportFragmentManager.beginTransaction().apply {
            add(R.id.container, EmailVerificationFragment(), "EmailVerificationFragment")
            commit()
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun navigateToAutoLoginFragment() {
        supportFragmentManager.beginTransaction().apply {
            add(R.id.container, AutoLoginFragment(), "AutoLoginFragment")
            commit()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_REGISTER_USER) {
            setResult(resultCode)
            finish()
        }
    }
}
