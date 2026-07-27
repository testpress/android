package `in`.testpress.testpress.ui

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import `in`.testpress.testpress.BuildConfig.APPLICATION_ID
import `in`.testpress.testpress.R
import `in`.testpress.testpress.ui.fragments.ExamResultFragment


class ExamResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exam_result)
        setupToolbar()
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ExamResultFragment.newInstance(getStudentNo()))
                .commitAllowingStateLoss()
        }
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar_actionbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.exam_results)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun getStudentNo(): String {
        val manager = getSystemService(Context.ACCOUNT_SERVICE) as AccountManager
        val accounts: Array<Account> = manager.getAccountsByType(APPLICATION_ID)
        return if (accounts.isNotEmpty()) accounts[0].name else ""
    }
}
