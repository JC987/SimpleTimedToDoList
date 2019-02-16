package com.example.jc.timedtodolist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;



public class MyReceiver extends BroadcastReceiver {
  public MyReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i("Receiver", "onReceive: ");
        //Intent intent1 = new Intent(context, MyService.class);
        //context.startService(intent1);

        NotificationHelper notificationHelper = new NotificationHelper(context);
        NotificationCompat.Builder nb = notificationHelper.getNotification();
        notificationHelper.getManager().cancelAll();

        notificationHelper.getManager().notify(1, nb.build());


    }
}