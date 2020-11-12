package `in`.testpress.testpress.util

import `in`.testpress.testpress.enums.VerificationMethod
import `in`.testpress.testpress.models.UserDetails
import `in`.testpress.testpress.util.fakes.FakeRegisterFormUserInputValidation
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class RegisterFormUserInputValidatorTest {

    @Test
    fun validDataShouldReturnTrue() {
        val registerFormUserInputValidation = FakeRegisterFormUserInputValidation(UserDetails(
                "helen01",
                "helen01@gmail.com",
                "9876543210",
                "1234567",
                "1234567"
        ), VerificationMethod.MOBILE)

        Assert.assertTrue(registerFormUserInputValidation.isValid())
    }

    @Test
    fun invalidEmailShouldReturnFalse() {
        val registerFormUserInputValidation = FakeRegisterFormUserInputValidation(UserDetails(
                "helen01",
                "helen01com.tf",
                "9876543210",
                "1234567",
                "1234567"
        ), VerificationMethod.MOBILE)

        Assert.assertEquals(false,registerFormUserInputValidation.isValid())
    }

    @Test
    fun invalidUserNameShouldReturnFalse() {
        val registerFormUserInputValidation = FakeRegisterFormUserInputValidation(UserDetails(
                "helen01!*()",
                "helen01com.tf",
                "9876543210",
                "1234567",
                "1234567"
        ), VerificationMethod.MOBILE)

        Assert.assertEquals(false,registerFormUserInputValidation.isValid())
    }

    @Test
    fun samePasswordAndConfirmPasswordShouldReturnTrue() {
        val registerFormUserInputValidation = FakeRegisterFormUserInputValidation(UserDetails(
                "helen01!*()",
                "helen01com.tf",
                "9876543210",
                "password",
                "password"
        ), VerificationMethod.MOBILE)

        Assert.assertEquals(false,registerFormUserInputValidation.isValid())
    }
}
