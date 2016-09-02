package com.example.smy.photoloading;

import android.support.v4.app.Fragment;

/**
 * 需要统计屏幕访问次数的fragment
 * Created by SMY on 2016/9/2.
 */
public class BaseTrackFragment extends Fragment {

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        //统计fragment屏幕
        if (!isVisibleToUser) {
            return;
        }
        /*
        final Tracker tracker = CameraApplication.getTracker(getActivity());
        if (tracker != null) {
            tracker.setScreenName(getClass().getSimpleName());
            tracker.send(new HitBuilders.ScreenViewBuilder().build());
        }*/
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
       /* RefWatcher refWatcher = CameraApplication.getRefWatcher(getActivity());
        if(refWatcher!=null){
            refWatcher.watch(this);
        }*/
    }
}
