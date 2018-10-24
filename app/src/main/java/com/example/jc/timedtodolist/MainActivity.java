package com.example.jc.timedtodolist;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
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
                createNewTask("",null);
            }
        });

        //chronometer.setCountDown(true);
        textViewTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.set_time_dialog, null);
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
                dialog.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        DecimalFormat decimalFormat = new DecimalFormat("00");
                        String time = decimalFormat.format(hour.getValue())+":"+
                                decimalFormat.format(min.getValue())+":"+decimalFormat.format(sec.getValue());

                        textViewTime.setText(time);
                    }
                });
                dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                dialog.show();
            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textViewTime.setText("00:00:00");
                textViewTime.setEnabled(true);
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
                Toast.makeText(MainActivity.this,"Resetting",Toast.LENGTH_SHORT ).show();
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // Log.d(TAG, "onClick: "+ editTextArrayList.size());
                confirmTaskList();

                if(editTextArrayList.size()>0 && !textViewTime.getText().toString().equals("00:00:00")) {
                    textViewTime.setEnabled(false);
                    addTask.setEnabled(false);
                    confirm.setEnabled(false);

                    countDown(convertTimeToMilli(textViewTime.getText().toString()));
                 //   Log.d(TAG, "onClick: if:  "+ editTextArrayList.size());
                }
                else {
                    Toast.makeText(MainActivity.this, "Add task to List and set time", Toast.LENGTH_SHORT).show();
                    totalTask = 1;
                }
            }
        });


    }

    public void countDown(long l){

        countDownTimer = new CountDownTimer(l, 1000) {

            public void onTick(long millisUntilFinished) {
                long hr = millisUntilFinished / (60 * 60 * 1000);
                long min = (millisUntilFinished - (hr * 60 * 60 * 1000)) / 60000;
                long sec = (millisUntilFinished - (hr * 60 * 60 * 1000) - (min * 60000)) / 1000;


                NumberFormat numberFormat = new DecimalFormat("00");


                textViewTime.setText(numberFormat.format(hr) + " : " + numberFormat.format(min) + " : " + numberFormat.format(sec));

            }

            public void onFinish() {
                textViewTime.setText("Times Up!");
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

                Log.d(TAG, "confirmTaskList: "+ editTextArrayList.size());
                i--;
                Log.d(TAG, "onClick: asdf");
            }


        }
    }

    public void createNewTask(String text, Boolean check){


        TableRow tableRow = new TableRow(MainActivity.this);
        TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT,TableLayout.LayoutParams.WRAP_CONTENT,5.0f);


        EditText editText = new EditText(MainActivity.this);
        TableRow.LayoutParams editTextParams = new TableRow.LayoutParams(0,TableLayout.LayoutParams.WRAP_CONTENT,4.0f);


     //   tableRow.setWeightSum(5.0f);
        CheckBox checkBox = new CheckBox(MainActivity.this);
        TableRow.LayoutParams checkBoxParams= new TableRow.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT,TableLayout.LayoutParams.WRAP_CONTENT,0.5f);
        //checkBox.setGravity(Gravity.CENTER_HORIZONTAL);
        checkBoxParams.setMarginStart(30);

        TextView textView1 = new TextView(this);
        TableRow.LayoutParams textViewParams= new TableRow.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT,TableLayout.LayoutParams.WRAP_CONTENT,0.5f);

        editText.setLayoutParams(editTextParams);
        checkBox.setLayoutParams(checkBoxParams);
        textView1.setLayoutParams(textViewParams);

        tableRow.setLayoutParams(tableRowParams);

        textView1.setText(totalTask + ") ");
        textView1.setTextSize(18);
        textView1.setTextColor(Color.parseColor("#000000"));
        textView1.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        editText.setHint("Leave blank if not needed!");


        totalTask++;

        textView1.setId(idCounter);
        editText.setId(idCounter+1);
        checkBox.setId(idCounter+2);

        idCounter += 3;

        tableRow.setPadding(4,8,4,32);

        editText.requestFocus();
        editText.setTextSize(18);


        editText.setText(text);

        if(check!=null) {
            checkBox.setEnabled(true);
            checkBox.setChecked(check);
            editText.setKeyListener(null);
        }
        else
            checkBox.setEnabled(false);

        textViewArrayList.add(textView1);
        checkBoxArrayList.add(checkBox);
        editTextArrayList.add(editText);
        tableRowArrayList.add(tableRow);

        tableRow.addView(textView1);
        tableRow.addView(editText);
        tableRow.addView(checkBox);
        tableLayout.addView(tableRow);


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
                createNewTask(sharedPreferences.getString("editText " + i, ""), sharedPreferences.getBoolean("checkBox " + i, false));
            }
          //  confirmTaskList();


            textViewTime.setText(sharedPreferences.getString("time", "00:00:00"));

            if (sharedPreferences.getBoolean("started", false)) {
                addTask.setEnabled(false);
                confirm.setEnabled(false);
                textViewTime.setEnabled(false);
                countDown( (convertTimeToMilli(sharedPreferences.getString("time", "00:00:00")) - diff) );

            }

        }
    }
}
