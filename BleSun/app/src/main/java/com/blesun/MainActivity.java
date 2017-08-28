package com.blesun;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.lib.ble.manager.BleLogger;
import com.lib.ble.manager.BleManager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ListView mListView;
    ListAdapter mListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListView = (ListView) findViewById(R.id.listView);
        mListAdapter = new ListAdapter();
        mListView.setAdapter(mListAdapter);

        BleManager.getInstance().initialize(this);

        if (!BleManager.isBleSupported(this)){
            Toast.makeText(this, "Ble unsupported", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!BleManager.getInstance().isBleEnabled()){
            BleManager.getInstance().enableBle(this);
        }

        BleManager.getInstance().startScan(mLeScanCallback);
        mStartTime = System.currentTimeMillis();
    }

    private long mStartTime;
    private List<BluetoothDevice> mDeviceList = new ArrayList<>();
    private List<Long> mLapse = new ArrayList<>();
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            BleLogger.print("smy", "Found BLE device:" + device.getName() + " mac:" + device.getAddress()
                    + " rssi:" + rssi);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String name = device.getName();
                    if (name == null)
                        return;
                    if (!isScannedDevice(device)) {
                        mDeviceList.add(device);
                        mLapse.add(getLapse());
                        mListAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    };

    private boolean isScannedDevice(BluetoothDevice device) {
        for (BluetoothDevice item : mDeviceList) {
            if (item.getAddress().equals(device.getAddress()))
                return true;
        }
        return false;
    }

    private long getLapse(){
        return System.currentTimeMillis() - mStartTime;
    }

    private class ListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mDeviceList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = View.inflate(MainActivity.this, R.layout.ble_item, null);
                viewHolder.tvMain = (TextView) convertView.findViewById(R.id.main);
                viewHolder.tvSub = (TextView) convertView.findViewById(R.id.sub);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            BluetoothDevice device = mDeviceList.get(position);

            viewHolder.tvMain.setText(device.getName());
            viewHolder.tvSub.setText(device.getAddress() + "      " + mLapse.get(position));

            return convertView;
        }

        public class ViewHolder {
            public TextView tvMain;
            public TextView tvSub;
        }
    }
}
