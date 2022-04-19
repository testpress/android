package `in`.testpress.testpress.ui.fragments

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.core.TestpressSdk
import `in`.testpress.core.TestpressSession
import `in`.testpress.testpress.Injector
import `in`.testpress.testpress.R
import `in`.testpress.testpress.authenticator.LoginActivityV2
import `in`.testpress.testpress.authenticator.ResetPasswordActivity
import `in`.testpress.testpress.core.TestpressService
import `in`.testpress.testpress.models.InstituteSettings
import `in`.testpress.testpress.util.UIUtils
import `in`.testpress.testpress.util.isEmpty
import `in`.testpress.util.ViewUtils
import android.accounts.AccountManager
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
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
        )
    }

    private fun authenticate(userId: String, accessToken: String) {
        val settings = `in`.testpress.models.InstituteSettings(instituteSettings.baseUrl)
            .setBookmarksEnabled(instituteSettings.bookmarksEnabled)
            .setCoursesFrontend(instituteSettings.showGameFrontend)
            .setCoursesGamificationEnabled(instituteSettings.coursesEnableGamification)
            .setCommentsVotingEnabled(instituteSettings.commentsVotingEnabled)
            .setAccessCodeEnabled(false)
        TestpressSdk.initialize(requireContext(), settings, userId, accessToken, TestpressSdk.Provider.TESTPRESS,
            object : TestpressCallback<TestpressSession?>() {
                override fun onSuccess(response: TestpressSession?) {
                    val authToken = response?.token
                    if (authToken != null) {
                        loginNavigation?.onLoginSuccess(username.text.toString(), authToken)
                    }
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
}