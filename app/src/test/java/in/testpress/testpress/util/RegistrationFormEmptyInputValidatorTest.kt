package `in`.testpress.testpress.util

import `in`.testpress.testpress.models.UserDetails
import `in`.testpress.testpress.util.fakes.FakeRegistrationFormEmptyInputValidation
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class RegistrationFormEmptyInputValidatorTest {
    @Test
    fun nonEmptyDataShouldReturnTrue() {
        val registrationFormEmptyInputValidation = FakeRegistrationFormEmptyInputValidation(UserDetails(
                "helen01",
                "helen01@gmail.com",
                "9876543210",
                "1234567",
                "1234567"
        ))

        Assert.assertTrue(registrationFormEmptyInputValidation.isValid())
    }

    @Test
    fun emptyEmailShouldReturnFalse() {
        val registrationFormEmptyInputValidation = FakeRegistrationFormEmptyInputValidation(UserDetails(
                "helen01",
                "",
                "9876543210",
                "1234567",
                "1234567"
        ))

        Assert.assertEquals(false, registrationFormEmptyInputValidation.isValid())
    }

    @Test
    fun emptyUserNameShouldReturnFalse() {
        val registrationFormEmptyInputValidation = FakeRegistrationFormEmptyInputValidation(UserDetails(
                "",
                "helen01@gmail.com",
                "9876543210",
                "1234567",
                "1234567"
        ))

        Assert.assertEquals(false, registrationFormEmptyInputValidation.isValid())
    }

    @Test
    fun emptyPasswordShouldReturnTrue() {
        val registrationFormEmptyInputValidation = FakeRegistrationFormEmptyInputValidation(UserDetails(
                "helen01",
                "helen01@gmail.com",
                "9876543210",
                "",
                ""
        ))

        Assert.assertEquals(false, registrationFormEmptyInputValidation.isValid())
    }
}
