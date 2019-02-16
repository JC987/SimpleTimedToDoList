package com.example.jc.timedtodolist;


import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;


public class NotificationHelper extends ContextWrapper {
    private static final String TAG = "NotificationHelper";

    public static final String channelID = "com.example.jc.simpletimedtodolist.channel_two_id";
    public static final String channelName = "com.example.jc.simpletimedtodolist.channel_two_name";

    private NotificationManager mManager;

    public NotificationHelper(Context base) {
        super(base);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationChannel channel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH);

        getManager().createNotificationChannel(channel);
    }

    public NotificationManager getManager() {
        if (mManager == null) {
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }

        return mManager;
    }

    public NotificationCompat.Builder getNotification() {

        SharedPreferences settingsPref = getSharedPreferences("Settings",MODE_PRIVATE);

        //If the user wants a times finished notification
        if(settingsPref.getBoolean("finished",true)) {

            Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelID)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentTitle("Time's Up!")
                    .setContentText("Don't forget to check your list!")
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setOngoing(false)
                    .setPriority(NotificationCompat.PRIORITY_HIGH);

            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            builder.setContentIntent(contentIntent);

            //if user wants notification to make a sound
            if (settingsPref.getBoolean("sound", true))
                builder.setSound(sound);

            return builder;

        }
        return null;
    }

}