package com.xiaoyi.camera.sdk;

import java.util.HashMap;

public interface PasswordInvalidProcesser {

    void onPasswordInvalid(AntsCamera anstCamera);

    void onUmengEvent(String category, HashMap<String, String> map);

    void onUmengTimeEvent(String category, int value, HashMap<String, String> map);

    void onXiaoyiEvent(String category, HashMap<String, String> map);


}
