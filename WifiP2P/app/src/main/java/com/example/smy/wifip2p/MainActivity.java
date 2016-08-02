package com.example.smy.wifip2p;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.example.smy.wifip2p.BroadcastReceiver.WiFiDirectBroadcastReceiver;
import com.example.smy.wifip2p.Task.FileServerAsyncTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends Activity {

    WifiP2pManager manager;
    WifiP2pManager.Channel channel;
    BroadcastReceiver receiver;
    IntentFilter intentFilter;
    WifiP2pManager.PeerListListener peerListListener;
    WifiP2pManager.ConnectionInfoListener connectionInfoListener;
    private WifiP2pInfo info;

    RecyclerView recyclerView;
    WifiResultAdapter adapter;
    private List peers = new ArrayList();
    private List<HashMap<String, String>> peersshow = new ArrayList();

    private StringBuffer mStringBuffer = new StringBuffer();
    TextView tvLog;

    private FileServerAsyncTask mFileServerTask = null;

    SocketConnection socketDataConnection = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        tvLog = (TextView) findViewById(R.id.tvLog);
        tvLog.setMovementMethod(new ScrollingMovementMethod());

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
        peerListListener = new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList listPeers) {
                peers.clear();
                peersshow.clear();
                Collection<WifiP2pDevice> aList = listPeers.getDeviceList();
                peers.addAll(aList);

                for (int i = 0; i < aList.size(); i++) {
                    WifiP2pDevice a = (WifiP2pDevice) peers.get(i);
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put("name", a.deviceName);
                    map.put("address", a.deviceAddress);
                    peersshow.add(map);
                }
                adapter = new WifiResultAdapter(peersshow);
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                adapter.SetOnItemClickListener(new OnWifiItemClickListener() {
                    @Override
                    public void OnItemClick(View view, int position) {
                        connectPeer(peersshow.get(position).get("address"), peersshow.get(position).get("name"));
                    }
                });
            }
        };
        connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
            @Override
            public void onConnectionInfoAvailable(WifiP2pInfo winfo) {
                info = winfo;
                showlog("Connection info available");
                if (socketDataConnection == null){
                    socketDataConnection = new SocketConnection(MainActivity.this);
                }
                showlog("start socket.");

                if (info.groupFormed && info.isGroupOwner) {
                    showlog("I'm group owner.");
                    socketDataConnection.start("", 7878, true);
                }else{
                    socketDataConnection.start(info.groupOwnerAddress.getHostAddress(), 7878, false);
                }

                findViewById(R.id.recyclerView).setVisibility(View.GONE);
                findViewById(R.id.ll_send_file).setVisibility(View.VISIBLE);
            }
        };
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this, peerListListener, connectionInfoListener);

        if (intentFilter == null){
            intentFilter = new IntentFilter();
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
            registerReceiver(receiver, intentFilter);
        }

    }

    public void showlog(final String s){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mStringBuffer.append(s);
                mStringBuffer.append("\n");
                tvLog.setText(mStringBuffer.toString());
            }
        });
    }

    public void onDeviceConnected(){
        findViewById(R.id.ll_connect).setEnabled(false);
    }

    public void onDeviceDisconnected(){
        findViewById(R.id.ll_connect).setEnabled(true);
        findViewById(R.id.ll_send_file).setVisibility(View.INVISIBLE);
        findViewById(R.id.recyclerView).setVisibility(View.VISIBLE);
        if(socketDataConnection != null){
            socketDataConnection.stop();
            socketDataConnection = null;
        }
    }

    public boolean isScanning = false;
    public void onClickScanP2PDevice(View v){
        if (!isScanning){
            discoverPeers();
            findViewById(R.id.ll_send_file).setVisibility(View.INVISIBLE);
        }
    }

    public void onClickStopScanDevice(View v){
        if (isScanning){
            stopDiscoverPeers();
        }
    }

    public void onClickBeGroupOwener(View v){
        BeGroupOwener();
    }

    public void onClickDisconnect(View v){
        DisconnectPeers();
    }

    public void discoverPeers(){
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int reason) {
            }
        });
    }

    private void stopDiscoverPeers() {
        manager.stopPeerDiscovery(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int reason) {
            }
        });
    }

    private void DisconnectPeers() {
        manager.removeGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int reason) {

            }
        });
        findViewById(R.id.ll_send_file).setVisibility(View.INVISIBLE);
    }

    private void BeGroupOwener(){
        manager.createGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                showlog("Become group owener success.");
            }

            @Override
            public void onFailure(int reason) {
                showlog("Become group owener fail.");
            }
        });
    }

    private String connectPeerAddress;
    private void connectPeer(final String address, final String name) {
        connectPeerAddress = address;
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = address;
        config.wps.setup = WpsInfo.PBC;
        manager.connect(channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                showlog("Connect peer invite success(" + address + ":" + name + ").");
            }

            @Override
            public void onFailure(int reason) {
                showlog("Connect peer invite fail(" + address + ":" + name + ").");
            }
        });
    }


    public static final int SEND_PICTURE_PICK = 92;
    public void onClickSendPicture(View v){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, SEND_PICTURE_PICK);
    }

    public void onClickSendData(View v){
        socketDataConnection.sendMessage("Hello World");
    }

    public void onReceiveMessage(String message){
        showlog("receive message : " + message);
        if(message.equals("FILE")){
            socketDataConnection.receiveFile(info.groupOwnerAddress.getHostAddress(), 8787);
            socketDataConnection.sendMessage("FILE_OK");
        }else if (message.equals("FILE_OK") && uri != null){
            showlog("send file start : " + uri.toString());
            socketDataConnection.sendFile(info.groupOwnerAddress.getHostAddress(), 8787, uri.toString());
        }
    }

    Uri uri = null;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SEND_PICTURE_PICK && resultCode == RESULT_OK) {
            super.onActivityResult(requestCode, resultCode, data);
            uri = data.getData();
            //showlog("Pick file " + uri + ".");
            socketDataConnection.sendMessage("FILE");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        if(socketDataConnection != null){
            socketDataConnection.stop();
            socketDataConnection = null;
        }
    }

    public interface OnWifiItemClickListener {
        void OnItemClick(View view, int position);
    }

    public class WifiResultAdapter extends RecyclerView.Adapter<WifiResultAdapter.ItemHolder> {

        private List<HashMap<String, String>> mList;

        public WifiResultAdapter(List<HashMap<String, String>> list) {
            super();
            this.mList = list;
        }

        public OnWifiItemClickListener mOnItemClickListener;

        public void SetOnItemClickListener(OnWifiItemClickListener listener) {
            this.mOnItemClickListener = listener;
        }

        @Override
        public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            LayoutInflater inflater = LayoutInflater.from(parent.getContext());

            View view = inflater.inflate(R.layout.card_item, parent, false);
            ItemHolder itemHolder = new ItemHolder(view);
            return itemHolder;
        }

        @Override
        public void onBindViewHolder(final ItemHolder holder, final int position) {

            holder.tvname.setText(mList.get(position).get("name"));
            holder.tvaddress.setText(mList.get(position).get("address"));

            if (mOnItemClickListener != null) {
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mOnItemClickListener.OnItemClick(holder.itemView, position);
                    }

                });
            }

        }

        @Override
        public int getItemCount() {
            return mList.size();
        }

        class ItemHolder extends RecyclerView.ViewHolder {

            public TextView tvname;
            public TextView tvaddress;

            public ItemHolder(View View) {
                super(View);
                tvname = (TextView) View.findViewById(R.id.tv_name);
                tvaddress = (TextView) View.findViewById(R.id.tv_address);
            }
        }
    }

}
