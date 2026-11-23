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

    @Test
    fun normalizeUrl_returnsNullForNullOrBlank() {
        assertNull(NotificationHelper.normalizeUrl(null))
        assertNull(NotificationHelper.normalizeUrl(""))
        assertNull(NotificationHelper.normalizeUrl("   "))
        assertNull(NotificationHelper.normalizeUrl("\t\n"))
    }

    @Test
    fun resolveSummaryUri_returnsNullForNullOrEmpty() {
        assertNull(NotificationHelper.resolveSummaryUri(null))
        assertNull(NotificationHelper.resolveSummaryUri(""))
        assertNull(NotificationHelper.resolveSummaryUri("   "))
    }

    @Test
    fun resolveSummaryUrl_returnsNullForNullOrEmpty() {
        assertNull(NotificationHelper.resolveSummaryUrl(null))
        assertNull(NotificationHelper.resolveSummaryUrl(""))
        assertNull(NotificationHelper.resolveSummaryUrl("   "))
    }

    @Test
    fun resolveSummaryUrl_resolvesValidGroupUrlPaths() {
        assertEquals(BASE_URL + "/posts/", NotificationHelper.resolveSummaryUrl("/posts/"))
        assertEquals(BASE_URL + "/learn/", NotificationHelper.resolveSummaryUrl("/learn/"))
    }

    @Test
    fun getNotificationRouting_routesExamsAndChaptersToContentsChannel() {
        val examsRouting = NotificationHelper.getNotificationRouting("/exams/123/")
        assertEquals(NotificationHelper.CONTENTS_CHANNEL, examsRouting.channelId)
        assertEquals("/learn/", examsRouting.groupUrlPath)

        val chaptersRouting = NotificationHelper.getNotificationRouting("chapters/456/")
        assertEquals(NotificationHelper.CONTENTS_CHANNEL, chaptersRouting.channelId)
        assertEquals("/learn/", chaptersRouting.groupUrlPath)

        val fullUrlExamsRouting = NotificationHelper.getNotificationRouting("https://testpress.in/exams/123/")
        assertEquals(NotificationHelper.CONTENTS_CHANNEL, fullUrlExamsRouting.channelId)
        assertEquals("/learn/", fullUrlExamsRouting.groupUrlPath)
    }

    @Test
    fun getNotificationRouting_routesPostsToPostsChannel() {
        val postRouting = NotificationHelper.getNotificationRouting("/p/123/")
        assertEquals(NotificationHelper.POSTS_CHANNEL, postRouting.channelId)
        assertEquals("/posts/", postRouting.groupUrlPath)

        val fullUrlPostRouting = NotificationHelper.getNotificationRouting("https://testpress.in/p/123/")
        assertEquals(NotificationHelper.POSTS_CHANNEL, fullUrlPostRouting.channelId)
        assertEquals("/posts/", fullUrlPostRouting.groupUrlPath)
    }

    @Test
    fun getNotificationRouting_routesProductsAndStoreToPostsChannelWithoutGroupUrl() {
        val productRouting = NotificationHelper.getNotificationRouting("/products/12/")
        assertEquals(NotificationHelper.POSTS_CHANNEL, productRouting.channelId)
        assertNull(productRouting.groupUrlPath)

        val storeRouting = NotificationHelper.getNotificationRouting("/store/")
        assertEquals(NotificationHelper.POSTS_CHANNEL, storeRouting.channelId)
        assertNull(storeRouting.groupUrlPath)

        val fullUrlProductRouting = NotificationHelper.getNotificationRouting("https://testpress.in/products/my-slug/")
        assertEquals(NotificationHelper.POSTS_CHANNEL, fullUrlProductRouting.channelId)
        assertNull(fullUrlProductRouting.groupUrlPath)
    }

    @Test
    fun getNotificationRouting_routesEmptyOrNoPathToPostsChannelWithoutGroupUrl() {
        val nullRouting = NotificationHelper.getNotificationRouting(null)
        assertEquals(NotificationHelper.POSTS_CHANNEL, nullRouting.channelId)
        assertNull(nullRouting.groupUrlPath)

        val emptyRouting = NotificationHelper.getNotificationRouting("")
        assertEquals(NotificationHelper.POSTS_CHANNEL, emptyRouting.channelId)
        assertNull(emptyRouting.groupUrlPath)

        val rootRouting = NotificationHelper.getNotificationRouting("/")
        assertEquals(NotificationHelper.POSTS_CHANNEL, rootRouting.channelId)
        assertNull(rootRouting.groupUrlPath)

        val hostOnlyRouting = NotificationHelper.getNotificationRouting("https://testpress.in")
        assertEquals(NotificationHelper.POSTS_CHANNEL, hostOnlyRouting.channelId)
        assertNull(hostOnlyRouting.groupUrlPath)
    }
}
