package com.example.smy.photoloading;

import android.content.Context;

import com.bumptech.glide.load.model.GenericLoaderFactory;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;

import java.io.InputStream;

/**
 * Created by SMY on 2016/9/2.
 */
public class MyFactory implements ModelLoaderFactory<GlideCameraUrl, InputStream> {

    @Override
    public ModelLoader<GlideCameraUrl, InputStream> build(Context context, GenericLoaderFactory factories) {
        return null;//new OkHttpUrlLoader(CameraHttpClient.getInstance().client);
    }

    @Override
    public void teardown() {

    }
}
