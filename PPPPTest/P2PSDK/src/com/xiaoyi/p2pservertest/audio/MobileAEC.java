package com.xiaoyi.p2pservertest.audio;


import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by xin.dingfeng on 2015/12/4.
 */
public class MobileAEC {
  //  static {
  //      System.loadLibrary("webrtc_aecm"); // to load the libwebrtc_aecm.so library.
  //  }

    // /////////////////////////////////////////////////////////
    // PUBLIC CONSTANTS

    /**
     * constant unable mode for Aecm configuration settings.
     */
    public static final short AECM_UNABLE = 0;

    /**
     * constant enable mode for Aecm configuration settings.
     */
    public static final short AECM_ENABLE = 1;

    public static long RecordTime = 0;

    public static long PlayTime = 0;

    public  void setRecordTime(long recordTime){
        RecordTime = recordTime;
    }

    public void setPlayTime(long playTime){
        PlayTime = playTime;
    }

    public long getRecordTime()
    {
        return RecordTime;
    }

    public long getPlayTime()
    {
        return PlayTime;
    }

    public Object WRITE_LOCK  = new Object();

    public int farendBufferIndex;
    public int sendBufferIndex;
    public int DelaytimeStamp;
    public static MobileAEC mMobileAEC = null;

    public static MobileAEC getInstance(){
        if(mMobileAEC == null){
            mMobileAEC = new MobileAEC();
            mMobileAEC.init(null);
            mMobileAEC.setAecmMode(AggressiveMode.MOST_AGGRESSIVE)
                    .prepare();
            mMobileAEC.farendBufferIndex = 0;
            mMobileAEC.sendBufferIndex = 0;
            mMobileAEC.DelaytimeStamp = 0;
        }

        return mMobileAEC;
    }

    public void reset(){
        this.sendBufferIndex = 0;
        this.farendBufferIndex = 0;
    }

    // /////////////////////////////////////////////////////////
    // PUBLIC NESTED CLASSES

    /**
     * For security reason, this class supports constant sampling frequency values in
     * {@link SamplingFrequency#FS_8000Hz FS_8000Hz}, {@link SamplingFrequency#FS_16000Hz FS_16000Hz}
     */
    public static final class SamplingFrequency {
        public long getFS() {
            return mSamplingFrequency;
        }

        /**
         * This constant represents sampling frequency in 8000Hz
         */
        public static final SamplingFrequency FS_8000Hz  = new SamplingFrequency(
                8000);

        /**
         * This constant represents sampling frequency in 16000Hz
         */
        public static final SamplingFrequency FS_16000Hz = new SamplingFrequency(
                16000);

        private final long                    mSamplingFrequency;

        private SamplingFrequency(long fs) {
            this.mSamplingFrequency = fs;
        }
    }

    /**
     * For security reason, this class supports constant aggressiveness of the AECM instance in
     * {@link AggressiveMode#MILD MILD}, {@link AggressiveMode#MEDIUM MEDIUM}, {@link AggressiveMode#HIGH HIGH},
     * {@link AggressiveMode#AGGRESSIVE AGGRESSIVE}, {@link AggressiveMode#MOST_AGGRESSIVE MOST_AGGRESSIVE}.
     */
    public static final class AggressiveMode {
        public int getMode() {
            return mMode;
        }

        /**
         * This constant represents the aggressiveness of the AECM instance in MILD_MODE
         */
        public static final AggressiveMode MILD            = new AggressiveMode(
                0);

        /**
         * This constant represents the aggressiveness of the AECM instance in MEDIUM_MODE
         */
        public static final AggressiveMode MEDIUM          = new AggressiveMode(
                1);

        /**
         * This constant represents the aggressiveness of the AECM instance in HIGH_MODE
         */
        public static final AggressiveMode HIGH            = new AggressiveMode(
                2);

