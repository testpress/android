package `in`.testpress.testpress.ui.fragments

import `in`.testpress.testpress.core.Constants.RequestCode
import `in`.testpress.testpress.models.Category
import `in`.testpress.testpress.ui.CreateForumActivity
import `in`.testpress.testpress.ui.ForumActivity
import `in`.testpress.ui.DiscussionsAdapter
import `in`.testpress.ui.fragments.DiscussionFragment
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.paging.ExperimentalPagingApi

class DiscussionFragmentv2: DiscussionFragment() {
    val categories = arrayListOf<Category>()
    override val adapter: DiscussionsAdapter = DiscussionsAdapter {
        Log.d("TAG", ": DiscussionFragment")
        val intent = Intent(activity, ForumActivity::class.java)
        intent.putExtra("Url", it.url)
        requireActivity().startActivity(intent)
    }

    @ExperimentalPagingApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.categories.observe(viewLifecycleOwner, Observer { domainCategories ->
            categories.addAll(domainCategories.map {  Category(it.id, it.name, it.color, it.slug) })
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d("TAG", "onActivityResult: DiscussionFragmentv2")
        adapter.refresh()
    }

    override fun setCreateButtonClickListener() {
        createButton.setOnClickListener {
            startActivityForResult(CreateForumActivity.createIntent(activity, categories.toList()),
                    RequestCode.CREATE_POST_REQUEST_CODE)
        }
    }
}