package com.example.smy.videoplayerd;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.File;
import java.net.URI;

public class MainActivity extends AppCompatActivity {

    private EditText edtPath;
    private SurfaceView svPlayer;
    private Button btnPlay;
    private Button btnPause;
    private Button btnReplay;
    private Button btnStop;
    private SeekBar seekBar;

    private MediaPlayer mediaPlayer;
    private int currentPos = 0;
    private boolean isPlaying;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtPath = (EditText) findViewById(R.id.edt_path);
        svPlayer = (SurfaceView) findViewById(R.id.surface);
        btnPlay = (Button) findViewById(R.id.btn_play);
        btnPause = (Button) findViewById(R.id.btn_pause);
        btnReplay = (Button) findViewById(R.id.btn_replay);
        btnStop = (Button) findViewById(R.id.btn_stop);
        seekBar = (SeekBar) findViewById(R.id.seekBar);

        btnPlay.setOnClickListener(btnclick);
        btnPause.setOnClickListener(btnclick);
        btnReplay.setOnClickListener(btnclick);
        btnStop.setOnClickListener(btnclick);

        svPlayer.getHolder().addCallback(callback);
        svPlayer.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        seekBar.setOnSeekBarChangeListener(change);

        findViewById(R.id.choose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileChooser();
            }
        });
    }

    private SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if(currentPos > 0)
            {
                play(currentPos);
                currentPos = 0;
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            //size
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (mediaPlayer != null && mediaPlayer.isPlaying())
            {
                currentPos = mediaPlayer.getCurrentPosition();
                mediaPlayer.stop();
            }
        }
    };

    private SeekBar.OnSeekBarChangeListener change = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            int progress = seekBar.getProgress();
            if(mediaPlayer != null && mediaPlayer.isPlaying())
            {
                mediaPlayer.seekTo(progress);
            }
        }
    };

    private View.OnClickListener btnclick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId())
            {
                case R.id.btn_play:
                    play(0);
                    break;
                case R.id.btn_pause:
                    pause();
                    break;
                case R.id.btn_replay:
                    replay();
                    break;
                case R.id.btn_stop:
                    stop();
                    break;
                default:
                    break;
            }
        }
    };


    protected void stop()
    {
        if (mediaPlayer != null &&mediaPlayer.isPlaying())
        {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            btnPlay.setEnabled(true);
            isPlaying = false;
        }
    }

    protected void play(final int m)
    {
        String path = edtPath.getText().toString();
        File file = new File(path);
        if (!file.exists())
        {
            Toast.makeText(this, "File path error", Toast.LENGTH_SHORT).show();
            return;
        }

        try
        {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(file.getAbsolutePath());
            mediaPlayer.setDisplay(svPlayer.getHolder());
            mediaPlayer.prepareAsync();

            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mediaPlayer.start();
                    mediaPlayer.seekTo(m);
                    seekBar.setMax(mediaPlayer.getDuration());
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                isPlaying = true;
                                while (isPlaying) {
                                    int current = mediaPlayer.getCurrentPosition();
                                    seekBar.setProgress(current);
                                    sleep(500);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(), "prepare video error", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }.start();
                    btnPlay.setEnabled(false);
                }
            });

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    btnPlay.setEnabled(true);
                    Toast.makeText(getApplicationContext(), "video play competed", Toast.LENGTH_SHORT).show();
                }
            });

            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    play(0);
                    isPlaying = false;
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

    protected void replay()
    {
        if (mediaPlayer != null && mediaPlayer.isPlaying())
        {
            mediaPlayer.seekTo(0);
            btnPause.setText("Pause");
            return;
        }
        isPlaying = false;
        play(0);
    }

    private void pause()
    {
        if (btnPause.getText().toString().equals("Continue"))
        {
            btnPause.setText("Pause");
            mediaPlayer.start();
            return;
        }
        if (mediaPlayer != null && mediaPlayer.isPlaying())
        {
            mediaPlayer.pause();
            btnPause.setText("Continue");
        }
    }

    private static final int FILE_SELECT_CODE = 1;
    //file choose
    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "Please Choose a Video File"), FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_SELECT_CODE)
        {
            if (resultCode == RESULT_OK)
            {
                Uri uri = data.getData();
                if (uri != null)
                {
                    String path = uri.toString();
                    if (path.toLowerCase().startsWith("file://"))
                    {
                        // Selected file/directory path is below
                        path = (new File(URI.create(path))).getAbsolutePath();
                    }
                    edtPath.setText(path);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
