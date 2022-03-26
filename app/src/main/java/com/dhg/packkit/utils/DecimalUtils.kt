package com.dhg.packkit.utils

import java.text.DecimalFormat

object DecimalUtils {
    //℃
    fun decimalformat(num: Float): String {
        val decimalFormat =
            DecimalFormat(".0") //构造方法的字符格式这里如果小数不足2位,会以0补足.
        return decimalFormat.format(num.toDouble()) //format 返
    }

    //    华氏温度转换   ℉
    fun decimalformatFahrenheit(num: Float): String {
        val decimalFormat =
            DecimalFormat(".0") //构造方法的字符格式这里如果小数不足2位,会以0补足.
        return decimalFormat.format((num * 1.8f + 32).toDouble()) //format 返
    }

    /**
     * 保留两位小数
     *
     * @param num
     * @return
     */
    fun decimalformat2pointer(num: Float): String {
        val decimalFormat =
            DecimalFormat(".00") //构造方法的字符格式这里如果小数不足2位,会以0补足.
        return decimalFormat.format(num.toDouble()) //format 返
    }

    fun deicalformat(time: Int): String? {
        val decimalFormat = DecimalFormat("00")
        var str = ""
        str = if (time < 60) {
            "00:" + decimalFormat.format(time.toLong())
        } else {
            val min = time / 60
            val sec = time - time / 60 * 60
            decimalFormat.format(min.toLong()) + ":" + decimalFormat.format(sec.toLong())
        }
        return str
    }


}