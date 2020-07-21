package in.testpress.testpress.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static in.testpress.testpress.util.Validator.isEmailValid;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class EmailValidatorTests {

    @Test public void correctEmailReturnsTrue() {
        assertTrue(isEmailValid("name@email.com"));
    }

    @Test
    public void emailWithSubDomainReturnsTrue() {
        assertTrue(isEmailValid("name@email.co.uk"));
    }

    @Test
    public void emailWithNoTLDReturnsFalse() {
        assertFalse(isEmailValid("name@email"));
    }

    @Test
    public void emailWithDoubleDotReturnsFalse() {
        assertFalse(isEmailValid("name@email..com"));
    }

    @Test
    public void emailWithNoUsernameReturnsFalse() {
        assertFalse(isEmailValid("@email.com"));
    }

    @Test
    public void emptyStringReturnsFalse() {
        assertFalse(isEmailValid(""));
    }

    @Test
    public void nullReturnsFalse() {
        assertFalse(isEmailValid(null));
    }
}