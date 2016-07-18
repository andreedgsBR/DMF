package com.project.dmf.android;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.lang.reflect.Array;
import java.util.ArrayList;

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

    public ArrayList<String> getAllItens(){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT *FROM fila";
        Cursor cursor = db.rawQuery(sql, null);
        ArrayList<String> dados = null;

        if(cursor != null && cursor.moveToFirst()){
            dados = new ArrayList<String>();

            do {
                dados.add(cursor.getString(1));
            } while(cursor.moveToNext());
        }

        return dados;
    }
}
