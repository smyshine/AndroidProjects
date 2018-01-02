package com.xiaoyi.camera.sdk;


import java.util.HashMap;

/**
 * Created by fengwu on 2016/5/13.
 */
public class MobileType {

    public static final String TAG = "MobileType";

    public static final int MOBILE_VOL_LOW = 0;
    public static final int MOBILE_VOL_MID = 1;
    public static final int MOBILE_VOL_HIG = 2;


    public HashMap<String, Integer> mMobileListDelay = null;
    public MobileTypeStruct myMobileTypeStruct = null;

    public MobileType()
    {
        mMobileListDelay = new HashMap<>();
        mMobileListDelay.put("defaults", 150);
        mMobileListDelay.put("MI 5",      140);
        mMobileListDelay.put("MI 4",      289);
        mMobileListDelay.put("MI 4LTE",   275);
        mMobileListDelay.put("MI MAX",   238);
        mMobileListDelay.put("SCL-AL00", 165);
        mMobileListDelay.put("Redmi Note 2", 190);
        mMobileListDelay.put("MX5",       150);
        mMobileListDelay.put("Nexus 6P",  136); //huawei
        mMobileListDelay.put("LGLS991",   111); //LG
        mMobileListDelay.put("X9007",     168); //oppo
        mMobileListDelay.put("MI 3W",     289); // mi 3
        mMobileListDelay.put("H60-L01",   219); // huawei
        mMobileListDelay.put("vivo X6L", 203);
        mMobileListDelay.put("HUAWEI GRA-UL00", 211);
        mMobileListDelay.put("Redmi Note 3", 264);
        mMobileListDelay.put("Redmi 3", 281);
        mMobileListDelay.put("Redmi 3S", 199);
        mMobileListDelay.put("EVA-AL00", 142); //huawei
        mMobileListDelay.put("CHM-UL00", 195); //huawei
        mMobileListDelay.put("SM-N9002", 200);
        mMobileListDelay.put("SM-G9280", 210);
        mMobileListDelay.put("SCL-TL00H", 166);
        mMobileListDelay.put("PE-TL10", 210);
        mMobileListDelay.put("Mi-4c", 268);
        mMobileListDelay.put("HUAWEI MT7-TL10", 207);
        mMobileListDelay.put("XT1575", 105);
        mMobileListDelay.put("MI NOTE LTE", 240);
        mMobileListDelay.put("HTC M9e", 174);
        mMobileListDelay.put("A0001", 167);
        mMobileListDelay.put("OPPO R9m", 267);
        mMobileListDelay.put("D5803", 249);

    }
}
