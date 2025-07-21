package `in`.testpress.testpress.util

import `in`.testpress.testpress.R
import `in`.testpress.testpress.models.InstituteSettings

fun getMenuItemName(titleResId: Int, instituteSettings: InstituteSettings): String =
    when (titleResId) {
        R.string.dashboard -> Strings.toString(instituteSettings.dashboardLabel)
        R.string.leaderboard -> Strings.toString(instituteSettings.leaderboardLabel)
        R.string.bookmarks -> Strings.toString(instituteSettings.bookmarksLabel)
        R.string.documents -> Strings.toString(instituteSettings.documentsLabel)
        R.string.store -> Strings.toString(instituteSettings.storeLabel)
        R.string.posts -> Strings.toString(instituteSettings.postsLabel)
        R.string.learn -> Strings.toString(instituteSettings.learnLabel)
        R.string.label_username -> Strings.toString(instituteSettings.loginLabel)
        R.string.label_password -> Strings.toString(instituteSettings.loginPasswordLabel)
        R.string.discussions -> Strings.toString(instituteSettings.forumLabel)
        else -> ""
    }