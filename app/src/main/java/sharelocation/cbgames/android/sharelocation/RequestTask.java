package sharelocation.cbgames.android.sharelocation;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

public class RequestTask extends AsyncTask<String, String, String> {

    private static final String TAG = "RequestTask";

    private WeakReference<Context> contextRef;

    public RequestTask (Context context){
        contextRef = new WeakReference<>(context);
    }

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
            JSONObject jsonResult = new JSONObject(content);
            Log.d(TAG, "'"+jsonResult.get("action")+"' action request");

            switch (jsonResult.getString("action")){
                case "auth":
                    if(jsonResult.getBoolean("isAuth")) {
                        MyInformation.get(contextRef.get()).getUser(0).setAuthHash(jsonResult.getString("hash"));
                        MyInformation.get(contextRef.get()).getUser(0).setUserName(jsonResult.getString("username"));
                    }else{
                        Log.d(TAG, "login/password incorrect");
                    }
                    break;
                case "sendlocation":
                    break;
                case "permissionlist":
                    break;
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
        //displayMessage(result);
    }
}
