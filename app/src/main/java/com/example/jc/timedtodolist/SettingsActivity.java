package com.example.jc.timedtodolist;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SettingsActivity extends AppCompatActivity {


    private CheckBox timeRemaining,finishedNoti,sound,addTaskSetting,editTask,disableTimer,addTimeSetting;
    private Spinner changeSizeSetting;
    private Button restore,save;
    private String currentMax;



    @Override
    public void onBackPressed() {
        super.onBackPressed();

        saveSettings();
        finish();
        Intent refresh = new Intent(SettingsActivity.this, MainActivity.class);
        refresh.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(refresh);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = getSharedPreferences("save and load",MODE_PRIVATE);

        setTheme(sharedPreferences.getInt("theme",R.style.AppTheme));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        //getActionBar().setTitle("Settings");
        setTitle("Settings");
        
        timeRemaining =findViewById(R.id.settingTimeRemaining);
        finishedNoti = findViewById(R.id.settingFinishedNotification);
        sound = findViewById(R.id.settingFinishedSound);
        addTaskSetting = findViewById(R.id.settingAddTask);
        editTask = findViewById(R.id.settingEditTask);
        disableTimer = findViewById(R.id.settingDisableTimer);
        addTimeSetting = findViewById(R.id.settingAddTime);
        changeSizeSetting = findViewById(R.id.settingChangeMaxSize);
        restore = findViewById(R.id.btnRestoreDefault);
        save = findViewById(R.id.btnSaveSettings);



        changeSizeSetting.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currentMax = adapterView.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        loadSettings();
        restore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                restoreSettings();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveSettings();
                Toast.makeText(SettingsActivity.this, "Settings Saved!", Toast.LENGTH_SHORT).show();
                finish();
                Intent refresh = new Intent(SettingsActivity.this, MainActivity.class);
                refresh.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(refresh);
            }
        });
        finishedNoti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!finishedNoti.isChecked()){
                    sound.setChecked(false);
                    sound.setEnabled(false);
                }
                else {
                    //sound.setChecked(true);
                    sound.setEnabled(true);
                }
            }
        });
    }
    private void restoreSettings(){
        timeRemaining.setChecked(true);
        finishedNoti.setChecked(true);
        sound.setChecked(true);
        sound.setEnabled(true);
        addTaskSetting.setChecked(true);
        editTask.setChecked(false);
        disableTimer.setChecked(false);
        addTimeSetting.setChecked(false);
        Toast.makeText(SettingsActivity.this, "Default Settings Restored!",
                Toast.LENGTH_SHORT).show();

        saveSettings();
        Toast.makeText(SettingsActivity.this, "Settings Saved!", Toast.LENGTH_SHORT).show();
    }
    private void loadSettings(){
        SharedPreferences settings = getSharedPreferences("Settings",MODE_PRIVATE);

        timeRemaining.setChecked(settings.getBoolean("remaining",true));
        finishedNoti.setChecked(settings.getBoolean("finished",true));
        sound.setChecked(settings.getBoolean("sound",true));
        addTaskSetting.setChecked(settings.getBoolean("addTaskSetting",true));
        editTask.setChecked(settings.getBoolean("editTask",false));
        addTimeSetting.setChecked(settings.getBoolean("addTimeSetting", false));
        disableTimer.setChecked(settings.getBoolean("disableTimer",false));
        currentMax = settings.getString("maxNumberOfTask", "50");
        setSpinnerPosition();
        if(!finishedNoti.isChecked())
            sound.setEnabled(false);

    }
    private void setSpinnerPosition(){
        switch (currentMax){
            case "10":
                changeSizeSetting.setSelection(0);
                break;
            case "20":
                changeSizeSetting.setSelection(1);
                break;
            case "30":
                changeSizeSetting.setSelection(2);
                break;
            case "40":
                changeSizeSetting.setSelection(3);
                break;
            case "50":
                changeSizeSetting.setSelection(4);
                break;
        }
    }
    private void saveSettings(){
        SharedPreferences settings = getSharedPreferences("Settings",MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putBoolean("remaining",timeRemaining.isChecked());
        editor.putBoolean("finished",finishedNoti.isChecked());
        editor.putBoolean("sound",sound.isChecked());
        editor.putBoolean("addTaskSetting",addTaskSetting.isChecked());
        editor.putBoolean("editTask",editTask.isChecked());
        editor.putBoolean("disableTimer",disableTimer.isChecked());
        editor.putBoolean("addTimeSetting", addTimeSetting.isChecked());
        editor.putString("maxNumberOfTask",currentMax);
        editor.apply();


    }


    @Override
    protected void onPause() {
        super.onPause();
       // mSaveSettings();
    }

    @Override
    public void finish() {
        super.finish();

    }
}
