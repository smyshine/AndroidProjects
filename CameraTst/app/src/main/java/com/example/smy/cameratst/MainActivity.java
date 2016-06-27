package com.example.smy.cameratst;

import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener{

    private Camera camera;

    //views
    private SeekBar seekBarXrotate;
    private SeekBar seekBarYrotate;
    private SeekBar seekBarZrotate;
    private TextView textViewXrotate;
    private TextView textViewYrotate;
    private TextView textViewZrotate;

    private SeekBar seekBarXskew;
    private SeekBar seekBarYskew;
    private TextView textViewXlean;
    private TextView textViewYlean;

    private SeekBar seekBarXzoom;
    private TextView textViewXzoom;
    private SeekBar seekBarYzoom;
    private TextView textViewYzoom;
    private SeekBar seekBarZzoom;
    private TextView textViewZzoom;

    private ImageView imageViewResult;

    //params
    private int rotateX, rotateY, rotateZ;
    private float skewX, skewY;
    private int zoomZ = -100;
    private int zoomX = -100;
    private int zoomY = -100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //camera
        camera = new Camera();

        //init views
        seekBarXrotate = (SeekBar) findViewById(R.id.sb_x_rotate);
        seekBarXrotate.setOnSeekBarChangeListener(this);
        textViewXrotate = (TextView) findViewById(R.id.tv_x_rotate_degree);
        seekBarYrotate = (SeekBar) findViewById(R.id.sb_y_rotate);
        seekBarYrotate.setOnSeekBarChangeListener(this);
        textViewYrotate = (TextView) findViewById(R.id.tv_y_rotate_degree);
        seekBarZrotate = (SeekBar) findViewById(R.id.sb_z_rotate);
        seekBarZrotate.setOnSeekBarChangeListener(this);
        textViewZrotate = (TextView) findViewById(R.id.tv_z_rotate_degree);

        seekBarXskew = (SeekBar) findViewById(R.id.sb_x_lean);
        seekBarXskew.setOnSeekBarChangeListener(this);
        textViewXlean = (TextView) findViewById(R.id.tv_x_lean_degree);
        seekBarYskew = (SeekBar) findViewById(R.id.sb_y_lean);
        seekBarYskew.setOnSeekBarChangeListener(this);
        textViewYlean = (TextView) findViewById(R.id.tv_y_lean_degree);

        seekBarXzoom = (SeekBar) findViewById(R.id.sb_x_zoom);
        seekBarXzoom.setOnSeekBarChangeListener(this);
        textViewXzoom = (TextView) findViewById(R.id.tv_x_zoom_degree);
        seekBarYzoom = (SeekBar) findViewById(R.id.sb_y_zoom);
        seekBarYzoom.setOnSeekBarChangeListener(this);
        textViewYzoom = (TextView) findViewById(R.id.tv_y_zoom_degree);
        seekBarZzoom = (SeekBar) findViewById(R.id.sb_z_zoom);
        seekBarZzoom.setOnSeekBarChangeListener(this);
        textViewZzoom = (TextView) findViewById(R.id.tv_z_zoom_degree);

        imageViewResult = (ImageView) findViewById(R.id.iv_result);

        refreshImage();
    }

    private void refreshImage()
    {
        //get image data
        BitmapDrawable bitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.sunshine);
        Bitmap bitmap = bitmapDrawable.getBitmap();
        //start to handle picture
        //step 1:get handle matrix, save and restore will make the picture more soft
        camera.save();
        Matrix matrix = new Matrix();
        camera.rotateX(rotateX);
        camera.rotateY(rotateY);
        camera.rotateZ(rotateZ);
        camera.translate(zoomX, zoomY, zoomZ);
        camera.getMatrix(matrix);
        camera.restore();
        matrix.preTranslate(bitmap.getWidth() >> 1, bitmap.getHeight() >> 1);
        matrix.preSkew(skewX, skewY);
        //step 2:generate new picture
        Bitmap newbit = null;
        try {
            newbit = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }
        catch (IllegalArgumentException e)
        {
            e.printStackTrace();
        }

        if (newbit != null)
        {
            imageViewResult.setImageBitmap(newbit);
        }

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
    {
        if (seekBar == seekBarXrotate){
            rotateX = progress;
            textViewXrotate.setText(String.valueOf(rotateX));
        }
        else if (seekBar == seekBarYrotate) {
            rotateY = progress;
            textViewYrotate.setText(String.valueOf(rotateY));
        }
        else if (seekBar == seekBarZrotate) {
            rotateZ = progress;
            textViewZrotate.setText(String.valueOf(rotateZ));
        }
        else if (seekBar == seekBarXskew) {
            skewX = (progress - 100) * 1.0f / 100;
            textViewXlean.setText(String.valueOf(skewX));
        }
        else if (seekBar == seekBarYskew) {
            skewY = (progress - 100) * 1.0f / 100;
            textViewYlean.setText(String.valueOf(skewY));
        }
        else if (seekBar == seekBarXzoom) {
            zoomX = progress - 100;
            textViewXzoom.setText(String.valueOf(zoomX));
        }
        else if (seekBar == seekBarYzoom) {
            zoomY = progress - 100;
            textViewYzoom.setText(String.valueOf(zoomY));
        }
        else if (seekBar == seekBarZzoom) {
            zoomZ = progress - 100;
            textViewZzoom.setText(String.valueOf(zoomZ));
        }

        refreshImage();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar){

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar){

    }

}
