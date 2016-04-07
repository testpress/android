

package in.testpress.testpress.core;

import java.util.HashMap;

/**
 * Testpress constants
 */
public final class Constants {
    private Constants() {}

    public static final class Auth {
        private Auth() {}

        /**
         * Account name
         */
        public static final String TESTPRESS_ACCOUNT_NAME = "testpress";

        /**
         * Account type id
         */
        public static final String TESTPRESS_ACCOUNT_TYPE = "in.testpress." + TESTPRESS_ACCOUNT_NAME;

        /**
         * Provider id
         */
        public static final String TESTPRESS_PROVIDER_AUTHORITY = "in.testpress." + TESTPRESS_ACCOUNT_NAME + ".sync";

        /**
         * Auth token type
         */
        public static final String AUTHTOKEN_TYPE = TESTPRESS_ACCOUNT_TYPE;
    }

    public static final class Http {
        private Http() {}


        /**
         * Base URL for all requests
         */
        public static final String URL_BASE = "http://demo.testpress.in";

        /**
         * Check Update url
         */
        public static final String CHECK_UPDATE_URL_Frag = "/api/android/check/";

        /**
         * Reset password URl
         *
         */
        public  static final String RESET_PASSWORD_URL = "/api/v2_2/password/reset/";


        /**
         * Authentication URL
         */
        public static final String URL_AUTH_FRAG = "/api/v2/auth-token/";
        public static final String URL_AUTH = URL_BASE + URL_AUTH_FRAG;

        /**
         * New User Register URL
         */
        public static final String URL_REGISTER_FRAG = "/api/v2/register/";
        public static final String URL_REGISTER = URL_BASE + URL_REGISTER_FRAG;
        /**
         * Code Verification URL
         */
        public static final String URL_VERIFY_FRAG = "/api/v2/verify/";
        public static final String URL_VERIFY = URL_BASE + URL_VERIFY_FRAG;

        /**
         * List Users URL
         */
        public static final String URL_USERS_FRAG =  "/api/v2/users/";
        public static final String URL_USERS = URL_BASE + URL_USERS_FRAG;

        /**
         * List Products Exams URL
         */
        public static final String URL_PRODUCTS_FRAG = "api/v2.2/products/";

        /**
         * List Products Exams URL
         */
        public static final String URL_ORDERS_FRAG = "/api/v2/orders/";

        /**
         * Profile Details URL
         */
        public static final String URL_PROFILE_DETAILS_FRAG =  "/api/v2.1/me/";
        public static final String URL_PROFILE_DETAILS = URL_BASE + URL_PROFILE_DETAILS_FRAG;

        /**
         * List Available Exams URL
         */
        public static final String URL_AVAILABLE_EXAMS_FRAG =  "api/v2.2/exams/available/";
        public static final String URL_AVAILABLE_EXAMS = URL_BASE + URL_AVAILABLE_EXAMS_FRAG;

        /**
         * List Upcoming Exams URL
         */
        public static final String URL_UPCOMING_EXAMS_FRAG =  "api/v2.2/exams/upcoming/";
        public static final String URL_UPCOMING_EXAMS = URL_BASE + URL_UPCOMING_EXAMS_FRAG;

        /**
         * List History Exams URL
         */
        public static final String URL_HISTORY_EXAMS_FRAG =  "api/v2.2/exams/history/";
        public static final String URL_HISTORY_EXAMS = URL_BASE + URL_HISTORY_EXAMS_FRAG;

        /**
         * Start Exam URL
         */
        public static final String URL_START_EXAM_FRAG =  "api/v2.2/exams/start/";
        public static final String URL_START_EXAM = URL_BASE + URL_START_EXAM_FRAG;

        /**
         * End Exam URL
         */
        public static final String URL_END_EXAM_FRAG =  "end/";
        public static final String URL_MAIL_PDF_FRAG =  "pdf/";
        public static final String URL_MAIL_PDF_QUESTIONS_FRAG =  "pdf-questions/";


        /**
         * Devices Register URL
         */
        public static final String URL_DEVICES_REGISTER_FRAG =  "/api/v2.2/devices/register/";
        public static final String URL_DEVICES_REGISTER = URL_BASE + URL_DEVICES_REGISTER_FRAG;

        /**
         * Devices Unregister URL
         */
        public static final String URL_DEVICES_UNREGISTER_FRAG =  "/api/v2.2/devices/unregister/";
        public static final String URL_DEVICES_UNREGISTER = URL_BASE + URL_DEVICES_UNREGISTER_FRAG;

        /**
         * Posts URL
         */
        public static final String URL_POSTS_FRAG =  "api/v2.2/posts/";
        public static final String URL_POSTS = URL_BASE + URL_POSTS_FRAG;
        public static final String URL_CATEGORIES_FRAG = URL_POSTS_FRAG + "categories/";

        /**
         * PARAMS for auth
         */
        public static final String PARAM_USERNAME = "username";
        public static final String PARAM_PASSWORD = "password";
        public static final String CONTENT_TYPE_JSON = "application/json";
        public static final String USERNAME = "username";
        public static final String PASSWORD = "password";
        public static final String SESSION_TOKEN = "sessionToken";


    }


    public static final class Extra {
        private Extra() {}
    }

    public static final class Intent {
        private Intent() {}

        /**
         * Action prefix for all intents created
         */
        public static final String INTENT_PREFIX = "in.testpress." + Auth.TESTPRESS_ACCOUNT_NAME + ".";

    }

    public static class Notification {
        private Notification() {
        }

        public static final int TIMER_NOTIFICATION_ID = 1000; // Why 1000? Why not? :)
    }

    public static final String GCM_PREFERENCE_NAME = "testpress." + Auth.TESTPRESS_ACCOUNT_NAME + ".GCM";
    public static final String GCM_PROPERTY_REG_ID = "GCMRegId";
    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";

    public static final HashMap<String, Integer> genderChoices;
    static
    {
        genderChoices = new HashMap<String, Integer>();
        genderChoices.put("--select--", -1);
        genderChoices.put("Male", 1);
        genderChoices.put("Female", 2);
        genderChoices.put("Transgender", 3);
    }

    public static final HashMap<String, Integer> stateChoices;
    static
    {
        stateChoices = new HashMap<String, Integer>();
        String[] states = {
                "--select--", "Others", "Andaman and Nicobar Islands", "Andhra Pradesh", "Arunachal Pradesh", "Assam",
                "Bihar", "Chandigarh", "Chhattisgarh", "Dadra and Nagar Haveli", "Daman and Diu", "Delhi", "Goa", "Gujarat",
                "Haryana", "Himachal Pradesh", "Jammu and Kashmir", "Jharkhand", "Karnataka", "Kerala", "Lakshadweep",
                "Madhya Pradesh", "Maharashtra", "Manipur", "Meghalaya", "Mizoram", "Nagaland", "Odisha", "Punjab", "Pondicherry",
                "Rajasthan", "Sikkim", "Tamil Nadu", "Tripura", "Uttar Pradesh", "Uttarakhand", "West Bengal", "Telengana"
        };
        for (int i = -1; i < 37; i++) {
            stateChoices.put(states[i + 1], i);
        }
    }
}
