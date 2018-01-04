package com.xiaoyi.camera.sdk;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Huan on 2016/4/19.
 * 云台预设点
 */
public class PTZPreset {

    public PTZPreset(int preset, String name) {
        this.preset = preset;
        this.name = name;
    }

    public PTZPreset(int preset) {
        this(preset, "云台预设点" + preset);
    }

    private int preset; //位置编号
    private String name;    //位置名称

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPreset() {
        return preset;
    }

    public void setPreset(int preset) {
        this.preset = preset;
    }

    public static List<PTZPreset> parsePresets(Integer[] ptzPresets) {
        List<PTZPreset> presets = new ArrayList<PTZPreset>();
        for (int i = 0; i < ptzPresets.length; i++) {
            if (ptzPresets[i] != 0) {
                presets.add(new PTZPreset(ptzPresets[i]));
            }
        }

        return presets;
    }

    @Override
    public String toString() {
        return name;
    }
}
