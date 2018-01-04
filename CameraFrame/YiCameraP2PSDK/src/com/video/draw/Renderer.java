package com.video.draw;

import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public final class Renderer implements GLSurfaceView.Renderer {

	public static final int TRANSLATE_X = 0;
	public static final int TRANSLATE_Y = 1;
	public static final int TRANSLATE_Z = 2;

//	static {
//		System.loadLibrary("videodraw");
//	}

	/**
	 * IMPORTANT: after each time created a new Renderer, it is a must to
	 * destroy it by calling this method to release related resources.
	 */
	public Renderer() {
		super();
	}

	/**
	 * interface to listen new frame events.
	 */
	public interface FrameListener {
		public void onNewFrame();

		public void onSurfaceCreate();

		public void onSurfaceChanged();
	}

	public void setListener(FrameListener listener) {
		mListener = listener;
	}

	public void resetListener() {
		mListener = null;
	}

	public void onDrawFrame(GL10 gl) {
		_draw();
	}

	public void onSurfaceChanged(GL10 gl, int width, int height) {
//		Log.d("render", "width=" + width);
//		Log.d("render", "height=" + height);
		_init(width, height);
		if (mListener != null) {
			mListener.onSurfaceChanged();
		}
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		_create();
		if (mListener != null) {
			mListener.onSurfaceCreate();
		}
	}

	/**
	 * IMPORTANT: after each time created a new Renderer, it is a must to
	 * destroy it by calling this method to release related resources.
	 */
	public void destroy() {
		mListener = null;
		//_destroy();
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		_destroy();
	}

	/**
	 * set whether it keeps aspect ratio or not.
	 * 
	 * @author xiezhigang
	 * @date 2013-11-08
	 */
	public native final void keepAspectRatio(boolean value);

	/**
	 * set display direction
	 * 
	 * @param direction
	 *            the direction to draw the frames. 0 - east 1 - north 2 - west
	 *            3 - south
	 */
	public native final void setDirection(int direction);

	public native final int getDirection();

	/**
	 * set flip mode
	 * 
	 * @param flip
	 *            the flip mode to draw the frames. 0 - none 1 - horizontal flip
	 *            2 - vertical flip 3 - flip frames on both directions.
	 */
	public native final void setFlip(int flip);

	public native final int getFlip();

	public native final int getAspectState();

	public native final void setAspectState(int state);

	public native final void setScale(float scale);

	public native final float getScale();

	public native final void setTranslate(float translateX, float translateY,
			float translateZ);

	public native final float getTranslate(int translateEnum);

	public native final void setRotate(float degree);

	public native final float getRotate();

	/**
	 * refresh the display.
	 */
	public native final void refresh();

	/**
	 * setFrame and display.
	 */
	public native final void setFrame(byte[] frameBuff, int frameWidth,
			int frameHeight);

	// member accessed by native methods.
	private int mNativeRenderer;

	// function accessed by native methods, to intriggle onNewFrame events.
	protected void fireOnNewFrame() {
		try {
			if (mListener != null) {
				mListener.onNewFrame();
			}
		} catch (Exception ex) {
			// Log.e(TAG, "fire OnNewFrame failed: " + ex.getMessage());
		}
	}

	private native final void _draw();

	private native final void _init(int width, int height);

	private native final void _create();

	private native final void _destroy();

	private FrameListener mListener; // holds one new frame events listener
}
