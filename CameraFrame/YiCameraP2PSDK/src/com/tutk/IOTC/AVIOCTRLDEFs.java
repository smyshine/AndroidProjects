package com.tutk.IOTC;

import android.text.TextUtils;

import com.xiaoyi.log.AntsLog;

import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

public class AVIOCTRLDEFs {
    public static final int IOTYPE_USER_IPCAM_RESP                              = 0xFFFF; //通用回复指令

    public static final int AVIOCTRL_RECORD_PLAY_START                          = 0x10;
    public static final int AVIOCTRL_RECORD_PLAY_STOP                           = 0x1;

    public static final int IOTYPE_USER_IPCAM_HEART                             = 0x6E; // 心跳

    public static final int IOTYPE_USER_IPCAM_AUDIOSTART                        = 0x300; // 开始音频
    public static final int IOTYPE_USER_IPCAM_AUDIOSTOP                         = 0x301; // 关闭音频

    public static final int IOTYPE_USER_IPCAM_DEVINFO_REQ                       = 0x330; // 设备信息
    public static final int IOTYPE_USER_IPCAM_DEVINFO_RESP                      = 0x331;

    public static final int IOTYPE_USER_IPCAM_LISTEVENT_REQ                     = 0x318; // 获取历史录像列表
    public static final int IOTYPE_USER_IPCAM_LISTEVENT_RESP                    = 0x319;

    public static final int IOTYPE_USER_IPCAM_TNP_EVENT_LIST_REQ                = 0x2347; // TNP获取历史录像列表
    public static final int IOTYPE_USER_IPCAM_TNP_EVENT_LIST_RESP               = 0x2348;

    public static final int IOTYPE_USER_IPCAM_RECORD_PLAYCONTROL                = 0x31A; // 播放历史视频控制
    public static final int IOTYPE_USER_IPCAM_RECORD_PLAYCONTROL2               = 0x31AE; // 播放历史视频控制，需要加密
    public static final int IOTYPE_USER_IPCAM_RECORD_PLAYCONTROL_RESP           = 0x31B;

    public static final int IOTYPE_USER_IPCAM_SPEAKERSTART                      = 0x350; // 开启对讲反向通道
    public static final int IOTYPE_USER_IPCAM_SPEAKERSTOP                       = 0x351;

    public static final int IOTYPE_USER_IPCAM_TRIGGER_SYNC_INFO_FROM_SERVER_REQ = 0x3c0; // 触发设备去服务器更新所有开关参数
    public static final int IOTYPE_USER_IPCAM_TRIGGER_SYNC_INFO_FROM_SERVER_RESP = 0x3c1;

    public static final int IOTYPE_USER_IPCAM_STOP_CLOUD_STORAGE_REQ            = 0x3c2; // 停止云存储模式
    public static final int IOTYPE_USER_IPCAM_STOP_CLOUD_STORAGE_REQ_RESP       = 0x3c3;

    public static final int IOTYPE_USER_IPCAM_START                             = 0x1FF; // 开始直播视频
    public static final int IOTYPE_USER_IPCAM_START2                            = 0x1FFE;// 开始直播视频,需要加密
    public static final int IOTYPE_USER_IPCAM_START_RESP                        = 0x200;

    public static final int IOTYPE_USER_IPCAM_TNP_START_REALTIME                = 0x2345; // 开始直播视频（带分辨率）
    public static final int IOTYPE_USER_IPCAM_TNP_START_RECORD                  = 0x2346; // 开始播放历史（带分辨率）

    public static final int IOTYPE_USER_TNP_IPCAM_KICK                          = 0x2349;

    public static final int IOTYPE_USER_IPCAM_STOP                              = 0x2FF; // 关闭直播视频


    public static final int IOTYPE_USER_IPCAM_UPDATE_CHECK_PHONE_REQ            = 0x1300; // 查看固件版本
    public static final int IOTYPE_USER_IPCAM_UPDATE_CHECK_PHONE_RSP            = 0x1301;

    public static final int IOTYPE_USER_IPCAM_UPDATE_PHONE_REQ                  = 0x1302; // 固件版本升级
    public static final int IOTYPE_USER_IPCAM_UPDATE_PHONE_RSP                  = 0x130F;

    public static final int IOTYPE_USER_IPCAM_CANCEL_UPDATE_PHONE_REQ           = 0x1308; // 取消固件升级
    public static final int IOTYPE_USER_IPCAM_CANCEL_UPDATE_PHONE_RESP          = 0x130E;

    public static final int IOTYPE_USER_IPCAM_REBOOT_PHONE_REQ                  = 0x1309; // 重新启动
    public static final int IOTYPE_USER_IPCAM_REBOOT_PHONE_RESP                 = 0x1310; // 重新启动

    public static final int IOTYPE_USER_IPCAM_RECVICE_ALARMINFO                 = 0x1FFF; // 报警信息

    public static final int IOTYPE_USER_IPCAM_SET_RESOLUTION                    = 0x1311; // 0=自适应;1=720p;2=vga;3=cif
    public static final int IOTYPE_USER_IPCAM_SET_RESOLUTION_RESP               = 0x1312;

    public static final int IOTYPE_USER_IPCAM_GET_RESOLUTION                    = 0x1313; // 0=自适应;1=720p;2=vga;3=cif
    public static final int IOTYPE_USER_IPCAM_GET_RESOLUTION_RESP               = 0x1314;

    public static final int IOTYPE_USER_IPCAM_SET_BEEP                          = 0x1331; //提示音开关：beepmode  0关闭1开启
    public static final int IOTYPE_USER_IPCAM_SET_BEEP_RESP                     = 0x1332;

    public static final int IOTYPE_USER_IPCAM_SET_SPEAKER_VOLUME                = 0x1333; // 设置speaker音量： speaker_volume [0 ~ 100]
    public static final int IOTYPE_USER_IPCAM_SET_SPEAKER_VOLUME_RESP           = 0x1334;

    public static final int IOTYPE_USER_IPCAM_SET_MIC_VOLUME                    = 0x133b; // 设置mic的音量
    public static final int IOTYPE_USER_IPCAM_SET_MIC_VOLUME_RESP               = 0x133c;


    public static final int IOTYPE_USER_IPCAM_SET_RECORD_MODE                   = 0x130a; // 设置移动侦测录制
    public static final int IOTYPE_USER_IPCAM_SET_RECORD_MODE_RESP              = 0x130b;

    public static final int IOTYPE_USER_IPCAM_CLOSE_CAMERA_REQ                  = 0x1304; // 关闭直播
    public static final int IOTYPE_USER_IPCAM_CLOSE_CAMERA_RESP                 = 0x130C;

    public static final int IOTYPE_USER_IPCAM_CLOSE_LIGHT_REQ                   = 0x1305; // 关闭指示灯
    public static final int IOTYPE_USER_IPCAM_CLOSE_LIGHT_RESP                  = 0x130D;

    public static final int IOTYPE_USER_IPCAM_SET_TF_FORMAT                     = 0x1315; // 格式化TF卡, 参考SMsgAVIoctrlTfFormate结构
    public static final int IOTYPE_USER_IPCAM_SET_TF_FORMAT_RESP                = 0x1316;

    public static final int IOTYPE_USER_IPCAM_START_CHECK                       = 0x1317; // 重新检测设备 /*不需要带结构*/
    public static final int IOTYPE_USER_IPCAM_START_CHECK_RESP                  = 0x1318;

    public static final int IOTYPE_USER_IPCAM_CHECK_STAT_REQ                    = 0x1319; /* 不需要带结构 */
    public static final int IOTYPE_USER_IPCAM_CHECK_STAT_REQ_RESP               = 0x131a; /* 返回DeviceInfo的结构体 */

    
    public static final int IOTYPE_USER_IPCAM_SET_MIRROR_FLIP                   = 0x131f; //设置视频翻转
    public static final int IOTYPE_USER_IPCAM_SET_MIRROR_FLIP_PESP              = 0x1320;


    public static final int IOTYPE_USER_IPCAM_IFRAME_PIECES_REQ                 = 0x1219; // 分片

    public static final int IOTYPE_USER_IPCAM_START_RTMP_REQ                    = 0x3b0; // 开启RTMP直播
    public static final int IOTYPE_USER_IPCAM_START_RTMP_RESP                   = 0x3b1;

    public static final int IOTYPE_USER_IPCAM_STOP_RTMP_REQ                     = 0x3b2; // 停止RTMP直播
    public static final int IOTYPE_USER_IPCAM_STOP_RTMP_REQ_RESP                = 0x3b3;

    public static final int IOTYPE_USER_IPCAM_QUERY_RTMP_STAT_REQ               = 0x3b4; // P2p查询当前RTMP直播情况
    public static final int IOTYPE_USER_IPCAM_QUERY_RTMP_STAT_RESP              = 0x3b5;

    public static final int IOTYPE_USER_IPCAM_SET_RTMP_ADDR_REQ                 = 0x3b6; // 设置地址
    public static final int IOTYPE_USER_IPCAM_SET_RTMP_ADDR_RESP                = 0x3b7;


    public static final int IOTYPE_USER_TRIGER_TIME_ZONE_REQ                    = 0x2000; //请求设备更新时区


    public static final int IOTYPE_USER_IPCAM_SET_DAYNIGHT_MODE                 = 0x1321; // 设置日夜转换模式
    public static final int IOTYPE_USER_IPCAM_SET_DAYNIGHT_MODE_RESP            = 0x1322;

    public static final int IOTYPE_USER_IPCAM_SET_MOTION_DETECT                 = 0x1325; // 设置区域报警
    public static final int IOTYPE_USER_IPCAM_SET_MOTION_DETECT_RESP            = 0x1326;

    public static final int IOTYPE_USER_IPCAM_GET_MOTION_DETECT                 = 0x1327; // 获取区域报警设置
    public static final int IOTYPE_USER_IPCAM_GET_MOTION_DETECT_RESP            = 0x1328;

    public static final int IOTYPE_USER_IPCAM_SET_HD_RESOLUTION                 = 0x1329; // 设置高清分辨率是720P或者1080P
    public static final int IOTYPE_USER_IPCAM_SET_HD_RESOLUTION_RESP            = 0x132a;

    public static final int IOTYPE_USER_IPCAM_SET_ROI                           = 0x132b; // 区域放大增强效果
    public static final int IOTYPE_USER_IPCAM_SET_ROI_RESP                      = 0x132c;

    public static final int IOTYPE_USER_IPCAM_SET_ALARM_MODE                    = 0x132d; // 报警模式设置
    public static final int IOTYPE_USER_IPCAM_SET_ALARM_MODE_RESP               = 0x132e;

    public static final int IOTYPE_USER_IPCAM_SET_LDC                           = 0x132f; // 畸变校正设置
    public static final int IOTYPE_USER_IPCAM_SET_LDC_RESP                      = 0x1330;


    public static final int IOTYPE_USER_IPCAM_SET_ALARM_SENSITIVITY             = 0x1335; // 设置报警灵敏度
    public static final int IOTYPE_USER_IPCAM_SET_ALARM_SENSITIVITY_RESP        = 0x1336;

    public static final int IOTYPE_USER_IPCAM_SET_RECORD_SPEED                  = 0x1337; //设置视频播放模式
    public static final int IOTYPE_USER_IPCAM_SET_RECORD_SPEED_RESP             = 0x1338;

    public static final int IOTYPE_USER_IPCAM_GET_RECORD_SPEED                  = 0x1339; //获取视频播放模式
    public static final int IOTYPE_USER_IPCAM_GET_RECORD_SPEED_RESP             = 0x133a;

    public static final int IOTYPE_USER_IPCAM_SET_TF_UMOUNT                     = 0x133d; //请求卸载sdcard
    public static final int IOTYPE_USER_IPCAM_SET_TF_UMOUNT_RESP                = 0x133e;

    public static final int IOTYPE_USER_IPCAM_SET_BABY_CRYING_MODE              = 0x133f; //宝宝哭泣
    public static final int IOTYPE_USER_IPCAM_SET_BABY_CRYING_MODE_RESP         = 0x1340;

