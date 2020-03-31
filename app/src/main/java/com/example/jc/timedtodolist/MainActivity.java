package com.example.jc.timedtodolist;

import android.annotation.TargetApi;
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
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.RemoteViews;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    Button btnAddTask, btnConfirmToDoList, btnResetToDoList;
    boolean isToDoListActive = false;
    TextView tvTimer;
    MaxHeightScrollView maxHeightScrollView;
    //LinearLayout linearLayout;
    TableLayout tableLayout;
    CountDownTimer countDownTimer = null;
    Chronometer chronometer;
    ToDoList toDoList;
    DatabaseHelper databaseHelper;

    private static final String CHANNEL_ONE_ID = "com.example.jc.simpletimedtodolist.channel_one_id";
    private static final String CHANNEL_ONE_NAME = "com.example.jc.simpletimedtodolist.channel_one_name";


    SharedPreferences sharedPreferences, settings;
    SharedPreferences.Editor editor;
    final static String TAG = "MainActivity";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        sharedPreferences = getSharedPreferences("save and load", MODE_PRIVATE);
        settings = getSharedPreferences("Settings", MODE_PRIVATE);
        setTheme(sharedPreferences.getInt("theme", R.style.AppTheme));
        databaseHelper = new DatabaseHelper(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);


        //instantiate vars
        chronometer = findViewById(R.id.chronometer);
        chronometer.setBase(SystemClock.elapsedRealtime());
        btnAddTask = findViewById(R.id.btnTaskAdd);
        btnConfirmToDoList = findViewById(R.id.btnTaskConfirm);
        tvTimer = findViewById(R.id.textTime);
        maxHeightScrollView = findViewById(R.id.scrollView);
        tableLayout = findViewById(R.id.tableLayout);
        btnResetToDoList = findViewById(R.id.btnTaskReset);
        toDoList = new ToDoList(this);
        toDoList.setTableLayout(tableLayout);

        // for tablets in landscape mode
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (getResources().getConfiguration().smallestScreenWidthDp >= 720 )
                maxHeightScrollView.setScale(0.6f);
            else if( getResources().getConfiguration().smallestScreenWidthDp >= 600)
                maxHeightScrollView.setScale(0.5f);
        }

        tvTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createTextViewDialog();
            }
        });

        //TODO: Remove longClick and all related functions
        tvTimer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                createTimePickerDialog();
                return false;
            }
        });

        btnAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    createNewTask();

            }
        });

        btnConfirmToDoList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*ArrayList<String> l = new ArrayList<>();
                l.add("my,task,added");
                l.add("");
                l.add(",,,,asdf,,,,");
                byte[] by =  makebyte(l);
                read(by);*/

                if (!toDoList.isToDoListEmpty() &&
                        !(tvTimer.getText().toString().equals(getString(R.string.default_timer_text)) ||
                                tvTimer.getText().toString().equals(getString(R.string.disabled_timer_text)))) {
                    confirmList();
                }
                else if (settings.getBoolean("disableTimer", false))
                    confirmList();
                else
                    Toast.makeText(MainActivity.this, "ToDoList and Timer can't be empty!",
                            Toast.LENGTH_LONG).show();
            }
        });

        btnResetToDoList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //toDoList.resetList();
                resetViews();
                clearAllNotifications();
            }
        });
    }

    private void confirmList() {
        toDoList.confirmList();
        long time = 0;
        if (!settings.getBoolean("disableTimer", false)) {
            time = convertTimeToMilliseconds(tvTimer.getText().toString());
            countDown(time);
        }
        setViewsOnceActive();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            if (settings.getBoolean("remaining", true) && !settings.getBoolean("disableTimer", false))
                buildTimeRemainingNotification(time);
        if ( !settings.getBoolean("disableTimer", false))
            createAlarmManager(time);
    }

    private void setViewsOnceActive(){
        btnConfirmToDoList.setEnabled(false);
        btnAddTask.setEnabled(settings.getBoolean("addTaskSetting", false));
        if(!settings.getBoolean("addTimeSetting", false))
            tvTimer.setClickable(false);
        else
            tvTimer.setClickable(!settings.getBoolean("disableTimer",false));
        tvTimer.setLongClickable(false);
        btnResetToDoList.setText(R.string.btn_text_finished);
        isToDoListActive = true;
    }

    private Task createNewTask() {
        Task task = new Task(this);
        task.addNewTask();
        if (isToDoListActive)
            task.getTaskCheckBox().setEnabled(settings.getBoolean("addTaskSetting", true));
        toDoList.addTask(task);
        return task;
    }

    private void createFinishedDialog(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        dialog.setTitle("Finished?")
                .setMessage("Do you want to save this ToDo list?")
                .setNegativeButton("Don't Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                      ;//  toDoList.getCompleted().clear(); toDoList.getFailed().clear();
                    }
                })
                .setPositiveButton("Yes, Save!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        byte[] completedByteArr = makebyte(toDoList.getCompleted());
                        byte[] failedByteArr = makebyte(toDoList.getFailed());

                        //read(by);
                        Date currentTime = Calendar.getInstance().getTime();
                        databaseHelper.addData(currentTime.toString(),completedByteArr,failedByteArr);
                        Toast.makeText(MainActivity.this, "ToDo List Saved!", Toast.LENGTH_SHORT).show();
                    }
                })
                .create()
                .show();
    }
    public void createTimePickerDialog(){
        final AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_time_picker, null);
        final TimePicker timePicker = dialogView.findViewById(R.id.timerPicker);
        timePicker.setIs24HourView(true);
        dialog.setTitle("Set Time");
        dialog.setView(dialogView);
        dialog.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                DecimalFormat decimalFormat = new DecimalFormat("00");
                String time =( decimalFormat.format(timePicker.getCurrentHour())
                        + ":" + decimalFormat.format(timePicker.getCurrentMinute())
                        + ":" + decimalFormat.format(0) );
                tvTimer.setText(time);
                dialogInterface.dismiss();
            }
        });
        dialog.show();
    }

    private void createTextViewDialog() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_set_time, null);

        dialog.setView(dialogView);
        if(settings.getBoolean("addTimeSetting", false) && isToDoListActive){
            handleDialogButtonsForAddingTime(dialog,dialogView);
        }
        else {
            handelDialogButtonsForSettingTime(dialog, dialogView);
        }
        dialog.show();
    }


    private void handelDialogButtonsForSettingTime(AlertDialog.Builder dialog, final View dialogView){
        dialog.setTitle("Set Time");
        dialog.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                setTextViewTime(dialogView);
            }
        });
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
    }

    /**
     * Time will be set in HH:MM:SS format. Values for HH, MM, SS is acquired from Number Pickers
     * from dialogView
     * @param dialogView
     */
    private void setTextViewTime(View dialogView){
        NumberPickerWithMinMax hour = dialogView.findViewById(R.id.numPickerHour);
        NumberPickerWithMinMax min = dialogView.findViewById(R.id.numPickerMin);
        NumberPickerWithMinMax sec = dialogView.findViewById(R.id.numPickerSec);

        DecimalFormat decimalFormat = new DecimalFormat("00");
        String time = decimalFormat.format(hour.getValue()) + ":" +
                decimalFormat.format(min.getValue()) + ":" + decimalFormat.format(sec.getValue());
        tvTimer.setText(time);
    }

    private void handleDialogButtonsForAddingTime(AlertDialog.Builder dialog, final View dialogView){
        dialog.setTitle("Add Time");
        dialog.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                setTextViewForAdding(dialogView);
                if (!settings.getBoolean("disableTimer", false)) {
                    addTimeToCountDown();
                }
            }
        });
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
    }

    private void setTextViewForAdding(View dialogView){
        final NumberPickerWithMinMax hour = dialogView.findViewById(R.id.numPickerHour);
        final NumberPickerWithMinMax min = dialogView.findViewById(R.id.numPickerMin);
        final NumberPickerWithMinMax sec = dialogView.findViewById(R.id.numPickerSec);

        String[] currentTimerValue;
        if(!tvTimer.getText().toString().equals(getString(R.string.Times_Up)))
            currentTimerValue = tvTimer.getText().toString().split(":");
        else
            currentTimerValue = getString(R.string.default_timer_text).split(":");

        DecimalFormat decimalFormat = new DecimalFormat("00");
        String time = decimalFormat.format(
                (hour.getValue()) + (Integer.parseInt(currentTimerValue[0])) )
                + ":" + decimalFormat.format(
                min.getValue() + (Integer.parseInt(currentTimerValue[1])))
                + ":" + decimalFormat.format(
                sec.getValue() + (Integer.parseInt(currentTimerValue[2])));
        tvTimer.setText(time);
    }


    private void addTimeToCountDown(){
        if(countDownTimer != null)
            countDownTimer.cancel();
        long time = convertTimeToMilliseconds(tvTimer.getText().toString());
        countDown(time);
        createNewCountDownNotification(time);
    }

    private void createNewCountDownNotification(long time){
        cancelAlarmManager();
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancelAll();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            buildTimeRemainingNotification(time);
        createAlarmManager(time);
    }

    /***
     * Set a count down timer from the time the user had chosen. Updates textViewTime.
     * @param time is long representing the time
     */
    private void countDown(long time) {
        countDownTimer = new CountDownTimer(time, 1000) {
            public void onTick(long millisecondsRemaining) {
                tvTimer.setText(convertTimeToExtendedFormat(millisecondsRemaining));
            }

            public void onFinish() {
                tvTimer.setText(R.string.Times_Up);
                countDownTimer = null;
            }
        }.start();
    }

    /**
     * Convert milliseconds to a human readable time( i.e. 21:50:30 )
     * @param millisecondsRemaining a long variable representing milliseconds
     * @return return a string representing time in the following format "08:45:10"
     */
    private String convertTimeToExtendedFormat(long millisecondsRemaining) {
        long hr = millisecondsRemaining / (60 * 60 * 1000);
        long min = (millisecondsRemaining - (hr * 60 * 60 * 1000)) / 60000;
        long sec = (millisecondsRemaining - (hr * 60 * 60 * 1000) - (min * 60000)) / 1000;
        NumberFormat numberFormat = new DecimalFormat("00");

        return numberFormat.format(hr) + ":" + numberFormat.format(min) + ":"
                + numberFormat.format(sec);
    }

    /**
     * Convert a string representing time into millieseconds
     * @param time Time is in the following format "02:10:50"
     * @return a long representing milliseconds i.e. 360,000
     */
    private long convertTimeToMilliseconds(String time) {
        long milli = 0;
        String[] arr;
        arr = time.split(":");
        milli += Integer.parseInt(arr[0]) * 60 * 60 * 1000;
        milli += Integer.parseInt(arr[1]) * 60 * 1000;
        milli += Integer.parseInt(arr[2]) * 1000;

        return milli;
    }

    private void resetViews() {
        if (settings.getBoolean("disableTimer", false)) {
            tvTimer.setText(R.string.disabled_timer_text);
            tvTimer.setClickable(false);
            tvTimer.setLongClickable(false);
        } else {
            tvTimer.setText(R.string.default_timer_text);
            tvTimer.setClickable(true);
            tvTimer.setLongClickable(true);
        }
        isToDoListActive = false;
        if (countDownTimer != null)
            countDownTimer.cancel();
        btnAddTask.setEnabled(true);
        btnConfirmToDoList.setEnabled(true);
        btnResetToDoList.setText(R.string.btn_text_reset);

        toDoList.resetList();


        createFinishedDialog();
        cancelAlarmManager();
        Task.setTotalNumberOfTask(0);
    }


    /**
     * Creates an alarm manager that calls the broadcast receiver MyReceiver.
     * MyReceiver will create a finished notification.
     *
     * @param time length of time in milli till finish notification should be sent
     */
    private void  createAlarmManager(long time) {
        Intent notifyIntent = new Intent(MainActivity.this,
                MyReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast
                (MainActivity.this, PendingIntent.FLAG_ONE_SHOT,
                        notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (alarmManager != null) {
            //setExact requires api 19 or up.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.ELAPSED_REALTIME,
                        SystemClock.elapsedRealtime() + time, pendingIntent);
            } else {
                alarmManager.set(AlarmManager.ELAPSED_REALTIME,
                        SystemClock.elapsedRealtime() + time, pendingIntent);
            }
        }
    }

    private void cancelAlarmManager() {
        Intent notifyIntent = new Intent(MainActivity.this,
                MyReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast
                (MainActivity.this, PendingIntent.FLAG_ONE_SHOT,
                        notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (alarmManager != null) alarmManager.cancel(pendingIntent);

    }


    private void buildTimeRemainingNotification(long time) {
        final RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.dialog_build_time_noti);
        remoteViews.setChronometer(R.id.remoteChrono, time + SystemClock.elapsedRealtime(), null, true);
        remoteViews.setTextViewText(R.id.remoteText, "Time Remaining:");
        if (time == 0) {
            remoteViews.setChronometer(R.id.remoteChrono, time + SystemClock.elapsedRealtime(), chronometer.getFormat(), false);
            remoteViews.setTextViewText(R.id.remoteText, "done");
        }

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O && manager != null) {
            createNotificationChannel(manager);
        }


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ONE_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContent(remoteViews)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_MAX);

        PendingIntent pendingSwitchIntent = createPendingSwitchIntent(builder);
        remoteViews.setOnClickPendingIntent(R.id.remoteButton, pendingSwitchIntent);

        if (manager != null)
            manager.notify(0, builder.build());

    }

    private PendingIntent createPendingSwitchIntent(NotificationCompat.Builder builder) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        Intent switchIntent = new Intent(this, MainActivity.switchButtonListener.class);
        PendingIntent pendingSwitchIntent = PendingIntent.getBroadcast(this, 0,
                switchIntent, 0);
        return pendingSwitchIntent;

    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannel(NotificationManager manager) {
        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ONE_ID
                , CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_HIGH);

        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.RED);
        notificationChannel.setShowBadge(true);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        manager.createNotificationChannel(notificationChannel);
    }


    private void clearAllNotifications() {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null)
            manager.cancelAll();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuSetting:
                Intent intentSettings = new Intent(this, SettingsActivity.class);
                this.startActivity(intentSettings);
                return true;
            case R.id.menuPrevLists:
                Intent intent = new Intent(this, PreviousLists.class);
                this.startActivity(intent);
                return true;
            case R.id.menuTheme:
                createThemeDialog();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void createThemeDialog() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_customize_theme, null);
        ImageButton blue = dialogView.findViewById(R.id.btnSquareBlue);
        ImageButton dark = dialogView.findViewById(R.id.btnSquareDark);
        ImageButton red = dialogView.findViewById(R.id.btnSquareRed);
        ImageButton light = dialogView.findViewById(R.id.btnSquareLight);
        ImageButton green = dialogView.findViewById(R.id.btnSquareGreen);
        ImageButton purple = dialogView.findViewById(R.id.btnSquarePurple);
        ImageButton gold = dialogView.findViewById(R.id.btnSquareGold);
        ImageButton cyan = dialogView.findViewById(R.id.btnSquareCyan);
        ImageButton brown = dialogView.findViewById(R.id.btnSquareBrown);
        ImageButton[] imageButtonArray = {blue,dark,red,light,green,purple,gold,cyan,brown};


        changeCurrentImageButtonsBackground(imageButtonArray);
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
                dialogInterface.dismiss();
            }
        });

        dialog.show();

    }

    private void changeCurrentImageButtonsBackground(ImageButton[] imageButtonArray){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int currentTheme = sharedPreferences.getInt("theme", R.style.AppTheme);
            switch (currentTheme){
                case R.style.AppTheme:
                    imageButtonArray[0].setBackground(getDrawable(R.drawable.square_blue_transparent));
                    imageButtonArray[0].setEnabled(false);
                    break;
                case R.style.AppThemeDark:
                    imageButtonArray[1].setBackground(getDrawable(R.drawable.square_dark_transparent));
                    imageButtonArray[1].setEnabled(false);
                    break;
                case R.style.AppThemeRed:
                    imageButtonArray[2].setBackground(getDrawable(R.drawable.square_red_transparent));
                    imageButtonArray[2].setEnabled(false);
                    break;
                case R.style.AppThemeLight:
                    imageButtonArray[3].setBackground(getDrawable(R.drawable.square_light_transparent));
                    imageButtonArray[3].setEnabled(false);
                    break;
                case R.style.AppThemeGreen:
                    imageButtonArray[4].setBackground(getDrawable(R.drawable.square_green_transparent));
                    imageButtonArray[4].setEnabled(false);
                    break;
                case R.style.AppThemePurple:
                    imageButtonArray[5].setBackground(getDrawable(R.drawable.square_purple_transparent));
                    imageButtonArray[5].setEnabled(false);
                    break;
                case R.style.AppThemeGold:
                    imageButtonArray[6].setBackground(getDrawable(R.drawable.square_gold_transparent));
                    imageButtonArray[6].setEnabled(false);
                    break;
                case R.style.AppThemeCyan:
                    imageButtonArray[7].setBackground(getDrawable(R.drawable.square_cyan_transparent));
                    imageButtonArray[7].setEnabled(false);
                    break;
                case R.style.AppThemeBrown:
                    imageButtonArray[8].setBackground(getDrawable(R.drawable.square_brown_transparent));
                    imageButtonArray[8].setEnabled(false);
                    break;
            }
        }
    }


    private void refreshActivity(int i) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("theme", i);
        editor.apply();
        finish();
        startActivity(getIntent());
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

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }


    private void saveState() {
        editor = sharedPreferences.edit();
        saveTimer();
        editor.putLong("systemTime", SystemClock.elapsedRealtime());
        editor.putInt("size", ToDoList.getListOfTasks().size());
        editor.putBoolean("isActive", isToDoListActive);
        editor.putBoolean("addTaskSetting", btnAddTask.isEnabled());
        editor.putBoolean("isConfirmEnabled", btnConfirmToDoList.isEnabled());
        editor.putString("resetText", btnResetToDoList.getText().toString());

        saveToDoList();
        toDoList.resetList();
        editor.apply();
    }

    private void saveTimer() {
        if (tvTimer.getText().toString().equals("Times Up!"))
            editor.putString("time", "00:00:00");
        else
            editor.putString("time", tvTimer.getText().toString());
    }

    private void saveToDoList() {
        Iterator iterator = ToDoList.getListOfTasks().iterator();
        Task currentTask;
        int ct = 1;
        while (iterator.hasNext()) {
            currentTask = (Task) iterator.next();
            editor.putString("taskDescription " + ct,
                    currentTask.getTaskDesc().getText().toString());
            editor.putBoolean("taskChecked " + ct, currentTask.getTaskCheckBox().isChecked());
            ct++;
        }
    }

    private void loadState() {
        isToDoListActive = sharedPreferences.getBoolean("isActive", false);
        if(isToDoListActive)
            btnAddTask.setEnabled(settings.getBoolean("addTaskSetting", true));
        if (toDoList.isToDoListEmpty()) {
            loadToDoList();
            loadTimer();
            btnConfirmToDoList.setEnabled(sharedPreferences.getBoolean("isConfirmEnabled", true));
            btnResetToDoList.setText(sharedPreferences.getString("resetText", getString(R.string.btn_text_reset)));
        }
    }

    private void loadToDoList() {
        int size = sharedPreferences.getInt("size", 0);

        for (int i = 1; i <= size; i++) {
            Task newTask = createNewTask();
            newTask.getTaskDesc().setText(sharedPreferences.getString("taskDescription " + i, ""));

            if (!settings.getBoolean("editTask", false) && isToDoListActive) {
                newTask.getTaskDesc().setKeyListener(null);
                newTask.getTaskDesc().setFocusable(false);
            }
            newTask.getTaskCheckBox().setChecked(sharedPreferences.getBoolean("taskChecked " + i, false));
            if (isToDoListActive) {
                newTask.getTaskCheckBox().setEnabled(true);
            }
        }
    }

    private void loadTimer() {
        long difference = SystemClock.elapsedRealtime() - sharedPreferences.getLong("systemTime", 0);

        String time = sharedPreferences.getString("time", "00:00:00");

        if (settings.getBoolean("disableTimer", false)) {
            time = getString(R.string.disabled_timer_text);
            tvTimer.setClickable(false);
            tvTimer.setLongClickable(false);
        } else if (time.equals(getString(R.string.disabled_timer_text)) && !settings.getBoolean("disableTimer", false)){
            time = getString(R.string.default_timer_text);
            tvTimer.setClickable(true);
            tvTimer.setLongClickable(true);
        }
        tvTimer.setText(time);
        if (isToDoListActive) {
            tvTimer.setClickable((!settings.getBoolean("disableTimer", false)) && settings.getBoolean("addTimeSetting", false));
            tvTimer.setLongClickable(false);
            if (!tvTimer.getText().toString().equals(getString(R.string.disabled_timer_text)))
                countDown((convertTimeToMilliseconds(time) - difference));
        }
    }


    public byte[] makebyte(ArrayList<String> modeldata) {
        try {

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(modeldata);
            byte[] employeeAsBytes = baos.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(employeeAsBytes);
            modeldata.clear();
            return employeeAsBytes;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    public byte[] makebyte2(ArrayList<Integer> modeldata) {
        try {

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(modeldata);
            byte[] employeeAsBytes = baos.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(employeeAsBytes);
            modeldata.clear();
            return employeeAsBytes;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    /**
     * This class will close the notification once the remote button is pressed.
     */
    public static class switchButtonListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null)
                manager.cancelAll();
        }
    }

}