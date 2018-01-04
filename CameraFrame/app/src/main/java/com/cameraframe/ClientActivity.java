package com.cameraframe;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.h264player.IPlayer;
import com.pppcommon.PPPHead;
import com.tnpclient.ClientPlayer;
import com.tnpclient.SQLCache;
import com.pppcommon.PPPPCallback;
import com.tnpclient.TNPClient;

import java.io.IOException;

import smy.com.cameraframe.R;

public class ClientActivity extends AppCompatActivity {
    private static final String TAG = "ClientActivity";

    private TextView tvLog;
    private TextureView textureView;
    private TNPClient client = null;
    private ClientPlayer player = null;

    private boolean playerSet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_client);

        tvLog = findViewById(R.id.log);
        tvLog.setMovementMethod(new ScrollingMovementMethod());

        textureView = findViewById(R.id.video_view);
        textureView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        client.startClient();
                    }
                }).start();
            }

            @Override
            public void onViewDetachedFromWindow(View v) {

            }
        });

        player = new ClientPlayer(this, textureView);

        client = new TNPClient(new PPPPCallback() {
            @Override
            public void log(final String message) {
                logMessage(message);
            }

            @Override
            public void onData(byte[] data) {
                onFrameData(data);
            }

            @Override
            public void onHead(PPPHead head) {
                initPlayer(head.getWidth(), head.getHeight(), head.getBitRate(), head.getFrameRate(), head.getiInterval());
            }
        });
    }

    private void initPlayer(int width, int height, int bitRate, int frameRate, int interval) {
        try {
            String mine_type = "video/avc";

            MediaFormat mediaFormat = MediaFormat.createVideoFormat(mine_type, width, height);
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate);
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, interval);

            player.setupCache(new SQLCache(this));
            player.setup(mine_type, mediaFormat);
            player.setupPCM(AudioManager.STREAM_MUSIC, 8000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, AudioTrack.MODE_STREAM);
            playerSet = true;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (client != null) {
            client.stopClient();
        }
        if (player != null && playerSet) {
            player.stop();
        }
    }

    private void onFrameData(byte[] data) {
        if (player != null && playerSet) {
            Log.d(TAG, "onFrameData: player add frame, " + data.length);
            player.addAVFrame(IPlayer.AVFRAME_TYPE.VIDEO, data, System.currentTimeMillis() / 1000, -1);
        }
    }

    private void logMessage(final String msg) {
        Log.d(TAG, "smy: " + msg);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvLog.append("\n" + msg);
            }
        });
    }
}
