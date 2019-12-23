package in.testpress.testpress.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;


@RunWith(PowerMockRunner.class)
public class PhoneNumberValidatorTests {
    @Test
    public void ValidatePhoneNumber_ShouldReturnTrue_ForValidPhoneNumber() {
        assertEquals(true, PhoneNumberValidator.validatePhoneNumber("1234567890"));
        assertEquals(true, PhoneNumberValidator.validatePhoneNumber("9900990099"));
    }

    @Test
    public void ValidatePhoneNumber_ShouldReturnFalse_ForPhoneNumberLengthLessThanTen() {
        assertEquals(false, PhoneNumberValidator.validatePhoneNumber("123456789"));
    }

    @Test
    public void ValidatePhoneNumber_ShouldReturnFalse_ForPhoneNumberLengthGreaterThanTen() {
        assertEquals(false, PhoneNumberValidator.validatePhoneNumber("12345678901"));
    }

    @Test
    public void ValidatePhoneNumber_ShouldReturnFalse_ForPhoneNumberContainingCharacter() {
        assertEquals(false, PhoneNumberValidator.validatePhoneNumber("12345a7890"));
        assertEquals(false, PhoneNumberValidator.validatePhoneNumber("123456789q"));
        assertEquals(false, PhoneNumberValidator.validatePhoneNumber("w234567890"));
    }

    @Test
    public void ValidatePhoneNumber_ShouldReturnFalse_ForPhoneNumberContainingSpecialCharacter() {
        assertEquals(false, PhoneNumberValidator.validatePhoneNumber("12345@7890"));
        assertEquals(false, PhoneNumberValidator.validatePhoneNumber("123457890_"));
        assertEquals(false, PhoneNumberValidator.validatePhoneNumber("!123457890"));
    }

    @Test
    public void ValidatePhoneNumber_ShouldReturnFalse_ForPhoneNumberStartingWithZero() {
        assertEquals(false, PhoneNumberValidator.validatePhoneNumber("0234567891"));
    }
}
