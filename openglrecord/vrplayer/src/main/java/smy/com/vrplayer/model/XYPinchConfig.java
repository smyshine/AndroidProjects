package smy.com.vrplayer.model;

/**
 * Created by hzqiujiadi on 16/9/29.
 * hzqiujiadi ashqalcn@gmail.com
 */

public class XYPinchConfig {
    private float max = 60;
    private float min = 1;
    private float defaultValue = 1;
    private float mSensitivity = 3;

    public XYPinchConfig setMax(float max) {
        this.max = max;
        return this;
    }

    public XYPinchConfig setMin(float min) {
        this.min = min;
        return this;
    }

    public XYPinchConfig setDefaultValue(float defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public XYPinchConfig setSensitivity(float mSensitivity) {
        this.mSensitivity = mSensitivity;
        return this;
    }

    public float getSensitivity() {
        return mSensitivity;
    }

    public float getMax() {
        return max;
    }

    public float getMin() {
        return min;
    }

    public float getDefaultValue() {
        return defaultValue;
    }
}
