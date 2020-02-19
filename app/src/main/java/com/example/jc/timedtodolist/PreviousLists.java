package com.example.jc.timedtodolist;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
    ArrayAdapter adapter;
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
                Log.d(TAG, "onItemSelected: data" + completed + "   " + failed);
                //goto detailed view...
                Intent intent = new Intent(getApplicationContext(), DetailedPreviousList.class);
                Log.d(TAG, "onItemSelected: data" + data.getPosition());
                intent.putExtra("completed", completed);
                intent.putExtra("failed", failed);
                startActivity(intent);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(PreviousLists.this, "long pressed " + i, Toast.LENGTH_SHORT).show();
                createDeleteItemDialog(i);
                return true;
            }
        });
    }

    private void createDeleteItemDialog(final int pos) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(PreviousLists.this);
        dialog.setTitle("Delete this List?")
                .setMessage("Do you want to permanently delete this list? (Can not be undone!)")

                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ;
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // delete this single item
                        try {
                            data.moveToPosition(pos);
                            String title = data.getString(1);
                            databaseHelper.deleteItem(title);
                            adapter.remove(title);
                            adapter.notifyDataSetChanged();
                            data = databaseHelper.getData();
                        }
                        catch(Exception e){
                            Log.d(TAG, "onClick: error removing data");
                        }
                    }
                })
        .create();
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_previous_lists, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuDeleteDatabase:
                databaseHelper.deleteDB();
                emptyListView();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void emptyListView(){
        ArrayList<String> list = new ArrayList<>();
        data.moveToFirst();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter);

    }

    private void populateListView(){
        ArrayList<String> list = new ArrayList<>();
        
        while(data.moveToNext()){
            list.add(data.getString(1));
            Log.d(TAG, "populateListView: data is "+ data.getString(1));
        }
        data.moveToFirst();
        adapter = new ArrayAdapter<>(this, R.layout.previous_list_item, list);
        listView.setAdapter(adapter);

    }




}
