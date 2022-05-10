package `in`.testpress.testpress.ui.fragments

import `in`.testpress.testpress.authenticator.LoginNavigationInterface
import android.content.Context
import androidx.fragment.app.Fragment

open class BaseAuthenticationFragment: Fragment() {
    var loginNavigation: LoginNavigationInterface? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        initializeListeners()
    }

    private fun initializeListeners() {
        loginNavigation = if (parentFragment != null) {
            parentFragment as? LoginNavigationInterface
        } else {
            context as? LoginNavigationInterface
        }
    }
}