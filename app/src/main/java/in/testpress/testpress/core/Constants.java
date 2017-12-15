

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
        public static final String TESTPRESS_ACCOUNT_NAME = "lmsdemo";

        /**
         * Account type id
         */
        public static final String TESTPRESS_ACCOUNT_TYPE = "in.testpress.tech." + TESTPRESS_ACCOUNT_NAME;

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
        public static final String URL_BASE = "http://lmsdemo.testpress.in";

        /**
         * Check Update url
         */
        public static final String CHECK_UPDATE_URL_Frag = "/api/android/check/";

        /**
         * Reset password URl
         *
         */
        public  static final String RESET_PASSWORD_URL = "/api/v2.2/password/reset/";

        /**
         * Authentication URL
         */
        public static final String URL_AUTH_FRAG = "/api/v2.2/auth-token/";

        /**
         * New User Register URL
         */
        public static final String URL_REGISTER_FRAG = "/api/v2.2/register/";
        /**
         * Code Verification URL
         */
        public static final String URL_VERIFY_FRAG = "/api/v2.2/verify/";

        /**
         * List Products URL
         */
        public static final String URL_PRODUCTS_FRAG = "api/v2.2/products/";

        /**
         * List Documents URL
         */
        public static final String URL_DOCUMENTS_FRAG = "api/v2.2/notes/";

        /**
         * List Products Exams URL
         */
        public static final String URL_ORDERS_FRAG = "/api/v2.2/orders/";

        /**
         * Profile Details URL
         */
        public static final String URL_PROFILE_DETAILS_FRAG =  "/api/v2.2/me/";

        /**
         * Devices Register URL
         */
        public static final String URL_DEVICES_REGISTER_FRAG =  "/api/v2.2/devices/register/";

        /**
         * Institute Settings URL
         */
        public static final String URL_INSTITUTE_SETTINGS_FRAG =  "/api/v2.2/settings/";

        /**
         * Posts URL
         */
        public static final String URL_POSTS_FRAG =  "api/v2.2/posts/";
        public static final String URL_CATEGORIES_FRAG = URL_POSTS_FRAG + "categories/";

        public static final String URL_FORUMS_FRAG =  "api/v2.3/forum/";

        public static final String URL_COMMENTS_FRAG =  "/comments/";

        public static final String CHAPTERS_PATH =  "/api/v2.2/chapters/";

        /**
         * Handle Success & Failure of payments
         */
        public static final String URL_PAYMENT_RESPONSE_HANDLER = URL_BASE + "/payments/response/payu/";

        /**
         * Query Params
         */
        public static final String PARENT = "parent";
        public static final String PAGE = "page";
        public static final String SINCE = "since";
        public static final String UNTIL = "until";
        public static final String ORDER = "order";
    }

    public static final String GCM_PREFERENCE_NAME = "testpress." + Auth.TESTPRESS_ACCOUNT_NAME + ".GCM";
    public static final String GCM_PROPERTY_REG_ID = "GCMRegId";

    public static final String IS_DEEP_LINK = "is_deep_link";
    public static final String DEEP_LINK_TO = "deep_link_to";
    public static final String DEEP_LINK_TO_PAYMENTS = "deep_link_to_payments";
    public static final String DEEP_LINK_TO_POST = "deep_link_to_post";

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
