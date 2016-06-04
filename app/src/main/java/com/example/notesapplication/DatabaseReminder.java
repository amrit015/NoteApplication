package com.example.notesapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;

/**
 * Extends SQLITOpenHelper to store the date and time of the reminder
 */
public class DatabaseReminder extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    public static String DATABASE_NAME = "SavedReminderDatabase", REMINDER_TABLE = "reminderSavedTable",
            KEY_ID = "_id", UNIQUE_ID = "unique_id", reminder_date = "date", reminder_time = "time";
    SQLiteDatabase db;
    Context c;

    public DatabaseReminder(File filename, Context context) {
        super(context, filename + ("/") + DATABASE_NAME, null, DATABASE_VERSION);
        c = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL("CREATE TABLE IF NOT EXISTS reminderSavedTable("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "time VARCHAR," +
                "date VARCHAR," + "unique_id TEXT" + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS " + REMINDER_TABLE);
        onCreate(db);
    }

    //adding time to database
    public void addToReminder(String unique_id, String date, String time) {
        // TODO Auto-generated method stub
        db = this.getReadableDatabase();
        ContentValues contentvalues = new ContentValues();
        contentvalues.put("unique_id", unique_id);
        contentvalues.put("date", date);
        contentvalues.put("time", time);
        db.insert(REMINDER_TABLE, null, contentvalues);
        db.close();
    }

    //getting time of the reminder previously saved
    public String getReminderTime(String unique_id) {
        String time = "";
        db = this.getWritableDatabase();
        // getting time when unique_id gets matched
        Cursor cursor = db.query(REMINDER_TABLE, new String[]{reminder_time}, "unique_id= ?", new String[]{unique_id}, null, null, null, null);
        if (cursor.getCount() != 0) {
            if (cursor.moveToFirst()) {
                do {
                    time = cursor.getString(cursor
                            .getColumnIndex("time"));
                } while (cursor.moveToNext());
            }
        }
        db.close();
        cursor.close();
        return time;
    }

    //getting date of the reminder previously saved
    public String getReminderDate(String unique_id) {
        String date = "";
        db = this.getWritableDatabase();
        // getting time when unique_id gets matched
        Cursor cursor = db.query(REMINDER_TABLE, new String[]{reminder_date}, "unique_id= ?", new String[]{unique_id}, null, null, null, null);
        if (cursor.getCount() != 0) {
            if (cursor.moveToFirst()) {
                do {
                    date = cursor.getString(cursor
                            .getColumnIndex("date"));
                } while (cursor.moveToNext());
            }
        }
        db.close();
        cursor.close();
        return date;
    }

    //updating reminder to current date
    public void UpdateReminderDate(String date, String unique_id) {
        // TODO Auto-generated method stub
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("date", date);
        db.update(REMINDER_TABLE, cv, "unique_id= ?", new String[]{unique_id});
        db.close();
    }

    //updating reminder to current time
    public void UpdateReminderTime(String time, String unique_id) {
        // TODO Auto-generated method stub
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("time", time);
        db.update(REMINDER_TABLE, cv, "unique_id= ?", new String[]{unique_id});
        db.close();
    }

    //removing from reminderSavedTable if necessary
    public void removeReminder(String unique_id) {
        // TODO Auto-generated method stub
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete("reminderSavedTable", "unique_id = ?", new String[]{unique_id});
            db.close();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }
}