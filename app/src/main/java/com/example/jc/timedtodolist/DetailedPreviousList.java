package com.example.jc.timedtodolist;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TableLayout;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

public class DetailedPreviousList extends AppCompatActivity {
    public static final String TAG = "DetailedPreviousList";
    ListView completedListView, failedListView;
    ArrayList<String> completed, failed;
    SharedPreferences sharedPreferences;
    TableLayout tableLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        sharedPreferences = getSharedPreferences("save and load", MODE_PRIVATE);
        setTheme(sharedPreferences.getInt("theme", R.style.AppTheme));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_previous_list);

        byte[] c = getIntent().getByteArrayExtra("completed");
        byte[] f = getIntent().getByteArrayExtra("failed");
        Log.d(TAG, "onCreate: " + c + "   " + f);
        completedListView = findViewById(R.id.listViewCompleted);
        failedListView = findViewById(R.id.listViewFailed);
        Log.d(TAG, "onCreate: " + c);
        Log.d(TAG, "onCreate: " + f);
        completed = readByteArr(c);
        failed = readByteArr(f);

        populateListViews();


    }

    private void populateListViews(){
        Log.d(TAG, "populateListViews: fail " + failed);
        Log.d(TAG, "populateListViews: comp " + completed);
        ListAdapter completedAdapter = new ArrayAdapter<>(this, R.layout.previous_list_item, completed);
        completedListView.setAdapter(completedAdapter);

        ListAdapter failedAdapter = new ArrayAdapter<>(this, R.layout.previous_list_item, failed);
        failedListView.setAdapter(failedAdapter);

    }


/*
    public ArrayList<Task> readByteArr(byte[] data) {
        try {

            Log.d(TAG, "readByteArr: " + data);
            ByteArrayInputStream baip = new ByteArrayInputStream(data);

            Log.d(TAG, "readByteArr: " + baip);
            ObjectInputStream ois = new ObjectInputStream(baip);

            Log.d(TAG, "readByteArr: " + (ois.readObject()));
            ArrayList<Task> dataobj = ((ArrayList) ois.readObject());
            Log.d(TAG, "read: dO " + dataobj);
            for(int i = 0; i < dataobj.size(); i++){

                Log.d(TAG, "read: daO is " + dataobj.get(i).getTaskDesc().toString());
            }
            return dataobj ;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
*/

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
