package com.example.smy.photoloading;

import android.app.Activity;
import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.InputStream;

/**
 * Created by SMY on 2016/9/2.
 */
public class YiImageLoader {

    static {
        Glide.get(MyApplication.getInstance().getApplicationContext()).
                register(GlideCameraUrl.class, InputStream.class, new MyFactory());
    }

    public static void loadYiImage(Context context, String imageUri, ImageView imageView, int resId) {
        loadYiImage(context, imageUri, imageView, resId, null);
    }


    public static void loadYiImage(Context context, String imageUri, ImageView imageView, int resId, final LoadCallback callback) {
        if (context instanceof Activity && ((Activity) context).isFinishing()) {
            return;
        }
        DrawableRequestBuilder builder = Glide.with(context).load(imageUri).placeholder(resId)
                .error(resId).dontAnimate().listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        if (callback != null) {
                            callback.onError();
                        }
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        if (callback != null) {
                            callback.onSuccess();
                        }
                        return false;
                    }
                });

        builder.dontAnimate();

        builder.into(imageView);
    }

    public interface LoadCallback {

        void onSuccess();

        void onError();
    }
}
