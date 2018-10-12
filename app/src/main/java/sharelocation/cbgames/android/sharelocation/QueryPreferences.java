package sharelocation.cbgames.android.sharelocation;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;

import com.google.android.gms.maps.model.LatLng;

public class QueryPreferences {
    private static final String PREF_AUTH_HASH = "auth_hash";
    private static final String PREF_CHECK_LOCATION = "checked_location";
    private static final String PREF_SHARECODE = "sharecode";
    private static final String PREF_TIMEOVER_CODE = "timeover_code";
    private static final String PREF_APP_PASSWORD = "app_password";
    private static final String PREF_NOTIFICATION = "notification";

    public static void setAppPasswrd(Context context, String password){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_APP_PASSWORD, password)
                .apply();
    }


    public static String getAppPasswrd(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_APP_PASSWORD, "");
    }

    public static boolean isCheckLocation(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_CHECK_LOCATION, true);
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
    }

    public static void setAuthHash(Context context, String hash) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_AUTH_HASH, hash)
                .apply();
    }

    public static String getShareCode(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_SHARECODE, "");
    }

    public static void setShareCode(Context context, String sharecode) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_SHARECODE, sharecode)
                .apply();
    }


    public static long getTimeOverCode(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getLong(PREF_TIMEOVER_CODE, 0);
    }

    public static void setTimeOverCode(Context context, long timeover_code) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putLong(PREF_TIMEOVER_CODE, timeover_code)
                .apply();
    }


    public static boolean getNotification(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_NOTIFICATION, true);
    }

    public static void setNotification(Context context, boolean isOn){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(PREF_NOTIFICATION, isOn)
                .apply();
    }

}
