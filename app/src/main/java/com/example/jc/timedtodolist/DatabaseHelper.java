package com.example.jc.timedtodolist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String TABLE_NAME = "saved_lists";
    public static final String COL1 = "title";
    public static final String COL2 = "completed";
    public static final String COL3 = "failed";
    public DatabaseHelper (Context context){
        super(context, TABLE_NAME, null, 1);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createTable = "CREATE TABLE "+ TABLE_NAME + "( ID INTEGER PRIMARY KEY " +
                "AUTOINCREMENT, " + COL1 + " TEXT, " + COL2 + " BLOB, " + COL3 + " BLOB)";

        sqLiteDatabase.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
    public boolean addData(String title, byte[] completed, byte[] failed){
        SQLiteDatabase db = this.getWritableDatabase();
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
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

    }
    /**
     * Returns all data
     * @return
     */
    public Cursor getData2(){

        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

    }

}
