package `in`.testpress.testpress.ui.fragments

import `in`.testpress.testpress.R
import `in`.testpress.testpress.authenticator.RegisterActivity
import `in`.testpress.testpress.databinding.RegisterActivityBinding
import `in`.testpress.testpress.repository.RegisterRepository
import `in`.testpress.testpress.util.ProgressUtil
import `in`.testpress.testpress.util.TextChangeUtil
import `in`.testpress.testpress.viewmodel.RegisterViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.register_activity.*
import java.util.*

open class RegistrationBaseFragment: Fragment() {

    lateinit var viewModel: RegisterViewModel

    lateinit var activity: RegisterActivity

    lateinit var binding: RegisterActivityBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.register_activity, container, false)
        activity = getActivity() as RegisterActivity
        return binding.root
    }

    fun initViewModel() {
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return RegisterViewModel(RegisterRepository(activity.testPressService), binding) as T
            }
        }).get(RegisterViewModel::class.java)
        binding.viewModel = viewModel
    }

    fun setPostDetailsException(exception: Exception?) {
        buttonRegister.isEnabled = true
        ProgressUtil.progressDialog.dismiss()
        viewModel.handleErrorResponse(exception)
    }

    fun setTextWatchers() {
        val editTextMap = Hashtable<EditText, TextView>()
        editTextMap[editTextUsername] = usernameErrorText
        editTextMap[editTextPassword] = passwordErrorText
        editTextMap[editTextConfirmPassword] = confirmPasswordErrorText
        editTextMap[editTextEmail] = emailErrorText
        editTextMap[editTextPhone] = phoneErrorText
        for (editText in editTextMap.keys()) {
            editTextMap[editText]?.let { TextChangeUtil.hideErrorMessageOnTextChange(editText, it) }
        }
        TextChangeUtil.showPasswordToggleOnTextChange(editTextPassword, passwordErrorText, passwordInputLayout)
        TextChangeUtil.showPasswordToggleOnTextChange(editTextConfirmPassword, confirmPasswordErrorText, confirmPasswordInputLayout)
    }
}
