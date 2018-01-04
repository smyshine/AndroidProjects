package com.xiaoyi.camera.sdk;

import com.tutk.IOTC.AVFrame;

/**
 * 视频播放回调接口
 * 
 * @author chenyc
 * 
 */

public interface AntsCameraListener {
    /**
     * 接收video Frame时回调
     * 
     * @param avFrame
     *            帧数据
     */
    public void receiveVideoFrameData(AVFrame avFrame);

    /**
     * 接收video 信息发送改变时回调
     * 
     * @param avFrame
     *            帧数据
     */
    public void receiveVideoInfoChanged(AVFrame avFrame);

    /**
     * 接收Frame,返回错误状态时回调
     * 
     * @param 错误状态
     */
    public void receiveErrorState(String step, int state, int reason);

    /**
     * 收到控制信息时回调
     * 
     * @param avFrame
     *            帧数据
     */
    public void receiveAudioFrameData(AVFrame avFrame);

    /**
     * 收到Speak通道打开信息
     * 
     */
    public void receiveSpeakEnableInfo(boolean enable);

    /**
     * 收到连接进度回调
     * 
     * @param progress
     *            进度
     */
    public void receiveConnectingProgress(int progress);

    /**
     * 收到连接成功回调
     * 
     * @param progress
     *            进度
     */
    public void receiveConnectSuccess();

    /**
     * 接收Frame,返回密码错误
     * 
     * @param 错误状态
     */
    public void receivePasswordError(int reason, int state);

    /**
     * 发送指令错误
     * 
     */
    public void receiveSendIOError(int command, int error);


}
