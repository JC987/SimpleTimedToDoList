package com.example.jc.timedtodolist;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

public class PreviousLists extends AppCompatActivity {
    public static final String TAG = "PreviousLists";
    DatabaseHelper databaseHelper;
    ListView listView;
    Cursor data;
    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPreferences = getSharedPreferences("save and load", MODE_PRIVATE);
        setTheme(sharedPreferences.getInt("theme", R.style.AppTheme));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_previous_lists);

        listView = findViewById(R.id.listView);
        databaseHelper = new DatabaseHelper(this);

        data = databaseHelper.getData();
        populateListView();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "onItemSelected: pressed pos " + i);
                data.moveToPosition(i);
                byte[] completed = data.getBlob(2);
                byte[] failed = data.getBlob(3);
                //goto detailed view...
                Intent intent = new Intent(getApplicationContext(), DetailedPreviousList.class);
                Log.d(TAG, "onItemSelected: data" + data.getPosition());
                intent.putExtra("completed", completed);
                intent.putExtra("failed", failed);
                startActivity(intent);
            }
        });
    }

    private void populateListView(){


        ArrayList<String> list = new ArrayList<>();

        while(data.moveToNext()){
            list.add(data.getString(1));
            Log.d(TAG, "populateListView: data is "+ data.getString(1));
        }
        data.moveToFirst();
        ListAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter);
    }




}
