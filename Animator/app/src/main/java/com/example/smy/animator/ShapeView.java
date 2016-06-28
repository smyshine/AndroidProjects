package com.example.smy.animator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by SMY on 2016/6/28.
 */
public class ShapeView extends View {

    public enum ShapeType{
        SHAPE_CIRCLE,
        SHAPE_RECTANGLE,
        SHAPE_ROUND_RECT,
        SHAPE_OVAL,
        SHAPE_ARC,
        SHAPE_TEXT
    }

    private ShapeType shapeType = ShapeType.SHAPE_CIRCLE;

    public void setShapeType(ShapeType type)
    {
        this.shapeType = type;
    }

    public ShapeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        switch (shapeType)
        {
            case SHAPE_CIRCLE:
                onDrawShapeCircle(canvas);
                break;
            case SHAPE_RECTANGLE:
                onDrawShapeRectangle(canvas);
                break;
            case SHAPE_ROUND_RECT:
                onDrawShapeRoundRect(canvas);
                break;
            case SHAPE_OVAL:
                onDrawShapeOval(canvas);
                break;
            case SHAPE_ARC:
                onDrawShapeArc(canvas);
                break;
            case SHAPE_TEXT:
                onDrawText(canvas);
                break;
            default:
                break;
        }
    }

    private void onDrawShapeCircle(Canvas canvas)
    {
        Paint paint = new Paint();
        //去锯齿
        paint.setAntiAlias(true);
        //设置颜色
        paint.setColor(getResources().getColor(android.R.color.holo_blue_light));
        //绘制普通圆
        canvas.drawCircle(200,200,100,paint);
        //设置空心Style
        paint.setStyle(Paint.Style.STROKE);
        //设置空心边框的宽度
        paint.setStrokeWidth(20);
        //绘制空心圆
        canvas.drawCircle(200,500,90,paint);
    }

    private void onDrawShapeRectangle(Canvas canvas)
    {
        Paint paint = new Paint();
        //去锯齿
        paint.setAntiAlias(true);
        //设置颜色
        paint.setColor(getResources().getColor(android.R.color.holo_blue_light));
        //绘制正方形
        canvas.drawRect(100, 100, 300, 300, paint);
        //上面代码等同于
        //RectF rel=new RectF(100,100,300,300);
        //canvas.drawRect(rel, paint);

        //设置空心Style
        paint.setStyle(Paint.Style.STROKE);
        //设置空心边框的宽度
        paint.setStrokeWidth(20);
        //绘制空心矩形
        canvas.drawRect(100, 400, 600, 800, paint);
    }

    private void onDrawShapeRoundRect(Canvas canvas)
    {
        Paint paint = new Paint();
        //去锯齿
        paint.setAntiAlias(true);
        //设置颜色
        paint.setColor(getResources().getColor(android.R.color.holo_blue_light));
        //绘制圆角矩形
        canvas.drawRoundRect(100, 100, 300, 300, 30, 30, paint);
        //上面代码等同于
        //RectF rel=new RectF(100,100,300,300);
        //canvas.drawRoundRect(rel,30,30,paint);
        //设置空心Style
        paint.setStyle(Paint.Style.STROKE);
        //设置空心边框的宽度
        paint.setStrokeWidth(20);
        //绘制空心圆角矩形
        canvas.drawRoundRect(100, 400, 600, 800, 30, 30, paint);
    }

    private void onDrawShapeOval(Canvas canvas)
    {
        Paint paint = new Paint();
        //去锯齿
        paint.setAntiAlias(true);
        //设置颜色
        paint.setColor(getResources().getColor(android.R.color.holo_orange_dark));
        //绘制椭圆
        canvas.drawOval(100, 100, 500, 300, paint);
        //设置空心Style
        paint.setStyle(Paint.Style.STROKE);
        //设置空心边框的宽度
        paint.setStrokeWidth(20);
        //绘制空心椭圆
        canvas.drawOval(100, 400, 600, 800, paint);
    }

    private void onDrawShapeArc(Canvas canvas)
    {
        Paint paint = new Paint();
        //去锯齿
        paint.setAntiAlias(true);
        //设置颜色
        paint.setColor(getResources().getColor(android.R.color.holo_orange_dark));
        RectF rel = new RectF(100, 100, 300, 300);
        //实心圆弧
        canvas.drawArc(rel, 0, 270, false, paint);
        //实心圆弧 将圆心包含在内
        RectF rel2 = new RectF(100, 400, 300, 600);
        canvas.drawArc(rel2, 0, 270, true, paint);
        //设置空心Style
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(20);

        RectF rel3 = new RectF(100, 700, 300, 900);
        canvas.drawArc(rel3, 0, 270, false, paint);

        RectF rel4 = new RectF(100, 1000, 300, 1200);
        canvas.drawArc(rel4, 0, 270, true, paint);
    }

    private void onDrawText(Canvas canvas)
    {
        Paint paint = new Paint();
        //去锯齿
        paint.setAntiAlias(true);
        //设置颜色
        paint.setColor(getResources().getColor(android.R.color.holo_orange_dark));
        paint.setTextSize(100);
        //绘制文本
        canvas.drawText("jGame of Thrones", 80, 150, paint);
    }
}
