package in.testpress.testpress.util;

import java.util.regex.Pattern;

public class Validator {

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