    public static final int IOTYPE_USER_IPCAM_SET_VER_RECOVER                   = 0x1341; //版本回退命令
    public static final int IOTYPE_USER_IPCAM_SET_VER_RECOVER_RESP              = 0x1342;

    public static final int IOTYPE_USER_IPCAM_GET_PRE_VERSION                   = 0x1343; //获取前一个版本号命令
    public static final int IOTYPE_USER_IPCAM_GET_PRE_VERSION_RESP              = 0x1344;

    public static final int IOTYPE_USER_IPCAM_SET_SMART_IA_MODE                 = 0x1345; //智能交互命令
    public static final int IOTYPE_USER_IPCAM_SET_SMART_IA_MODE_RESP            = 0x1346;

    public static final int IOTYPE_USER_IPCAM_SET_MOTION_RECT_ROI_MODE          = 0x1347; //报警区域ROI
    public static final int IOTYPE_USER_IPCAM_SET_MOTION_RECT_ROI_MODE_RESP     = 0x1348;

    public static final int IOTYPE_USER_IPCAM_SET_UPLOAD_LOG                    = 0x1349; //固件上传log日志
    public static final int IOTYPE_USER_IPCAM_SET_UPLOAD_LOG_RESP               = 0x134a;

    public static final int IOTYPE_USER_IPCAM_GET_IPC_INFO                      = 0x134b; //取2代固件的硬件信息
    public static final int IOTYPE_USER_IPCAM_GET_IPC_INFO_RESP                 = 0x134c;

    public static final int IOTYPE_USER_IPCAM_SET_SILENT_UPGRADE                = 0x134f;
    public static final int IOTYPE_USER_IPCAM_SET_SILENT_UPGRADE_RESP           = 0x1350;

    public static final int IOTYPE_USER_IPCAM_SET_WIFI_SWITCH                   = 0x1351;
    public static final int IOTYPE_USER_IPCAM_SET_WIFI_SWITCH_RESP              = 0x1352; //切换wifi



    //小米路由转存

    public static final int IOTYPE_USER_IPCAM_GET_VIDEO_BACKUP                  =  0x1355;
    public static final int IOTYPE_USER_IPCAM_GET_VIDEO_BACKUP_RESP             =  0x1356;

    //pgc直播
    public static final int IOTYPE_USER_IPCAM_SET_PGC_LIVE                      =  0x1359;
    public static final int IOTYPE_USER_IPCAM_SET_PGC_LIVE_RESP                 =  0x135A;


    //云台相关指令
    public static final int IOTYPE_USER_PTZ_DIRECTION_CTRL                      = 0x4012; // 云台方向操作
    public static final int IOTYPE_USER_PTZ_CTRL_STOP                           = 0x4013; // 云台方向操作停止
    public static final int IOTYPE_USER_PTZ_HOME                                = 0x4014; // 云台回原点
    public static final int IOTYPE_USER_PTZ_JUMP_TO_POINT                       = 0x4015; // 跳转到某点


    public static final int IOTYPE_USER_SET_PTZ_CRUISE_REQ                      = 0x4010; // 巡航开关设置
    public static final int IOTYPE_USER_SET_PTZ_CRUISE_RESP                     = 0x4011;


    public static final int IOTYPE_USER_PTZ_PRESET_ADD_REQ                      = 0x4000; // 增加预置点
    public static final int IOTYPE_USER_PTZ_PRESET_ADD_RESP                     = 0x4001;
    public static final int IOTYPE_USER_PTZ_PRESET_DEL_REQ                      = 0x4002; // 删除预置点
    public static final int IOTYPE_USER_PTZ_PRESET_DEL_RESP                     = 0x4003;

    public static final int IOTYPE_USER_PTZ_PRESET_CALL                         = 0x4006; // 跳转预置点


    public static final int IOTYPE_USER_PTZ_SET_CURISE_STAY_TIME_REQ            = 0x4007; // 设置巡航模式和停留时间
    public static final int IOTYPE_USER_PTZ_SET_CURISE_STAY_TIME_RESP           = 0x4008;

    public static final int IOTYPE_USER_PTZ_SET_CRUISE_PERIOD_REQ               = 0x4009; // 巡航时间段设置
    public static final int IOTYPE_USER_PTZ_SET_CRUISE_PERIOD_RESP              = 0x400a;

    public static final int IOTYPE_USER_PTZ_SET_MOTION_TRACK_REQ                = 0x400b; // 移动跟踪开关设置
    public static final int IOTYPE_USER_PTZ_SET_MOTION_TRACK_RESP               = 0x400c;


    public static final int IOTYPE_USER_PANORAMA_CAPTURE_START		            = 0x4020; // 开始全景抓拍
    public static final int IOTYPE_USER_PANORAMA_CAPTURE_START_RSP		        = 0x4021;

    public static final int IOTYPE_USER_PANORAMA_CAPTURE_ABROT		            = 0x4022; // 中止全景抓拍
    public static final int IOTYPE_USER_PANORAMA_CAPTURE_ABROT_RSP              = 0x4023;

    public static final int IOTYPE_USER_PANORAMA_CAPTURE_SCHEDULE_POLLING	    = 0x4024; // 全景抓拍进度轮循
    public static final int IOTYPE_USER_PANORAMA_CAPTURE_SCHEDULE_POLLING_RSP   = 0x4025;



    // TNP网络检测功能模拟指令发送的过程
    public static final int IOTYPE_USER_IPCAM_TNP_NETWORK_CHECK                 = 0xEE01;
    public static final int IOTYPE_USER_IPCAM_TNP_NETWORK_CHECK_RESP            = 0xEE02;

    // TNP检查设备在线状态模拟指令发送的过程
    public static final int IOTYPE_USER_IPCAM_TNP_ONLINE_STATUS                 = 0xEE03;
    public static final int IOTYPE_USER_IPCAM_TNP_ONLINE_STATUS_RESP            = 0XEE04;

    public static final int IOTYPE_USER_IPCAM_SET_ABS_SENSITIVITY = 0x135d; // 声音侦测灵敏度设置
    public static final int IOTYPE_USER_IPCAM_SET_ABS_SENSITIVITY_RESP = 0x135e;




    public static class SAvEvent {
        public STimeDay utctime;
        public byte event;
        public byte status;
        public short duration;

        public SAvEvent(byte[] data, int offset, boolean isByteOrderBig) {
            utctime = new STimeDay(data, offset, isByteOrderBig);
            event = data[8 + offset];
            status = data[9 + offset];
            duration = Packet.byteArrayToShort(data, 10 + offset, isByteOrderBig);
        }

        public static int getTotalSize() {
            return 12;
        }

        public void updateEvent(SAvEvent sevent) {
            utctime = sevent.utctime;
            event = sevent.event;
            status = sevent.status;
            duration = sevent.duration;
        }
    }

    public static class SFrameInfo {
        byte cam_index;
        short codec_id;
        byte flags;
        byte onlineNum;
        byte[] reserved = new byte[3];
        int reserved2;
        long timestamp;

        public static byte[] parseContent(short paramShort, byte paramByte1, byte paramByte2,
                                          byte paramByte3, long paramLong, boolean isByteOrderBig) {
            byte[] arrayOfByte = new byte[16];
            System.arraycopy(Packet.shortToByteArray(paramShort, isByteOrderBig), 0, arrayOfByte, 0, 2);
            arrayOfByte[2] = paramByte1;
            arrayOfByte[3] = paramByte2;
            arrayOfByte[4] = paramByte3;
            System.arraycopy(Packet.longToByteArray(paramLong, isByteOrderBig), 0, arrayOfByte, 12, 4);// long
            // to
            // unsign
            // int
            return arrayOfByte;
        }

        public static long createAudioTimestamp(long lTimestampKey) {
            final long UNSIGNINT_KEYLIMIT = 214748365; // (20*UNSIGNINT_KEYLIMIT) > (2<<31);

            if (lTimestampKey >= UNSIGNINT_KEYLIMIT) {
                lTimestampKey -= UNSIGNINT_KEYLIMIT;
            }

            long lTimestamp = 20 * lTimestampKey;
            // System.currentTimeMillis();
            return lTimestamp;
        }
    }

    public static class SMsgAVIoctrlAVStream {
        int channel = 0;
        byte[] reserved = new byte[4];

        public static byte[] parseContent(int paramInt, boolean isByteOrderBig) {
            byte[] arrayOfByte = new byte[8];
            System.arraycopy(Packet.intToByteArray(paramInt, isByteOrderBig), 0, arrayOfByte, 0, 4);
            return arrayOfByte;
        }
    }

    public static class SMsgAVIoctrlCurrentFlowInfo {
        public int channel;
        public int elapse_time_ms;
        public int lost_incomplete_frame_count;
        public int total_actual_frame_size;
        public int total_expected_frame_size;
        public int total_frame_count;

        public static byte[] parseContent(int paramInt1, int paramInt2, int paramInt3,
                                          int paramInt4, int paramInt5, int paramInt6, boolean isByteOrderBig) {
            byte[] arrayOfByte = new byte[32];
            System.arraycopy(Packet.intToByteArray(paramInt1, isByteOrderBig), 0, arrayOfByte, 0, 4);
            System.arraycopy(Packet.intToByteArray(paramInt2, isByteOrderBig), 0, arrayOfByte, 4, 4);
            System.arraycopy(Packet.intToByteArray(paramInt3, isByteOrderBig), 0, arrayOfByte, 8, 4);
            System.arraycopy(Packet.intToByteArray(paramInt4, isByteOrderBig), 0, arrayOfByte, 12, 4);
            System.arraycopy(Packet.intToByteArray(paramInt5, isByteOrderBig), 0, arrayOfByte, 16, 4);
            System.arraycopy(Packet.intToByteArray(paramInt6, isByteOrderBig), 0, arrayOfByte, 20, 4);
            return arrayOfByte;
        }
    }

    public static class SMsgAVIoctrlUpgradeDeviceVersionReq {
        static byte[] reserved = new byte[4];

        public static byte[] parseContent(String url) {
            if ((url == null) || (url.length() == 0)) {
                return null;
            }

            url = url.trim();
            if ((url == null) || (url.length() == 0)) {
                return null;
            }

            byte[] buffer = new byte[url.length() + 4];
            System.arraycopy(url.getBytes(), 0, buffer, 0, url.length());
            return buffer;
        }
    }


    public static class SMsgAVIoctrlDeviceVersionReq {
        static byte[] reserved = new byte[4];

        public static byte[] parseContent() {
            return reserved;
        }
    }

    public static class SMsgAVIoctrlDeviceInfoReq {
        static byte[] reserved = new byte[4];

        public static byte[] parseContent() {
            return reserved;
        }
    }

    public static class SMsgAVIoctrlDeviceReboot {
        static byte[] reserved = new byte[4];

        public static byte[] parseContent() {
            return reserved;
        }
    }

    public static class SMsgAVIoctrlTfFormate {
        static int flag;

        public static byte[] parseContent(boolean isByteOrderBig) {
            byte[] arrayOfByte = new byte[4];
            System.arraycopy(Packet.intToByteArray(flag, isByteOrderBig), 0, arrayOfByte, 0, 4);

            return arrayOfByte;
        }
    }

    public class SMsgAVIoctrlEvent {
        int channel;
        int event;
        byte[] reserved = new byte[4];
        AVIOCTRLDEFs.STimeDay stTime;

        public SMsgAVIoctrlEvent() {
        }
    }

    public class SMsgAVIoctrlEventConfig {
        long channel;
        byte ftp;
        byte localIO;
        byte mail;
        byte p2pPushMsg;

        public SMsgAVIoctrlEventConfig() {
        }
    }

    public static class SMsgAVIoctrlFormatExtStorageReq {
        byte[] reserved = new byte[4];
        int storage;

        public static byte[] parseContent(int paramInt, boolean isByteOrderBig) {
            byte[] arrayOfByte = new byte[8];
            System.arraycopy(Packet.intToByteArray(paramInt, isByteOrderBig), 0, arrayOfByte, 0, 4);
            return arrayOfByte;
        }
    }

