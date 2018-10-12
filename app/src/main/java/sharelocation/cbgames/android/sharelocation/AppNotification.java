package sharelocation.cbgames.android.sharelocation;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import static android.app.Notification.EXTRA_NOTIFICATION_ID;
import static android.content.Context.NOTIFICATION_SERVICE;

public class AppNotification{
    public static void ShowPermanent(Context context, boolean isOn){
        if(isOn && QueryPreferences.getNotification(context) && QueryPreferences.isCheckLocation(context)) {
            RemoteViews notificationLayout = new RemoteViews("sharelocation.cbgames.android.sharelocation", R.layout.notification_small);
            RemoteViews notificationLayoutExpanded = new RemoteViews("sharelocation.cbgames.android.sharelocation", R.layout.notification_large);

            Resources resources = context.getResources();

            Intent snoozeIntent = new Intent(context, ShareReceiver.class);
            snoozeIntent.setAction("stop_location");
            snoozeIntent.putExtra(EXTRA_NOTIFICATION_ID, 0);
            PendingIntent snoozePendingIntent =
                    PendingIntent.getBroadcast(context, 0, snoozeIntent, 0);

            Intent snoozeIntentHide = new Intent(context, ShareReceiver.class);
            snoozeIntentHide.setAction("hide_notification");
            snoozeIntentHide.putExtra(EXTRA_NOTIFICATION_ID, 0);
            PendingIntent snoozePendingIntentHide =
                    PendingIntent.getBroadcast(context, 0, snoozeIntentHide, 0);

            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel notificationChannel = new NotificationChannel("0", "My Notifications", NotificationManager.IMPORTANCE_DEFAULT);

                // Configure the notification channel.
                notificationChannel.setDescription("Channel");
                notificationChannel.enableLights(false);
                notificationChannel.setLightColor(Color.GREEN);
                notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
                notificationChannel.enableVibration(false);
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
                    .addAction(android.R.drawable.ic_dialog_map, context.getString(R.string.hide), snoozePendingIntentHide)
                    .addAction(android.R.drawable.ic_dialog_map, context.getString(R.string.stop), snoozePendingIntent)
                    .setDefaults(0)
                    .build();
            notificationManager.notify(0, notification);
        }else {
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(0);
        }
    }
}
