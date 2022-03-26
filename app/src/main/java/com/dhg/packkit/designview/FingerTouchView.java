package com.dhg.packkit.designview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class FingerTouchView extends View {
    private static final String TAG = FingerTouchView.class.getSimpleName();
    private float mPreX, mPreY;
    private Path mPath = new Path();

    public FingerTouchView(Context context) {
        super(context);
    }

    public FingerTouchView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//      return  drawLinePath(event);
        return drawBesselPath(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(20);
        canvas.drawPath(mPath, paint);
        drawSubTextView(canvas);
    }

    private void drawSubTextView(Canvas canvas){
        Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        String text = "乌龟&梦想";
        paint.setTextSize(200);

        paint.setSubpixelText(false);
        canvas.drawText(text,0,200,paint);

        canvas.translate(0,300);
        paint.setSubpixelText(true);
        canvas.drawText(text,0,200,paint);
    }
    private boolean drawBesselPath(MotionEvent event) {


        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                mPath.moveTo(event.getX(), event.getY());
                mPreX = event.getX();
                mPreY = event.getY();

                return true;
            }
            case MotionEvent.ACTION_MOVE: {
                float endX = (mPreX + event.getX()) / 2;
                float endY = (mPreY + event.getY()) / 2;
                mPath.quadTo(mPreX, mPreY, endX, endY);
                mPreX = event.getX();
                mPreY = event.getY();
                invalidate();
            }
            break;
            default:
                break;
        }
        return super.onTouchEvent(event);

    }

    private boolean drawLinePath(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                mPath.moveTo(event.getX(), event.getY());
                return true;
            }
            case MotionEvent.ACTION_MOVE:
                mPath.lineTo(event.getX(), event.getY());
                postInvalidate();
                break;
            default:
                break;

        }
        return super.onTouchEvent(event);
    }


    public void reset() {
        mPath.reset();
        invalidate();
    }
}
