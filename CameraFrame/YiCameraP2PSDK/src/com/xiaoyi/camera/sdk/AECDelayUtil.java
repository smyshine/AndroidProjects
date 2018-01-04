package com.xiaoyi.camera.sdk;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.freq.util.Freq;
import com.xiaoyi.log.AntsLog;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Created by shi.lei on 2016/5/17.
 */
public class AECDelayUtil {

    public static final String TAG = "AECDelayUtil";
    public static final int FREQUENCY_CAP_PLAY = 16000;
    public static final int DATA_LENGTH_1000MS = FREQUENCY_CAP_PLAY*2;
    public static final int DATA_LENGTH_100MS = DATA_LENGTH_1000MS/10;
    public static final int DATA_LENGTH_10MS =  DATA_LENGTH_1000MS/100;
    public static final String PCM_SAMPLE_FILE = "assets/5k_16k.pcm";

    public static final int mDelayPlay = 100; // unit 100 ms
    public static int SPECIAL_FREQUENCY =5000; // 21016; //19982;
    private long startWriteTime;
    private long getVolumeTime;
    private long delayTime;
    private AudioTrack mAudioTrack;
    private AudioRecord mAudioRecord;
    private ReadAudioThread mReadAudioThread;
    private RecordAudioThread mRecordAudioThread;
    private DataListener dataListener;
    private boolean isRecordAudio;
    private AudioManager audioManager;
    private int volume;
    private int maxVolume;

    private static final String DELAY_TIME = "delaytime";
    private static final String VOLUME = "volume";

    private static final int DELAYTIME = 0;

