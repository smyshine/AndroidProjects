package com.lib.ble.manager;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by SMY on 2016/12/2.
 */
public class BleManager {
    private static final String TAG = "BleManager";

    private static final int TIME_OUT_PERIOD = 10000;
    private static final int SETTING_REPLY_TIME_OUT_PERIOD = 40000;
    private static final int SEND_MESSAGE_RETRY_COUNT = 3;
    private static final int MSG_SEND_TIME_OUT = 6000;
    private static final int MSG_START_LIVE_TIMEOUT = 10000;
    private static final int HEARTBEAT_PERIOD = 10000;
    private static final int HEARTBEAT_TIMEOUT = 40000;
    private static final int WAIT_AUTHORIZATION_HINT = 6000;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    private static final int STATE_SCANNING = 3;

    private static final int MSG_OPERATION_TIME_OUT = 200;
    private static final int MSG_OPERATION_COMMON_ERR = 201;
    private static final int MSG_OPERATION_START_CONNECT = 301;
    private static final int MSG_OPERATION_CONNECTED = 302;
    private static final int MSG_AUTHORIZATION_HINT = 303;

    public static final int ACTIVITY_BLE_OPEN_REQUEST = 1000;

    public static final int OPERATION_NONE = 0;
    public static final int OPERATION_CONNECT = 1;
    public static final int OPERATION_SET_WIFI = 2;//设置wifi信息，即ssid、密码
    public static final int OPERATION_SET_LIVE = 3;//设置直播参数信息
    public static final int OPERATION_START_LIVE = 4;//开始直播
    public static final int OPERATION_STOP_LIVE = 5;//live信息配置好的的时候退出直播，即预览的时候
    public static final int OPERATION_EXIT_LIVE = 6;//wifi信息配置好了的时候退出直播
    public static final int OPERATION_GET_STATE = 7;//获取相机当前状态
    public static final int OPERATION_PREPARE_LIVE = 8;//直播准备
    public static final int OPERATION_START_SESSION = 9;
    public static final int OPERATION_HEARTBEAT = 10;
    public static final int OPERATION_MAC = 11;

    //uuid definition
    private static final String UUID_Service_Control = "00000000-6179-696a-656b-69796f616978";
    private static final String UUID_Wifi_Settings = "01000000-6179-696a-656b-69796f616978";//wifi settings, ssid/password
    private static final String UUID_Live_Settings = "02000000-6179-696a-656b-69796f616978";//live settings, resolution/framerate/
    private static final String UUID_Live_Control = "03000000-6179-696a-656b-69796f616978";//start/stop live
    private static final String UUID_State_Control = "04000000-6179-696a-656b-69796f616978";//live state control

    private BluetoothManager mBluetoothManager;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothAdapter mBluetoothAdapter;
    private static BleManager mBleManager;
    private Context mContext;
    private BluetoothAdapter.LeScanCallback mScanCallback;
    private List<OnBleEventListener> mListener = new ArrayList<>();

    private String mBluetoothDeviceAddress;
    private volatile boolean mIsCameraConnected = false;
    private boolean mIsSessionStarted = false;
    private boolean mIsCameraStatusGot = false;
    private boolean mIsCameraLiving = false;
    private boolean mIsLivePrepared = false;//直播是否准备好
    private boolean mIsConnectCheck = false;//当前连接行为是否为check（即不走直播流程）
    private long mConnectStartTime = 0;
    private long mCameraLiveStartTime = 0;//直播开始时间
    private int mConnectionState = STATE_DISCONNECTED;
    private int mOperation = OPERATION_NONE;

    private boolean mAuthWhenConnect = true;

    public BleManager() {

    }

    public synchronized static BleManager getInstance() {
        if (mBleManager == null) {
            mBleManager = new BleManager();
        }
        return mBleManager;
    }

    public void clean(){
        mBleManager = null;
        cancelCheckHeartbeat();
        stopMessageThread();
        clearMessageQueue();
        mListener.clear();
    }

    public boolean initialize(Context ctx) {
        mContext = ctx.getApplicationContext();
        BlePreferenceUtil.getInstance().init(mContext);

        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                BleLogger.print2File(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            BleLogger.print2File(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        mIsCameraConnected = false;
        mIsSessionStarted = false;
        mConnectionState = STATE_DISCONNECTED;
        mOperation = OPERATION_NONE;

        return true;
    }

    public void setAuthWhenConnect(boolean auth){
        this.mAuthWhenConnect = auth;
    }

    public static boolean isBleSupported(Context ctx) {
        return ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    public boolean isBleEnabled() {
        return (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled());
    }

    public boolean isCameraConnected(String address) {
        List<BluetoothDevice> devices = mBluetoothManager.getConnectedDevices(BluetoothProfile.GATT);
        for (BluetoothDevice device : devices) {
            if (device.getAddress().equals(address)){
                return true;
            }
        }
        return false;
    }

    public boolean isCameraConnected(){
        return mConnectionState == STATE_CONNECTED;
    }

    public void check(String address){
        BleLogger.print2File(TAG, "check live, address : " + address);
        if (!address.isEmpty()){
            mIsConnectCheck = true;
            connect(address);
        }
    }

    public void startScan(BluetoothAdapter.LeScanCallback scanCallback) {
        if (mBluetoothAdapter == null) {
            BleLogger.print2File(TAG, "startScan fail.BluetoothAdapter not initialized");
            return;
        }
        mScanCallback = scanCallback;
        mBluetoothAdapter.startLeScan(scanCallback);
    }

    public void stopScan() {
        if (mBluetoothAdapter == null) {
            BleLogger.print2File(TAG, "stopScan fail.BluetoothAdapter not initialized");
            return;
        }
        mBluetoothAdapter.stopLeScan(mScanCallback);
        mScanCallback = null;
    }

    public void enableBle(Activity act) {
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            act.startActivityForResult(enableBtIntent, ACTIVITY_BLE_OPEN_REQUEST);
        }
    }

    public void addListener(OnBleEventListener listener) {
        if (!mListener.contains(listener)) {
            mListener.add(listener);
        }
    }

    public void removeListener(OnBleEventListener listener) {
        mListener.remove(listener);
    }

    private BleLiveParams bleLiveParams = new BleLiveParams();

    public BleLiveParams getBleLiveParams(){
        return this.bleLiveParams;
    }

    /**
     * 蓝牙流程：start session--->get state---->prepare live--->set wifi--->set live--->start live--->stop live
     *                                      -->stop live
     * */

    private void startBleConnection(){
        initCharacteristicNotifications();
        startMessageThread();
        if (mAuthWhenConnect){
            notifyMac();
        } else {
            startSession();
        }
    }

    public boolean notifyMac(){
        if (!mIsCameraConnected){
            return false;
        }

        notifyMacInternal();

        return true;
    }

    public static String getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:",b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
            BleLogger.print2File("getMacException : ", ex.toString());
            ex.printStackTrace();
        }
        return "02:00:00:00:00:00";
    }

