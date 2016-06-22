package com.example.smy.basicglsv;

import android.content.Context;
import android.opengl.GLSurfaceView;

/**
 * Created by SMY on 2016/6/16.
 */
public class BasicGLSurfaceView extends GLSurfaceView {
    public BasicGLSurfaceView(Context context) {
        super(context);
        setEGLContextClientVersion(2);
        setRenderer(new GLES20TriangleRenderer(context));
    }
}
