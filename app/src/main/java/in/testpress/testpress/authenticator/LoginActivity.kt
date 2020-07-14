package `in`.testpress.testpress.authenticator

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.core.TestpressSdk
import `in`.testpress.core.TestpressSession
import `in`.testpress.testpress.BuildConfig.APPLICATION_ID
import `in`.testpress.testpress.BuildConfig.BASE_URL
import `in`.testpress.testpress.Injector
import `in`.testpress.testpress.R
import `in`.testpress.testpress.TestpressApplication
import `in`.testpress.testpress.core.Constants
import `in`.testpress.testpress.core.TestpressService
import `in`.testpress.testpress.events.UnAuthorizedErrorEvent
import `in`.testpress.testpress.models.InstituteSettings
import `in`.testpress.testpress.models.InstituteSettingsDao
import `in`.testpress.testpress.ui.MainActivity
import `in`.testpress.testpress.ui.PostActivity
import `in`.testpress.testpress.ui.WebViewActivity
import `in`.testpress.testpress.ui.WebViewActivity.*
import `in`.testpress.testpress.util.*
import `in`.testpress.testpress.util.UIUtils.getMenuItemName
import `in`.testpress.util.UIUtils
import `in`.testpress.util.ViewUtils
import android.accounts.Account
import android.accounts.AccountManager
import android.accounts.AccountManager.*
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo.IME_ACTION_SEND
import android.widget.EditText
import butterknife.ButterKnife
import com.afollestad.materialdialogs.GravityEnum
import com.afollestad.materialdialogs.MaterialDialog
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.github.kevinsawicki.wishlist.Toaster
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.GoogleApiClient
import com.squareup.otto.Bus
import kotlinx.android.synthetic.main.empty_view.*
import kotlinx.android.synthetic.main.login_activity.*
import org.greenrobot.eventbus.Subscribe
import java.io.IOException
import javax.inject.Inject

class LoginActivity: ActionBarAccountAuthenticatorActivity() {

    companion object {
        const val PARAM_CONFIRM_CREDENTIALS = "confirmCredentials"
        const val PARAM_USERNAME = "username"
        const val PARAM_AUTHTOKEN_TYPE = "authtokenType"

        const val REQUEST_CODE_REGISTER_USER = 1111
        const val REQUEST_CODE_GOOGLE_SIGN_IN = 2222

        private lateinit var accountManager: AccountManager
    }

    @Inject lateinit var testPressService: TestpressService

    @Inject lateinit var bus: Bus

    private lateinit var authToken: String
    private var authTokenType: String? = null
    private var internetConnectivityChecker = InternetConnectivityChecker(this)

    private var confirmCredentials: Boolean? = null
    private var username: String? = null
    private var password: String? = null
    private var requestNewAccount = false

    private lateinit var callbackManager: CallbackManager
    private lateinit var googleApiClient: GoogleApiClient
    private lateinit var instituteSettingsDao: InstituteSettingsDao
    private lateinit var instituteSettings: InstituteSettings

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        Injector.inject(this)

        accountManager = AccountManager.get(this)
        callbackManager = CallbackManager.Factory.create()

        getDataFromIntent()

        requestNewAccount = (username == null)

        if (AccessToken.getCurrentAccessToken() != null) {
            LoginManager.getInstance().logOut()
        }

        setContentView(R.layout.login_activity)

        ButterKnife.inject(this)
        UIUtils.setIndeterminateDrawable(this, pb_loading, 4)

