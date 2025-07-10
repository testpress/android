package `in`.testpress.testpress.util

import android.app.Application
import android.util.Log
import `in`.testpress.course.helpers.OfflineAttachmentSyncManager
import `in`.testpress.database.TestpressDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch


fun Application.syncDownloads() {
    CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
        try {
            val dao = TestpressDatabase.invoke(this@syncDownloads.applicationContext)
                .offlineAttachmentDao()
            val syncManager =
                OfflineAttachmentSyncManager(this@syncDownloads.applicationContext, dao)
            syncManager.syncDownloads()
        } catch (e: Exception) {
            Log.e("DownloadSync", "Failed to sync downloads", e)
        }
    }
}