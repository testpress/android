package `in`.testpress.testpress.util

import `in`.testpress.testpress.util.Validator.isEmailValid
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class EmailValidatorTest {
    @Test
    fun correctEmailReturnsTrue() {
        Assert.assertTrue(isEmailValid("name@email.com"))
    }

    @Test
    fun emailWithSubDomainReturnsTrue() {
        Assert.assertTrue(isEmailValid("name@email.co.uk"))
    }

    @Test
    fun emailWithNoTLDReturnsFalse() {
        Assert.assertFalse(isEmailValid("name@email"))
    }

    @Test
    fun emailWithDoubleDotReturnsFalse() {
        Assert.assertFalse(isEmailValid("name@email..com"))
    }

    @Test
    fun emailWithNoUsernameReturnsFalse() {
        Assert.assertFalse(isEmailValid("@email.com"))
    }

    @Test
    fun emptyStringReturnsFalse() {
        Assert.assertFalse(isEmailValid(""))
    }

    @Test
    fun nullReturnsFalse() {
        Assert.assertFalse(isEmailValid(null))
    }
}
