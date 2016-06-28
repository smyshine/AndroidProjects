package com.example.smy.animator;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

public class DrawShape extends Activity {

    ShapeView shapeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_draw_shape);
        shapeView = (ShapeView) findViewById(R.id.shapeView);
        shapeView.setShapeType(ShapeView.ShapeType.SHAPE_CIRCLE);
    }

    public void onClickCircle(View v)
    {
        shapeView.setShapeType(ShapeView.ShapeType.SHAPE_CIRCLE);
        shapeView.invalidate();
    }

    public void onClickRectangle(View v)
    {
        shapeView.setShapeType(ShapeView.ShapeType.SHAPE_RECTANGLE);
        shapeView.invalidate();
    }

    public void onClickRoundRectangle(View v)
    {
        shapeView.setShapeType(ShapeView.ShapeType.SHAPE_ROUND_RECT);
        shapeView.invalidate();
    }

    public void onClickOval(View v)
    {
        shapeView.setShapeType(ShapeView.ShapeType.SHAPE_OVAL);
        shapeView.invalidate();
    }

    public void onClickArc(View v)
    {
        shapeView.setShapeType(ShapeView.ShapeType.SHAPE_ARC);
        shapeView.invalidate();
    }

    public void onClickText(View v)
    {
        shapeView.setShapeType(ShapeView.ShapeType.SHAPE_TEXT);
        shapeView.invalidate();
    }
}
