package `in`.testpress.testpress.ui.fragments

import `in`.testpress.enums.Status
import `in`.testpress.testpress.BuildConfig
import `in`.testpress.testpress.TestpressApplication
import `in`.testpress.testpress.R
import `in`.testpress.testpress.core.Constants.Http
import `in`.testpress.testpress.core.TestpressService
import `in`.testpress.testpress.databinding.PhoneLoginLayoutBinding
import `in`.testpress.testpress.models.InstituteSettings
import `in`.testpress.testpress.repository.InstituteRepository
import `in`.testpress.testpress.ui.WebViewActivity
import `in`.testpress.testpress.util.UIUtils
import `in`.testpress.testpress.util.isEmpty
import `in`.testpress.testpress.viewmodel.LoginViewModel
import `in`.testpress.util.ViewUtils
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.credentials.*
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import javax.inject.Inject


class PhoneAuthenticationFragment: BaseAuthenticationFragment() {
    lateinit var viewModel: LoginViewModel
    @Inject
    lateinit var testPressService: TestpressService
    private val instituteSettings = InstituteSettings.getInstance()
    private var _binding: PhoneLoginLayoutBinding? = null
    private val binding get() = _binding!!

    private val phonePickIntentResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result?.data != null) {
                val intent = result.data!!
                autoFillPhoneNumber(intent)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TestpressApplication.getAppComponent().inject(this)
        initViewModel()
        showUserMobileNumbers(phonePickIntentResultLauncher)
    }

    private fun showUserMobileNumbers(phonePickIntentResultLauncher: ActivityResultLauncher<IntentSenderRequest>) {
        val hintRequest = HintRequest.Builder()
            .setHintPickerConfig(
                CredentialPickerConfig.Builder()
                    .setShowCancelButton(true)
                    .build()
            )
            .setPhoneNumberIdentifierSupported(true)
            .build()
        val options = CredentialsOptions.Builder().forceEnableSaveDialog().build()
        val credentialsClient = Credentials.getClient(requireContext(), options)
        val intent = credentialsClient.getHintPickerIntent(hintRequest)
        try {
            val intentSenderRequest = IntentSenderRequest.Builder(intent.intentSender).build()
            phonePickIntentResultLauncher.launch(intentSenderRequest)
        } catch (e: IntentSender.SendIntentException) {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = PhoneLoginLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LoginViewModel(
                    InstituteRepository(requireContext(), testPressService)
                ) as T
            }
        }).get(LoginViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnClickListeners()
        binding.phoneNumber.setOnEditorActionListener { _, actionId, _ ->
            when(actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    requestOTP(binding.phoneNumber.text.toString(), binding.countryCode.selectedCountryCode)
                    true
                }
                else -> false
            }
        }
        showOrHideButtons()
    }

    private fun showOrHideButtons() {
        ViewUtils.setGone(binding.userNameLogin, 1 !in instituteSettings.allowedLoginMethods)
        ViewUtils.setGone(binding.facebookSignIn, !instituteSettings.facebookLoginEnabled)
        ViewUtils.setGone(binding.googleSignIn, !instituteSettings.googleLoginEnabled)
        ViewUtils.setGone(
            binding.socialLoginLayout, !instituteSettings.facebookLoginEnabled &&
                    !instituteSettings.googleLoginEnabled
        )
    }

    private fun setOnClickListeners() {
        binding.userNameLogin.setOnClickListener {
            loginNavigation?.goToUsernameAuthentication()
        }

        binding.verifyOtp.setOnClickListener {
            if (binding.phoneNumber.isEmpty()) {
                binding.phoneNumber.error = "Please enter your mobile number"
            } else {
                requestOTP(binding.phoneNumber.text.toString(), binding.countryCode.selectedCountryCode)
            }
        }

        binding.googleSignIn.setOnClickListener {
            loginNavigation?.signInWithGoogle()
        }

        binding.phoneLayoutPrivacyPolicy.setOnClickListener {
            val intent = Intent(requireActivity(), WebViewActivity::class.java)
            intent.putExtra(WebViewActivity.URL_TO_OPEN, BuildConfig.BASE_URL + Http.URL_PRIVACY_POLICY_FLAG)
            intent.putExtra(WebViewActivity.ACTIVITY_TITLE, "Privacy Policy")
            startActivity(intent)
        }
    }

    private fun requestOTP(phoneNumber: String, countryCode: String) {
        viewModel.requestOTP(phoneNumber, countryCode).observe(viewLifecycleOwner, { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    loginNavigation?.goToOTPVerification(phoneNumber, countryCode)
                }
                Status.ERROR -> {
                    if (resource.data?.detail != null) {
                        UIUtils.showAlert(requireContext(),resource.data?.detail!!)
                    } else {
                        UIUtils.showAlert(requireContext(),"Error occurred while sending OTP. Please try again.")
                    }
                }
                else -> {}
            }
        })
    }

    private fun autoFillPhoneNumber(intent: Intent) {
        val phoneNumberUtil = PhoneNumberUtil.createInstance(requireContext())
        val phoneNo = intent.getParcelableExtra<Credential>(Credential.EXTRA_KEY)?.id
        if (phoneNo != null) {
            val number = phoneNumberUtil.parse(phoneNo, "")
            binding.phoneNumber.setText(number.nationalNumber.toString())
            binding.countryCode.setCountryForPhoneCode(number.countryCode)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}