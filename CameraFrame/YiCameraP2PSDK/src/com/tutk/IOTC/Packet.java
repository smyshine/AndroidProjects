package com.tutk.IOTC;

public class Packet {

    public static final int byteArrayToInt(byte[] paramArrayOfByte, int paramInt, boolean isByteOrderBig){
        if(isByteOrderBig) {
            return byteArrayToInt_Big(paramArrayOfByte, paramInt);
        } else {
            return byteArrayToInt_Little(paramArrayOfByte, paramInt);
        }
    }

    public static final long byteArrayToLong(byte[] paramArrayOfByte, int paramInt, boolean isByteOrderBig) {
        if(isByteOrderBig) {
            return byteArrayToLong_Big(paramArrayOfByte, paramInt);
        } else {
            return byteArrayToLong_Little(paramArrayOfByte, paramInt);
        }
    }

    public static final short byteArrayToShort(byte[] paramArrayOfByte, int paramInt, boolean isByteOrderBig) {
        if(isByteOrderBig) {
            return byteArrayToShort_Big(paramArrayOfByte, paramInt);
        } else {
            return byteArrayToShort_Little(paramArrayOfByte, paramInt);
        }
    }

    public static final byte[] intToByteArray(int paramInt, boolean isByteOrderBig) {
        if(isByteOrderBig) {
            return intToByteArray_Big(paramInt);
        } else {
            return intToByteArray_Little(paramInt);
        }
    }

    public static final byte[] longToByteArray(long paramLong, boolean isByteOrderBig){
        if(isByteOrderBig) {
            return longToByteArray_Big(paramLong);
        } else {
            return longToByteArray_Little(paramLong);
        }
    }

    public static final byte[] shortToByteArray(short paramShort, boolean isByteOrderBig) {
        if(isByteOrderBig) {
            return shortToByteArray_Big(paramShort);
        } else {
            return shortToByteArray_Little(paramShort);
        }
    }

