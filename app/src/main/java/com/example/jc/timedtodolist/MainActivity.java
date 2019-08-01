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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.RemoteViews;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    Button addTask, confirmToDoList, resetToDoList;
    boolean isToDoListActive = false;
    TextView textViewTimer;
    MaxHeightScrollView maxHeightScrollView;
    //LinearLayout linearLayout;
    TableLayout tableLayout;
    CountDownTimer countDownTimer = null;
    Chronometer chronometer;
    ToDoList toDoList;

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

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //instantiate vars
        chronometer = findViewById(R.id.chronometer);
        chronometer.setBase(SystemClock.elapsedRealtime());
        addTask = findViewById(R.id.btnTaskAdd);
        confirmToDoList = findViewById(R.id.btnTaskConfirm);
        textViewTimer = findViewById(R.id.textTime);
        maxHeightScrollView = findViewById(R.id.scrollView);
        tableLayout = findViewById(R.id.tableLayout);
        resetToDoList = findViewById(R.id.btnTaskReset);
        toDoList = new ToDoList(this);
        toDoList.setTableLayout(tableLayout);

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (getResources().getConfiguration().smallestScreenWidthDp >= 720 )
                maxHeightScrollView.setScale(0.6f);
            else if( getResources().getConfiguration().smallestScreenWidthDp >= 600)
                maxHeightScrollView.setScale(0.5f);
        }

        textViewTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    setTextViewTimer();

            }
        });
        textViewTimer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                setTimeTimePicker();
                return false;
            }
        });
        addTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d(TAG, "onClick: addTask Pressed");
                    createNewTask();

            }
        });

        confirmToDoList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!toDoList.isToDoListEmpty() &&
                        !(textViewTimer.getText().toString().equals(getString(R.string.default_text_time)) ||
                                textViewTimer.getText().toString().equals(getString(R.string.disabled_timer_text)))) {
                    listConfirmed();
                }
                //TODO:Check and then remove this else if
                else if (settings.getBoolean("disableTimer", false))
                    listConfirmed();
                else
                    Toast.makeText(MainActivity.this, "ToDoList and Timer can't be empty!",
                            Toast.LENGTH_LONG).show();
            }
        });

        resetToDoList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toDoList.resetList();
                resetViews();
                cancelAlarmManager();
                clearAllNotifications();
            }
        });
    }

    private void listConfirmed() {
        toDoList.confirmList();
        long time = 0;
        if (!settings.getBoolean("disableTimer", false)) {
            time = convertTimeToMilliseconds(textViewTimer.getText().toString());
            countDown(time);
        }
        confirmToDoList.setEnabled(false);
        addTask.setEnabled(settings.getBoolean("addTaskSetting", false));
        if(!settings.getBoolean("addTimeSetting", false))
            textViewTimer.setClickable(false);
        else
            textViewTimer.setClickable(!settings.getBoolean("disableTimer",false));
        textViewTimer.setLongClickable(false);
        resetToDoList.setText(R.string.btn_text_finished);
        isToDoListActive = true;
        if (settings.getBoolean("remaining", true) && !settings.getBoolean("disableTimer", false))
            buildTimeRemainingNotification(time);
        if (settings.getBoolean("finished", true) && !settings.getBoolean("disableTimer", false))
            createAlarmManager(time);
    }

    private Task createNewTask() {
        Task task = new Task(this);
        task.addNewTask();
        if (isToDoListActive)
            task.getTaskCheckBox().setEnabled(settings.getBoolean("addTaskSetting", true));
        toDoList.addTask(task);
        return task;
    }

    public void setTimeTimePicker(){
        final AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_time_picker, null);
        final TimePicker timePicker = dialogView.findViewById(R.id.timerPicker);
        timePicker.setIs24HourView(true);
        dialog.setView(dialogView);
        dialog.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                DecimalFormat decimalFormat = new DecimalFormat("00");
                String time =( decimalFormat.format(timePicker.getCurrentHour())
                        + ":" + decimalFormat.format(timePicker.getCurrentMinute())
                        + ":" + decimalFormat.format(0) );
                textViewTimer.setText(time);
                dialogInterface.dismiss();
            }
        });
        dialog.show();
    }

    /**
     * This function will create a dialog with three num-pickers represented as
     * 'HH', 'MM', and'SS'. Then set textViewTimer according to  those values. Ex: '01:05:40'
     */
    private void setTextViewTimer() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_set_time, null);

        dialog.setView(dialogView);
        if(settings.getBoolean("addTimeSetting", false) && isToDoListActive){
            addTimeNumberPicker(dialog,dialogView);
        }
        else {
            setTimeNumberPicker(dialog, dialogView);
        }
        dialog.show();
    }

    private void setTimeNumberPicker(AlertDialog.Builder dialog, View dialogView){
        final NumberPicker hour = dialogView.findViewById(R.id.numPickerHour);
        final NumberPicker min = dialogView.findViewById(R.id.numPickerMin);
        final NumberPicker sec = dialogView.findViewById(R.id.numPickerSec);
        hour.setMaxValue(23);
        min.setMaxValue(59);
        sec.setMaxValue(59);

        dialog.setTitle("Set Time");
        dialog.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                DecimalFormat decimalFormat = new DecimalFormat("00");
                String time = decimalFormat.format(hour.getValue()) + ":" +
                        decimalFormat.format(min.getValue()) + ":" + decimalFormat.format(sec.getValue());
                textViewTimer.setText(time);

            }
        });
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
    }

    private void addTimeNumberPicker(AlertDialog.Builder dialog, View dialogView){
        final NumberPicker hour = dialogView.findViewById(R.id.numPickerHour);
        final NumberPicker min = dialogView.findViewById(R.id.numPickerMin);
        final NumberPicker sec = dialogView.findViewById(R.id.numPickerSec);
        hour.setMaxValue(23);
        min.setMaxValue(59);
        sec.setMaxValue(59);

        dialog.setTitle("Add Time");
        dialog.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String[] currentTimerValue;
                if(!textViewTimer.getText().toString().equals(getString(R.string.Times_Up)))
                    currentTimerValue = textViewTimer.getText().toString().split(":");
                else
                    currentTimerValue = getString(R.string.default_text_time).split(":");

                DecimalFormat decimalFormat = new DecimalFormat("00");
                String time = decimalFormat.format(
                        (hour.getValue()) + (Integer.parseInt(currentTimerValue[0])) )
                        + ":" + decimalFormat.format(
                        min.getValue() + (Integer.parseInt(currentTimerValue[1])))
                        + ":" + decimalFormat.format(
                        sec.getValue() + (Integer.parseInt(currentTimerValue[2])));
                textViewTimer.setText(time);
                if (!settings.getBoolean("disableTimer", false)) {
                    if(countDownTimer != null)
                        countDownTimer.cancel();
                    long tmp = convertTimeToMilliseconds(textViewTimer.getText().toString());
                    countDown(tmp);
                    NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.cancelAll();
                    buildTimeRemainingNotification(tmp);
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

    /***
     * Set a count down timer from the time the user had chosen. Updates textViewTime.
     * @param time is long representing the time
     */
    private void countDown(long time) {
        countDownTimer = new CountDownTimer(time, 1000) {
            public void onTick(long millisecondsRemaining) {
                textViewTimer.setText(convertTimeToExtendedFormat(millisecondsRemaining));
            }

            public void onFinish() {
                textViewTimer.setText(R.string.Times_Up);
                countDownTimer = null;
            }
        }.start();
    }

    private String convertTimeToExtendedFormat(long millisecondsRemaining) {
        long hr = millisecondsRemaining / (60 * 60 * 1000);
        long min = (millisecondsRemaining - (hr * 60 * 60 * 1000)) / 60000;
        long sec = (millisecondsRemaining - (hr * 60 * 60 * 1000) - (min * 60000)) / 1000;
        NumberFormat numberFormat = new DecimalFormat("00");

        return numberFormat.format(hr) + ":" + numberFormat.format(min) + ":"
                + numberFormat.format(sec);
    }

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
            textViewTimer.setText(R.string.disabled_timer_text);
            textViewTimer.setClickable(false);
            textViewTimer.setLongClickable(false);
        } else {
            textViewTimer.setText(R.string.default_text_time);
            textViewTimer.setClickable(true);
            textViewTimer.setLongClickable(true);
        }
        isToDoListActive = false;
        if (countDownTimer != null)
            countDownTimer.cancel();
        addTask.setEnabled(true);
        confirmToDoList.setEnabled(true);
        resetToDoList.setText(R.string.btn_text_reset);
        toDoList.resetList();
        cancelAlarmManager();
        Task.setTotalNumberOfTask(0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        loadState();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }


    /**
     * Creates an alarm manager that calls a broadcast receiver MyReceiver.
     * MyReceiver will create a finished notification.
     *
     * @param time length of time in milli till finish notification should be sent
     */
    private void createAlarmManager(long time) {

        Intent notifyIntent = new Intent(MainActivity.this,
                MyReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast
                (MainActivity.this, PendingIntent.FLAG_ONE_SHOT,
                        notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (alarmManager != null) {
            //setExact requires api 19 or up.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Log.i(TAG, "onClick: api version greater than or equal to 19");
                alarmManager.setExact(AlarmManager.ELAPSED_REALTIME,
                        SystemClock.elapsedRealtime() + time, pendingIntent);
            } else {
                Log.i(TAG, "onClick: api version below 19");
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
                , CHANNEL_ONE_NAME, manager.IMPORTANCE_HIGH);
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.RED);
        notificationChannel.setShowBadge(true);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        manager.createNotificationChannel(notificationChannel);
    }

    private void clearAllNotifications() {
        //clear all notifications
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null)
            manager.cancelAll();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuSetting:
                Intent intentSettings = new Intent(this, SettingsActivity.class);
                this.startActivity(intentSettings);
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
        ImageButton green = dialogView.findViewById(R.id.btnSquareGreen);
        ImageButton purple = dialogView.findViewById(R.id.btnSquarePurple);
        ImageButton light = dialogView.findViewById(R.id.btnSquareLight);
        ImageButton gold = dialogView.findViewById(R.id.btnSquareGold);
        ImageButton cyan = dialogView.findViewById(R.id.btnSquareCyan);
        ImageButton brown = dialogView.findViewById(R.id.btnSquareBrown);

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

    private void refreshActivity(int i) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("theme", i);
        editor.apply();
        //   onPause();
        finish();
        startActivity(getIntent());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    private void saveState() {
        editor = sharedPreferences.edit();
        saveTimer();
        editor.putLong("systemTime", SystemClock.elapsedRealtime());
        editor.putInt("size", ToDoList.getListOfTasks().size());
        editor.putBoolean("isActive", isToDoListActive);
        editor.putBoolean("isAddTaskEnabled", addTask.isEnabled());
        editor.putBoolean("isConfirmEnabled", confirmToDoList.isEnabled());
        editor.putString("resetText", resetToDoList.getText().toString());

        saveToDoList();
        toDoList.resetList();
        editor.apply();
    }

    private void saveTimer() {
        if (textViewTimer.getText().toString().equals("Times Up!"))
            editor.putString("time", "00:00:00");
        else
            editor.putString("time", textViewTimer.getText().toString());
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
        Log.d(TAG, "loadState: ");
        if (toDoList.isToDoListEmpty()) {
            Log.d(TAG, "loadState: list is empty");
            isToDoListActive = sharedPreferences.getBoolean("isActive", false);
            loadToDoList();
            loadTimer();
            addTask.setEnabled(sharedPreferences.getBoolean("isAddTaskEnabled", true));
            confirmToDoList.setEnabled(sharedPreferences.getBoolean("isConfirmEnabled", true));
            resetToDoList.setText(sharedPreferences.getString("resetText", getString(R.string.btn_text_reset)));
        }
    }

    private void loadToDoList() {
        int size = sharedPreferences.getInt("size", 0);
        Log.d(TAG, "loadToDoList: ");
        for (int i = 1; i <= size; i++) {
            Task newTask = createNewTask();
            newTask.getTaskDesc().setText(sharedPreferences.getString("taskDescription " + i, ""));

            if (!settings.getBoolean("editTask", false)) {
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
        Log.d(TAG, "loadTimer: ");
        String time = sharedPreferences.getString("time", "00:00:00");

        if (settings.getBoolean("disableTimer", false)) {
            time = getString(R.string.disabled_timer_text);
            textViewTimer.setClickable(false);
            textViewTimer.setLongClickable(false);
        } else if (time.equals(getString(R.string.disabled_timer_text)) && !settings.getBoolean("disableTimer", false)){
            time = getString(R.string.default_text_time);
            textViewTimer.setClickable(true);
            textViewTimer.setLongClickable(true);
        }
        textViewTimer.setText(time);
        if (isToDoListActive) {
            Log.d(TAG, "loadTimer: list is active");
            textViewTimer.setClickable((!settings.getBoolean("disableTimer", false)) && settings.getBoolean("addTimeSetting", false));
            textViewTimer.setLongClickable(false);
            if (!textViewTimer.getText().toString().equals(getString(R.string.disabled_timer_text)))
                countDown((convertTimeToMilliseconds(time) - difference));
        }
      //  if (getResources().getString(R.string.Times_Up).equals(textViewTimer.getText().toString()))
        //    textViewTimer.setClickable(false);
    }


    /**
     * This class will close the notification once the button is pressed.
     */
    public static class switchButtonListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d(TAG, "I am here");

            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null)
                manager.cancelAll();
        }
    }

}