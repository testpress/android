package `in`.testpress.testpress.ui.compose.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.compose.material3.MaterialTheme
import `in`.testpress.testpress.R

class NewDashboardFragment : Fragment() {
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            
            setContent {
                MaterialTheme {
                    NewDashboardScreen(
                        onMenuClick = {
                            openNavigationDrawer()
                        }
                    )
                }
            }
        }
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!isHidden) {
            hideActivityToolbar()
        }
    }
    
    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) {
            showActivityToolbar()
        } else {
            hideActivityToolbar()
            hideActivityToolbar()
        }
    }
    
    override fun onDestroyView() {
        showActivityToolbar()
        super.onDestroyView()
    }
    
    private fun hideActivityToolbar() {
        (activity as? AppCompatActivity)?.supportActionBar?.hide()
    }
    
    private fun showActivityToolbar() {
        (activity as? AppCompatActivity)?.supportActionBar?.show()
    }
    
    private fun openNavigationDrawer() {
        try {
            val drawerLayout = activity?.findViewById<DrawerLayout>(R.id.drawer_layout)
            drawerLayout?.openDrawer(GravityCompat.START)
        } catch (e: Exception) {
            showToast("Unable to open menu")
        }
    }
    
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
    
    companion object {
        fun newInstance(): NewDashboardFragment {
            return NewDashboardFragment()
        }
    }
}
