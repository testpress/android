package `in`.testpress.testpress.ui.fragments

import `in`.testpress.enums.Status
import `in`.testpress.testpress.R
import `in`.testpress.testpress.util.ProgressUtil
import android.view.View
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.register_activity.*
import kotlinx.android.synthetic.main.success_message_layout.*

open class EmailVerificationFragment : RegistrationBaseFragment() {

    override fun initViewModelObservers() {
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

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun register() {
        ProgressUtil.showLoadingDialog(activity)
        viewModel.register()
    }

    fun showVerifyEmailLayout() {
        registerLayout.visibility = View.GONE
        success_description.setText(R.string.activation_email_sent_message)
        success_complete.visibility = View.VISIBLE
    }
}
