package com.tnpclient;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.h264player.CacheFrame;
import com.h264player.IDataCache;


/**
 * Created by starvedia on 2017/2/12.
 */

public class SQLCache implements IDataCache {

    public static final String TABLE_VIDEO = "Video";
    public static final String TABLE_AUDIO = "Audio";
    public static final String KEY_ID = "_id";
    public static final String FRAME_DATA = "FrameData";
    public static final String IS_KEYFRAME = "isKeyFrame";
    public static final String PTS = "Pts";
    public static final String CREATE_VIDEO_TABLE =
            "CREATE TABLE " + TABLE_VIDEO + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    FRAME_DATA + " Blob NOT NULL, " +
                    IS_KEYFRAME + " INTEGER NOT NULL, " +
                    PTS +  " INTEGER)";
    public static final String CREATE_AUDIO_TABLE =
            "CREATE TABLE " + TABLE_AUDIO + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    FRAME_DATA + " Blob NOT NULL, " +
                    IS_KEYFRAME + " INTEGER NOT NULL, " +
                    PTS +  " INTEGER)";
    private static final String TAG = "SQLCache";

    enum FRAME_TYPE{
        AUDIO, VIDEO
    }
    private SQLiteDatabase db;
    private int playVideoIndex = 1;
    private int playAudioIndex = 1;
    public SQLCache(Context context){
        db = SQLCacheDBHelper.getDatabase(context);
        clear();
    }

    @Override
    public int getCacheCount() {
        Cursor videoCursor = getTableCursor(FRAME_TYPE.VIDEO);
        Cursor audioCursor = getTableCursor(FRAME_TYPE.AUDIO);
        return videoCursor.getCount()+audioCursor.getCount();
    }

    @Override
    public CacheFrame popVideoFrame() {
        CacheFrame result = null;
        Cursor cursor = db.query(getTableName(FRAME_TYPE.VIDEO), new String[]{"*"}, "_id="+playVideoIndex, null, null, null, null);
        if(cursor.moveToNext()){
            result = new CacheFrame(cursor.getBlob(1), cursor.getInt(3), cursor.getInt(2));
            ++playVideoIndex;
        }
        cursor.close();
        return result;
    }

    @Override
    public CacheFrame popAudioFrame() {
        CacheFrame result = null;
        Cursor cursor = db.query(getTableName(FRAME_TYPE.AUDIO), new String[]{"*"}, "_id="+playAudioIndex, null, null, null, null);
        if(cursor.moveToNext()){
            result = new CacheFrame(cursor.getBlob(1), cursor.getInt(3), cursor.getInt(2));
            ++playAudioIndex;
        }
        cursor.close();
        return result;
    }

    @Override
    public boolean pushVideoFrame(CacheFrame videoFrame) {
        return putFrameToDB(FRAME_TYPE.VIDEO, videoFrame);
    }

    @Override
    public boolean pushAudioFrame(CacheFrame audioFrame) {
        return putFrameToDB(FRAME_TYPE.AUDIO, audioFrame);
    }

    private boolean putFrameToDB(FRAME_TYPE type, CacheFrame inputFrame){
        ContentValues cv = new ContentValues();
        cv.put(FRAME_DATA, inputFrame.data);
        cv.put(IS_KEYFRAME, inputFrame.isKeyFrame);
        cv.put(PTS, inputFrame.timestampMS);
        long id = db.insert(getTableName(type), null, cv);
        return (id > 0) ? true : false;
    }

    @NonNull
    private String getTableName(FRAME_TYPE type) {
        return (type== FRAME_TYPE.VIDEO)? TABLE_VIDEO : TABLE_AUDIO;
    }

    private Cursor getTableCursor(FRAME_TYPE type){
        return db.query(getTableName(type), null, null, null, null, null, null);
    }

    @Override
    public void clear() {
        db.delete(TABLE_VIDEO, null, null);
        db.delete(TABLE_AUDIO, null, null);
    }

    @Override
    public void seekTo(float progress) {
    }
}
