package `in`.testpress.testpress.ui.fragments

import `in`.testpress.enums.Status
import `in`.testpress.testpress.Injector
import `in`.testpress.testpress.R
import `in`.testpress.testpress.core.TestpressService
import `in`.testpress.testpress.models.InstituteSettings
import `in`.testpress.testpress.repository.InstituteRepository
import `in`.testpress.testpress.util.UIUtils
import `in`.testpress.testpress.util.isEmpty
import `in`.testpress.testpress.viewmodel.LoginViewModel
import `in`.testpress.util.ViewUtils
import android.content.Intent
import android.content.IntentSender
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.credentials.*
import com.google.i18n.phonenumbers.PhoneNumberUtil
import kotlinx.android.synthetic.main.phone_login_layout.*
import javax.inject.Inject


class PhoneAuthenticationFragment: BaseAuthenticationFragment() {
    lateinit var viewModel: LoginViewModel
    @Inject
    lateinit var testPressService: TestpressService
    private val instituteSettings = InstituteSettings.getInstance()

    private val phonePickIntentResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result?.data != null) {
                val intent = result.data!!
                autoFillPhoneNumber(intent)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Injector.inject(this)
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
        return inflater.inflate(R.layout.phone_login_layout, container, false)
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
        phoneNumber.setOnEditorActionListener { _, actionId, _ ->
            when(actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    requestOTP(phoneNumber.text.toString(), countryCode.selectedCountryCode)
                    true
                }
                else -> false
            }
        }
        showOrHideButtons()
    }

    private fun showOrHideButtons() {
        ViewUtils.setGone(facebookSignIn, !instituteSettings.facebookLoginEnabled)
        ViewUtils.setGone(googleSignIn, !instituteSettings.googleLoginEnabled)
        ViewUtils.setGone(
            socialLoginLayout, !instituteSettings.facebookLoginEnabled &&
                    !instituteSettings.googleLoginEnabled
        )
    }

    private fun setOnClickListeners() {
        val primaryColor =
            String.format("#%06x", ContextCompat.getColor(requireContext(), R.color.testpress_color_primary) and 0xffffff) + "75"
        userNameLogin.backgroundTintList = ColorStateList.valueOf(Color.parseColor(primaryColor))
        userNameLogin.setOnClickListener {
            loginNavigation?.goToUsernameAuthentication()
        }

        verifyOtp.setOnClickListener {
            if (phoneNumber.isEmpty()) {
                phoneNumber.error = "Please enter your mobile number"
            } else {
                requestOTP(phoneNumber.text.toString(), countryCode.selectedCountryCode)
            }
        }

        googleSignIn.setOnClickListener {
            loginNavigation?.signInWithGoogle()
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
            }
        })
    }

    private fun autoFillPhoneNumber(intent: Intent) {
        val phoneNumberUtil = PhoneNumberUtil.getInstance(requireContext())
        val phoneNo = intent.getParcelableExtra<Credential>(Credential.EXTRA_KEY)?.id
        if (phoneNo != null) {
            val number = phoneNumberUtil.parse(phoneNo, "")
            phoneNumber.setText(number.nationalNumber.toString())
            countryCode.setCountryForPhoneCode(number.countryCode)
        }
    }
}