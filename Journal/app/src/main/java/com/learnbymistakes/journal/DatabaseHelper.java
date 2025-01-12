package com.learnbymistakes.journal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Journal.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "journal_entries",
            COLUMN_ID = "id",
            COLUMN_DATE = "date",
            COLUMN_TEXT = "text",
            COLUMN_JOURNAL_IMAGE_PATH = "journalImagePath",
            COLUMN_TASK_IMAGE_PATH = "taskImagePath",
            COLUMN_PLANNER_IMAGE_PATH = "plannerImagePath";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DATE + " TEXT UNIQUE, " +
                COLUMN_TEXT + " TEXT, " +
                COLUMN_JOURNAL_IMAGE_PATH + " TEXT, " +
                COLUMN_TASK_IMAGE_PATH + " TEXT, " +
                COLUMN_PLANNER_IMAGE_PATH + " TEXT)";
        sqLiteDatabase.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public boolean saveEntry(String date, String text, String journalImagePath,
                             String taskImagePath, String plannerImagePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_TEXT, text);
        values.put(COLUMN_JOURNAL_IMAGE_PATH, journalImagePath);
        values.put(COLUMN_TASK_IMAGE_PATH, taskImagePath);
        values.put(COLUMN_PLANNER_IMAGE_PATH, plannerImagePath);

        long result = db.replace(TABLE_NAME, null, values);
        return result != -1;
    }

    public Cursor getEntryByDate(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_NAME, null, COLUMN_DATE + "=?", new String[]{date}, null, null, null);
    }
}
