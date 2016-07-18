package com.project.dmf.android;

import android.content.Context;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by André Eduardo on 18/07/2016.
 */
public class DB_Helper extends SQLiteOpenHelper {

    private static String DB_NAME  = "dmfDB";
    private static int DB_VERSION = 1;

    private static String TABLE_FILA =
            "CREATE TABLE fila(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "dados TEXT" +
                    ");";


    public DB_Helper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_FILA);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int currentVersion) {

    }
}

