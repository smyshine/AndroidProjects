package com.example.smy.choosepic;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by SMY on 2016/6/29.
 */
public class PictureChoosingDialogFragment extends DialogFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int style = R.style.DimPanel;
        setStyle(DialogFragment.STYLE_NO_TITLE, style);
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View view = layoutInflater.inflate(inflateLayout(), viewGroup, false);

        view.findViewById(R.id.take_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhotoClick();
            }
        });

        view.findViewById(R.id.choose_album).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickAlbumClick();
            }
        });

        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CancelClick();
            }
        });

        view.findViewById(R.id.empty_area).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emptyAreaClick();
            }
        });

        return view;
    }

    private void takePhotoClick()
    {
        if (listener != null)
        {
            listener.onTakePictureClick(this);
        }
        dismiss();
    }

    private void pickAlbumClick()
    {
        if (listener != null)
        {
            listener.onPickAlbumClick(this);
        }
        dismiss();
    }

    private void CancelClick()
    {
        if (listener != null)
        {
            listener.onCancelClick(this);
        }
        dismiss();
    }

    private void emptyAreaClick() {
        if (listener != null)
        {
            listener.onEmptyAreaClick(this);
        }
        dismiss();
    }

    protected int inflateLayout() {
        return R.layout.picture_choosing_fragment;
    }

    private onOptionClickListener listener;

    public void setOnOptionClickListener(onOptionClickListener listener)
    {
        this.listener = listener;
    }

    public interface onOptionClickListener{
        void onTakePictureClick(DialogFragment paramDialogFragment);
        void onPickAlbumClick(DialogFragment paramDialogFragment);
        void onCancelClick(DialogFragment paramDialogFragment);
        void onEmptyAreaClick(DialogFragment paramDialogFragment);
    }

}
