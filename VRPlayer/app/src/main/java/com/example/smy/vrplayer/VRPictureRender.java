package com.example.smy.vrplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;

import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;

/**
 * Created by SMY on 2016/7/12.
 */
public class VRPictureRender extends BaseVRRender{

    private Context mContext;
    private Bitmap mPicture;

    public VRPictureRender(Context context){
        super(context.getApplicationContext());
        mContext = context;
    }
    @Override
    public void initScene() {
        Material material = new Material();
        if(mPicture != null) {
            try {
                material.addTexture(new Texture("photo", mPicture));
            } catch (ATexture.TextureException e) {
                e.printStackTrace();
            }
        }
        material.setColor(0);
        //material.setColor(Color.TRANSPARENT);
        initScene(material);
    }

    public void initBitmap(Bitmap bitmap){
        mPicture = bitmap;
        if(mSphere != null){
            if(mSphere.getMaterial() == null){
                initScene();
                return;
            } else {
                try {
                    mSphere.getMaterial().addTexture(new Texture("photo", mPicture));
                } catch (ATexture.TextureException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onRenderSurfaceDestroyed(final SurfaceTexture surface) {
        onDestroy();
    }

    @Override
    public void onRendererShutdown() {
        onDestroy();
    }

    public void onDestroy(){
        super.onRenderSurfaceDestroyed(null);
    }
}
