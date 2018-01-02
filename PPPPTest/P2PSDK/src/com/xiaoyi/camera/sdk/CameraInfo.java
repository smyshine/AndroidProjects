package com.xiaoyi.camera.sdk;

import com.tutk.IOTC.AVIOCTRLDEFs.SMsAVIoctrlIpcInfoCfgResp;
import com.tutk.IOTC.AVIOCTRLDEFs.SMsAVIoctrlMotionDetectCfg;
import com.tutk.IOTC.AVIOCTRLDEFs.SMsgAVIoctrlDeviceInfoResp;


public class CameraInfo {

    public String firmwareVersion;

    public SMsgAVIoctrlDeviceInfoResp deviceInfo;

    public SMsAVIoctrlIpcInfoCfgResp ipcInfo;

    public SMsAVIoctrlMotionDetectCfg motionDetect;

}
