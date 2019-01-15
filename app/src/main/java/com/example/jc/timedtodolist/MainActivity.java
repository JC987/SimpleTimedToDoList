package com.example.jc.timedtodolist;

import android.app.ActivityManager;
import android.app.AlarmManager;
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
import android.os.Build;
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
    private int mTotalTask = 1, mIdCounter = 0;


    final static  String TAG = "mainActivity";
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //first use settings
       // loadSettings();

        //instantiate vars
        chronometer = findViewById(R.id.chronometer);
        chronometer.setBase(SystemClock.elapsedRealtime());
        addTask = findViewById(R.id.btnTaskAdd);
        confirm = findViewById(R.id.btnTaskConfirm);
        textViewTime = findViewById(R.id.textTime);
        scrollView = findViewById(R.id.scrollView);
        linearLayout = findViewById(R.id.linearLayout);
        tableLayout = findViewById(R.id.tableLayout);
        reset = findViewById(R.id.btnTaskReset);

        //Calls createNewTask(...)

        addTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: addTask Pressed");
                createNewTask("",null,false);
            }
        });


        //Calls setTextViewTime()

        textViewTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: textViewTime Pressed");
                setTextViewTime();
            }
        });

        /*
         * Stops services, Clears notifications, and calls mResetList.
         */
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activeList=false;
                //Stop service
                stopService(new Intent(MainActivity.this,MyService.class));

                // TODO: BackgroundSerivce is not functioning properly the following
                // TODO: lines will be removed
               // stopService(new Intent(MainActivity.this, BackgroundService.class));
                SharedPreferences prefs= getSharedPreferences("Service", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear();editor.apply();

                //clear all notifications
                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if(manager!=null)
                    manager.cancelAll();

                //call reset func
                mResetList();
                Toast.makeText(MainActivity.this,"Resetting",Toast.LENGTH_SHORT ).show();
            }
        });

        //lock the list, start the timer, create notification
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences settingsPref = getSharedPreferences("Settings",MODE_PRIVATE);
                confirmTaskList();

                //Check to see if there is a least 1 task and
                // at least 1 second to complete it.
                if(editTextArrayList.size()>0 && !textViewTime.getText().toString().equals("00:00:00")) {

                    //Makes sure the list can no longer be edited
                    textViewTime.setClickable(false);
                    //TODO: create string resource
                    reset.setText(R.string.Finished);
                    if(!settingsPref.getBoolean("afterConfirm",false)) {
                        addTask.setEnabled(false);


                        confirm.setEnabled(false);
                    }
                    activeList = true;

                    long tmp = convertTimeToMilli(textViewTime.getText().toString());
                    countDown(tmp);


                    boolean tmpBool = settingsPref.getBoolean("finished",true);
                    boolean tmpBool2 = settingsPref.getBoolean("remaining",true);
                    if(tmpBool || tmpBool2) {
                        mCreateService(tmp);
                    }
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && settingsPref.getBoolean("remaining",true)) {
                        buildTimerNotification(tmp);
                    }

                }
                else if(!settingsPref.getBoolean("noTimer",false)){
                    Toast.makeText(MainActivity.this, "Add task to List and set time", Toast.LENGTH_SHORT).show();
                    mTotalTask = 1;
                }
                else{//List can be started without a timer

                    //Makes sure the list can no longer be edited
                    textViewTime.setClickable(false);
                    //TODO: create string resource
                    reset.setText(R.string.Finished);
                    activeList = true;
                    if(!settingsPref.getBoolean("afterConfirm",false)) {
                        addTask.setEnabled(false);
                        confirm.setEnabled(false);
                    }



                }


            }
        });


    }

    /*private void loadSettings(){
        SharedPreferences settingsPreferences = getSharedPreferences("Settings", MODE_PRIVATE);
        SharedPreferences.Editor settingsEditor = settingsPreferences.edit();

        boolean first = settingsPreferences.getBoolean("first",true);
        if(first){
            settingsEditor.putBoolean("first",false);
            settingsEditor.putBoolean("remainingNotification",true);
            settingsEditor.putBoolean("finishedNotification",true);
            settingsEditor.putBoolean("soundNotification",true);
            settingsEditor.putBoolean("dailyNotification",true);
            settingsEditor.putBoolean("adjustAfterConfirm",false);
            settingsEditor.putBoolean("startWithoutTimer",false);
        }
        settingsEditor.apply();
    }*/


    /**
     * Creates an alarm manager that calls a broadcast receiver MyReceiver.
     * Which calls a service to create a finish notification.
     * @param tmp length of time in milli till finish notification should be sent
     */
    private void mCreateService(long tmp){

        Intent notifyIntent = new Intent(MainActivity.this,
                MyReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast
                (MainActivity.this, PendingIntent.FLAG_ONE_SHOT,
                        notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if(alarmManager!=null) {
            //setExact requires api 19 or up.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Log.i(TAG, "onClick: api version greater than or equal to 19");
                alarmManager.setExact(AlarmManager.ELAPSED_REALTIME,
                        SystemClock.elapsedRealtime() + tmp, pendingIntent);
            } else {
                Log.i(TAG, "onClick: api version below 19");
                alarmManager.set(AlarmManager.ELAPSED_REALTIME,
                        SystemClock.elapsedRealtime() + tmp, pendingIntent);
            }
        }
    }

    /**
     * Reset all views and vars in order to create a new list
     */
    private void mResetList(){
        Log.d(TAG, "onClick: reset Pressed");
        if(reset.getText().toString().equals("Finished?")){

            //Toast.makeText(MainActivity.this,"To do list Finished!\n Starting new list!",Toast.LENGTH_LONG).show();
            reset.setText(R.string.btn_txt_reset);
        }

        //Resetting all views
        textViewTime.setText(R.string.default_txt_time);
        textViewTime.setClickable(true);
        confirm.setEnabled(true);
        addTask.setEnabled(true);

        if(countDownTimer!=null)
            countDownTimer.cancel();
        tableLayout.removeAllViews();
        mTotalTask = 1; mIdCounter = 0;

        editTextArrayList.clear();
        tableRowArrayList.clear();
        checkBoxArrayList.clear();
        textViewArrayList.clear();
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

    //TODO: Not currently in use, will remove.
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


    /***
     * Converts string into a long
     * @param time is a string that represents the time the user had chosen
     * @return the time as a long
     * For Example: A string '00:01:05' returns 65,000
     */
    private long convertTimeToMilli(String time){
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


    /***
     * Set a count down timer from the time the user had chosen. Updates textViewTime.
     * @param l is long representing the time
     */
    private void countDown(long l){

        countDownTimer = new CountDownTimer(l, 1000) {

            public void onTick(long millisUntilFinished) {
                long hr = millisUntilFinished / (60 * 60 * 1000);
                long min = (millisUntilFinished - (hr * 60 * 60 * 1000)) / 60000;
                long sec = (millisUntilFinished - (hr * 60 * 60 * 1000) - (min * 60000)) / 1000;
                NumberFormat numberFormat = new DecimalFormat("00");

                String tmp = numberFormat.format(hr) + ":" + numberFormat.format(min) + ":" + numberFormat.format(sec);
                textViewTime.setText(tmp);

            }
            public void onFinish() {
                textViewTime.setText(R.string.Times_Up);
                countDownTimer=null;
            }
        }.start();
    }

    /***
     * Remove every table row that has an empty edit text.
     * For every table row enable checkboxes, disable edit texts, set text view's text
     */
    private void confirmTaskList(){
        SharedPreferences settingsPref = getSharedPreferences("Settings",MODE_PRIVATE);

        for(int i = 0; i<editTextArrayList.size(); i++){
            if(!settingsPref.getBoolean("afterConfirm",false)) {
                editTextArrayList.get(i).setKeyListener(null);
                editTextArrayList.get(i).setFocusable(false);
            }
            checkBoxArrayList.get(i).setEnabled(true);

            String tmp = i+1 + " )";
            textViewArrayList.get(i).setText(tmp);

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

    /**
     * This function will create a dialog with three num-pickers represented as
     * 'HH', 'MM', and'SS'. Then set textViewTime with those values, '01:05:40'
     */
    private void setTextViewTime(){
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


    /**
     * dynamically create a table row that has edit text, checkbox, and text view.
     * If app had been closed and then reopen values will be reassigned. Otherwise
     * assign generic values
     * @param text text value from and EditText
     * @param check if the CheckBox was checked
     * @param started no in use
     */
    private void createNewTask(String text, Boolean check, Boolean started){
        //Create views
        TableRow tableRow = new TableRow(MainActivity.this);
        TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT,TableLayout.LayoutParams.WRAP_CONTENT,5.0f);

        EditText editText = new EditText(MainActivity.this);
        TableRow.LayoutParams editTextParams = new TableRow.LayoutParams(0,TableLayout.LayoutParams.WRAP_CONTENT,4.0f);

        CheckBox checkBox = new CheckBox(MainActivity.this);
        TableRow.LayoutParams checkBoxParams= new TableRow.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT,TableLayout.LayoutParams.WRAP_CONTENT,0.5f);

        TextView textView1 = new TextView(this);
        TableRow.LayoutParams textViewParams= new TableRow.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT,TableLayout.LayoutParams.WRAP_CONTENT,0.5f);

        // set layout parameters
        checkBoxParams.setMarginStart(30);

        editText.setLayoutParams(editTextParams);
        checkBox.setLayoutParams(checkBoxParams);
        textView1.setLayoutParams(textViewParams);
        tableRow.setLayoutParams(tableRowParams);

        // set views
        String tmp = mTotalTask + ") ";
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

        mTotalTask++;

        //TODO: Remove mIdCounter, I thought I would need and id for each view but right now I don't
        textView1.setId(mIdCounter);
        editText.setId(mIdCounter+1);
        checkBox.setId(mIdCounter+2);

        mIdCounter += 3;

        //add views to tableRow
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

    /**
     * Save all task in the list and timers value using shared preferences.
     * Called from onPause
     */
    private void saveState(){
        SharedPreferences sharedPreferences = getSharedPreferences("pref",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt("size", editTextArrayList.size());
        if(textViewTime.getText().toString().equals("Times Up!"))
            editor.putString("time","00:00:00");
        else
            editor.putString("time", textViewTime.getText().toString());

        editor.putBoolean("started",activeList);
        for(int i = 0; i < editTextArrayList.size(); i++){
            editor.putString("editText "+ i, editTextArrayList.get(i).getText().toString());
            editor.putBoolean("checkBox " + i, checkBoxArrayList.get(i).isChecked());
        }
        editor.putLong("systemTime", SystemClock.elapsedRealtime());
        editor.apply();

    }

    /**
     * Load all tasks and timer value from shared preference
     */
    private void loadState() {
        SharedPreferences sharedPreferences = getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences settingsPref = getSharedPreferences("Settings",MODE_PRIVATE);
        //SharedPreferences.Editor editor = sharedPreferences.edit();

        if (tableRowArrayList.size() <= 0) {
            int size = sharedPreferences.getInt("size", 0);
            long diff = SystemClock.elapsedRealtime() - sharedPreferences.getLong("systemTime",0);
            for (int i = 0; i < size; i++) {
                createNewTask(sharedPreferences.getString("editText " + i, ""), sharedPreferences.getBoolean("checkBox " + i, false),sharedPreferences.getBoolean("started", false));
            }


            textViewTime.setText(sharedPreferences.getString("time", "00:00:00"));
            if (sharedPreferences.getBoolean("started", false)) {
                Log.d(TAG, "loadState: started = " +sharedPreferences.getBoolean("started", false));
                if(!settingsPref.getBoolean("afterConfirm",false)) {
                    addTask.setEnabled(false);
                    confirm.setEnabled(false);
                }
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

    /**
     * create a notification that has a chronometer counting down from the time user chose.
     * @param l is the timer value
     */
    private void buildTimerNotification(long l){
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

        if(manager!=null)
            manager.notify(0, builder.build());
        Log.d(TAG, "build: build noti");
        //}
    }

    @Override
    protected void onDestroy() {
        // ok i think i know what cause the service to be created twice, because
        // service was stopped when main was destroyed and when service was so perhaps two
        // services were created through the broadcast receiver.

        //stopService(new Intent(MainActivity.this, BackgroundService.class));
        Log.i("aa", "onDestroy! form MAIN!");
        super.onDestroy();

    }

    /**
     * This class will close the notification once the button is pressed.
     */
    public static class switchButtonListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d(TAG, "I am here");

            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if(manager!=null)
            manager.cancelAll();
        }
    }

}
