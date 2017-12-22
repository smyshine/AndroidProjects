package com.customview.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

import com.customview.R;
import com.customview.activity.transformer.TransformerHelper;
import com.zhy.base.adapter.ViewHolder;
import com.zhy.base.adapter.recyclerview.CommonAdapter;
import com.zhy.base.adapter.recyclerview.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerActivity extends AppCompatActivity {

    private static final String TAG = "ViewPagerActivity";

    public static final String EFFECT_TYPE = "effect";
    private static final String VERTICAL_PAGER = "vertical";

    int[] mColor = {R.color.a, R.color.b, R.color.c, R.color.d, R.color.e, R.color.f, R.color.g,
            R.color.a, R.color.b, R.color.c, R.color.d, R.color.e, R.color.f, R.color.g};
    int mCurrentPostion = 0;
    List<ImageView> mImageViews = new ArrayList<>();
    Animation mAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pager);

        int effect = getIntent().getIntExtra(EFFECT_TYPE, 0);
        boolean vertical = getIntent().getBooleanExtra(VERTICAL_PAGER, false);

        initMenu();

        ViewPager viewPager;
        if (vertical){
            viewPager = (VerticalViewPager) findViewById(R.id.verticalViewPager);
            viewPager.setOverScrollMode(View.OVER_SCROLL_NEVER);
        } else {
            viewPager = (ViewPager) findViewById(R.id.viewPager);
        }
        viewPager.setVisibility(View.VISIBLE);
        viewPager.setPageMargin(1);
        viewPager.setOffscreenPageLimit(5);

        final float factor = 0.5f;
        mAnimation = new ScaleAnimation(0.0f, 1.0f, 0.9f, 1.0f, 150, 450);
        mAnimation.setDuration(500);
        mAnimation.setFillAfter(true);
        mAnimation.setInterpolator(new Interpolator() {
            @Override
            public float getInterpolation(float input) {
                return (float) (Math.pow(2, -10 * input) * Math.sin((input - factor / 4) * (2 * Math.PI) / factor) + 0.9f);
            }
        });

        for (int i = 0; i < mColor.length; i++) {
            ImageView imageView = new ImageView(getApplicationContext());
            imageView.setImageResource(mColor[i]);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(100, 100));
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            mImageViews.add(imageView);
        }

        viewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return mColor.length;
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                ImageView imageView = mImageViews.get(position);
                container.addView(imageView);
                return imageView;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView((View) object);
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Log.d(TAG, "onPageScrolled: ");
            }

            @Override
            public void onPageSelected(int position) {
                Log.d(TAG, "onPageSelected: ");
                mImageViews.get(mCurrentPostion).clearAnimation();
                mCurrentPostion = position;
                mImageViews.get(mCurrentPostion).startAnimation(mAnimation);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                Log.d(TAG, "onPageScrollStateChanged: ");
            }
        });

        viewPager.setPageTransformer(true, TransformerHelper.getTransformer(effect));
    }

    private void initMenu(){
        CommonAdapter<String> adapter = new CommonAdapter<String>(this, R.layout.item, TransformerHelper.getTransformerList()){
            @Override
            public void convert(ViewHolder holder, String o) {
                holder.setText(R.id.id_info, o);
            }
        };
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(ViewGroup parent, View view, Object o, int position) {
                Intent intent = new Intent(ViewPagerActivity.this, ViewPagerActivity.class);
                intent.putExtra(EFFECT_TYPE, position + 1);
                intent.putExtra(VERTICAL_PAGER, position == TransformerHelper.VERTICAL_PAGER_INDEX);
                startActivity(intent);
                finish();
            }

            @Override
            public boolean onItemLongClick(ViewGroup parent, View view, Object o, int position) {
                return false;
            }
        });

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(adapter);

    }
}
