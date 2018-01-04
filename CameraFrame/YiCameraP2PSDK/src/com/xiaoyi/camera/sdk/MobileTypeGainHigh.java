package com.xiaoyi.camera.sdk;

import com.xiaoyi.log.AntsLog;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by fengwu on 2016/5/13.
 */
public class MobileTypeGainHigh extends MobileType{

    public static final String TAG = "MobileTypeGainHigh";

    private HashMap<String, MobileTypeStruct> mMobileList = null;
    private static MobileTypeGainHigh sInstance = null;

    public static MobileTypeGainHigh getInstance()
    {
        if(sInstance == null)
            synchronized (MobileTypeGainHigh.class) {
                {
                    sInstance = new MobileTypeGainHigh();
                }
            }
        return sInstance;
    }
    private MobileTypeGainHigh()
    {

        mMobileList = new HashMap<>();
        Iterator iter = mMobileListDelay.entrySet().iterator();

        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String) entry.getKey();
            int val = (int) entry.getValue();
            add(key,val);
            AntsLog.d(TAG,"key="+key+" val="+val);
        }
    }

    private void add (String nm, int delay)
    {
        add(nm, delay, 5, 10, 20, 4);
    }

    private void add(String nm, int delay, int agcPlayGain, int agcPlayLimit, int agcRecGain, int agcRecLimit)
    {
        mMobileList.put(nm, new MobileTypeStruct(delay,  agcPlayGain,  agcPlayLimit,  agcRecGain,  agcRecLimit));
    }

    public boolean isHasMobileType(String nm){
        return mMobileList.containsKey(nm);
    }


    private MobileTypeStruct getMobileAudioParam(String nm) {
        MobileTypeStruct mobileTypeStruct;
        mobileTypeStruct = mMobileList.get(nm);
        if (mobileTypeStruct != null) {
            return mobileTypeStruct;
        }

        for (String key : mMobileList.keySet()) {
            if (nm.startsWith(key)) {
                return mMobileList.get(key);
            }
        }
        return mMobileList.get("defaults");
    }

    public MobileTypeStruct getMyMobileTypeStruct(String nm){
        if(myMobileTypeStruct == null){
            synchronized (this) {
                myMobileTypeStruct = getMobileAudioParam(nm);
            }
        }
        return myMobileTypeStruct;
    }

    private void addBydb(String nm, int aecDelay, int db)
    {
        int mobileVolType;
        if(db >= 80) mobileVolType = MOBILE_VOL_HIG;
        else if(db >= 75) mobileVolType = MOBILE_VOL_MID;
        else mobileVolType = MOBILE_VOL_LOW;
        putStructByType(nm, aecDelay, mobileVolType);
    }

    private void putStructByType(String nm, int aecDelay, int mobileVolType)
    {
        MobileTypeStruct myStruct;
        switch(mobileVolType)
        {
            case MOBILE_VOL_HIG:
                myStruct = new MobileTypeStruct(aecDelay, 9,3,9,1);
                break;
            case MOBILE_VOL_MID:
                myStruct = new MobileTypeStruct(aecDelay, 9,3,12,3);
                break;
            case MOBILE_VOL_LOW:
                myStruct = new MobileTypeStruct(aecDelay, 9,10,15,5);
                break;
            default:
                AntsLog.d(TAG, "error! unknow mobile vol type:"+mobileVolType);
                myStruct = new MobileTypeStruct(aecDelay, 9,3,12,3);
        }
        mMobileList.put(nm, myStruct);
    }

}
