package com.example.jc.timedtodolist;

import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
                byte[] by = data.getBlob(2);
                ArrayList<String> completed = readByteArr(by);
                ArrayList<String> failed = readByteArr(by);
                //goto detailed view...
                Log.d(TAG, "onItemSelected: data" + data.getPosition() + "  "
                +  completed +"  " + data.getCount());
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
