package in.testpress.testpress.util;

import com.hbb20.CountryCodePicker;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validator {
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

    public static boolean isEmailValid(String email)
    {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if (email == null)
            return false;
        return pat.matcher(email).matches();
    }

    public static boolean isUsernameValid(String username) {
        String usernameRegex = "^[A-Za-z0-9_@.+-]*";

        Pattern pattern = Pattern.compile(usernameRegex);
        if (username == null){
            return false;
        }
        return pattern.matcher(username).matches();
    }
}
