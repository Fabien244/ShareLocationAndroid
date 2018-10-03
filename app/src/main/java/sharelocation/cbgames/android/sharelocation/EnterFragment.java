package sharelocation.cbgames.android.sharelocation;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import static sharelocation.cbgames.android.sharelocation.MyInformation.*;

public class EnterFragment extends Fragment {

    private Button mEnterButton;
    private EditText mEnterText;


    private RecyclerView mRecyclerView;
    private CodesAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_enter, container, false);
        mEnterText = (EditText) v.findViewById(R.id.code_text);
        mEnterButton = (Button) v.findViewById(R.id.enter_btn);//
        mEnterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String codeText = mEnterText.getText().toString();
                new EnterCodeTask().execute("http://sharelocation.games-cb.com/index.php/app/Permission?share_code="+ codeText);
            }
        });


        mRecyclerView = (RecyclerView) v.findViewById(R.id.list_codes);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        reloadListCode();
        return v;
    }

    private void reloadListCode(){
        ArrayList<MyInformation.InformationUser> informationUsersOriginal = MyInformation.get(getContext()).getUsers();
        ArrayList<MyInformation.InformationUser> informationUsersCopy = new ArrayList<MyInformation.InformationUser>();

        for(int i=1; i<informationUsersOriginal.size(); i++){
            informationUsersCopy.add(informationUsersOriginal.get(i));
        }

        if (mAdapter == null) {
            mAdapter = new CodesAdapter(informationUsersCopy);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setInformUsers(informationUsersCopy);
            mAdapter.notifyDataSetChanged();
        }

    }


    private class EnterCodeTask extends AsyncTask<String, String, String> {

        private static final String TAG = "EnterCodeTask";

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
                if (jsonResult.getString("action").equals("permission")) {

                    InformationUser.StatusCode statusCode = InformationUser.StatusCode.valueOf(jsonResult.getString("statuscode"));

                    boolean available = false;
                    switch (statusCode){
                        case NONE:
                        case AVAILABLE:
                        case ONLINE:
                            available = true;
                            break;
                        case NOT_AVAILABLE:
                        case OFFLINE:
                            available = false;
                            break;
                    }

                    if (available) {
                        if(!jsonResult.getString("sharecode").toLowerCase().equals(MyInformation.get(getContext()).getUser(0).getCode().toLowerCase())) {
                            String username = jsonResult.getString("username");
                            String code = jsonResult.getString("sharecode");
                            Double lat = jsonResult.getDouble("latitude");
                            Double lon = jsonResult.getDouble("longitude");
                            MyInformation.get(getContext()).updateUser(code, statusCode, username, lat, lon);

                            android.support.v4.app.FragmentTransaction tran = getFragmentManager().beginTransaction();
                            tran.replace(R.id.fragment_container, new MapFragment());
                            tran.commit();
                        }else{
                            Log.d(TAG, "you can’t add yourself");
                            Toast.makeText(getContext(), R.string.dont_my_code, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d(TAG, "Share code incorrect");
                        Toast.makeText(getContext(), R.string.share_code_incorrect, Toast.LENGTH_SHORT).show();
                    }
                }
                reloadListCode();
            }catch (Exception e){
                Log.e(TAG, e.toString());
            }
        }
    }



    private class CodeHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private MyInformation.InformationUser mInformUser;

        private TextView mCodeTextView;
        private TextView mUserNameTextView;
        private TextView mStatusCodeTextView;
        private Button mDeleteCodeButton;

        public CodeHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_codes, parent, false));
            itemView.setOnClickListener(this);

            mCodeTextView = (TextView) itemView.findViewById(R.id.share_code);
            mUserNameTextView = (TextView) itemView.findViewById(R.id.username);
            mStatusCodeTextView = (TextView) itemView.findViewById(R.id.statuscode);
            mDeleteCodeButton = (Button) itemView.findViewById(R.id.delete_code);

            mDeleteCodeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MyInformation.get(getContext()).removeUser(mInformUser.getCode());
                    reloadListCode();
                }
            });
        }

        public void bind(MyInformation.InformationUser informUser) {
            mInformUser = informUser;
            mUserNameTextView.setText(mInformUser.getUserName());
            mCodeTextView.setText(getResources().getString(R.string.code)+": "+mInformUser.getCode().toUpperCase());
            switch (mInformUser.getStatusCode()){
                case NONE:
                    mStatusCodeTextView.setText("");
                    break;
                case AVAILABLE:
                    mStatusCodeTextView.setText(getResources().getString(R.string.available));
                    mStatusCodeTextView.setTextColor(getResources().getColor(R.color.colorGreen));
                    break;
                case NOT_AVAILABLE:
                    mStatusCodeTextView.setText(getResources().getString(R.string.not_available));
                    mStatusCodeTextView.setTextColor(getResources().getColor(R.color.colorRed));
                    break;
                case OFFLINE:
                    mStatusCodeTextView.setText(getResources().getString(R.string.offline));
                    mStatusCodeTextView.setTextColor(getResources().getColor(R.color.colorRed));
                    break;
                case ONLINE:
                    mStatusCodeTextView.setText(getResources().getString(R.string.online));
                    mStatusCodeTextView.setTextColor(getResources().getColor(R.color.colorGreen));
                    break;
            }
        }

        @Override
        public void onClick(View view) {

        }
    }

    private class CodesAdapter extends RecyclerView.Adapter<CodeHolder> {

        private ArrayList<MyInformation.InformationUser> mInformUsers;

        public CodesAdapter(ArrayList<MyInformation.InformationUser> informUsers) {
            mInformUsers = informUsers;
        }

        @Override
        public CodeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new CodeHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(CodeHolder holder, int position) {
            MyInformation.InformationUser infromUser = mInformUsers.get(position);
            holder.bind(infromUser);
        }

        @Override
        public int getItemCount() {
            return mInformUsers.size();
        }


        public void setInformUsers(ArrayList<MyInformation.InformationUser> informUsers) {
            mInformUsers = informUsers;
        }
    }
}
