package com.example.spektrumv3;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class SqlImageDatabase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "dictionary.db";
    private static final String TABLE_IMAGE = "image";
    private static final String FIELD_URI = "uri";
    private static final int DATABASE_VERSION = 1;

    public SqlImageDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //    SqlImageDatabase(Context context) {
//        super(context, DATABASE_NAME, null, DATABASE_VERSION);
//    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " +
                TABLE_IMAGE + "(_id integer PRIMARY KEY," + FIELD_URI + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //nothing to do here
    }

    public long insertImage(String uri) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FIELD_URI, uri);
        return db.insert(TABLE_IMAGE, null, values);
    }

    public Cursor getImageList() {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT _id, " + FIELD_URI + " FROM " + TABLE_IMAGE;
        return db.rawQuery(query, null);
    }

    public void deleteAll() {
        SQLiteDatabase db = getReadableDatabase();
        String query = "DELETE FROM " + TABLE_IMAGE;
        db.delete(TABLE_IMAGE, null, null);
    }
}
