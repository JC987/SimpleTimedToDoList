package com.example.jc.timedtodolist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;



public class MyReceiver extends BroadcastReceiver {
  public MyReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {


        NotificationHelper notificationHelper = new NotificationHelper(context);
        NotificationCompat.Builder nb = notificationHelper.getNotification();
        notificationHelper.getManager().cancelAll();

        if(nb != null)
            notificationHelper.getManager().notify(1, nb.build());


    }
}