package `in`.testpress.testpress.authenticator

import `in`.testpress.enums.Status
import `in`.testpress.testpress.Injector
import `in`.testpress.testpress.R
import `in`.testpress.testpress.core.TestpressService
import `in`.testpress.testpress.repository.InstituteRepository
import `in`.testpress.testpress.ui.fragments.OTPVerificationFragement
import `in`.testpress.testpress.ui.fragments.PhoneAuthenticationFragment
import `in`.testpress.testpress.ui.fragments.UsernameAuthentication
import `in`.testpress.testpress.ui.view.LoadingDialog
import `in`.testpress.testpress.viewmodel.LoginViewModel
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.kevinsawicki.wishlist.Toaster
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import javax.inject.Inject


class LoginActivityV2: ActionBarAccountAuthenticatorActivity(), LoginNavigationInterface {
    lateinit var viewModel: LoginViewModel
    lateinit var loadingDialog: LoadingDialog
    lateinit var googleApiClient: GoogleSignInClient
    val handleGoogleSignIn = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        it?.data?.let { intent ->
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(intent)
            val s = "${result?.isSuccess} ${result?.status?.statusCode} ${result?.status?.statusMessage}"
            Toaster.showLong(this, s)
        }
    }

    @Inject
    lateinit var testPressService: TestpressService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Injector.inject(this)
        setContentView(R.layout.container_layout_without_toolbar)
        loadingDialog = LoadingDialog(this)
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
                    loadingDialog.hideDialog()
                    goToPhoneAuthentication()
                }
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

    override fun onLoginSuccess(intent: Intent) {
        setAccountAuthenticatorResult(intent.extras)
    }

    override fun goToOTPVerification(phoneNumber: String, countryCode: String) {
        val otpVerificationFragement = OTPVerificationFragement()
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

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount == 1) {
            finish()
        } else {
            super.onBackPressed()
        }
    }
}

interface LoginNavigationInterface {
    fun goToUsernameAuthentication()
    fun goToPhoneAuthentication()
    fun signInWithGoogle()
    fun goToOTPVerification(phoneNumber: String, countryCode: String)
    fun onLoginSuccess(intent:Intent)
}