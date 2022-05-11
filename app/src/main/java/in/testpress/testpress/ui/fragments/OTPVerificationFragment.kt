package `in`.testpress.testpress.ui.fragments

import `in`.testpress.enums.Status
import `in`.testpress.testpress.Injector
import `in`.testpress.testpress.R
import `in`.testpress.testpress.core.TestpressService
import `in`.testpress.testpress.repository.InstituteRepository
import `in`.testpress.testpress.ui.utils.AutoDetectOTP
import `in`.testpress.testpress.util.UIUtils
import `in`.testpress.testpress.util.isEmpty
import `in`.testpress.testpress.viewmodel.LoginViewModel
import android.accounts.AccountManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.otp_verification_layout.*
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject
import kotlin.concurrent.schedule

class OTPVerificationFragment: BaseAuthenticationFragment() {
    lateinit var viewModel: LoginViewModel
    lateinit var accountManager: AccountManager

    @Inject
    lateinit var testPressService: TestpressService
    lateinit var phoneNumber: String
    lateinit var countryCode: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Injector.inject(this)
        initViewModel()
        accountManager = AccountManager.get(requireContext())
        autoFillOTP()
        populateArguments()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.otp_verification_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnClickListeners()
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

    private fun autoFillOTP() {
        val autoDetectOTP = AutoDetectOTP(requireActivity())
        autoDetectOTP.startSmsRetriever(object : AutoDetectOTP.SmsCallback {
            override fun connectionFailed() {
            }

            override fun connectionSuccess() {
            }

            override fun smsCallback(sms: String) {
                if (sms.contains("authorization code")) {
                    val otpRegex = "(\\d{4})"  // 4 digits
                    val pattern = Pattern.compile(otpRegex)
                    val matcher = pattern.matcher(sms)
                    if (matcher.find()) {
                        val otp = matcher.group(0)
                        if (isVisible) {
                            otpField.setText(otp)
                            Timer().schedule(500) {
                                verifyOTP()
                            }
                        }
                    }
                }
            }
        })
    }

    private fun populateArguments() {
        phoneNumber = requireArguments().getString("phoneNumber")!!
        countryCode = requireArguments().getString("countryCode")!!
    }

    fun setOnClickListeners() {
        verifyOTP.setOnClickListener {
            verifyOTP()
        }

        resentOtp.setOnClickListener {
            viewModel.requestOTP(phoneNumber, countryCode).observe(viewLifecycleOwner, { resource ->
                when(resource.status) {
                    Status.SUCCESS -> {
                        helpText.text = "We resent your OTP"
                    }
                    Status.ERROR -> {
                        UIUtils.showAlert(requireContext(),resource.data?.detail ?: "Couldn't send OTP. Please try again")
                    }
                }
            })
        }
    }

    fun verifyOTP() {
        if (otpField.isEmpty()) {
            otpField.error = "Please enter OTP number"
            return
        }
        requireActivity().runOnUiThread {
            viewModel.verifyOTP(Integer.parseInt(otpField.text.toString()), phoneNumber).observe(this, { resource ->
                when(resource.status) {
                    Status.SUCCESS -> {
                        if (resource.data?.token != null) {
                            loginNavigation?.onLoginSuccess(phoneNumber, resource.data!!.token!!)
                        }
                    }
                    Status.ERROR -> {
                        if (resource.data?.nonFieldErrors != null) {
                            val error = resource.data?.nonFieldErrors!!.joinToString(separator = "\n") { it }
                            UIUtils.showAlert(requireContext(),error)
                        } else {
                            UIUtils.showAlert(requireContext(),"Error occurred while verifying OTP. Please try again.")
                        }
                    }
                }
            })
        }
    }
}