package com.tnp;

import com.xiaoyi.camera.sdk.AntsCamera;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by Chuanlong on 2016/1/15.
 */
public class TnpStatistic {

    private final static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static void onConnectXiaoyiEvent(String uid, String p2pid, boolean isSuccess, int ret, String mode, long costTime){

        String connectResult = "DeviceID:" + uid + "/" + p2pid + ",Time:" + formatToMillionSeconds(System.currentTimeMillis())
                + ",Log:" + (isSuccess ?
                ("Connect OK,Mode:" + mode + ",ConnectTime:" + costTime + "MS") :
                ("Connect Error,Result:" + ret + ",ConnectTime:" + costTime + "MS" ));

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("ReportInfo", connectResult);

        if(AntsCamera.passwordInvalidProcesser != null){
            AntsCamera.passwordInvalidProcesser.onXiaoyiEvent("TNPReport", map);
        }
    }

    public static void onCommandXiaoyiEvent(String uid, String p2pid, boolean isSend, String commandType, boolean isSuccess, int ret, int commandNumber){

        String commandResult = "DeviceID:" + uid + "/" + p2pid + ",Time:" + formatToMillionSeconds(System.currentTimeMillis())
                + ",Log:" + (isSend ? "SendCommand" : "RecvCommand")
                + (isSuccess ? " OK" : " Error")
                + ",CommandType:" + commandType + ",CommandNumber:" + commandNumber
                + ",ErrorCode:" + ret;

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("ReportInfo", commandResult);

        if(AntsCamera.passwordInvalidProcesser != null){
            AntsCamera.passwordInvalidProcesser.onXiaoyiEvent("TNPReport", map);
        }
    }

    public static void onVideoXiaoyiEvent(String uid, String p2pid, boolean isRealTime, long costTime){

        String timeResult = "DeviceID:" + uid + "/" + p2pid + ",Time:" + formatToMillionSeconds(System.currentTimeMillis())
                + ",Log:Recv First " + (isRealTime ? "RealTime" : "Record") + " Video Frame, Timestamp:" + costTime;

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("ReportInfo", timeResult);

        if(AntsCamera.passwordInvalidProcesser != null){
            AntsCamera.passwordInvalidProcesser.onXiaoyiEvent("TNPReport", map);
        }
    }

    public static void onErrorXiaoyiEvent(String uid, String p2pid, int ret){

        String errorResult = "DeviceID:" + uid + "/" + p2pid + ",Time:" + formatToMillionSeconds(System.currentTimeMillis())
                + ",Log:Session Error:" + ret;

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("ReportInfo", errorResult);

        if(AntsCamera.passwordInvalidProcesser != null){
            AntsCamera.passwordInvalidProcesser.onXiaoyiEvent("TNPReport", map);
        }
    }

    public static void onReadErrorXiaoyiEvent(String uid, String p2pid, int error,
                                              int channel, int tnpVersion, int ioType,
                                              int expectSize, int readSize){
        String errorResult = "DeviceID:" + uid + "/" + p2pid + ",Time:" + formatToMillionSeconds(System.currentTimeMillis())
                + ",Log:Read TNP Data Error,ErrorCode:" + error + ",DataChannel:" + channel
                + ",tnpVersion:"+tnpVersion + ",ioType:" + ioType
                + ",expectedDataSize:" + expectSize + ",readDataSize:" + readSize;


        HashMap<String, String> map = new HashMap<String, String>();
        map.put("ReportInfo", errorResult);

        if(AntsCamera.passwordInvalidProcesser != null){
            AntsCamera.passwordInvalidProcesser.onXiaoyiEvent("TNPReport", map);
        }
    }



    public static void onConnectUmengEvent(String info){
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("info", info);

        if(AntsCamera.passwordInvalidProcesser != null){
            AntsCamera.passwordInvalidProcesser.onUmengEvent("TNPConnect", map);
        }
    }

    public static void onConnectErrorUmengEvent(int error){
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("info", ""+error);

        if(AntsCamera.passwordInvalidProcesser != null){
            AntsCamera.passwordInvalidProcesser.onUmengEvent("TNPConnectError", map);
        }
    }

    public static void onErrorUmengEvent(int error){
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("info", ""+error);

        if(AntsCamera.passwordInvalidProcesser != null){
            AntsCamera.passwordInvalidProcesser.onUmengEvent("TNPError", map);
        }
    }

    public static void onConnectModeUmengEvent(String mode){
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("info", mode);

        if(AntsCamera.passwordInvalidProcesser != null){
            AntsCamera.passwordInvalidProcesser.onUmengEvent("TNPConnectMode", map);
        }
    }

    public static void onConnectUmengTimeEvent(long costTime){
        if(costTime < 0 || costTime > 60 * 1000){
            return;
        }

        if(AntsCamera.passwordInvalidProcesser != null){
            AntsCamera.passwordInvalidProcesser.onUmengTimeEvent("TNPConnectTime", (int)costTime, new HashMap<String, String>());
        }
    }

    public static void onConnectP2PUmengTimeEvent(long costTime){
        if(costTime < 0 || costTime > 60 * 1000){
            return;
        }

        if(AntsCamera.passwordInvalidProcesser != null){
            AntsCamera.passwordInvalidProcesser.onUmengTimeEvent("TNPConnectTimeP2P", (int)costTime, new HashMap<String, String>());
        }
    }

    public static void onConnectRelayUmengTimeEvent(long costTime){
        if(costTime < 0 || costTime > 60 * 1000){
            return;
        }

        if(AntsCamera.passwordInvalidProcesser != null){
            AntsCamera.passwordInvalidProcesser.onUmengTimeEvent("TNPConnectTimeRelay", (int)costTime, new HashMap<String, String>());
        }
    }

    public static void onCommandUmengEvent(String info){
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("info", info);

        if(AntsCamera.passwordInvalidProcesser != null){
            AntsCamera.passwordInvalidProcesser.onUmengEvent("TNPCommandResult", map);
        }
    }

    public static void onRealTimeStartVideoUmengTimeEvent(long costTime){
        if(costTime < 0 || costTime > 60 * 1000){
            return;
        }

        if(AntsCamera.passwordInvalidProcesser != null){
            AntsCamera.passwordInvalidProcesser.onUmengTimeEvent("TNPRealTimeStartVideoTime", (int)costTime, new HashMap<String, String>());
        }
    }

    public static void onRecordStartVideoUmengTimeEvent(long costTime){
        if(costTime < 0 || costTime > 60 * 1000){
            return;
        }

        if(AntsCamera.passwordInvalidProcesser != null){
            AntsCamera.passwordInvalidProcesser.onUmengTimeEvent("TNPRecordStartVideoTime", (int)costTime, new HashMap<String, String>());
        }
    }


    private static String formatToMillionSeconds(long mTime){
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        return sdf.format(new Date(mTime));
    }


}
