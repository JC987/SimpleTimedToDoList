package com.example.jc.timedtodolist;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

public class DetailedPreviousList extends AppCompatActivity {
    public static final String TAG = "DetailedPreviousList";
    ListView completedListView, failedListView;
    ArrayList<String> completed, failed;
    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        sharedPreferences = getSharedPreferences("save and load", MODE_PRIVATE);
        setTheme(sharedPreferences.getInt("theme", R.style.AppTheme));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_previous_list);

        byte[] c = getIntent().getByteArrayExtra("completed");
        byte[] f = getIntent().getByteArrayExtra("failed");

        completedListView = findViewById(R.id.completedListView);
        failedListView = findViewById(R.id.failedListView);

        completed = readByteArr(c);
        failed = readByteArr(f);

        populateListViews();


    }

    private void populateListViews(){
        Log.d(TAG, "populateListViews: fail " + failed);
        Log.d(TAG, "populateListViews: comp " + completed);
        ListAdapter completedAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, completed);
        completedListView.setAdapter(completedAdapter);

        ListAdapter failedAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, failed);
        failedListView.setAdapter(failedAdapter);

    }



    public ArrayList<String> readByteArr(byte[] data) {
        try {


            ByteArrayInputStream baip = new ByteArrayInputStream(data);
            ObjectInputStream ois = new ObjectInputStream(baip);
            ArrayList dataobj = (ArrayList) ois.readObject();
            Log.d(TAG, "read: dO " + dataobj);
            for(int i = 0; i < dataobj.size(); i++){

                Log.d(TAG, "read: daO is " + dataobj.get(i));
            }
            return dataobj ;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}
