package sharelocation.cbgames.android.sharelocation;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.LocationResult;

import java.util.List;

public class ShareReceiver extends BroadcastReceiver {
    private static final String TAG = "ShareReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Received broadcast intent: " + intent.getAction());
        switch (intent.getAction()){
            case "stop_location":
                NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.cancel(0);
                SendLocationService.setServiceAlarm(context, false);
                break;
            case "android.intent.action.BOOT_COMPLETED":
                SendLocationService.setServiceAlarm(context, true);
                break;
            case "locations":
                LocationResult result = LocationResult.extractResult(intent);
                if (result != null) {
                    List<Location> locations = result.getLocations();
                    Log.d(TAG, locations+"");
                }
                break;
        }
    }
}
