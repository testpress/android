package in.testpress.testpress.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static in.testpress.testpress.util.Validator.isUsernameValid;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class UserNameValidatorTests {

    @Test
    public void alphaNumericUsernameReturnsTrue() {
        assertTrue(isUsernameValid("name05"));
    }

    @Test
    public void onlyAlphabetsUsernameReturnsTrue() {
        assertTrue(isUsernameValid("name"));
    }

    @Test
    public void usernameWithHyphenReturnsTrue() {
        assertTrue(isUsernameValid("name-name"));
    }

    @Test
    public void usernameWithPeriodReturnsTrue() {
        assertTrue(isUsernameValid("name.name"));
    }

    @Test
    public void usernameWithPlusReturnsTrue() {
        assertTrue(isUsernameValid("+name"));
    }

    @Test
    public void usernameWithUnderscoreReturnsTrue() {
        assertTrue(isUsernameValid("name_05"));
    }

    @Test
    public void usernameWithAtSignReturnsTrue() {
        assertTrue(isUsernameValid("name@s"));
    }

    @Test
    public void usernameWithAlphaNumericAndSpecialCharactersReturnsTrue() {
        assertTrue(isUsernameValid("name@123.s+76-sa0"));
    }

    @Test
    public void usernameWithAsteriskReturnsFalse() {
        assertFalse(isUsernameValid("name*"));
    }

    @Test
    public void usernameWithSpecialCharacterReturnsFalse() {
        assertFalse(isUsernameValid("^name%"));
    }

    @Test
    public void usernameWithBracketsReturnsFalse() {
        assertFalse(isUsernameValid("[name)"));
    }
}
