package com.dhg.packkit.utils

import android.content.Context
import android.util.TypedValue
import android.view.WindowManager

/**
 * Created by hou on 2015/8/7.
 */
object DisplayUtils {


    fun getWidthHeight(context: Context): IntArray {
        val wm = context
            .getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val width = wm.defaultDisplay.width
        val height = wm.defaultDisplay.height

        return intArrayOf(width, height)
    }

    fun dip2px(context: Context, dipValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dipValue * scale + 0.5f).toInt()
    }

    fun px2dip(context: Context, pxValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }

    /**
     * dp转px
     *
     * @param context
     * @param dpVal
     * @return pxVal
     */
    fun dp2px(context: Context, dpVal: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dpVal, context.resources.displayMetrics
        ).toInt()
    }

    /**
     * sp转px
     *
     * @param context
     * @param spVal
     * @return pxVal
     */
    fun sp2px(context: Context, spVal: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            spVal, context.resources.displayMetrics
        ).toInt()
    }

    /**
     * px转dp
     *
     * @param context
     * @param pxVal
     * @return dpVal
     */
    fun px2dp(context: Context, pxVal: Float): Float {
        val scale = context.resources.displayMetrics.density
        return pxVal / scale
    }

    /**
     * px转sp
     *
     * @param context
     * @param pxVal
     * @return spVal
     */
    fun px2sp(context: Context, pxVal: Float): Float {
        return pxVal / context.resources.displayMetrics.scaledDensity
    }


    /**
     * sp转px
     *
     * @param context
     * @param spVal
     * @return pxVal
     */
    fun sp2dp(context: Context, spVal: Int): Int {
        return px2dp(context, sp2px(context, spVal.toFloat()).toFloat()).toInt()
    }



}
