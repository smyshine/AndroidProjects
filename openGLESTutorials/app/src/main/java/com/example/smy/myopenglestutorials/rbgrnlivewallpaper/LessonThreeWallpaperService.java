package com.example.smy.myopenglestutorials.rbgrnlivewallpaper;

import android.opengl.GLSurfaceView.Renderer;

import com.example.smy.myopenglestutorials.LessonThreeRenderer;

public class LessonThreeWallpaperService extends OpenGLES2WallpaperService {

	@Override
	Renderer getNewRenderer() {
		return new LessonThreeRenderer();
	}
}