    public class SMsgAVIoctrlFormatExtStorageResp {
        byte[] reserved = new byte[3];
        byte result;
        int storage;

        public SMsgAVIoctrlFormatExtStorageResp() {
        }
    }

    public static class SMsgAVIoctrlGetAudioOutFormatReq {
        public static byte[] parseContent() {
            return new byte[8];
        }
    }

    public class SMsgAVIoctrlGetAudioOutFormatResp {
        public int channel;
        public int format;

        public SMsgAVIoctrlGetAudioOutFormatResp() {
        }
    }

    public static class SMsgAVIoctrlGetEnvironmentReq {
        int channel;
        byte[] reserved = new byte[4];

        public static byte[] parseContent(int paramInt, boolean isByteOrderBig) {
            byte[] arrayOfByte = new byte[8];
            System.arraycopy(Packet.intToByteArray(paramInt, isByteOrderBig), 0, arrayOfByte, 0, 4);
            return arrayOfByte;
        }
    }

    public class SMsgAVIoctrlGetEnvironmentResp {
        int channel;
        byte mode;
        byte[] reserved = new byte[3];

        public SMsgAVIoctrlGetEnvironmentResp() {
        }
    }

    public static class SMsgAVIoctrlGetFlowInfoReq {
        public int channel;
        public int collect_interval;
    }

    public static class SMsgAVIoctrlGetFlowInfoResp {
        public int channel;
        public int collect_interval;

        public static byte[] parseContent(int paramInt1, int paramInt2, boolean isByteOrderBig) {
            byte[] arrayOfByte = new byte[8];
            System.arraycopy(Packet.intToByteArray(paramInt1, isByteOrderBig), 0, arrayOfByte, 0, 4);
            System.arraycopy(Packet.intToByteArray(paramInt2, isByteOrderBig), 0, arrayOfByte, 4, 4);
            return arrayOfByte;
        }
    }

    public static class SMsgAVIoctrlGetMotionDetectReq {
        int channel;
        byte[] reserved = new byte[4];

        public static byte[] parseContent(int paramInt, boolean isByteOrderBig) {
            byte[] arrayOfByte = new byte[8];
            System.arraycopy(Packet.intToByteArray(paramInt, isByteOrderBig), 0, arrayOfByte, 0, 4);
            return arrayOfByte;
        }
    }

    public class SMsgAVIoctrlGetMotionDetectResp {
        int channel;
        int sensitivity;

        public SMsgAVIoctrlGetMotionDetectResp() {
        }
    }

    public class SMsgAVIoctrlGetRcdDurationReq {
        int channel;
        byte[] reserved = new byte[4];

        public SMsgAVIoctrlGetRcdDurationReq() {
        }
    }

    public class SMsgAVIoctrlGetRcdDurationResp {
        int channel;
        int durasecond;
        int presecond;

        public SMsgAVIoctrlGetRcdDurationResp() {
        }
    }

    public static class SMsgAVIoctrlGetRecordReq {
        int channel;
        byte[] reserved = new byte[4];

        public static byte[] parseContent(int channel, int type, boolean isByteOrderBig) {
            byte[] arrayOfByte = new byte[8];
            System.arraycopy(Packet.intToByteArray(channel, isByteOrderBig), 0, arrayOfByte, 0, 4);
            System.arraycopy(Packet.intToByteArray(type, isByteOrderBig), 0, arrayOfByte, 4, 4);
            return arrayOfByte;
        }
    }

    public class SMsgAVIoctrlGetRecordResp {
        int channel;
        int recordType;

        public SMsgAVIoctrlGetRecordResp() {
        }
    }

    public static class SMsgAVIoctrlGetStreamCtrlReq {
        int channel;
        byte[] reserved = new byte[4];

        public static byte[] parseContent(int paramInt, boolean isByteOrderBig) {
            byte[] arrayOfByte = new byte[8];
            System.arraycopy(Packet.intToByteArray(paramInt, isByteOrderBig), 0, arrayOfByte, 0, 4);
            return arrayOfByte;
        }
    }

    public class SMsgAVIoctrlGetStreamCtrlResp {
        int channel;
        byte quality;
        byte[] reserved = new byte[3];

        public SMsgAVIoctrlGetStreamCtrlResp() {
        }
    }

    public static class SMsgAVIoctrlGetSupportStreamReq {
        public static int getContentSize() {
            return 4;
        }

        public static byte[] parseContent() {
            return new byte[4];
        }
    }

    public class SMsgAVIoctrlGetSupportStreamResp {
        public AVIOCTRLDEFs.SStreamDef[] mStreamDef;
        public long number;

        public SMsgAVIoctrlGetSupportStreamResp() {
        }
    }

    public static class SMsgAVIoctrlGetVideoModeReq {
        int channel;
        byte[] reserved = new byte[4];

        public static byte[] parseContent(int paramInt, boolean isByteOrderBig) {
            byte[] arrayOfByte = new byte[8];
            System.arraycopy(Packet.intToByteArray(paramInt, isByteOrderBig), 0, arrayOfByte, 0, 4);
            return arrayOfByte;
        }
    }

    public class SMsgAVIoctrlGetVideoModeResp {
        int channel;
        byte mode;
        byte[] reserved = new byte[3];

        public SMsgAVIoctrlGetVideoModeResp() {
        }
    }

    public static class SMsgAVIoctrlGetWifiReq {
        static byte[] reserved = new byte[4];

        public static byte[] parseContent() {
            return reserved;
        }
    }

    public class SMsgAVIoctrlGetWifiResp {
        byte enctype;
        byte mode;
        byte[] password = new byte[32];
        byte signal;
        byte[] ssid = new byte[32];
        byte status;

        public SMsgAVIoctrlGetWifiResp() {
        }
    }

    public static class SMsgAVIoctrlListEventReq {
        int channel;
        byte[] endutctime = new byte[8];
        byte event;
        byte[] reversed = new byte[2];
        byte[] startutctime = new byte[8];
        byte status;

        public static byte[] parseConent(int channel, long startTime, long endTime,
                                         byte type, byte status, boolean isByteOrderBig) {
            Calendar startCal = Calendar.getInstance(TimeZone.getTimeZone("gmt"));
            Calendar endCal = Calendar.getInstance(TimeZone.getTimeZone("gmt"));
            startCal.setTimeInMillis(startTime);
            endCal.setTimeInMillis(endTime);
            byte[] data = new byte[24];
            System.arraycopy(Packet.intToByteArray(channel, isByteOrderBig), 0, data, 0, 4);
            System.arraycopy(AVIOCTRLDEFs.STimeDay.parseContent(startCal.get(1),
                    1 + startCal.get(2), startCal.get(5), startCal.get(7), startCal.get(11),
                    startCal.get(12), 0, isByteOrderBig), 0, data, 4, 8);
            System.arraycopy(
                    AVIOCTRLDEFs.STimeDay.parseContent(endCal.get(1), 1 + endCal.get(2),
                            endCal.get(5), endCal.get(7), endCal.get(11), endCal.get(12), 0, isByteOrderBig), 0,
                    data, 12, 8);
            data[20] = type;
            data[21] = status;
            return data;
        }

        public static byte[] parseConent(AVIOCTRLDEFs.STimeDay start, AVIOCTRLDEFs.STimeDay end, boolean isByteOrderBig) {

            byte[] data = new byte[24];
            System.arraycopy(Packet.intToByteArray(0, isByteOrderBig), 0, data, 0, 4);
            System.arraycopy(start.getContent(), 0, data, 4, 8);
            System.arraycopy(end.getContent(), 0, data, 12, 8);
            data[20] = 1;
            data[21] = 1;
            return data;
        }
    }

    public static class SMsgAVIoctrlListEventResp {
        public int channel; // Camera Index
        public int total; // Total event amount in this search session
        public byte index; // package index, 0,1,2...;
        // because avSendIOCtrl() send package up to 1024
        // bytes one time, you may want split search results
        // to serveral package to send.
        public byte endflag; // end flag; endFlag = 1 means this package is the
        // last one.
        public byte count; // how much events in this package
        public byte reserved;
        public AVIOCTRLDEFs.SAvEvent[] stEvent;

        public SMsgAVIoctrlListEventResp(byte[] data, boolean isByteOrderBig) {
            channel = Packet.byteArrayToInt(data, 0, isByteOrderBig);
            total = Packet.byteArrayToInt(data, 4, isByteOrderBig);
            index = data[8];
            endflag = data[9];
            count = data[10];
            reserved = data[11];
            if (data.length >= (12 + count * SAvEvent.getTotalSize())) {
                stEvent = new AVIOCTRLDEFs.SAvEvent[count];
                for (int i = 0; i < count; i++) {
                    stEvent[i] = new AVIOCTRLDEFs.SAvEvent(data, 12 + i * SAvEvent.getTotalSize(), isByteOrderBig);
                }
            }
        }
    }

    public static class SAvTnpEvent {
        public static final int SIZE = 12;

        public STimeDay starttime;
        public int duration;

        public SAvTnpEvent(byte[] data, int offset, boolean isByteOrderBig) {
            starttime = new STimeDay(data, offset, isByteOrderBig);
            duration = Packet.byteArrayToInt(data, 8 + offset, isByteOrderBig);
        }
    }

    public static class SMsgAVIoctrlTnpListEventResp {
        public static final int HEAD_SIZE = 12;

        public short eventCount;
        public byte[] reserved = new byte[10];
        public SAvTnpEvent[] avTnpEvent;

        public SMsgAVIoctrlTnpListEventResp(byte[] data, boolean isByteOrderBig) {
            eventCount = Packet.byteArrayToShort(data, 0, isByteOrderBig);
            if (data.length >= (HEAD_SIZE + eventCount * SAvTnpEvent.SIZE)) {
                avTnpEvent = new SAvTnpEvent[eventCount];
                for (int i = 0; i < eventCount; i++) {
                    avTnpEvent[i] = new SAvTnpEvent(data, 12 + i * SAvTnpEvent.SIZE, isByteOrderBig);
                }
            } else {
                eventCount = 0;
            }

        }

    }


    public static class SMsgAVIoctrlListWifiApReq {
        static byte[] reserved = new byte[4];

        public static byte[] parseContent() {
            return reserved;
        }
    }

    public class SMsgAVIoctrlListWifiApResp {
        int number;
        AVIOCTRLDEFs.SWifiAp stWifiAp;

        public SMsgAVIoctrlListWifiApResp() {
        }
    }

    public static class SMsgAVIoctrlPlayRecord {
        int Param;
        int channel;
        int command;
        byte[] reserved = new byte[4];
        byte[] stTimeDay = new byte[8];

        public static byte[] parseContent(int paramInt1, int paramInt2, int paramInt3,
                                          long paramLong, boolean isByteOrderBig) {
            byte[] arrayOfByte = new byte[24];
            System.arraycopy(Packet.intToByteArray(paramInt1, isByteOrderBig), 0, arrayOfByte, 0, 4);
            System.arraycopy(Packet.intToByteArray(paramInt2, isByteOrderBig), 0, arrayOfByte, 4, 4);
            System.arraycopy(Packet.intToByteArray(paramInt3, isByteOrderBig), 0, arrayOfByte, 8, 4);
            Calendar localCalendar = Calendar.getInstance(TimeZone.getTimeZone("gmt"));
            localCalendar.setTimeInMillis(paramLong);
            localCalendar.add(5, -1);
            localCalendar.add(5, 1);
            System.arraycopy(AVIOCTRLDEFs.STimeDay.parseContent(localCalendar.get(1),
                    localCalendar.get(2), localCalendar.get(5), localCalendar.get(7),
                    localCalendar.get(11), localCalendar.get(12), localCalendar.get(13), isByteOrderBig), 0,
                    arrayOfByte, 12, 8);
            return arrayOfByte;
        }

