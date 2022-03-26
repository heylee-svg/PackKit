package com.dhg.packkit.designview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.RegionIterator;
import android.util.Log;
import android.view.View;

public class MyRegionView extends View {

    public MyRegionView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);


        drawTextView2(canvas);



    }
    private void drawRegionView(Canvas canvas){
        //初始化画笔
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(2);

        Region rgn = new Region(10,10,100,100);

//		rgn.set(100, 100, 200, 200);
//        drawRegion(canvas, rgn, paint);
    }
    private void drawTextView2(Canvas canvas){


            String text = "harvic\'s blog";
            int baseLineY = 200;
            int baseLineX = 0 ;

//设置paint
            Paint paint = new Paint();
            paint.setTextSize(120); //以px为单位
            paint.setTextAlign(Paint.Align.LEFT);

//画text所占的区域
            Paint.FontMetricsInt fm = paint.getFontMetricsInt();
            int top = baseLineY + fm.top;
            int bottom = baseLineY + fm.bottom;
            int width = (int)paint.measureText(text);
            Rect rect = new Rect(baseLineX,top,baseLineX+width,bottom);

            paint.setColor(Color.GREEN);
            canvas.drawRect(rect,paint);

//画最小矩形
            Rect minRect = new Rect();
            paint.getTextBounds(text,0,text.length(),minRect);
            minRect.top = baseLineY + minRect.top;
            minRect.bottom = baseLineY + minRect.bottom;
            paint.setColor(Color.RED);
            canvas.drawRect(minRect,paint);

//写文字
            paint.setColor(Color.BLACK);
            canvas.drawText(text, baseLineX, baseLineY, paint);

    }
    private void drawTextView(Canvas canvas){
        int baseLineX = 0 ;
        int baseLineY = 200;

        //画基线
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        canvas.drawLine(baseLineX, baseLineY, 3000, baseLineY, paint);

        //写文字
        paint.setColor(Color.GREEN);
        paint.setTextSize(120); //以px为单位
        canvas.drawText("harvic\'s blog", baseLineX, baseLineY, paint);

    }

    //这个函数不懂没关系，下面会细讲
    private void drawRegion(Canvas canvas, Region rgn, Paint paint)
    {
        RegionIterator iter = new RegionIterator(rgn);
        Rect r = new Rect();

        while (iter.next(r)) {
            Log.i("MyRegionView","r is:"+r);
            canvas.drawRect(r, paint);

        }
    }

}
