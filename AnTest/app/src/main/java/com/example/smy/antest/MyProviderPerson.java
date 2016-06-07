package com.example.smy.antest;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * Created by SMY on 2016/6/2.
 */
public class MyProviderPerson extends ContentProvider{
    private static final UriMatcher matcher;
    private DBHelper helper;
    private SQLiteDatabase db;

    private static final String AUTHORITY = "com.example.smy.antest";
    private static final int PERSON_ALL = 0;
    private static final int PERSON_ONE = 1;

    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.smy.person";
    private static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.smy.person";

    private static final Uri NOTIFY_URI = Uri.parse("content://" + AUTHORITY + "/persons");

    static {
        matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(AUTHORITY, "persons", PERSON_ALL);
        matcher.addURI(AUTHORITY, "persons/#", PERSON_ONE);
    }

    @Override
    public boolean onCreate()
    {
        helper = new DBHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri)
    {
        int match = matcher.match(uri);
        switch (match)
        {
            case PERSON_ALL:
                return CONTENT_TYPE;
            case PERSON_ONE:
                return CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        db = helper.getReadableDatabase();
        int match = matcher.match(uri);
        switch (match)
        {
            case PERSON_ALL:
                break;
            case PERSON_ONE:
                long _id = ContentUris.parseId(uri);
                selection = "_id = ?";
                selectionArgs = new String[]{String.valueOf(_id)};
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        return db.query("person", projection, selection, selectionArgs, null, null, sortOrder);
    }

    @Override
    public Uri insert(Uri uri, ContentValues values)
    {
        int match = matcher.match(uri);
        if (match != PERSON_ALL)
        {
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        db = helper.getWritableDatabase();
        if (values == null)
        {
            values = new ContentValues();
            values.put("name", "no name");
            values.put("age", "1");
            values.put("info", "no info");
        }
        long rowId = db.insert("person", null, values);
        if(rowId > 0)
        {
            notifyDataChanged();
            return ContentUris.withAppendedId(uri, rowId);
        }

        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs)
    {
        db = helper.getWritableDatabase();
        int match = matcher.match(uri);
        switch (match)
        {
            case PERSON_ALL:
                break;
            case PERSON_ONE:
                long _id = ContentUris.parseId(uri);
                selection = "_id = ?";
                selectionArgs = new String[]{String.valueOf(_id)};
                break;
        }
        int count = db.delete("person", selection, selectionArgs);
        if (count > 0)
        {
            notifyDataChanged();
        }
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues value, String selection, String[] selectionArgs)
    {
        db = helper.getWritableDatabase();
        int match = matcher.match(uri);
        switch (match)
        {
            case PERSON_ALL:
                break;
            case PERSON_ONE:
                long _id = ContentUris.parseId(uri);
                selection = "_id = ?";
                selectionArgs = new String[]{String.valueOf(_id)};
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        int count = db.update("person", value, selection, selectionArgs);
        if (count > 0)
        {
            notifyDataChanged();
        }
        return count;
    }

    private void notifyDataChanged()
    {
        getContext().getContentResolver().notifyChange(NOTIFY_URI, null);
    }
}
