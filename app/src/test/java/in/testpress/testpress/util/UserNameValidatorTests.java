package in.testpress.testpress.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class UserNameValidatorTests {

    @Test
    public void usernameValidator_AlphaNumericUsername_ReturnsTrue() {
        assertTrue(Validator.isUsernameValid("name05"));
    }

    @Test
    public void usernameValidator_OnlyAlphabetsUsername_ReturnsTrue() {
        assertTrue(Validator.isUsernameValid("name"));
    }

    @Test
    public void usernameValidator_UsernameWithHyphen_ReturnsTrue() {
        assertTrue(Validator.isUsernameValid("name-name"));
    }

    @Test
    public void usernameValidator_UsernameWithPeriod_ReturnsTrue() {
        assertTrue(Validator.isUsernameValid("name.name"));
    }

    @Test
    public void usernameValidator_UsernameWithPlus_ReturnsTrue() {
        assertTrue(Validator.isUsernameValid("+name"));
    }

    @Test
    public void usernameValidator_UsernameWithUnderscore_ReturnsTrue() {
        assertTrue(Validator.isUsernameValid("name_05"));
    }

    @Test
    public void usernameValidator_UsernameWithAtSign_ReturnsTrue() {
        assertTrue(Validator.isUsernameValid("name@s"));
    }

    @Test
    public void usernameValidator_UsernameWithAsterisk_ReturnsFalse() {
        assertFalse(Validator.isUsernameValid("name*"));
    }

    @Test
    public void usernameValidator_UsernameWithSpecialCharacter_ReturnsFalse() {
        assertFalse(Validator.isUsernameValid("^name%"));
    }

    @Test
    public void usernameValidator_UsernameWithBraces_ReturnsFalse() {
        assertFalse(Validator.isUsernameValid("[name)"));
    }

    @Test
    public void usernameValidator_UsernameWithAlphaNumericAndSpecialCharacters_ReturnsTrue() {
        assertTrue(Validator.isUsernameValid("name@123.s+76-sa0"));
    }
}
