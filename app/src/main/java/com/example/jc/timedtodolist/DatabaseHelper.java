package com.example.jc.timedtodolist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static com.example.jc.timedtodolist.PreviousLists.TAG;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String TABLE_NAME = "saved_lists";
    public static final String COL1 = "title";
    public static final String COL2 = "completed";
    public static final String COL3 = "failed";
    public DatabaseHelper (Context context){
        super(context, TABLE_NAME, null, 1);
        Log.d(TAG, "DatabaseHelper: NEW DB HELPER");
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createTable = "CREATE TABLE "+ TABLE_NAME + "( ID INTEGER PRIMARY KEY " +
                "AUTOINCREMENT, " + COL1 + " TEXT, " + COL2 + " BLOB, " + COL3 + " BLOB)";
        Log.d(TAG, "onCreate: table created");
        sqLiteDatabase.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
    public boolean addData(String title, byte[] completed, byte[] failed){
        SQLiteDatabase db = this.getWritableDatabase();
       // db.rawQuery("CREATE TABLE " + TABLE_NAME, null);
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL1, title);
        contentValues.put(COL2, completed);
        contentValues.put(COL3, failed);
        long result = db.insert(TABLE_NAME,null, contentValues);
        return result != -1;
    }

    /**
     * Returns all data
     * @return
     */
    public Cursor getData(){

        SQLiteDatabase db = this.getWritableDatabase();


        //db.execSQL("CREATE TABLE IF NOT EXISTS "+ TABLE_NAME);
        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY "+ COL1 + " DESC", null);

    }
    /**
     * Returns all data
     * @return
     */
    public void deleteDB(){

        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM  " + TABLE_NAME);

    }
    public void deleteItem(String title){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM  " + TABLE_NAME + " WHERE " + COL1 + " = '" + title +"'");
    }

}
