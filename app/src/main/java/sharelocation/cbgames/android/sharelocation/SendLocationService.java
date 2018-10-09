package sharelocation.cbgames.android.sharelocation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.app.Notification.EXTRA_NOTIFICATION_ID;
import static android.support.v4.app.NotificationCompat.DEFAULT_SOUND;
import static android.support.v4.app.NotificationCompat.DEFAULT_VIBRATE;
import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class SendLocationService extends Service {

    private static final String TAG = "SendLocationService";

    private GoogleApiClient mClient;
    private LocationRequest mLocationRequest;

    // 60 секунд
    private static final long POLL_INTERVAL_MS = TimeUnit.MINUTES.toMillis(1);

    public static Intent newIntent(Context context) {
        return new Intent(context, SendLocationService.class);
    }


    FusedLocationProviderClient mFusedLocationClient;
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @SuppressLint("MissingPermission")
    protected void startLocationUpdates() {

        if(mFusedLocationClient == null)
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        Log.d(TAG, "search location");

        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //mLocationRequest.setInterval(UPDATE_INTERVAL);
        //mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        // do work here
                        onLocationChanged(locationResult.getLastLocation());
                        //sendLocation(locationResult.getLastLocation());
                    }
                },
                Looper.myLooper());
    }

    public void onLocationChanged(Location location) {
        // New location has now been determined
        //String msg = "Updated Location: " +
        //        Double.toString(location.getLatitude()) + "," +
        //        Double.toString(location.getLongitude());
       // Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        // You can now create a LatLng Object for use with maps
        //LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        sendLocation(location);
    }

    @SuppressLint("MissingPermission")
    public void getLastLocation() {
        // Get last known recent location using new Google Play Services SDK (v11+)
        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this);

        locationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // GPS location can be null if GPS is switched off
                        if (location != null) {
                            onLocationChanged(location);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("MapDemoActivity", "Error trying to get last GPS location");
                        e.printStackTrace();
                    }
                });
    }

    private boolean checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            requestPermissions();
            return false;
        }
    }

    private void requestPermissions() {
        //ActivityCompat.requestPermissions(this,
        //        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
        //        0);
    }

    public void sendLocation(Location location){
        Log.d(TAG, "send location");
        //QueryPreferences.setLastLocation(getBaseContext(), location);
        MyInformation.InformationUser user = MyInformation.get(this).getUser("0");
        user.setLocation(new LatLng(location.getLatitude(), location.getLongitude()));
        MyInformation.get(getBaseContext()).updateUser(user);
        new LocationRequestTask(location).execute();
    }

    public static void setServiceAlarm(Context context, boolean isOn) {
        Intent i = SendLocationService.newIntent(context);
        PendingIntent pi = PendingIntent.getService(context, 0, i, 0);
        AlarmManager alarmManager = (AlarmManager)
                context.getSystemService(Context.ALARM_SERVICE);
        if (isOn) {
            QueryPreferences.setCheckLocation(context, true);
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime(), POLL_INTERVAL_MS, pi);


            RemoteViews notificationLayout = new RemoteViews("sharelocation.cbgames.android.sharelocation", R.layout.notification_small);
            RemoteViews notificationLayoutExpanded = new RemoteViews("sharelocation.cbgames.android.sharelocation", R.layout.notification_large);

            Resources resources = context.getResources();

            Intent snoozeIntent = new Intent(context, ShareReceiver.class);
            snoozeIntent.setAction("stop_location");
            snoozeIntent.putExtra(EXTRA_NOTIFICATION_ID, 0);
            PendingIntent snoozePendingIntent =
                    PendingIntent.getBroadcast(context, 0, snoozeIntent, 0);

            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel notificationChannel = new NotificationChannel("0", "My Notifications", NotificationManager.IMPORTANCE_DEFAULT);

                // Configure the notification channel.
                notificationChannel.setDescription("Channel description");
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.RED);
                notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
                notificationChannel.enableVibration(true);
                notificationManager.createNotificationChannel(notificationChannel);
            }

            Notification notification = new NotificationCompat.Builder(context, "0")
                    .setTicker(resources.getString(R.string.online))
                    .setSmallIcon(android.R.drawable.ic_dialog_map)
                    .setContentIntent(pIntent)
                    .setAutoCancel(false)
                    .setOngoing(true)
                    .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                    .setCustomContentView(notificationLayout)
                    .setCustomBigContentView(notificationLayoutExpanded)
                    .addAction(android.R.drawable.ic_dialog_map, context.getString(R.string.stop), snoozePendingIntent)
                    .setDefaults(0)
                    .build();
            //NotificationManagerCompat notificationManager =
                    //NotificationManagerCompat.from(context);
            notificationManager.notify(0, notification);
            //context.sendBroadcast(new Intent(ACTION_SHOW_NOTIFICATION));

        } else {
            NotificationManager mNotificationManager = (NotificationManager)  context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(0);
            QueryPreferences.setCheckLocation(context, false);
            alarmManager.cancel(pi);
            pi.cancel();
        }
    }

    public static boolean isServiceAlarmOn(Context context) {
        Intent i = SendLocationService.newIntent(context);
        PendingIntent pi = PendingIntent
                .getService(context, 0, i, PendingIntent.FLAG_NO_CREATE);
        return pi != null;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        //Toast.makeText(getApplicationContext(), "Service started", Toast.LENGTH_LONG).show();
        onHandleIntent(intent);
        return START_STICKY;
    }

    String actions = "locations";
    protected void onHandleIntent(Intent intent) {
        if (!isNetworkAvailableAndConnected()) {
            return;
        }
        //Log.i(TAG, "Received an intent: " + intent);
        //findLocation();
        startLocationUpdates();
    }

    private boolean isNetworkAvailableAndConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        boolean isNetworkAvailable = cm.getActiveNetworkInfo() != null;
        boolean isNetworkConnected = isNetworkAvailable &&
                cm.getActiveNetworkInfo().isConnected();
        return isNetworkConnected;
    }


    class LocationRequestTask extends AsyncTask<String, String, String> {

        private static final String TAG = "LocationRequestTask";
        private Location mLocation;

        public LocationRequestTask(Location location){
            mLocation = location;
        }

        protected String doInBackground(String... urls) {
            try {
                String hash = QueryPreferences.getAuthHash(getBaseContext());
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
            Log.d(TAG, "result: "+result);
            // this is executed on the main thread after the process is over
            // update your UI here
        }
    }
}