        /**
         * This constant represents the aggressiveness of the AECM instance in AGGRESSIVE_MODE
         */
        public static final AggressiveMode AGGRESSIVE      = new AggressiveMode(
                3);

        /**
         * This constant represents the aggressiveness of the AECM instance in MOST_AGGRESSIVE_MODE
         */
        public static final AggressiveMode MOST_AGGRESSIVE = new AggressiveMode(
                4);

        private final int                  mMode;

        private AggressiveMode(int mode) {
            mMode = mode;
        }
    }

    // /////////////////////////////////////////////////////////
    // PRIVATE MEMBERS

    private int               mAecmHandler = -1;   // the handler of AECM instance.
    private AecmConfig        mAecmConfig  = null; // the configurations of AECM instance.
    private SamplingFrequency mSampFreq    = null; // sampling frequency of input speech data.
    private boolean           mIsInit      = false; // whether the AECM instance is initialized or not.
    private static ReentrantLock mlock = null;
    public static ByteRingBuffer  mByteRingBuffer = null;
    public static boolean mPlayerFlag = false;
    public static boolean mRecordFlag = false;
    public static long timeStamp = 0;

    // /////////////////////////////////////////////////////////
    // CONSTRUCTOR

    /**
     * To generate a new AECM instance, whether you set the sampling frequency of each parameter or not are both ok.
     *
     * @param sampFreqOfData
     *            - sampling frequency of input audio data. if null, then {@link SamplingFrequency#FS_16000Hz
     *            FS_16000Hz} is set.
     */
    public void init(SamplingFrequency sampFreqOfData) {
        setSampFreq(sampFreqOfData);
        mAecmConfig = new AecmConfig();

        // create new AECM instance but without initialize. Init things are in prepare() method instead.
        mAecmHandler = nativeCreateAecmInstance();
       // mByteRingBuffer = new ByteRingBuffer(1024*8*100);
    }

     public void CreateByteRingBuffer()
     {
         mByteRingBuffer = new ByteRingBuffer(1024*8*1024);
         mlock = new ReentrantLock(true);
     }

     public ByteRingBuffer getByteRingBuffer()
     {
         return mByteRingBuffer;
     }
    // /////////////////////////////////////////////////////////
    // PUBLIC METHODS

    /**
     * set the sampling rate of speech data.
     *
     * @param fs
     *            - sampling frequency of speech data, if null then {@link SamplingFrequency#FS_16000Hz FS_16000Hz} is
     *            set.
     */
    public void setSampFreq(SamplingFrequency fs) {
        if (fs == null)
            mSampFreq = SamplingFrequency.FS_16000Hz;
        else
            mSampFreq = fs;
    }

    /**
     * set the far-end signal of AECM instance.
     *
     * @param farendBuf
     * @param numOfSamples
     * @return the {@link MobileAEC MobileAEC} object itself.
     * @throws Exception
     *             - if farendBuffer() is called on an unprepared AECM instance or you pass an invalid parameter.<br>
     */
    public MobileAEC farendBuffer(short[] farendBuf, int numOfSamples)
            throws Exception {
        // check if AECM instance is not initialized.
        if (!mIsInit) {
            // TODO(billhoo) - create a custom exception instead of using java.lang.Exception
            throw new Exception(
                    "setFarendBuffer() called on an unprepared AECM instance.");
        }
        int ret = nativeBufferFarend(mAecmHandler, farendBuf, numOfSamples);

       // AntsLog.d("audio", "avFrame length:" + avFrame.getFrmSize() + ", decode length:" + length);
       /* if (nativeBufferFarend(mAecmHandler, farendBuf, numOfSamples) == -1) {
            throw new Exception(
                    "setFarendBuffer() failed due to invalid arguments.");
        }*/
        return this;
    }

