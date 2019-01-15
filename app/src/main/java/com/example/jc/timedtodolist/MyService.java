package com.example.jc.timedtodolist;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

public class MyService extends IntentService {
    private static final int NOTIFICATION_ID = 3;
    private static final String TAG = "service";

    public MyService() {
        super("MyService");

        Log.i(TAG, "MyService: constructer");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "onHandleIntent: ");
        SharedPreferences settingsPref = getSharedPreferences("Settings",MODE_PRIVATE);

        if(settingsPref.getBoolean("finished",true)) {
            Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "channelId")
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentTitle("Time's Up!")
                    .setContentText("Don't forget to check your list!")
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setOngoing(false)
                    .setPriority(NotificationCompat.PRIORITY_MAX);


            if (settingsPref.getBoolean("sound", true)) builder.setSound(sound);

            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            builder.setContentIntent(contentIntent);

            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.cancelAll();
                manager.notify(NOTIFICATION_ID, builder.build());
            }

        }
        else{
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null)    manager.cancelAll();

        }

        //stopSelf();

            /*Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle("Times up!");
        builder.setContentText("Don't forget to check your list!");

        builder.setSmallIcon(R.drawable.ic_launcher_foreground);
        Intent notifyIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 2, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        //to be able to launch your activity from the notification
        builder.setContentIntent(pendingIntent);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancelAll();

        Notification notificationCompat = builder.build();
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(NOTIFICATION_ID, notificationCompat);*/



    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Toast.makeText(MyService.this,"SERVICE DESTROYED", Toast.LENGTH_SHORT).show();
    }
}
