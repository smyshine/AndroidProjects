package com.example.smy.vrplayer.VRView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.example.smy.vrplayer.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by SMY on 2016/7/13.
 */
public class VRSettingView extends FrameLayout{

    private CustomCardboardView mSurfaceView;
    private View view;

    private final int DOWNLOAD_PICTURE_SUCCESS = 0;
    private final int DOWNLOAD_PICTURE_FAILED = 1;

    private Bitmap mPicture;

    public VRSettingView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public VRSettingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        view = LayoutInflater.from(getContext()).inflate(R.layout.widget_vr_setting, null);
        mSurfaceView = (CustomCardboardView) view.findViewById(R.id.setSurfaceView);
        mSurfaceView.setTransparent(true);
        //mSurfaceView.setAntiAliasingMode(ISurface.ANTI_ALIASING_CONFIG.MULTISAMPLING);
        //mSurfaceView.setSampleCount(2);

        mSurfaceView.initRender(false);
        mSurfaceView.setUseSensor(true);
        mSurfaceView.setVRModeEnabled(true);

        addView(view, new FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT));
    }

    public CustomCardboardView getSurfaceView(){
        return mSurfaceView;
    }

    public void setScale(int width, int height){
        mSurfaceView.setScale(width, height);
    }

    public void setDataSource(String url)
    {
        loadBitmap(url);
    }

    public void setDataSource(Bitmap bitmap)
    {
        mSurfaceView.setDataSource(bitmap);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWNLOAD_PICTURE_SUCCESS:
                    mSurfaceView.setDataSource(mPicture);
                    break;
                case DOWNLOAD_PICTURE_FAILED:
                    //Toast.makeText(getContext(), R.string.pic_edit_filter_load_sticker_failed, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    /**
     * @param urlpath
     * @return Bitmap
     * 根据图片url获取图片对象
     */
    public static Bitmap getBitMBitmap(String urlpath) {
        Bitmap map = null;
        try {
            URL url = new URL(urlpath);
            URLConnection conn = url.openConnection();
            conn.connect();
            InputStream in;
            in = conn.getInputStream();
            map = BitmapFactory.decodeStream(in);
            // TODO Auto-generated catch block
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }
    /**
     * @param urlpath
     * @return Bitmap
     * 根据url获取布局背景的对象
     */
    public static Drawable getDrawable(String urlpath){
        Drawable d = null;
        try {
            URL url = new URL(urlpath);
            URLConnection conn = url.openConnection();
            conn.connect();
            InputStream in;
            in = conn.getInputStream();
            d = Drawable.createFromStream(in, "background.jpg");
            // TODO Auto-generated catch block
        } catch (IOException e) {
            e.printStackTrace();
        }
        return d;
    }

    public static Bitmap getBitmapFromFile(String path){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        return bitmap;
    }

    private void loadBitmap(final String url){
        new Thread() {
            @Override
            public void run() {
                mPicture = getBitmapFromFile(url);
                if (mPicture == null) {
                    handler.sendEmptyMessage(DOWNLOAD_PICTURE_FAILED);
                    return;
                }
                handler.sendEmptyMessage(DOWNLOAD_PICTURE_SUCCESS);
            }
        }.start();
    }

}
