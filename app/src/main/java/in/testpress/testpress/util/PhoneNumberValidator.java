package in.testpress.testpress.util;

import com.hbb20.CountryCodePicker;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhoneNumberValidator {
    public static boolean validatePhoneNumber(String phoneNumber) {
        Pattern phoneNumberPattern = Pattern.compile("^[1-9]{1}[0-9]{9}$");
        Matcher phoneNumberMatcher = phoneNumberPattern.matcher(phoneNumber);

        if (phoneNumberMatcher.matches()) {
            return true;
        }
        return false;
    }

    public static boolean validateInternationalPhoneNumber(CountryCodePicker countryCodePicker) {
        return countryCodePicker.isValidFullNumber();
    }
}
