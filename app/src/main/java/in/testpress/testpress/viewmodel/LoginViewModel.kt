package `in`.testpress.testpress.viewmodel

import `in`.testpress.network.Resource
import `in`.testpress.testpress.models.InstituteSettings
import `in`.testpress.testpress.models.pojo.GenerateOTPResponse
import `in`.testpress.testpress.models.pojo.OTPLoginResponse
import `in`.testpress.testpress.repository.InstituteRepository
import androidx.lifecycle.*
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: InstituteRepository): ViewModel() {

    fun getInstituteSettings(): LiveData<Resource<InstituteSettings>> {
        return repository.getInstituteSettings()
    }

    fun requestOTP(phoneNumber: String, countryCode: String): LiveData<Resource<GenerateOTPResponse>> {
        return repository.requestOTP(phoneNumber.toLong(), countryCode)
    }

    fun verifyOTP(otp: Int, phoneNumber: String): LiveData<Resource<OTPLoginResponse>> {
        return repository.verifyOTP(otp, phoneNumber.toLong())
    }
}