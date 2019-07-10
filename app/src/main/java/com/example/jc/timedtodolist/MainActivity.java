package com.example.jc.timedtodolist;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.RemoteViews;
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
    MaxHeightScrollView scrollView;
    //LinearLayout linearLayout;
    TableLayout tableLayout;
    CountDownTimer countDownTimer = null;
    Chronometer chronometer;
    ArrayList<TextView> textViewArrayList = new ArrayList<>();
    ArrayList<EditText> editTextArrayList = new ArrayList<>();
    ArrayList<CheckBox> checkBoxArrayList = new ArrayList<>();
    ArrayList<TableRow> tableRowArrayList = new ArrayList<>();
    private int totalTask = 1, idCounter = 0;


    private static final String CHANNEL_ONE_ID= "com.example.jc.simpletimedtodolist.channel_one_id";
    private static final String CHANNEL_ONE_NAME= "com.example.jc.simpletimedtodolist.channel_one_name";

    final static  String TAG = "mainActivity";
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        SharedPreferences settingsPref = getSharedPreferences("Settings",MODE_PRIVATE);

        setTheme(settingsPref.getInt("themeInt",R.style.AppTheme));



            super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //instantiate vars
        chronometer = findViewById(R.id.chronometer);
        chronometer.setBase(SystemClock.elapsedRealtime());
        addTask = findViewById(R.id.btnTaskAdd);
        confirm = findViewById(R.id.btnTaskConfirm);
        textViewTime = findViewById(R.id.textTime);
        scrollView = findViewById(R.id.scrollView);
        tableLayout = findViewById(R.id.tableLayout);
        reset = findViewById(R.id.btnTaskReset);

      //0  addTask.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

        //TODO: I wanted to know the display metrics of my devices will remove later
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowmanager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        windowmanager.getDefaultDisplay().getMetrics(displayMetrics);
        Log.d(TAG, "onCreate: DM height in px" + displayMetrics.heightPixels);
        Log.d(TAG, "onCreate: DM desnity" + displayMetrics.density);


        //Buttons onClick
        addTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: addTask Pressed");
                createNewTask("",null,false);
                Log.d(TAG, "onClick: sv H" + scrollView.getHeight());
            }
        });

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
                //Todo: delete
                cancelAlarmManager();

                //clear all notifications
                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if(manager!=null)
                    manager.cancelAll();

                //call reset func
                resetList();
                Toast.makeText(MainActivity.this,"Resetting",Toast.LENGTH_SHORT ).show();
            }
        });


        //lock the list, start the timer, create notification
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences settingsPref = getSharedPreferences("Settings",MODE_PRIVATE);


                //Check to see if there is a least 1 task and at least 1 second to complete it.
                if(editTextArrayList.size()>0 && !textViewTime.getText().toString().equals("00:00:00")) {
                    confirmTaskList();
                    //Makes sure the list can no longer be edited
                    confirm.setEnabled(false);
                    activeList = true;
                    textViewTime.setClickable(false);
                    reset.setText(R.string.Finished);

                    if(!settingsPref.getBoolean("afterConfirm",false)) {
                        addTask.setEnabled(false);
                    }

                    long tmp = convertTimeToMilli(textViewTime.getText().toString());
                    countDown(tmp);

                    // if user want to have a remaining and/or finished notification
                    if(settingsPref.getBoolean("finished",true) ||
                            settingsPref.getBoolean("remaining",true)) {

                        createAlarmManager(tmp);
                    }

                    // Notification requires a count down chronometer, only available in N and up
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N &&
                            settingsPref.getBoolean("remaining",true)) {

                        buildTimerNotification(tmp);
                    }

                }
                else if(!settingsPref.getBoolean("noTimer",false)){
                    Toast.makeText(MainActivity.this, "Add task to List and set time",
                            Toast.LENGTH_SHORT).show();

                }
                else{ //noTimer is true meaning the list can be started without any time
                        //Do not need to start countDown or create notifications

                    //Makes sure the list can no longer be edited
                    textViewTime.setClickable(false);
                    reset.setText(R.string.Finished);
                    activeList = true;
                    confirm.setEnabled(false);

                    if(!settingsPref.getBoolean("afterConfirm",false)) {
                        addTask.setEnabled(false);
                        confirm.setEnabled(false);
                    }

                }


            }
        });


    }

    /**
     * Creates an alarm manager that calls a broadcast receiver MyReceiver.
     * Which calls a alarm manger to create a finish notification.
     * @param tmp length of time in milli till finish notification should be sent
     */
    private void createAlarmManager(long tmp){

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

    private void cancelAlarmManager(){

        Intent notifyIntent = new Intent(MainActivity.this,
                MyReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast
                (MainActivity.this, PendingIntent.FLAG_ONE_SHOT,
                        notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if(alarmManager!=null)  alarmManager.cancel(pendingIntent);

    }

    /**
     * Reset all views and vars in order to create a new list
     */
    private void resetList(){
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
        totalTask = 1; idCounter = 0;

        editTextArrayList.clear();
        tableRowArrayList.clear();
        checkBoxArrayList.clear();
        textViewArrayList.clear();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuSetting:
                Intent intentSettings = new Intent(this,SettingsActivity.class);
                this.startActivity(intentSettings);
                return true;
            case R.id.menuTheme:
                createThemeDialog();
                /* I wanna create a dialog box instead and have the option to go to a new page
                Intent intentTheme = new Intent(this,SettingsActivity.class);
                this.startActivity(intentTheme);
                return true;*/
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void createThemeDialog(){
        final AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_customize_theme, null);
        final ImageButton blue = dialogView.findViewById(R.id.btnSquareBlue);
        final ImageButton dark = dialogView.findViewById(R.id.btnSquareDark);
        final ImageButton red = dialogView.findViewById(R.id.btnSquareRed);
        final ImageButton green = dialogView.findViewById(R.id.btnSquareGreen);
        final ImageButton purple = dialogView.findViewById(R.id.btnSquarePurple);
        final ImageButton light = dialogView.findViewById(R.id.btnSquareLight);
        final ImageButton gold = dialogView.findViewById(R.id.btnSquareGold);
        final ImageButton cyan = dialogView.findViewById(R.id.btnSquareCyan);
        final ImageButton brown = dialogView.findViewById(R.id.btnSquareBrown);
        
        blue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshActivity(R.style.AppTheme);
            }
        });
        dark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshActivity(R.style.AppThemeDark);
            }
        });
        red.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshActivity(R.style.AppThemeRed);
            }
        });
        green.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshActivity(R.style.AppThemeGreen);
            }
        });
        purple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshActivity(R.style.AppThemePurple);

            }
        });
        light.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshActivity(R.style.AppThemeLight);
            }
        });

        gold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshActivity(R.style.AppThemeGold);
            }
        });

        cyan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshActivity(R.style.AppThemeCyan);
            }
        });

        brown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshActivity(R.style.AppThemeBrown);
            }
        });

        dialog.setView(dialogView);
        dialog.setTitle("Customize App Theme");
        dialog.setMessage("Choose a theme color preset");
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        dialog.show();
    }

    public void refreshActivity(int i){
        SharedPreferences settingsPref = getSharedPreferences("Settings",MODE_PRIVATE);
        SharedPreferences.Editor editor = settingsPref.edit();
        editor.putInt("themeInt",i);
        editor.apply();
        onPause();
        finish();
        startActivity(getIntent());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.menu_main,menu);
         return true;//return super.onCreateOptionsMenu(menu);
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

                String tmp = numberFormat.format(hr) + ":" + numberFormat.format(min) + ":"
                        + numberFormat.format(sec);
                textViewTime.setText(tmp);

            }
            public void onFinish() {
                textViewTime.setText(R.string.Times_Up);
                //addTask.setEnabled(false);
                //confirm.setEnabled(false);

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
            if(!settingsPref.getBoolean("afterConfirm",false) ) {
                editTextArrayList.get(i).setKeyListener(null);
                editTextArrayList.get(i).setFocusable(false);
             //   if()
                checkBoxArrayList.get(i).setEnabled(true);
            }



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
        totalTask=tableRowArrayList.size()+1;
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
        final TableRow tableRow = new TableRow(MainActivity.this);
        TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT,TableLayout.LayoutParams.WRAP_CONTENT,5.0f);

        final EditText editText = new EditText(MainActivity.this);
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
        String tmp = totalTask + " ) ";
        textView1.setText(tmp);
        textView1.setTextSize(18);
        textView1.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        editText.setHint("Leave blank if not needed!");
        editText.requestFocus();
        editText.setTextSize(18);
        editText.setText(text);

        tableRow.setPadding(4,8,4,32);

        SharedPreferences settingsPref = getSharedPreferences("Settings",MODE_PRIVATE);


        if(check!=null && started) {
            checkBox.setEnabled(true);
            checkBox.setChecked(check);

            //editText.setKeyListener(null);
        }
        else if(!settingsPref.getBoolean("afterConfirm",false)) {
            checkBox.setEnabled(false);
          //  editText.setFocusable(false);
        }
        Log.d(TAG, "createNewTask: set properties for views");

        totalTask++;

        //TODO: Remove mIdCounter, I thought I would need and id for each view but right now I don't
        textView1.setId(idCounter);
        editText.setId(idCounter+1);
        checkBox.setId(idCounter+2);

        idCounter += 3;

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


        // this works--
        textView1.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Toast.makeText(MainActivity.this,"worked",Toast.LENGTH_SHORT).show();
                createRemoveDialog(tableRow);
                return false;
            }
        });
    }

    private void createRemoveDialog(final TableRow  tableRow){
       AlertDialog.Builder alert = new AlertDialog.Builder(this);

       alert.setMessage("Are you sure you want to remove the task? (May mess up numbering)")
               .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialogInterface, int i) {
                       dialogInterface.dismiss();
                   }
               })
               .setPositiveButton("Yes, I'm Sure", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialogInterface, int i) {
                       ViewGroup parent = (ViewGroup) tableRow.getParent();


                           for(int j = 0; j<tableRowArrayList.size();j++){
                               if(tableRowArrayList.get(j) == tableRow){
                                   if (parent != null) {
                                      /* tableRow.setVisibility(View.GONE);
                                       tableRow.removeAllViews();
                                       parent.removeView(tableRow);*/

                                       tableRowArrayList.get(j).setVisibility(View.GONE);
                                       tableRowArrayList.remove(j);
                                       editTextArrayList.remove(j);
                                       textViewArrayList.remove(j);
                                       checkBoxArrayList.remove(j);

                                   }
                               }
                           }
                           //confirmTaskList();

                           //saveState();


                   }
               })
               .create();
       alert.show();

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

        editor.putString("reset",reset.getText().toString());
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
       // if(sharedPreferences.getString("reset","Reset?").equals("Reset?"))
        reset.setText(sharedPreferences.getString("reset","Reset?"));
        if (tableRowArrayList.size() <= 0) {

            int size = sharedPreferences.getInt("size", 0);
            long diff = SystemClock.elapsedRealtime() - sharedPreferences.getLong("systemTime",0);
            for (int i = 0; i < size; i++) {
                createNewTask(sharedPreferences.getString("editText " + i, ""), sharedPreferences.getBoolean("checkBox " + i, false),sharedPreferences.getBoolean("started", false));
            }
            confirmTaskList();
            if(settingsPref.getBoolean("afterConfirm",false)) {
                confirm.setEnabled(false);
            }

            activeList = sharedPreferences.getBoolean("started", false);
            textViewTime.setText(sharedPreferences.getString("time", "00:00:00"));
            if (activeList && !sharedPreferences.getString("time","00:00:00").equals("00:00:00")) {
                Log.d(TAG, "loadState: started = " +sharedPreferences.getBoolean("started", false));
                if(!settingsPref.getBoolean("afterConfirm",false)) {
                    addTask.setEnabled(false);
                    confirm.setEnabled(false);
                }
                textViewTime.setClickable(false);
                countDown( (convertTimeToMilli(sharedPreferences.getString("time", "00:00:00")) - diff) );

            }
            else if(settingsPref.getBoolean("noTimer",false) && activeList){
                textViewTime.setClickable(false);
                if(!settingsPref.getBoolean("afterConfirm",false)) {
                    addTask.setEnabled(false);
                    confirm.setEnabled(false);
                }
                textViewTime.setText(sharedPreferences.getString("time","00:00:00"));
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
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ONE_ID
                    ,CHANNEL_ONE_NAME,manager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setShowBadge(true);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            manager.createNotificationChannel(notificationChannel);
        }


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,CHANNEL_ONE_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContent(remoteViews)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_MAX);

        Intent notificationIntent = new Intent(this,MainActivity.class);
        PendingIntent contentIntent =  PendingIntent.getActivity(this,0,notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(contentIntent);


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
