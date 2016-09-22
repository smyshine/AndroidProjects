package com.example.smy.collapsingtextview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by SMY on 2016/9/20.
 */
public class CollapsibleTextView extends LinearLayout implements View.OnClickListener {

    /** default text show max lines */
    private static final int DEFAULT_MAX_LINE_COUNT = 2;

    private static final int COLLAPSIBLE_STATE_NONE = 0;
    private static final int COLLAPSIBLE_STATE_SHRINKUP = 1;
    private static final int COLLAPSIBLE_STATE_SPREAD = 2;

    private TextView desc;
    private TextView descOp;

    private String shrinkup;
    private String spread;
    private int mState;
    private boolean flag;

    public CollapsibleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        shrinkup = context.getString(R.string.collapsing_text_desc_shrink_up);
        spread = context.getString(R.string.collapsing_text_desc_spread);
        View view = inflate(context, R.layout.collapsible_text_view, this);
        view.setPadding(0, -1, 0, 0);
        desc = (TextView) view.findViewById(R.id.desc_tv);
        descOp = (TextView) view.findViewById(R.id.desc_op_tv);
        descOp.setOnClickListener(this);
    }

    public CollapsibleTextView(Context context) {
        this(context, null);
    }

    public final void setDesc(CharSequence charSequence, TextView.BufferType bufferType) {
        desc.setText(charSequence, bufferType);
        mState = COLLAPSIBLE_STATE_SPREAD;
        requestLayout();
    }

    @Override
    public void onClick(View v) {
        flag = false;
        requestLayout();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (!flag) {
            flag = true;
            if (desc.getLineCount() <= DEFAULT_MAX_LINE_COUNT) {
                mState = COLLAPSIBLE_STATE_NONE;
                descOp.setVisibility(View.GONE);
                desc.setMaxLines(DEFAULT_MAX_LINE_COUNT + 1);
            } else {
                post(new InnerRunnable());
            }
        }
    }

    class InnerRunnable implements Runnable {
        @Override
        public void run() {
            if (mState == COLLAPSIBLE_STATE_SPREAD) {
                desc.setMaxLines(DEFAULT_MAX_LINE_COUNT);
                descOp.setVisibility(View.VISIBLE);
                descOp.setText(spread);
                mState = COLLAPSIBLE_STATE_SHRINKUP;
            } else if (mState == COLLAPSIBLE_STATE_SHRINKUP) {
                desc.setMaxLines(Integer.MAX_VALUE);
                descOp.setVisibility(View.VISIBLE);
                descOp.setText(shrinkup);
                mState = COLLAPSIBLE_STATE_SPREAD;
            }
        }
    }
}
