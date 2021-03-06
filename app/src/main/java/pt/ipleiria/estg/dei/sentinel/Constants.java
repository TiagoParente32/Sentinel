package pt.ipleiria.estg.dei.sentinel;

public final class Constants {

    public static final String KEEP_SIGNEDIN = "keep_signed_in";



    public static final String API_KEY = "";//missing
    public static final String API_SECRET = "";//missing
    public static final String CALLBACKURL = "app://sentinel";


    public static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
    public static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";
    public static final String PREF_KEY_TWITTER_LOGIN = "isTwitterLoggedIn";

    // Twitter oauth urls
    public static final String URL_TWITTER_OAUTH_VERIFIER = "oauth_verifier";


    //INTENT TAGS
    public static final String DATA_INTENT_TEMPERATURE = "data_intent_temperature";
    public static final String DATA_INTENT_HUMIDITY = "data_intent_humidity";
    public static final String DATA_INTENT_AIRQUALITY = "data_intent_airquality";
    public static final String DATA_INTENT_LOCATION = "data_intent_location";
    public static final String DATA_INTENT_SPINNER_DATA= "data_intent_spinner_adapter";


    public static final String PREFERENCES_FILE_NAME = "pt.ipleiria.estg.dei.sentinel.SHARED_PREFERENCES";
    public static final String PREFERENCES_FAVORITES_SET ="favorites_string_set";

    public static final String PREFERENCES_EXPOSURE_SET ="exposure_string_set";

    public static final String PREFERENCES_NOTIFICATIONS_SET = "notifications_string_set";
    public static final String PREFERENCES_NOTIFICATIONS_UNREAD = "notifications_unread_int";

    public static final String PREFERENCES_NOTIFICATIONS_ON ="notifications_bool_on";

    public static final String PREFERENCES_LOGGED_IN = "user_logged_in";
}
