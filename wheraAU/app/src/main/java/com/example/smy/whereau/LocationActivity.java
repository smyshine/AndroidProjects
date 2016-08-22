package com.example.smy.whereau;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;
import com.tencent.map.geolocation.TencentPoi;

import java.util.ArrayList;
import java.util.List;

public class LocationActivity extends Activity implements TencentLocationListener {

    public static final String LOCATION = "location";

    private TencentLocationManager mLocationManager;
    private ListView listView;
    LocationAdapter locationAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_location);
        listView = (ListView) findViewById(R.id.listView);
        locationAdapter = new LocationAdapter();
        listView.setAdapter(locationAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String result = locationAdapter.getItem(position).toString();
                Intent intent = new Intent();
                intent.putExtra(LOCATION, result);
                setResult(RESULT_OK, intent);
                onBackPressed();
            }
        });

        mLocationManager = TencentLocationManager.getInstance(this);
        // 设置坐标系为 gcj-02, 缺省坐标为 gcj-02, 所以通常不必进行如下调用
        mLocationManager.setCoordinateType(TencentLocationManager.COORDINATE_TYPE_GCJ02);

        startLocation(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 退出 activity 前一定要停止定位!
        stopLocation(null);
    }

    // ====== view listener

    // 响应点击"停止"
    public void stopLocation(View view) {
        mLocationManager.removeUpdates(this);
    }

    // 响应点击"开始"
    public void startLocation(View view) {
        // 创建定位请求
        TencentLocationRequest request = TencentLocationRequest.create()
                .setInterval(5000) // 设置定位周期
                .setRequestLevel(TencentLocationRequest.REQUEST_LEVEL_POI); // 设置定位level

        // 开始定位
        mLocationManager.requestLocationUpdates(request, this);
    }

    // ====== location callback
    @Override
    public void onLocationChanged(TencentLocation location, int error,
                                  String reason) {
        updateLocation(location);
    }

    @Override
    public void onStatusUpdate(String name, int status, String desc) {
        // ignore
    }

    // ====== location callback

    private List<String> listsName = new ArrayList<>();
    private List<String> listsAddress = new ArrayList<>();
    private void updateLocation(TencentLocation location){
        listsName.clear();
        listsAddress.clear();

        addListItem(location.getCity(), "");

        List<TencentPoi> poiList = location.getPoiList();
        int size = poiList.size();
        for (int i = 0; i < size; i++) {
            addListItem(poiList.get(i).getName(), poiList.get(i).getAddress());
        }

        addListItem(location.getStreetNo(), "");
        addListItem(location.getStreet(), "");
        addListItem(location.getVillage(), "");
        addListItem(location.getTown(), "");
        addListItem(location.getDistrict(), "");
        addListItem(location.getProvince(), "");
        addListItem(location.getNation(), "");

        locationAdapter.setLocations(listsName, listsAddress);
        locationAdapter.notifyDataSetChanged();
    }

    private void addListItem(String name, String address){
        listsName.add(name);
        listsAddress.add(address);
    }

    private class LocationAdapter extends BaseListAdapter{
        private List<String> locationNames = new ArrayList<>();
        private List<String> locationAddress = new ArrayList<>();

        public LocationAdapter() {
            super(R.layout.location_result_item);
        }

        @Override
        public int getCount() {
            return locationNames.size();
        }

        public void setLocations(List<String> names, List<String> address){
            locationNames = names;
            locationAddress = address;
        }

        @Override
        public Object getItem(int position) {
            return locationNames.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(LocationActivity.this, mResourceId, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            String name = locationNames.get(position);
            String address = locationAddress.get(position);
            if (TextUtils.isEmpty(address)){
                holder.getRelativeLayout(R.id.llLocation1).setVisibility(View.GONE);
                holder.getTextView(R.id.list_item).setVisibility(View.VISIBLE);
                holder.getTextView(R.id.list_item).setText(name);
            }else{
                holder.getRelativeLayout(R.id.llLocation1).setVisibility(View.VISIBLE);
                holder.getTextView(R.id.list_item).setVisibility(View.GONE);
                holder.getTextView(R.id.tvName).setText(name);
                holder.getTextView(R.id.tvAddress).setText(address);
            }

            return holder.getItemView();
        }

    }

    @Override
    public void onBackPressed(){
        stopLocation(null);
        finish();
    }

    public void onClickBack(View view){
        onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocation(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocation(null);
    }
}
