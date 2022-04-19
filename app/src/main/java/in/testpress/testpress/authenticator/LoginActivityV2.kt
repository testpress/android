package `in`.testpress.testpress.authenticator

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.core.TestpressSdk
import `in`.testpress.core.TestpressSession
import `in`.testpress.enums.Status
import `in`.testpress.testpress.BuildConfig
import `in`.testpress.testpress.Injector
import `in`.testpress.testpress.R
import `in`.testpress.testpress.core.Constants
import `in`.testpress.testpress.core.TestpressService
import `in`.testpress.testpress.models.InstituteSettings
import `in`.testpress.testpress.repository.InstituteRepository
import `in`.testpress.testpress.ui.fragments.OTPVerificationFragment
import `in`.testpress.testpress.ui.fragments.PhoneAuthenticationFragment
import `in`.testpress.testpress.ui.fragments.UsernameAuthentication
import `in`.testpress.testpress.ui.view.LoadingDialog
import `in`.testpress.testpress.util.CommonUtils
import `in`.testpress.testpress.util.GCMPreference
import `in`.testpress.testpress.util.UIUtils
import `in`.testpress.testpress.viewmodel.LoginViewModel
import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import javax.inject.Inject


class LoginActivityV2: ActionBarAccountAuthenticatorActivity(), LoginNavigationInterface {
    lateinit var viewModel: LoginViewModel
    lateinit var loadingDialog: LoadingDialog
    lateinit var googleApiClient: GoogleSignInClient
    lateinit var instituteSettings: InstituteSettings
    lateinit var accountManager: AccountManager

