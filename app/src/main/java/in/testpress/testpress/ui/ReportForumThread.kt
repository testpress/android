package `in`.testpress.testpress.ui

import `in`.testpress.testpress.Injector
import `in`.testpress.testpress.R
import `in`.testpress.testpress.TestpressServiceProvider
import `in`.testpress.testpress.util.SafeAsyncTask
import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import kotlinx.android.synthetic.main.report_spam_thread.*
import javax.inject.Inject


class ReportForumThread: Activity() {
    @Inject
    lateinit var serviceProvider: TestpressServiceProvider


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Injector.inject(this)
        setContentView(R.layout.report_spam_thread)
        forumTitle.text = intent.getStringExtra("title")
        setOnClickListeners()
    }

    private fun setOnClickListeners() {
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        cancelButton.setOnClickListener {
            onBackPressed()
        }

        spamReasons.setOnCheckedChangeListener { group, checkedId ->
            submitButton.isEnabled = true
            showOrHideCustomReasonInput(checkedId)
        }

        submitButton.setOnClickListener {
            when (spamReasons.checkedRadioButtonId) {
                -1 -> Toast.makeText(this, "No option selected", Toast.LENGTH_LONG).show()
                R.id.customReason -> {
                    if (commentBox.text.toString().length < 10) {
                        Toast.makeText(
                            this,
                            "Please enter atleast 10 characters",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        reportForumThread(commentBox.text.toString())
                    }
                }
                else -> {
                    val selectedReason = findViewById<RadioButton>(spamReasons.checkedRadioButtonId)
                    reportForumThread(selectedReason.text.toString())
                }
            }
        }
    }

    private fun showOrHideCustomReasonInput(checkedId: Int) {
        if (checkedId == R.id.customReason) {
            commentBox.visibility = View.VISIBLE
        } else {
            commentBox.visibility = View.GONE
        }
    }

    fun reportForumThread(reason: String) {
        val forumId = intent.getIntExtra("forum_id", -1)
        object : SafeAsyncTask<Boolean>() {
            override fun call(): Boolean {
                serviceProvider.getService(this@ReportForumThread).reportPost(
                    forumId,
                    reason
                )
                return true
            }

            @Throws(RuntimeException::class)
            override fun onException(exception: Exception) {
                exception.printStackTrace()
                Toast.makeText(
                    baseContext,
                    "You have already reported this post",
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onSuccess(t: Boolean?) {
                onBackPressed()
                Toast.makeText(
                    baseContext,
                    "You have successfully reported this post",
                    Toast.LENGTH_LONG
                ).show()
            }
        }.execute()
    }
}