        setEditorActionListener()
        initializeViews()
        setTextChangedListener()
        initFacebookSignIn()
        initGoogleSignIn()
        setUpInstituteSettings()
        setLoginLabel(instituteSettings)
        setVisibilityResendVerificationSMS(instituteSettings)
        setOnClickListeners()

    }

    private fun getDataFromIntent() {
        val intent = intent
        username = intent.getStringExtra(PARAM_USERNAME)
        authTokenType = intent.getStringExtra(PARAM_AUTHTOKEN_TYPE)
        confirmCredentials = intent.getBooleanExtra(PARAM_CONFIRM_CREDENTIALS, false)
    }

    private fun setEditorActionListener() {
        et_password.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == IME_ACTION_SEND) {
                signIn()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    private fun initializeViews() {
        et_username.setSingleLine()
        et_username.requestFocus()
        et_password.typeface = Typeface.DEFAULT
        et_password.transformationMethod = PasswordTransformationMethod()
        or.typeface = TestpressSdk.getRubikMediumFont(this)
    }

    private fun setTextChangedListener() {
        et_password.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                password_error.visibility = View.GONE
                s?.let {
                    password_textInput_layout.isPasswordVisibilityToggleEnabled = s.isNotEmpty()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })

        et_username.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                username_error.visibility = View.GONE
                if (et_username.text.toString().contains(" ")) {
                    et_username.setText((et_username.text.toString().replace(" ", "")))
                    et_username.text?.length?.let { et_username.setSelection(it) }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })
    }

    private fun  initFacebookSignIn() {
        fb_login_button.run {
            fb_login_button.invalidate()
            fb_login_button.setPermissions("email")
            fb_login_button.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult?) {
                    login_layout.visibility = View.GONE
                    result?.accessToken?.userId?.let {
                        username = it
                    }
                    if (result != null) {
                        authenticate(result.accessToken.userId,
                                result.accessToken.userId, TestpressSdk.Provider.FACEBOOK)
                    }
                }

                override fun onCancel() {
                }

                override fun onError(error: FacebookException?) {
                    if (error?.message?.contains("CONNECTION_FAILURE") == true) {
                        showAlert(getString(R.string.no_internet_try_again))
                    } else {
                        Log.e("Facebook sign in error", "check hashes")
                        showAlert(getString(R.string.something_went_wrong_please_try_after))
                    }
                }

            })
        }
    }

    private fun initGoogleSignIn() {
        val googleSignInOptions = GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN )
                .requestEmail()
                .requestIdToken(getString(R.string.server_client_id))
                .build()

        googleApiClient = GoogleApiClient.Builder(this)
                .enableAutoManage(this) { connectionResult ->
                    if (connectionResult.errorMessage != null) {
                        showAlert(connectionResult.errorMessage!!)
                    } else {
                        showAlert(connectionResult.toString())
                    }
                }
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                .build()
    }

    private fun setUpInstituteSettings() {
        val daoSession = TestpressApplication.getDaoSession()
        instituteSettingsDao = daoSession.instituteSettingsDao
        val instituteSettingsList = instituteSettingsDao.queryBuilder()
                .where(InstituteSettingsDao.Properties.BaseUrl.eq(BASE_URL))
                .list()

        if (instituteSettingsList.size == 0) {
            getInstituteSettings()
        } else {
            instituteSettings = instituteSettingsList[0]
            updateInstituteSpecificFields()
        }
    }

    private fun setLoginLabel(instituteSettings: InstituteSettings) {
        if (Strings.toString(getMenuItemName(R.string.label_username, instituteSettings)).isNotEmpty()) {
            username_textInput_layout.hint = getMenuItemName(R.string.label_username, instituteSettings)
        }

        if (Strings.toString(getMenuItemName(R.string.label_password, instituteSettings)).isNotEmpty()) {
            password_textInput_layout.hint = getMenuItemName(R.string.label_password, instituteSettings)
        }
    }

    private fun setVisibilityResendVerificationSMS(instituteSettings: InstituteSettings) {
        if (instituteSettings.verificationMethod == "M") {
            b_resend_activation.visibility = View.VISIBLE
        } else {
            b_resend_activation.visibility = View.GONE
        }
    }

    private fun getInstituteSettings() {
        pb_loading.visibility = View.VISIBLE
        login_layout.visibility = View.GONE
        object : SafeAsyncTask<InstituteSettings>() {
            override fun call(): InstituteSettings {
                return testPressService.instituteSettings
            }

            override fun onException(exception: java.lang.Exception?) {
                if (exception?.cause is IOException) {
                    setEmptyText(R.string.network_error, R.string.no_internet_try_again,
                            R.drawable.ic_error_outline_black_18dp)
                } else {
                    setEmptyText(R.string.network_error, R.string.try_after_sometime,
                            R.drawable.ic_error_outline_black_18dp)
                }
                pb_loading.visibility = View.GONE
                retry_button.setOnClickListener {
                    empty_container.visibility = View.GONE
                    getInstituteSettings()
                }
            }

            override fun onSuccess(instituteSettings: InstituteSettings?) {
                super.onSuccess(instituteSettings)
                instituteSettings?.baseUrl = BASE_URL
                instituteSettingsDao.insertOrReplace(instituteSettings)
                instituteSettings?.let {
                    this@LoginActivity.instituteSettings = it
                }
                updateInstituteSpecificFields()
                login_layout.visibility = View.VISIBLE
                pb_loading.visibility = View.GONE
            }
        }.execute()
    }

    private fun setEmptyText(title: Int, description: Int, left: Int) {
        empty_container.visibility = View.VISIBLE
        empty_title.setText(title)
        empty_title.setCompoundDrawablesWithIntrinsicBounds(left, 0, 0, 0)
        empty_description.setText(description)
    }

    private fun authenticate(userId: String, accessToken: String,
                             provider: TestpressSdk.Provider) {

        val settings = `in`.testpress.models.InstituteSettings(instituteSettings.baseUrl)
                .setBookmarksEnabled(instituteSettings.bookmarksEnabled)
                .setCoursesFrontend(instituteSettings.showGameFrontend)
                .setCoursesGamificationEnabled(instituteSettings.coursesEnableGamification)
                .setCommentsVotingEnabled(instituteSettings.commentsVotingEnabled)
                .setAccessCodeEnabled(false)

        TestpressSdk.initialize(this, settings, userId, accessToken, provider,
                object : TestpressCallback<TestpressSession>() {
                    override fun onSuccess(result: TestpressSession?) {
                        if (provider == TestpressSdk.Provider.FACEBOOK &&
                                Profile.getCurrentProfile() != null) {
                            username = Profile.getCurrentProfile().name
                        }
                        result?.token?.let {
                            authToken = it
                        }
                        testPressService.setAuthToken(authToken)
                        onAuthenticationResult(true)
                    }

                    override fun onException(exception: TestpressException?) {
                        login_layout.visibility = View.VISIBLE
                        if (exception?.isNetworkError == true) {
                            showAlert(getString(R.string.no_internet_try_again))
                        } else if (exception?.isClientError == true) {
                            if (exception.message?.isEmpty() == false) {
                                showAlert(exception.message!!)
                            } else {
                                showAlert(getString(R.string.invalid_username_or_password))
                            }
                        } else {
                            showAlert(getString(R.string.testpress_some_thing_went_wrong_try_again))
                        }
                    }
                })
    }

    fun onAuthenticationResult(result: Boolean) {
        if (result) {
            if (confirmCredentials == false) {
                finishLogin()
            } else {
                finishConfirmCredentials(true)
            }
        } else {
            Ln.d("onAuthenticationResult: failed to authenticate")
            if (requestNewAccount) {
                Toaster.showLong(this, R.string.message_auth_failed_new_account)
            } else {
                Toaster.showLong(this, R.string.message_auth_failed)
            }
        }
    }

    private fun finishLogin() {
        val sharedPreferences = getSharedPreferences(Constants.GCM_PREFERENCE_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean(GCMPreference.SENT_TOKEN_TO_SERVER, false).apply()

        CommonUtils.registerDevice(this, testPressService)
        val account = Account(username, APPLICATION_ID)

        if (requestNewAccount) {
            accountManager.addAccountExplicitly(account, password, null)
            accountManager.setAuthToken(account, APPLICATION_ID, authToken)
        } else {
            accountManager.setPassword(account, password)
        }

        val daoSession = TestpressApplication.getDaoSession()
        val postDao = daoSession.postDao
        postDao.deleteAll()
        daoSession.clear()
        if (authTokenType != null && authTokenType == APPLICATION_ID) {
            navigateToWebViewActivity()
        } else {
            navigateToMainActivity()
        }
        finish()
    }

    private fun navigateToWebViewActivity() {
        val intent = Intent()
        intent.putExtra(KEY_ACCOUNT_NAME, username)
        intent.putExtra(KEY_ACCOUNT_TYPE, APPLICATION_ID)
        intent.putExtra(KEY_AUTHTOKEN, authToken)
        setAccountAuthenticatorResult(intent.extras)
        setResult(WebViewActivity.RESULT_OK, intent)
    }

    private fun navigateToMainActivity() {
        var intent = Intent(this, MainActivity::class.java)
        if (getIntent().getStringExtra(Constants.DEEP_LINK_TO) != null) {
            when (getIntent().getStringExtra(Constants.DEEP_LINK_TO)) {
                Constants.DEEP_LINK_TO_POST -> {
                    intent =  Intent(this, PostActivity::class.java)
                    intent.putExtra(Constants.IS_DEEP_LINK, true)
                    intent.putExtras(getIntent().extras)
                }
                else -> {
                    intent =  Intent(this, MainActivity::class.java)
                }

            }
        }
        intent.flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    private fun finishConfirmCredentials(result: Boolean) {
        val account = Account(username, APPLICATION_ID)
        accountManager.setPassword(account, password)

        val intent = Intent()
        intent.putExtra(KEY_BOOLEAN_RESULT, result)
        setAccountAuthenticatorResult(intent.extras)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun updateInstituteSpecificFields() {
        ViewUtils.setGone(fb_login_button, !instituteSettings.facebookLoginEnabled)
        ViewUtils.setGone(google_sign_in_button, !instituteSettings.googleLoginEnabled)
        ViewUtils.setGone(social_sign_in_buttons, !instituteSettings.facebookLoginEnabled &&
                !instituteSettings.googleLoginEnabled)
        ViewUtils.setGone(signup, !instituteSettings.allowSignup)
    }


    override fun onResume() {
        super.onResume()
        bus.register(this)
    }

    override fun onPause() {
        super.onPause()
        bus.unregister(this)
    }

    private fun populated(editText: EditText): Boolean {
        return editText.length() > 0
    }

    @Subscribe
    private fun onUnAuthorizedErrorEvent(unAuthorizedErrorEvent: UnAuthorizedErrorEvent) {
        // Could not authorize for some reason.
        Toaster.showLong(this, R.string.message_bad_credentials)
    }

    private fun setOnClickListeners() {
        b_signin.setOnClickListener {
            signIn()
        }
        signup.setOnClickListener {
            signUp()
        }
        b_resend_activation.setOnClickListener {
            openResendVerificationCode()
        }
        forgot_password.setOnClickListener {
            verify()
        }
        google_sign_in_button.setOnClickListener {
            googleSignIn()
        }
    }

    fun signIn() {
        if (!populated(et_username)) {
            username_error.visibility = View.VISIBLE
            username_error.text = getString(R.string.empty_input_error)
        }
        if (!populated(et_password)) {
            password_error.visibility = View.VISIBLE
            password_error.text = getString(R.string.empty_input_error)
        }
        if (populated(et_username) && populated(et_password)) {
            if(internetConnectivityChecker.isConnected) {
                handleLogin(b_signin)
            } else {
                internetConnectivityChecker.showAlert()
            }
        }
    }

    private fun handleLogin(view: View) {
        if (requestNewAccount) {
            username = et_username.text.toString()
        }

        password = et_password.text.toString()

        username?.let { userName ->
            password?.let { password ->
                authenticate(userName, password, TestpressSdk.Provider.TESTPRESS)
            }
        }
    }

    fun signUp() {
        if(instituteSettings.customRegistrationEnabled != null && instituteSettings.customRegistrationEnabled) {
            val intent = Intent(this@LoginActivity, WebViewActivity::class.java)
            intent.putExtra(ACTIVITY_TITLE, "Register")
            intent.putExtra(SHOW_LOGOUT, "false")
            intent.putExtra(URL_TO_OPEN, "$BASE_URL/register/")
            startActivity(intent)
        }
        else if(internetConnectivityChecker.isConnected) {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            getIntent()?.extras?.let {
                intent.putExtras(it)
            }
            startActivityForResult(intent, REQUEST_CODE_REGISTER_USER)
        } else {
            internetConnectivityChecker.showAlert()
        }
    }

    fun openResendVerificationCode() {
        val intent = Intent(this, WebViewActivity::class.java)
        intent.putExtra(URL_TO_OPEN, "$BASE_URL/resend/")
        intent.putExtra(ACTIVITY_TITLE, "Resend Verification SMS")
        startActivity(intent)
    }

    fun verify() {
        if (internetConnectivityChecker.isConnected) {
            val intent = Intent(this, ResetPasswordActivity::class.java)
            startActivity(intent)
        } else {
            internetConnectivityChecker.showAlert()
        }
    }

    fun googleSignIn() {
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient)
        startActivityForResult(signInIntent, REQUEST_CODE_GOOGLE_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_REGISTER_USER) {
            if (resultCode == RESULT_OK) {
                finish()
            }
        } else if (requestCode == REQUEST_CODE_GOOGLE_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result != null && result.isSuccess) {
                //noinspection ConstantConditions
                username = if (result.signInAccount?.givenName == null ||
                        result.signInAccount!!.givenName!!.isEmpty()) {
                    result.signInAccount?.givenName.toString()
                } else {
                    result.signInAccount!!.email.toString()
                }
                result.signInAccount?.idToken?.let {idToken ->
                    result.signInAccount?.id?.let { id ->
                        authenticate(id, idToken, TestpressSdk.Provider.GOOGLE)
                    }
                }
            } else if (result?.status?.statusCode == CommonStatusCodes.NETWORK_ERROR) {
                showAlert(getString(R.string.no_internet_try_again))
            } else if (result?.status?.statusCode == CommonStatusCodes.DEVELOPER_ERROR) {
                showAlert(getString(R.string.google_sign_in_wrong_hash))
            } else if (result?.status?.statusCode == 12501) {
                Log.e("Google sign in error", "Might be wrong app certificate SHA1")
                showAlert(getString(R.string.something_went_wrong_please_try_after))
            } else {
                Log.e("Google sign in error", result?.status.toString())
            }
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun showAlert(alertMessage: String) {
        MaterialDialog.Builder(this)
                .content(alertMessage)
                .neutralText(R.string.ok)
                .neutralColorRes(R.color.primary)
                .buttonsGravity(GravityEnum.END)
                .show()
    }
}