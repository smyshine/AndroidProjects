package com.example.smy.vrplayer;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by SMY on 2016/6/16.
 */
public class VideoSeekBar extends FrameLayout implements SeekBar.OnSeekBarChangeListener, View.OnClickListener {

    public interface OnSeekBarChangedListener{
        void onSeekBarChanged(int progress);
        void onPlayClick();
        void onScaleClick();
    }

    private SeekBar seekBar;
    private ImageView imgPlay;
    private ImageView imgScale;
    private LinearLayout llScale;
    private RelativeLayout rlLayout;
    private TextView tvTime;
    private int videoLength;
    private OnSeekBarChangedListener onSeekBarChangeListener;

    public VideoSeekBar(Context context, AttributeSet attrs, int defStype)
    {
        super(context, attrs, defStype);
        init();
    }

    public VideoSeekBar(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    private void init()
    {
        View view = View.inflate(getContext(), R.layout.widget_seek_bar, null);
        seekBar = (SeekBar) view.findViewById(R.id.seekBar);
        imgPlay = (ImageView) view.findViewById(R.id.imgPlay);
        imgScale = (ImageView) view.findViewById(R.id.imgScale);
        llScale = (LinearLayout) view.findViewById(R.id.llScale);
        rlLayout = (RelativeLayout) view.findViewById(R.id.rlLayout);
        tvTime = (TextView) view.findViewById(R.id.tvTime);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        addView(view, params);
    }

    //hide or show 全屏缩放按钮
    public void hideScaleBtn()
    {
        llScale.setVisibility(GONE);
    }
    public void showScaleBtn()
    {
        llScale.setVisibility(VISIBLE);
    }

    public void setBackgroundColor(int color)
    {
        rlLayout.setBackgroundColor(color);
    }

    public void setCompundEnable(boolean enable)
    {
        seekBar.setEnabled(enable);
        imgPlay.setEnabled(enable);
        llScale.setEnabled(enable);
    }

    public void setPlayBackfround(Drawable drawable)
    {
        imgPlay.setImageDrawable(drawable);
    }

    public void setPlayBackground(int res)
    {
        imgPlay.setImageResource(res);
    }

    public void setSeekBackground(Drawable drawable)
    {
        seekBar.setThumb(drawable);
    }

    public void setScaleBackground(Drawable drawable)
    {
        imgScale.setImageDrawable(drawable);
    }

    public void setOnSeekBarChangeListener(OnSeekBarChangedListener seekBarChangeListener)
    {
        this.onSeekBarChangeListener = seekBarChangeListener;
    }

    public void setVideoLength(int videoLength, int playPregress)
    {
        this.videoLength = videoLength;
        seekBar.setMax(videoLength);
        setProgress(playPregress);
    }

    public void setProgress(int progress)
    {
        tvTime.setText(getTimeProgressText(progress));
        seekBar.setProgress(progress);
    }

    public void setSecondaryProgress(int progress)
    {
        seekBar.setSecondaryProgress(progress);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.imgPlay:
                if (onSeekBarChangeListener != null)
                {
                    onSeekBarChangeListener.onPlayClick();
                }
                break;
            case R.id.llScale:
                if (onSeekBarChangeListener != null)
                {
                    onSeekBarChangeListener.onScaleClick();
                }
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
    {
        if (fromUser)
        {
            if (onSeekBarChangeListener != null)
            {
                onSeekBarChangeListener.onSeekBarChanged(progress);
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar)
    {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar)
    {}

    private String getTimeProgressText(int progress)
    {
        return String.format("%1$s/%2$s", setTime(progress), setTime(videoLength));
    }

    private String setTime(int time)
    {
        time = time / 1000;
        int min = time / 60;
        int second = time % 60;
        if (min < 10)
        {
            if (second < 10)
            {
                return "0" + min + ":0" + second;
            }
            else
            {
                return "0" + min + ":" + second;
            }
        }
        else
        {
            if (second < 10)
            {
                return min + ":0" + second;
            }
            else
            {
                return min + ":" + second;
            }
        }
    }

}
