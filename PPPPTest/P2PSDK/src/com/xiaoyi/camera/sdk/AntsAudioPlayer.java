package com.xiaoyi.camera.sdk;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import com.aac.utils.DecodeAAC;
import com.tutk.IOTC.AVFrame;
import com.xiaoyi.log.AntsLog;
import com.xiaoyi.p2pservertest.audio.ByteRingBuffer;
import com.xiaoyi.p2pservertest.audio.MobileAEC;
import com.xiaoyi.p2pservertest.audio.MobileAGC;
import com.xiaoyi.p2pservertest.audio.MobileNS;
import com.xiaoyi.p2pservertest.audio.MobileVAD;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedList;

public class AntsAudioPlayer {

    public static final String TAG = "AntsAudioPlayer";

    public static final int AUDIO_CODEID_AAC = 138;
    private static final short G711_CODEC = 140;

    private int audioNum = 16000;

    private AudioTrack mAudioTrack;

    private ThreadDecodeAudio mThreadDecodeAudio;

    private Object OBJECT = new Object();
    private int talkMode = AntsCamera.SINGLE_MODE;
    private int preTalkMode = -1;
    private boolean  talkModeChangeFlag = false;
    private MobileAEC mMobileAEC;
    private MobileAGC mMobileAGC;
    private MobileNS mMobileNS;
    private String mModuleGain;
    //    private G711Decoder mG711Decoder;
    private LinkedList<AVFrame> audioFrameQueue = new LinkedList<AVFrame>();
    private MobileVAD mobileVAD;
    private int mNeedDropAudioFrm = 0; //fengwu add
    private int mActualDropAudioFrm = 0; //fengwu add

    private int mIsAudioOptimize = 0; // 0 no, 1 yes

//    private boolean duplex;      //双通模式


    public AntsAudioPlayer(String mModuleGain) {
        this(mModuleGain, 0);
    }

