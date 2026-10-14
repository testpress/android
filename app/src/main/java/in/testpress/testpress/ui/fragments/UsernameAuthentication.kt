package `in`.testpress.testpress.ui.fragments

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.core.TestpressSdk
import `in`.testpress.core.TestpressSession
import `in`.testpress.testpress.BuildConfig
import `in`.testpress.testpress.TestpressApplication
import `in`.testpress.testpress.R
import `in`.testpress.testpress.authenticator.LoginActivity
import `in`.testpress.testpress.authenticator.LoginActivityV2
import `in`.testpress.testpress.authenticator.RegisterActivity
import `in`.testpress.testpress.authenticator.ResetPasswordActivity
import `in`.testpress.testpress.core.Constants
import `in`.testpress.testpress.core.TestpressService
import `in`.testpress.testpress.databinding.UsernameLoginLayoutBinding
import `in`.testpress.testpress.models.InstituteSettings
import `in`.testpress.testpress.ui.WebViewActivity
import `in`.testpress.testpress.util.UIUtils
import `in`.testpress.testpress.util.isEmpty
import `in`.testpress.util.ViewUtils
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat.getSystemService
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import `in`.testpress.testpress.util.DeviceBindingDialogs
import javax.inject.Inject


class UsernameAuthentication : BaseAuthenticationFragment() {
    lateinit var accountManager: AccountManager
    private val instituteSettings: InstituteSettings?
        get() = (activity as? LoginActivityV2)?.getLoadedInstituteSettings()
            ?: InstituteSettings.getInstance()
    private var confirmationDialog: AlertDialog? = null

    @Inject
    lateinit var testPressService: TestpressService
    private var _binding: UsernameLoginLayoutBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TestpressApplication.getAppComponent().inject(this)
        accountManager = AccountManager.get(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = UsernameLoginLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeInputFields()
        initializeButtons()
        updateLabels()
        showOrHideButtons()
    }

