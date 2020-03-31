package com.example.jc.timedtodolist;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
        completedListView = findViewById(R.id.listViewCompleted);
        failedListView = findViewById(R.id.listViewFailed);
        completed = readByteArr(c);
        failed = readByteArr(f);

        populateListViews();


    }

    private void populateListViews(){
        ListAdapter completedAdapter = new ArrayAdapter<>(this, R.layout.previous_list_item, completed);
        completedListView.setAdapter(completedAdapter);

        ListAdapter failedAdapter = new ArrayAdapter<>(this, R.layout.previous_list_item, failed);
        failedListView.setAdapter(failedAdapter);

    }


    public ArrayList<String> readByteArr(byte[] data) {
        try {


            ByteArrayInputStream baip = new ByteArrayInputStream(data);
            ObjectInputStream ois = new ObjectInputStream(baip);
            ArrayList dataobj = (ArrayList) ois.readObject();

            return dataobj ;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}
