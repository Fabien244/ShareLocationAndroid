package sharelocation.cbgames.android.sharelocation;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

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
        }
    }
}