        public static byte[] parseContent(int channel, int command, int userParam, byte[] timeBuf,
                                          byte useCount, boolean isByteOrderBig) {
            byte[] arrayOfByte = new byte[24];
            System.arraycopy(Packet.intToByteArray(channel, isByteOrderBig), 0, arrayOfByte, 0, 4);
            System.arraycopy(Packet.intToByteArray(command, isByteOrderBig), 0, arrayOfByte, 4, 4);
            System.arraycopy(Packet.intToByteArray(userParam, isByteOrderBig), 0, arrayOfByte, 8, 4);
            System.arraycopy(timeBuf, 0, arrayOfByte, 12, 8);
            arrayOfByte[20] = useCount;
            return arrayOfByte;
        }
    }

    public static class SMsgAVIoctrlPlayRecordResp {
        int channel;
        byte[] reserved = new byte[4];
        int result;

        public SMsgAVIoctrlPlayRecordResp() {
        }

        public SMsgAVIoctrlPlayRecordResp(byte[] data) {
        }
    }

    public static class SMsgAVIoctrlTnpPlayRecord {
        byte usecount;
        byte resolution;
        byte version;
        byte[] reserved = new byte[1];
        STimeDay recordTime;

        public static byte[] parseContent(byte usecount, byte resolution, byte version, long timestamp, boolean isByteOrderBig) {
            STimeDay currentPlaySTimeDay = new STimeDay(timestamp, isByteOrderBig);

            byte[] arrayOfByte = new byte[12];
            arrayOfByte[0] = usecount;
            arrayOfByte[1] = resolution;
            arrayOfByte[2] = version;
            System.arraycopy(currentPlaySTimeDay.toByteArray(), 0, arrayOfByte, 4, 8);
            return arrayOfByte;
        }
    }

    public static class SMsgAVIoctrlTnpPlay {
        byte usecount;
        byte resolution;
        byte version;
        byte[] reserved = new byte[1];

        public static byte[] parseContent(byte usecount, byte resolution, byte version) {
            byte[] arrayOfByte = new byte[4];
            arrayOfByte[0] = usecount;
            arrayOfByte[1] = resolution;
            arrayOfByte[2] = version;
            return arrayOfByte;
        }
    }

    public static class SMsgAVIoctrlPtzCmd {
        byte aux;
        byte channel;
        byte control;
        byte limit;
        byte point;
        byte[] reserved = new byte[2];
        byte speed;

        public static byte[] parseContent(byte paramByte1, byte paramByte2, byte paramByte3,
                                          byte paramByte4, byte paramByte5, byte paramByte6) {
            byte[] arrayOfByte = new byte[8];
            arrayOfByte[0] = paramByte1;
            arrayOfByte[1] = paramByte2;
            arrayOfByte[2] = paramByte3;
            arrayOfByte[3] = paramByte4;
            arrayOfByte[4] = paramByte5;
            arrayOfByte[5] = paramByte6;
            return arrayOfByte;
        }
    }

    public static class SMsgAVIoctrlSetEnvironmentReq {
        int channel;
        byte mode;
        byte[] reserved = new byte[3];

        public static byte[] parseContent(int paramInt, byte paramByte, boolean isByteOrderBig) {
            byte[] arrayOfByte = new byte[8];
            System.arraycopy(Packet.intToByteArray(paramInt, isByteOrderBig), 0, arrayOfByte, 0, 4);
            arrayOfByte[4] = paramByte;
            return arrayOfByte;
        }
    }

    public class SMsgAVIoctrlSetEnvironmentResp {
        int channel;
        byte[] reserved = new byte[3];
        byte result;

        public SMsgAVIoctrlSetEnvironmentResp() {
        }
    }

    public static class SMsgAVIoctrlSetMotionDetectReq {
        int channel;
        int sensitivity;

        public static byte[] parseContent(int paramInt1, int paramInt2, boolean isByteOrderBig) {
            byte[] arrayOfByte1 = new byte[8];
            byte[] arrayOfByte2 = Packet.intToByteArray(paramInt1, isByteOrderBig);
            byte[] arrayOfByte3 = Packet.intToByteArray(paramInt2, isByteOrderBig);
            System.arraycopy(arrayOfByte2, 0, arrayOfByte1, 0, 4);
            System.arraycopy(arrayOfByte3, 0, arrayOfByte1, 4, 4);
            return arrayOfByte1;
        }
    }

    public class SMsgAVIoctrlSetMotionDetectResp {
        byte[] reserved = new byte[3];
        byte result;

        public SMsgAVIoctrlSetMotionDetectResp() {
        }
    }

    public static class SMsgAVIoctrlSetPasswdReq {
        byte[] newPasswd = new byte[32];
        byte[] oldPasswd = new byte[32];

        public static byte[] parseContent(String paramString1, String paramString2) {
            byte[] arrayOfByte1 = paramString1.getBytes();
            byte[] arrayOfByte2 = paramString2.getBytes();
            byte[] arrayOfByte3 = new byte[64];
            System.arraycopy(arrayOfByte1, 0, arrayOfByte3, 0, arrayOfByte1.length);
            System.arraycopy(arrayOfByte2, 0, arrayOfByte3, 32, arrayOfByte2.length);
            return arrayOfByte3;
        }
    }

    public class SMsgAVIoctrlSetPasswdResp {
        byte[] reserved = new byte[3];
        byte result;

        public SMsgAVIoctrlSetPasswdResp() {
        }
    }

    public class SMsgAVIoctrlSetRcdDurationReq {
        int channel;
        int durasecond;
        int presecond;

        public SMsgAVIoctrlSetRcdDurationReq() {
        }
    }

    public class SMsgAVIoctrlSetRcdDurationResp {
        byte[] reserved = new byte[3];
        byte result;

        public SMsgAVIoctrlSetRcdDurationResp() {
        }
    }

    public static class SMsgAVIoctrlSetRecordReq {
        int channel;
        int recordType;
        byte[] reserved = new byte[4];

        public static byte[] parseContent(int paramInt1, int paramInt2, boolean isByteOrderBig) {
            byte[] arrayOfByte1 = new byte[12];
            byte[] arrayOfByte2 = Packet.intToByteArray(paramInt1, isByteOrderBig);
            byte[] arrayOfByte3 = Packet.intToByteArray(paramInt2, isByteOrderBig);
            System.arraycopy(arrayOfByte2, 0, arrayOfByte1, 0, 4);
            System.arraycopy(arrayOfByte3, 0, arrayOfByte1, 4, 4);
            return arrayOfByte1;
        }
    }

    public class SMsgAVIoctrlSetRecordResp {
        byte[] reserved = new byte[3];
        byte result;

        public SMsgAVIoctrlSetRecordResp() {
        }
    }

    public static class SMsgAVIoctrlSetStreamCtrlReq {
        int channel;
        byte quality;
        byte[] reserved = new byte[3];

        public static byte[] parseContent(int paramInt, byte paramByte, boolean isByteOrderBig) {
            byte[] arrayOfByte = new byte[8];
            System.arraycopy(Packet.intToByteArray(paramInt, isByteOrderBig), 0, arrayOfByte, 0, 4);
            arrayOfByte[4] = paramByte;
            return arrayOfByte;
        }
    }

    public class SMsgAVIoctrlSetStreamCtrlResp {
        byte[] reserved = new byte[4];
        int result;

        public SMsgAVIoctrlSetStreamCtrlResp() {
        }
    }

    public static class SMsgAVIoctrlSetVideoModeReq {
        int channel;
        byte mode;
        byte[] reserved = new byte[3];

        public static byte[] parseContent(int paramInt, byte paramByte, boolean isByteOrderBig) {
            byte[] arrayOfByte = new byte[8];
            System.arraycopy(Packet.intToByteArray(paramInt, isByteOrderBig), 0, arrayOfByte, 0, 4);
            arrayOfByte[4] = paramByte;
            return arrayOfByte;
        }
    }

    public class SMsgAVIoctrlSetVideoModeResp {
        int channel;
        byte[] reserved = new byte[3];
        byte result;

        public SMsgAVIoctrlSetVideoModeResp() {
        }
    }

    public static class SMsgAVIoctrlSetWifiReq {
        byte enctype;
        byte mode;
        byte[] password = new byte[32];
        byte[] reserved = new byte[10];
        byte[] ssid = new byte[32];

        public static byte[] parseContent(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2,
                                          byte paramByte1, byte paramByte2) {
            byte[] arrayOfByte = new byte[76];
            System.arraycopy(paramArrayOfByte1, 0, arrayOfByte, 0, paramArrayOfByte1.length);
            System.arraycopy(paramArrayOfByte2, 0, arrayOfByte, 32, paramArrayOfByte2.length);
            arrayOfByte[64] = paramByte1;
            arrayOfByte[65] = paramByte2;
            return arrayOfByte;
        }
    }

    public class SMsgAVIoctrlSetWifiResp {
        byte[] reserved = new byte[3];
        byte result;

        public SMsgAVIoctrlSetWifiResp() {
        }
    }

    public static class SMsgAVIoctrlTimeZone {
        public int cbSize;
        public int nGMTDiff;
        public int nIsSupportTimeZone;
        public byte[] szTimeZoneString = new byte[256];

        public static byte[] parseContent() {
            return new byte[268];
        }

        public static byte[] parseContent(int paramInt1, int paramInt2, int paramInt3,
                                          byte[] paramArrayOfByte, boolean isByteOrderBig) {
            byte[] arrayOfByte = new byte[268];
            System.arraycopy(Packet.intToByteArray(paramInt1, isByteOrderBig), 0, arrayOfByte, 0, 4);
            System.arraycopy(Packet.intToByteArray(paramInt2, isByteOrderBig), 0, arrayOfByte, 4, 4);
            System.arraycopy(Packet.intToByteArray(paramInt3, isByteOrderBig), 0, arrayOfByte, 8, 4);
            System.arraycopy(paramArrayOfByte, 0, arrayOfByte, 12, paramArrayOfByte.length);
            return arrayOfByte;
        }

        // 时区差异
        public static byte[] parseContent(int i, boolean isByteOrderBig) {
            byte[] zoneOffset = new byte[4];
            System.arraycopy(Packet.intToByteArray(i, isByteOrderBig), 0, zoneOffset, 0, 4);
            return zoneOffset;
        }

    }

    public static class SMsAVIoctrlDayNightMode {
        int mode;
        STimeDay day2night;
        STimeDay night2day;
        int[] placeholder = new int[3];

        public static byte[] parseContent(int mode, long day2night, long night2day, boolean isByteOrderBig) {
            Calendar startCal = Calendar.getInstance(TimeZone.getTimeZone("gmt"));
            Calendar endCal = Calendar.getInstance(TimeZone.getTimeZone("gmt"));
            startCal.setTimeInMillis(day2night);
            endCal.setTimeInMillis(night2day);
            byte[] data = new byte[32];
            System.arraycopy(Packet.intToByteArray(mode, isByteOrderBig), 0, data, 0, 4);
            System.arraycopy(AVIOCTRLDEFs.STimeDay.parseContent(startCal.get(1),
                    1 + startCal.get(2), startCal.get(5), startCal.get(7), startCal.get(11),
                    startCal.get(12), 0, isByteOrderBig), 0, data, 4, 8);
            System.arraycopy(AVIOCTRLDEFs.STimeDay.parseContent(endCal.get(1),
                    1 + endCal.get(2), endCal.get(5), endCal.get(7), endCal.get(11),
                    endCal.get(12), 0, isByteOrderBig), 0, data, 12, 8);
            return data;
        }
    }

    public static class SMsAVIoctrlHDResolutionCfg {
        int resolution;

        public static byte[] parseContent(int hd_resolution, boolean isByteOrderBig) {
            return Packet.intToByteArray(hd_resolution, isByteOrderBig);
        }
    }

    public static class SMsAVIoctrlROICfg {
        /**
         * 1:720*480(NTSC), 2:720*576(PAL),
         * 3:320*240, 4:640*360(VGA),
         * 5:1280*720(720P), 6:1920*1080(1080P)
         */
        int resolution;
        int strength;
        int use_count;
        int top_left_x;
        int top_left_y;
        int bottom_right_x;
        int bottom_right_y;