    public static final String byteArrayToString(byte[] paramArrayOfByte, int length) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            stringBuilder.append((char) paramArrayOfByte[i]);
        }
        return stringBuilder.toString();
    }


    public static final String printByteArray(byte[] paramArrayOfByte, int length) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            stringBuilder.append(" "+(int)(paramArrayOfByte[i]));
        }
        return stringBuilder.toString();
    }


    private static final int byteArrayToInt_Big(byte[] paramArrayOfByte, int paramInt){
        return ((0xFF & paramArrayOfByte[paramInt]) << 24)
                | (0xFF & paramArrayOfByte[(paramInt + 1)]) << 16
                | (0xFF & paramArrayOfByte[(paramInt + 2)]) << 8
                | (0xFF & paramArrayOfByte[(paramInt + 3)]);
    }
    private static final int byteArrayToInt_Little(byte[] paramArrayOfByte, int paramInt) {
        return (0xFF & paramArrayOfByte[paramInt])
                | (0xFF & paramArrayOfByte[(paramInt + 1)]) << 8
                | (0xFF & paramArrayOfByte[(paramInt + 2)]) << 16
                | (0xFF & paramArrayOfByte[(paramInt + 3)]) << 24;
    }

    private static final long byteArrayToLong_Big(byte[] paramArrayOfByte, int paramInt) {
        return ((0xFF & paramArrayOfByte[paramInt]) << 56)
                | (0xFF & paramArrayOfByte[(paramInt + 1)]) << 48
                | (0xFF & paramArrayOfByte[(paramInt + 2)]) << 40
                | (0xFF & paramArrayOfByte[(paramInt + 3)]) << 32
                | (0xFF & paramArrayOfByte[(paramInt + 4)]) << 24
                | (0xFF & paramArrayOfByte[(paramInt + 5)]) << 16
                | (0xFF & paramArrayOfByte[(paramInt + 6)]) << 8
                | (0xFF & paramArrayOfByte[(paramInt + 7)]);
    }
    private static final long byteArrayToLong_Little(byte[] paramArrayOfByte, int paramInt) {
        return (0xFF & paramArrayOfByte[paramInt])
                | (0xFF & paramArrayOfByte[(paramInt + 1)]) << 8
                | (0xFF & paramArrayOfByte[(paramInt + 2)]) << 16
                | (0xFF & paramArrayOfByte[(paramInt + 3)]) << 24
                | (0xFF & paramArrayOfByte[(paramInt + 4)]) << 32
                | (0xFF & paramArrayOfByte[(paramInt + 5)]) << 40
                | (0xFF & paramArrayOfByte[(paramInt + 6)]) << 48
                | (0xFF & paramArrayOfByte[(paramInt + 7)]) << 56;
    }

    private static final short byteArrayToShort_Big(byte[] paramArrayOfByte, int paramInt) {
        return (short) (((0xFF & paramArrayOfByte[paramInt]) << 8)
                | (0xFF & paramArrayOfByte[(paramInt + 1)]));
    }
    private static final short byteArrayToShort_Little(byte[] paramArrayOfByte, int paramInt) {
        return (short) ((0xFF & paramArrayOfByte[paramInt])
                | (0xFF & paramArrayOfByte[(paramInt + 1)]) << 8);
    }

    private static final byte[] intToByteArray_Big(int paramInt) {
        byte[] arrayOfByte = new byte[4];
        arrayOfByte[0] = ((byte) (paramInt >>> 24));
        arrayOfByte[1] = ((byte) (paramInt >>> 16));
        arrayOfByte[2] = ((byte) (paramInt >>> 8));
        arrayOfByte[3] = ((byte) paramInt);
        return arrayOfByte;
    }
    private static final byte[] intToByteArray_Little(int paramInt) {
        byte[] arrayOfByte = new byte[4];
        arrayOfByte[0] = ((byte) paramInt);
        arrayOfByte[1] = ((byte) (paramInt >>> 8));
        arrayOfByte[2] = ((byte) (paramInt >>> 16));
        arrayOfByte[3] = ((byte) (paramInt >>> 24));
        return arrayOfByte;
    }

    private static final byte[] longToByteArray_Big(long paramLong){
        byte[] arrayOfByte = new byte[8];
        arrayOfByte[0] = ((byte) (int) (paramLong >>> 56));
        arrayOfByte[1] = ((byte) (int) (paramLong >>> 48));
        arrayOfByte[2] = ((byte) (int) (paramLong >>> 40));
        arrayOfByte[3] = ((byte) (int) (paramLong >>> 32));
        arrayOfByte[4] = ((byte) (int) (paramLong >>> 24));
        arrayOfByte[5] = ((byte) (int) (paramLong >>> 16));
        arrayOfByte[6] = ((byte) (int) (paramLong >>> 8));
        arrayOfByte[7] = ((byte) (int) paramLong);
        return arrayOfByte;
    }
    private static final byte[] longToByteArray_Little(long paramLong) {
        byte[] arrayOfByte = new byte[8];
        arrayOfByte[0] = ((byte) (int) paramLong);
        arrayOfByte[1] = ((byte) (int) (paramLong >>> 8));
        arrayOfByte[2] = ((byte) (int) (paramLong >>> 16));
        arrayOfByte[3] = ((byte) (int) (paramLong >>> 24));
        arrayOfByte[4] = ((byte) (int) (paramLong >>> 32));
        arrayOfByte[5] = ((byte) (int) (paramLong >>> 40));
        arrayOfByte[6] = ((byte) (int) (paramLong >>> 48));
        arrayOfByte[7] = ((byte) (int) (paramLong >>> 56));
        return arrayOfByte;
    }

    private static final byte[] shortToByteArray_Big(short paramShort) {
        byte[] arrayOfByte = new byte[2];
        arrayOfByte[0] = ((byte) (paramShort >>> 8));
        arrayOfByte[1] = ((byte) paramShort);
        return arrayOfByte;
    }
    private static final byte[] shortToByteArray_Little(short paramShort) {
        byte[] arrayOfByte = new byte[2];
        arrayOfByte[0] = ((byte) paramShort);
        arrayOfByte[1] = ((byte) (paramShort >>> 8));
        return arrayOfByte;
    }
}
