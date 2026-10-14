package `in`.testpress.testpress.util

import `in`.testpress.testpress.R
import `in`.testpress.testpress.models.InstituteSettings
import android.content.Context
import androidx.appcompat.app.AlertDialog
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito.mock

@RunWith(JUnit4::class)
class DeviceBindingDialogsTest {

    private val mockContext = mock(Context::class.java)
    private val mockDialog = mock(AlertDialog::class.java)

    private var capturedTitleRes: Int? = null
    private var capturedMessageRes: Int? = null
    private var capturedOnConfirm: Runnable? = null
    private var capturedOnCancel: Runnable? = null
    private var dialogBuildCount = 0

    private val originalDialogBuilder = DeviceBindingDialogs.dialogBuilder

    @Before
    fun setUp() {
        dialogBuildCount = 0
        capturedTitleRes = null
        capturedMessageRes = null
        capturedOnConfirm = null
        capturedOnCancel = null

        DeviceBindingDialogs.dialogBuilder = { _, title, message, onConfirm, onCancel ->
            dialogBuildCount++
            capturedTitleRes = title
            capturedMessageRes = message
            capturedOnConfirm = onConfirm
            capturedOnCancel = onCancel
            mockDialog
        }
    }

    @After
    fun tearDown() {
        DeviceBindingDialogs.dialogBuilder = originalDialogBuilder
    }

    @Test
    fun confirmIfBindingEnabledCallsOnProceedDirectlyWhenBindingDisabled() {
        var proceedCalled = false
        var cancelCalled = false
        val settings = InstituteSettings().apply { deviceBindingMode = "disabled" }

        val result = DeviceBindingDialogs.confirmIfBindingEnabled(
            mockContext,
            settings,
            onProceed = { proceedCalled = true },
            onCancel = { cancelCalled = true }
        )

        assertNull(result)
        assertTrue(proceedCalled)
        assertFalse(cancelCalled)
        assertEquals(0, dialogBuildCount)
    }

    @Test
    fun confirmIfBindingEnabledCallsOnProceedDirectlyWhenSettingsNull() {
        var proceedCalled = false
        var cancelCalled = false

        val result = DeviceBindingDialogs.confirmIfBindingEnabled(
            mockContext,
            null,
            onProceed = { proceedCalled = true },
            onCancel = { cancelCalled = true }
        )

        assertNull(result)
        assertTrue(proceedCalled)
        assertFalse(cancelCalled)
        assertEquals(0, dialogBuildCount)
    }

    @Test
    fun confirmIfBindingEnabledShowsDialogWhenBindingEnabled() {
        var proceedCalled = false
        var cancelCalled = false
        val settings = InstituteSettings().apply {
            deviceBindingMode = InstituteSettings.DEVICE_BINDING_1_MOBILE_1_DESKTOP
        }

        val result = DeviceBindingDialogs.confirmIfBindingEnabled(
            mockContext,
            settings,
            onProceed = { proceedCalled = true },
            onCancel = { cancelCalled = true }
        )

        assertEquals(mockDialog, result)
        assertEquals(1, dialogBuildCount)
        assertEquals(R.string.device_binding_confirm_title, capturedTitleRes)
        assertEquals(R.string.device_binding_confirm_message, capturedMessageRes)
        assertFalse(proceedCalled)
        assertFalse(cancelCalled)

        // When user confirms
        capturedOnConfirm?.run()
        assertTrue(proceedCalled)
        assertFalse(cancelCalled)
    }

    @Test
    fun confirmIfBindingEnabledInvokesOnCancelWhenCancelled() {
        var proceedCalled = false
        var cancelCalled = false
        val settings = InstituteSettings().apply {
            deviceBindingMode = InstituteSettings.DEVICE_BINDING_MOBILE_APP_ONLY
        }

        DeviceBindingDialogs.confirmIfBindingEnabled(
            mockContext,
            settings,
            onProceed = { proceedCalled = true },
            onCancel = { cancelCalled = true }
        )

        capturedOnCancel?.run()
        assertFalse(proceedCalled)
        assertTrue(cancelCalled)
    }

    @Test
    fun handleLogoutWhenDisabledShowsRegularConfirmationDirectly() {
        var proceedToLogoutCalled = false
        val settings = InstituteSettings().apply { deviceBindingMode = null }

        DeviceBindingDialogs.handleLogout(
            mockContext,
            settings,
            onProceedToLogout = { proceedToLogoutCalled = true }
        )

        assertEquals(1, dialogBuildCount)
        assertEquals(R.string.logout, capturedTitleRes)
        assertEquals(R.string.logout_confirm_message, capturedMessageRes)

        capturedOnConfirm?.run()
        assertTrue(proceedToLogoutCalled)
    }

    @Test
    fun handleLogoutWhenEnabledChainsWarningToRegularConfirmation() {
        var proceedToLogoutCalled = false
        val settings = InstituteSettings().apply {
            deviceBindingMode = InstituteSettings.DEVICE_BINDING_ENABLED
        }

        DeviceBindingDialogs.handleLogout(
            mockContext,
            settings,
            onProceedToLogout = { proceedToLogoutCalled = true }
        )

        // First dialog is the warning
        assertEquals(1, dialogBuildCount)
        assertEquals(R.string.device_binding_logout_warning_title, capturedTitleRes)
        assertEquals(R.string.device_binding_logout_warning_message, capturedMessageRes)
        assertFalse(proceedToLogoutCalled)

        // Confirming warning triggers the second dialog (regular confirmation)
        capturedOnConfirm?.run()
        assertEquals(2, dialogBuildCount)
        assertEquals(R.string.logout, capturedTitleRes)
        assertEquals(R.string.logout_confirm_message, capturedMessageRes)
        assertFalse(proceedToLogoutCalled)

        // Confirming regular logout invokes proceedToLogout
        capturedOnConfirm?.run()
        assertTrue(proceedToLogoutCalled)
    }

    @Test
    fun handleLogoutWhenWarningCancelledDoesNotProceed() {
        var proceedToLogoutCalled = false
        var cancelCalled = false
        val settings = InstituteSettings().apply {
            deviceBindingMode = InstituteSettings.DEVICE_BINDING_ENABLED
        }

        DeviceBindingDialogs.handleLogout(
            mockContext,
            settings,
            onProceedToLogout = { proceedToLogoutCalled = true },
            onCancel = { cancelCalled = true }
        )

        capturedOnCancel?.run()
        assertEquals(1, dialogBuildCount)
        assertFalse(proceedToLogoutCalled)
        assertTrue(cancelCalled)
    }
}