        public static byte[] parseContent(int resolution, int strength, int use_count, int x1, int y1, int x2, int y2, boolean isByteOrderBig) {
            byte[] arrayOfByte = new byte[28];
            System.arraycopy(Packet.intToByteArray(resolution, isByteOrderBig), 0, arrayOfByte, 0, 4);
            System.arraycopy(Packet.intToByteArray(strength, isByteOrderBig), 0, arrayOfByte, 4, 4);
            System.arraycopy(Packet.intToByteArray(use_count, isByteOrderBig), 0, arrayOfByte, 8, 4);
            System.arraycopy(Packet.intToByteArray(x1, isByteOrderBig), 0, arrayOfByte, 12, 4);
            System.arraycopy(Packet.intToByteArray(y1, isByteOrderBig), 0, arrayOfByte, 16, 4);
            System.arraycopy(Packet.intToByteArray(x2, isByteOrderBig), 0, arrayOfByte, 20, 4);
            System.arraycopy(Packet.intToByteArray(y2, isByteOrderBig), 0, arrayOfByte, 24, 4);
            return arrayOfByte;
        }
    }

    public static class SMsAVIoctrlAlarmMode {
        int mode;

        public static byte[] parseContent(int mode, boolean isByteOrderBig) {
            return Packet.intToByteArray(mode, isByteOrderBig);
        }
    }

    public static class SMsAVIoctrlLdcCfg {
        int mode;

        public static byte[] parseContent(int mode, boolean isByteOrderBig) {
            return Packet.intToByteArray(mode, isByteOrderBig);
        }
    }

    public static class SMsAVIoctrlMotionDetectCfg {
        public int open; // 0 is close, 1 is open
        /**
         * 1:720*480(NTSC), 2:720*576(PAL),
         * 3:320*240, 4:640*360(VGA),
         * 5:1280*720(720P), 6:1920*1080(1080P)
         */
        public int resolution;
        public int top_left_x;
        public int top_left_y;
        public int bottom_right_x;
        public int bottom_right_y;

        public static byte[] parseContent(int open, int resolution, int x1, int y1, int x2, int y2, boolean isByteOrderBig) {
            byte[] arrayOfByte = new byte[24];
            System.arraycopy(Packet.intToByteArray(open, isByteOrderBig), 0, arrayOfByte, 0, 4);
            System.arraycopy(Packet.intToByteArray(resolution, isByteOrderBig), 0, arrayOfByte, 4, 4);
            System.arraycopy(Packet.intToByteArray(x1, isByteOrderBig), 0, arrayOfByte, 8, 4);
            System.arraycopy(Packet.intToByteArray(y1, isByteOrderBig), 0, arrayOfByte, 12, 4);
            System.arraycopy(Packet.intToByteArray(x2, isByteOrderBig), 0, arrayOfByte, 16, 4);
            System.arraycopy(Packet.intToByteArray(y2, isByteOrderBig), 0, arrayOfByte, 20, 4);
            return arrayOfByte;
        }

        public SMsAVIoctrlMotionDetectCfg(byte[] data, boolean isByteOrderBig) {
            open = Packet.byteArrayToInt(data, 0, isByteOrderBig);
            resolution = Packet.byteArrayToInt(data, 4, isByteOrderBig);
            top_left_x = Packet.byteArrayToInt(data, 8, isByteOrderBig);
            top_left_y = Packet.byteArrayToInt(data, 12, isByteOrderBig);
            bottom_right_x = Packet.byteArrayToInt(data, 16, isByteOrderBig);
            bottom_right_y = Packet.byteArrayToInt(data, 20, isByteOrderBig);
        }

    }


    public static class SMsAVIoctrlVideoBackupGetResp {
        public  byte   is_mi_router;              /* 是否是小米路由器, 0: 不是小米路由器（或者小米路由器接口判断失败） 1: 是小米路由器  */
        public  byte   is_sd;                     /* 是否有sd卡,  0: 没有SD卡或者SD卡状态不正常，  1: 有SD卡且状态正常   （有SD卡才支持路由器转存功能） */
        public  byte   enable;                    /*0: 关闭，1：打开*/
        public  byte   resolution;                /*0: HD,   1：SD*/
        public  byte   backup_period;             /*0: 一天循环  1：一周循环  2：一个月循环  3：无限制直至剩余1G空间  */
        public  byte   user_path;                 /*0: 路由盘，  1: 扩展盘*/
        public  byte[] pad = new byte[2];
        public  int    router_sd_total_size;      /*路由盘总空间，类型：int , 单位：M，APP换算显示M/G/T**/
        public  int    router_sd_free_size;       /*路由盘可用空间，类型：int , 单位：M，APP换算显示M/G/T*/
        public  int    router_sd_cam_used_size;   /*路由盘摄像机视频已占用空间，类型：int , 单位：M，APP换算显示M/G/T*/
        public  int    extra_sd_total_size;       /*扩展盘总空间，类型：int , 单位：M，APP换算显示M/G/T**/
        public  int    extra_sd_free_size;        /*扩展盘可用空间，类型：int , 单位：M，APP换算显示M/G/T*/
        public  int    extra_sd_cam_used_size;    /*扩展盘摄像机视频已占用空间，类型：int , 单位：M，APP换算显示M/G/T*/
        public  byte[] reserved = new byte[20];


        public static SMsAVIoctrlVideoBackupGetResp parse(byte[] data, boolean isByteOrderBig) {
            SMsAVIoctrlVideoBackupGetResp resp = new SMsAVIoctrlVideoBackupGetResp();
            resp.is_mi_router            = data[0];
            resp.is_sd                   = data[1];
            resp.enable                  = data[2];
            resp.resolution              = data[3];
            resp.backup_period           = data[4];
            resp.user_path               = data[5];
            resp.pad[0]                  = data[6];
            resp.pad[1]                  = data[7];
            resp.router_sd_total_size    = Packet.byteArrayToInt(data, 8, isByteOrderBig);
            resp.router_sd_free_size     = Packet.byteArrayToInt(data, 12, isByteOrderBig);
            resp.router_sd_cam_used_size = Packet.byteArrayToInt(data, 16, isByteOrderBig);
            resp.extra_sd_total_size     = Packet.byteArrayToInt(data, 20, isByteOrderBig);
            resp.extra_sd_free_size      = Packet.byteArrayToInt(data, 24, isByteOrderBig);
            resp.extra_sd_cam_used_size  = Packet.byteArrayToInt(data, 28, isByteOrderBig);
            return resp;
        }

    }


    public static class SMsAVIoctrlAlarmSensitivity {
        int sensitivity; /* 0:high, 1:middle, 2:low */

        public static byte[] parseContent(int sensitivity, boolean isByteOrderBig) {
            return Packet.intToByteArray(sensitivity, isByteOrderBig);
        }
    }

    public static class SMsAVIoctrlBeepMode {
        int beepMode; // 0表示关闭，1表示打开

        public static byte[] parseContent(int beepMode, boolean isByteOrderBig) {
            return Packet.intToByteArray(beepMode, isByteOrderBig);
        }
    }

    public static class SMsAVIoctrlSpeakerVolume {
        int speakerVolume; // 0~100音量设置

        public static byte[] parseContent(int speakerVolume, boolean isByteOrderBig) {
            return Packet.intToByteArray(speakerVolume, isByteOrderBig);
        }
    }

    public static class SMsAVIoctrlMicVolume {
        int micVolume; // 0~100音量设置

        public static byte[] parseContent(int micVolume, boolean isByteOrderBig) {
            return Packet.intToByteArray(micVolume, isByteOrderBig);
        }
    }


    public static class SStreamDef {
        public int channel;
        public int index;

        public SStreamDef(byte[] paramArrayOfByte, boolean isByteOrderBig) {
            this.index = Packet.byteArrayToShort(paramArrayOfByte, 0, isByteOrderBig);
            this.channel = Packet.byteArrayToShort(paramArrayOfByte, 2, isByteOrderBig);
            // Log.i("SStreamDef", "index:"+index+",channel:"+channel);
        }

        public String toString() {
            return "CH" + String.valueOf(1 + this.index);
        }
    }

    public static class STimeDay implements Serializable {
        /**
         *
         */
        private static final long serialVersionUID = 1L;
        public byte day;
        public byte hour;
        private byte[] mBuf = new byte[8];
        public byte minute;
        public byte month;
        public byte second;
        public byte wday;
        public short year;
        long millis;

        public boolean isByteOrderBig;

        public STimeDay(STimeDay time) {
            this.year = time.year;
            this.month = time.month;
            this.day = time.day;
            this.wday = time.wday;
            this.hour = time.hour;
            this.minute = time.minute;
            this.second = time.second;
            updateTimeInMillis();
        }

        public STimeDay(int year, int month, int day, int hour, int minute, int second, boolean isByteOrderBig) {
            this.year = (short) year;
            this.month = (byte) month;
            this.day = (byte) day;
            this.hour = (byte) hour;
            this.minute = (byte) minute;
            this.second = (byte) second;
            this.isByteOrderBig = isByteOrderBig;
            mBuf = parseContent(this.year, this.month, this.day, this.wday, this.hour, this.minute,
                    this.second, this.isByteOrderBig);
            updateTimeInMillis();

            GregorianCalendar startCal = new GregorianCalendar(TimeZone.getDefault());
            startCal.setTimeInMillis(millis);
            this.wday = (byte) startCal.get(Calendar.DAY_OF_WEEK);
        }

        public STimeDay(byte[] buf, boolean isByteOrderBig) {
            System.arraycopy(buf, 0, this.mBuf, 0, 8);
            this.year = Packet.byteArrayToShort(buf, 0, isByteOrderBig);
            this.month = buf[2];
            this.day = buf[3];
            this.wday = buf[4];
            this.hour = buf[5];
            this.minute = buf[6];
            this.second = buf[7];
            updateTimeInMillis();
            this.isByteOrderBig = isByteOrderBig;
        }

        public STimeDay(byte[] buf, int offset, boolean isByteOrderBig) {
            System.arraycopy(buf, offset, this.mBuf, 0, 8);
            this.year = Packet.byteArrayToShort(mBuf, 0, isByteOrderBig);
            this.month = mBuf[2];
            this.day = mBuf[3];
            this.wday = mBuf[4];
            this.hour = mBuf[5];
            this.minute = mBuf[6];
            this.second = mBuf[7];
            updateTimeInMillis();
            this.isByteOrderBig = isByteOrderBig;
        }

        public STimeDay(long time, boolean isByteOrderBig) {
            // GregorianCalendar startCal = new GregorianCalendar(
            // TimeZone.getTimeZone("GMT"));
            GregorianCalendar startCal = new GregorianCalendar(TimeZone.getDefault());
            startCal.setTimeInMillis(time);
            this.year = (short) startCal.get(Calendar.YEAR);
            this.month = (byte) (1 + startCal.get(Calendar.MONTH));
            this.day = (byte) startCal.get(Calendar.DAY_OF_MONTH);
            this.wday = (byte) startCal.get(Calendar.DAY_OF_WEEK);
            this.hour = (byte) startCal.get(Calendar.HOUR_OF_DAY);
            this.minute = (byte) startCal.get(Calendar.MINUTE);
            this.second = (byte) startCal.get(Calendar.SECOND);
            this.isByteOrderBig = isByteOrderBig;
            mBuf = parseContent(this.year, this.month, this.day, this.wday, this.hour, this.minute,
                    this.second, isByteOrderBig);
            updateTimeInMillis();
        }

        public void updateCurrentTime(long timeseconds) {
            GregorianCalendar startCal = new GregorianCalendar(TimeZone.getDefault());
            startCal.setTimeInMillis(timeseconds * 1000);
            this.year = (short) startCal.get(Calendar.YEAR);
            this.month = (byte) (1 + startCal.get(Calendar.MONTH));
            this.day = (byte) startCal.get(Calendar.DAY_OF_MONTH);
            this.wday = (byte) startCal.get(Calendar.DAY_OF_WEEK);
            this.hour = (byte) startCal.get(Calendar.HOUR_OF_DAY);
            this.minute = (byte) startCal.get(Calendar.MINUTE);
            this.second = (byte) startCal.get(Calendar.SECOND);
            // mBuf = parseContent(this.year, this.month, this.day, this.wday,
            // this.hour, this.minute, this.second);
            updateTimeInMillis();
        }

