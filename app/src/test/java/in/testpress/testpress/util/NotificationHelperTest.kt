package `in`.testpress.testpress.util

import `in`.testpress.testpress.BuildConfig.BASE_URL
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class NotificationHelperTest {

    @Test
    fun buildFullUrl_returnsNullForNullOrEmpty() {
        assertNull(NotificationHelper.buildFullUrl(null))
        assertNull(NotificationHelper.buildFullUrl(""))
        assertNull(NotificationHelper.buildFullUrl("   "))
    }

    @Test
    fun buildFullUrl_prependsBaseUrlForRelativePathWithSlash() {
        assertEquals(BASE_URL + "/products/slug/", NotificationHelper.buildFullUrl("/products/slug/"))
    }

    @Test
    fun buildFullUrl_prependsBaseUrlForRelativePathWithoutSlash() {
        assertEquals(BASE_URL + "/products/slug/", NotificationHelper.buildFullUrl("products/slug/"))
    }

    @Test
    fun buildFullUrl_returnsFullUrlForHttpAndHttps() {
        assertEquals("https://testpress.in/products/slug/", NotificationHelper.buildFullUrl("https://testpress.in/products/slug/"))
        assertEquals("http://testpress.in/products/slug/", NotificationHelper.buildFullUrl("http://testpress.in/products/slug/"))
    }

    @Test
    fun buildFullUrl_handlesDashboardAndStorePaths() {
        assertEquals(BASE_URL + "/dashboard_v1", NotificationHelper.buildFullUrl("/dashboard_v1"))
        assertEquals(BASE_URL + "/store/", NotificationHelper.buildFullUrl("store/"))
    }
}
