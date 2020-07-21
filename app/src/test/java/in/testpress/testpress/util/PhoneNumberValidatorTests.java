package in.testpress.testpress.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import static in.testpress.testpress.util.Validator.validatePhoneNumber;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


@RunWith(PowerMockRunner.class)
public class PhoneNumberValidatorTests {
    @Test
    public void validPhoneNumberReturnsTrue() {
        assertTrue(validatePhoneNumber("1234567890"));
        assertTrue(validatePhoneNumber("9900990099"));
    }

    @Test
    public void phoneNumberLengthLessThanTenReturnsFalse() {
        assertFalse(validatePhoneNumber("123456789"));
    }

    @Test
    public void phoneNumberLengthGreaterThanTenReturnsFalse() {
        assertFalse(validatePhoneNumber("12345678901"));
    }

    @Test
    public void phoneNumberContainingCharacterReturnsFalse() {
        assertFalse(validatePhoneNumber("12345a7890"));
        assertFalse(validatePhoneNumber("123456789q"));
        assertFalse(validatePhoneNumber("w234567890"));
    }

    @Test
    public void phoneNumberContainingSpecialCharacterReturnsFalse() {
        assertFalse(validatePhoneNumber("12345@7890"));
        assertFalse(validatePhoneNumber("123457890_"));
        assertFalse(validatePhoneNumber("!123457890"));
    }

    @Test
    public void phoneNumberStartingWithZeroReturnsFalse() {
        assertFalse(validatePhoneNumber("0234567891"));
    }
}
