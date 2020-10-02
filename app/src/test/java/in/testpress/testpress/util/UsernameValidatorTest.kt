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
    fun usernameWithUnderscoreReturnsTrue() {
        Assert.assertTrue(isUsernameValid("name_05"))
    }

    @Test
    fun usernameWithHyphenReturnsFalse() {
        Assert.assertFalse(isUsernameValid("name-name"))
    }

    @Test
    fun usernameWithPeriodReturnsFalse() {
        Assert.assertFalse(isUsernameValid("name.name"))
    }

    @Test
    fun usernameWithPlusReturnsFalse() {
        Assert.assertFalse(isUsernameValid("+name"))
    }

    @Test
    fun usernameWithAtSignReturnsFalse() {
        Assert.assertFalse(isUsernameValid("name@s"))
    }

    @Test
    fun usernameWithAlphaNumericAndSpecialCharactersReturnsFalse() {
        Assert.assertFalse(isUsernameValid("name@123.s+76-sa0"))
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
