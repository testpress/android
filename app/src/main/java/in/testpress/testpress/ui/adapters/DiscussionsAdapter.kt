package `in`.testpress.testpress.ui.adapters

import `in`.testpress.testpress.R
import `in`.testpress.testpress.models.Forum
import `in`.testpress.testpress.ui.utils.DiffUtilCallBack
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.documents_list_item.view.*

class DiscussionsAdapter :
        PagingDataAdapter<Forum, DiscussionsAdapter.RedditViewHolder>(DiffUtilCallBack()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RedditViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.forum_list_item, parent, false)
        return RedditViewHolder(view)
    }

    override fun onBindViewHolder(holder: RedditViewHolder, position: Int) {
        getItem(position)?.let { holder.bindPost(it) }
    }

    class RedditViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText: TextView = itemView.title

        fun bindPost(forumPost: Forum) {
            with(forumPost) {
                titleText.text = title
            }
        }
    }
}