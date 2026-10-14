package `in`.testpress.testpress.models

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class InstituteSettingsDeviceBindingTest {

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
    fun isDeviceBindingEnabledReturnsTrueForEnabledCaseInsensitive() {
        val lowercaseSettings = InstituteSettings().apply {
            deviceBindingMode = InstituteSettings.DEVICE_BINDING_ENABLED
        }
        assertTrue(lowercaseSettings.isDeviceBindingEnabled)

        val uppercaseSettings = InstituteSettings().apply {
            deviceBindingMode = "ENABLED"
        }
        assertTrue(uppercaseSettings.isDeviceBindingEnabled)
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
    fun isDeviceBindingEnabledReturnsFalseForUnknownMode() {
        val settings = InstituteSettings().apply {
            deviceBindingMode = "unknown_mode_value"
        }
        assertFalse(settings.isDeviceBindingEnabled)
    }
}
