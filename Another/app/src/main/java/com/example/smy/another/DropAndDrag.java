package com.example.smy.another;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class DropAndDrag extends Activity{

    ImageView image;
    private RelativeLayout.LayoutParams layoutParams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drop_and_drag);
        image = (ImageView)findViewById(R.id.imageView);

        image.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipData.Item item = new ClipData.Item((CharSequence)v.getTag());
                String[] mimeType = {ClipDescription.MIMETYPE_TEXT_PLAIN};

                ClipData dragData = new ClipData(v.getTag().toString(), mimeType, item);
                View.DragShadowBuilder myShadow = new View.DragShadowBuilder(image);

                v.startDrag(dragData, myShadow, null, 0);

                return true;
            }
        });

        image.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        layoutParams = (RelativeLayout.LayoutParams) v.getLayoutParams();
                        return true;
                    case DragEvent.ACTION_DRAG_ENDED:
                    case DragEvent.ACTION_DROP:
                    case DragEvent.ACTION_DRAG_LOCATION:
                    case DragEvent.ACTION_DRAG_ENTERED:
                    //    int x_cord = (int) event.getX();
                    //    int y_cord = (int) event.getY();
                    //    break;
                    case DragEvent.ACTION_DRAG_EXITED:
                        int x_cord = (int) event.getX();
                        int y_cord = (int) event.getY();
                        layoutParams.leftMargin = x_cord;
                        layoutParams.topMargin = y_cord;
                        v.setLayoutParams(layoutParams);
                        return true;
                    //case DragEvent.ACTION_DRAG_LOCATION:
                    //    x_cord = (int) event.getX();
                    //    y_cord = (int) event.getY();
                    //    break;
                    //case DragEvent.ACTION_DRAG_ENDED:
                    //    break;
                    //case DragEvent.ACTION_DROP:
                    //    return true;
                    default:
                        return true;
                }
                //return true;
            }
        });

        image.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    ClipData data = ClipData.newPlainText("", "");
                    View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(image);

                    image.startDrag(data, shadowBuilder, image, 0);
                    image.setVisibility(View.INVISIBLE);
                    return true;
                }
                return false;
            }
        });
    }
}
