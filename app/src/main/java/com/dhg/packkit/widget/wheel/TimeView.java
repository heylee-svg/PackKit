package com.dhg.packkit.widget.wheel;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.dhg.packkit.utils.DensityUtil;

/**
 * Created by sanvar on 18-2-6.
 */

public class TimeView extends View {

    Paint p;
    int l;
    int r;
    String t1;
    String t2;
    float density;
    private static final int offset = 20;
    private int width;


    public TimeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TimeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        p = new Paint(Paint.ANTI_ALIAS_FLAG);
        density = Resources.getSystem().getDisplayMetrics().density;
        p.setTextSize(density * 12);
        p.setColor(Color.RED);
        t1 = t2 = "";
        width = DensityUtil.getScreenWidth(this.getContext());
    }


    private int getTextWidth(Paint paint, String str) {
        int iRet = 0;
        if (str != null && str.length() > 0) {
            int len = str.length();
            float[] widths = new float[len];
            paint.getTextWidths(str, widths);
            for (int j = 0; j < len; j++) {
                iRet += (int) Math.ceil(widths[j]);
            }
        }
        return iRet;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (TextUtils.isEmpty(t1) || TextUtils.isEmpty(t2)) {
            return;
        }
        int w1 = getTextWidth(p, t1); // 取文字居中
        int w2 = getTextWidth(p, t2);
        int range = w1 / 2 + w2 / 2 + offset; // 各取一半,+20 为空出一点,免得太近
        int[] ret = getFinalPosition(l, r, w1, w2, range);
        int y = getMeasuredHeight() - getPaddingBottom();
        canvas.drawText(t1, ret[0], y, p);
        canvas.drawText(t2, ret[1], y, p);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int mode = MeasureSpec.getMode(heightMeasureSpec);
        if (mode == MeasureSpec.AT_MOST) {
            Rect rect = new Rect();
            p.getTextBounds("03-22 23:", 0, "03-22 23:".length(), rect);
            int height = rect.height()+2;//加上两个像素保证文字底部绘制完全
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height + getPaddingBottom() + getPaddingTop(), MeasureSpec.EXACTLY);
            setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
        } else {
            setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
        }
    }

    private int[] getFinalPosition(int l, int r, int w1, int w2, int range) {
        int left = 0;
        int right = 0;
        int[] ret = new int[2];
        if (((l < range + w2 / 2) || (r < range + w2 / 2)) && r - l < range + range / 2) { // 最左边且贴一起了. (w2/2) 第二段文字中间,否者
            left = Math.max(0, l - w1 / 2);
            right = left + range;
        } else if (((r > width - range - w1 / 2 - offset) || (l > width - range - w1 / 2 - offset)) && r - l < range + range / 2) { // 最右边且贴一起了.,实测可能多几个像素
            right = Math.min(width - range, r - w2 / 2);
            left = right - range;
        } else if (l < range && r > width - range) { // 分别位于左右两端
            left = Math.max(0, l - w1 / 2);
            right = Math.min(width - range, r - w2 / 2);
        } else if (l < range) { // 单单左边靠左 (如果贴在一起,不会走到这里,(r < range + offset))
            left = Math.max(0, l - w1 / 2);
            right = r - w2 / 2;
        } else if (r > width - range) { // 单单右边靠右 (如果贴在一起,不会走到这里,(l > width - range - offset))
            left = l - w1 / 2;
            right = Math.min(width - range, r - w2 / 2);
        } else if (r - l < range) { // 自由位置贴一起了
            int x = (r - l) / 2; // 取中间位置
            left = l + x - w1;
            right = left + range;
        } else { // 自由且分开
            left = l - w1 / 2;
            right = r - w2 / 2;
        }
        ret[0] = left;
        ret[1] = right;
        return ret;
    }


    public void setPosition(int l, int r, String t1, String t2) {
        this.l = l;
        this.r = r;
        this.t1 = t1;
        this.t2 = t2;

        invalidate();
    }
}
