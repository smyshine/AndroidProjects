package com.xiaoyi.camera.sdk;

public enum PanDirection {
    PTZ_DIRECTION_UP(1),//云台上仰
    PTZ_DIRECTION_DOWN(2),    //云台下俯
    PTZ_DIRECTION_LEFT(3),    //云台左转
    PTZ_DIRECTION_RIGHT(4),    //云台右转
    PTZ_DIRECTION_LEFT_UP(5),    //云台左转和上仰
    PTZ_DIRECTION_LEFT_DOWN(6),    //云台左转和下俯
    PTZ_DIRECTION_RIGHT_UP(7),    //云台右转和上仰
    PTZ_DIRECTION_RIGHT_DOWN(8);    //云台右转和下俯

    // 定义私有变量
    private int nCode;


    private static PanDirection[] allValues = values();
    public static PanDirection fromOrdinal(int n) {return allValues[n-1];}

    // 构造函数，枚举类型只能为私有
    private PanDirection(int _nCode) {
        this.nCode = _nCode;
    }


    @Override
    public String toString() {
        return String.valueOf(this.nCode);
    }

    public int getDirectionCode(){
        return this.nCode;
    }


}


