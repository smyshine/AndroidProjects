package com.xiaoyi.camera.sdk;

import android.os.Build;

import com.xiaoyi.log.AntsLog;
import com.xiaoyi.p2pservertest.audio.MobileAGC;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by xin.dingfeng on 2016/5/13.
 */
public class AudioUtil {

    public static final String CAMERA_MODULE_GAIN_DEFAULT = "DEFAULT";
    public static final String CAMERA_MODULE_GAIN_LOW = "GAIN_LOW";
    public static final String CAMERA_MODULE_GAIN_HIGH = "GAIN_HIGH";

    public static short getDelay(String mModuleGain){
        /*int delay = 150;
        if(Build.MODEL.contains("MI 5")){
            delay = 140;
        }
        else if(Build.MODEL.contains("MI 4")){
            delay = 250;
        }else if(Build.MODEL.contains("SCL-AL00")){
            delay = 170;
        }else if(Build.MODEL.contains("MX5")){
            delay = 150;
        }*/

        int delay = MobileTypeGainLow.getInstance().getMyMobileTypeStruct(Build.MODEL).mAecDealy;
        if(mModuleGain.equals(CAMERA_MODULE_GAIN_HIGH)){
             delay = MobileTypeGainHigh.getInstance().getMyMobileTypeStruct(Build.MODEL).mAecDealy;
        }else if(mModuleGain.equals(CAMERA_MODULE_GAIN_LOW)){
             delay = MobileTypeGainLow.getInstance().getMyMobileTypeStruct(Build.MODEL).mAecDealy;

        }

        AntsLog.d("MobileAudioParam","aec delay time:"+ delay);

        return (short) delay;

    }

    public static void PlayMobileAGCInit(MobileAGC mMobileAGC, int audioNum, String mModuleGain){
       /* if(Build.MODEL.contains("MI 5")){
            mMobileAGC.init(audioNum, 9, 10);
        }
        else if(Build.MODEL.contains("MI 4")){
            mMobileAGC.init(audioNum, 9, 3);
        }else if(Build.MODEL.contains("SCL-AL00")){
            mMobileAGC.init(audioNum, 9, 3);
        }else if(Build.MODEL.contains("SCL-AL00")){
            mMobileAGC.init(audioNum, 9, 3);
        }
        else {
            mMobileAGC.init(audioNum, 9, 10);
        }*/
        int gain = MobileTypeGainLow.getInstance().getMyMobileTypeStruct(Build.MODEL).mAgcPlayGain;
        int limit = MobileTypeGainLow.getInstance().getMyMobileTypeStruct(Build.MODEL).mAgcPlayLimit;
        if(mModuleGain.equals(CAMERA_MODULE_GAIN_HIGH)){
             gain = MobileTypeGainHigh.getInstance().getMyMobileTypeStruct(Build.MODEL).mAgcPlayGain;
             limit = MobileTypeGainHigh.getInstance().getMyMobileTypeStruct(Build.MODEL).mAgcPlayLimit;
        }else if(mModuleGain.equals(CAMERA_MODULE_GAIN_LOW)){
             gain = MobileTypeGainLow.getInstance().getMyMobileTypeStruct(Build.MODEL).mAgcPlayGain;
             limit = MobileTypeGainLow.getInstance().getMyMobileTypeStruct(Build.MODEL).mAgcPlayLimit;
        }

        AntsLog.d("MobileAudioParam","play gain:"+ gain+", play limit:"+limit);
        mMobileAGC.init(audioNum, gain, limit);

    }

    public static void RecordMobileAGCInt(MobileAGC mobileAGC, int sampleRate,String mModuleGain){
        /*if(Build.MODEL.contains("MI 5")){
            mobileAGC.init(sampleRate, 9, 1);
        }
        else if(Build.MODEL.contains("MI 4")){
            mobileAGC.init(sampleRate, 18, 1);
        }else if(Build.MODEL.contains("SCL-AL00")){
            mobileAGC.init(sampleRate, 14, 1);
        }
        else {
            mobileAGC.init(sampleRate, 9, 1);
        }*/

        int gain = MobileTypeGainLow.getInstance().getMyMobileTypeStruct(Build.MODEL).mAgcRecGain;
        int limit = MobileTypeGainLow.getInstance().getMyMobileTypeStruct(Build.MODEL).mAgcRecLimit;

        if(mModuleGain.equals(CAMERA_MODULE_GAIN_HIGH)){
             gain = MobileTypeGainHigh.getInstance().getMyMobileTypeStruct(Build.MODEL).mAgcRecGain;
             limit = MobileTypeGainHigh.getInstance().getMyMobileTypeStruct(Build.MODEL).mAgcRecLimit;
        }else if(mModuleGain.equals(CAMERA_MODULE_GAIN_LOW)){
             gain = MobileTypeGainLow.getInstance().getMyMobileTypeStruct(Build.MODEL).mAgcRecGain;
             limit = MobileTypeGainLow.getInstance().getMyMobileTypeStruct(Build.MODEL).mAgcRecLimit;
        }

        AntsLog.d("MobileAudioParam","Rec gain:"+ gain+", rec limit:"+limit);
        mobileAGC.init(sampleRate, gain, limit);
    }

    public static void AmplifyPCMData(byte[] tempBufferOneInstace){
        if(Build.MODEL.contains("MI 5")){
            AmplifyPCMData(tempBufferOneInstace, tempBufferOneInstace.length, (float) 0.3);
        }
        else if(Build.MODEL.contains("MI 4")){
            AmplifyPCMData(tempBufferOneInstace, tempBufferOneInstace.length, (float) 0.3);
        }
        else {
            AmplifyPCMData(tempBufferOneInstace, tempBufferOneInstace.length, (float) 0.3);
        }
    }


    public static void AmplifyPCMData(byte[] pData, int nLen, float multiple){

        int nCur = 0;
        short[] temp = new short[nLen / 2];

        ByteBuffer.wrap(pData).order(ByteOrder.LITTLE_ENDIAN)
                .asShortBuffer().get(temp);

        while (nCur < nLen / 2)
        {
            //    short* volum = (short*)(pData + nCur);
            short volume = temp[nCur];
            temp[nCur] = (short)(volume * multiple);

            if (temp[nCur] < -0x8000)
            {
                temp[nCur] = -0x8000;
            }

            if (temp[nCur] > 0x7FFF)//爆音的处理
            {
                temp[nCur] = 0x7FFF;
            }

            nCur++;
        }

        ByteBuffer.wrap(pData).order(ByteOrder.LITTLE_ENDIAN)
                .asShortBuffer().put(temp);

    }

}
