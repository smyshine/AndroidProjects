package com.example.smy.myopenglestutorials.switchinglivewallpaper;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.example.smy.myopenglestutorials.R;

public class WallpaperSettings extends PreferenceActivity {
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}	
}
