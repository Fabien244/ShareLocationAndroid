package sharelocation.cbgames.android.sharelocation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

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

public class SendLocationService extends Service {



    public static final String ACTION_SHOW_NOTIFICATION =
            "sharelocation.cbgames.android.sharelocation.SHOW_NOTIFICATION";
    private static final String TAG = "SendLocationService";
    final String AUTH_HASH_CODE = "MyInformation";

    private GoogleApiClient mClient;
    private Location mLocation;

    // 60 секунд
    private static final long POLL_INTERVAL_MS = TimeUnit.MINUTES.toMillis(1);

    public static Intent newIntent(Context context) {
        return new Intent(context, SendLocationService.class);
    }



    @Override
    public void onCreate() {
        super.onCreate();
    }

    @SuppressLint("MissingPermission")
    private void reconnectGoogleApiClient(){
        mClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        Log.d(TAG, "GoogleApi connected");
                        findLocation();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                    }
                })
                .build();
        mClient.connect();

        mLocation = getLastKnownLocation();
        MyInformation.get(this).getUser(0).setLocation(mLocation);
    }

    private Location getLastKnownLocation() {
        LocationManager mLocationManager = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            @SuppressLint("MissingPermission") Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    @SuppressLint("MissingPermission")
    private void findLocation() {
        if(mClient != null && mClient.isConnected() && !mClient.isConnecting()) {
            LocationRequest request = LocationRequest.create();
            request.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

            LocationServices.FusedLocationApi
                    .requestLocationUpdates(mClient, request, new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            mLocation = location;
                            sendLocation(mLocation);
                            Log.d(TAG, "location sended");
                        }
                    });
        }else{
            reconnectGoogleApiClient();
        }
    }

    public void sendLocation(Location location){
        MyInformation.get(this).getUser(0).setLocation(location);
        new LocationRequestTask(location).execute();
    }

    public static void setServiceAlarm(Context context, boolean isOn) {
        Intent i = SendLocationService.newIntent(context);
        PendingIntent pi = PendingIntent.getService(context, 0, i, 0);
        AlarmManager alarmManager = (AlarmManager)
                context.getSystemService(Context.ALARM_SERVICE);
        if (isOn) {
            QueryPreferences.setCheckLocation(context, true);
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME,
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
                    .build();
            NotificationManagerCompat notificationManager =
                    NotificationManagerCompat.from(context);
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


    protected void onHandleIntent(Intent intent) {
        if (!isNetworkAvailableAndConnected()) {
            return;
        }
        Log.i(TAG, "Received an intent: " + intent);
        findLocation();
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
                SharedPreferences sPref = getSharedPreferences(AUTH_HASH_CODE, MODE_PRIVATE);
                String hash = sPref.getString("hash","");
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
