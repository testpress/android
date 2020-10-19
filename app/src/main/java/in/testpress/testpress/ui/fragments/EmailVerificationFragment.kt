package `in`.testpress.testpress.ui.fragments

import `in`.testpress.enums.Status
import `in`.testpress.testpress.R
import `in`.testpress.testpress.authenticator.RegisterActivity
import `in`.testpress.testpress.util.ProgressUtil
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.register_activity.*
import kotlinx.android.synthetic.main.success_message_layout.*

class EmailVerificationFragment : RegistrationBaseFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        initViewModelObservers()
        setTextWatchers()
    }

    private fun initViewModelObservers() {
        viewModel.isUserDataValid.observe(viewLifecycleOwner, Observer {
            if (it) {
                register()
            }
        })

        viewModel.registrationResponse.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Status.SUCCESS -> showVerifyEmailLayout()
                Status.ERROR -> setPostDetailsException(it.exception)
            }
        })
    }

    private fun register() {
        ProgressUtil.showLoadingDialog(activity)
        viewModel.register()
    }

    private fun showVerifyEmailLayout() {
        registerLayout.visibility = View.GONE
        success_description.setText(R.string.activation_email_sent_message)
        success_complete.visibility = View.VISIBLE
    }
}
