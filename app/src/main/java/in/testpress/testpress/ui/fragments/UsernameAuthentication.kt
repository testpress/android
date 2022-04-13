package `in`.testpress.testpress.ui.fragments

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.core.TestpressSdk
import `in`.testpress.core.TestpressSession
import `in`.testpress.testpress.BuildConfig
import `in`.testpress.testpress.Injector
import `in`.testpress.testpress.R
import `in`.testpress.testpress.authenticator.LoginActivityV2
import `in`.testpress.testpress.authenticator.ResetPasswordActivity
import `in`.testpress.testpress.core.Constants
import `in`.testpress.testpress.core.TestpressService
import `in`.testpress.testpress.models.InstituteSettings
import `in`.testpress.testpress.util.CommonUtils
import `in`.testpress.testpress.util.GCMPreference
import `in`.testpress.testpress.util.UIUtils
import `in`.testpress.testpress.util.isEmpty
import `in`.testpress.util.ViewUtils
import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import com.facebook.Profile
import kotlinx.android.synthetic.main.username_login_layout.*
import javax.inject.Inject


class UsernameAuthentication : BaseAuthenticationFragment() {
    lateinit var accountManager: AccountManager
    private val instituteSettings = InstituteSettings.getInstance()

    @Inject
    lateinit var testPressService: TestpressService

    lateinit var activity: LoginActivityV2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Injector.inject(this)
        accountManager = AccountManager.get(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.username_login_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeInputFields()
        initializeButtons()
        updateLabels()
        showOrHideButtons()
    }

    private fun initializeInputFields() {
        password.transformationMethod = PasswordTransformationMethod()
        password.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    signIn()
                    true
                }
                else -> false
            }
        }

        username.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (username.text.toString().contains(" ")) {
                    username.setText(username.text.toString().replace(" ", ""))
                    username.setSelection(username.text?.length ?: 0)
                }
            }
        })
        username.requestFocus()
    }

    private fun initializeButtons() {
        val primaryColor =
            String.format("#%06x", ContextCompat.getColor(requireContext(), R.color.testpress_color_primary) and 0xffffff) + "75"
        phoneLogin.backgroundTintList = ColorStateList.valueOf(Color.parseColor(primaryColor))
        forgotPassword.backgroundTintList = ColorStateList.valueOf(Color.parseColor(primaryColor))

        phoneLogin.setOnClickListener {
            loginNavigation?.goToPhoneAuthentication()
        }

        signIn.setOnClickListener {
            if (username.isEmpty() or password.isEmpty()) {
                if (username.isEmpty()) {
                    username.error = "Please enter username/email address"
                }
                if (password.isEmpty()) {
                    password.error = "Please enter your password"
                }
            } else {
                signIn()
            }
        }

        forgotPassword.setOnClickListener {
            val intent = Intent(requireContext(), ResetPasswordActivity::class.java)
            requireActivity().startActivity(intent)
        }

        googleSignIn.setOnClickListener {
            loginNavigation?.signInWithGoogle()
        }
    }

    private fun updateLabels() {
        if (UIUtils.getMenuItemName(R.string.label_username, instituteSettings).isNotEmpty()) {
            username.hint = UIUtils.getMenuItemName(
                R.string.label_username,
                instituteSettings
            )
        }

        if (UIUtils.getMenuItemName(R.string.label_password, instituteSettings).isNotEmpty()) {
            password.hint = UIUtils.getMenuItemName(
                R.string.label_password,
                instituteSettings
            )
        }
    }

    private fun showOrHideButtons() {
        ViewUtils.setGone(facebookSignIn, !instituteSettings.facebookLoginEnabled)
        ViewUtils.setGone(googleSignIn, !instituteSettings.googleLoginEnabled)
        ViewUtils.setGone(
            socialLoginLayout, !instituteSettings.facebookLoginEnabled &&
                    !instituteSettings.googleLoginEnabled
        )
    }

    private fun signIn() {
        authenticate(
            username.text.toString(), password.text.toString(),
            TestpressSdk.Provider.TESTPRESS
        )
    }

    private fun authenticate(
        userId: String, accessToken: String,
        provider: TestpressSdk.Provider
    ) {
        val settings = `in`.testpress.models.InstituteSettings(instituteSettings.baseUrl)
            .setBookmarksEnabled(instituteSettings.bookmarksEnabled)
            .setCoursesFrontend(instituteSettings.showGameFrontend)
            .setCoursesGamificationEnabled(instituteSettings.coursesEnableGamification)
            .setCommentsVotingEnabled(instituteSettings.commentsVotingEnabled)
            .setAccessCodeEnabled(false)
        TestpressSdk.initialize(requireContext(), settings, userId, accessToken, provider,
            object : TestpressCallback<TestpressSession?>() {
                override fun onSuccess(response: TestpressSession?) {
                    if (provider == TestpressSdk.Provider.FACEBOOK &&
                        Profile.getCurrentProfile() != null
                    ) {

                    }
                    val authToken = response?.token
                    testPressService.setAuthToken(authToken)
                    val sharedPreferences =
                        requireActivity().getSharedPreferences(
                            Constants.GCM_PREFERENCE_NAME, Context.MODE_PRIVATE
                        )
                    sharedPreferences.edit().putBoolean(GCMPreference.SENT_TOKEN_TO_SERVER, false)
                        .apply()
                    CommonUtils.registerDevice(requireActivity(), testPressService)
                    val account = Account(username.text.toString(), BuildConfig.APPLICATION_ID)
                    accountManager.addAccountExplicitly(account, password.text.toString(), null)
                    accountManager.setAuthToken(account, BuildConfig.APPLICATION_ID, authToken)
                    autoLogin(authToken)
                }

                override fun onException(e: TestpressException) {
                    if (e.isNetworkError) {
                        UIUtils.showAlert(requireContext(),getString(R.string.no_internet_try_again))
                    } else if (e.isClientError) {
                        if (e.message?.isNotEmpty() == true) {
                            UIUtils.showAlert(requireContext(), e.message!!)
                        } else {
                            UIUtils.showAlert(requireContext(),getString(R.string.invalid_username_or_password))
                        }
                    } else {
                        UIUtils.showAlert(requireContext(),getString(R.string.testpress_some_thing_went_wrong_try_again))
                    }
                }
            })
    }

    private fun autoLogin(authToken: String?) {
        val intent = Intent()
        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, username.text.toString())
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, BuildConfig.APPLICATION_ID)
        intent.putExtra(AccountManager.KEY_AUTHTOKEN, authToken)
        loginNavigation?.onLoginSuccess(intent)
        requireActivity().setResult(RESULT_OK, intent)
        requireActivity().finish()
    }
}