package `in`.testpress.testpress.ui.utils

import `in`.testpress.testpress.BuildConfig.BASE_URL
import `in`.testpress.testpress.BuildConfig.WHITE_LABELED_HOST_URL
import `in`.testpress.testpress.ui.utils.DeeplinkHandler.ProductRoutingDestination
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class DeeplinkHandlerTest {

    @Test
    fun extractHost_returnsCorrectHostOrNull() {
        assertNull(DeeplinkHandler.extractHost(null))
        assertNull(DeeplinkHandler.extractHost(""))
        assertNull(DeeplinkHandler.extractHost("   "))

        assertEquals("example.com", DeeplinkHandler.extractHost("https://example.com"))
        assertEquals("example.com", DeeplinkHandler.extractHost("https://example.com/"))
        assertEquals("example.com", DeeplinkHandler.extractHost("https://example.com/path/to/page"))
        assertEquals("example.com", DeeplinkHandler.extractHost("http://example.com:8080/path"))
        assertEquals("example.com", DeeplinkHandler.extractHost("example.com"))
        assertEquals("example.com", DeeplinkHandler.extractHost("example.com/path"))
    }

    @Test
    fun isAppHost_matchesBaseUrlAndWhiteLabeledHost() {
        val baseHost = DeeplinkHandler.extractHost(BASE_URL)
        val whiteLabeledHost = DeeplinkHandler.extractHost(WHITE_LABELED_HOST_URL)

        assertFalse(DeeplinkHandler.isAppHost(null))
        assertFalse(DeeplinkHandler.isAppHost(""))
        assertFalse(DeeplinkHandler.isAppHost("   "))
        assertFalse(DeeplinkHandler.isAppHost("external-domain.com"))
        assertFalse(DeeplinkHandler.isAppHost("google.com"))

        if (baseHost != null) {
            assertTrue(DeeplinkHandler.isAppHost(baseHost))
            assertTrue(DeeplinkHandler.isAppHost(baseHost.uppercase()))
        }

        if (whiteLabeledHost != null) {
            assertTrue(DeeplinkHandler.isAppHost(whiteLabeledHost))
            assertTrue(DeeplinkHandler.isAppHost(whiteLabeledHost.uppercase()))
        }
    }

    @Test
    fun isDigitsOnly_validatesCorrectly() {
        assertFalse(DeeplinkHandler.isDigitsOnly(null))
        assertFalse(DeeplinkHandler.isDigitsOnly(""))
        assertFalse(DeeplinkHandler.isDigitsOnly("abc"))
        assertFalse(DeeplinkHandler.isDigitsOnly("123a"))
        assertFalse(DeeplinkHandler.isDigitsOnly("12.34"))
        assertFalse(DeeplinkHandler.isDigitsOnly("-123"))

        assertTrue(DeeplinkHandler.isDigitsOnly("123"))
        assertTrue(DeeplinkHandler.isDigitsOnly("0"))
        assertTrue(DeeplinkHandler.isDigitsOnly("99999999999999"))
    }

    @Test
    fun decideProductRouting_routesToOpenStoreWhenSegmentsInvalid() {
        val decisionNull = DeeplinkHandler.decideProductRouting(null, false, null)
        assertEquals(ProductRoutingDestination.OPEN_STORE, decisionNull.destination)

        val decisionEmpty = DeeplinkHandler.decideProductRouting(emptyList(), false, null)
        assertEquals(ProductRoutingDestination.OPEN_STORE, decisionEmpty.destination)

        val decisionOneSegment = DeeplinkHandler.decideProductRouting(listOf("products"), false, null)
        assertEquals(ProductRoutingDestination.OPEN_STORE, decisionOneSegment.destination)

        val decisionThreeSegments = DeeplinkHandler.decideProductRouting(listOf("products", "12", "extra"), false, null)
        assertEquals(ProductRoutingDestination.OPEN_STORE, decisionThreeSegments.destination)
    }

    @Test
    fun decideProductRouting_routesCachedProductWithCourseIdsToCoursePreview() {
        val decision = DeeplinkHandler.decideProductRouting(
            listOf("products", "my-slug"),
            true,
            listOf(101, 102)
        )
        assertEquals(ProductRoutingDestination.OPEN_COURSE_PREVIEW, decision.destination)
        assertEquals("my-slug", decision.productSlug)
        assertEquals(listOf(101, 102), decision.courseIds)
    }

    @Test
    fun decideProductRouting_routesCachedProductWithoutCourseIdsToStoreShowProduct() {
        val decisionEmptyCourses = DeeplinkHandler.decideProductRouting(
            listOf("products", "my-slug"),
            true,
            emptyList()
        )
        assertEquals(ProductRoutingDestination.OPEN_LEGACY_PRODUCT, decisionEmptyCourses.destination)
        assertEquals("my-slug", decisionEmptyCourses.productSlug)

        val decisionNullCourses = DeeplinkHandler.decideProductRouting(
            listOf("products", "my-slug"),
            true,
            null
        )
        assertEquals(ProductRoutingDestination.OPEN_LEGACY_PRODUCT, decisionNullCourses.destination)
        assertEquals("my-slug", decisionNullCourses.productSlug)
    }

    @Test
    fun decideProductRouting_routesUncachedNumericIdToFetchProductDetail() {
        val decision = DeeplinkHandler.decideProductRouting(
            listOf("products", "42"),
            false,
            null
        )
        assertEquals(ProductRoutingDestination.FETCH_PRODUCT_V3, decision.destination)
        assertEquals(Integer.valueOf(42), decision.numericProductId)
    }

    @Test
    fun decideProductRouting_routesUncachedOverflowNumericIdToStoreShowProduct() {
        // ID that exceeds Integer.MAX_VALUE should not crash or attempt network fetch with invalid int
        val decision = DeeplinkHandler.decideProductRouting(
            listOf("products", "99999999999999999999"),
            false,
            null
        )
        assertEquals(ProductRoutingDestination.OPEN_LEGACY_PRODUCT, decision.destination)
        assertEquals("99999999999999999999", decision.productSlug)
    }

    @Test
    fun decideProductRouting_routesUncachedSlugToStoreShowProduct() {
        val decision = DeeplinkHandler.decideProductRouting(
            listOf("products", "physics-course"),
            false,
            null
        )
        assertEquals(ProductRoutingDestination.OPEN_LEGACY_PRODUCT, decision.destination)
        assertEquals("physics-course", decision.productSlug)
    }
}
