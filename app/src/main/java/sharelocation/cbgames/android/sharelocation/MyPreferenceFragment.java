package sharelocation.cbgames.android.sharelocation;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MyPreferenceFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "MyPreferenceFragment";
    /*
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }
    */

    private Context mContext;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings);

        mContext = getContext();
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(mContext);
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        switch (key){
            case "notification":
                Boolean isOn = prefs.getBoolean("notification", true);
                AppNotification.ShowPermanent(mContext, isOn);
                break;
            case "app_password":
                //String pass = md5(prefs.getString("app_password", "12345"));
                String pass = prefs.getString("app_password", "12345");
                if(pass.length() <= 1) {
                    QueryPreferences.setAppPasswrd(mContext, "");
                }else {
                    if (pass.length() > 3) {
                        QueryPreferences.setAppPasswrd(mContext, pass);
                    } else {
                        Toast.makeText(mContext, R.string.pass_length_error, Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public static final String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
