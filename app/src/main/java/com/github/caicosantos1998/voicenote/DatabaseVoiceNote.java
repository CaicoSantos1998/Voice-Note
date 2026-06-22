package com.github.caicosantos1998.voicenote;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseVoiceNote extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "VoiceNote.db";
    private static final int DATABASE_VERSION = 3;

    public static final String TABLE_NAME = "notes";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_VALUE = "value";
    public static final String COLUMN_LOCATION = "location";
    public static final String COLUMN_NAME = "name";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " TEXT NOT NULL, " +
                    COLUMN_LOCATION + " TEXT, " +
                    COLUMN_VALUE + " INTEGER NOT NULL);";

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

    public boolean insertNote(String name, String location, int value) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name.toUpperCase().trim());
        values.put(COLUMN_LOCATION, location);
        values.put(COLUMN_VALUE, value);
        long result = db.insert(TABLE_NAME, null, values);
        db.close();

        return result!=-1;
    }

    public String searchPerson(String name) {
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            StringBuilder sb = new StringBuilder();
            String queryName = name.toLowerCase().trim();

            Cursor cursor = db.query(TABLE_NAME,
                    new String[]{COLUMN_NAME, COLUMN_LOCATION, COLUMN_VALUE},
                    COLUMN_NAME + " LIKE ?",
                    new String[]{"%" + queryName + "%"},
                    null, null, COLUMN_ID + " ASC");

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String namePerson = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
                    String location = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION));
                    int value = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_VALUE));

                    sb
                            .append(namePerson.toUpperCase())
                            .append(" da ")
                            .append(location)
                            .append(" deve ")
                            .append(value)
                            .append("\n");
                }
                cursor.close();
            }
            db.close();
            return sb.toString().trim();
        } catch (Exception e) {
            return "Error!" + e.getMessage();
        }
    }

    public String getTagName(String language) {
        SQLiteDatabase db = this.getReadableDatabase();
        StringBuilder sb = new StringBuilder();
        String query = "SELECT DISTINCT " + COLUMN_NAME + " FROM " +
                TABLE_NAME + " ORDER BY " + COLUMN_NAME + " ASC";
        Cursor cursor = db.rawQuery(query, null);
        if(cursor!=null){
            int nameIndex = cursor.getColumnIndex(COLUMN_NAME);
            if(nameIndex!=-1){
                while(cursor.moveToNext()) {
                    String name = cursor.getString(nameIndex);
                    sb.append("👤 ").append(name.toUpperCase()).append("\n");
                }
            }
            cursor.close();
        }
        db.close();
        String result = sb.toString().trim();
        if(result.isEmpty()){
            return "en".equals(language) ? "No users registered" : "Nenhum usuário cadastrado";
        } else {
            return result;
        }
    }

    public String getAllNotes() {
        SQLiteDatabase db = this.getReadableDatabase();
        StringBuilder sb = new StringBuilder();
        Cursor cursor = db.query(TABLE_NAME, new String[]{COLUMN_VALUE},
                null, null, null, null, COLUMN_ID + " ASC");

        if(cursor!=null){
            while(cursor.moveToNext()) {
                String note = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VALUE));
                if(sb.length()>0){
                    sb.append("\n");
                }
                sb.append(note);
            }
            cursor.close();
        }
        db.close();
        return sb.toString();
    }
}
