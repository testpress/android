package `in`.testpress.testpress.ui

import `in`.testpress.R
import `in`.testpress.testpress.ui.fragments.DiscussionFragmentv2
import `in`.testpress.ui.BaseToolBarActivity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log

class DisscussionActivity: BaseToolBarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.testpress_container_layout)
        Log.d("TAG", "onCreate: DisscussionActivity")
        val fragment = DiscussionFragmentv2()
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment).commitAllowingStateLoss()
    }


    companion object {
        @JvmStatic
        fun createIntent(context: Context): Intent {
            return Intent(context, DisscussionActivity::class.java)
        }
    }


}