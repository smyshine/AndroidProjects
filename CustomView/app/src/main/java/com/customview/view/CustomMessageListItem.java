package com.customview.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.customview.R;

/**
 * Created by SMY on 2017/10/18.
 */

public class CustomMessageListItem extends RelativeLayout {

    private static final int[] STATUS_MESSAGE_READED = {R.attr.status_message_readed };
    private boolean mMessageReaded = false;

    public CustomMessageListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setMessageReaded(boolean readed){
        if (this.mMessageReaded != readed){
            this.mMessageReaded = readed;
            refreshDrawableState();
        }
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        if (mMessageReaded){
            final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
            mergeDrawableStates(drawableState,  STATUS_MESSAGE_READED);
            return drawableState;
        }
        return super.onCreateDrawableState(extraSpace);
    }
}