        public STimeDay(int year, int month, int day, int wday, int hour, int minute, int second, boolean isByteOrderBig) {
            this.year = (short) year;
            this.month = (byte) month;
            this.day = (byte) day;
            this.wday = (byte) wday;
            this.hour = (byte) hour;
            this.minute = (byte) minute;
            this.second = (byte) second;
            mBuf = parseContent(this.year, this.month, this.day, this.wday, this.hour, this.minute,
                    this.second, isByteOrderBig);
            updateTimeInMillis();
        }

        public byte[] getContent() {
            mBuf = parseContent(this.year, this.month, this.day, this.wday, this.hour, this.minute,
                    this.second, this.isByteOrderBig);
            return mBuf;
        }

        public static byte[] parseContent(int year, int month, int day, int wday, int hour,
                                          int minute, int second, boolean isByteOrderBig) {
            byte[] buf = new byte[8];
            System.arraycopy(Packet.shortToByteArray((short) year, isByteOrderBig), 0, buf, 0, 2);
            buf[2] = ((byte) month);
            buf[3] = ((byte) day);
            buf[4] = ((byte) wday);
            buf[5] = ((byte) hour);
            buf[6] = ((byte) minute);
            buf[7] = ((byte) second);
            return buf;
        }

        // public String toString(){
        // Calendar localCalendar = Calendar.getInstance(TimeZone
        // .getTimeZone("gmt"));
        // localCalendar.setTimeInMillis(getTimeInMillis());
        // SimpleDateFormat localSimpleDateFormat = new
        // SimpleDateFormat("HH:mm:ss");
        // return localSimpleDateFormat.format(localCalendar.getTime());
        // }

        public String getLocalFormatString(String format) {
            Calendar localCalendar = Calendar.getInstance(TimeZone.getDefault());
            localCalendar.setTimeInMillis(getTimeInMillis());
            SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat(format);
            localSimpleDateFormat.setTimeZone(TimeZone.getDefault());
            return localSimpleDateFormat.format(localCalendar.getTime());
        }

        public static String formatLocalString(String format, long time) {
            Calendar localCalendar = Calendar.getInstance(TimeZone.getDefault());
            localCalendar.setTimeInMillis(time);
            SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat(format);
            localSimpleDateFormat.setTimeZone(TimeZone.getDefault());
            return localSimpleDateFormat.format(localCalendar.getTime());
        }

        // public String getLocalTime() {
        // Calendar localCalendar = Calendar.getInstance(TimeZone
        // .getTimeZone("gmt"));
        // localCalendar.setTimeInMillis(getTimeInMillis());
        // SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat();
        // localSimpleDateFormat.setTimeZone(TimeZone.getDefault());
        // return localSimpleDateFormat.format(localCalendar.getTime());
        // }
        //
        // public String getLocalTimeLineStr() {
        // Calendar localCalendar = Calendar.getInstance(TimeZone
        // .getTimeZone("gmt"));
        // localCalendar.setTimeInMillis(getTimeInMillis());
        // SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat(
        // "HH:mm");
        // localSimpleDateFormat.setTimeZone(TimeZone.getDefault());
        // return localSimpleDateFormat.format(localCalendar.getTime());
        // }

        public long getTimeInMillis() {
            return millis;
        }

        private void updateTimeInMillis() {
            Calendar localCalendar = Calendar.getInstance(TimeZone.getDefault());
            localCalendar.set(this.year, -1 + this.month, this.day, this.hour, this.minute,
                    this.second);
            millis = localCalendar.getTimeInMillis();
        }

        public byte[] toByteArray() {
            return this.mBuf;
        }
    }

    public static class SWifiAp implements Serializable {
        public byte enctype;
        public byte mode;
        public byte signal;
        public byte[] ssid = new byte[32];
        public byte status;

        public SWifiAp(byte[] data) {
            System.arraycopy(data, 1, this.ssid, 0, data.length);
            this.mode = data[32];
            this.enctype = data[33];
            this.signal = data[34];
            this.status = data[35];
        }

        public SWifiAp(byte[] ssid, byte mode, byte enctype, byte signal, byte status) {
            System.arraycopy(ssid, 0, this.ssid, 0, ssid.length);
            this.mode = mode;
            this.enctype = enctype;
            this.signal = signal;
            this.status = status;
        }

        public static int getTotalSize() {
            return 36;
        }

        @Override
        public String toString() {
            return new String(ssid);
        }
    }

    public static class SMsgAVIoctrlDeviceInfoResp {

        public final static int V1_SIZE = 56;
        public final static int V2_EXTEND_SIZE = 256;

        /**
         * >=1新版支持丢包统计和tf状态信息;
         * >=2支持翻转;
         * >=3支持多国语言;
         * >=4支持utc时区
         * >=5支持夜视手动设置;
         * >=6支持报警灵敏度和报警区域;
         * >=7支持version_type字段;
         * >=8支持云台相关信息;
         */
        public byte interface_version;
        public byte lossrate;           /* 局域网丢包率0-99 */
        public byte tfstat;             /* tf状态0=状态良好，1=速度过低，2=需要格式化，3=格式化后依旧异常,4=空间过小不支持，5=没有tf卡， 6=格式化中*/
        public byte internet_lossrate;  /* 预估的外网丢包率0-99 */
        public byte internet_visit;     /* 外网访问，0=无法访问，1=可以访问 */
        public byte check_stat;         /* 0=没有检查完毕，1=检查完毕 */
        public byte update_without_tf;
        public byte language;           /* 0=简体中文; 1=繁体中文； 2=韩语 */
        public byte hardware_version;   /* 0:一代摄像机; 1:二代摄像机; 2:H19摇头机; 3:H20摇头机1080P; 10:Y18; 11:Y20; 100:M10米家小蚁智能摄像机; */


        /*********
         * 1代设备和2代设备定义不同的区域
         **********************/
        public byte is_utc_time;        /* 设备存储录像是否UTC时区，0=非utc时间，1=utc时间 */
        public byte day_night_mode;     /* 红外灯模式，1:Auto, 2:Day, 3:Night, 4:Time */
        public byte alarm_sensitivity;  /* 报警灵敏度，0:high, 1:middle, 2:low */
        public byte version_type;       /* 固件分支版本，0:大陆版 1:台湾版 2:韩国版 3:美国版 4:以色列 5:菲律宾和印度尼西亚 6:印度尼西亚 7:北美 8:欧洲  100:大陆Dogfood版 103:美国Dogfood版 */
        public byte video_backup;       /* 备份视频到小米路由，0:为设备不支持，1:为支持，2:为路由器硬盘不足*/
        public byte ldc_mode;           /* 畸变校正开关，0:关闭，1:打开 */
        public byte baby_crying_mode;   /* Baby哭功能开关, 1:close; 2:open */
        public byte mic_mode;           /* Mic的音量开关， 101:close; 100:open */
        public byte talk_mode;          /* 设置对讲模式, 1单项对讲，2双向对讲*/


        // 1代设备，9 - 16
        private byte v1_is_utc_time;        /* 1代设备，设备存储录像是否UTC时区，0=非utc时间，1=utc时间 */
        private byte v1_day_night_mode;     /* 1代设备，红外灯模式，1:Auto, 2:Day, 3:Night */
        private byte v1_alarm_sensitivity;  /* 1代设备，报警灵敏度，0:high, 1:middle, 2:low */
        private byte v1_version_type;       /* 1代设备，固件分支版本，0:大陆版 1:台湾版 2:韩国版 3:美国版 4:以色列 5:菲律宾和印度尼西亚 6:印度尼西亚 7:北美 8:欧洲  100:大陆Dogfood版 103:美国Dogfood版 */
        private byte v1_video_backup;       /* 1代设备，备份视频到小米路由，0:为设备不支持，1:为支持，2:为路由器硬盘不足*/
        private byte v1_ldc_mode;           /* 1代设备，畸变校正开关，0:关闭，1:打开 */
        private byte v1_baby_crying_mode;   /* 1代设备，Baby哭功能开关, 1:close; 2:open */
        private byte v1_mic_mode;           /* 1代设备，Mic的音量开关， 101:close; 100:open */
        public byte v1_talk_mode;           /* 1代设备，对讲模式, 1单项对讲，2双向对讲*/


        // 2代设备，9 - 19
        private byte v2_version_type;       /* 2代设备，固件分支版本，0:大陆版 1:台湾版 2:韩国版 3:美国版 4:以色列 5:菲律宾和印度尼西亚 6:印度尼西亚 7:北美 8:欧洲  100:大陆Dogfood版 103:美国Dogfood版 */
        private byte v2_day_night_mode;     /* 2代设备，红外灯模式，1:Auto, 2:Day, 3:Night, 4:Time */
        public  byte v2_hd_resolution;      /* 2代设备，高清情况下的分辨率，1:720P, 2:1080P, 3:super_1080P */
        public  byte v2_alarm_mode;         /* 2代设备，报警模式开关，0:移动检测报警, 1:人体检测报警 */
        private byte v2_ldc_mode;           /* 2代设备，畸变校正开关，0:关闭，1:打开 */
        private byte v2_is_utc_time;        /* 2代设备，设备存储录像是否UTC时区，0=非utc时间，1=utc时间 */
        private byte v2_alarm_sensitivity;  /* 2代设备，报警灵敏度，0:high, 1:middle, 2:low */
        public  byte v2_beep_mode;          /* 2代设备，摄像机升级提示，0表示关闭，1表示打开*/
        public  byte v2_speaker_volume;     /* 2代设备，Speaker的音量，0~100 */
        private byte v2_is_extend;          /* 2代设备，扩展2代DeviceInfo接口, 0:表示V1_SIZE, 1:表示V2_EXTEND_SIZE */
        public  byte v2_silent_upgrade;     /* 2代设备，是否支持静默升级，0:not support; 1:close; 2:open */
        /*******************************/


        public  int version;
        public  int channel;
        public  int total;
        public  int free;
        public  byte close_camera;           // 0 打开摄像机 1 关闭摄像机
        public  byte close_light;            // 0 开灯 1 关灯
        public  byte update_stat;            // 0 没下载 1 下载中 2 下载完 3失败
        public  byte update_progress;        //
        public  byte record_mode;            // 0=移动侦测录像;1=全录
        public  byte update_mode;            // 0=ftp;1=http
        public  byte reverse_mode;           // 0=不翻转；1=翻转


        // 2代设备扩展的字段，默认0表示没有这个功能, 56 - 255
        private byte v2_extend_mic_mode              = 0;    /* 2代设备，Mic的音量开关， 101:close; 100:open */
        private byte v2_extend_baby_crying_mode      = 0;    /* 2代设备，Baby哭功能开关, 1:close; 2:open   */
        public  byte v2_extend_gesture_mode          = 0;    /* 2代设备, 手势识别开关，1:close; 2:open   */
        public  byte v2_extend_motion_roi            = 0;    /* 2代设备，报警区域是否设置ROI高清, 1:close; 2:open   */
        public  byte v2_extend_safe_remove_sd        = 0;    /* 2代设备，是否支持安全删除SD卡 */
        public  byte v2_extend_version_rollback      = 0;    /* 2代设备，是否支持版本回退功能 */
        public  byte v2_extend_upload_log            = 0;    /* 2代设备，是否支持触发设备上传日志 */
        public  byte v2_extend_wifi_switch           = 0;    /* 2代设备，切换wifi 1:close; 2:open */
        private byte v2_extend_video_backup          = 0;    /* 2代设备，0:not support; >0:support */
        public  byte v2_extend_video_talkmode        = 0;    /* 2代设备，0:not support; 1:duplex spk */
        public  byte v2_extend_pgc_live              = 0;    /* 2代设备，0:not support; 1:duplex spk */
        public  byte v2_extend_micboost_set          = 0;    /* 2代设备，0:not support; 1:duplex spk */
        public  byte v2_extend_abnormal_sound          = 0;    /* 异常声音监测报警，0: not support,  1: support*/
        public byte v2_extend_abnormal_sound_sensitivity = 0;  /* 声音侦测灵敏度设置，0: not support,   MIN:   ~   MAX: */


