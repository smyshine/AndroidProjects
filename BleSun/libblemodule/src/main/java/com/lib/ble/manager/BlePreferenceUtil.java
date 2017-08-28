package com.lib.ble.manager;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by SMY on 2017/1/17.
 */
public class BlePreferenceUtil {
    private static BlePreferenceUtil mInstance;
    private SharedPreferences mPref;

    private BlePreferenceUtil() {

    }

    public synchronized static BlePreferenceUtil getInstance() {
        if (null == mInstance) {
            mInstance = new BlePreferenceUtil();
        }
        return mInstance;
    }

    public void init(Context context) {
        if (mPref == null) {
            mPref = context.getApplicationContext().getSharedPreferences("profile", Context.MODE_PRIVATE);
        }
    }

    public void putString(String key, String value) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public String getString(String key) {
        return getString(key, "");
    }

    public String getString(String key, String defaultValue) {
        return mPref.getString(key, defaultValue);
    }

    public boolean contains(String key) {
        return mPref.contains(key);
    }

    public void remove(String key) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.remove(key);
        editor.commit();
    }
}
