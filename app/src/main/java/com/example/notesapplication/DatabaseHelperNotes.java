package com.example.notesapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.util.ArrayList;

/*
    Extends SQLiteOpenHelper to save the id, title and notes on the local database in the phone storage
*/
public class DatabaseHelperNotes extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    public static String DATABASE_NAME = "notesDatabase", NOTES_TABLE = "notesTable",
            KEY_ID = "_id", NOTES_TITLE = "title",
            NOTES_CONTENT = "notes";
    SQLiteDatabase db;
    Context c;
    private ArrayList<NotesModule> listNotes = new ArrayList<NotesModule>();


    public DatabaseHelperNotes(File filename, Context context) {
        super(context, filename + ("/") + DATABASE_NAME, null, DATABASE_VERSION);
        c = context;
    }


    //creating table if it doesnt exist previously
    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL("CREATE TABLE IF NOT EXISTS notesTable("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "title TEXT, " + "notes VARCHAR)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldversion, int newversion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS " + NOTES_TABLE);
        onCreate(db);
    }


    // adding title and notes to the table through Object
    public void addNotes(NotesModule nm) {
        // TODO Auto-generated method stub
        db = this.getReadableDatabase();
        ContentValues contentvalues = new ContentValues();
        contentvalues.put("title", nm.title);
        contentvalues.put("notes", nm.notes);
        db.insert(NOTES_TABLE, null, contentvalues);
        db.close();
    }

    //getting notes and title to display
    public ArrayList<NotesModule> getNotes() {
        // TODO Auto-generated method stub
        listNotes.clear();
        db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("Select * from notesTable", null);
        if (cursor.getCount() != 0) {
            if (cursor.moveToFirst()) {
                do {
                    NotesModule item = new NotesModule();
                    item.title = cursor.getString(cursor
                            .getColumnIndex("title"));
                    item.notes = cursor.getString(cursor
                            .getColumnIndex("notes"));
                    listNotes.add(item);
                } while (cursor.moveToNext());
            }
        }
        cursor.close();
        db.close();
        return listNotes;
    }

    // updating title and notes on the database
    public void updateNotes(NotesModule nm, String title) {
        // TODO Auto-generated method stub
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv1 = new ContentValues();
        cv1.put("title", nm.title);
        cv1.put("notes", nm.notes);
        db.update(NOTES_TABLE, cv1, "title = ?", new String[]{title});
        db.close();
    }

    //removing thw rows of the title
    public void removeNotes(String title) {
        // TODO Auto-generated method stub
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete("notesTable", "title = ?", new String[]{title});
            db.close();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

}