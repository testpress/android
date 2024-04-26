package `in`.testpress.testpress.ui.fragments

import `in`.testpress.enums.Status
import `in`.testpress.testpress.R
import `in`.testpress.testpress.util.ProgressUtil
import android.os.Bundle
import android.view.View
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.Observer

open class EmailVerificationFragment : RegistrationBaseFragment() {



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnClickListener()
    }

    override fun initViewModelObservers() {
        viewModel.isUserDataValid.observe(viewLifecycleOwner, Observer {
            if (it) {
                register()
            }
        })

        viewModel.registrationResponse.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Status.SUCCESS -> showVerifyEmailLayout()
                Status.ERROR -> onRegisterException(it.exception)
                else -> {}
            }
        })
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun register() {
        ProgressUtil.showLoadingDialog(activity)
        viewModel.register()
    }

    private fun showVerifyEmailLayout() {
        ProgressUtil.progressDialog.dismiss()
        binding.registerLayout.visibility = View.GONE
        binding.successMessageLayout!!.successDescription.setText(R.string.activation_email_sent_message)
        binding.successMessageLayout!!.successComplete.visibility = View.VISIBLE
    }

    private fun setOnClickListener() {
        binding.successMessageLayout!!.successOk.setOnClickListener {
            activity.finish()
        }
    }
}
