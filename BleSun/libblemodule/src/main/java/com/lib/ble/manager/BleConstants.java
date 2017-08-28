package com.lib.ble.manager;

/**
 * Created by SMY on 2016/12/23.
 */
public class BleConstants {
    /**
     * 蓝牙通信模块key-value定义-----------------开始-----------------
     * */
    public static final String COMMON_RET_VALUE = "rval";
    public static final String COMMON_TYPE = "type";
    public static final String COMMON_PARAM = "param";

    public static final int RETURN_TYPE_UNKNOWN = -1;
    public static final int RETURN_RVAL_UNKNOWN = -102;
    public static final int RETURN_TYPE_AUTH_HINT = -103;

    public static final int COMMON_TYPE_SETTING_VALUE = 0;//消息设置返回通知（即有没有收到消息）
    public static final int COMMON_TYPE_NOTIFICATION_VALUE = 1;//设置结果返回通知，包含具体错误号--
    public static final int COMMON_TYPE_SESSION_VALUE = 2;//session结果通知type
    public static final int COMMON_TYPE_HEARTBEAT_VALUE = 3;//heartbeat结果通知type
    public static final int COMMON_RVAL_OK = 0;//设置成功
    public static final int COMMON_RVAL_FAIL = 1;//设置失败
    public static final int COMMON_RVAL_WIFI_CONNECT_SUCCESS = 1;//wifi连接成功，此时param为ip地址
    public static final int COMMON_RVAL_WIFI_PWD_ERROR = 2;//wifi密码错误
    public static final int COMMON_RVAL_WIFI_CONNECT_FAIL = 3;//wifi连接失败
    public static final int COMMON_RVAL_WIFI_TIMEOUT = 4;//相机扫描wifi超时
    public static final int COMMON_RVAL_LIVING = 11;//直播中
    public static final int COMMON_RVAL_SERVER_AUT_ERROR = 12;//直播服务器权限错误
    public static final int COMMON_RVAL_SERVER_ERROR = 13;//服务器错误
    public static final int COMMON_RVAL_LIVE_STOP = 14;//直播结束
    public static final int COMMON_RVAL_LIVE_CONNECTING_SERVER = 15;//正在连接服务器
    public static final int COMMON_RVAL_LIVE_BITRATE = 16;//直播码率通知，单位kps
    public static final int COMMON_RVAL_LIVE_LOW_NETWORK = 17;//直播网络差，稍后将结束直播
    public static final int COMMON_RVAL_LIVE_MAC_PERMISSION_OK = 18;//mac权限检查通过
    public static final int COMMON_RVAL_LIVE_MAC_PERMISSION_ERR = 19;//mac权限检查错误
    public static final int ERR_RESOLUTION = -1;//分辨率设置错误
    public static final int ERR_FRAME_RATE = -2;//帧率设置错误
    public static final int ERR_SERVER = -3;//服务器类型错误
    public static final int ERR_RECORD = -4;//同步录制设置错误
    public static final int ERR_STREAM_URL = -5;//推流服务器错误
    public static final String COMMON_TOKEN = "token";

    //wifi settings
    public static final String WIFI_SSID = "ssid";
    public static final String WIFI_PASSWORD = "pwd";

    //live settings
    public static final String LIVE_RESOLUTION = "res";//分辨率，0：1440*720；
    public static final String LIVE_FRAME_RATE = "fr";//帧率，0：30p；1:60p；
    public static final String LIVE_QUALITY = "qu";//画质，0：标清，1：高清，2：超清，3：自适应
    public static final String LIVE_RECORD = "record";//同步录制，0：关闭；1：开启
    public static final String LIVE_SERVER = "server";//服务器类型
    public static final String LIVE_STREAM_URL = "url";//推流地址~
    public static final int LIVE_SER_USERDEFINE = 0;//自定义直播平台
    public static final int LIVE_SER_YOUTUBE = 1;//youtube直播
    public static final int LIVE_SER_FACEBOOK = 2;//facebook直播
    public static final int LIVE_SER_WEIBO = 3;

    //live controls
    public static final String LIVE_CONTROL = "live";
    public static final int LIVE_START = 0;//开始直播
    public static final int LIVE_STOP = 1;//结束直播，直播已开启
    public static final int LIVE_EXIT = 2;//退出直播，直播尚未开启成功时退出直播
    public static final int LIVE_PREPARE = 3;//直播准备

    //live state controls
    public static final String LIVE_GET_STATE = "get";
    public static final int LIVE_GET_LIVE_STATE = 0;//获取直播状态，且启动直播流程（此时固件会滴响）
    public static final int LIVE_CHECK_STATE = 1;//只获取直播状态，不走启动流程
    public static final int LIVE_STATE_LIVING = 0;//正在直播中
    public static final int LIVE_STATE_NORMAL = 1;//普通状态
    public static final int LIVE_STATE_NOTIFICATION = 2;//直播通知
    public static final String LIVE_SESSION = "session";
    public static final int LIVE_SESSION_START = 0;
    public static final int LIVE_SESSION_SUCCESS = 0;
    public static final int LIVE_SESSION_FAIL = -1;
    public static final int LIVE_SESSION_APP_CONNECTING = -2;//app通过wifi连接中
    public static final int LIVE_SESSION_CAMERA_BUSY = -3;//相机忙
    public static final int COMMON_STATE_LOW_POWER = -4;//相机低电量
    public static final String LIVE_HEARTBEAT = "heart";
    public static final int LIVE_HEARTBEAT_SUCCESS = 0;
    public static final int LIVE_HEARTBEAT_FAIL = -1;
    public static final String LIVE_PHONE_MAC = "mac";

    //other error code
    public static final int LIVE_ERROR_SEND_FAIL = 100;//发送命令失败
    public static final int OPERATION_ERR_TIME_OUT = 101;
    public static final int OPERATION_ERR_DISCONNECTED = 102;
    public static final int OPERATION_ERR_CHARACTERISTIC_NOT_FOUND = 103;
    public static final int OPERATION_ERR_SERVICE_NOT_FOUND = 104;
    public static final int OPERATION_ERR_HEARTBEAT_TIMEOUT = 105;

    /**
     * 蓝牙通信模块key-value定义-----------------结束-----------------
     * */
}