        //H19 巡航预置点
        public List<Integer> presets;


        //云台相关信息
        public SMsgAVIoctrlPTZInfoResp pizInfo;

        public static SMsgAVIoctrlDeviceInfoResp parse(byte[] data, boolean isByteOrderBig) {
            SMsgAVIoctrlDeviceInfoResp resp = new SMsgAVIoctrlDeviceInfoResp();
            resp.interface_version = data[0];
            resp.lossrate = data[1];
            resp.tfstat = data[2];
            resp.internet_lossrate = data[3];
            resp.internet_visit = data[4];
            resp.check_stat = data[5];
            resp.update_without_tf = data[6];
            resp.language = data[7];
            resp.hardware_version = data[8];

            resp.version = Packet.byteArrayToInt(data, 32, isByteOrderBig);
            resp.channel = Packet.byteArrayToInt(data, 36, isByteOrderBig);
            resp.total = Packet.byteArrayToInt(data, 40, isByteOrderBig);
            resp.free = Packet.byteArrayToInt(data, 44, isByteOrderBig);
            resp.close_camera       = data[48];
            resp.close_light        = data[49];
            resp.update_stat        = data[50];
            resp.update_progress    = data[51];
            resp.record_mode        = data[52];
            resp.update_mode        = data[53];
            resp.reverse_mode       = data[54];

            if (resp.hardware_version != 1) {
                // h18, h19, h20, m10
                resp.v1_is_utc_time         = data[9];
                resp.v1_day_night_mode      = data[10];
                resp.v1_alarm_sensitivity   = data[11];
                resp.v1_version_type        = data[12];
                resp.v1_video_backup        = data[13];
                resp.v1_ldc_mode            = data[14];
                resp.v1_baby_crying_mode    = data[15];
                resp.v1_mic_mode            = data[16];
                resp.v1_talk_mode           = data[17];

                resp.v2_version_type = 0;
                resp.v2_day_night_mode = 0;
                resp.v2_hd_resolution = 0;
                resp.v2_alarm_mode = 0;
                resp.v2_ldc_mode = 0;
                resp.v2_is_utc_time = 0;
                resp.v2_alarm_sensitivity = 0;
                resp.v2_beep_mode = 0;
                resp.v2_speaker_volume = 0;
                resp.v2_is_extend           = 0;
                resp.v2_silent_upgrade      = 0;
                resp.v2_extend_mic_mode = 0;
                resp.v2_extend_baby_crying_mode = 0;
                resp.v2_extend_gesture_mode = 0;
                resp.v2_extend_motion_roi = 0;
                resp.v2_extend_safe_remove_sd = 0;
                resp.v2_extend_version_rollback = 0;
                resp.v2_extend_upload_log = 0;
                resp.v2_extend_wifi_switch = 0;
                resp.v2_extend_video_backup = 0;
                resp.v2_extend_video_talkmode = 0;
                resp.v2_extend_micboost_set = 0;
                resp.v2_extend_pgc_live       =0;
                resp.v2_extend_abnormal_sound =0;
                resp.v2_extend_abnormal_sound_sensitivity=0;

                resp.day_night_mode = resp.v1_day_night_mode;
                resp.is_utc_time = resp.v1_is_utc_time;
                resp.alarm_sensitivity = resp.v1_alarm_sensitivity;
                resp.version_type = resp.v1_version_type;
                resp.video_backup = resp.v1_video_backup;
                resp.ldc_mode = resp.v1_ldc_mode;
                resp.baby_crying_mode = resp.v1_baby_crying_mode;
                resp.mic_mode = resp.v1_mic_mode;
                resp.talk_mode = resp.v1_talk_mode;

                //读取云台相关信息
                if ((resp.hardware_version == 2 || resp.hardware_version == 3) && resp.interface_version >= 8) {
                    if(data.length >= 88) {
                        //解析预置点
                        byte[] presetsData = new byte[12];
                        System.arraycopy(data, 56, presetsData, 0, 12);
                        resp.presets = new ArrayList<>(Arrays.asList(SMsgAVIoctrlPTZPresetGETResp.parseDeviceInfo(presetsData, isByteOrderBig)));

                        //解析云台相关信息
                        byte[] pizData = new byte[20];
                        System.arraycopy(data, 68, pizData, 0, 20);
                        resp.pizInfo = SMsgAVIoctrlPTZInfoResp.parse(pizData, isByteOrderBig);
                    }
                    else {
                        resp.presets = new ArrayList<>();
                        resp.pizInfo = new SMsgAVIoctrlPTZInfoResp();
                    }
                }
            } else {
                // 2代设备
                resp.v1_is_utc_time = 0;
                resp.v1_day_night_mode = 0;
                resp.v1_alarm_sensitivity = 0;
                resp.v1_version_type = 0;
                resp.video_backup = 0;
                resp.v1_ldc_mode = 0;
                resp.v1_baby_crying_mode = 0;
                resp.v1_mic_mode = 0;
                resp.v1_talk_mode = 0;

                resp.v2_version_type        = data[9];
                resp.v2_day_night_mode      = data[10];
                resp.v2_hd_resolution       = data[11];
                resp.v2_alarm_mode          = data[12];
                resp.v2_ldc_mode            = data[13];
                resp.v2_is_utc_time         = data[14];
                resp.v2_alarm_sensitivity   = data[15];
                resp.v2_beep_mode           = data[16];
                resp.v2_speaker_volume      = data[17];
                resp.v2_is_extend           = data[18];
                resp.v2_silent_upgrade      = data[19];

                resp.day_night_mode     = resp.v2_day_night_mode;
                resp.is_utc_time        = resp.v2_is_utc_time;
                resp.alarm_sensitivity  = resp.v2_alarm_sensitivity;
                resp.version_type       = resp.v2_version_type;
                resp.ldc_mode           = resp.v2_ldc_mode;

                if (resp.v2_is_extend > 0 && data.length >= V2_EXTEND_SIZE) {
                    // 说明DeviceInfo是2代设备的扩展
                    resp.v2_extend_mic_mode         = data[56];
                    resp.v2_extend_baby_crying_mode = data[57];
                    resp.v2_extend_gesture_mode     = data[58];
                    resp.v2_extend_motion_roi       = data[59];
                    resp.v2_extend_safe_remove_sd   = data[60];
                    resp.v2_extend_version_rollback = data[61];
                    resp.v2_extend_upload_log       = data[62];
                    resp.v2_extend_wifi_switch      = data[63];
                    resp.v2_extend_video_backup     = data[64];
                    resp.v2_extend_video_talkmode   = data[65];
                    resp.v2_extend_pgc_live         = data[68];//data[66]全双工多路互斥（保留）data[67]私有直播（保留）
                    resp.v2_extend_micboost_set     = data[69];
                    resp.v2_extend_abnormal_sound = data[70];
                    resp.v2_extend_abnormal_sound_sensitivity=data[71];

                    resp.baby_crying_mode           = resp.v2_extend_baby_crying_mode;
                    resp.video_backup               = resp.v2_extend_video_backup;
                    resp.mic_mode                   = resp.v2_extend_mic_mode;
                }

            }

            return resp;
        }

    }

    public static class SMsgAVIoctrlDeviceSwitchReq {
        public static byte[] parseContent(int open, boolean isByteOrderBig) {
            return Packet.intToByteArray(open, isByteOrderBig);
        }
    }

    public static class SMsgAVIoctrlSetResolutionReq {
        public static byte[] parseContent(int open, byte userCount, boolean isByteOrderBig) {
            byte[] arrayOfByte = new byte[5];
            System.arraycopy(Packet.intToByteArray(open, isByteOrderBig), 0, arrayOfByte, 0, 4);
            arrayOfByte[4] = userCount;
            return arrayOfByte;
        }
    }

    public static class SMsgAVIoctrlStartRtmpReq {
        public static byte[] parseContent(int rtmpTime, boolean isByteOrderBig) {
            return Packet.intToByteArray(rtmpTime, isByteOrderBig);
        }
    }

    public static class SMsgAVIoctrlUmountSDcardReq {
        public static byte[] parseContent(int reserved, boolean isByteOrderBig) {
            return Packet.intToByteArray(reserved, isByteOrderBig);
        }
    }

    public static class SMsgAVIoctrlSetUploadLogReq {
        public static byte[] parseContent(int reserved, boolean isByteOrderBig) {
            return Packet.intToByteArray(reserved, isByteOrderBig);
        }
    }


    public static class SMsgAVIoctrlSmartIaModeReq {
        public static byte[] parseContent(int reserved, boolean isByteOrderBig) {
            return Packet.intToByteArray(reserved, isByteOrderBig);
        }
    }

    public static class SMsgAVIoctrlVersionRecoverReq {
        public static byte[] parseContent(int reserved, boolean isByteOrderBig) {
            return Packet.intToByteArray(reserved, isByteOrderBig);
        }
    }

    public static class SMsgAVIoctrlVersionRecoverResp {
        int result;

        public static SMsgAVIoctrlVersionRecoverResp parse(byte[] data, boolean isByteOrderBig) {
            SMsgAVIoctrlVersionRecoverResp resp = new SMsgAVIoctrlVersionRecoverResp();
            resp.result = Packet.byteArrayToInt(data, 0, isByteOrderBig);
            return resp;
        }

        public int getResult() {
            return result;
        }
    }

    public static class SMsgAVIoctrlSetPlayModeReq {
        public static byte[] parseContent(int reserved, boolean isByteOrderBig) {
            return Packet.intToByteArray(reserved, isByteOrderBig);
        }
    }

    public static class SMsgAVIoctrlSetPlayModeResp {
        int result;

        public static SMsgAVIoctrlSetPlayModeResp parse(byte[] data, boolean isByteOrderBig) {
            SMsgAVIoctrlSetPlayModeResp resp = new SMsgAVIoctrlSetPlayModeResp();
            resp.result = Packet.byteArrayToInt(data, 0, isByteOrderBig);
            return resp;
        }

        public int getSpeed() {
            return result;
        }
    }

    public static class SMsgAVIoctrlGetPlayModeResp {
        int result;

        public static SMsgAVIoctrlGetPlayModeResp parse(byte[] data, boolean isByteOrderBig) {
            SMsgAVIoctrlGetPlayModeResp resp = new SMsgAVIoctrlGetPlayModeResp();
            resp.result = Packet.byteArrayToInt(data, 0, isByteOrderBig);
            return resp;
        }

        public int getSpeed() {
            return result;
        }
    }

    public static class SMsgAVIoctrlQueryRtmpStateResp {
        public int leftTime; /* 0=不处于直播;>0 处于直播状态，以及直播剩余的时间 */
        public String url; // rtmp url

        public static SMsgAVIoctrlQueryRtmpStateResp parse(byte[] data, boolean isByteOrderBig) {
            SMsgAVIoctrlQueryRtmpStateResp resp = new SMsgAVIoctrlQueryRtmpStateResp();
            resp.leftTime = Packet.byteArrayToInt(data, 0, isByteOrderBig);
            resp.url = new String(data, 4, 512).trim();
            return resp;
        }
    }

    public static class SMsgAVIoctrlCloudStorageResp {
        int result;

        public static SMsgAVIoctrlCloudStorageResp parse(byte[] data, boolean isByteOrderBig) {
            SMsgAVIoctrlCloudStorageResp resp = new SMsgAVIoctrlCloudStorageResp();
            resp.result = Packet.byteArrayToInt(data, 0, isByteOrderBig);
            return resp;
        }
    }

    public static class SMsgAVIoctrlTriggerDeviceSyncResp {
        int result;
        public static SMsgAVIoctrlTriggerDeviceSyncResp parse(byte[] data, boolean isByteOrderBig){
            SMsgAVIoctrlTriggerDeviceSyncResp resp = new SMsgAVIoctrlTriggerDeviceSyncResp();
            resp.result = Packet.byteArrayToInt(data, 0, isByteOrderBig);
            return resp;
        }
    }

