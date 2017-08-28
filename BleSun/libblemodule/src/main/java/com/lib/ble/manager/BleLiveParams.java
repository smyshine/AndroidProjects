package com.lib.ble.manager;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by SMY on 2016/12/12.
 */
public class BleLiveParams implements Parcelable {
    public int rememberPwd = 1;
    public String wifiMac = "";
    public String wifiSsid = "";
    public String wifiPwd = "";

    public int resolution = 0;
    public int frameRate = 0;
    public int quality = 0;
    public int record = 0;
    public String description;
    public String streamUrl = "";
    public String streamId;//for youtube only
    public int server = 0;//0-自定义;1-youtube;2-facebook
    public static final int LIVE_SER_USERDEFINE = 0;//自定义直播平台
    public static final int LIVE_SER_YOUTUBE = 1;//youtube直播
    public static final int LIVE_SER_FACEBOOK = 2;//facebook直播
    public static final int LIVE_SER_WEIBO = 3;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(rememberPwd);
        parcel.writeString(wifiMac);
        parcel.writeString(wifiSsid);
        parcel.writeString(wifiPwd);
        parcel.writeInt(resolution);
        parcel.writeInt(frameRate);
        parcel.writeInt(record);
        parcel.writeString(description);
        parcel.writeString(streamUrl);
        parcel.writeString(streamId);
        parcel.writeInt(server);
        parcel.writeInt(quality);
    }

    public BleLiveParams(Parcel source){
        this.rememberPwd = source.readInt();
        this.wifiMac = source.readString();
        this.wifiSsid = source.readString();
        this.wifiPwd = source.readString();
        this.resolution = source.readInt();
        this.frameRate = source.readInt();
        this.record = source.readInt();
        this.description = source.readString();
        this.streamUrl = source.readString();
        this.streamId = source.readString();
        this.server = source.readInt();
        this.quality = source.readInt();
    }

    public static final Creator<BleLiveParams> CREATOR = new Creator<BleLiveParams>() {

        @Override
        public BleLiveParams createFromParcel(Parcel source) {
            return new BleLiveParams(source);
        }

        @Override
        public BleLiveParams[] newArray(int size) {
            return null;
        }
    };

    public BleLiveParams(){}

    public void setStreamUrl(String url){
        this.streamUrl = url;
    }

    /*
    * <item>"1536*768"</item>
        <item>"2560*1280"</item>
        <item>"3840*1920"</item>
        */
    public int getResolutionWidth(){
        if (resolution == 0){
            return 1536;
        }
        if (resolution == 1){
            return 2560;
        }
        if (resolution == 2){
            return 3840;
        }
        return 1440;
    }

    public int getResolutionHeight(){
        if (resolution == 0){
            return 768;
        }
        if (resolution == 1){
            return 1280;
        }
        if (resolution == 2){
            return 1920;
        }
        return 720;
    }

}
