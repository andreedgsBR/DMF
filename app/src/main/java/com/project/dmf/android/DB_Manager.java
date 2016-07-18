package com.project.dmf.android;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by André Eduardo on 18/07/2016.
 */
public class DB_Manager {
    private static DB_Helper dbHelper = null;

    public DB_Manager(Context context){
        if(dbHelper==null){
            dbHelper = new DB_Helper(context);
        }
    }

    public void addItens(String dados){
        String sql = "INSERT INTO fila (dados) VALUES ("+dados+")";
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        db.execSQL(sql);
    }
}
