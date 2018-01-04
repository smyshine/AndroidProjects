package com.xiaoyi.camera.util;

import java.util.Calendar;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.tutk.IOTC.AVFrame;
import com.xiaoyi.log.AntsLog;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AntsUtil {

    public static String byteArrayToString(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            stringBuilder.append((char) bytes[i]);
        }
        return stringBuilder.toString();
    }

    public static boolean checkSupportAudio(String version) {
        if (version != null && version.startsWith("2.5")) { return true; }
        return false;
    }

    public static byte[] int2bytes(int num) {
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            b[3 - i] = (byte) (num >>> (24 - i * 8));
        }
        return b;
    }

    public byte[] int2bytes2(int channel) {
        byte[] b = new byte[32];
        for (int i = 0; i < 32; i++) {
            b[i] = (byte) (channel >>> (24 - i * 8));
        }
        return b;
    }

    static public int bytes2int(byte[] b) {
        // 0xff�Ķ����Ʊ�ʾΪ: 1111 1111
        int mask = 0xff;
        int temp = 0;
        int res = 0;
        for (int i = 0; i < 4; i++) {
            res <<= 8;// ����8λ���ʲô��˼?
            temp = b[3 - i] & mask;
            res |= temp;
        }
        return res;
    }

    static public short bytes2short(byte[] b) {
        short mask = 0xff;
        short temp = 0;
        short res = 0;
        for (int i = 0; i < 2; i++) {
            res <<= 8;
            temp = (short) (b[1 - i] & mask);
            res |= temp;
        }
        return res;
    }

    static public byte[] short2bytes(short data) {
        byte bytes[] = new byte[2];

        bytes[1] = (byte) ((data & 0xff00) >> 8);
        bytes[0] = (byte) (data & 0x00ff);
        return bytes;
    }

    public static String getVedioFileNameWithTime() {
        Calendar localCalendar = Calendar.getInstance();
        int i = localCalendar.get(1);
        int j = 1 + localCalendar.get(2);
        int k = localCalendar.get(5);
        int m = localCalendar.get(11);
        int n = localCalendar.get(12);
        int i1 = localCalendar.get(13);
        localCalendar.get(14);
        StringBuffer localStringBuffer = new StringBuffer();
        localStringBuffer.append("VEDIO_");
        localStringBuffer.append(i);
        if (j < 10) localStringBuffer.append('0');
        localStringBuffer.append(j);
        if (k < 10) localStringBuffer.append('0');
        localStringBuffer.append(k);
        localStringBuffer.append('_');
        if (m < 10) localStringBuffer.append('0');
        localStringBuffer.append(m);
        if (n < 10) localStringBuffer.append('0');
        localStringBuffer.append(n);
        if (i1 < 10) localStringBuffer.append('0');
        localStringBuffer.append(i1);
        localStringBuffer.append(".mp4");
        return localStringBuffer.toString();
    }

    public static boolean hasBind(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String flag = sp.getString("bind_flag", "");
        if ("ok".equalsIgnoreCase(flag)) { return true; }
        return false;
    }

    public static void calc1(short[] lin, int off, int len) {
        int i, j;
        for (i = 0; i < len; i++) {
            j = lin[i + off];
            lin[i + off] = (short) (j >> 2);
        }
    }

    private static final int cacheSize = 640;

    public static byte[] yuv420pToyuv420sp(byte[] yuvbuff, int width, int height) {
        byte[] uvbuf = new byte[width * height / 2];
        int ylength = width * height;
        int ulength = width * height / 4;
        int uindex = 0;
        int vindex = 0;
        for (int i = 0; i < width * height / 2; i++) {
            if (i % 2 == 0) {
                uvbuf[i] = yuvbuff[ylength + ulength + vindex];
                vindex++;
            } else {
                uvbuf[i] = yuvbuff[ylength + uindex];
                uindex++;
            }

        }
        System.arraycopy(uvbuf, 0, yuvbuff, ylength, uvbuf.length);
        return yuvbuff;
    }

    private static char[] NonceBase = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
            'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c',
            'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
            'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };

    /**
     * 生成15位随机数
     * 
     * @param length
     * @return
     */
    public static String genNonce(int length) {
        char[] nonce = new char[length];
        for (int i = 0; i < length; i++) {
            int random = Math.abs(new Random().nextInt());
            int j = random % NonceBase.length;
            nonce[i] = NonceBase[j];
        }
        String result = new String(nonce);
        return result;
    }

    public static String hmacSha1(String key, String datas) {
        String reString = "";

        try {
            byte[] data = key.getBytes("UTF-8");
            SecretKey secretKey = new SecretKeySpec(data, "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(secretKey);

            byte[] text = datas.getBytes("UTF-8");
            byte[] text1 = mac.doFinal(text);

            reString = new String(Base64.encode(text1));

        } catch (Exception e) {
        }

        return reString;
    }

    public static String getPassword(String nonce, String secret) {
        String user = "user=xiaoyiuser&nonce=" + nonce;
        String hmac = hmacSha1(secret, user);
        if (hmac.length() > 15) {
            hmac = hmac.substring(0, 15);
        }
        return hmac;
    }

    public static void decryptIframe(AVFrame avFrame, String key) {
        AntsLog.d("decrypt", "key=" + key);
        if (avFrame.frmData.length < 36) { return; }
        byte[] content = new byte[16];
        System.arraycopy(avFrame.frmData, 4, content, 0, 16);
        byte[] result = AESIPC.decrypt(content, key);
        System.arraycopy(result, 0, avFrame.frmData, 4, 16);

        System.arraycopy(avFrame.frmData, 20, content, 0, 16);
        byte[] result2 = AESIPC.decrypt(content, key);
        System.arraycopy(result2, 0, avFrame.frmData, 20, 16);
    }

    public static String getHex(byte[] bytes, int length) {
        if (bytes == null) return null;
        StringBuilder stringBuilder = new StringBuilder(2 * bytes.length);
        for (int k = 0; k < length; k++) {
            int m = bytes[k];
            stringBuilder.append("0123456789ABCDEF".charAt((m & 0xF0) >> 4))
                    .append("0123456789ABCDEF".charAt(m & 0xF)).append(" ");
        }
        return stringBuilder.toString();
    }

}
