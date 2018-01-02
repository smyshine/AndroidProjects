package com.tutk.IOTC;

import com.xiaoyi.log.AntsLog;

public class TutkLanSearcher {

    public static final String TAG = "TutkLanSearcher";

    private SearchTheard mSearchThread;

    public interface ITutkOnSearchResultListener {
        void onSearchResult(String uid, String ip);

        void onSearchError();
    }

    public ITutkOnSearchResultListener tutkOnSearchResultListener;

    public TutkLanSearcher(ITutkOnSearchResultListener listener) {
        this.tutkOnSearchResultListener = listener;
    }

    public void setTutkOnSearchResultListener(ITutkOnSearchResultListener listener) {
        this.tutkOnSearchResultListener = listener;
    }

    public void removeTutkOnSearchResultCallback() {
        this.tutkOnSearchResultListener = null;
    }

    public void startLANSearch(int nWaitTimeMs) {
        IOTCAPIs.IOTC_Search_Device_Start(nWaitTimeMs, 100);
        mSearchThread = new SearchTheard();
        mSearchThread.start();
        AntsLog.d(TAG, "TutkLanSearcher start");
    }

    public void stopLANSearch() {
        IOTCAPIs.IOTC_Search_Device_Stop();
        if (mSearchThread != null) {
            mSearchThread.stopThread();
            mSearchThread = null;
        }
        AntsLog.d(TAG, "TutkLanSearcher stop");
    }

    public class SearchTheard extends Thread {

        boolean isRunning = true;
        int[] nArray = new int[1];

        public void run() {
            while (isRunning) {

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (!isRunning) {
                    break;
                }

                st_SearchDeviceInfo[] ab_LanSearchInfo = IOTCAPIs.IOTC_Search_Device_Result(nArray,
                        1);
                AntsLog.d(TAG, "searched size=" + nArray[0]);
                if (nArray[0] < 0) {
                    if (tutkOnSearchResultListener != null) {
                        tutkOnSearchResultListener.onSearchError();
                    }
                    break;
                }

                for (int i = 0; i < nArray[0]; i++) {
                    st_SearchDeviceInfo info = ab_LanSearchInfo[i];
                    if (tutkOnSearchResultListener != null) {
                        String uid = new String(info.UID).trim();
                        String ip = new String(info.IP).trim();
                        tutkOnSearchResultListener.onSearchResult(uid, ip);
                    }
                }

            }
        };

        public void stopThread() {
            isRunning = false;
        }

    }

}
