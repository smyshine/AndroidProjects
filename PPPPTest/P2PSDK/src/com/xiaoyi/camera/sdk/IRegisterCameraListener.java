package com.xiaoyi.camera.sdk;

import com.tutk.IOTC.AVFrame;

public abstract interface IRegisterCameraListener {

    // 接收　video Frame时回调
    abstract void receiveVideoFrameData(AVFrame avFrame);

    // 接收　audio Frame 收到控制信息时回调
    abstract void receiveAudioFrameData(AVFrame avFrame);

    // 返回错误状态时回调
    abstract void receiveErrorState(String step, int state);

    // 接收session返回的信息
    abstract void receiveSessionInfo(String step, int state);

    // 收到控制信息时回调
    abstract void receiveIOCtrlData(int type, byte[] data);

    // Channel连接时回调
    abstract void receiveChannelInfo(int code);

    // Speak通道打开
    abstract void receiveSpeakEnableInfo(boolean enable);

    // 接收连接状态
    abstract void receiveConnectingProgress(int state);

    // 接收到连接成功
    abstract void receiveConnectedSuccess();

    // 收到P2PMessage发送失败
    abstract void receiveSendP2PMessageError(P2PMessage p2pMessage);

    // 收到网络检测的数据返回, string
    abstract void receiveNetworkCheck(int code, byte[] info);

    // 收到设备的在线状态返回, SMsgAVIoctrlOnlineStatusResp
    abstract void receiveOnlineStatus(int code, byte[] data);

}
