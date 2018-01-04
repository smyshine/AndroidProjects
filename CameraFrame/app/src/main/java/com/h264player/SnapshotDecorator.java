package com.h264player;

import android.graphics.Bitmap;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by Nat on 2017/1/29.
 */

public class SnapshotDecorator extends PlayerDecorator {

    public SnapshotDecorator(IPlayer iPlayer) {
        super(iPlayer);
    }

    public void snapshot(String pathname){

        try {
            FileOutputStream out = new FileOutputStream(new File("/sdcard", "haha3.jpg"));
            super.getTextureView().getBitmap().compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
