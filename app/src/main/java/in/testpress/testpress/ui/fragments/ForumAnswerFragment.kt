package `in`.testpress.testpress.ui.fragments

import `in`.testpress.database.entities.CommentEntity
import `in`.testpress.enums.Status
import `in`.testpress.testpress.R
import `in`.testpress.testpress.repository.ForumAnswerRepository
import `in`.testpress.testpress.util.FormatDate
import `in`.testpress.testpress.util.UILImageGetter
import `in`.testpress.testpress.util.ZoomableImageString
import `in`.testpress.testpress.viewmodel.DiscussionAnswerViewModel
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ForumAnswerFragment: Fragment() {
    lateinit var viewModel: DiscussionAnswerViewModel
    lateinit var userName: TextView
    lateinit var comment: TextView
    lateinit var date: TextView
    lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DiscussionAnswerViewModel(ForumAnswerRepository(requireContext())) as T
            }
        }).get(DiscussionAnswerViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.forum_answer_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViews(view)
        hideDivider()
        loadAnswer()
    }

    private fun hideDivider() {
        val divider: View = requireView().findViewById(R.id.comment_seperator)
        divider.visibility = View.GONE
    }

    private fun initializeViews(view: View) {
        userName = view.findViewById(R.id.user_name)
        comment = view.findViewById(R.id.comment)
        date = view.findViewById(R.id.submit_date)
        progressBar = view.findViewById(R.id.progressBar)
    }

    private fun loadAnswer() {
        val id = requireArguments().getLong("id")
        viewModel.getDiscussionAnswer(id).observe(viewLifecycleOwner, {
            when (it?.status) {
                Status.SUCCESS -> {
                    progressBar.visibility = View.GONE
                    displayAnswer(it.data?.comment)
                }
                Status.ERROR -> {
                    progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Error in refreshing answer", Toast.LENGTH_SHORT).show()
                }
                Status.LOADING -> progressBar.visibility = View.VISIBLE
                else -> {}
            }
        })
    }

    private fun displayAnswer(commentEntity: CommentEntity?) {
        userName.text = commentEntity?.user?.displayName
        displayCommentText(commentEntity?.comment)
        displayCommentDate(commentEntity?.created)
    }

    private fun displayCommentText(comment: String?) {
        val htmlSpan = Html.fromHtml(
            comment,
            UILImageGetter(this.comment, activity), null
        )
        val zoomableImageQuestion = ZoomableImageString(activity)
        this.comment.text = zoomableImageQuestion.convertString(htmlSpan)
        this.comment.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun displayCommentDate(dateString: String?) {
        val submitDateMillis = FormatDate.getDate(
            dateString,
            "yyyy-MM-dd'T'HH:mm:ss", "UTC"
        ).time
        date.text = FormatDate.getAbbreviatedTimeSpan(submitDateMillis)
    }
}