package com.example.smy.photoloading;

/**
 * Created by SMY on 2016/9/2.
 */
public abstract class AlbumBaseFragment extends BaseTrackFragment{
    public abstract void changeCheckmode();

    public abstract boolean onBackPressed();

    public abstract void doDeleteFile();

    public abstract void doVisibleForUser();

    public abstract void clearCheckStatus();

    public abstract void selectAll();
}
