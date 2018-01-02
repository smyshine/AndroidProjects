package com.video.draw;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

import android.opengl.GLSurfaceView;

public class EGLWindowSurfaceFactory implements GLSurfaceView.EGLWindowSurfaceFactory {
	private static int EGL_BACK_BUFFER = 0x3084;
	@Override
	public EGLSurface createWindowSurface(EGL10 egl, EGLDisplay display,
			EGLConfig config, Object nativeWindow) {
		// this is a bit of a hack to work around Droid init problems - if you
		// don't have this, it'll get hung up on orientation changes
		EGLSurface eglSurface = null;
		int attribList[] ={EGL10.EGL_RENDER_BUFFER, EGL_BACK_BUFFER,EGL10.EGL_NONE};
		while (eglSurface == null) {
			try {
				eglSurface = egl.eglCreateWindowSurface(display, config,
						nativeWindow, attribList);
			} catch (Throwable t) {
			} finally {
				if (eglSurface == null) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException t) {
					}
				}
			}
		}
		return eglSurface;
	}

	@Override
	public void destroySurface(EGL10 egl, EGLDisplay display, EGLSurface surface) {
		egl.eglDestroySurface(display, surface);
	}

}
