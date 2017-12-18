package me.webhop.sawanet.widgetofprovidence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static android.content.ContentValues.TAG;

/**
 * Created by sawa on 12/18/17.
 */

public class DBhelper extends SQLiteOpenHelper{
    public DBhelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // init table
//        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS widget");
        db.execSQL("CREATE TABLE widget" +
                "(_id INTEGER PRIMARY KEY, " +
                "path TEXT)");
        Log.d(TAG, "SQLite Database table created.");
    }

    public void dropAllRecords(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM widget");
    }

    public void addId(final int id){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO widget (_id) VALUES (" + id + ");");
    }

    public void addPath(final int id, String uri){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE widget SET path = \"" + uri + "\" WHERE _id = " + id);
    }

    public void removeWidget(final int id){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM widget " +
                "WHERE _id=" + id + ";");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