    private boolean notifyMacInternal(){
        mOperation = OPERATION_MAC;

        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_AUTHORIZATION_HINT), WAIT_AUTHORIZATION_HINT);

        JSONObject jo = new JSONObject();
        try {
            jo.put(BleConstants.LIVE_PHONE_MAC, getMacAddr());
            jo.put("id", 3);
            jo.put(BleConstants.COMMON_TOKEN, 0);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        addMessage(new BleMessage(UUID_State_Control, jo.toString(), mOperation));

        return true;
    }

    public boolean startSession(){
        if (!mIsCameraConnected){
            return false;
        }

        sendMessageInternal(OPERATION_START_SESSION);

        return true;
    }

    private boolean sendHeartbeat(){
        if (!mIsCameraConnected){
            return false;
        }

        sendMessageInternal(OPERATION_HEARTBEAT);

        return true;
    }

    public boolean getLiveState(){
        if (!mIsCameraConnected){
            return false;
        }

        sendMessageInternal(OPERATION_GET_STATE);

        return true;
    }

    private boolean prepareLive(){
        if (!mIsCameraConnected){
            return false;
        }

        sendMessageInternal(OPERATION_PREPARE_LIVE);

        return true;
    }

    public boolean setWifiInfo(){
        if (!mIsCameraConnected || bleLiveParams == null) {
            return false;
        }

        sendMessageInternal(OPERATION_SET_WIFI);

        return true;
    }

    public boolean setLiveInfo(){
        if (!mIsCameraConnected || bleLiveParams == null) {
            return false;
        }

        sendMessageInternal(OPERATION_SET_LIVE);

        return true;
    }

    public boolean startLive(){
        if (!mIsCameraConnected) {
            return false;
        }

        sendMessageInternal(OPERATION_START_LIVE);

        return true;
    }

    public boolean stopLive(){
        if (!mIsCameraConnected) {
            return false;
        }

        sendMessageInternal(OPERATION_STOP_LIVE);

        return true;
    }

    //如果尚未获取到固件状态则直接断开连接
    public boolean exitLive(){
        if (!mIsCameraConnected) {
            return false;
        }
        if (!mIsCameraStatusGot){
            BleLogger.print2File(TAG, "exit live disconnect");
            disconnect();
            return false;
        }

        clearMessageWhileExit();
        sendMessageInternal(OPERATION_EXIT_LIVE);

        return true;
    }

    //exit时remove其他message，避免时间到了之后又发送了---
    private void clearMessageWhileExit(){
        mSendMessageHandler.removeMessages(OPERATION_SET_WIFI);
        mSendMessageHandler.removeMessages(OPERATION_SET_LIVE);
        mSendMessageHandler.removeMessages(OPERATION_GET_STATE);
    }

    private void sendMessageInternal(int operation){
        Message message = mSendMessageHandler.obtainMessage();
        message.what = operation;
        message.arg1 = 1;
        mSendMessageHandler.sendMessage(message);
        BleLogger.print(TAG, "handleMessage: send : " + operation);
    }

    public void doSendMessage(int what){
        if (!mIsCameraConnected){
            BleLogger.print2File(TAG, "camera unconnected, dismiss send message command, what: " + what);
            return;
        }

        switch (what){
            case OPERATION_GET_STATE:
                getLiveStateInternal();
                break;
            case OPERATION_SET_WIFI:
                setWifiInfoInternal();
                break;
            case OPERATION_SET_LIVE:
                setLiveInfoInternal();
                break;
            case OPERATION_START_LIVE:
                startLiveInternal();
                break;
            case OPERATION_STOP_LIVE:
                stopLiveInternal();
                break;
            case OPERATION_EXIT_LIVE:
                exitLiveInternal();
                break;
            case OPERATION_PREPARE_LIVE:
                prepareLiveInternal();
                break;
            case OPERATION_START_SESSION:
                startSessionInternal();
                break;
            case OPERATION_HEARTBEAT:
                sendHeartbeatInternal();
                break;
        }
    }

    private void initCharacteristicNotifications(){
        setCharacteristicNotification(UUID_State_Control);
        setCharacteristicNotification(UUID_Live_Control);
        setCharacteristicNotification(UUID_Wifi_Settings);
        setCharacteristicNotification(UUID_Live_Settings);
    }

    private boolean startSessionInternal(){
        mOperation = OPERATION_START_SESSION;

        JSONObject jo = new JSONObject();
        try {
            jo.put(BleConstants.LIVE_SESSION, BleConstants.LIVE_SESSION_START);
            jo.put(BleConstants.COMMON_TOKEN, 0);
            jo.put("id", 1);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        addMessage(new BleMessage(UUID_State_Control, jo.toString(), mOperation));

        return true;
    }

    private int mHeartCount;
    private boolean sendHeartbeatInternal(){
        mOperation = OPERATION_HEARTBEAT;

        JSONObject jo = new JSONObject();
        try {
            ++mHeartCount;
            jo.put("id", 2);
            jo.put(BleConstants.LIVE_HEARTBEAT, mHeartCount);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        writeCharacteristicSlice(UUID_State_Control, jo, OPERATION_GET_STATE);

        mSettingTimeoutHandler.sendMessageDelayed(mSendMessageHandler.obtainMessage(mOperation), HEARTBEAT_TIMEOUT);

        return true;
    }

    private Timer mTimer;
    private TimerTask mTimerTask;

    private void startCheckHeartbeat() {
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                sendHeartbeat();
            }
        };
        mTimer = new Timer();
        mTimer.schedule(mTimerTask, 5000, HEARTBEAT_PERIOD);
    }

    private void cancelCheckHeartbeat() {
        if (mTimer != null) {
            mHeartCount = 0;
            mTimerTask.cancel();
            mTimerTask = null;
            mTimer.cancel();
            mTimer = null;
        }
    }

    private boolean getLiveStateInternal(){
        mOperation = OPERATION_GET_STATE;

        JSONObject jo = new JSONObject();
        try {
            if (mIsConnectCheck){
                jo.put(BleConstants.LIVE_GET_STATE, BleConstants.LIVE_CHECK_STATE);
            } else {
                jo.put(BleConstants.LIVE_GET_STATE, BleConstants.LIVE_GET_LIVE_STATE);
            }
            jo.put("id", 0);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        writeCharacteristicSlice(UUID_State_Control, jo, mOperation);

        return true;
    }

    private void prepareLiveInternal(){
        mOperation = OPERATION_PREPARE_LIVE;

        JSONObject jo = new JSONObject();
        try {
            jo.put(BleConstants.LIVE_CONTROL, BleConstants.LIVE_PREPARE);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        writeCharacteristicSlice(UUID_Live_Control, jo, mOperation);

        mSendMessageHandler.removeMessages(mOperation);
    }

    private void setWifiInfoInternal(){
        mOperation = OPERATION_SET_WIFI;

        JSONObject jo = new JSONObject();
        try {
            jo.put(BleConstants.WIFI_SSID, bleLiveParams.wifiSsid);
            jo.put(BleConstants.WIFI_PASSWORD, bleLiveParams.wifiPwd);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        writeCharacteristicSlice(UUID_Wifi_Settings, jo, mOperation);
    }

    private void setLiveInfoInternal(){
        mOperation = OPERATION_SET_LIVE;

        JSONObject jo = new JSONObject();
        try {
            jo.put(BleConstants.LIVE_RESOLUTION, bleLiveParams.resolution);
            jo.put(BleConstants.LIVE_FRAME_RATE, bleLiveParams.frameRate);
            jo.put(BleConstants.LIVE_RECORD, bleLiveParams.record);
            jo.put(BleConstants.LIVE_SERVER, bleLiveParams.server);
            jo.put(BleConstants.LIVE_STREAM_URL, bleLiveParams.streamUrl);
            jo.put(BleConstants.LIVE_QUALITY, bleLiveParams.quality);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        writeCharacteristicSlice(UUID_Live_Settings, jo, mOperation);
    }

    private void startLiveInternal(){
        mOperation = OPERATION_START_LIVE;

        JSONObject jo = new JSONObject();
        try {
            jo.put(BleConstants.LIVE_CONTROL, BleConstants.LIVE_START);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        writeCharacteristicSlice(UUID_Live_Control, jo, mOperation);
    }

    private void stopLiveInternal(){
        mOperation = OPERATION_STOP_LIVE;

        JSONObject jo = new JSONObject();
        try {
            jo.put(BleConstants.LIVE_CONTROL, BleConstants.LIVE_STOP);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        writeCharacteristicSlice(UUID_Live_Control, jo, mOperation);
        mSendMessageHandler.removeMessages(mOperation);

        //等3s断开连接
        mHandler.postDelayed(rDisconnect, 3000);
    }

    private void exitLiveInternal(){
        mOperation = OPERATION_EXIT_LIVE;

        JSONObject jo = new JSONObject();
        try {
            jo.put(BleConstants.LIVE_CONTROL, BleConstants.LIVE_EXIT);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        writeCharacteristicSlice(UUID_Live_Control, jo, mOperation);
        mSendMessageHandler.removeMessages(mOperation, mOperation);

        //等3s断开连接
        mHandler.postDelayed(rDisconnect, 3000);
    }

    private Runnable rDisconnect = new Runnable() {
        @Override
        public void run() {
            disconnect();
        }
    };


    /**
     * 处理Write指令后返回的Response
     * @param characteristic
     * @param gattStatus
     */
    private void processWriteCommand(BluetoothGattCharacteristic characteristic, int gattStatus) {
        if (gattStatus == BluetoothGatt.GATT_SUCCESS){
            mCurrentSend = true;
            String uuid = characteristic.getUuid().toString();
            if (uuid.equals(UUID_Live_Control) && mIsLivePrepared){
                //直播准备好则开始发送参数信息
                setWifiInfo();
                mIsLivePrepared = false;
            }
        }
    }

    /**
     * 处理Read指令后返回的Response
     * @param characteristic
     * @param gattStatus
     */
    private void processReadCommand(BluetoothGattCharacteristic characteristic, int gattStatus) {
        String uuid = characteristic.getUuid().toString();
        String value = getData(characteristic);
        BleLogger.print2File(TAG, "processReadCommand:" + value + "");
        if (uuid.equals(UUID_Wifi_Settings)){

        }

    }

    //发送消息的响应，即是否收到消息
    private void handleMessageCallback(String uuid, int rval,  String param){
        //信息接收失败，什么也不做，当做没有接收到反馈，超过超时重试则认为失败
        //信息接收成功，则remove超时重试的message并callback

        if (uuid.equals(UUID_Wifi_Settings)){
            if (rval == BleConstants.COMMON_RVAL_OK){
                //wifi消息接收成功
                mSendMessageHandler.removeMessages(OPERATION_SET_WIFI);
                BleLogger.print(TAG, "handleMessage: remove setWifi messages");
                for (OnBleEventListener listener : mListener) {
                    listener.onCommandNotification(OPERATION_SET_WIFI, rval, param);
                }
            }
        } else if (uuid.equals(UUID_Live_Settings)){
            if (rval == BleConstants.COMMON_RVAL_OK){
                //live参数消息接收成功
                mSendMessageHandler.removeMessages(OPERATION_SET_LIVE);
                BleLogger.print(TAG, "handleMessage: remove setLive messages");
                for (OnBleEventListener listener : mListener) {
                    listener.onCommandNotification(OPERATION_SET_LIVE, rval, param);
                }
            }
        }//开始结束直播、获取直播状态直接从type1返回，所以不在此处removeMessage。
    }

    //设置结果的响应，即是否设置成功、有何错误等
    private void handleSettingCallback(String uuid, int rval,  String param){
        int operation = OPERATION_NONE;
        if (uuid.equals(UUID_Wifi_Settings)){
            operation = OPERATION_SET_WIFI;
            mSettingTimeoutHandler.removeMessages(OPERATION_SET_WIFI);
        } else if (uuid.equals(UUID_Live_Settings)){
            operation = OPERATION_SET_LIVE;
        } else if (uuid.equals(UUID_Live_Control)){
            operation = OPERATION_START_LIVE;
            mSendMessageHandler.removeMessages(OPERATION_START_LIVE);
            mSendMessageHandler.removeMessages(OPERATION_STOP_LIVE);
            mSendMessageHandler.removeMessages(OPERATION_EXIT_LIVE);
            BleLogger.print(TAG, "handleMessage: remove : " + OPERATION_START_LIVE + ", " + OPERATION_STOP_LIVE + ", " + OPERATION_EXIT_LIVE);
            if (mIsCameraLiving && rval == BleConstants.COMMON_RVAL_LIVE_STOP){
                BleLogger.print2File(TAG, "disconnect stop live");
                mHandler.removeCallbacks(rDisconnect);
                disconnect();
            } else if (rval == BleConstants.COMMON_RVAL_LIVING){
                mIsCameraLiving = true;
                mCameraLiveStartTime = System.currentTimeMillis();
            } else if (rval == BleConstants.COMMON_RVAL_LIVE_MAC_PERMISSION_OK){
                //权限认证通过，开始session
                mHandler.removeMessages(MSG_AUTHORIZATION_HINT);
                startSession();
            } else if (rval == BleConstants.COMMON_RVAL_LIVE_MAC_PERMISSION_ERR){
                //权限认证失败，UI提示
                mHandler.removeMessages(MSG_AUTHORIZATION_HINT);
            }
        } else if (uuid.equals(UUID_State_Control)){
            operation = OPERATION_GET_STATE;
            mIsCameraStatusGot = true;
            //收到了获取反馈
            mSendMessageHandler.removeMessages(OPERATION_GET_STATE);
            BleLogger.print(TAG, "handleMessage: remove : " + OPERATION_GET_STATE);
            if (rval == BleConstants.LIVE_STATE_NORMAL){
                if (mIsConnectCheck){
                    BleLogger.print2File(TAG, "disconnect first check");
                    disconnect();//是第一次运行check的，断开连接避免在扫描时流程错乱
                } else {
                    prepareLive();//正在直播中则不发送wifi、live设置信息
                    mIsLivePrepared = true;
                }
            } else if (rval == BleConstants.LIVE_STATE_LIVING){
                mIsCameraLiving = true;
                mCameraLiveStartTime = System.currentTimeMillis() - Integer.valueOf(param) * 1000;
            }
        }

        for (OnBleEventListener listener : mListener) {
            listener.onCommandNotification(operation, rval, param);
        }
    }

    private int mToken = 0;
    //start session结果响应
    private void handleStartSessionCallback(String uuid, int rval,  String param){
        //
        int operation = OPERATION_NONE;
        if (uuid.equals(UUID_State_Control)){
            operation = OPERATION_START_SESSION;
            if (rval == BleConstants.LIVE_SESSION_SUCCESS){
                mIsSessionStarted = true;
                mSendMessageHandler.removeMessages(OPERATION_START_SESSION);
                mToken = Integer.valueOf(param);
                getLiveState();
                startCheckHeartbeat();
            }
        }
        for (OnBleEventListener listener : mListener) {
            listener.onCommandNotification(operation, rval, param);
        }
    }

    //heartbeat结果响应
    private void handleHeartbeatCallback(String uuid, int rval,  String param){
        int operation = OPERATION_NONE;
        if (uuid.equals(UUID_State_Control)){
            operation = OPERATION_HEARTBEAT;
            if (rval == BleConstants.LIVE_HEARTBEAT_SUCCESS){
                //心跳正常，取消本次超时和重试
                mSettingTimeoutHandler.removeMessages(OPERATION_HEARTBEAT);
                mSendMessageHandler.removeMessages(OPERATION_HEARTBEAT);
            } else {
                //心跳异常
                cancelCheckHeartbeat();
            }
        }
        for (OnBleEventListener listener : mListener) {
            listener.onCommandNotification(operation, rval, param);
        }
    }


    /**
     * 将蓝牙返回的消息转换为String
     * @param characteristic
     * @return
     */
    private String getData(BluetoothGattCharacteristic characteristic) {
        final byte[] data = characteristic.getValue();
        //Log.d(TAG, "---data size---" + data.length);
        if (data != null && data.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for (byte byteChar : data) {
//                stringBuilder.append(String.format("%02X ", byteChar));

                try {
                    char c = (char) Integer.parseInt(Byte.toString(byteChar));
                    if (c != '\u0000') {
                        stringBuilder.append(String.valueOf(c));
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    break;
                }

            }

            return stringBuilder.toString();
        }
        return "";
    }

    private String getData(byte[] data) {
        if (data != null && data.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for (byte byteChar : data) {
//                stringBuilder.append(String.format("%02X ", byteChar));

                try {
                    char c = (char) Integer.parseInt(Byte.toString(byteChar));
                    if (c != '\u0000') {
                        stringBuilder.append(String.valueOf(c));
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    break;
                }

            }

            return stringBuilder.toString();
        }
        return "";
    }

    private void setCharacteristicNotification(String uuid) {
        BleLogger.print(TAG, "set notification, uuid: " + uuid);
        if (mBluetoothAdapter == null) {
            BleLogger.print2File(TAG, "BluetoothAdapter not initialized");
            return;
        }
        if (mBluetoothGatt == null){
            BleLogger.print2File(TAG, "mBluetoothGatt not initialized");
            return;
        }
        BluetoothGattService service = mBluetoothGatt.getService(UUID.fromString(UUID_Service_Control));
        if (service != null) {
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(uuid));
            if (characteristic != null) {
                mBluetoothGatt.setCharacteristicNotification(characteristic, true);
            } else {
                BleLogger.print2File(TAG, "Characteristic not found, uuid: " + uuid);
                mHandler.sendMessage(mHandler.obtainMessage(MSG_OPERATION_COMMON_ERR, BleConstants.OPERATION_ERR_CHARACTERISTIC_NOT_FOUND));
            }
        } else {
            BleLogger.print2File(TAG, "Service not found, uuid: " + UUID_Service_Control);
            mHandler.sendMessage(mHandler.obtainMessage(MSG_OPERATION_COMMON_ERR, BleConstants.OPERATION_ERR_SERVICE_NOT_FOUND));
        }
    }

    private boolean doConnect(String address) {
        if (mBluetoothAdapter == null || TextUtils.isEmpty(address)) {
            BleLogger.print2File(TAG, "doConnect , BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        if (mBluetoothGatt != null){
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }

        mBluetoothDeviceAddress = address;
        mConnectStartTime = System.currentTimeMillis();
        mHandler.sendEmptyMessage(MSG_OPERATION_START_CONNECT);
        return true;
    }

    private void doDisconnect() {
        BleLogger.print2File(TAG, "disconnect BLE connection internally");
        if (mConnectionState == STATE_SCANNING && mBluetoothAdapter != null) {
            //stopConnectScan();
        }
        mHandler.removeCallbacksAndMessages(null);
        mSendMessageHandler.removeCallbacksAndMessages(null);
        mSettingTimeoutHandler.removeCallbacksAndMessages(null);
        mOperation = OPERATION_NONE;
        mIsCameraStatusGot = false;
        mIsCameraLiving = false;
        mCurrentMessageSendOver = true;
        mCurrentSend = true;
        mCameraLiveStartTime = 0;
        mConnectStartTime = 0;
        mIsCameraConnected = false;
        mIsConnectCheck = false;
        if (mBluetoothAdapter == null) {
            BleLogger.print2File(TAG, "doDisconnect, BluetoothAdapter not initialized");
            return;
        }
        if (mBluetoothGatt == null){
            BleLogger.print2File(TAG, "doDisconnect, mBluetoothGatt not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
        cancelCheckHeartbeat();
        stopMessageThread();
    }

    private void resetTimeout() {
        mHandler.removeCallbacksAndMessages(null);
        mHandler.sendEmptyMessageDelayed(MSG_OPERATION_TIME_OUT, TIME_OUT_PERIOD);
    }

    public void disconnect() {
        BleLogger.print2File(TAG, "disconnect BLE connection initiative");
        doDisconnect();
    }

    public boolean connect(String address) {
        BleLogger.print2File(TAG, "Request paring to device: " + address);
        if (mIsCameraConnected && isCameraConnected(address) && address.equals(mBluetoothDeviceAddress))
            return true;
        if (!isBleEnabled()){
            mIsCameraConnected = false;
            return false;
        }
        if (mOperation != OPERATION_NONE) {
            doDisconnect();
        }
        clearMessageQueue();
        mCurrentMessageSendOver = true;
        mCurrentSend = true;
        mIsCameraConnected = false;
        mIsSessionStarted = false;
        mOperation = OPERATION_CONNECT;
        mHandler.removeCallbacksAndMessages(null);
        mHandler.sendEmptyMessageDelayed(MSG_OPERATION_TIME_OUT, TIME_OUT_PERIOD);
        return doConnect(address);
    }

    public boolean isCameraLiving(){
        return this.mIsCameraLiving;
    }

    public long getLivingTime(){
        return this.mCameraLiveStartTime == 0 ? 0 : (System.currentTimeMillis() - mCameraLiveStartTime) / 1000;
    }

    public interface OnBleEventListener {
        void onCameraConnected(String address);

        void onCameraDisconnected();

        void onCommandNotification(int operation, int rVal, String param);
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            BleLogger.print2File(TAG, "Write response:" + characteristic.getUuid().toString() + " status:" + status + " Operation:" + mOperation);
            //获取write response
            if (status != BluetoothGatt.GATT_SUCCESS) {
                BleLogger.print2File(TAG, "Characteristic write error, uuid:" + characteristic.getUuid().toString());
            }
            processWriteCommand(characteristic, status);
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            BleLogger.print2File(TAG, "GATT status changed, status:" + status + " newState:" + newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mConnectionState = STATE_CONNECTED;
                // 稳定持续1s的connected状态，则认为真正连接成功了（因为有的蓝牙能搜到能连接但是距离有点远导致连接上后立马断开，通信不稳）
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mConnectionState == STATE_CONNECTED) {
                            BleLogger.print2File(TAG, "Discover services...");
                            mIsCameraConnected = true;
                            mHandler.sendEmptyMessage(MSG_OPERATION_CONNECTED);
                            if (mBluetoothGatt != null)
                                mBluetoothGatt.discoverServices();
                        }
                    }
                }, 1000);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mConnectionState = STATE_DISCONNECTED;
                mCurrentMessageSendOver = true;
                mCurrentSend = true;
                mIsCameraConnected = false;
                mIsSessionStarted = false;
                BleLogger.print2File(TAG, "Disconnected from GATT server, status:" + status);
                mBluetoothGatt.disconnect();
                mBluetoothGatt.close();
                mBluetoothGatt = null;
                cancelCheckHeartbeat();
                if (mConnectStartTime != 0){
                    if ((System.currentTimeMillis() - mConnectStartTime > 2000)){
                        //连接成功之后不到2s就断开，一般是133错误，需要清掉gatt信息再试
                        mHandler.sendMessage(mHandler.obtainMessage(MSG_OPERATION_COMMON_ERR, BleConstants.OPERATION_ERR_DISCONNECTED));
                    } else {
                        mHandler.sendEmptyMessage(MSG_OPERATION_START_CONNECT);
                    }
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            BleLogger.print2File(TAG, "Read response:" + characteristic.getUuid().toString() + " status:" + status);
            if (status != BluetoothGatt.GATT_SUCCESS) {
                BleLogger.print2File(TAG, "Characteristic read error, uuid:" + characteristic.getUuid().toString());
            }
            processReadCommand(characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            //Log.d(TAG, "Notification received:" + characteristic.getUuid() + " value:" + getData(characteristic));
            processNotification(characteristic);

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            BleLogger.print2File(TAG, "onServicesDiscovered : status : " + status);
            super.onServicesDiscovered(gatt, status);
            if (status != BluetoothGatt.GATT_SUCCESS) {
                mHandler.sendMessage(mHandler.obtainMessage(MSG_OPERATION_COMMON_ERR, BleConstants.OPERATION_ERR_SERVICE_NOT_FOUND));
                return;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e){
                e.printStackTrace();
            }
            startBleConnection();
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            BleLogger.print2File(TAG, "uuid:" + descriptor.getUuid().toString());
            super.onDescriptorWrite(gatt, descriptor, status);
        }
    };

    private Handler mSendMessageHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            BleLogger.print2File(TAG, "handleMessage: " + msg.what + ", arg1 : " + msg.arg1);
            if (msg.arg1 > SEND_MESSAGE_RETRY_COUNT){
                //超过重试次数，则认为失败
                for (OnBleEventListener listener : mListener){
                    listener.onCommandNotification(msg.what, BleConstants.LIVE_ERROR_SEND_FAIL, "");
                }
                return;
            }
            Message newMessage = mSendMessageHandler.obtainMessage();
            newMessage.what = msg.what;
            newMessage.arg1 = msg.arg1 + 1;
            if (msg.what == OPERATION_START_LIVE){
                mSendMessageHandler.sendMessageDelayed(newMessage, MSG_START_LIVE_TIMEOUT);
            } else{
                mSendMessageHandler.sendMessageDelayed(newMessage, MSG_SEND_TIME_OUT);
            }
            BleLogger.print2File(TAG, "handleMessage: send " + msg.what + " delayed " );
            Intent intent = new Intent(mContext, BleSendMessageService.class);
            intent.putExtra(BleSendMessageService.OPERATION, msg.what);
            mContext.startService(intent);
        }
    };

    private Handler mSettingTimeoutHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            switch (msg.what){
                case OPERATION_SET_WIFI:
                    doDisconnect();
                    for (OnBleEventListener listener : mListener) {
                        listener.onCommandNotification(msg.what, BleConstants.LIVE_ERROR_SEND_FAIL, "");
                    }
                    break;
                case OPERATION_HEARTBEAT:
                    cancelCheckHeartbeat();
                    for (OnBleEventListener listener : mListener){
                        listener.onCommandNotification(OPERATION_HEARTBEAT, BleConstants.OPERATION_ERR_HEARTBEAT_TIMEOUT, "");
                    }
            }
        }
    };

    private Handler mHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int operation = mOperation;
            switch (msg.what) {
                case MSG_OPERATION_TIME_OUT:
                    doDisconnect();
                    int errCode = BleConstants.OPERATION_ERR_TIME_OUT;
                    mOperation = OPERATION_NONE;
                    for (OnBleEventListener listener : mListener) {
                        listener.onCommandNotification(operation, errCode, "");
                    }
                    break;
                case MSG_OPERATION_COMMON_ERR:
                    removeCallbacksAndMessages(null);
                    if (operation == OPERATION_CONNECT || mConnectionState == STATE_DISCONNECTED) {
                        doDisconnect();
                    }
                    mOperation = OPERATION_NONE;
                    for (OnBleEventListener listener : mListener) {
                        listener.onCommandNotification(operation, (Integer) msg.obj, "");
                    }
                    break;
                case MSG_OPERATION_START_CONNECT:
                    BluetoothDevice device;
                    Log.d(TAG, "Trying to create a new connection.");
                    try {
                        resetTimeout();
                        device = mBluetoothAdapter.getRemoteDevice(mBluetoothDeviceAddress);
                        mBluetoothGatt = device.connectGatt(mContext, false, mGattCallback);
                        mConnectionState = STATE_CONNECTING;
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d(TAG, "Wrong BLE address");
                    }
                case MSG_OPERATION_CONNECTED:
                    removeCallbacksAndMessages(null);
                    mOperation = OPERATION_NONE;
                    for (OnBleEventListener listener : mListener) {
                        listener.onCameraConnected(mBluetoothDeviceAddress);
                    }
                    break;
                case MSG_AUTHORIZATION_HINT:
                    for (OnBleEventListener listener : mListener) {
                        listener.onCommandNotification(OPERATION_MAC, BleConstants.RETURN_TYPE_AUTH_HINT, "");
                    }
                    break;
            }
        }
    };


    /**--------------分包发送开始----------------*/
    private byte[] getStartPackageData(){
        return new byte[]{(byte) 0xfe, (byte) mTotalIndex};
    }

    private byte[] getEndPackageData(){
        return new byte[]{(byte) 0xff};
    }

    private byte[] getDataPackage(String data){
        return (new String(new byte[]{(byte) mCurrentIndex, (byte) data.length()}) + data).getBytes();
    }

    private int mCurrentIndex = 0;
    private int mTotalIndex = 0;
    private volatile boolean mCurrentSend = true;//当前package发送完毕
    private volatile boolean mCurrentMessageSendOver = true;//当前整条消息发送完毕

    private void writeCharacteristicSlice(String uuid, JSONObject jsonObject, int operation){
        try {
            jsonObject.put(BleConstants.COMMON_TOKEN, mToken);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        addMessage(new BleMessage(uuid, jsonObject.toString(), operation));
    }

    private void writeCharacteristicSlice(String uuid, String value) {
        BleLogger.print2File(TAG, "slice write uuid: " + uuid + " value:" + value);
        mTotalIndex = value.length() / 18 + 1;
        mCurrentIndex = 0;
        mCurrentMessageSendOver = false;

        while (mCurrentIndex <= mTotalIndex + 1){
            mCurrentSend = false;

            if (mCurrentIndex == 0){
                writeCharacteristic(uuid, getStartPackageData());
            } else if (mCurrentIndex == mTotalIndex + 1){
                mCurrentMessageSendOver = true;
                writeCharacteristic(uuid, getEndPackageData());
            } else if (mCurrentIndex == mTotalIndex){//最后一个数据包
                writeCharacteristic(uuid, getDataPackage(value.substring((mCurrentIndex-1) * 18)));
            } else if (mCurrentIndex < mTotalIndex){
                writeCharacteristic(uuid, getDataPackage(value.substring((mCurrentIndex-1) * 18, mCurrentIndex * 18)));
            }
            ++mCurrentIndex;

            int sleepTime = 0;

            while (!mCurrentSend){
                try {
                    Thread.sleep(50);
                    sleepTime += 50;
                    if (sleepTime > 3000){
                        break;
                    }
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
            }

            if (!mCurrentSend){
                //send timeout
                break;
            }
        }
    }

    private void writeCharacteristic(String uuid, byte[] value) {
        BleLogger.print(TAG, "partial write uuid: " + uuid + " value:" + getData(value));
        if (mBluetoothAdapter == null) {
            BleLogger.print2File(TAG, "write fail, BluetoothAdapter not initialized");
            return;
        }
        if (mBluetoothGatt == null) {
            BleLogger.print2File(TAG, "write fail, BluetoothGatt not initialized");
            return;
        }
        BluetoothGattService service = mBluetoothGatt.getService(UUID.fromString(UUID_Service_Control));
        if (service != null) {
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(uuid));
            if (characteristic != null) {
                characteristic.setValue(value);
                mBluetoothGatt.writeCharacteristic(characteristic);
            } else {
                BleLogger.print2File(TAG, "Characteristic not found, uuid: " + uuid);
                mHandler.sendMessage(mHandler.obtainMessage(MSG_OPERATION_COMMON_ERR, BleConstants.OPERATION_ERR_CHARACTERISTIC_NOT_FOUND));
            }
        } else {
            BleLogger.print2File(TAG, "Service not found, uuid: " + UUID_Service_Control);
            mHandler.sendMessage(mHandler.obtainMessage(MSG_OPERATION_COMMON_ERR, BleConstants.OPERATION_ERR_SERVICE_NOT_FOUND));
        }
    }

    /**-------------分包发送结束---------------*/


    /**-------------接收数据分包开始------------*/
    private int mCurReceiveIndex = 0;
    private int mTotalReceive = 0;
    private String mReceiveData = "";
    private void processNotification(BluetoothGattCharacteristic characteristic){
        BleLogger.print(TAG, "processNotification uuid: " + characteristic.getUuid() + " value:" + getData(characteristic));

        byte[] value = characteristic.getValue();
        if (/*value.length == 2 && */ value[0] == (byte) 0xfe || value[0] == (byte) 0xFE){
            BleLogger.print(TAG, "processNotification uuid: " + characteristic.getUuid() + " start.");
            mTotalReceive = (int) value[1];
            mReceiveData = "";
        } else if (/*value.length == 1 && */ value[0] == (byte) 0xff || value[0] == (byte) 0xFF){
            BleLogger.print(TAG, "processNotification uuid: " + characteristic.getUuid() + " end.");
            processNotification(characteristic.getUuid().toString(), mReceiveData);
        } else if (/*value.length > 2 && */(int)value[0] <= mTotalReceive){
            mCurReceiveIndex = (int) value[0];
            int len = (int) value[1];
            byte[] data = new byte[len];
            for (int i = 0; i < len; ++i){
                data[i] = value[i+2];
            }
            mReceiveData += new String(data);
        }
    }

    private void processNotification(String uuid, String value){
        BleLogger.print2File(TAG, "processNotification uuid: " + uuid + " value:" + value);
        if (value.isEmpty()){
            BleLogger.print2File(TAG, "processNotification: value empty! ");
            return;
        }

        JSONObject json = null;
        int type = BleConstants.RETURN_TYPE_UNKNOWN;
        int rval = BleConstants.RETURN_RVAL_UNKNOWN;
        String param = "";
        try {
            json = new JSONObject(value);
            type = json.getInt(BleConstants.COMMON_TYPE);
            rval = json.getInt(BleConstants.COMMON_RET_VALUE);
            param = json.optString(BleConstants.COMMON_PARAM);
        } catch (JSONException e){
            e.printStackTrace();
        }
        if (type == BleConstants.RETURN_TYPE_UNKNOWN || rval == BleConstants.RETURN_RVAL_UNKNOWN){
            BleLogger.print2File(TAG, "processNotification: no return type or rval ! ");
            return;
        }

        if (!mIsCameraConnected){
            BleLogger.print2File(TAG, "get notification but camera disconnected, abandon it!");
            return;
        }

        if (type == BleConstants.COMMON_TYPE_NOTIFICATION_VALUE
                && (rval == BleConstants.COMMON_RVAL_LIVE_MAC_PERMISSION_OK || rval == BleConstants.COMMON_RVAL_LIVE_MAC_PERMISSION_ERR)){
            BleLogger.print2File(TAG, "get notification about permission, result : " + rval + "(18 for ok and 19 for error)");
        } else if (type != BleConstants.COMMON_TYPE_SESSION_VALUE && !mIsSessionStarted){
            BleLogger.print2File(TAG, "get notification but session unstarted yet and current is not about session, abandon it!");
            return;
        }

        if (type == BleConstants.COMMON_TYPE_SETTING_VALUE){
            handleMessageCallback(uuid, rval, param);
        } else if (type == BleConstants.COMMON_TYPE_NOTIFICATION_VALUE){
            handleSettingCallback(uuid, rval, param);
        } else if (type == BleConstants.COMMON_TYPE_SESSION_VALUE){
            handleStartSessionCallback(uuid, rval, param);
        } else if (type == BleConstants.COMMON_TYPE_HEARTBEAT_VALUE){
            handleHeartbeatCallback(uuid, rval, param);
        }
    }

    /**-------------接收数据分包结束------------*/

    /**-------------消息发送处理线程 start------------*/

    private LinkedBlockingQueue<BleMessage> mMessageQueue;
    private BleMessage mCurrentMessage;
    private MessageSendThread mMessageSendThread;

    public void startMessageThread() {
        stopMessageThread();
        BleLogger.print2File(TAG, "startMessageThread.");
        if (mMessageQueue == null) {
            mMessageQueue = new LinkedBlockingQueue<>();
        } else {
            mMessageQueue.clear();
        }

        if (mMessageSendThread == null || !mMessageSendThread.isAlive()) {
            mMessageSendThread = new MessageSendThread();
            mMessageSendThread.start();
        }
    }

    private void stopMessageThread() {
        if (mMessageSendThread != null) {
            BleLogger.print2File(TAG, "stopMessageThread.");
            mMessageSendThread.stop = true;
            mMessageSendThread.interrupt();
            try {
                mMessageSendThread.join(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mMessageSendThread = null;
        }
    }

    private void clearMessageQueue(){
        BleLogger.print2File(TAG, "clearMessageQueue, current size : " + (mMessageQueue == null ? 0 : mMessageQueue.size()));
        if (mMessageQueue != null){
            mMessageQueue.clear();
        }
        mCurrentMessageSendOver = true;
    }

    private class MessageSendThread extends Thread {

        private boolean stop = false;

        MessageSendThread(){

        }

        @Override
        public void run() {
            super.run();
            while (!stop){
                if (mMessageQueue.size() > 0 && mCurrentMessageSendOver && mCurrentSend){
                    mCurrentMessage = mMessageQueue.poll();
                    if (mCurrentMessage == null){
                        continue;
                    }
                    //如果尚未连接蓝牙则不处理
                    if (!mIsCameraConnected && mCurrentMessage.operation != OPERATION_START_SESSION){
                        BleLogger.print2File(TAG, "sendMessage fail. ble unconnected");
                        continue;
                    }
                    //如果session尚未建立且命令不是start session和通知mac，则不处理
                    if (mCurrentMessage.operation != OPERATION_MAC && !mIsSessionStarted && mCurrentMessage.operation != OPERATION_START_SESSION){
                        BleLogger.print2File(TAG, "sendMessage fail. session unstarted and current is not start session");
                        continue;
                    }

                    sendMessage(mCurrentMessage);
                } else {
                    BleLogger.print2File(TAG, "sendMessage fail. queue.size:" + mMessageQueue.size()
                            + " , currentMessageOver:" + mCurrentMessageSendOver
                            + " , currentSend:" + mCurrentSend);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
            clearMessageQueue();
            cancelCheckHeartbeat();
            mCurrentMessage = null;
        }
    }

    private void sendMessage(BleMessage message){
        writeCharacteristicSlice(message.uuid, message.value);
    }

    private void addMessage(BleMessage message){
        BleLogger.print2File(TAG, "add message to queue : " + message.value);
        if (!mMessageQueue.contains(message)){
            mMessageQueue.add(message);
        } else {
            BleLogger.print2File(TAG, "add fail. already added.");
        }
    }

    /**-------------消息发送处理线程 end------------*/
}
