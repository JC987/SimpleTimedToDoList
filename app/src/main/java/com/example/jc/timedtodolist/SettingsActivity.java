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


    private CheckBox remainingNoti,finishedNoti,soundNoti,dailyNoti,darkTheme,afterConfirm,noTimer;
    private Button restore,save;
   // private Switch theme;


    @Override
    public void onBackPressed() {
        super.onBackPressed();

        mSaveSettings();
        finish();
        Intent refresh = new Intent(SettingsActivity.this, MainActivity.class);
        refresh.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(refresh);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = getSharedPreferences("Settings",MODE_PRIVATE);

        setTheme(sharedPreferences.getInt("themeInt",R.style.AppTheme));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        //getActionBar().setTitle("Settings");
        setTitle("Settings");
        //
     //   Toast.makeText(this,"theme " + theme.isChecked(),Toast.LENGTH_SHORT).show();
        remainingNoti=findViewById(R.id.settingCheckBox1);
        finishedNoti = findViewById(R.id.settingCheckBox2);
        soundNoti = findViewById(R.id.settingCheckBox3);
     //   dailyNoti = findViewById(R.id.settingCheckBox4);
     //   darkTheme = findViewById(R.id.settingCheckBox4);
//        theme = findViewById(R.id.themeSwitch);
        afterConfirm = findViewById(R.id.settingCheckBox5);
        noTimer = findViewById(R.id.settingCheckBox6);
        //negTimer = findViewById(R.id.settingCheckBox7);

        restore = findViewById(R.id.btnRestoreDefault);
        save = findViewById(R.id.btnSaveSettings);

        mLoadSettings();


        restore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRestoreSettings();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSaveSettings();
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
                    soundNoti.setChecked(false);
                    soundNoti.setEnabled(false);
                }
                else {
                    //soundNoti.setChecked(true);
                    soundNoti.setEnabled(true);
                }
            }
        });
    }
    private void mRestoreSettings(){
        remainingNoti.setChecked(true);
        finishedNoti.setChecked(true);
        soundNoti.setChecked(true);
        soundNoti.setEnabled(true);
        //dailyNoti.setChecked(true);
       // darkTheme.setEnabled(false);
        afterConfirm.setChecked(false);
        noTimer.setChecked(false);
//        theme.setChecked(true);
        //negTimer.setChecked(false);

        Toast.makeText(SettingsActivity.this, "Default Settings Restored!",
                Toast.LENGTH_SHORT).show();

        mSaveSettings();
    }
    private void mLoadSettings(){

        SharedPreferences sharedPreferences = getSharedPreferences("Settings",MODE_PRIVATE);

        remainingNoti.setChecked(sharedPreferences.getBoolean("remaining",true));
        finishedNoti.setChecked(sharedPreferences.getBoolean("finished",true));
        soundNoti.setChecked(sharedPreferences.getBoolean("sound",true));
        //dailyNoti.setChecked(sharedPreferences.getBoolean("daily",true));
        //darkTheme.setChecked(sharedPreferences.getBoolean("darkTheme",false));
        afterConfirm.setChecked(sharedPreferences.getBoolean("afterConfirm",false));
        noTimer.setChecked(sharedPreferences.getBoolean("noTimer",false));
//        theme.setChecked(sharedPreferences.getBoolean("Theme",true));
        //negTimer.setChecked(sharedPreferences.getBoolean("negTimer",false));
        if(!finishedNoti.isChecked())
            soundNoti.setEnabled(false);
//        Toast.makeText(this,"theme " + theme.isChecked(),Toast.LENGTH_SHORT).show();

    }
    private void mSaveSettings(){
        SharedPreferences sharedPreferences = getSharedPreferences("Settings",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean("remaining",remainingNoti.isChecked());
        editor.putBoolean("finished",finishedNoti.isChecked());
        editor.putBoolean("sound",soundNoti.isChecked() );
     //   editor.putBoolean("Theme",theme.isChecked());
        //editor.putBoolean("daily",dailyNoti.isChecked());
    //    editor.putBoolean("darkTheme",darkTheme.isChecked());
        editor.putBoolean("afterConfirm",afterConfirm.isChecked());
        editor.putBoolean("noTimer",noTimer.isChecked());
        //editor.putBoolean("negTimer",negTimer.isChecked());
        editor.apply();

        Toast.makeText(SettingsActivity.this, "Settings Saved!", Toast.LENGTH_SHORT).show();
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
