package com.github.caicosantos1998.voicenote;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseVoiceNote extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "VoiceNote.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "notes";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TEXT = "text_content";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME +
                    " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TEXT + " TEXT NOT NULL);";

    public DatabaseVoiceNote(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertNote(String text) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TEXT, text);
        long result = db.insert(TABLE_NAME, null, values);
        db.close();

        return result!=-1;
    }

    public String getAllNotes() {
        SQLiteDatabase db = this.getReadableDatabase();
        StringBuilder stringBuilder = new StringBuilder();
        Cursor cursor = db.query(TABLE_NAME, new String[]{COLUMN_TEXT},
                null, null, null, null, COLUMN_ID + " ASC");

        if(cursor!=null){
            while(cursor.moveToNext()) {
                String note = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TEXT));
                if(stringBuilder.length()>0){
                    stringBuilder.append("\n");
                }
                stringBuilder.append(note);
            }
            cursor.close();
        }
        db.close();
        return stringBuilder.toString();
    }
}
