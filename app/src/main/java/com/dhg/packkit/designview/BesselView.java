package com.dhg.packkit.designview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class BesselView extends View {
    public BesselView(Context context) {
        super(context);
    }

    public BesselView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint paint = new Paint();
        paint.setStrokeWidth(20);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.GREEN);

        Path path = new Path();
        path.moveTo(100, 300);
        path.quadTo(200, 200, 300, 300);
        path.quadTo(400, 400, 500, 300);

        canvas.drawPath(path, paint);
    }

}
