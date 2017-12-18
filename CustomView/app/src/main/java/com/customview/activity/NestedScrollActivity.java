package com.customview.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.customview.R;
import com.customview.TabsFragment;
import com.customview.view.SimpleViewPagerIndicator;

public class NestedScrollActivity extends AppCompatActivity {

    private String[] mTitles = new String[]{"Intro", "Judge", "About"};
    private SimpleViewPagerIndicator mIndicator;
    private ViewPager mViewPager;
    private FragmentPagerAdapter mAdapter;
    private TabsFragment[] mFragments = new TabsFragment[mTitles.length];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nested_scroll);

        initViews();
        initDatas();
        initEvents();
    }

    private void initViews(){
        mIndicator = (SimpleViewPagerIndicator) findViewById(R.id.id_stickynavlayout_indicator);
        mViewPager = (ViewPager) findViewById(R.id.id_stickynavlayout_viewpager);
    }

    private void initDatas(){
        mIndicator.setTitles(mTitles);

        for (int i = 0; i < mTitles.length; i++) {
            mFragments[i] = (TabsFragment) TabsFragment.newInstance(mTitles[i]);
        }
        mAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return mFragments[position];
            }

            @Override
            public int getCount() {
                return mTitles.length;
            }
        };
        mViewPager.setAdapter(mAdapter);
        mViewPager.setCurrentItem(0);
    }

    private void initEvents(){
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                mIndicator.scroll(position, positionOffset);
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }
}
