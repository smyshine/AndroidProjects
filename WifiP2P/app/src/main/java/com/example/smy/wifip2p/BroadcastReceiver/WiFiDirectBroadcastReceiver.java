package com.example.smy.wifip2p.BroadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;

import com.example.smy.wifip2p.MainActivity;

/**
 * Created by SMY on 2016/7/29.
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver{
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private MainActivity activity;
    WifiP2pManager.PeerListListener peerListListener;
    WifiP2pManager.ConnectionInfoListener connectionInfoListener;

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, MainActivity activity,
                                       WifiP2pManager.PeerListListener listListener,
                                       WifiP2pManager.ConnectionInfoListener infoListener){
        super();
        this.manager = manager;
        this.channel = channel;
        this.activity = activity;
        this.peerListListener = listListener;
        this.connectionInfoListener = infoListener;
    }

    @Override
    public void onReceive(Context context, Intent intent){
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)){
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED){
                activity.showlog("Wlan direct is enabled.");
            } else {
                activity.showlog("Wlan direct is disabled.");
                //wifi direct is disabled
            }
        } else if (WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION.equals(action)){
            int State = intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE, -1);
            if (State == WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED){
                activity.showlog("Scan start.");
                activity.isScanning = true;
            }
            else if (State == WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED){
                activity.showlog("Scan stop.");
                activity.isScanning = false;
            }
        }else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)){
            if (manager != null){
                manager.requestPeers(channel, peerListListener);
            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)){
            if (manager == null) {
                return;
            }
            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkInfo.isConnected()) {
                activity.showlog("Connected.");
                manager.requestConnectionInfo(channel, connectionInfoListener);
                activity.onDeviceConnected();
            } else {
                activity.showlog("Disconnected.");
                activity.onDeviceDisconnected();
                return;
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)){

        }
    }

}
