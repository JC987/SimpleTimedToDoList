package com.example.jc.timedtodolist;
/**
 * Created by JC on 11/1/2018.
 */

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.*;
import android.os.*;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class BackgroundService extends Service {
    public long counter=0;
    public boolean done = false;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private static final String TAG = "BackgroundService";
    @Override
    public void onCreate() {
        prefs = getSharedPreferences("Service", MODE_PRIVATE);
        editor = prefs.edit();
        Log.d(TAG, "onCreate: counter = "+counter);
        Toast.makeText(this, "Service created!", Toast.LENGTH_LONG).show();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

       SharedPreferences prefs = getSharedPreferences("Service", MODE_PRIVATE);
        Log.d(TAG, "onStartCommand: called " + prefs.getInt("called",0));


        long found = prefs.getLong("counter",0);
        counter = found;
        Log.d(TAG, "onStartCommand: found = "+found);
        startTimer();
        return START_STICKY;
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy:");

        Toast.makeText(this, "Service stopped", Toast.LENGTH_LONG).show();
        Intent broadcastIntent = new Intent(this, SensorRestarterBroadcastReceiver.class);
        editor.putBoolean("done",done);
        editor.apply();


        sendBroadcast(broadcastIntent);
        stoptimertask();
        Log.d(TAG, "onDestroy: hopefully done");
    }

    private Timer timer;
    private TimerTask timerTask;
    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, to wake up every 1 second
        timer.schedule(timerTask, 0, 1000); //
    }

    /**
     * it sets the timer to print the counter every x seconds
     */
    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                if(counter>0) {
                    Log.i("in timer", "in timer ++++  " + (counter -= 1000));
                    editor.putLong("counter",counter);
                    editor.apply();
                }
                    else{
                    buildNoti();
                    stoptimertask();
                    done = true;
                }
            }
        };
    }

    public void buildNoti(){
        Log.d(TAG, "buildNoti: building fininshed noti");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"channelId")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("Time's Up!")
                .setContentText("It's time to check the list.")
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(false)
                .setPriority(NotificationCompat.PRIORITY_MAX);

        Intent notificationIntent = new Intent(this,MainActivity.class);
        PendingIntent contentIntent =  PendingIntent.getActivity(this,0,notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(contentIntent);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0,builder.build());

        stopSelf();
    }

    /**
     * not needed
     */
    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;

        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}