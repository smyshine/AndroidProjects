package com.example.smy.vrplayer;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import java.io.File;

public class SurfaceViewTest extends Activity{

    private SurfaceView surfaceView1;
    private SurfaceView surfaceView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_surface_view_test);

        surfaceView1 = (SurfaceView) findViewById(R.id.sf_1);
        surfaceView1.getHolder().addCallback(callback);
        surfaceView1.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        surfaceView2 = (SurfaceView) findViewById(R.id.sf_2);
        surfaceView2.setBackground(LayoutToDrawable(R.layout.picture_setting_float));

        //surfaceView1.setZOrderOnTop(true);
        //surfaceView1.getHolder().setFormat(PixelFormat.TRANSPARENT);

        //play(0, "/storage/emulated/0/smy/test.mp4");
    }

    private SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            play(0, "/storage/emulated/0/smy/test.mp4");
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            //size

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (mediaPlayer != null && mediaPlayer.isPlaying())
            {
                mediaPlayer.stop();
            }
        }
    };

    private void play(final int m, String path)
    {
        File file = new File(path);
        if (!file.exists())
        {
            Toast.makeText(this, "File path error", Toast.LENGTH_SHORT).show();
            return;
        }

        try
        {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(file.getAbsolutePath());
            mediaPlayer.setDisplay(surfaceView1.getHolder());
            mediaPlayer.prepareAsync();

            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mediaPlayer.start();
                    mediaPlayer.seekTo(m);
                }
            });

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    Toast.makeText(getApplicationContext(), "video play competed", Toast.LENGTH_SHORT).show();
                }
            });

            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    play(0, "/storage/emulated/0/smy/test.mp4");
                    Toast.makeText(getApplicationContext(), "video error 2", Toast.LENGTH_SHORT).show();
                    return false;
                }
            });
        }
        catch (Exception e)
        {
            Toast.makeText(getApplicationContext(), "video error 3", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private MediaPlayer mediaPlayer;

    public Bitmap convertLayoutToBitmap(int layoutId){
        View view = getLayoutInflater().inflate(/*R.layout.picture_setting_float*/layoutId, null);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        Log.d("SDBG","get measure : " + width + " ;  " + height);
        int widthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
        view.measure(widthSpec, heightSpec);
        view.layout(0, 0, width, height);

        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();

        return bitmap;
    }

    public Drawable LayoutToDrawable(int  layout_id ){
        Bitmap snapshot = convertLayoutToBitmap(layout_id);
        Drawable drawable = new BitmapDrawable(snapshot);
        return drawable;
    }

    public static Bitmap convertViewToBitmap(View view, int size) {
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        int width = size*40;

        view.layout(0, 0, width, view.getMeasuredHeight());  //根据字符串的长度显示view的宽度
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
        return bitmap;
    }
}