    private fun initializeInputFields() {
        binding.password.transformationMethod = PasswordTransformationMethod()
        binding.password.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    hideSoftKeyboard()
                    signIn()
                    true
                }
                else -> false
            }
        }

        binding.username.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (binding.username.text.toString().contains(" ")) {
                    binding.username.setText(binding.username.text.toString().replace(" ", ""))
                    binding.username.setSelection(binding.username.text?.length ?: 0)
                }
            }
        })
        binding.username.requestFocus()
    }

    private fun hideSoftKeyboard(){
        val inputManager= requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(this.view?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    private fun initializeButtons() {
        binding.phoneLogin.setOnClickListener {
            loginNavigation?.goToPhoneAuthentication()
        }

        binding.forgotPassword.setOnClickListener {
            var openedCustomUrl = false
            val settings = instituteSettings
            if (settings != null && settings.hasCustomForgotPasswordUrl()) {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(settings.customForgotPasswordUrl))
                    startActivity(intent)
                    openedCustomUrl = true
                } catch (e: android.content.ActivityNotFoundException) {
                    android.widget.Toast.makeText(
                        requireContext(),
                        "No suitable app was found to open this URL. Please install any browser app",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
            if (!openedCustomUrl) {
                val intent = Intent(requireContext(), ResetPasswordActivity::class.java)
                requireActivity().startActivity(intent)
            }
        }

        binding.signIn.setOnClickListener {
            if (binding.username.isEmpty() or binding.password.isEmpty()) {
                if (binding.username.isEmpty()) {
                    binding.username.error = "Please enter username/email address"
                }
                if (binding.password.isEmpty()) {
                    binding.password.error = "Please enter your password"
                }
            } else {
                signIn()
            }
        }


        binding.googleSignIn.setOnClickListener {
            loginNavigation?.signInWithGoogle()
        }

        binding.signUp.setOnClickListener {
            val settings = instituteSettings
            if (settings?.customRegistrationEnabled == true){
                val intent = Intent(requireContext(), WebViewActivity::class.java)
                intent.putExtra(WebViewActivity.ACTIVITY_TITLE, "Register")
                intent.putExtra(WebViewActivity.SHOW_LOGOUT, "false")
                intent.putExtra(WebViewActivity.ALLOW_EXTERNAL_LINK,true)
                intent.putExtra(WebViewActivity.URL_TO_OPEN, settings.customRegistrationUrl)
                startActivity(intent)
            } else {
                val intent = Intent(requireContext(), RegisterActivity::class.java)
                startActivityForResult(intent, LoginActivity.REQUEST_CODE_REGISTER_USER)
            }
        }

        binding.usernameLayoutPrivacyPolicy.setOnClickListener {
            val intent = Intent(requireActivity(), WebViewActivity::class.java)
            intent.putExtra(WebViewActivity.URL_TO_OPEN, BuildConfig.BASE_URL + Constants.Http.URL_PRIVACY_POLICY_FLAG)
            intent.putExtra(WebViewActivity.ACTIVITY_TITLE, "Privacy Policy")
            startActivity(intent)
        }

    }

    private fun updateLabels() {
        val settings = instituteSettings ?: return
        val usernameLabel = UIUtils.getMenuItemName(R.string.label_username, settings)
        if (usernameLabel.isNotEmpty()) {
            binding.username.hint = usernameLabel
        }

        val passwordLabel = UIUtils.getMenuItemName(R.string.label_password, settings)
        if (passwordLabel.isNotEmpty()) {
            binding.password.hint = passwordLabel
        }
    }

    private fun showOrHideButtons() {
        instituteSettings?.let { settings ->
            if (!settings.allowSignup) {
                binding.signUp.visibility = View.GONE
            }
            ViewUtils.setGone(binding.phoneLogin, 3 !in settings.allowedLoginMethods)
            ViewUtils.setGone(binding.facebookSignIn, !settings.facebookLoginEnabled)
            ViewUtils.setGone(binding.googleSignIn, !settings.googleLoginEnabled)
            ViewUtils.setGone(
                binding.socialLoginLayout, !settings.facebookLoginEnabled &&
                        !settings.googleLoginEnabled
            )
            if (settings.disableForgotPassword != null){
                ViewUtils.setGone(binding.forgotPassword, settings.disableForgotPassword)
            }
        }
    }

    private fun signIn() {
        if (confirmationDialog?.isShowing == true) return
        val username = binding.username.text.toString()
        val password = binding.password.text.toString()
        confirmationDialog = DeviceBindingDialogs.confirmIfBindingEnabled(
            requireContext(),
            instituteSettings,
            onProceed = {
                if (isAdded && view != null) {
                    authenticate(username, password)
                }
            }
        )
    }

    private fun authenticate(username: String, password: String) {
        val currentSettings = instituteSettings ?: return
        val settings = `in`.testpress.models.InstituteSettings(currentSettings.baseUrl)
            .setWhiteLabeledHostUrl(BuildConfig.WHITE_LABELED_HOST_URL)
            .setBookmarksEnabled(currentSettings.bookmarksEnabled)
            .setCoursesFrontend(currentSettings.showGameFrontend)
            .setCoursesGamificationEnabled(currentSettings.coursesEnableGamification)
            .setCommentsVotingEnabled(currentSettings.commentsVotingEnabled)
            .setAndroidSentryDns(currentSettings.androidSentryDns)
            .setAccessCodeEnabled(false)
            .setDeviceBindingMode(currentSettings.deviceBindingMode)
        TestpressSdk.initialize(requireContext(), settings, username, password, TestpressSdk.Provider.TESTPRESS,
            object : TestpressCallback<TestpressSession?>() {
                override fun onSuccess(response: TestpressSession?) {
                    val authToken = response?.token
                    if (authToken != null) {
                        loginNavigation?.onLoginSuccess(username, password, authToken)
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
                        UIUtils.showAlert(requireContext(),getString(R.string.something_went_wrong_please_try_after))
                    }
                }
            })
    }

    override fun onDestroyView() {
        confirmationDialog?.dismiss()
        confirmationDialog = null
        super.onDestroyView()
        _binding = null
    }
}