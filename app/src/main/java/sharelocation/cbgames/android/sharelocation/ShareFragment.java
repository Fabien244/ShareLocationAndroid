package sharelocation.cbgames.android.sharelocation;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class ShareFragment extends Fragment{

    private TextView mShareCodeTextView;
    private Button mGenerateNewCodeButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_share, container, false);

        mShareCodeTextView = (TextView) v.findViewById(R.id.share_code);
        mShareCodeTextView.setText(MyInformation.get(getContext()).getUser(0).getCode());

        mGenerateNewCodeButton = (Button) v.findViewById(R.id.generate_new_code);
        mGenerateNewCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new GenerateNewCodeRequestTask().execute();
            }
        });

        return v;
    }

    class GenerateNewCodeRequestTask extends AsyncTask<String, String, String> {

        private static final String TAG = "LocationRequestTask";

        public GenerateNewCodeRequestTask(){

        }

        protected String doInBackground(String... urls) {
            try {
                String hash = MyInformation.get(getContext()).getUser(0).getAuthHash();
                URL url = new URL("http://sharelocation.games-cb.com/index.php/app/ShareCode?hash="+MyInformation.get(getContext()).getUser(0).getAuthHash());
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
                return content;
            }catch (Exception e){
                Log.e(TAG, e.toString());
            }
            return null;
        }

        protected void onProgressUpdate(String... progress) {
        }

        protected void onPostExecute(String result) {
            try {
                JSONObject jsonResult = new JSONObject(result);
                Log.d(TAG, "'" + jsonResult.get("action") + "' action request");
                Log.d(TAG, result);
                if (jsonResult.getString("action").equals("sharecode")) {
                    MyInformation.get(getContext()).getUser(0).setCode(jsonResult.getString("share_code"));
                    mShareCodeTextView.setText(jsonResult.getString("share_code"));
                }
            }catch (Exception e){
                Log.e(TAG, e.toString());
            }
        }
    }
}
