package sharelocation.cbgames.android.sharelocation;

import android.content.Context;
import android.preference.PreferenceManager;

public class QueryPreferences {
    private static final String PREF_AUTH_HASH = "auth_hash";
    private static final String PREF_CHECK_LOCATION = "checked_location";

    public static boolean isCheckLocation(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_CHECK_LOCATION, false);
    }
    public static void setCheckLocation(Context context, boolean isOn) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(PREF_CHECK_LOCATION, isOn)
                .apply();
    }

    public static String getAuthHash(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_AUTH_HASH, "");
    }//

    public static void setAuthHash(Context context, String hash) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_AUTH_HASH, hash)
                .apply();
    }
}
