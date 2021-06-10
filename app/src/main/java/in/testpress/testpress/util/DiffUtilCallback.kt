package `in`.testpress.testpress.util

import `in`.testpress.testpress.models.Forum
import androidx.recyclerview.widget.DiffUtil

class DiffUtilCallBack : DiffUtil.ItemCallback<Forum>() {
    override fun areItemsTheSame(oldItem: Forum, newItem: Forum): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Forum, newItem: Forum): Boolean {
        return oldItem.contentHtml == newItem.contentHtml
                && oldItem.commentsCount == newItem.commentsCount
    }
}