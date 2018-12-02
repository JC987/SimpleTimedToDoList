package com.example.jc.timedtodolist;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RemoteViews;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    Button addTask, confirm, reset;
    Boolean activeList = false;
    TextView textViewTime;
    ScrollView scrollView;
    LinearLayout linearLayout;
    TableLayout tableLayout;
    CountDownTimer countDownTimer = null;
    Chronometer chronometer;
    ArrayList<TextView> textViewArrayList = new ArrayList<>();
    ArrayList<EditText> editTextArrayList = new ArrayList<>();
    ArrayList<CheckBox> checkBoxArrayList = new ArrayList<>();
    ArrayList<TableRow> tableRowArrayList = new ArrayList<>();
    int totalTask = 1, idCounter = 0;

    final static  String TAG = "mainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        chronometer=findViewById(R.id.chronometer);
        chronometer.setBase(SystemClock.elapsedRealtime());
        addTask = findViewById(R.id.btnTaskAdd);
        confirm = findViewById(R.id.btnTaskConfirm);
        textViewTime = findViewById(R.id.textTime);
        scrollView = findViewById(R.id.scrollView);
        linearLayout = findViewById(R.id.linearLayout);
        tableLayout = findViewById(R.id.tableLayout);
        reset = findViewById(R.id.btnTaskReset);

        addTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: addTask Pressed");
                createNewTask("",null,false);
            }
        });

        //chronometer.setCountDown(true);
        textViewTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: textViewTime Pressed");
                setTextViewTime();
            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopService(new Intent(MainActivity.this, BackgroundService.class));
                SharedPreferences prefs= getSharedPreferences("Service", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear();editor.apply();
                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                manager.cancelAll();
                Log.d(TAG, "onClick: reset Pressed");
                if(reset.getText().toString().equals("Finished?")){


                    Toast.makeText(MainActivity.this,"To do list Finished!\n Starting new list!",Toast.LENGTH_LONG).show();
                    reset.setText("Reset");
                }

                textViewTime.setText("00:00:00");
                textViewTime.setClickable(true);
                confirm.setEnabled(true);
                addTask.setEnabled(true);
                if(countDownTimer!=null)
                    countDownTimer.cancel();
                tableLayout.removeAllViews();
                totalTask = 1; idCounter = 0;

                editTextArrayList.clear();
                tableRowArrayList.clear();
                checkBoxArrayList.clear();
                textViewArrayList.clear();
                //saveState();
                Toast.makeText(MainActivity.this,"Resetting",Toast.LENGTH_SHORT ).show();
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Log.d(TAG, "onClick: confirm Pressed");
                confirmTaskList();

                if(editTextArrayList.size()>0 && !textViewTime.getText().toString().equals("00:00:00")) {

                    textViewTime.setClickable(false);

                    reset.setText("Finished?");
                    addTask.setEnabled(false);
                    confirm.setEnabled(false);
                    activeList = true;

                    long tmp = convertTimeToMilli(textViewTime.getText().toString());
                    countDown(tmp);
                    buildTimerNotification(convertTimeToMilli(textViewTime.getText().toString()));

                    if (!isMyServiceRunning(BackgroundService.class)) {
                        Intent service = new Intent(MainActivity.this, BackgroundService.class);
                        service.putExtra("value", textViewTime.getText().toString());
                        service.putExtra("val",tmp);
                        SharedPreferences prefs = getSharedPreferences("Service", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putLong("counter",tmp);
                        editor.apply();
                        Log.d("aa", "onClick: from MAIN create service");
                        startService(service);

                   }

                }
                else {
                    Toast.makeText(MainActivity.this, "Add task to List and set time", Toast.LENGTH_SHORT).show();
                    totalTask = 1;
                }


            }
        });



    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuSetting:
                Intent i = new Intent(this,SettingsActivity.class);
                this.startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.menu_main,menu);
         return true;//return super.onCreateOptionsMenu(menu);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
    }

    public void countDown(long l){

        countDownTimer = new CountDownTimer(l, 1000) {

            public void onTick(long millisUntilFinished) {
                long hr = millisUntilFinished / (60 * 60 * 1000);
                long min = (millisUntilFinished - (hr * 60 * 60 * 1000)) / 60000;
                long sec = (millisUntilFinished - (hr * 60 * 60 * 1000) - (min * 60000)) / 1000;
                NumberFormat numberFormat = new DecimalFormat("00");

                textViewTime.setText(numberFormat.format(hr) + ":" + numberFormat.format(min) + ":" + numberFormat.format(sec));
               // buildTimerNotification(millisUntilFinished);
            }
            public void onFinish() {
                textViewTime.setText("Times Up!");
                countDownTimer=null;
               // buildTimerNotification(0);
            }
        }.start();
    }

    public long convertTimeToMilli(String time){
        long l=0;
        String[] arr;
        if(time.contains(" "))
            arr = time.split(" : ");
        else
            arr = time.split(":");
        l += Integer.parseInt(arr[0]) * 60 * 60 * 1000;
        l += Integer.parseInt(arr[1]) * 60 * 1000;
        l += Integer.parseInt(arr[2]) * 1000;
        return l;
    }

    public void confirmTaskList(){
        for(int i = 0; i<editTextArrayList.size(); i++){
            editTextArrayList.get(i).setKeyListener(null);
            editTextArrayList.get(i).setFocusable(false);
            checkBoxArrayList.get(i).setEnabled(true);

            textViewArrayList.get(i).setText(i+1 + " )");

            if(editTextArrayList.get(i).getText().toString().equals("")) {
                tableRowArrayList.get(i).setVisibility(View.GONE);
                tableRowArrayList.remove(i);
                editTextArrayList.remove(i);
                textViewArrayList.remove(i);
                checkBoxArrayList.remove(i);

                Log.d(TAG, "confirmTaskList: removed table row " + i);
                i--;

            }


        }
    }

    public void setTextViewTime(){

        final AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_set_time, null);
        final NumberPicker hour = dialogView.findViewById(R.id.numPickerHour);
        final NumberPicker min = dialogView.findViewById(R.id.numPickerMin);
        final NumberPicker sec = dialogView.findViewById(R.id.numPickerSec);
        hour.setMaxValue(23);
        hour.setMinValue(0);
        min.setMaxValue(59);
        hour.setMinValue(0);
        sec.setMaxValue(59);
        dialog.setView(dialogView);
        dialog.setTitle("Set Time");
        Log.d(TAG, "setTextViewTime: inflate dialog");
        dialog.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                DecimalFormat decimalFormat = new DecimalFormat("00");
                String time = decimalFormat.format(hour.getValue())+":"+
                        decimalFormat.format(min.getValue())+":"+decimalFormat.format(sec.getValue());
                textViewTime.setText(time);
                Log.d(TAG, "onClick Positive Dialog: SET textViewTime " + time);
            }
        });
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d(TAG, "onClick Negative Dialog:");
            }
        });
        dialog.show();
    }

    public void createNewTask(String text, Boolean check, Boolean started){


        TableRow tableRow = new TableRow(MainActivity.this);
        TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT,TableLayout.LayoutParams.WRAP_CONTENT,5.0f);

        EditText editText = new EditText(MainActivity.this);
        TableRow.LayoutParams editTextParams = new TableRow.LayoutParams(0,TableLayout.LayoutParams.WRAP_CONTENT,4.0f);

        CheckBox checkBox = new CheckBox(MainActivity.this);
        TableRow.LayoutParams checkBoxParams= new TableRow.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT,TableLayout.LayoutParams.WRAP_CONTENT,0.5f);

        TextView textView1 = new TextView(this);
        TableRow.LayoutParams textViewParams= new TableRow.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT,TableLayout.LayoutParams.WRAP_CONTENT,0.5f);

        Log.d(TAG, "createNewTask: created table view");

        checkBoxParams.setMarginStart(30);

        editText.setLayoutParams(editTextParams);
        checkBox.setLayoutParams(checkBoxParams);
        textView1.setLayoutParams(textViewParams);
        tableRow.setLayoutParams(tableRowParams);

        Log.d(TAG, "createNewTask: setLayoutParams for views");

        String tmp = totalTask + ") ";
        textView1.setText(tmp);
        textView1.setTextSize(18);
        textView1.setTextColor(Color.parseColor("#000000"));
        textView1.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        editText.setHint("Leave blank if not needed!");
        editText.requestFocus();
        editText.setTextSize(18);
        editText.setText(text);

        tableRow.setPadding(4,8,4,32);

        if(check!=null && started) {
            checkBox.setEnabled(true);
            checkBox.setChecked(check);

                editText.setKeyListener(null);
        }
        else
            checkBox.setEnabled(false);
        Log.d(TAG, "createNewTask: set properties for views");

        totalTask++;

        textView1.setId(idCounter);
        editText.setId(idCounter+1);
        checkBox.setId(idCounter+2);

        idCounter += 3;

        Log.d(TAG, "createNewTask: incremented task no. and id ct");

        textViewArrayList.add(textView1);
        checkBoxArrayList.add(checkBox);
        editTextArrayList.add(editText);
        tableRowArrayList.add(tableRow);

        tableRow.addView(textView1);
        tableRow.addView(editText);
        tableRow.addView(checkBox);
        tableLayout.addView(tableRow);

        Log.d(TAG, "\n createNewTask: TABLE ROW ADDED \n");
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadState();
    }

    public void saveState(){
        SharedPreferences sharedPreferences = getSharedPreferences("pref",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt("size", editTextArrayList.size());
        if(textViewTime.getText().toString().equals("Times Up!"))
            editor.putString("time","00:00:00");
        else
            editor.putString("time", textViewTime.getText().toString());
        editor.putBoolean("started",countDownTimer!=null);
        for(int i = 0; i < editTextArrayList.size(); i++){
            editor.putString("editText "+ i, editTextArrayList.get(i).getText().toString());
            editor.putBoolean("checkBox " + i, checkBoxArrayList.get(i).isChecked());
        }
        editor.putLong("systemTime", SystemClock.elapsedRealtime());
        editor.apply();

    }
    public void loadState() {
        SharedPreferences sharedPreferences = getSharedPreferences("pref", MODE_PRIVATE);
        //SharedPreferences.Editor editor = sharedPreferences.edit();

        if (tableRowArrayList.size() <= 0) {
            int size = sharedPreferences.getInt("size", 0);
            long diff = SystemClock.elapsedRealtime() - sharedPreferences.getLong("systemTime",0);
            for (int i = 0; i < size; i++) {
                Log.d(TAG, "loadState: loaded new task");
                createNewTask(sharedPreferences.getString("editText " + i, ""), sharedPreferences.getBoolean("checkBox " + i, false),sharedPreferences.getBoolean("started", false));
            }
          //  confirmTaskList();


            textViewTime.setText(sharedPreferences.getString("time", "00:00:00"));
            if (sharedPreferences.getBoolean("started", false)) {
                Log.d(TAG, "loadState: started = " +sharedPreferences.getBoolean("started", false));

                addTask.setEnabled(false);
                confirm.setEnabled(false);
                textViewTime.setClickable(false);
                countDown( (convertTimeToMilli(sharedPreferences.getString("time", "00:00:00")) - diff) );

            }
            else{
                Log.d(TAG, "loadState: started = " +sharedPreferences.getBoolean("started", false));
                addTask.setEnabled(true);
                confirm.setEnabled(true);
                textViewTime.setClickable(true);
                textViewTime.setText(sharedPreferences.getString("time","00:00:00"));
                
            }


        }
    }

    public void buildTimerNotification(long l){
        final RemoteViews remoteViews = new RemoteViews(getPackageName(),R.layout.dialog_build_time_noti);
        remoteViews.setChronometer(R.id.remoteChrono,l+SystemClock.elapsedRealtime(),null,true);
        remoteViews.setTextViewText(R.id.remoteText,"Time Remaining:");
        if(l==0) {
            remoteViews.setChronometer(R.id.remoteChrono,l+SystemClock.elapsedRealtime(),chronometer.getFormat(),false);
            remoteViews.setTextViewText(R.id.remoteText,"done");
        }


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"channelId")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContent(remoteViews)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_MAX);

        Intent notificationIntent = new Intent(this,MainActivity.class);
        PendingIntent contentIntent =  PendingIntent.getActivity(this,0,notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(contentIntent);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent switchIntent = new Intent(this, switchButtonListener.class);
        PendingIntent pendingSwitchIntent = PendingIntent.getBroadcast(this, 0,
                switchIntent, 0);

        remoteViews.setOnClickPendingIntent(R.id.remoteButton,
                pendingSwitchIntent);

            manager.notify(0, builder.build());
           Log.d(TAG, "build: build noti");
        //}
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(MainActivity.this, BackgroundService.class));
        Log.i("aa", "onDestroy! form MAIN!");
        super.onDestroy();

    }

    public static class switchButtonListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "I am here");

            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancelAll();
        }
    }
}