    public AECDelayUtil(Context context) {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        volume =  audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case DELAYTIME:
                    Bundle bundle = msg.getData();
                    getAverage(bundle.getLong(DELAY_TIME), bundle.getDouble(VOLUME));
                    break;
            }
        }
    };

    public void releaseReadAudioThread() {
        if(mAudioTrack!=null){
            mAudioTrack.release();
            mAudioTrack=null;
        }
    }

    public void releaseRecordAudioThread() {
        stopRecordThread();
        relaseAudioRecord();
    }

    public void resetVolume(){
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_PLAY_SOUND);
    }

    private void relaseAudioRecord(){
        if(mAudioRecord!=null&&mAudioRecord.getRecordingState()!=AudioRecord.RECORDSTATE_STOPPED){
            mAudioRecord.stop();
        }
        if(mAudioRecord!=null){
            mAudioRecord.release();
            mAudioRecord=null;
        }
    }

    private void stopRecordThread(){
        if(mRecordAudioThread != null){
            mRecordAudioThread.stopThread();
        }
    }

    public void startReadAndRecordThread(){

        AntsLog.i(TAG, "startReadAndRecordThread ...");
        releaseReadAudioThread();
        releaseRecordAudioThread();
        mReadAudioThread = new ReadAudioThread();
        mRecordAudioThread = new RecordAudioThread();
        Freq.nativeInitFft(FREQUENCY_CAP_PLAY, SPECIAL_FREQUENCY);
        mReadAudioThread.start();
        mRecordAudioThread.start();
    }


    private class ReadAudioThread extends Thread{

        private static final String TAGRAD = "ReadAudioThread";

        private byte[] decodeData = new byte[DATA_LENGTH_1000MS];
        private byte[] emptyData = new byte[DATA_LENGTH_1000MS+100];

        private int audioNum = FREQUENCY_CAP_PLAY;
        private InputStream inputStream;

        @Override
        public void run() {
            super.run();
            int bufferSize = AudioTrack.getMinBufferSize(audioNum,
                    AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
            mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, audioNum,
                    AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize, AudioTrack.MODE_STREAM);
            AntsLog.d(TAG, "audio buffer size:"+bufferSize+", audioTrack:"+mAudioTrack);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, AudioManager.FLAG_PLAY_SOUND);
            mAudioTrack.play();
            try {
                inputStream = AECDelayUtil.class.getClassLoader().getResourceAsStream(PCM_SAMPLE_FILE);
                int length = inputStream.read(decodeData, 0 ,decodeData.length);
                if (length < 0){
                    AntsLog.d("audioRead", "get inPutStream failed");
                    return;
                }
                Arrays.fill(emptyData, 0, emptyData.length, (byte)0);

                startWriteTime = System.currentTimeMillis();
                AntsLog.d(TAG,"playTIME==="+startWriteTime+", len:"+length);
                try {
/*                    for(int i=0; i <= DATA_LENGTH_100MS*mDelayPlay/100-bufferSize;i=i+bufferSize){
                        mAudioTrack.write(emptyData, i, bufferSize);
 //                       mAudioTrack.flush();
                    }
                    for(int i = 0;i <= DATA_LENGTH_100MS - bufferSize; i=i+bufferSize){
                        mAudioTrack.write(decodeData, i, bufferSize);
//                        mAudioTrack.flush();
                    }*/
                    mAudioTrack.write(emptyData, 0, DATA_LENGTH_100MS*mDelayPlay/100);
                    mAudioTrack.write(decodeData, 0, DATA_LENGTH_100MS*3);
                    mAudioTrack.flush();
                    //length = inputStream.read(decodeData, 0 ,decodeData.length);
                }
                catch (Exception e) {
                    AntsLog.d("audioPlay","====writeFailed");
                }
                try {
                    inputStream.close();
                } catch (IOException e) {
                    AntsLog.d(TAG, "inputStream close failed");
                    e.printStackTrace();
                }
            } catch (Exception e) {
                AntsLog.d(TAG, "read failed");
                e.printStackTrace();
            }
            releaseReadAudioThread();
        }

    }

    void writeShort2file(int idx, String fname, short[] buff) {
        try{
            RandomAccessFile randomFile = new RandomAccessFile(fname, "rw");
            long fileLength = randomFile.length();
            randomFile.seek(fileLength);

            for (int i=0; i<buff.length;i++){
                randomFile.writeShort(buff[i]);
            }
            randomFile.close();
        }catch (Exception ex){
            AntsLog.i(TAG, "write file exception :"+ex.getMessage() );
        }
        if(idx %100 == 1)AntsLog.i(TAG, "write file done!"+fname +", ctn:"+idx);
    }

    void writeChar2file(int idx, String fname, byte[] buff) {

        try{
            RandomAccessFile randomFile = new RandomAccessFile(fname, "rw");
            long fileLength = randomFile.length();
            randomFile.seek(fileLength);
            randomFile.write(buff, 0, buff.length);
            randomFile.close();
        }catch (Exception ex){
            AntsLog.i(TAG, "write file exception :"+ex.getMessage() );
        }
        if(idx %1000 == 1)AntsLog.i(TAG, "write char file done!"+fname +", ctn:"+idx+", len:"+buff.length);
    }

    private class RecordAudioThread extends Thread{

        private static final String TAGRED = "RecordAudioThread";

        private int sampleRate = FREQUENCY_CAP_PLAY;
        int bufferSize = 320;
        byte[] tempBuffer = new byte[bufferSize];
        short[] tempBufferShortOneInstance = new short[bufferSize / 2];

        @Override
        public void run() {
            super.run();
            int min = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            if(min < bufferSize){
                min = bufferSize;
            }
            mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, min);
            if (mAudioRecord == null || mAudioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                AntsLog.d("audioRecord", "audioRecord open failed");
                return;
            }
            mAudioRecord.startRecording();
            isRecordAudio = true;
            while (isRecordAudio) {
                int bufferRead = mAudioRecord.read(tempBuffer, 0, bufferSize);
                //    不能处理非空数据 如果没有权限传过来的是空值  所以要判断一下
                if(bufferRead < 0){
                    AntsLog.d(TAG, "bufferRead length < 0");
                    break;
                }
                getVolumeTime = System.currentTimeMillis();
//                AmplifyPCMData(tempBuffer, tempBuffer.length, (float) 0.3);
                ByteBuffer.wrap(tempBuffer).order(ByteOrder.LITTLE_ENDIAN)
                        .asShortBuffer().get(tempBufferShortOneInstance);
                int freq = Freq.nativeGetFreq(tempBufferShortOneInstance, bufferRead/2);
                double volume = getVolume(tempBufferShortOneInstance, tempBufferShortOneInstance.length);
                if(freq > SPECIAL_FREQUENCY-100 && freq < SPECIAL_FREQUENCY+100)
                {
                    AntsLog.d(TAG, "recordTime==="+getVolumeTime);
                    delayTime = getVolumeTime-startWriteTime-mDelayPlay;
                    AntsLog.d(TAG, "delayTime==="+delayTime+", freq:"+freq);
                    if(delayTime>100 ){
                        Message message = handler.obtainMessage();
                        message.what=DELAYTIME;
//                        message.obj=delayTime;
                        Bundle bundle = new Bundle();
                        bundle.putLong(DELAY_TIME, delayTime);
                        bundle.putDouble(VOLUME, volume);
                        message.setData(bundle);
                        handler.sendMessage(message);
//                        getMin((int)delayTime);
                    }
                    break;
                }
            }
            relaseAudioRecord();
            releaseReadAudioThread();
        }

        public void stopThread (){
            isRecordAudio = false;
        }

        private double getVolume(short[] buffer, int read){
            long v = 0;
            // 将 buffer 内容取出，进行平方和运算
            for (int i = 0; i < read; i++) {
                v += buffer[i] * buffer[i];
            }
            // 平方和除以数据总长度，得到音量大小。
            double mean = v / (double)read ;
            double volume = 10 * Math.log10(mean);
            AntsLog.d(TAG, "分贝值:" + volume);
            return  volume;
        }

    }

    private long sum=0;
    private double sumVolume= 0;

    private long min1 = Integer.MAX_VALUE;
    private double min2 = Integer.MAX_VALUE;
    private long max1 = 0;
    private double max2 = 0;

    private long [] delayTimes = new long[200];
    private double [] volumes = new double[200];

    private long average = 0;
    private double averageVolume = 0;

    private int num = 0;

    private void getAverage(long delayTime, double volume){

        if(delayTime < min1){
            min1 = delayTime;
        }
        if(volume < min2){
            min2 = volume;
        }
        if(delayTime > max1){
            max1 = delayTime;
        }
        if(volume > max2){
            max2 = volume;
        }
        delayTimes[num] = delayTime;
        volumes[num] = volume;
        sum=sum+delayTime;
        sumVolume = sumVolume+volume;
        num++;
        if (num > 2){
            average = (sum - min1 - max1)/(num - 2);
            averageVolume = (sumVolume - min2 - max2)/(num - 2);
        }else {
            average = sum/num;
            averageVolume = sumVolume/num;
        }
        dataListener.getData(average, num, delayTimes, volumes, averageVolume);
        for(int i=0; i<num; i++){
            AntsLog.d(TAG, "delayTimes["+i+"]:"+delayTimes[i]);
        }
        AntsLog.d(TAG, "delayTimes["+num+"]:"+"ends");
        AntsLog.d(TAG, "delayTimesAverage:"+average+",sum:"+sum+",num:"+num+",max:"+max1+",min:"+min1);
        AntsLog.d(TAG, "delayTimesAverage:"+"ThisTimeAllEnds");
    }


//    获取最小delayTime

    private int min = 250;
    private void getMin(int delayTime){
        if(num != 0 && min > delayTime){
            min = delayTime;
        }
        delayTimes[num] = delayTime;
        num++;
        if(num > 4){
            SharedPreferencesUtil.getInstance().putDelayTime(min);
            SharedPreferencesUtil.getInstance().putIsNeedTest(false);
            AntsLog.d(TAG, "num="+num+"==="+"IsNeedTest="+false);
        }
    }

    public void setDataListener(DataListener dataListener) {
        this.dataListener = dataListener;
    }

    public interface DataListener{
        public void getData(long average, int num, long [] delayTimes, double [] volume, double averageVolume);
    }

}

