package `in`.testpress.testpress.util

import `in`.testpress.testpress.authenticator.RegisterActivity
import `in`.testpress.testpress.models.UserDetails
import `in`.testpress.testpress.util.fakes.FakeRegisterFormValidation
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class RegisterFormValidationTest {

    @Test
    fun validDataShouldReturnTrue() {
        val registerFormValidation = FakeRegisterFormValidation(UserDetails(
                "helen01",
                "helen01@gmail.com",
                "9876543210",
                "1234567",
                "1234567"
        ), RegisterActivity.VerificationMethod.MOBILE)

        Assert.assertTrue(registerFormValidation.isValid())
    }

    @Test
    fun invalidEmailShouldReturnFalse() {
        val registerFormValidation = FakeRegisterFormValidation(UserDetails(
                "helen01",
                "helen01com.tf",
                "9876543210",
                "1234567",
                "1234567"
        ), RegisterActivity.VerificationMethod.MOBILE)

        Assert.assertEquals(false,registerFormValidation.isValid())
    }

    @Test
    fun invalidUserNameShouldReturnFalse() {
        val registerFormValidation = FakeRegisterFormValidation(UserDetails(
                "helen01!*()",
                "helen01com.tf",
                "9876543210",
                "1234567",
                "1234567"
        ), RegisterActivity.VerificationMethod.MOBILE)

        Assert.assertEquals(false,registerFormValidation.isValid())
    }

    @Test
    fun samePasswordAndConfirmPasswordShouldReturnTrue() {
        val registerFormValidation = FakeRegisterFormValidation(UserDetails(
                "helen01!*()",
                "helen01com.tf",
                "9876543210",
                "password",
                "password"
        ), RegisterActivity.VerificationMethod.MOBILE)

        Assert.assertEquals(false,registerFormValidation.isValid())
    }
}
