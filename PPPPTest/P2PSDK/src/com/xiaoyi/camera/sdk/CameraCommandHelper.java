package com.xiaoyi.camera.sdk;

import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.AVIOCTRLDEFs.SMsgAVIoctrlDeviceInfoResp;
import com.tutk.IOTC.AVIOCTRLDEFs.SMsgAVIoctrlQueryRtmpStateResp;
import com.tutk.IOTC.EventInfo;
import com.tutk.IOTC.Packet;
import com.xiaoyi.camera.sdk.P2PMessage.IMessageResponse;
import com.xiaoyi.log.AntsLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CameraCommandHelper {

    private final static String TAG = "CameraCommandHelper";

    private AntsCamera antsCamera;
    private boolean isByteOrderBig;

    public CameraCommandHelper(AntsCamera antsCamera) {
        this.antsCamera = antsCamera;
        this.isByteOrderBig = antsCamera.isByteOrderBig();
    }

    public static interface OnCommandResponse<T> {
        void onResult(T obj);

        void onError(int errorCode);
    }

    public static interface OnGetEventsCommandResponse {
        void onEvents(List<EventInfo> events, int total);

        void onError(int errorCode);
    }

    /**
     * 获取事件
     *
     * @param getEventsCallback
     */
    public void getEvents(final OnGetEventsCommandResponse getEventsCallback) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(new Date());
        cal1.add(Calendar.HOUR, -720);
        long startDate = cal1.getTimeInMillis();
        long endDate = System.currentTimeMillis();
        getEvents(startDate, endDate, getEventsCallback);
    }

    /**
     * 获取事件
     *
     * @param startDate
     * @param endDate
     * @param getEventsCallback
     */
    public void getEvents(long startDate, long endDate,
                          final OnGetEventsCommandResponse getEventsCallback) {
        AntsLog.d(TAG, "getEvents,startDate:" + startDate + ",endDate:" + endDate);

        if (antsCamera.getCameraType() == P2PDevice.TYPE_TNP) {
            getTnpEvents(startDate, endDate, getEventsCallback);
        } else {
            getTutkLangtaoEvents(startDate, endDate, getEventsCallback);
        }

    }

    private void getTutkLangtaoEvents(long startDate, long endDate,
                                      final OnGetEventsCommandResponse getEventsCallback) {

        int reqId = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_LISTEVENT_REQ;
        int resId = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_LISTEVENT_RESP;
        byte[] data = AVIOCTRLDEFs.SMsgAVIoctrlListEventReq.parseConent(0, startDate, endDate,
                EventInfo.EVENT_ALL, EventInfo.STATUS_UNREAD, isByteOrderBig);

        IMessageResponse resp = new IMessageResponse() {

            @Override
            public boolean onResponse(byte[] data) {
                int eventSize = AVIOCTRLDEFs.SAvEvent.getTotalSize();
                if (data == null || data.length < eventSize) {
                    if(getEventsCallback != null) {
                        getEventsCallback.onError(-1);
                    }
                    return false;
                }

//                getEventsCallback.onEventsForXiaomi(new SMsgAVIoctrlListEventResp(data, isByteOrderBig));

                ArrayList<EventInfo> events = new ArrayList<EventInfo>();
                byte[] buffer = new byte[4];
                System.arraycopy(data, 0, buffer, 0, 4);
//                int channel = AntsUtil.bytes2int(buffer);
                int channel = Packet.byteArrayToInt(buffer, 0, isByteOrderBig);
                System.arraycopy(data, 4, buffer, 0, 4);
//                int total = AntsUtil.bytes2int(buffer);
                int total = Packet.byteArrayToInt(buffer, 0, isByteOrderBig);
                byte index = (byte) data[8];
                byte flag = (byte) data[9];
                byte count = (byte) data[10];
                byte reserved = (byte) data[11];
                // 没有查到事件
                if (total == 0) {
                    if(getEventsCallback != null) {
                        getEventsCallback.onEvents(events, total);
                    }
                    return true;
                }

                int realCount = data.length / eventSize - 1;

                if (realCount != count) {
                    return false;
                }

                // 获取事件
                try {
                    for (int i = 1; i <= count; i++) {
                        byte[] timeBuf = new byte[8];
                        System.arraycopy(data, i * eventSize, timeBuf, 0, 8);

                        AVIOCTRLDEFs.STimeDay sTimeDay = new AVIOCTRLDEFs.STimeDay(timeBuf, isByteOrderBig);
                        byte type = data[(8 + (i * eventSize))];
                        byte status = data[(9 + (i * eventSize))];

                        byte[] reservedArr = new byte[2];
                        System.arraycopy(data, 10 + i * eventSize, reservedArr, 0, 2);
//                        short length = AntsUtil.bytes2short(reservedArr);
                        short length = Packet.byteArrayToShort(reservedArr, 0, isByteOrderBig);
                        EventInfo eventInfo = new EventInfo(sTimeDay, length);
                        events.add(eventInfo);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if(getEventsCallback != null) {
                    getEventsCallback.onEvents(events, total);
                }

                if (flag == 1) {
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public void onError(int error) {
                if(getEventsCallback != null) {
                    getEventsCallback.onError(error);
                }
            }
        };

        antsCamera.sendP2PMessage(new P2PMessage(reqId, resId, data, resp));

    }

    private void getTnpEvents(long startDate, long endDate,
                              final OnGetEventsCommandResponse getEventsCallback) {

        int reqId = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_TNP_EVENT_LIST_REQ;
        final int resId = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_TNP_EVENT_LIST_RESP;
        byte[] data = AVIOCTRLDEFs.SMsgAVIoctrlListEventReq.parseConent(0, startDate, endDate,
                EventInfo.EVENT_ALL, EventInfo.STATUS_UNREAD, isByteOrderBig);

        antsCamera.sendP2PMessage(new P2PMessage(reqId, resId, data, new IMessageResponse() {
            @Override
            public boolean onResponse(byte[] data) {
                if (data == null || data.length < AVIOCTRLDEFs.SMsgAVIoctrlTnpListEventResp.HEAD_SIZE) {
                    if(getEventsCallback != null) {
                        getEventsCallback.onError(-1);
                    }
                    return false;
                }

                AVIOCTRLDEFs.SMsgAVIoctrlTnpListEventResp tnpEvents = new AVIOCTRLDEFs.SMsgAVIoctrlTnpListEventResp(data, isByteOrderBig);

                List<EventInfo> events = new ArrayList<EventInfo>();
                for (int i = 0; i < tnpEvents.eventCount; i++) {
                    AVIOCTRLDEFs.SAvTnpEvent tnpEvent = tnpEvents.avTnpEvent[i];
                    EventInfo eventInfo = new EventInfo(tnpEvent.starttime, tnpEvent.duration);
                    events.add(eventInfo);
                }
                if(getEventsCallback != null) {
                    getEventsCallback.onEvents(events, events.size());
                }
                return true;
            }

            @Override
            public void onError(int error) {
                if(getEventsCallback != null) {
                    getEventsCallback.onError(error);
                }
            }
        }));


    }


    /**
     * 获取固件版本
     *
     * @param callback
     */
    public void doGetCameraVersion(final OnCommandResponse<String> callback) {
        AntsLog.d(TAG, "doGetCameraVersion!");
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_UPDATE_CHECK_PHONE_REQ;
        int respType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_UPDATE_CHECK_PHONE_RSP;
        byte[] input = AVIOCTRLDEFs.SMsgAVIoctrlDeviceVersionReq.parseContent();
        IMessageResponse resp = new IMessageResponse() {

            @Override
            public boolean onResponse(byte[] data) {
                String version = Packet.byteArrayToString(data, data.length).trim();
                antsCamera.getCameraInfo().firmwareVersion = version;
                if (callback != null) {
                    callback.onResult(version);
                }
                return true;
            }

            @Override
            public void onError(int error) {
                if (callback != null) {
                callback.onError(error);
            }
            }

        };

        if (antsCamera == null) {
            return;
        }

        antsCamera.sendP2PMessage(new P2PMessage(reqType, respType, input, resp));

    }

    /**
     * 发送固件升级指令
     *
     * @param url
     * @param callback
     */

    public void doSendUpgradeCommand(String url,
                                     final OnCommandResponse<SMsgAVIoctrlDeviceInfoResp> callback) {
        AntsLog.d(TAG, "doSendUpgradeCommand:" + url);
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_UPDATE_PHONE_REQ;
        int respType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_UPDATE_PHONE_RSP;
        byte[] input = AVIOCTRLDEFs.SMsgAVIoctrlUpgradeDeviceVersionReq.parseContent(url);

        antsCamera.sendP2PMessage(new P2PMessage(reqType, respType, input, new IMessageResponse() {

            @Override
            public boolean onResponse(byte[] data) {
                if(callback != null){
                callback.onResult(SMsgAVIoctrlDeviceInfoResp.parse(data, isByteOrderBig));
                }
                return true;
            }

            @Override
            public void onError(int error) {
                if (callback != null) {
                callback.onError(error);
            }
            }
        }));

    }

    /**
     * 关闭/开启指示灯
     *
     * @param callback
     */
    public void doOpenOrCloseLight(boolean on,
                                   final OnCommandResponse<SMsgAVIoctrlDeviceInfoResp> callback) {
        AntsLog.d(TAG, "doOpenOrCloseLight:" + on);
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_CLOSE_LIGHT_REQ;
        int respType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_CLOSE_LIGHT_RESP;

        // 0 开灯 ，1关灯
        byte[] input = null;
        if (on) {
            input = Packet.intToByteArray(0, isByteOrderBig);
        } else {
            input = Packet.intToByteArray(1, isByteOrderBig);
        }

        antsCamera.sendP2PMessage(new P2PMessage(reqType, respType, input, new IMessageResponse() {

            @Override
            public boolean onResponse(byte[] data) {
                SMsgAVIoctrlDeviceInfoResp deviceInfo = SMsgAVIoctrlDeviceInfoResp.parse(data, isByteOrderBig);
                antsCamera.getCameraInfo().deviceInfo = deviceInfo;
                if (callback != null) {
                    callback.onResult(deviceInfo);
                }
                return true;
            }

            @Override
            public void onError(int error) {
                if (callback != null) {
                    callback.onError(error);
                }
            }
        }));

    }

    /**
     * 关闭/开启视频
     *
     * @param callback
     */
    public void doOpenOrCloseVideo(boolean on,
                                   final OnCommandResponse<SMsgAVIoctrlDeviceInfoResp> callback) {
        AntsLog.d(TAG, "doOpenOrCloseVideo:" + on);
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_CLOSE_CAMERA_REQ;
        int respType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_CLOSE_CAMERA_RESP;

        // 0 开灯 ，1关灯
        byte[] input = null;
        if (on) {
            input = Packet.intToByteArray(0, isByteOrderBig);
        } else {
            input = Packet.intToByteArray(1, isByteOrderBig);
        }

        antsCamera.sendP2PMessage(new P2PMessage(reqType, respType, input, new IMessageResponse() {

            @Override
            public boolean onResponse(byte[] data) {
                SMsgAVIoctrlDeviceInfoResp deviceInfo = SMsgAVIoctrlDeviceInfoResp.parse(data, isByteOrderBig);
                antsCamera.getCameraInfo().deviceInfo = deviceInfo;
                if (callback != null) {
                    callback.onResult(deviceInfo);
                }
                return true;
            }

            @Override
            public void onError(int error) {
                if (callback != null) {
                callback.onError(error);
            }
            }
        }));
    }

    /**
     * 获取设备状态
     *
     * @param callback
     */
    public void getDeviceInfo(final OnCommandResponse<SMsgAVIoctrlDeviceInfoResp> callback) {
        AntsLog.d(TAG, "getDeviceInfo!");
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_DEVINFO_REQ;
        int respType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_DEVINFO_RESP;
        byte[] input = AVIOCTRLDEFs.SMsgAVIoctrlDeviceInfoReq.parseContent();

        antsCamera.sendP2PMessage(new P2PMessage(reqType, respType, input, new IMessageResponse() {

            @Override
            public boolean onResponse(byte[] data) {
                SMsgAVIoctrlDeviceInfoResp deviceInfo = SMsgAVIoctrlDeviceInfoResp.parse(data, isByteOrderBig);
                antsCamera.getCameraInfo().deviceInfo = deviceInfo;
                if (callback != null) {
                    callback.onResult(deviceInfo);
                }
                return true;
            }

            @Override
            public void onError(int error) {
                if (callback != null) {
                    callback.onError(error);
                }

            }
        }));

    }


    /**
     * 获取视频备份
     */

    public void getVideoBackup(final OnCommandResponse<AVIOCTRLDEFs.SMsAVIoctrlVideoBackupGetResp> callback) {
        AntsLog.d(TAG, "getVideoBackup");
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_VIDEO_BACKUP;
        int respType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_VIDEO_BACKUP_RESP;
        antsCamera.sendP2PMessage(new P2PMessage(reqType, respType, new byte[8], new IMessageResponse() {
            @Override
            public boolean onResponse(byte[] data) {
                AVIOCTRLDEFs.SMsAVIoctrlVideoBackupGetResp videoBackupCfg = AVIOCTRLDEFs.SMsAVIoctrlVideoBackupGetResp.parse(data, isByteOrderBig);
                if (callback != null) {
                    callback.onResult(videoBackupCfg);
                }
                return true;
            }

            @Override
            public void onError(int error) {
                if (callback != null) {
                    callback.onError(error);
                }
            }
        }));

    }
    /**
     * 重启设备
     */
    public void rebootDevice() {
        AntsLog.d(TAG, "rebootDevice!");
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_REBOOT_PHONE_REQ;
        // int respType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_REBOOT_PHONE_RESP;
        antsCamera.sendP2PMessage(new P2PMessage(reqType, new byte[8]));
    }

    /**
     * 取消升级
     */
    public void cancelUpgrade(final OnCommandResponse<SMsgAVIoctrlDeviceInfoResp> callback) {
        AntsLog.d(TAG, "cancelUpgrade!");
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_CANCEL_UPDATE_PHONE_REQ;
        int respType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_CANCEL_UPDATE_PHONE_RESP;
        antsCamera.sendP2PMessage(new P2PMessage(reqType, respType, new byte[8],
                new IMessageResponse() {

                    @Override
                    public boolean onResponse(byte[] data) {
                        SMsgAVIoctrlDeviceInfoResp deviceInfo = SMsgAVIoctrlDeviceInfoResp.parse(data, isByteOrderBig);
                        antsCamera.getCameraInfo().deviceInfo = deviceInfo;
                        if(callback != null){
                        callback.onResult(deviceInfo);
                        }
                        return true;
                    }

                    @Override
                    public void onError(int error) {
                        if (callback != null) {
                        callback.onError(error);
                    }
                    }
                }));
    }

    /**
     * 设置分辨率
     * <p/>
     * 0=自适应;1=720p;2=vga;3=cif
     */
    public void setResolution(int type, final OnCommandResponse<Integer> callback) {
        AntsLog.d(TAG, "setResolution:" + type);
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_RESOLUTION;
        int respType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_RESOLUTION_RESP;
        byte[] data = new byte[8];
        antsCamera.addUseCount();
        antsCamera.setResolutionType(type);
        System.arraycopy(Packet.intToByteArray(type, isByteOrderBig), 0, data, 0, 4);
        System.arraycopy(Packet.intToByteArray(antsCamera.getUseCount(), isByteOrderBig), 0, data, 4, 4);
        antsCamera.sendP2PMessage(new P2PMessage(reqType, respType, data, new IMessageResponse() {

            @Override
            public boolean onResponse(byte[] data) {
                int resolution = Packet.byteArrayToInt(data, 0, isByteOrderBig);
                if (callback != null) {
                    callback.onResult(resolution);
                }
                return true;
            }

            @Override
            public void onError(int error) {
                if (callback != null) {
                    callback.onError(error);
                }
            }
        }));
    }

    public static interface getResoutionCallback {
        void onData(int type);

        void onError(int errorCode);
    }

    /**
     * 获取分辨率
     */
    public void getResolution(final getResoutionCallback callback) {
        AntsLog.d(TAG, "getResolution!");
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_RESOLUTION;
        int respType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_RESOLUTION_RESP;

        antsCamera.sendP2PMessage(new P2PMessage(reqType, respType, new byte[8],
                new IMessageResponse() {

                    @Override
                    public boolean onResponse(byte[] data) {
                        int type = Packet.byteArrayToInt(data, 0, isByteOrderBig);
                        callback.onData(type);
                        return true;
                    }

                    @Override
                    public void onError(int error) {
                        callback.onError(error);

                    }
                }));
    }

    /**
     * 清除MsgAVIoctrlDeviceInfoRespData的数据，重新检查获取
     */
    public void restartCheckDevice() {
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_START_CHECK;
        // int respType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_START_CHECK_RESP;
        antsCamera.sendP2PMessage(new P2PMessage(reqType, new byte[8]));
    }

    /**
     * 格式化TF卡
     */
    public void formatTfCard(final OnCommandResponse<SMsgAVIoctrlDeviceInfoResp> callback) {
        AntsLog.d(TAG, "formatTfCard!");
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_TF_FORMAT;
        int respType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_TF_FORMAT_RESP;
        byte[] arrayOfByte = AVIOCTRLDEFs.SMsgAVIoctrlTfFormate.parseContent(isByteOrderBig);
        antsCamera.sendP2PMessage(new P2PMessage(reqType, respType, arrayOfByte,
                new IMessageResponse() {

                    @Override
                    public boolean onResponse(byte[] data) {
                        SMsgAVIoctrlDeviceInfoResp deviceInfo = SMsgAVIoctrlDeviceInfoResp.parse(data, isByteOrderBig);
                        antsCamera.getCameraInfo().deviceInfo = deviceInfo;
                        callback.onResult(deviceInfo);
                        return true;
                    }

                    @Override
                    public void onError(int error) {
                        if (callback != null) {
                        callback.onError(error);
                    }
                    }
                }));
    }

    /**
     * 询问检测是否结束。返回MsgAVIoctrlDeviceInfoRespData数据
     */
    public void requestCheckState(final OnCommandResponse<SMsgAVIoctrlDeviceInfoResp> callback) {
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_CHECK_STAT_REQ;
        int respType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_CHECK_STAT_REQ_RESP;

        antsCamera.sendP2PMessage(new P2PMessage(reqType, respType, new byte[8],
                new IMessageResponse() {

                    @Override
                    public boolean onResponse(byte[] data) {
                        SMsgAVIoctrlDeviceInfoResp deviceInfo = SMsgAVIoctrlDeviceInfoResp.parse(data, isByteOrderBig);
                        antsCamera.getCameraInfo().deviceInfo = deviceInfo;
                        if(callback != null){
                        callback.onResult(deviceInfo);
                        }
                        return true;
                    }

                    @Override
                    public void onError(int error) {
                        if(callback != null){
                        callback.onError(error);
                    }
                    }
                }));

    }

    /**
     * 浪涛心跳请求
     */
    public void requestLangtaoHeart() {
        if (antsCamera == null) return;

        if (antsCamera.getCameraType() == P2PDevice.TYPE_TUTK) {
            return;
        }

        int reqType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_HEART;

        antsCamera.sendP2PMessage(new P2PMessage(reqType, new byte[8]));

    }

    /**
     * 开启/关闭移动侦测录像
     *
     * @param motionRecord
     * @param callback
     */
    public void setMotionRecord(boolean motionRecord,
                                final OnCommandResponse<SMsgAVIoctrlDeviceInfoResp> callback) {
        byte[] data = AVIOCTRLDEFs.SMsgAVIoctrlDeviceSwitchReq.parseContent(motionRecord ? 0 : 1, isByteOrderBig);
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_RECORD_MODE;
        int respType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_RECORD_MODE_RESP;

        antsCamera.sendP2PMessage(new P2PMessage(reqType, respType, data, new IMessageResponse() {

            @Override
            public boolean onResponse(byte[] data) {
                SMsgAVIoctrlDeviceInfoResp deviceInfo = SMsgAVIoctrlDeviceInfoResp.parse(data, isByteOrderBig);
                antsCamera.getCameraInfo().deviceInfo = deviceInfo;
                if (callback != null) {
                    callback.onResult(deviceInfo);
                }
                return true;
            }

            @Override
            public void onError(int error) {
                if (callback != null) {
                callback.onError(error);
            }
            }
        }));

    }

    /**
     * 开启/关闭翻转
     *
     * @param isReverse
     * @param callback
     */
    public void setReverse(boolean isReverse,
                           final OnCommandResponse<SMsgAVIoctrlDeviceInfoResp> callback) {
        byte[] data = AVIOCTRLDEFs.SMsgAVIoctrlDeviceSwitchReq.parseContent(isReverse ? 1 : 0, isByteOrderBig);
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_MIRROR_FLIP;
        int respType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_MIRROR_FLIP_PESP;

        antsCamera.sendP2PMessage(new P2PMessage(reqType, respType, data, new IMessageResponse() {

            @Override
            public boolean onResponse(byte[] data) {
                SMsgAVIoctrlDeviceInfoResp deviceInfo = SMsgAVIoctrlDeviceInfoResp.parse(data, isByteOrderBig);
                antsCamera.getCameraInfo().deviceInfo = deviceInfo;
                if (callback != null) {
                    callback.onResult(deviceInfo);
                }
                return true;
            }

            @Override
            public void onError(int error) {
                if (callback != null) {
                callback.onError(error);
            }
            }
        }));

    }

    /**
     * 设置红外灯模式为 1:Auto, 2:Day, 3:Night, 4:Time
     *
     * @param mode
     * @param callback
     */
    public void setDayNight(int mode, final OnCommandResponse<SMsgAVIoctrlDeviceInfoResp> callback) {
        byte[] data = AVIOCTRLDEFs.SMsAVIoctrlDayNightMode.parseContent(mode, 0L, 0L, isByteOrderBig);
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_DAYNIGHT_MODE;
        int respType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_DAYNIGHT_MODE_RESP;

        antsCamera.sendP2PMessage(new P2PMessage(reqType, respType, data, new IMessageResponse() {
            @Override
            public boolean onResponse(byte[] data) {
                SMsgAVIoctrlDeviceInfoResp deviceInfo = SMsgAVIoctrlDeviceInfoResp.parse(data, isByteOrderBig);
                antsCamera.getCameraInfo().deviceInfo = deviceInfo;
                if(callback != null){
                callback.onResult(deviceInfo);
                }
                return true;
            }

            @Override
            public void onError(int error) {
                if (callback != null) {
                callback.onError(error);
            }
            }
        }));

    }

    /**
     * 报警区域ROI
     */
    public void setMotionRectRoiMode(int mode, final OnCommandResponse<SMsgAVIoctrlDeviceInfoResp> callback) {
        AntsLog.d(TAG, "setMotionSectRoiMode");
        byte[] data = AVIOCTRLDEFs.SMsgAVIoctrlSmartIaModeReq.parseContent(mode, isByteOrderBig);
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_MOTION_RECT_ROI_MODE;
        int respType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_MOTION_RECT_ROI_MODE_RESP;
        antsCamera.sendP2PMessage(new P2PMessage(reqType, respType, data, new IMessageResponse() {
            @Override
            public boolean onResponse(byte[] data) {
                SMsgAVIoctrlDeviceInfoResp deviceInfo = SMsgAVIoctrlDeviceInfoResp.parse(data, isByteOrderBig);
                antsCamera.getCameraInfo().deviceInfo = deviceInfo;
                if(callback != null){
                callback.onResult(deviceInfo);
                }
                return true;
            }

            @Override
            public void onError(int error) {
                if (callback != null) {
                callback.onError(error);
            }
            }
        }));
    }

    /**
     * 宝宝哭泣
     */
    public void setBabyCryingMode(int mode, final OnCommandResponse<SMsgAVIoctrlDeviceInfoResp> callback) {
        AntsLog.d(TAG, "setBabyCryingMode");
        byte[] data = AVIOCTRLDEFs.SMsgAVIoctrlSmartIaModeReq.parseContent(mode, isByteOrderBig);
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_BABY_CRYING_MODE;
        int respType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_BABY_CRYING_MODE_RESP;
        antsCamera.sendP2PMessage(new P2PMessage(reqType, respType, data, new IMessageResponse() {
            @Override
            public boolean onResponse(byte[] data) {
                SMsgAVIoctrlDeviceInfoResp deviceInfo = SMsgAVIoctrlDeviceInfoResp.parse(data, isByteOrderBig);
                antsCamera.getCameraInfo().deviceInfo = deviceInfo;
                if(callback != null){
                callback.onResult(deviceInfo);
                }
                return true;
            }

            @Override
            public void onError(int error) {
                if (callback != null) {
                callback.onError(error);
            }
            }
        }));
    }

    /**
     * 智能交互
     */
    public void setSmartIaMode(int mode, final OnCommandResponse<SMsgAVIoctrlDeviceInfoResp> callback) {
        AntsLog.d(TAG, "setSmartIaMode");
        byte[] data = AVIOCTRLDEFs.SMsgAVIoctrlSmartIaModeReq.parseContent(mode, isByteOrderBig);
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_SMART_IA_MODE;
        int respType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_SMART_IA_MODE_RESP;
        antsCamera.sendP2PMessage(new P2PMessage(reqType, respType, data, new IMessageResponse() {
            @Override
            public boolean onResponse(byte[] data) {
                SMsgAVIoctrlDeviceInfoResp deviceInfo = SMsgAVIoctrlDeviceInfoResp.parse(data, isByteOrderBig);
                antsCamera.getCameraInfo().deviceInfo = deviceInfo;
                if(callback != null){
                callback.onResult(deviceInfo);
                }
                return true;
            }

            @Override
            public void onError(int error) {
                if (callback != null) {
                callback.onError(error);
            }
            }
        }));
    }

    /**
     * 版本回退
     */
    public void setVersionRecover(final OnCommandResponse<AVIOCTRLDEFs.SMsgAVIoctrlVersionRecoverResp> callback) {
        AntsLog.d(TAG, "setVersionRecover");
        byte[] data = AVIOCTRLDEFs.SMsgAVIoctrlVersionRecoverReq.parseContent(0, isByteOrderBig);
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_VER_RECOVER;
        int respType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_VER_RECOVER_RESP;
        antsCamera.sendP2PMessage(new P2PMessage(reqType, respType, data, new IMessageResponse() {
            @Override
            public boolean onResponse(byte[] data) {
                AVIOCTRLDEFs.SMsgAVIoctrlVersionRecoverResp resp = AVIOCTRLDEFs.SMsgAVIoctrlVersionRecoverResp.parse(data, isByteOrderBig);
                if (callback != null) {
                    callback.onResult(resp);
                }
                return true;
            }

            @Override
            public void onError(int error) {
                if (callback != null) {
                    callback.onError(error);
                }
            }
        }));
    }

    /**
     * 上传固件log
     */

    public void setUploadLog(final OnCommandResponse<SMsgAVIoctrlDeviceInfoResp> callback) {
        AntsLog.d(TAG, "setUploadLog");
        byte[] data = AVIOCTRLDEFs.SMsgAVIoctrlSetUploadLogReq.parseContent(0, isByteOrderBig);
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_UPLOAD_LOG;
        int respType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_UPLOAD_LOG_RESP;

        antsCamera.sendP2PMessage(new P2PMessage(reqType, respType, data, new IMessageResponse() {
            @Override
            public boolean onResponse(byte[] data) {
                SMsgAVIoctrlDeviceInfoResp deviceInfo = SMsgAVIoctrlDeviceInfoResp.parse(data, isByteOrderBig);
                antsCamera.getCameraInfo().deviceInfo = deviceInfo;
                if(callback != null){
                callback.onResult(deviceInfo);
                }
                return true;
            }

            @Override
            public void onError(int error) {
                if (callback != null) {
                callback.onError(error);
            }
            }
        }));

    }

    /**
     * 获取前一版本
     */
    public void getPreVersion(final OnCommandResponse<String> callback) {
        AntsLog.d(TAG, "getPreVersion");
        byte[] input = AVIOCTRLDEFs.SMsgAVIoctrlVersionRecoverReq.parseContent(0, isByteOrderBig);
        //  byte[] input = AVIOCTRLDEFs.SMsgAVIoctrlDeviceVersionReq.parseContent();
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_PRE_VERSION;
        int respType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_PRE_VERSION_RESP;
        antsCamera.sendP2PMessage(new P2PMessage(reqType, respType, input, new IMessageResponse() {
            @Override
            public boolean onResponse(byte[] data) {

                String version = Packet.byteArrayToString(data, data.length).trim();
                AntsLog.d(TAG, "getPreVersion onResponse version="+version);
                if (callback != null) {
                    callback.onResult(version);
                }
                return true;
            }

            @Override
            public void onError(int error) {
                AntsLog.d(TAG, "getPreVersion error");
                if (callback != null) {
                    callback.onError(error);
                }
            }
        }));
    }

    /**
     * 安全删除 SDcard
     */

    public void setUmountSDcard(final OnCommandResponse<SMsgAVIoctrlDeviceInfoResp> callback) {
        AntsLog.d(TAG, "setUmountSDcard");
        byte[] data = AVIOCTRLDEFs.SMsgAVIoctrlUmountSDcardReq.parseContent(0, isByteOrderBig);
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_TF_UMOUNT;
        int respType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_TF_UMOUNT_RESP;
        antsCamera.sendP2PMessage(new P2PMessage(reqType, respType, data, new IMessageResponse() {
            @Override
            public boolean onResponse(byte[] data) {
                SMsgAVIoctrlDeviceInfoResp deviceInfo = SMsgAVIoctrlDeviceInfoResp.parse(data, isByteOrderBig);
                antsCamera.getCameraInfo().deviceInfo = deviceInfo;
                if(callback != null){
                callback.onResult(deviceInfo);
                }
                return true;
            }

            @Override
            public void onError(int error) {
                if (callback != null) {
                callback.onError(error);
            }
            }
        }));
    }

    /**
     * 设置录像视频播放模式
     */

    public void setVideoPlayMode(int speed, final OnCommandResponse<AVIOCTRLDEFs.SMsgAVIoctrlSetPlayModeResp> callback) {
        AntsLog.d(TAG, "setVideoPlayMode");
        byte[] data = AVIOCTRLDEFs.SMsgAVIoctrlSetPlayModeReq.parseContent(speed, isByteOrderBig);
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_RECORD_SPEED;
        int respType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_RECORD_SPEED_RESP;
        antsCamera.sendP2PMessage(new P2PMessage(reqType, respType, data, new IMessageResponse() {
            @Override
            public boolean onResponse(byte[] data) {
                AVIOCTRLDEFs.SMsgAVIoctrlSetPlayModeResp resp = AVIOCTRLDEFs.SMsgAVIoctrlSetPlayModeResp.parse(data, isByteOrderBig);
                if (callback != null) {
                    callback.onResult(resp);
                }
                return true;
            }

            @Override
            public void onError(int error) {
                if (callback != null) {
                    callback.onError(error);
                }
            }
        }));
    }

    /**
     * 获取录像视频播放模式
     */
    public void getVideoPlayMode(final OnCommandResponse<AVIOCTRLDEFs.SMsgAVIoctrlGetPlayModeResp> callback) {
        byte[] data = AVIOCTRLDEFs.SMsgAVIoctrlSetPlayModeReq.parseContent(0, isByteOrderBig);
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_RECORD_SPEED;
        int respType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_RECORD_SPEED_RESP;
        antsCamera.sendP2PMessage(new P2PMessage(reqType, respType, data, new IMessageResponse() {
            @Override
            public boolean onResponse(byte[] data) {
                AVIOCTRLDEFs.SMsgAVIoctrlGetPlayModeResp resp = AVIOCTRLDEFs.SMsgAVIoctrlGetPlayModeResp.parse(data, isByteOrderBig);
                if (callback != null) {
                    callback.onResult(resp);
                }
                return true;
            }

            @Override
            public void onError(int error) {
                if (callback != null) {
                    callback.onError(error);
                }
            }
        }));
    }


    /**
     * 设置高清的清晰度：1,720P; 2,1080P; 3,super_1080P
     *
     * @param resolution
     * @param callback
     */
    public void setResolutionHigh(int resolution, final OnCommandResponse<SMsgAVIoctrlDeviceInfoResp> callback) {
        byte[] data = AVIOCTRLDEFs.SMsAVIoctrlHDResolutionCfg.parseContent(resolution, isByteOrderBig);
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_HD_RESOLUTION;
        int respType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_HD_RESOLUTION_RESP;

        antsCamera.sendP2PMessage(new P2PMessage(reqType, respType, data, new IMessageResponse() {
            @Override
            public boolean onResponse(byte[] data) {
                SMsgAVIoctrlDeviceInfoResp deviceInfo = SMsgAVIoctrlDeviceInfoResp.parse(data, isByteOrderBig);
                antsCamera.getCameraInfo().deviceInfo = deviceInfo;
                if(callback != null){
                callback.onResult(deviceInfo);
                }
                return true;
            }

            @Override
            public void onError(int error) {
                if (callback != null) {
                callback.onError(error);
            }
            }
        }));

    }

    /**
     * 设置用户观看区域编码清晰
     * resolution:
     * 1:720*480(NTSC), 2:720*576(PAL), 3:320*240,
     * 4:640*360(VGA), 5:1280*720(720P), 6:1920*1080(1080P)
     * <p/>
     * strength 目前默认为0
     * <p/>
     * (x1,y1)和(x2,y2)是以图像左上角为(0,0)坐标，x轴为width，y轴为height（注意y轴都是负数）
     */
    public void setROIRect(int resolution, int strength, int x1, int y1, int x2, int y2) {
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_ROI;
        int respType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_ROI_RESP;

        antsCamera.addUseCount();
        byte[] data = AVIOCTRLDEFs.SMsAVIoctrlROICfg.parseContent(resolution, strength, antsCamera.getUseCount(), x1, y1, x2, y2, isByteOrderBig);
        antsCamera.sendP2PMessage(new P2PMessage(reqType, respType, data, new IMessageResponse() {
            @Override
            public boolean onResponse(byte[] data) {
                return true;
            }

            @Override
            public void onError(int error) {

            }
        }));

    }


    /**
     * 设置报警情况下是 0:移动检测报警; 1:人体检测报警
     */
    public void setAlarmMode(int mode, final OnCommandResponse<SMsgAVIoctrlDeviceInfoResp> callback) {
        byte[] data = AVIOCTRLDEFs.SMsAVIoctrlAlarmMode.parseContent(mode, isByteOrderBig);
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_ALARM_MODE;
        int respType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_ALARM_MODE_RESP;

        antsCamera.sendP2PMessage(new P2PMessage(reqType, respType, data, new IMessageResponse() {
            @Override
            public boolean onResponse(byte[] data) {
                SMsgAVIoctrlDeviceInfoResp deviceInfo = SMsgAVIoctrlDeviceInfoResp.parse(data, isByteOrderBig);
                antsCamera.getCameraInfo().deviceInfo = deviceInfo;
                if(callback != null){
                callback.onResult(deviceInfo);
                }
                return true;
            }

            @Override
            public void onError(int error) {
                if (callback != null) {
                callback.onError(error);
            }
            }
        }));

    }

    /**
     * 设置报警情况下的灵敏度 0:high, 1:middle, 2:low
     */
    public void setAlarmSensitivity(int sensitivity, final OnCommandResponse<SMsgAVIoctrlDeviceInfoResp> callback) {
        byte[] data = AVIOCTRLDEFs.SMsAVIoctrlAlarmSensitivity.parseContent(sensitivity, isByteOrderBig);
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_ALARM_SENSITIVITY;
        int respType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_ALARM_SENSITIVITY_RESP;

        antsCamera.sendP2PMessage(new P2PMessage(reqType, respType, data, new IMessageResponse() {
            @Override
            public boolean onResponse(byte[] data) {
                SMsgAVIoctrlDeviceInfoResp deviceInfo = SMsgAVIoctrlDeviceInfoResp.parse(data, isByteOrderBig);
                antsCamera.getCameraInfo().deviceInfo = deviceInfo;
                if(callback != null){
                callback.onResult(deviceInfo);
                }
                return true;
            }

            @Override
            public void onError(int error) {
                if (callback != null) {
                callback.onError(error);
            }
            }
        }));
    }

    /**
     * 设置固件升级语音提示
     *
     * @param beepMode 0表示关闭，1表示打开
     * @param callback
     */
    public void setBeepMode(int beepMode, final OnCommandResponse<SMsgAVIoctrlDeviceInfoResp> callback) {
        AntsLog.d(TAG, "setBeepMode:" + beepMode);
        byte[] data = AVIOCTRLDEFs.SMsAVIoctrlBeepMode.parseContent(beepMode, isByteOrderBig);
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_BEEP;
        int respType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_BEEP_RESP;

        antsCamera.sendP2PMessage(new P2PMessage(reqType, respType, data, new IMessageResponse() {

            @Override
            public boolean onResponse(byte[] data) {
                SMsgAVIoctrlDeviceInfoResp deviceInfo = SMsgAVIoctrlDeviceInfoResp.parse(data, isByteOrderBig);
                antsCamera.getCameraInfo().deviceInfo = deviceInfo;
                if(callback != null){
                callback.onResult(deviceInfo);
                }
                return true;
            }

            @Override
            public void onError(int error) {
                if (callback != null) {
                    callback.onError(error);
                }
            }
        }));
    }

    /**
     * 设置语音通话时扬声器音量
     *
     * @param speakerVolume 0~100
     * @param callback
     */
    public void setSpeakerVolume(int speakerVolume, final OnCommandResponse<SMsgAVIoctrlDeviceInfoResp> callback) {
        AntsLog.d(TAG, "setSpeakerVolume:" + speakerVolume);
        byte[] data = AVIOCTRLDEFs.SMsAVIoctrlSpeakerVolume.parseContent(speakerVolume, isByteOrderBig);
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_SPEAKER_VOLUME;
        int respType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_SPEAKER_VOLUME_RESP;

        antsCamera.sendP2PMessage(new P2PMessage(reqType, respType, data, new IMessageResponse() {

            @Override
            public boolean onResponse(byte[] data) {
                SMsgAVIoctrlDeviceInfoResp deviceInfo = SMsgAVIoctrlDeviceInfoResp.parse(data, isByteOrderBig);
                antsCamera.getCameraInfo().deviceInfo = deviceInfo;
                if(callback != null){
                callback.onResult(deviceInfo);
                }
                return true;
            }

            @Override
            public void onError(int error) {
                if (callback != null) {
                    callback.onError(error);
                }
            }
        }));
    }


    /**
     * 设置摄像机本地录制的音量
     */
    public void setMicVolume(int micVolume, final OnCommandResponse<SMsgAVIoctrlDeviceInfoResp> callback) {
        AntsLog.d(TAG, "setMicVolume:" + micVolume);
        byte[] data = AVIOCTRLDEFs.SMsAVIoctrlMicVolume.parseContent(micVolume, isByteOrderBig);
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_MIC_VOLUME;
        int respType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_MIC_VOLUME_RESP;

        antsCamera.sendP2PMessage(new P2PMessage(reqType, respType, data, new IMessageResponse() {

            @Override
            public boolean onResponse(byte[] data) {
                SMsgAVIoctrlDeviceInfoResp deviceInfo = SMsgAVIoctrlDeviceInfoResp.parse(data, isByteOrderBig);
                antsCamera.getCameraInfo().deviceInfo = deviceInfo;
                if(callback != null){
                    callback.onResult(deviceInfo);
                }
                return true;
            }

            @Override
            public void onError(int error) {
                if (callback != null) {
                    callback.onError(error);
                }
            }
        }));
    }


    /**
     * 设置畸变校正模式，0:关闭；1:打开
     */
    public void setLdcMode(int mode, final OnCommandResponse<SMsgAVIoctrlDeviceInfoResp> callback) {
        byte[] data = AVIOCTRLDEFs.SMsAVIoctrlLdcCfg.parseContent(mode, isByteOrderBig);
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_LDC;
        int respType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_LDC_RESP;

        antsCamera.sendP2PMessage(new P2PMessage(reqType, respType, data, new IMessageResponse() {
            @Override
            public boolean onResponse(byte[] data) {
                SMsgAVIoctrlDeviceInfoResp deviceInfo = SMsgAVIoctrlDeviceInfoResp.parse(data, isByteOrderBig);
                antsCamera.getCameraInfo().deviceInfo = deviceInfo;
                if(callback != null){
                callback.onResult(deviceInfo);
                }
                return true;
            }

            @Override
            public void onError(int error) {
                if (callback != null) {
                callback.onError(error);
            }
            }
        }));
    }


    /**
     * 获取报警区域
     * open: 0 is close, 1 is open
     * <p/>
     * resolution:
     * 1:720*480(NTSC), 2:720*576(PAL), 3:320*240,
     * 4:640*360(VGA), 5:1280*720(720P), 6:1920*1080(1080P)
     * <p/>
     * (x1,y1)和(x2,y2)是以图像左上角为(0,0)坐标，x轴为width，y轴为height（注意y轴都是负数）
     */
    public void getMotionDetect(final OnCommandResponse<AVIOCTRLDEFs.SMsAVIoctrlMotionDetectCfg> callback) {
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_MOTION_DETECT;
        int respType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_MOTION_DETECT_RESP;

        antsCamera.sendP2PMessage(new P2PMessage(reqType, respType, new byte[8], new IMessageResponse() {
            @Override
            public boolean onResponse(byte[] data) {
                AVIOCTRLDEFs.SMsAVIoctrlMotionDetectCfg resp = new AVIOCTRLDEFs.SMsAVIoctrlMotionDetectCfg(data, isByteOrderBig);
                antsCamera.getCameraInfo().motionDetect = resp;
                if(callback != null){
                callback.onResult(resp);
                }
                return true;
            }

            @Override
            public void onError(int error) {
                if (callback != null) {
                callback.onError(error);
            }
            }
        }));
    }


    /**
     * 设置报警区域
     * open: 0 is close, 1 is open
     * <p/>
     * resolution:
     * 1:720*480(NTSC), 2:720*576(PAL), 3:320*240,
     * 4:640*360(VGA), 5:1280*720(720P), 6:1920*1080(1080P)
     * <p/>
     * (x1,y1)和(x2,y2)是以图像左上角为(0,0)坐标，x轴为width，y轴为height（注意y轴都是负数）
     */
    public void setMotionDetect(int open, int resolution, int x1, int y1, int x2, int y2,
                                final OnCommandResponse<AVIOCTRLDEFs.SMsAVIoctrlMotionDetectCfg> callback) {
        byte[] data = AVIOCTRLDEFs.SMsAVIoctrlMotionDetectCfg.parseContent(open, resolution, x1, y1, x2, y2, isByteOrderBig);
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_MOTION_DETECT;
        int respType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_MOTION_DETECT_RESP;

        antsCamera.sendP2PMessage(new P2PMessage(reqType, respType, data, new IMessageResponse() {
            @Override
            public boolean onResponse(byte[] data) {
                AVIOCTRLDEFs.SMsAVIoctrlMotionDetectCfg resp = new AVIOCTRLDEFs.SMsAVIoctrlMotionDetectCfg(data, isByteOrderBig);
                antsCamera.getCameraInfo().motionDetect = resp;
                if(callback != null){
                callback.onResult(resp);
                }
                return true;
            }

            @Override
            public void onError(int error) {
                if (callback != null) {
                callback.onError(error);
            }
            }
        }));
    }


    /**
     * 设置摄像机静默升级，enable, 1关闭，2打开
     * */
    public void setSilentUpgrade(int enable, final OnCommandResponse<SMsgAVIoctrlDeviceInfoResp> callback){
        AntsLog.d(TAG, "setSilentUpgrade:" + enable);
        byte[] data = AVIOCTRLDEFs.SMsAVIoctrlSilentUpgradeCfg.parseContent(enable, isByteOrderBig);
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_SILENT_UPGRADE;
        int respType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_SILENT_UPGRADE_RESP;

        antsCamera.sendP2PMessage(new P2PMessage(reqType, respType, data, new IMessageResponse() {
            @Override
            public boolean onResponse(byte[] data) {
                SMsgAVIoctrlDeviceInfoResp deviceInfo = SMsgAVIoctrlDeviceInfoResp.parse(data, isByteOrderBig);
                antsCamera.getCameraInfo().deviceInfo = deviceInfo;
                if(callback != null){
                    callback.onResult(deviceInfo);
                }
                return true;
            }

            @Override
            public void onError(int error) {
                if(callback != null){
                    callback.onError(error);
                }
            }
        }));

    }

    /**
     * 开启直播
     *
     * @param time
     * @param callback
     */
    public void startRtmpStream(int time,
                                final OnCommandResponse<SMsgAVIoctrlQueryRtmpStateResp> callback) {
        byte[] data = AVIOCTRLDEFs.SMsgAVIoctrlStartRtmpReq.parseContent(time, isByteOrderBig);
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_START_RTMP_REQ;
        int respType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_START_RTMP_RESP;

        antsCamera.sendP2PMessage(new P2PMessage(reqType, respType, data, new IMessageResponse() {

            @Override
            public boolean onResponse(byte[] data) {
                SMsgAVIoctrlQueryRtmpStateResp resp = SMsgAVIoctrlQueryRtmpStateResp.parse(data, isByteOrderBig);
                if (callback != null) {
                    callback.onResult(resp);
                }
                return true;
            }

            @Override
            public void onError(int error) {
                if (callback != null) {
                callback.onError(error);
            }
            }
        }));
    }

    /**
     * 开启直播,并设置url
     *
     * @param url
     * @param callback
     */
    public void startAndSetAddrRtmpStream(String url,
                                          final OnCommandResponse<SMsgAVIoctrlQueryRtmpStateResp> callback) {
        byte[] data = url.getBytes();
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_RTMP_ADDR_REQ;
        int respType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_RTMP_ADDR_RESP;

        antsCamera.sendP2PMessage(new P2PMessage(reqType, respType, data, new IMessageResponse() {

            @Override
            public boolean onResponse(byte[] data) {
                SMsgAVIoctrlQueryRtmpStateResp resp = SMsgAVIoctrlQueryRtmpStateResp.parse(data, isByteOrderBig);
                if (callback != null) {
                    callback.onResult(resp);
                }
                return true;
            }

            @Override
            public void onError(int error) {
                if (callback != null) {
                callback.onError(error);
            }
            }
        }));
    }

    /**
     * 关闭直播
     *
     * @param callback
     */
    public void stopRtmpStream(final OnCommandResponse<SMsgAVIoctrlQueryRtmpStateResp> callback) {
        byte[] data = new byte[8];
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_STOP_RTMP_REQ;
        int respType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_STOP_RTMP_REQ_RESP;
        antsCamera.sendP2PMessage(new P2PMessage(reqType, respType, data, new IMessageResponse() {

            @Override
            public boolean onResponse(byte[] data) {
                SMsgAVIoctrlQueryRtmpStateResp resp = SMsgAVIoctrlQueryRtmpStateResp.parse(data, isByteOrderBig);
                if (callback != null) {
                    callback.onResult(resp);
                }
                return true;
            }

            @Override
            public void onError(int error) {
                if (callback != null) {
                callback.onError(error);
            }
            }
        }));
    }

    /**
     * 查询直播状态
     *
     * @param callback
     */
    public void queryRtmpStream(final OnCommandResponse<SMsgAVIoctrlQueryRtmpStateResp> callback) {
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_QUERY_RTMP_STAT_REQ;
        int respType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_QUERY_RTMP_STAT_RESP;
        byte[] data = new byte[8];
        antsCamera.sendP2PMessage(new P2PMessage(reqType, respType, data, new IMessageResponse() {

            @Override
            public boolean onResponse(byte[] data) {
                SMsgAVIoctrlQueryRtmpStateResp resp = SMsgAVIoctrlQueryRtmpStateResp.parse(data, isByteOrderBig);
                if (callback != null) {
                    callback.onResult(resp);
                }
                return true;
            }

            @Override
            public void onError(int error) {
                if (callback != null) {
                callback.onError(error);
            }
            }
        }));
    }

    /**
     * 触发设备更新所有服务器的设置
     *
     * @param callback
     */
	public void triggerDeviceSyncInfoFromServer(int reserved, final OnCommandResponse<AVIOCTRLDEFs.SMsgAVIoctrlTriggerDeviceSyncResp> callback) {
        byte[] data = AVIOCTRLDEFs.SMsgAVIoctrlSmartIaModeReq.parseContent(reserved, this.isByteOrderBig);
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_TRIGGER_SYNC_INFO_FROM_SERVER_REQ;
        int respType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_TRIGGER_SYNC_INFO_FROM_SERVER_RESP;
        antsCamera.sendP2PMessage(new P2PMessage(reqType, respType, data, new IMessageResponse() {

            @Override
            public boolean onResponse(byte[] data) {
                AVIOCTRLDEFs.SMsgAVIoctrlTriggerDeviceSyncResp resp = AVIOCTRLDEFs.SMsgAVIoctrlTriggerDeviceSyncResp.parse(data, isByteOrderBig);
                if (callback != null) {
                    callback.onResult(resp);
                }
                return true;
            }

            @Override
            public void onError(int error) {
                if (callback != null) {
                    callback.onError(error);
                }
            }
        }));
    }



    /**
     * 切换wifi
     */
    public void switchWifi(int mode, final OnCommandResponse<AVIOCTRLDEFs.SMsgAVIoctrlCloudStorageResp> callback){
        byte[] data = AVIOCTRLDEFs.SMsgAVIoctrlSmartIaModeReq.parseContent(mode, this.isByteOrderBig);
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_WIFI_SWITCH;
        int respType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_WIFI_SWITCH_RESP ;
        antsCamera.sendP2PMessage(new P2PMessage(reqType, respType, data, new IMessageResponse() {
            @Override
            public boolean onResponse(byte[] data) {
                AVIOCTRLDEFs.SMsgAVIoctrlCloudStorageResp resp = AVIOCTRLDEFs.SMsgAVIoctrlCloudStorageResp.parse(data, isByteOrderBig);
                if (callback != null) {
                    callback.onResult(resp);
                }
                return true;
            }

            @Override
            public void onError(int error) {
                if (callback != null) {
                    callback.onError(error);
                }
            }
        }));
    }

    /**
     * 取IPC的变长的设备信息
     */
    public void getIpcInfo(final OnCommandResponse<AVIOCTRLDEFs.SMsAVIoctrlIpcInfoCfgResp> callback) {
        byte[] data = new byte[4];
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_IPC_INFO;
        int respType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_IPC_INFO_RESP;
        antsCamera.sendP2PMessage(new P2PMessage(reqType, respType, data, new IMessageResponse() {
            @Override
            public boolean onResponse(byte[] data) {
                AVIOCTRLDEFs.SMsAVIoctrlIpcInfoCfgResp info = AVIOCTRLDEFs.SMsAVIoctrlIpcInfoCfgResp.parse(data);
                antsCamera.getCameraInfo().ipcInfo = info;
                if (callback != null) {
                    callback.onResult(info);
                }
                return true;
            }

            @Override
            public void onError(int error) {
                if (callback != null) {
                callback.onError(error);
            }
            }
        }));

    }



    public void getNetworkCheck(final OnCommandResponse<String> callback){
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_TNP_NETWORK_CHECK;
        int respType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_TNP_NETWORK_CHECK_RESP;
        antsCamera.getNetworkInfo(new P2PMessage(reqType, respType, new byte[4], new IMessageResponse() {
            @Override
            public boolean onResponse(byte[] data) {
                String info = Packet.byteArrayToString(data, data.length);
                if(callback != null){
                    callback.onResult(info);
                }
                return true;
            }

            @Override
            public void onError(int error) {
                if (callback != null) {
                callback.onError(error);
            }
            }
        }));

    }

    public void getOnlineStatus(final OnCommandResponse<AVIOCTRLDEFs.SMsgAVIoctrlOnlineStatusResp> callback){
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_TNP_ONLINE_STATUS;
        int respType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_TNP_ONLINE_STATUS_RESP;
        antsCamera.getOnlineStatus(new P2PMessage(reqType, respType, new byte[4], new IMessageResponse() {
            @Override
            public boolean onResponse(byte[] data) {
                AVIOCTRLDEFs.SMsgAVIoctrlOnlineStatusResp resp = AVIOCTRLDEFs.SMsgAVIoctrlOnlineStatusResp.parse(data, isByteOrderBig);
                if(callback != null){
                    callback.onResult(resp);
                }
                return true;
            }

            @Override
            public void onError(int error) {
                if(callback != null){
                    callback.onError(error);
                }
            }
        }));
    }


    /**
     * 发送云台移动数据
     *
     * @param direction
     * @param speed
     */
    public void sendPanDirection(int direction, int speed) {
        AntsLog.d(TAG, "direction :" + direction + " speed : " + speed);
        byte[] data = AVIOCTRLDEFs.SMsgAVIoctrlPTZDireCTRL.parseContent(direction, speed, this.isByteOrderBig);
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_PTZ_DIRECTION_CTRL;
        antsCamera.sendP2PMessage(new P2PMessage(reqType, data));

    }


    /**
     * 增加云台预设点
     */
    public void addUserPtzPreset(final OnCommandResponse<AVIOCTRLDEFs.SMsgAVIoctrlPTZPresetGETResp> callback) {
        byte[] data = new byte[4];
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_PTZ_PRESET_ADD_REQ;
        int respType = AVIOCTRLDEFs.IOTYPE_USER_PTZ_PRESET_ADD_RESP;

        antsCamera.sendP2PMessage(new P2PMessage(reqType, respType, data, new IMessageResponse() {
            @Override
            public boolean onResponse(byte[] data) {
                AVIOCTRLDEFs.SMsgAVIoctrlPTZPresetGETResp info = AVIOCTRLDEFs.SMsgAVIoctrlPTZPresetGETResp.parse(data, isByteOrderBig);
                antsCamera.getCameraInfo().deviceInfo.presets = new ArrayList<>(Arrays.asList(info.presets));
                if (callback != null) {
                    callback.onResult(info);
                }
                return true;
            }

            @Override
            public void onError(int error) {
                callback.onError(error);
            }
        }));
    }

    /**
     * 删除云台预设点
     */
    public void delUserPtzPreset(int preset,  final OnCommandResponse<AVIOCTRLDEFs.SMsgAVIoctrlPTZPresetGETResp> callback){
        delUserPtzPreset(preset, true, callback);
    }

    /**
     * 删除云台预设点
     */
    public void delUserPtzPreset(int preset,  final boolean shouldEnd, final OnCommandResponse<AVIOCTRLDEFs.SMsgAVIoctrlPTZPresetGETResp> callback) {
        byte[] data = AVIOCTRLDEFs.SMsgAVIoctrlPTZPresetCall.parseContent(preset, this.isByteOrderBig);
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_PTZ_PRESET_DEL_REQ;
        int respType = AVIOCTRLDEFs.IOTYPE_USER_PTZ_PRESET_DEL_RESP;
        antsCamera.sendP2PMessage(new P2PMessage(reqType, respType, data, new IMessageResponse() {


            @Override
            public boolean onResponse(byte[] data) {
                AVIOCTRLDEFs.SMsgAVIoctrlPTZPresetGETResp info = AVIOCTRLDEFs.SMsgAVIoctrlPTZPresetGETResp.parse(data, isByteOrderBig);
                antsCamera.getCameraInfo().deviceInfo.presets = new ArrayList<>(Arrays.asList(info.presets));
                if (callback != null) {
                    callback.onResult(info);
                }
                return shouldEnd;
            }

            @Override
            public void onError(int error) {
                callback.onError(error);
            }
        }));
    }
    /**
     * 跳转预设点
     */

    public void callUserPtzPreset(int preset){
        byte[] data = AVIOCTRLDEFs.SMsgAVIoctrlPTZPresetCall.parseContent(preset, this.isByteOrderBig);
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_PTZ_PRESET_CALL;
        antsCamera.sendP2PMessage(new P2PMessage(reqType,data));
    }



    /**
     * 设置巡航模式和停留时间
     * IOTYPE_USER_PTZ_SET_CURISE_STAY_TIME
     */

    public void setPtzPresetModeAndTime(int mode, int sleep, final OnCommandResponse<byte[]> callback) {
        byte[] data = AVIOCTRLDEFs.SMsgAVIoctrlPTZCruiseModeAndTime.parseContent(mode, sleep, this.isByteOrderBig);
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_PTZ_SET_CURISE_STAY_TIME_REQ;
        int respType = AVIOCTRLDEFs.IOTYPE_USER_PTZ_SET_CURISE_STAY_TIME_RESP;
        antsCamera.sendP2PMessage(new P2PMessage(reqType, respType, data, new IMessageResponse() {

            @Override
            public boolean onResponse(byte[] data) {
                if (callback != null) {
                    callback.onResult(data);
                }
                return true;
            }

            @Override
            public void onError(int error) {
                callback.onError(error);
            }
        }));

    }




    /**
     * IOTYPE_USER_PTZ_HOME
     * 云台回原点
     */

    public void gotoPtzHome() {
        byte[] data = new byte[4];
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_PTZ_HOME;
        antsCamera.sendP2PMessage(new P2PMessage(reqType, data));
    }

    /**
     * IOTYPE_USER_PTZ_CTRL_STOP
     * 云台方向操作停止
     */
    public void stopPtzCtrl() {
        AntsLog.d(TAG, "stopPtzCtrl SUCCESS");

        byte[] data = new byte[4];
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_PTZ_CTRL_STOP;
        antsCamera.sendP2PMessage(new P2PMessage(reqType, data));
    }

    /**
     * 转移到某坐标
     */

    public void moveToPoint(int transverseProportion, int longitudinalProportion) {

        AntsLog.d(TAG, "transverseProportion : " + transverseProportion + " longitudinalProportion : " + longitudinalProportion);

        byte[] data = AVIOCTRLDEFs.SMsgAVIoctrlPTZJumpPointSet.parseContent(transverseProportion, longitudinalProportion, this.isByteOrderBig);
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_PTZ_JUMP_TO_POINT;
        antsCamera.sendP2PMessage(new P2PMessage(reqType, data));

    }


    /**
     * 巡航时间段设置（单位：秒）
     *
     */
    public void setPTZCruisePeriod(int startTime, int endTime, final OnCommandResponse<byte[]> callback){
        byte[] data = AVIOCTRLDEFs.SMsgAVIoctrlPTZCruisePeriod.parseContent(startTime,endTime,isByteOrderBig);
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_PTZ_SET_CRUISE_PERIOD_REQ;
        int respType = AVIOCTRLDEFs.IOTYPE_USER_PTZ_SET_CRUISE_PERIOD_RESP;
        antsCamera.sendP2PMessage(new P2PMessage(reqType, respType, data, new IMessageResponse() {
            @Override
            public boolean onResponse(byte[] data) {
                callback.onResult(data);
                return true;
            }

            @Override
            public void onError(int error) {
                callback.onError(error);

            }
        }));
    }



    /**
     * IOTYPE_USER_PTZ_CRUISE_START
     * 设置巡航状态开关
     */
    public void setPtzCruiseState(boolean on, final OnCommandResponse<byte[]> callback) {
        byte[] data = Packet.intToByteArray(on?1:0, isByteOrderBig);
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_SET_PTZ_CRUISE_REQ;
        int respType = AVIOCTRLDEFs.IOTYPE_USER_SET_PTZ_CRUISE_RESP;
        antsCamera.sendP2PMessage(new P2PMessage(reqType, respType, data, new IMessageResponse() {
            @Override
            public boolean onResponse(byte[] data) {
                callback.onResult(data);
                return true;
            }

            @Override
            public void onError(int error) {
                callback.onError(error);
            }
        }));
    }




    /**
     * 设置移动跟踪开关 （0关 1开）
     */

    public void setMotionTrackState(boolean on, final OnCommandResponse<byte[]> callback){
        byte[] data = Packet.intToByteArray(on?1:0, isByteOrderBig);
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_PTZ_SET_MOTION_TRACK_REQ;
        int respType = AVIOCTRLDEFs.IOTYPE_USER_PTZ_SET_MOTION_TRACK_RESP;
        antsCamera.sendP2PMessage(new P2PMessage(reqType, respType, data, new IMessageResponse() {
            @Override
            public boolean onResponse(byte[] data) {
                callback.onResult(data);
                return true;
            }

            @Override
            public void onError(int error) {
                callback.onError(error);

            }
        }));
    }

    //查询pgc直播状态
    public void checkPGCLiveState(int mode, final OnCommandResponse<Integer> callback){
        byte[] data = Packet.intToByteArray(mode, this.isByteOrderBig);
        short reqType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_PGC_LIVE;
        short respType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_PGC_LIVE_RESP;
        this.antsCamera.sendP2PMessage(new P2PMessage(reqType, respType, data, new IMessageResponse() {
            public boolean onResponse(byte[] data) {
                int result = Packet.byteArrayToInt(data, 0, CameraCommandHelper.this.isByteOrderBig);
                callback.onResult(Integer.valueOf(result));
                return true;
            }

            public void onError(int error) {
                callback.onError(error);
            }
        }));
    }



    public void startPanoramaCapture(final OnCommandResponse<Integer> callback){
        byte[] data = new byte[4];
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_PANORAMA_CAPTURE_START;
        int respType = AVIOCTRLDEFs.IOTYPE_USER_PANORAMA_CAPTURE_START_RSP;
        antsCamera.sendP2PMessage(new P2PMessage(reqType, respType, data, new IMessageResponse() {
            @Override
            public boolean onResponse(byte[] data) {
                int result = Packet.byteArrayToInt(data, 0, CameraCommandHelper.this.isByteOrderBig);
                callback.onResult(Integer.valueOf(result));
                return true;
            }

            @Override
            public void onError(int error) {
                callback.onError(error);
            }
        }));
    }

    public void stopPanoramaCapture(){
        byte[] data = new byte[4];
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_PANORAMA_CAPTURE_ABROT;
        antsCamera.sendP2PMessage(new P2PMessage(reqType, data));
    }

    public void pollingPanoramaCapture(final OnCommandResponse<AVIOCTRLDEFs.SMsgAVIoctrlPanoramaCaptureScheduleResp> callback){
        byte[] data = new byte[4];
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_PANORAMA_CAPTURE_SCHEDULE_POLLING;
        int respType = AVIOCTRLDEFs.IOTYPE_USER_PANORAMA_CAPTURE_SCHEDULE_POLLING_RSP;

        antsCamera.sendP2PMessage(new P2PMessage(reqType, respType, data, new IMessageResponse() {
            @Override
            public boolean onResponse(byte[] data) {
                if(callback != null){
                    AVIOCTRLDEFs.SMsgAVIoctrlPanoramaCaptureScheduleResp resp = AVIOCTRLDEFs.SMsgAVIoctrlPanoramaCaptureScheduleResp.parse(data, isByteOrderBig);
                    callback.onResult(resp);
                }
                return true;
            }

            @Override
            public void onError(int error) {
                if(callback != null){
                    callback.onError(error);
                }
            }
        }));


    }

    /**
     * 设置声音侦测报警灵敏度
     *
     * @param soundSensitivity
     * @param callback
     */
    public void adjustSoundSensitivity(int soundSensitivity, final OnCommandResponse<SMsgAVIoctrlDeviceInfoResp> callback) {
        AntsLog.d(TAG, "adjustSoundSensitivity:" + soundSensitivity);
        byte[] data = Packet.intToByteArray(soundSensitivity, this.isByteOrderBig);
        int reqType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_ABS_SENSITIVITY;
        int respType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_ABS_SENSITIVITY_RESP;
        antsCamera.sendP2PMessage(new P2PMessage(reqType, respType, data, new IMessageResponse() {
            @Override
            public boolean onResponse(byte[] data) {
                SMsgAVIoctrlDeviceInfoResp deviceInfo = SMsgAVIoctrlDeviceInfoResp.parse(data, isByteOrderBig);
                antsCamera.getCameraInfo().deviceInfo = deviceInfo;
                if (callback != null) {
                    callback.onResult(deviceInfo);
                }
                return true;
            }

            @Override
            public void onError(int error) {
                if (callback != null) {
                    callback.onError(error);
                }
            }
        }));
    }


}
