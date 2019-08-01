package com.example.jc.timedtodolist;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Switch;
import android.widget.Toast;

import java.util.Set;

public class SettingsActivity extends AppCompatActivity {


    private CheckBox timeRemaining,finishedNoti,sound,addTaskSetting,editTask,disableTimer,addTimeSetting;
    private Button restore,save;



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
        SharedPreferences settings = getSharedPreferences("Settings",MODE_PRIVATE);
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
        restore = findViewById(R.id.btnRestoreDefault);
        save = findViewById(R.id.btnSaveSettings);

        loadSettings();


        restore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRestoreSettings();
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
    private void mRestoreSettings(){
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
        if(!finishedNoti.isChecked())
            sound.setEnabled(false);

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