    public static class SMsAVIoctrlIpcInfoCfgReq {
        int reserved;

        public static byte[] parseContent(int reserved, boolean isByteOrderBig) {
            return Packet.intToByteArray(reserved, isByteOrderBig);
        }
    }

    public static class SMsAVIoctrlIpcInfoCfgResp {
        private String jsonData = "";

        public String mMac = "";
        public String mDid = "";
        public String mUid = "";
        public String mDidm = "";
        public String mSn = "";
        public String mKey = "";
        public String mVersion = "";

        public static SMsAVIoctrlIpcInfoCfgResp parse(byte[] data) {
            SMsAVIoctrlIpcInfoCfgResp info = new SMsAVIoctrlIpcInfoCfgResp();

            try {
                String jsonString = Packet.byteArrayToString(data, data.length);
                if (!TextUtils.isEmpty(jsonString)) {
                    JSONObject jsonObject = new JSONObject(jsonString);
                    info.jsonData = jsonObject.toString();
                    info.mMac = jsonObject.optString("mac");
                    info.mDid = jsonObject.optString("did");
                    info.mUid = jsonObject.optString("uid");
                    info.mDidm = jsonObject.optString("didm");
                    info.mSn = jsonObject.optString("sn");
                    info.mKey = jsonObject.optString("key");
                    info.mVersion = jsonObject.optString("version");
                    AntsLog.d("IpcInfo", "json:" + jsonString);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return info;
        }
    }

    public static class SMsAVIoctrlSilentUpgradeCfg {
        int enable; /* 1:close; 2:open */
        public static byte[] parseContent(int enable, boolean isByteOrderBig) {
            return Packet.intToByteArray(enable, isByteOrderBig);
        }
    }


    public static class SAudioFrame {
        public byte[] data;
        public long timeStamp;
    }


    /**
     * PTZ方向控制
     */
    public static class SMsgAVIoctrlPTZDireCTRL {
        public static byte[] parseContent(int direction, int speed, boolean isByteOrderBig) {
            byte[] arrayOfByte = new byte[8];
            System.arraycopy(Packet.intToByteArray(direction, isByteOrderBig), 0, arrayOfByte, 0, 4);
            System.arraycopy(Packet.intToByteArray(speed, isByteOrderBig), 0, arrayOfByte, 4, 4);
            return arrayOfByte;
        }
    }


    /**
     * 预置点跳转
     * preset //(1-128)
     */
    public static class SMsgAVIoctrlPTZPresetCall {
        public int preset;

        public static byte[] parseContent(int preset, boolean isByteOrderBig) {
            return Packet.intToByteArray(preset, isByteOrderBig);
        }

        public static SMsgAVIoctrlPTZPresetCall parse(byte[] data, boolean isByteOrderBig) {
            SMsgAVIoctrlPTZPresetCall resp = new SMsgAVIoctrlPTZPresetCall();
            resp.preset = Packet.byteArrayToInt(data, 0, isByteOrderBig);
            return resp;
        }
    }

    /**
     * 获取预置点
     */
    public static class SMsgAVIoctrlPTZPresetGETResp {
        public short opResult;  //是否成功标示 0,失败;1,成功
        public short presetIndex; //当前添加或删除的日志点

        public int point_count;            //(0-128)预置点总个
        public Integer[] presets;  //预置点数组

        public static SMsgAVIoctrlPTZPresetGETResp parse(byte[] data, boolean isByteOrderBig) {
            AntsLog.d("presets data",  Packet.printByteArray(data,data.length));
            SMsgAVIoctrlPTZPresetGETResp resp = new SMsgAVIoctrlPTZPresetGETResp();
            resp.opResult = Packet.byteArrayToShort(data, 0, isByteOrderBig);
            resp.presetIndex = Packet.byteArrayToShort(data, 2, isByteOrderBig);

            resp.point_count = data[4];
            resp.presets = new Integer[resp.point_count];

            int j = 0;
            for (int i = 8; i < data.length; i++) {
                if (data[i] != 0 && j < resp.point_count) {
                    resp.presets[j++] = (int) data[i];
                }
            }
//            for (int i = 0; i < resp.presets.length; i++) {
//                Log.d("presets[" + i + "]", resp.presets[i] + " : point_count=" + resp.point_count);
//            }
            return resp;
        }

        public static Integer[] parseDeviceInfo(byte[] data, boolean isByteOrderBig) {
            AntsLog.d("presets deviceInfo data",  Packet.printByteArray(data,data.length));
            int point_count = data[0];
            Integer[] presets = new Integer[point_count];
            int j = 0;
            for (int i = 4; i < data.length; i++) {
                if (data[i] != 0 && j < point_count) {
                    presets[j++] = (int) data[i];
                }
            }
//            for (int i = 0; i < presets.length; i++) {
//                Log.d("presets[" + i + "]", presets[i] + " : point_count=" + point_count);
//            }
            return presets;
        }


    }


    /**
     * 巡航模式和停留时间(秒)
     */
    public static class SMsgAVIoctrlPTZCruiseModeAndTime {
        public int mode; //巡航模式
        public int sleep; //停留时间(秒)

        public static byte[] parseContent(int mode, int sleep, boolean isByteOrderBig) {
            byte[] parseData = new byte[8];
            System.arraycopy(Packet.intToByteArray(mode, isByteOrderBig), 0, parseData, 0, 4);
            System.arraycopy(Packet.intToByteArray(sleep, isByteOrderBig), 0, parseData, 4, 4);
            return parseData;
        }

        public static SMsgAVIoctrlPTZCruiseModeAndTime parse(byte[] data, boolean isByteOrderBig) {
            SMsgAVIoctrlPTZCruiseModeAndTime resp = new SMsgAVIoctrlPTZCruiseModeAndTime();
            resp.mode = Packet.byteArrayToInt(data, 0, isByteOrderBig);
            resp.sleep = Packet.byteArrayToInt(data, 4, isByteOrderBig);
            return resp;
        }
    }


    /**
     * 巡航每个点停留时间(秒)
     */
    public static class SMsgAVIoctrlPTZCruiseCall {
        public int sleep;

        public static byte[] parseContent(int sleep, boolean isByteOrderBig) {
            return Packet.intToByteArray(sleep, isByteOrderBig);
        }

        public static SMsgAVIoctrlPTZCruiseCall parse(byte[] data, boolean isByteOrderBig) {
            SMsgAVIoctrlPTZCruiseCall resp = new SMsgAVIoctrlPTZCruiseCall();
            resp.sleep = Packet.byteArrayToInt(data, 0, isByteOrderBig);
            return resp;
        }
    }

    /**
     * 跳转到某个坐标
     * SMsgAVIoctrlPTZJumpPointSet
     */

    public static class SMsgAVIoctrlPTZJumpPointSet {

        public static byte[] parseContent(int transverseProportion, int longitudinalProportion, boolean isByteOrderBig) {

            byte[] transverseArray = Packet.intToByteArray(transverseProportion, isByteOrderBig);
            byte[] longitudinalArray = Packet.intToByteArray(longitudinalProportion, isByteOrderBig);
            byte[] parseData = new byte[transverseArray.length + longitudinalArray.length];

            System.arraycopy(transverseArray, 0, parseData, 0, 4);
            System.arraycopy(longitudinalArray, 0, parseData, 4, 4);

            return parseData;
        }


    }


    /**
     * 巡航时间段
     */
    public static class SMsgAVIoctrlPTZCruisePeriod {
        public int startTime; //开始时间
        public int endTime; //结束时间

        public static byte[] parseContent(int startTime, int endTime, boolean isByteOrderBig) {
            byte[] parseData = new byte[8];
            System.arraycopy(Packet.intToByteArray(startTime, isByteOrderBig), 0, parseData, 0, 4);
            System.arraycopy(Packet.intToByteArray(endTime, isByteOrderBig), 0, parseData, 4, 4);
            return parseData;
        }

        public static SMsgAVIoctrlPTZCruisePeriod parse(byte[] data, boolean isByteOrderBig) {
            SMsgAVIoctrlPTZCruisePeriod resp = new SMsgAVIoctrlPTZCruisePeriod();
            resp.startTime = Packet.byteArrayToInt(data, 0, isByteOrderBig);
            resp.endTime = Packet.byteArrayToInt(data, 4, isByteOrderBig);
            return resp;
        }

    }


    /**
     * 云台相关信息
     */
    public static class SMsgAVIoctrlPTZInfoResp {
        public byte motionTrackState;           //移动跟踪状态  0,off; 1,on
        public byte curiseState;                  // 巡航状态   0,off; 1,on
        public byte cruiseMode;                   //0,预置巡航; 1,全景巡航

        public int presetCruiseStayTime;        //预置巡航点停留时间(秒)
        public int panoramicCruiseStayTime;    //全景巡航点停留时间(秒)

        public int startTime;                  //每天巡航开始时间(秒)
        public int endTime;                    //每天巡航结束时间(秒)

        public static SMsgAVIoctrlPTZInfoResp parse(byte[] data, boolean isByteOrderBig) {
            SMsgAVIoctrlPTZInfoResp resp = new SMsgAVIoctrlPTZInfoResp();
            resp.motionTrackState = data[0];
            resp.curiseState = data[1];
            resp.cruiseMode = data[2];
            resp.presetCruiseStayTime = Packet.byteArrayToInt(data, 4, isByteOrderBig);
            resp.panoramicCruiseStayTime = Packet.byteArrayToInt(data, 8, isByteOrderBig);
            resp.startTime = Packet.byteArrayToInt(data, 12, isByteOrderBig);
            resp.endTime = Packet.byteArrayToInt(data, 16, isByteOrderBig);
            return resp;
        }

    }


    public static class SMsgAVIoctrlOnlineStatusResp {
        public int online;
        public int lastOnlineTime;

        public static byte[] parseContent(int online, int lastLoginTime, boolean isByteOrderBig) {
            byte[] data = new byte[8];
            System.arraycopy(Packet.intToByteArray(online, isByteOrderBig), 0, data, 0, 4);
            System.arraycopy(Packet.intToByteArray(lastLoginTime, isByteOrderBig), 0, data, 4, 4);
            return data;
        }

        public static SMsgAVIoctrlOnlineStatusResp parse(byte[] data, boolean isByteOrderBig) {
            SMsgAVIoctrlOnlineStatusResp resp = new SMsgAVIoctrlOnlineStatusResp();
            resp.online = Packet.byteArrayToInt(data, 0, isByteOrderBig);
            resp.lastOnlineTime = Packet.byteArrayToInt(data, 4, isByteOrderBig);
            return resp;
        }

    }


    public static class SMsgAVIoctrlPanoramaCaptureScheduleResp {
        public int state;  //0表示当前可以执行全景拍摄； 1表示全景拍摄中；2表示全景拍摄已经完成，但处于等待图片上传中；3表示全景拍摄失败
        public int percent;
        public byte[] reserved = new byte[12];

        public static SMsgAVIoctrlPanoramaCaptureScheduleResp parse(byte[] data, boolean isByteOrderBig){
            SMsgAVIoctrlPanoramaCaptureScheduleResp resp = new SMsgAVIoctrlPanoramaCaptureScheduleResp();
            resp.state= Packet.byteArrayToInt(data, 0, isByteOrderBig);
            resp.percent = Packet.byteArrayToInt(data, 4, isByteOrderBig);
            return resp;
        }

    }




    // auto command from device
    public static class SMsgAUIoctrlTNPIpcamKickResp {
        public byte reason;
        public byte[] reserved = new byte[3];

        public static SMsgAUIoctrlTNPIpcamKickResp parse(byte[] data, boolean isByteOrderBig){
            SMsgAUIoctrlTNPIpcamKickResp resp = new SMsgAUIoctrlTNPIpcamKickResp();
            resp.reason = data[0];
            return resp;
        }

    }

}
