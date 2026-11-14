package `in`.testpress.testpress.repository

import `in`.testpress.testpress.core.TestpressService
import javax.inject.Inject

class ForceDataRepository @Inject constructor(private val service: TestpressService) {
    fun isDataCollected(token: String): Boolean {
        service.setAuthToken(token)
        return service.checkPermission()?.isDataCollected == true
    }
}
