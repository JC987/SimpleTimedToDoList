package com.example.jc.timedtodolist;

/**
 * Created by JC on 11/2/2018.
 */
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class SensorRestarterBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(SensorRestarterBroadcastReceiver.class.getSimpleName(), "Service Stops! Oooooooooooooppppssssss!!!!");
        Intent intent1 = new Intent(context, BackgroundService.class);

        SharedPreferences prefs= context.getSharedPreferences("Service", context.MODE_PRIVATE);
     //   SharedPreferences.Editor editor = prefs.edit();
        boolean tmp =prefs.getBoolean("done",false);
        int c = prefs.getInt("called",0);
       // editor.putInt("called",++c);
       // editor.apply();
        Log.d("aa", "onReceive: counter is "+prefs.getLong("counter",0) +" done is "+ tmp);
        //Log.d("aa", "onReceive: " + prefs.getInt("called",0));
        if(!tmp)
            context.startService(intent1);


    }
    /*private boolean isMyServiceRunning(Class<?> serviceClass,Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
    }*/
}
