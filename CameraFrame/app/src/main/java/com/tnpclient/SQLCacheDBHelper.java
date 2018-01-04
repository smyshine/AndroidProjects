package com.tnpclient;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by starvedia on 2017/2/11.
 */

public class SQLCacheDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "cache.db";
    public static final int VERSION = 1;
    private static final String TAG = "SQLCacheDBHelper";
    private static SQLiteDatabase database;

    public SQLCacheDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQLCache.CREATE_VIDEO_TABLE);
        sqLiteDatabase.execSQL(SQLCache.CREATE_AUDIO_TABLE);
    }

    public static SQLiteDatabase getDatabase(Context context) {
        if (database == null || !database.isOpen()) {
            database = new SQLCacheDBHelper(context, DATABASE_NAME, null, VERSION).getWritableDatabase();
            Log.i(TAG,"Create");
        }

        database.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" + SQLCache.TABLE_AUDIO + "'");
        database.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" + SQLCache.TABLE_VIDEO + "'");

        return database;
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        Log.i(TAG,"onUpgrade");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SQLCache.TABLE_VIDEO);
        onCreate(sqLiteDatabase);
    }

}
