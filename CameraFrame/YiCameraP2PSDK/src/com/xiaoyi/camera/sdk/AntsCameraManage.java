package com.xiaoyi.camera.sdk;

import android.text.TextUtils;

import com.xiaoyi.log.AntsLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AntsCameraManage {

    private static final String TAG = "AntsCameraManage";
    private static Map<String, AntsCamera> mAntsCameraMap = new HashMap<String, AntsCamera>();
    private static int maxSessions = 12;

    //  private static MobileAEC mMobileAEC;
    public static AntsCamera getAntsCamera(P2PDevice p2pDevice) {
        AntsCamera antsCamera = mAntsCameraMap.get(p2pDevice.uid);
        if (antsCamera == null || antsCamera.getCameraType() != p2pDevice.type) {
            if (p2pDevice.type == P2PDevice.TYPE_TUTK) {
                antsCamera = new AntsCameraTutk(p2pDevice);
            } else if (p2pDevice.type == P2PDevice.TYPE_LANGTAO) {
                antsCamera = new AntsCameraLangtao(p2pDevice);
            } else if (p2pDevice.type == P2PDevice.TYPE_TNP) {
                antsCamera = new AntsCameraTnp(p2pDevice);
            }
            mAntsCameraMap.put(p2pDevice.uid, antsCamera);
        } else {
            if (antsCamera.getCameraType() == P2PDevice.TYPE_TNP
                    && needUpdateTnpInfo(p2pDevice.tnpServerString, p2pDevice.tnpLicenseDeviceKey,
                    antsCamera.getTnpServerString(), antsCamera.getTnpLicenseDeviceKey())) {
                antsCamera.updateTnpConnectInfo(p2pDevice.tnpServerString, p2pDevice.tnpLicenseDeviceKey);
            }
        }
        return antsCamera;
    }


    public static void remove(String uid) {
        mAntsCameraMap.remove(uid);
    }

    public static void remove(AntsCamera camera) {
        camera.disconnect();
        mAntsCameraMap.remove(camera.getUID());
    }

    // 预连接所有设备
    public static void openAllCamera() {
        List<AntsCameraTnp> tnpCameraList = new ArrayList<>();

        for (AntsCamera antsCamera : mAntsCameraMap.values()) {
            AntsLog.d(TAG," uid: "+antsCamera.getUID()+" p2pid: "+antsCamera.getP2PID()+" type: "+antsCamera.getCameraType());

            if (antsCamera.getCameraType() == P2PDevice.TYPE_TUTK) {
                antsCamera.connect();
            }else if(antsCamera.getCameraType() == P2PDevice.TYPE_TNP){
                tnpCameraList.add((AntsCameraTnp) antsCamera);
            }
        }

        //对于TNP设备，预连接N-2个设备
        AntsLog.d(TAG," tnp count: "+tnpCameraList.size());
        for (int i = 0; i < tnpCameraList.size(); i++) {
            if (i < maxSessions - 2) {
                AntsCameraTnp tnpCamera=tnpCameraList.get(i);
                if (!P2PDevice.MODEL_D11.equals(tnpCamera.getDeviceModel())) {
                    tnpCamera.connect();
                }
                AntsLog.d(TAG," tnp preconnect: "+tnpCamera.getUID());
            }
        }

    }

    // 关闭所有连接
    public static void closeAllCamera() {
        for (AntsCamera antsCamera : mAntsCameraMap.values()) {
            antsCamera.disconnect();
        }
    }

    /**
     * 关闭门铃设备连接
     */
    public static void closeDoorbellCamera() {
        for (AntsCamera antsCamera : mAntsCameraMap.values()) {
            if (P2PDevice.MODEL_D11.equals(antsCamera.getDeviceModel())) {
                antsCamera.disconnect();
            }
        }
    }

    /**
     * 设置app所能连接的TNP摄像机的最大Session数
     *
     * @param maxSessionCount app所连接的tnp摄像机的最大session数
     */
    public static void setMaxSessions(int maxSessionCount) {
        maxSessions = maxSessionCount;
    }

    /**
     * 当前app已连接的tnp设备数大于maxSessions时，当再次连接Tnp设备时会提示-3017错误
     * 因此，做以下处理：当已连接的tnp设备数大于12时，将预连接时间最早且当前不在看视频
     * 的摄像头连接断开释放一个资源，然后在重新连接前述出现-3017错误Tnp设备
     *
     * @param uid    待连接的设备的uid
     */
    public static void closeEarliestConnectTnpCamera(String uid) {
        List<AntsCameraTnp> connectedTnpList = new ArrayList<>();

        for (AntsCamera antsCamera : mAntsCameraMap.values()) {
            if (antsCamera instanceof AntsCameraTnp && antsCamera.isConnected()) {
                connectedTnpList.add((AntsCameraTnp) antsCamera);
            }
        }

        AntsLog.d(TAG, " tnp connected count: " + connectedTnpList.size() + " need connect uid: " + uid);
        boolean isConnected = false;
        for (AntsCameraTnp tnp : connectedTnpList) {
            AntsLog.d(TAG, " connected tnp :  uid: " + tnp.getUID() + " p2pid: " + tnp.getP2PID()+" ts: "+tnp.getSessionEstablishedTimestamp());
            if (tnp.getUID().equals(uid)) {
                isConnected = true;
            }
        }

        if (connectedTnpList.size() < maxSessions||isConnected) {
            return;
        }

        Collections.sort(connectedTnpList, new AntsCameraTnpComparator());

        AntsCameraTnp earliestTimeTnp = connectedTnpList.get(0);
        if (earliestTimeTnp != null) {
            AntsLog.d(TAG, " disconnect tnp: uid: " + earliestTimeTnp.getUID() + " ts: " + earliestTimeTnp.getSessionEstablishedTimestamp());
            earliestTimeTnp.disconnect();
        }

    }

    private static class AntsCameraTnpComparator implements Comparator<AntsCameraTnp> {
        @Override
        public int compare(AntsCameraTnp lhs, AntsCameraTnp rhs) {
            if (lhs == null || rhs == null) {
                return 0;
            }

            if (lhs.getSessionEstablishedTimestamp() > rhs.getSessionEstablishedTimestamp()) {
                return 1;
            } else if (lhs.getSessionEstablishedTimestamp() == rhs.getSessionEstablishedTimestamp()) {
                return 0;
            } else {
                return -1;
            }

        }
    }


    private static boolean needUpdateTnpInfo(String newTnpServerString, String newTnpLicenseDeviceKey,
                                             String tnpServerString, String tnpLicenseDeviceKey){

        if(!TextUtils.isEmpty(newTnpServerString) && !TextUtils.isEmpty(newTnpLicenseDeviceKey)){
            if(!newTnpServerString.equals(tnpServerString) || !newTnpLicenseDeviceKey.equals(tnpLicenseDeviceKey)){
                return true;
            }
        }

        return false;
    }

}
