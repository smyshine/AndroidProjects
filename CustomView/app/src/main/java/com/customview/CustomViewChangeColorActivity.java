package com.customview;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewConfiguration;

import com.customview.view.CustomViewChangeColorIcon;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class CustomViewChangeColorActivity extends FragmentActivity implements ViewPager.OnPageChangeListener , View.OnClickListener{

    private ViewPager mViewPager;
    private List<Fragment> mTabs = new ArrayList<>();
    private FragmentPagerAdapter mAdapter;

    private String[] mTitles = new String[]{"Home Fragment", "Album Fragment", "Setting Fragment"};

    private List<CustomViewChangeColorIcon> mTabIndicator = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_view_change_color);

        setOverflowShowingAlways();
        if (getActionBar() != null){
            getActionBar().setDisplayShowHomeEnabled(false);
        }
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        initData();

        mViewPager.setAdapter(mAdapter);
        mViewPager.addOnPageChangeListener(this);
    }

    private void setOverflowShowingAlways()
    {
        try {
            // true if a permanent menu key is present, false otherwise.
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class
                    .getDeclaredField("sHasPermanentMenuKey");
            menuKeyField.setAccessible(true);
            menuKeyField.setBoolean(config, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initData(){
        for (String title : mTitles){
            TabFragment tabFragment = new TabFragment();
            Bundle args = new Bundle();
            args.putString("title", title);
            tabFragment.setArguments(args);
            mTabs.add(tabFragment);
        }

        mAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return mTabs.get(position);
            }

            @Override
            public int getCount() {
                return mTabs.size();
            }
        };

        initTabIndicator();
    }

    private void initTabIndicator()
    {
        CustomViewChangeColorIcon one = (CustomViewChangeColorIcon) findViewById(R.id.ccHome);
        CustomViewChangeColorIcon two = (CustomViewChangeColorIcon) findViewById(R.id.ccAlbum);
        CustomViewChangeColorIcon three = (CustomViewChangeColorIcon) findViewById(R.id.ccSetting);

        mTabIndicator.add(one);
        mTabIndicator.add(two);
        mTabIndicator.add(three);

        one.setOnClickListener(this);
        two.setOnClickListener(this);
        three.setOnClickListener(this);

        one.setIconAlpha(1.0f);
    }

    /**
     * 重置其他的Tab
     */
    private void resetOtherTabs()
    {
        for (int i = 0; i < mTabIndicator.size(); i++)
        {
            mTabIndicator.get(i).setIconAlpha(0);
        }
    }

    @Override
    public void onClick(View v) {
        resetOtherTabs();
        switch (v.getId()){
            case R.id.ccHome:
                mTabIndicator.get(0).setIconAlpha(1.0f);
                mViewPager.setCurrentItem(0, false);
                break;
            case R.id.ccAlbum:
                mTabIndicator.get(1).setIconAlpha(1.0f);
                mViewPager.setCurrentItem(1, false);
                break;
            case R.id.ccSetting:
                mTabIndicator.get(2).setIconAlpha(1.0f);
                mViewPager.setCurrentItem(2, false);
                break;
        }
    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (positionOffset > 0)
        {
            CustomViewChangeColorIcon left = mTabIndicator.get(position);
            CustomViewChangeColorIcon right = mTabIndicator.get(position + 1);

            left.setIconAlpha(1 - positionOffset);
            right.setIconAlpha(positionOffset);
        }
    }
}
