

package in.testpress.testpress.core;

/**
 * Testpress constants
 */
public final class Constants {
    private Constants() {}

    public static final class Auth {
        private Auth() {}

        /**
         * Account type id
         */
        public static final String TESTPRESS_ACCOUNT_TYPE = "in.testpress.testpress";

        /**
         * Account name
         */
        public static final String TESTPRESS_ACCOUNT_NAME = "testpress";

        /**
         * Provider id
         */
        public static final String TESTPRESS_PROVIDER_AUTHORITY = "in.testpress.testpress.sync";

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
        public static final String URL_BASE = "https://demo.testpress.in";

        /**
         * Check Update url
         */
        public static final String CHECK_UPDATE_URL_Frag = "/api/android/check/";


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
        public static final String URL_PRODUCTS_FRAG = "api/v2.1/products/";

        /**
         * List Available Exams URL
         */
        public static final String URL_AVAILABLE_EXAMS_FRAG =  "api/v2.1/exams/available/";
        public static final String URL_AVAILABLE_EXAMS = URL_BASE + URL_AVAILABLE_EXAMS_FRAG;

        /**
         * List Upcoming Exams URL
         */
        public static final String URL_UPCOMING_EXAMS_FRAG =  "api/v2.1/exams/upcoming/";
        public static final String URL_UPCOMING_EXAMS = URL_BASE + URL_UPCOMING_EXAMS_FRAG;

        /**
         * List History Exams URL
         */
        public static final String URL_HISTORY_EXAMS_FRAG =  "api/v2.1/exams/history/";
        public static final String URL_HISTORY_EXAMS = URL_BASE + URL_HISTORY_EXAMS_FRAG;

        /**
         * Start Exam URL
         */
        public static final String URL_START_EXAM_FRAG =  "api/v2.1/exams/start/";
        public static final String URL_START_EXAM = URL_BASE + URL_START_EXAM_FRAG;

        /**
         * End Exam URL
         */
        public static final String URL_END_EXAM_FRAG =  "end/";
        public static final String URL_END_EXAM = URL_BASE + URL_END_EXAM_FRAG;

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
        public static final String INTENT_PREFIX = "in.testpress.testpress.";

    }

    public static class Notification {
        private Notification() {
        }

        public static final int TIMER_NOTIFICATION_ID = 1000; // Why 1000? Why not? :)
    }

}


