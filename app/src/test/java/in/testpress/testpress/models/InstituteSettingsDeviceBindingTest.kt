package `in`.testpress.testpress.models

import `in`.testpress.testpress.TestpressModule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class InstituteSettingsDeviceBindingTest {

    private val gson = TestpressModule().provideGson()

    @Test
    fun isDeviceBindingEnabledReturnsTrueFor1Mobile1Desktop() {
        val settings = InstituteSettings().apply {
            deviceBindingMode = InstituteSettings.DEVICE_BINDING_1_MOBILE_1_DESKTOP
        }
        assertTrue(settings.isDeviceBindingEnabled)
    }

    @Test
    fun isDeviceBindingEnabledReturnsTrueForMobileAppOnly() {
        val settings = InstituteSettings().apply {
            deviceBindingMode = InstituteSettings.DEVICE_BINDING_MOBILE_APP_ONLY
        }
        assertTrue(settings.isDeviceBindingEnabled)
    }

    @Test
    fun isDeviceBindingEnabledReturnsTrueForCaseInsensitiveModes() {
        val upperMobileAndDesktop = InstituteSettings().apply {
            deviceBindingMode = "1_MOBILE_1_DESKTOP"
        }
        assertTrue(upperMobileAndDesktop.isDeviceBindingEnabled)

        val upperMobileOnly = InstituteSettings().apply {
            deviceBindingMode = "MOBILE_APP_ONLY"
        }
        assertTrue(upperMobileOnly.isDeviceBindingEnabled)
    }

    @Test
    fun isDeviceBindingEnabledReturnsFalseForNull() {
        val settings = InstituteSettings().apply {
            deviceBindingMode = null
        }
        assertFalse(settings.isDeviceBindingEnabled)
    }

    @Test
    fun isDeviceBindingEnabledReturnsFalseForEmptyString() {
        val settings = InstituteSettings().apply {
            deviceBindingMode = ""
        }
        assertFalse(settings.isDeviceBindingEnabled)
    }

    @Test
    fun isDeviceBindingEnabledReturnsFalseForDesktopAppOnly() {
        val settings = InstituteSettings().apply {
            deviceBindingMode = InstituteSettings.DEVICE_BINDING_DESKTOP_APP_ONLY
        }
        assertFalse(settings.isDeviceBindingEnabled)
    }

    @Test
    fun isDeviceBindingEnabledReturnsFalseForOff() {
        val settings = InstituteSettings().apply {
            deviceBindingMode = InstituteSettings.DEVICE_BINDING_OFF
        }
        assertFalse(settings.isDeviceBindingEnabled)

        val uppercaseSettings = InstituteSettings().apply {
            deviceBindingMode = "OFF"
        }
        assertFalse(uppercaseSettings.isDeviceBindingEnabled)
    }

    @Test
    fun isDeviceBindingEnabledReturnsFalseForWhitespace() {
        val settings = InstituteSettings().apply {
            deviceBindingMode = "   "
        }
        assertFalse(settings.isDeviceBindingEnabled)
    }

    @Test
    fun isDeviceBindingEnabledReturnsFalseForDisabled() {
        val settings = InstituteSettings().apply {
            deviceBindingMode = "disabled"
        }
        assertFalse(settings.isDeviceBindingEnabled)
    }

    @Test
    fun isDeviceBindingEnabledReturnsFalseForEnabled() {
        val settings = InstituteSettings().apply {
            deviceBindingMode = "enabled"
        }
        assertFalse(settings.isDeviceBindingEnabled)

        val upperSettings = InstituteSettings().apply {
            deviceBindingMode = "ENABLED"
        }
        assertFalse(upperSettings.isDeviceBindingEnabled)
    }

    @Test
    fun isDeviceBindingEnabledReturnsFalseForUnknownMode() {
        val settings = InstituteSettings().apply {
            deviceBindingMode = "unknown_mode_value"
        }
        assertFalse(settings.isDeviceBindingEnabled)
    }

    @Test
    fun appProductionParserDeserializesDeviceBindingModeFieldFromApiJson() {
        val json = """{"device_binding_mode": "1_mobile_1_desktop"}"""
        val settings = gson.fromJson(json, InstituteSettings::class.java)

        assertEquals("1_mobile_1_desktop", settings.deviceBindingMode)
        assertTrue(settings.isDeviceBindingEnabled)
    }

    @Test
    fun appProductionParserDeserializesMissingDeviceBindingModeAsNull() {
        val json = """{"allow_signup": true}"""
        val settings = gson.fromJson(json, InstituteSettings::class.java)

        assertNull(settings.deviceBindingMode)
        assertFalse(settings.isDeviceBindingEnabled)
    }
}
