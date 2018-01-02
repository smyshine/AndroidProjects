package com.tnp.model;

import android.provider.Settings;
import android.text.TextUtils;

import com.tutk.IOTC.Packet;

/**
 * Created by Chuanlong on 2015/11/17.
 */
public class TNPIOCtrlHead {

    public static final int LEN_HEAD = 40;

    public short commandType;
    public short commandNumber; //指令SeqNumber
    public short exHeaderSize;  //扩展头长度,必须是4的整数倍
    public short dataSize;      //不含该头长度


    // union, send use authInfo, receive use authResult
    public byte[] authInfo = new byte[LEN_HEAD - 8];  //"username,password"
    public int authResult = -1; //0:成功;1:密码错误;2:nonce错误;3:版本号不支持


    public boolean isByteOrderBig;

    private TNPIOCtrlHead(boolean isByteOrderBig){
        this.isByteOrderBig = isByteOrderBig;
    }

    public TNPIOCtrlHead(short reqType, short number, short size, String username, String password, int auth, boolean isByteOrderBig){
        commandType = reqType;
        commandNumber = number;
        exHeaderSize = 0;
        dataSize = size;
        if(TextUtils.isEmpty(username) || TextUtils.isEmpty(password)){
            authResult = auth;
            this.isByteOrderBig = isByteOrderBig;
        }else{
            authResult = -1;
            String authInfoStr = username + "," + password;
            System.arraycopy(authInfoStr.getBytes(), 0, authInfo, 0, authInfoStr.length());
            this.isByteOrderBig = isByteOrderBig;
        }
    }

    public byte[] toByteArray(){
        byte[] byteArray = new byte[LEN_HEAD];
        System.arraycopy(Packet.shortToByteArray(commandType, isByteOrderBig), 0, byteArray, 0, 2);
        System.arraycopy(Packet.shortToByteArray(commandNumber, isByteOrderBig), 0, byteArray, 2, 2);
        System.arraycopy(Packet.shortToByteArray(exHeaderSize, isByteOrderBig), 0, byteArray, 4, 2);
        System.arraycopy(Packet.shortToByteArray(dataSize, isByteOrderBig), 0, byteArray, 6, 2);
        System.arraycopy(authInfo, 0, byteArray, 8, LEN_HEAD-8);
        return byteArray;
    }

    public static TNPIOCtrlHead parse(byte[] data, boolean isByteOrderBig){
        TNPIOCtrlHead tnpIOCtrlHead = new TNPIOCtrlHead(isByteOrderBig);
        tnpIOCtrlHead.commandType = Packet.byteArrayToShort(data, 0, isByteOrderBig);
        tnpIOCtrlHead.commandNumber = Packet.byteArrayToShort(data, 2, isByteOrderBig);
        tnpIOCtrlHead.exHeaderSize = Packet.byteArrayToShort(data, 4, isByteOrderBig);
        tnpIOCtrlHead.dataSize = Packet.byteArrayToShort(data, 6, isByteOrderBig);
        tnpIOCtrlHead.authResult = Packet.byteArrayToInt(data, 8, isByteOrderBig);
        return tnpIOCtrlHead;
    }

}
