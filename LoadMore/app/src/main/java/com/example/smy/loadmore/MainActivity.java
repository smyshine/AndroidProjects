package com.example.smy.loadmore;

import android.app.ListActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends ListActivity implements AbsListView.OnScrollListener {

    private ListView listView;
    private int visibleLastIndex = 0;
    private int visibleItemCount;
    private ListViewAdapter adapter;
    private View loadMoreView;
    private Button loadMoreButton;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadMoreView = getLayoutInflater().inflate(R.layout.load_more, null);
        loadMoreButton = (Button) loadMoreView.findViewById(R.id.btnLoadmore);

        listView = getListView();

        listView.addFooterView(loadMoreView);//设置列表底层视图

        initAdapter();

        setListAdapter(adapter);

        listView.setOnScrollListener(this);
    }

    private void initAdapter()
    {
        ArrayList<String> items = new ArrayList<String>();
        for(int i = 0; i < 10; ++i)
        {
            items.add(String.valueOf(i + 1));
        }
        adapter = new ListViewAdapter(this, items);
    }

    @Override
    public void onScroll(AbsListView view, int firstvisible, int visibleitemcnt, int totalitemcnt)
    {
        this.visibleItemCount = visibleitemcnt;
        visibleLastIndex = firstvisible + visibleitemcnt - 1;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int state)
    {
        int itemLastind = adapter.getCount() - 1;
        int laseind = itemLastind + 1;
        if(state == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && visibleLastIndex == laseind)
        {
            loadData();
            adapter.notifyDataSetChanged();
            listView.setSelection(visibleLastIndex - visibleItemCount + 1);

            Log.i("LOADMORE", "loading...");
        }
    }

    public void loadMore(View view)
    {
        loadMoreButton.setText("loading...");
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadData();
                adapter.notifyDataSetChanged();
                listView.setSelection(visibleLastIndex - visibleItemCount + 1);
            }
        }, 2000);
    }

    private void loadData()
    {
        int count = adapter.getCount();
        for(int i = count; i < count + 10; ++i)
        {
            adapter.addItem(String.valueOf(i + 1));
        }
    }
}
