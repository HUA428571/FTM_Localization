package com.example.ftm_1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

// 自定义视图类，用于展示定位点
public class LocationView extends View
{
    // 画笔对象，用于绘制图形

    private Paint paint;
    private Paint apPaint; // 用于绘制AP的画笔
    private Paint locationPaint; // 用于绘制定位点的画笔


    // 房间尺寸和AP位置
    private float roomLength, roomWidth;
    private float[][] apPositions = new float[4][2];
    // 定位点的坐标，初始化为(0,0)
    private float x = -100, y = -100;

    private float scale; // 缩放比例
    private float offsetX, offsetY; // 绘图的偏移量

    // 构造函数，用于创建视图实例
    public LocationView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        initLocationView(); // 初始化视图
    }

    // 初始化视图的方法

    private void initLocationView()
    {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK); // 将颜色设置为黑色
        paint.setStrokeWidth(5f); // 设置线条宽度，加粗线条

        // AP的画笔
        apPaint = new Paint();
        apPaint.setAntiAlias(true);
        apPaint.setColor(Color.BLUE); // AP的颜色

        // 定位点的画笔
        locationPaint = new Paint();
        locationPaint.setAntiAlias(true);
        locationPaint.setColor(Color.RED); // 定位点的颜色
    }

    // 设置房间尺寸的方法
    public void setRoomSize(float length, float width)
    {
        this.roomLength = length;
        this.roomWidth = width;

        // 获取屏幕尺寸
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float screenWidth = metrics.widthPixels;
        float screenHeight = metrics.heightPixels;
        Log.d("LocationView", "screenWidth: " + screenWidth + ", screenHeight: " + screenHeight);

        // 计算缩放比例（屏幕宽度的80%）
        scale = (screenWidth * 0.80f) / length;

        // 计算偏移量，使房间居中显示
        offsetX = (screenWidth - (length * scale)) / 2;
//        offsetY = (screenHeight - (width * scale)) / 2;
        offsetY = 10f;

        invalidate(); // 重绘视图
    }

    // 设置AP位置的方法
    public void setApPositions(float[][] positions)
    {
        this.apPositions = positions;
        invalidate(); // 重绘视图
    }

    public void setLocation(float positionX, float positionY)
    {
        this.x = positionX;
        this.y = positionY;
        invalidate(); // 重绘视图
    }


    // 设置定位点位置的方法，并重绘视图
    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        // 绘制房间矩形，考虑缩放和偏移
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(offsetX, offsetY, offsetX + roomLength * scale, offsetY + roomWidth * scale, paint);

        // 绘制AP位置
        for (float[] apPosition : apPositions)
        {
            canvas.drawCircle(offsetX + apPosition[0] * scale, offsetY + apPosition[1] * scale, 10, apPaint);
        }

        // 绘制定位点，考虑缩放和偏移
        canvas.drawCircle(offsetX + x * scale, offsetY + y * scale, 10, locationPaint);
    }
}