    /**
     * core process of AECM instance, must called on a prepared AECM instance. we only support 80 or 160 sample blocks
     * of data.
     *
     * @param nearendNoisy
     *            - In buffer containing one frame of reference nearend+echo signal. If noise reduction is active,
     *            provide the noisy signal here.
     * @param nearendClean
     *            - In buffer containing one frame of nearend+echo signal. If noise reduction is active, provide the
     *            clean signal here. Otherwise pass a NULL pointer.
     * @param out
     *            - Out buffer, one frame of processed nearend.
     * @param numOfSamples
     *            - Number of samples in nearend buffer
     * @param delay
     *            - Delay estimate for sound card and system buffers <br>
     *            delay = (t_render - t_analyze) + (t_process - t_capture)<br>
     *            where<br>
     *            - t_analyze is the time a frame is passed to farendBuffer() and t_render is the time the first sample
     *            of the same frame is rendered by the audio hardware.<br>
     *            - t_capture is the time the first sample of a frame is captured by the audio hardware and t_process is
     *            the time the same frame is passed to echoCancellation().
     *
     * @throws Exception
     *             - if echoCancellation() is called on an unprepared AECM instance or you pass an invalid parameter.<br>
     */
    public void echoCancellation(short[] nearendNoisy, short[] nearendClean,
                                 short[] out, short numOfSamples, short delay) throws Exception {
        // check if AECM instance is not initialized.
        if (!mIsInit) {
            // TODO(billhoo) - create a custom exception instead of using java.lang.Exception
            throw new Exception(
                    "echoCancelling() called on an unprepared AECM instance.");
        }

        int ret = nativeAecmProcess(mAecmHandler, nearendNoisy, nearendClean, out,
                numOfSamples, delay);


       /* if (nativeAecmProcess(mAecmHandler, nearendNoisy, nearendClean, out,
                numOfSamples, delay) == -1)
            // TODO(billhoo) - create a custom exception instead of using java.lang.Exception
            throw new Exception(
                    "echoCancellation() failed due to invalid arguments.");*/
    }

    /**
     * Set the aggressiveness mode of AECM instance, more higher the mode is, more aggressive the instance will be.
     *
     * @param mode
     * @return the {@link MobileAEC MobileAEC} object itself.
     * @throws NullPointerException
     *             - if mode is null.
     */
    public MobileAEC setAecmMode(AggressiveMode mode)
            throws NullPointerException {
        // check the mode argument.
        if (mode == null)
            throw new NullPointerException(
                    "setAecMode() failed due to null argument.");

        mAecmConfig.mAecmMode = (short) mode.getMode();
        return this;
    }

    /**
     * When finished the pre-works or any settings are changed, call this to make AECM instance prepared. Otherwise your
     * new settings will be ignored by the AECM instance.
     *
     * @return the {@link MobileAEC MobileAEC} object itself.
     */
    public MobileAEC prepare() {
        if (mIsInit) {
            close();
            mAecmHandler = nativeCreateAecmInstance();
        }

        mInitAecmInstance((int) mSampFreq.getFS());
        mIsInit = true;

        // set AecConfig to native side.
        nativeSetConfig(mAecmHandler, mAecmConfig);
        return this;
    }

    /**
     * Release the resources in AECM instance and the AECM instance is no longer available until next <b>prepare()</b>
     * is called.<br>
     * You should <b>always</b> call this <b>manually</b> when all things are done.
     */
    public void close() {
        if (mIsInit) {
            nativeFreeAecmInstance(mAecmHandler);
            mAecmHandler = -1;
            mIsInit = false;
        }
    }