    public AntsAudioPlayer(String mModuleGain, int mIsAudioOptimize){
        this.mModuleGain = mModuleGain;
        this.mIsAudioOptimize = mIsAudioOptimize;
        try {
            DecodeAAC.nOpen();
            this.mThreadDecodeAudio = new ThreadDecodeAudio();
            this.mThreadDecodeAudio.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


/*    public void setDuplex(boolean dup) {
        AntsLog.e("mAudioTrack", "setDuplex " + dup);
        duplex = dup;

    }*/

    public void setTalkMode(int mode){
        talkMode = mode;
        if(preTalkMode == talkMode){
            talkModeChangeFlag = false;
        } else {
            talkModeChangeFlag = true;
        }
        preTalkMode = talkMode;
    }


    public void release() {
        AntsLog.d(TAG, "release");
        if (mThreadDecodeAudio != null) {
            mThreadDecodeAudio.stopThread();
            mThreadDecodeAudio.interrupt();
        }
        releaseAudioTrack();
    }

    public void releaseAudioTrack() {
        AntsLog.d(TAG, "releaseAudioTrack");
        if (this.mAudioTrack != null) {
            this.mAudioTrack.release();
            this.mAudioTrack = null;
        }
    }

    public void startPlay() {
        try {
            releaseAudioTrack();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 加入需要解码的AVFrame
     *
     * @param avFrame
     */
    public void addAvFrame(AVFrame avFrame) {
        synchronized (OBJECT) {
            audioNum = AVFrame.getSamplerate(avFrame.getFlags())/2;
            audioFrameQueue.add(avFrame);
        }
        synchronized (mWaitObject) {
            mWaitObject.notify();
        }
    }

    private final Object mWaitObject = new Object();

    private class ThreadDecodeAudio extends Thread {

        private byte[] decodeData = new byte[10000];
        private byte[] decodeDataMoNo = new byte[5000];
        private short[] decodeDataTemp = new short[5000];
        private short[] decodeDataTempMoNo = new short[2500];
        private int decodeErrorCount = 0;
        private ByteRingBuffer RingBuffer = null;
        private short[] writeBuffer = new short[1280];
        private byte[] priWriteBuffer = new byte[2560];
        private byte[] priWriteBuffer_bak = new byte[2560]; //fengwu add 0927
        private boolean mIsBackupBuffer = false; //fengwu add 0927
        private short[] agcTmpOut = new short[160];
        private byte[] nsTmpByte = new byte[320];
        private short[] nsTmpOut = new short[160];
        private short[] nsTmpIn = new short[160];

        public void AmplifyPCMData(byte[] pData, int nLen, float multiple) {

            int nCur = 0;
            short[] temp = new short[nLen / 2];

            ByteBuffer.wrap(pData).order(ByteOrder.LITTLE_ENDIAN)
                    .asShortBuffer().get(temp);

            while (nCur < nLen / 2) {
                //    short* volum = (short*)(pData + nCur);
                short volume = temp[nCur];
                temp[nCur] = (short) (volume * multiple);

                if (temp[nCur] < -0x8000) {
                    temp[nCur] = -0x8000;
                }

                if (temp[nCur] > 0x7FFF)//爆音的处理
                {
                    temp[nCur] = 0x7FFF;
                }

                nCur++;
            }

            ByteBuffer.wrap(pData).order(ByteOrder.LITTLE_ENDIAN)
                    .asShortBuffer().put(temp);

        }

        private double getVolume(short[] buffer, int read) {
            long v = 0;
            // 将 buffer 内容取出，进行平方和运算
            for (int i = 0; i < read; i++) {
                v += buffer[i] * buffer[i];
            }

            if(v < 1) return 0;

            double volume = 10 * Math.log10(v);
            //AntsLog.d(TAG, "分贝值:" + volume);
            return  volume;
        }

        private int isVoice(byte[] bbuf, int len) {
            if(mIsAudioOptimize != 1) {
                // if not H20, return 1;
                return 1;
            }

            if(talkMode != AntsCamera.MIC_MODE || talkMode != AntsCamera.VOIP_MODE) {
                // if not duplex, return 1;
                return 1;
            }

            if(mNeedDropAudioFrm <= 0){
                return 1;
            }



            byte[] nsTmpByte2 = new byte[320];
            //short[] nsTmpOut = new short[160];
            short[] nsTmpIn2 = new short[160];

            int i = 0;
            double volAver= 0;
            for(i=0;i<len/320;i++) {
                System.arraycopy(bbuf, i*320, nsTmpByte2, 0, 320);
                ByteBuffer.wrap(nsTmpByte2).order(ByteOrder.LITTLE_ENDIAN)
                        .asShortBuffer().get(nsTmpIn2);
                volAver += getVolume(nsTmpIn2, 160);
            }

            volAver /= (len/320);

            AntsLog.d(TAG, "volAver:" + volAver);
            if(volAver >25) return 1;

            mNeedDropAudioFrm--;
            return 0;
        }


        private boolean isRunning = true;
        @Override
        public void run() {
            super.run();
            AntsLog.d(TAG, "ThreadDecodeAudio start ");

            int offset = 0;
//            mG711Decoder = new G711Decoder();
            mMobileAEC.mPlayerFlag = true;

            int tmpCnt = 0;
            while (isRunning) {
                AntsLog.d(TAG, "nDecode audioFrameQueue size = " + audioFrameQueue.size());

                if(talkModeChangeFlag){
                    AntsLog.d(TAG, "talk mode is change, release audio track.");
                    releaseAudioTrack();
                    talkModeChangeFlag = false;
                }

                if(audioFrameQueue.size() > 5) {
                    synchronized (OBJECT) {
                        if(mIsAudioOptimize == 1){
                            if(audioFrameQueue.size() > 100) {
                                AntsLog.d(TAG, "drop incoming audio "+tmpCnt+", sz:"+audioFrameQueue.size() );
                                audioFrameQueue.clear();
                                mNeedDropAudioFrm = 0;
                            }else {
                                tmpCnt++;
                                if(mNeedDropAudioFrm < audioFrameQueue.size()) {
                                    mNeedDropAudioFrm = audioFrameQueue.size();
                                }
                                AntsLog.d(TAG, "may drop incoming audio " + tmpCnt + ", sz:" + audioFrameQueue.size());
                            }
                        }else{
                            audioFrameQueue.clear();
                        }
                    }
                }


                AntsLog.d(TAG, "nDecode audioFrameQueue.isEmpty() = " + audioFrameQueue.isEmpty()+", size:"+audioFrameQueue.size() );
                if (!audioFrameQueue.isEmpty()) {
                    AVFrame avFrame = null;
                    synchronized (OBJECT) {
                        avFrame = audioFrameQueue.poll();
                    }

                    if (avFrame == null) {
                        continue;
                    }

                    if (mAudioTrack == null ) {
                        AntsLog.d(TAG, "audioNum : " + audioNum);

                        int bufferSize = 2560;
                        AntsLog.d(TAG, "===talkMode="+talkMode);
                        // 这样做AEC还是一直存在 ，只是延迟了创建时间
                        if (talkMode == AntsCamera.MIC_MODE || talkMode == AntsCamera.VOIP_MODE) {
                            if (mMobileAGC == null) {
                                RingBuffer = new ByteRingBuffer(2 * 1024 * 1024);
                                mMobileAGC = new MobileAGC();
                                AudioUtil.PlayMobileAGCInit(mMobileAGC, audioNum, mModuleGain);
                                AntsLog.d(TAG, "===MobileAGC init mModule="+mModuleGain);
                            }
                            if (mMobileNS == null) {
                                mMobileNS = new MobileNS();
                                mMobileNS.init(audioNum);
                                mMobileNS.setPolicyMode(1);
                                AntsLog.d(TAG, "===MobileNS init");
                            }
                            if(mMobileAEC == null){
                                mMobileAEC = MobileAEC.getInstance();
                                AntsLog.d(TAG, "===MobileAEC init");
                            }
                        }
                        if(talkMode == AntsCamera.SINGLE_MODE){
                            mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, audioNum,
                                    AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,
                                    bufferSize, AudioTrack.MODE_STREAM);
                            AntsLog.d(TAG, "===mAudioTrack1 init");
                            AntsLog.d(TAG, "===talkMode:"+ talkMode);
                        }else if(talkMode == AntsCamera.MIC_MODE){
                            mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, audioNum,
                                    AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                                    bufferSize, AudioTrack.MODE_STREAM);
                            AntsLog.d(TAG, "===mAudioTrack2 init");
                            AntsLog.d(TAG, "===talkMode:"+ talkMode);
                        }else if (talkMode == AntsCamera.VOIP_MODE){
                            mAudioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL, audioNum,
                                    AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                                    bufferSize, AudioTrack.MODE_STREAM);
                            AntsLog.d(TAG, "===mAudioTrack3 init");
                            AntsLog.d(TAG, "===talkMode:"+ talkMode);
                        }

                        //      mAudioTrack
                        //          .setStereoVolume(mAudioTrack.getMaxVolume(), mAudioTrack.getMaxVolume());
                        mAudioTrack.play();
                    }


                    //   mG711Decoder.G711aDecode(avFrame.frmData, avFrame.getFrmSize(), decodeDataTemp);
                    AntsLog.d(TAG, "nDecode receive " + System.currentTimeMillis()
                            + " avFrame " + avFrame.getFrmNo() + "-" + avFrame.getFrmSize());

                    int length = DecodeAAC.nDecode(avFrame.frmData, avFrame.getFrmSize(),
                            decodeData, decodeData.length);

                    AntsLog.d(TAG, "nDecode decoded avFrame "
                            + avFrame.getFrmNo()+ "-" + avFrame.getFrmSize()
                            + ", decoded length:" + length);

                    if (avFrame.getFrmSize() == 0) {
                        continue;
                    }

                    AntsLog.d(TAG, "nDecode decodeErrorCount: " + decodeErrorCount);
                    if (decodeErrorCount > 3) {
                        decodeErrorCount = 0;
                        DecodeAAC.nClose();
                        DecodeAAC.nOpen();
                    }

                    if (length == 0) {
                        decodeErrorCount++;
                        continue;
                    }

                    //       AntsLog.d("mAudioTrack", "---- duplex  " + duplex);

                    if (talkMode == AntsCamera.SINGLE_MODE) {
                        try {
                            mAudioTrack.write(decodeData, 0, length);
                            mAudioTrack.flush();
                        } catch (Exception e) {

                        }
                    } else if(talkMode == AntsCamera.MIC_MODE || talkMode == AntsCamera.VOIP_MODE){
                        int count = 0;
                        int i = 0;
                        ByteBuffer.wrap(decodeData).order(ByteOrder.LITTLE_ENDIAN)
                                .asShortBuffer().get(decodeDataTemp);

                        while (i < length / 2) {
                            decodeDataTempMoNo[count] = decodeDataTemp[i];
                            i += 2;
                            count++;
                        }

                        ByteBuffer.wrap(decodeDataMoNo).order(ByteOrder.LITTLE_ENDIAN)
                                .asShortBuffer().put(decodeDataTempMoNo);
                        RingBuffer.write(decodeDataMoNo, 0, length / 2);
                        if (RingBuffer.getUsed() < 2560) {
                            continue;
                        }
                        RingBuffer.read(priWriteBuffer, 0, priWriteBuffer.length);

                        int isVoice = isVoice(priWriteBuffer, priWriteBuffer.length);
                        if(isVoice == 0) {
                            mActualDropAudioFrm++;
                            AntsLog.d(TAG, "actual drop audio:"+mActualDropAudioFrm+", still need drop:"+mNeedDropAudioFrm);

                            //fengwu copy data to backup buffer
                            System.arraycopy(priWriteBuffer, 0, priWriteBuffer_bak, 0, 2560);
                            mIsBackupBuffer = true;
                        }

                        if (mMobileAEC.mRecordFlag && isVoice == 1) {
                            for (int loop = 0; loop < 2; loop++) {
                                if (loop == 0) {
                                    // handle backup buffer
                                    if (mIsBackupBuffer == false) continue;
                                    mIsBackupBuffer = false;
                                }

                                offset = 0;
                                while (offset <= 2240) {
                                    if (loop == 0) {
                                        System.arraycopy(priWriteBuffer_bak, offset, nsTmpByte, 0, 320);
                                    } else {
                                        System.arraycopy(priWriteBuffer, offset, nsTmpByte, 0, 320);
                                    }
                                    ByteBuffer.wrap(nsTmpByte).order(ByteOrder.LITTLE_ENDIAN)
                                            .asShortBuffer().get(nsTmpIn);

                                    if (loop == 0) {
                                        // need to lower the voice db
                                        int ii;
                                        for (ii = 0; ii < 160; ii++) {
                                            nsTmpIn[ii] = (short) (nsTmpIn[ii] / (short) 128);
                                        }
                                    }

                                    try {
                                        mMobileNS.NsProcess(nsTmpIn, 1, nsTmpOut);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    try {
                                        mMobileAGC.Process(nsTmpOut, 1, audioNum, agcTmpOut, 0, (short) 0);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    try {
                                        mMobileAEC.farendBuffer(agcTmpOut, agcTmpOut.length);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    System.arraycopy(agcTmpOut, 0, writeBuffer, offset / 2, agcTmpOut.length);
                                    offset += 320;

                                }

//                                for(int start = 0; start<=1120 ;start+=160) {
//
//                                    System.arraycopy(writeBuffer, start, nsTmpOut, 0, 160);
//                                    try {
//                                        mMobileAEC.farendBuffer(nsTmpOut, nsTmpOut.length);
//                                    } catch (Exception e) {
//                                        e.printStackTrace();
//                                    }
//                                }

                                try {
                                    mAudioTrack.write(writeBuffer, 0, writeBuffer.length);
                                    mAudioTrack.flush();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }
                        }
                    }

                } else {
                    synchronized (mWaitObject) {
                        try {
                            mWaitObject.wait();
                        } catch (InterruptedException e) {
                            return;
                        }
                    }
                }
            }

            DecodeAAC.nClose();
        }
        public void stopThread(){
            isRunning = false;
        }

    }

    public double AudioVolume(byte[] buffer, int length){
        long v = 0;
        // 将 buffer 内容取出，进行平方和运算
        for (int i = 0; i < length; i++) {
            v += buffer[i] * buffer[i];
        }
        // 平方和除以数据总长度，得到音量大小。
        double mean = v / (double) length;
        double volume = 10 * Math.log10(mean);
        return  volume;
    }

    public void clearBuffer() {
        audioFrameQueue.clear();
        clearAEC();
    }

    public void clearAEC(){
        short[] aecTmpOut = new short[160];
        if(mMobileAEC != null){
            try {
                mMobileAEC.farendBuffer(aecTmpOut, aecTmpOut.length);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
