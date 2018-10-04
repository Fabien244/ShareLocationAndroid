package sharelocation.cbgames.android.sharelocation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceActivity;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class SplashActivity extends AppCompatActivity  {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID).toString();
        new AuthTask().execute("http://sharelocation.games-cb.com/index.php/app/auth?device_id="+ deviceId);
    }

    public void openPage(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }



    private class AuthTask extends AsyncTask<String, String, String> {

        private static final String TAG = "AsyncTask";
        final String AUTH_HASH_CODE = "MyInformation";

        protected String doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setDoOutput(true);
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.connect();
                BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String content = "", line;
                while ((line = rd.readLine()) != null) {
                    content += line + "\n";
                }
                return content;
            }catch (Exception e){
                Log.e(TAG, e.toString());
            }
            return null;
        }

        protected void onProgressUpdate(String... progress) {
        }

        protected void onPostExecute(String result) {
            // this is executed on the main thread after the process is over
            // update your UI here
            try {
                JSONObject jsonResult = new JSONObject(result);
                Log.d(TAG, "'" + jsonResult.get("action") + "' action request");
                Log.d(TAG, result);
                if (jsonResult.getString("action").equals("auth")) {
                    if (jsonResult.getBoolean("isAuth")) {
                        MyInformation.get(getBaseContext()).getUser(0).setAuthHash(jsonResult.getString("hash"));
                        MyInformation.get(getBaseContext()).getUser(0).setUserName(jsonResult.getString("username"));
                        MyInformation.get(getBaseContext()).getUser(0).setCode(jsonResult.getString("sharecode"));

                        SharedPreferences sPref = getSharedPreferences(AUTH_HASH_CODE, MODE_PRIVATE);
                        SharedPreferences.Editor ed = sPref.edit();
                        ed.putString("hash", jsonResult.getString("hash"));
                        ed.commit();

                        Log.d(TAG, "Login Successful!");
                        openPage();
                    } else {
                        Log.d(TAG, "login/password incorrect");
                    }
                }
            }catch (Exception e){
                Log.e(TAG, e.toString());
            }
        }
    }
}