package `in`.testpress.testpress.util

import `in`.testpress.testpress.util.Validator.isUsernameValid
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class UserNameValidatorTest {
    @Test
    fun alphaNumericUsernameReturnsTrue() {
        Assert.assertTrue(isUsernameValid("name05"))
    }

    @Test
    fun onlyAlphabetsUsernameReturnsTrue() {
        Assert.assertTrue(isUsernameValid("name"))
    }

    @Test
    fun usernameWithHyphenReturnsTrue() {
        Assert.assertTrue(isUsernameValid("name-name"))
    }

    @Test
    fun usernameWithPeriodReturnsTrue() {
        Assert.assertTrue(isUsernameValid("name.name"))
    }

    @Test
    fun usernameWithPlusReturnsTrue() {
        Assert.assertTrue(isUsernameValid("+name"))
    }

    @Test
    fun usernameWithUnderscoreReturnsTrue() {
        Assert.assertTrue(isUsernameValid("name_05"))
    }

    @Test
    fun usernameWithAtSignReturnsTrue() {
        Assert.assertTrue(isUsernameValid("name@s"))
    }

    @Test
    fun usernameWithAlphaNumericAndSpecialCharactersReturnsTrue() {
        Assert.assertTrue(isUsernameValid("name@123.s+76-sa0"))
    }

    @Test
    fun usernameWithAsteriskReturnsFalse() {
        Assert.assertFalse(isUsernameValid("name*"))
    }

    @Test
    fun usernameWithSpecialCharacterReturnsFalse() {
        Assert.assertFalse(isUsernameValid("^name%"))
    }

    @Test
    fun usernameWithBracketsReturnsFalse() {
        Assert.assertFalse(isUsernameValid("[name)"))
    }
}