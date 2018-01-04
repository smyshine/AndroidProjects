package com.xiaoyi.camera.sdk;

import android.text.TextUtils;

import java.io.Serializable;

public class P2PDevice implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int TYPE_UNKNOWN = -1;
    public static final int TYPE_TUTK = 0;
    public static final int TYPE_LANGTAO = 1;
    public static final int TYPE_TNP = 2;


    public static final String TYPE_TNP_PREFIX = "TNP";

    public static final String TYPE_DES_TUTK = "Tutk";
    public static final String TYPE_DES_LANGTAO = "Langtao";
    public static final String TYPE_DES_TNP = "Tnp";
    public static final String TYPE_DES_UNKNOWN = "Unknown";



    public static final String MODEL_V1 = "yunyi.camera.v1";
    public static final String MODEL_V2 = "yunyi.camera.htwo1";
    public static final String MODEL_H19 = "h19";
    public static final String MODEL_M20 = "yunyi.camera.mj1";
    public static final String MODEL_H20 = "h20";
    public final static String MODEL_Y20  = "yunyi.camera.y20";
    public final static String MODEL_D11  = "d11";
    public final static String MODEL_Y10  = "y10";


    public String uid;

    public String p2pid;

    public String account;

    public String pwd;

    public int type;

    public String tnpServerString;

    public String tnpLicenseDeviceKey;

    public String model;

    public boolean isEncrypted;

    public int device2UtcOffsetHour;

    public boolean isFactoryTest;


    public P2PDevice(String uid, String p2pid, String account, String pwd, int type, String tnpServerString, String tnpLicenseDeviceKey, String model, boolean isEncrypted, int device2UtcOffsetHour){
        this.uid = uid;
        this.p2pid = p2pid;
        this.account = TextUtils.isEmpty(account) ? "admin" : account;
        this.pwd = pwd;
        this.type = type;
        this.tnpServerString = tnpServerString;
        this.tnpLicenseDeviceKey = tnpLicenseDeviceKey;
        this.model = model;
        this.isEncrypted = isEncrypted;
        this.device2UtcOffsetHour = device2UtcOffsetHour;
    }

    public P2PDevice(String uid, String p2pid, String account, String pwd, int type, String tnpServerString, String tnpLicenseDeviceKey, String model, boolean isEncrypted, int device2UtcOffsetHour, boolean isFactoryTest){
        this.uid = uid;
        this.p2pid = p2pid;
        this.account = TextUtils.isEmpty(account) ? "admin" : account;
        this.pwd = pwd;
        this.type = type;
        this.tnpServerString = tnpServerString;
        this.tnpLicenseDeviceKey = tnpLicenseDeviceKey;
        this.model = model;
        this.isEncrypted = isEncrypted;
        this.device2UtcOffsetHour = device2UtcOffsetHour;
        this.isFactoryTest = isFactoryTest;
    }

    public String getTypeDes(){
        String typeDes;
        if(type == TYPE_TUTK){
            typeDes = TYPE_DES_TUTK;
        }else if(type == TYPE_LANGTAO){
            typeDes = TYPE_DES_LANGTAO;
        }else if(type == TYPE_TNP){
            typeDes = TYPE_DES_TNP;
        }else{
            typeDes = TYPE_DES_UNKNOWN;
        }
        return typeDes;
    }

    public static int getDeviceType(String p2pid) {

        if(TextUtils.isEmpty(p2pid)){
            return TYPE_UNKNOWN;
        }else{
            if(isTnpDeviceType(p2pid)){
                return TYPE_TNP;
            }else{
                if (p2pid.toLowerCase().equals(p2pid)) {
                    return TYPE_LANGTAO;
                } else {
                    return TYPE_TUTK;
                }
            }
        }
    }


    private static boolean isTnpDeviceType(String p2pid){

        if(TextUtils.isEmpty(p2pid)){
            return false;
        }

        if(!p2pid.startsWith(TYPE_TNP_PREFIX)){
            return false;
        }

        String[] tokens = p2pid.split("-");

        if(tokens == null || tokens.length != 3){
            return false;
        }

        return true;
    }


}