    val handleGoogleSignIn = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        it?.data?.let { intent ->
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(intent)
            if (result?.isSuccess == true) {
                googleSignInAuthentication(result)
            }
        }
    }

    @Inject
    lateinit var testPressService: TestpressService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Injector.inject(this)
        setContentView(R.layout.container_layout_without_toolbar)
        loadingDialog = LoadingDialog(this)
        accountManager = AccountManager.get(this)
        initializeViewModel()
        fetchInstituteSettings()
        initializeGoogleSignIn()
    }

    private fun initializeViewModel() {
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LoginViewModel(
                    InstituteRepository(this@LoginActivityV2, testPressService)
                ) as T
            }
        }).get(LoginViewModel::class.java)
    }

    private fun fetchInstituteSettings() {
        viewModel.getInstituteSettings().observe(this, { resource ->
            when (resource.status) {
                Status.LOADING -> loadingDialog.showDialog()
                Status.SUCCESS -> {
                    instituteSettings = resource.data!!
                    loadingDialog.hideDialog()
                    if (3 in instituteSettings.allowedLoginMethods) {
                        goToPhoneAuthentication()
                    } else {
                        goToUsernameAuthentication()
                    }
                }
                Status.ERROR -> loadingDialog.hideDialog()
            }
        })
    }

    private fun initializeGoogleSignIn() {
        val googleSignInOptions = GoogleSignInOptions.Builder(
            GoogleSignInOptions.DEFAULT_SIGN_IN
        )
            .requestEmail()
            .requestIdToken(getString(R.string.server_client_id))
            .build()
        googleApiClient = GoogleSignIn.getClient(this, googleSignInOptions)
    }

    override fun goToUsernameAuthentication() {
        replaceFragment(UsernameAuthentication())
    }

    override fun goToPhoneAuthentication() {
        replaceFragment(PhoneAuthenticationFragment())
    }

    override fun signInWithGoogle() {
        handleGoogleSignIn.launch(googleApiClient.signInIntent)
    }

    override fun goToOTPVerification(phoneNumber: String, countryCode: String) {
        val otpVerificationFragement = OTPVerificationFragment()
        otpVerificationFragement.arguments = Bundle().apply {
            putString("phoneNumber", phoneNumber)
            putString("countryCode", countryCode)
        }
        replaceFragment(otpVerificationFragement)
    }

    private fun replaceFragment(fragment: Fragment) {
        val backStateName: String = fragment.javaClass.name
        val manager = supportFragmentManager
        val fragmentPopped: Boolean = manager.popBackStackImmediate(backStateName, 0)
        if (!fragmentPopped && manager.findFragmentByTag(backStateName) == null) { //fragment not in back stack, create it.
            val fragmentTransaction = manager.beginTransaction()
            fragmentTransaction.replace(R.id.fragment_container, fragment, backStateName)
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            fragmentTransaction.addToBackStack(backStateName)
            fragmentTransaction.commit()
        }
    }


    override fun onLoginSuccess(username: String, token: String) {
        val settings = getInstituteSettings()
        val session = TestpressSession(settings, token)
        TestpressSdk.setTestpressSession(this, session)
        testPressService.setAuthToken(token)
        addTokenToAccountManager(username, token)
        registerDevice()
        autoLogin(username, token)
    }

    private fun addTokenToAccountManager(username: String, token: String) {
        val account = Account(username, BuildConfig.APPLICATION_ID)
        accountManager.addAccountExplicitly(account, null, null)
        accountManager.setAuthToken(account, BuildConfig.APPLICATION_ID, token)
    }

    private fun registerDevice() {
        val sharedPreferences = getSharedPreferences(
            Constants.GCM_PREFERENCE_NAME, Context.MODE_PRIVATE
        )
        sharedPreferences.edit().putBoolean(GCMPreference.SENT_TOKEN_TO_SERVER, false)
            .apply()
        CommonUtils.registerDevice(this, testPressService)
    }

    private fun autoLogin(username: String, token: String) {
        val intent = Intent()
        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, username)
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, BuildConfig.APPLICATION_ID)
        intent.putExtra(AccountManager.KEY_AUTHTOKEN, token)
        setAccountAuthenticatorResult(intent.extras)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }


    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount == 1) {
            finish()
        } else {
            super.onBackPressed()
        }
    }

    private fun googleSignInAuthentication(result: GoogleSignInResult) {
        val userId = result.signInAccount!!.getId()
        val accessToken = result.signInAccount!!.getIdToken()
        val settings = getInstituteSettings()
        val username = if (!result.signInAccount!!.givenName.isNullOrEmpty()) {
            result.signInAccount!!.givenName
        } else {
            result.signInAccount!!.email
        }
        TestpressSdk.initialize(this, settings, userId, accessToken, TestpressSdk.Provider.GOOGLE,
            object : TestpressCallback<TestpressSession?>() {
                override fun onSuccess(response: TestpressSession?) {
                    val authToken = response?.token
                    if (authToken != null) {
                        onLoginSuccess(username, authToken)
                    }
                }

                override fun onException(e: TestpressException) {
                    if (e.isNetworkError) {
                        UIUtils.showAlert(this@LoginActivityV2, getString(R.string.no_internet_try_again))
                    } else if (e.isClientError) {
                        if (e.message?.isNotEmpty() == true) {
                            UIUtils.showAlert(this@LoginActivityV2, e.message!!)
                        } else {
                            UIUtils.showAlert(this@LoginActivityV2,getString(R.string.invalid_username_or_password))
                        }
                    } else {
                        UIUtils.showAlert(this@LoginActivityV2,getString(R.string.testpress_some_thing_went_wrong_try_again))
                    }
                }
            })
    }

    private fun getInstituteSettings(): `in`.testpress.models.InstituteSettings {
        val settings = `in`.testpress.models.InstituteSettings(instituteSettings.baseUrl)
            .setBookmarksEnabled(instituteSettings.bookmarksEnabled)
            .setCoursesFrontend(instituteSettings.showGameFrontend)
            .setCoursesGamificationEnabled(instituteSettings.coursesEnableGamification)
            .setCommentsVotingEnabled(instituteSettings.commentsVotingEnabled)
            .setAccessCodeEnabled(false)
        return settings
    }
}

interface LoginNavigationInterface {
    fun goToUsernameAuthentication()
    fun goToPhoneAuthentication()
    fun signInWithGoogle()
    fun goToOTPVerification(phoneNumber: String, countryCode: String)
    fun onLoginSuccess(username:String, token: String)
}