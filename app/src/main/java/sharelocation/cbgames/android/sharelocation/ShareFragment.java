package sharelocation.cbgames.android.sharelocation;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
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
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ShareFragment extends Fragment{

    private static final String DIALOG_DATE = "DialogDate";
    private static final int REQUEST_DATE = 0;

    private TextView mShareCodeTextView;
    private TextView mTimeOverCodeTextView;
    private Button mGenerateNewCodeButton;
    private Button mTimeOverButton;
    private Button mCopyCodeButton;
    private Button mShareCodeButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_share, container, false);

        mShareCodeTextView = (TextView) v.findViewById(R.id.share_code);
        mShareCodeTextView.setText(MyInformation.get(getContext()).getUser("0").getCode());

        mTimeOverCodeTextView = (TextView) v.findViewById(R.id.timeoverTextView);
        setTimeOverTextChange();

        mGenerateNewCodeButton = (Button) v.findViewById(R.id.generate_new_code);
        mGenerateNewCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new GenerateNewCodeRequestTask().execute();
            }
        });

        mTimeOverButton = (Button) v.findViewById(R.id.select_timeover);
        mTimeOverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment
                        .newInstance(QueryPreferences.getTimeOverCode(getContext()));
                dialog.setTargetFragment(ShareFragment.this, REQUEST_DATE);
                dialog.show(manager, DIALOG_DATE);
            }
        });

        mCopyCodeButton = (Button) v.findViewById(R.id.copy_sharecode);
        mCopyCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("", QueryPreferences.getShareCode(getContext()));
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getContext(), R.string.code_copied, Toast.LENGTH_SHORT).show();
            }
        });

        mShareCodeButton = (Button) v.findViewById(R.id.share_sharecode);
        mShareCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String generateShareText = getResources().getString(R.string.share_sharecode_description)+" "+QueryPreferences.getShareCode(getContext());
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, generateShareText);
                i.putExtra(Intent.EXTRA_SUBJECT,
                        getString(R.string.share_code_title));
                i = Intent.createChooser(i, getString(R.string.share_code_title));
                startActivity(i);
            }
        });

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_DATE) {
            Date date = (Date) data
                    .getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            new UpdateTimeOverCodeRequestTask(date.getTime()/1000).execute();
        }
    }

    private void setTimeOverTextChange(){
        Resources resources = getResources();
        long unixSeconds = QueryPreferences.getTimeOverCode(getContext());

        Date date = new java.util.Date(unixSeconds*1000L);

        //SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        //sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT-4"));
        String formattedDate = sdf.format(date);

        mTimeOverCodeTextView.setText(String.format(resources.getString(R.string.datetimeover), formattedDate));
    }

    class GenerateNewCodeRequestTask extends AsyncTask<String, String, String> {

        private static final String TAG = "GenerateNewCodeRequestTask";

        public GenerateNewCodeRequestTask(){

        }

        protected String doInBackground(String... urls) {
            try {
                String hash = QueryPreferences.getAuthHash(getContext());
                URL url = new URL("http://sharelocation.games-cb.com/index.php/app/ShareCode?hash="+hash);
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
                    String shareCode = jsonResult.getString("share_code");

                    Log.d(TAG, "result: "+MyInformation.get(getContext()).getUser(QueryPreferences.getShareCode(getContext())).getId());
                    MyInformation.InformationUser user = MyInformation.get(getContext()).getUser(QueryPreferences.getShareCode(getContext()));
                    user.setCode(shareCode);
                    MyInformation.get(getContext()).updateUser(user, true);

                    mShareCodeTextView.setText(shareCode);
                    QueryPreferences.setShareCode(getContext(), shareCode);
                    QueryPreferences.setTimeOverCode(getContext(), jsonResult.getLong("datetime_over"));
                    setTimeOverTextChange();
                }
            }catch (Exception e){
                Log.e(TAG, e.toString());
            }
        }
    }

    class UpdateTimeOverCodeRequestTask extends AsyncTask<String, String, String> {

        private static final String TAG = "UpdateTimeOverCodeRequestTask";
        private long mUnixSeconds = 0;


        public UpdateTimeOverCodeRequestTask(long unix_seconds){
            mUnixSeconds = unix_seconds;
        }

        protected String doInBackground(String... urls) {
            try {
                String hash = QueryPreferences.getAuthHash(getContext());
                URL url = new URL("http://sharelocation.games-cb.com/index.php/app/UpdateTimeOver?hash="+hash+"&timeover="+mUnixSeconds);
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
                if (jsonResult.getString("action").equals("updatetimeover")) {
                    QueryPreferences.setTimeOverCode(getContext(), jsonResult.getLong("datetime_over"));
                    setTimeOverTextChange();
                    Toast.makeText(getContext(), R.string.timeover_changed, Toast.LENGTH_SHORT).show();
                }
            }catch (Exception e){
                Log.e(TAG, e.toString());
            }
        }
    }
}