    // ////////////////////////////////////////////////////////
    // PROTECTED METHODS

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        // TODO(billhoo) need a safety one.
        if (mIsInit) {
            close();
        }
    }

    public void lock()
    {
        mlock.lock();
    }

    public void unlock()
    {
        mlock.unlock();
    }


    // ////////////////////////////////////////////////////////
    // PRIVATE METHODS

    /**
     * initialize the AECM instance
     *
     * @param SampFreq
     */
    private void mInitAecmInstance(int SampFreq) {
        if (!mIsInit) {
            nativeInitializeAecmInstance(mAecmHandler, SampFreq);

            // initialize configurations of AECM instance.
            mAecmConfig = new AecmConfig();

            // set default configuration of AECM instance
            nativeSetConfig(mAecmHandler, mAecmConfig);

            mIsInit = true;
        }
    }

    // ////////////////////////////////////////////////////////
    // PRIVATE NESTED CLASSES

    /**
     * Acoustic Echo Cancellation for Mobile Configuration class, holds the config Info. of AECM instance.<br>
     * [NOTE] <b>DO NOT</b> modify the name of members, or you must change the native code to match your modifying.
     * Otherwise the native code could not find pre-binding members name.<br>
     *
     */
    @SuppressWarnings("unused")
    public class AecmConfig {
        private short mAecmMode = (short) AggressiveMode.MOST_AGGRESSIVE.getMode(); // default AggressiveMode.AGGRESSIVE
        private short mCngMode  = AECM_ENABLE;                                // AECM_UNABLE, AECM_ENABLE (default)
    }

    // ///////////////////////////////////////////
    // PRIVATE NATIVE INTERFACES

    /**
     * Allocates the memory needed by the AECM. The memory needs to be initialized separately using the
     * nativeInitializeAecmInstance() method.
     *
     * @return -1: error<br>
     *         other values: created AECM instance handler.
     *
     */
    private static native int nativeCreateAecmInstance();

    /**
     * Release the memory allocated by nativeCreateAecmInstance().
     *
     * @param aecmHandler
     *            - handler of the AECM instance created by nativeCreateAecmInstance()
     * @return 0: OK<br>
     *         -1: error
     */
    private static native int nativeFreeAecmInstance(int aecmHandler);

    /**
     * Initializes an AECM instance.
     *
     * @param aecmHandler
     *            - Handler of AECM instance
     * @param samplingFrequency
     *            - Sampling frequency of data
     * @return: 0: OK<br>
     *          -1: error
     */
    private static native int nativeInitializeAecmInstance(int aecmHandler,
                                                           int samplingFrequency);

    /**
     * Inserts an 80 or 160 sample block of data into the farend buffer.
     *
     * @param aecmHandler
     *            - Handler to the AECM instance
     * @param farend
     *            - In buffer containing one frame of farend signal for L band
     * @param nrOfSamples
     *            - Number of samples in farend buffer
     * @return: 0: OK<br>
     *          -1: error
     */
    private static native int nativeBufferFarend(int aecmHandler,
                                                 short[] farend, int nrOfSamples);

    /**
     * Runs the AECM on an 80 or 160 sample blocks of data.
     *
     * @param aecmHandler
     *            - Handler to the AECM handler
     * @param nearendNoisy
     *            - In buffer containing one frame of reference nearend+echo signal. If noise reduction is active,
     *            provide the noisy signal here.
     * @param nearendClean
     *            - In buffer containing one frame of nearend+echo signal. If noise reduction is active, provide the
     *            clean signal here.Otherwise pass a NULL pointer.
     * @param out
     *            - Out buffer, one frame of processed nearend.
     * @param nrOfSamples
     *            - Number of samples in nearend buffer
     * @param msInSndCardBuf
     *            - Delay estimate for sound card and system buffers <br>
     * @return: 0: OK<br>
     *          -1: error
     */
    private static native int nativeAecmProcess(int aecmHandler,
                                                short[] nearendNoisy, short[] nearendClean, short[] out,
                                                int nrOfSamples, short msInSndCardBuf);

    /**
     * Enables the user to set certain parameters on-the-fly.
     *
     * @param aecmHandler
     *            - Handler to the AECM instance
     * @param aecmConfig
     *            - the new configuration of AECM instance to set.
     *
     * @return 0: OK<br>
     *         -1: error
     */
    private static native int nativeSetConfig(int aecmHandler,
                                              AecmConfig aecmConfig);
}

