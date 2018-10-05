package sharelocation.cbgames.android.sharelocation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RemoteViews;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import static android.app.Notification.EXTRA_NOTIFICATION_ID;

public class MainActivity extends AppCompatActivity {

    public final static String FILE_NAME = "filename";
    private static final int REQUEST_ERROR = 0;
    private static final String TAG = "MainActivity";
    private static final String[] LOCATION_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };
    private static final int REQUEST_LOCATION_PERMISSIONS = 0;
    private GoogleApiClient mClient;
    private Location mLocation;
    private boolean isCheckedLocation = false;

    private DrawerLayout mDrawerLayout;

    boolean mStopHandler = false;
    Handler mHandler;

    private Switch checkedLocationSwitch;

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            // do your stuff - don't create a new runnable here!
            if (!mStopHandler) {
                mHandler.postDelayed(this, 5000);
                getAllCodes();
            }
        }
    };


    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        //actionbar.setHomeAsUpIndicator(R.drawable);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        View headerView = navigationView.getHeaderView(0);
        TextView navUsername = (TextView) headerView.findViewById(R.id.username_text);
        navUsername.setText(MyInformation.get(this).getUser(0).getUserName());

        checkedLocationSwitch = (Switch) navigationView.getMenu().getItem(0).getActionView().findViewById(R.id.checked_location);
        checkedLocationSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkedLocation(true);
            }
        });
        checkedLocation(false);

        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        int id = menuItem.getItemId();
                        android.support.v4.app.FragmentTransaction tran = getSupportFragmentManager().beginTransaction();
                        switch (id){
                            case R.id.checked_location_item:
                                checkedLocation(true);
                                break;
                            case R.id.nav_share_code:
                                tran.replace(R.id.fragment_container, new ShareFragment());
                                tran.commit();
                                break;
                            case R.id.nav_enter_code:
                                tran.replace(R.id.fragment_container, new EnterFragment());
                                tran.commit();
                                break;
                            case R.id.nav_settings:
                                break;
                        }
                        // set item as selected to persist highlight
                        //menuItem.setChecked(true);
                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();
                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here

                        return true;
                    }
                });


        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);
        if (fragment == null) {
            fragment = new MapFragment();
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }


        mHandler = new Handler();
        mHandler.post(runnable);
    }


    private void checkedLocation(boolean isChange){
        if (checkedLocationSwitch != null) {
            if (isChange)
                isCheckedLocation = !QueryPreferences.isCheckLocation(this);
            else
                isCheckedLocation = QueryPreferences.isCheckLocation(this);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(LOCATION_PERMISSIONS,
                            REQUEST_LOCATION_PERMISSIONS);
                }
            }else {
                if (!SendLocationService.isServiceAlarmOn(this) || !isCheckedLocation)
                    SendLocationService.setServiceAlarm(getBaseContext(), isCheckedLocation);
            }
            checkedLocationSwitch.setChecked(isCheckedLocation);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void sendLocation(Location location){
        MyInformation.get(this).getUser(0).setLocation(location);
        new LocationRequestTask(location).execute();
    }

    private void findLocation() {
        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        request.setInterval(5000);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(LOCATION_PERMISSIONS,
                        REQUEST_LOCATION_PERMISSIONS);
            }
            return;
        }


        LocationServices.FusedLocationApi
                .requestLocationUpdates(mClient, request, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        mLocation = location;
                        sendLocation(mLocation);
                        Log.d(TAG, "location sended");
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSIONS:
                //findLocation();
                checkedLocation(true);
            default:
                super.onRequestPermissionsResult(requestCode, permissions,
                        grantResults);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        checkedLocation(false);
    }

    @Override
    public void onStop() {
        super.onStop();
        //mClient.disconnect();
    }


    @Override
    public void onResume() {
        super.onResume();
        checkedLocation(false);
    }


    @Override
    public void onBackPressed() {

        int count = getSupportFragmentManager().getBackStackEntryCount();

        if (count == 0) {
            android.support.v4.app.FragmentTransaction tran = getSupportFragmentManager().beginTransaction();
            tran.replace(R.id.fragment_container, new MapFragment());
            tran.commit();
        } else {
            getFragmentManager().popBackStack();
        }
    }


    private double meterDistanceBetweenPoints(double lat_a, double lng_a, double lat_b, double lng_b) {
        double pk = (double) (180.f/Math.PI);

        double a1 = lat_a / pk;
        double a2 = lng_a / pk;
        double b1 = lat_b / pk;
        double b2 = lng_b / pk;

        double t1 = Math.cos(a1) * Math.cos(a2) * Math.cos(b1) * Math.cos(b2);
        double t2 = Math.cos(a1) * Math.sin(a2) * Math.cos(b1) * Math.sin(b2);
        double t3 = Math.sin(a1) * Math.sin(b1);
        double tt = Math.acos(t1 + t2 + t3);

        return 6366000 * tt;
    }

    public void getAllCodes(){
        String jsonData = "";

        ArrayList<MyInformation.InformationUser> informUsers = MyInformation.get(getBaseContext()).getUsers();
        for(int i=1; i<informUsers.size(); i++){
            if(i == 1)
                jsonData = "\""+informUsers.get(i).getCode()+"\"";
            else
                jsonData += ",\""+informUsers.get(i).getCode()+"\"";
        }
        if(informUsers.size() > 1)
            new GetAllCodesRequestTask(jsonData).execute();
    }

    class LocationRequestTask extends AsyncTask<String, String, String> {

        private static final String TAG = "LocationRequestTask";
        private Location mLocation;

        public LocationRequestTask(Location location){
            mLocation = location;
        }

        protected String doInBackground(String... urls) {
            try {
                String hash = MyInformation.get(getBaseContext()).getUser(0).getAuthHash();
                URL url = new URL("http://sharelocation.games-cb.com/index.php/app/sendLocation?hash="+hash+"&latitude="+mLocation.getLatitude()+"&longtitude="+mLocation.getLongitude());
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
            // this is executed on the main thread after the process is over
            // update your UI here
        }
    }
    class ShareCodeRequestTask extends AsyncTask<String, String, String> {

        private static final String TAG = "LocationRequestTask";
        private Location mLocation;

        public ShareCodeRequestTask(Location location){
            mLocation = location;
        }

        protected String doInBackground(String... urls) {
            try {
                String hash = MyInformation.get(getBaseContext()).getUser(0).getAuthHash();
                URL url = new URL("http://sharelocation.games-cb.com/index.php/app/sharecode?hash="+hash+"&newCode="+"0"+"&datetime="+"-1");
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
            // this is executed on the main thread after the process is over
            // update your UI here
        }
    }

    class GetAllCodesRequestTask extends AsyncTask<String, String, String> {

        private static final String TAG = "LocationRequestTask";
        private String mJsonData;

        public GetAllCodesRequestTask(String jsonData){
            mJsonData = jsonData;
        }

        protected String doInBackground(String... urls) {
            try {
                String hash = MyInformation.get(getBaseContext()).getUser(0).getAuthHash();
                URL url = new URL("http://sharelocation.games-cb.com/index.php/app/PermissionList?share_codes_json={\"share_codes\":["+mJsonData+"]}");
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
                if (jsonResult.getString("action").equals("permission_list")) {
                    for(int i=0; i<jsonResult.length()-1; i++){
                        String code = jsonResult.getJSONObject(i+"").getString("sharecode");
                        String username = jsonResult.getJSONObject(i+"").getString("username");
                        Double lat = jsonResult.getJSONObject(i+"").getDouble("latitude");
                        Double lon = jsonResult.getJSONObject(i+"").getDouble("longitude");
                        MyInformation.InformationUser.StatusCode statusCode = MyInformation.InformationUser.StatusCode.valueOf(jsonResult.getJSONObject(i+"").getString("statuscode"));

                        MyInformation.get(getBaseContext()).updateUser(code, statusCode, username, lat, lon);

                        if (statusCode == MyInformation.InformationUser.StatusCode.NOT_AVAILABLE || statusCode == MyInformation.InformationUser.StatusCode.OFFLINE) {
                            Log.d(TAG, "Code "+statusCode.name());
                        }
                    }
                }
            }catch (Exception e){
                Log.e(TAG, e.toString());
            }
        }
    }
}
