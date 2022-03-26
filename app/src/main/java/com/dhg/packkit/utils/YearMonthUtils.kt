package com.dhg.packkit.utils

import android.annotation.SuppressLint
import java.io.Serializable
import java.lang.Exception
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * 年月的一些格式化
 */
object DateUtil {
    var weekName = arrayOf("周日", "周一", "周二", "周三", "周四", "周五", "周六")
    fun getMonthDays(year: Int, month: Int): Int {
        var year = year
        var month = month
        if (month > 12) {
            month = 1
            year += 1
        } else if (month < 1) {
            month = 12
            year -= 1
        }
        val arr = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        var days = 0
        if (year % 4 == 0 && year % 100 != 0 || year % 400 == 0) {
            arr[1] = 29 // 闰年
        }
        try {
            days = arr[month - 1]
        } catch (e: Exception) {
            e.stackTrace
        }
        return days
    }

    /**
     * 给定long时间转换为当月第几天
     */
    fun getDayInMonth(time: Long): String? {
        val instance = Calendar.getInstance()
        instance.timeInMillis = time
        val i = instance[Calendar.DAY_OF_MONTH]
        return i.toString() + ""
    }

    /**
     * long-->当前时间前一天string yyyyMMdd
     */
    fun getPreDay(currenttime: Long): String {
        val preTime = currenttime - 24 * 60 * 60 * 1000
        val timeFormat = SimpleDateFormat("yyyyMMdd")
        return timeFormat.format(preTime)
    }

    /**
     * long ->当前时间后一天yyyyMMdd
     */
    fun getNextDay(currenttime: Long): String {
        val preTime = currenttime + 24 * 60 * 60 * 1000
        val timeFormat = SimpleDateFormat("yyyyMMdd")
        return timeFormat.format(preTime)
    }

    private val year: Int
        get() = Calendar.getInstance()[Calendar.YEAR]
    val month: Int
        get() = Calendar.getInstance()[Calendar.MONTH] + 1
    val currentMonthDay: Int
        get() = Calendar.getInstance()[Calendar.DAY_OF_MONTH]
    val weekDay: Int
        get() = Calendar.getInstance()[Calendar.DAY_OF_WEEK]
    val hour: Int
        get() = Calendar.getInstance()[Calendar.HOUR_OF_DAY]
    val minute: Int
        get() = Calendar.getInstance()[Calendar.MINUTE]
    val nextSunday: CustomDate
        get() {
            val c = Calendar.getInstance()
            c.add(Calendar.DATE, 7 - weekDay + 1)
            return CustomDate(
                c[Calendar.YEAR],
                c[Calendar.MONTH] + 1,
                c[Calendar.DAY_OF_MONTH]
            )
        }

    fun getWeekSunday(year: Int, month: Int, day: Int, pervious: Int): IntArray {
        val time = IntArray(3)
        val c = Calendar.getInstance()
        c[Calendar.YEAR] = year
        c[Calendar.MONTH] = month
        c[Calendar.DAY_OF_MONTH] = day
        c.add(Calendar.DAY_OF_MONTH, pervious)
        time[0] = c[Calendar.YEAR]
        time[1] = c[Calendar.MONTH] + 1
        time[2] = c[Calendar.DAY_OF_MONTH]
        return time
    }

    /**
     * 输入年月获取是星期几
     */
    fun getWeekDayFromDate(year: Int, month: Int): Int {
        val cal = Calendar.getInstance()
        cal.time = getDateFromString(year, month)
        var week_index = cal[Calendar.DAY_OF_WEEK] - 1
        if (week_index < 0) {
            week_index = 0
        }
        return week_index
    }

    /**
     * int year ,int month ->Date yyyy-Mm-dd
     */
    @SuppressLint("SimpleDateFormat")
    fun getDateFromString(year: Int, month: Int): Date? {
        val dateString = year.toString() + "-" + (if (month > 9) month else "0$month") + "-01"
        var date: Date? = null
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd")
            date = sdf.parse(dateString)
        } catch (e: ParseException) {
            println(e.message)
        }
        return date
    }

    fun isToday(date: CustomDate): Boolean {
        return date.year === year && date.month === month && date.day === currentMonthDay
    }

    fun isCurrentMonth(date: CustomDate): Boolean {
        return date.year === year && date.month === month
    }

    /**
     * string date ->long Date
     */
    fun getDateMillisecond(strDate: String?, dateFormat: String?): Long {
        val date: Date
        try {
            date = SimpleDateFormat(dateFormat, Locale.US).parse(strDate)
            return date.time / 1000
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return 0
    }

    fun getDateTimeByMillisecond_underline(
        str: String?,
        tz: String
    ): String {
        var tz = tz
        tz = "GMT$tz"
        val date = Date(java.lang.Long.valueOf(str))
        val timeZone = TimeZone.getTimeZone(tz.trim { it <= ' ' })
        val format = SimpleDateFormat("yyyy-MM-dd")
        format.timeZone = timeZone
        return format.format(date)
    }

    fun summerTimeTransform(tz: String?, dstSavings: Int): String {
        var tz = tz
        var symbol = ""
        var x = ""
        var y = ""
        try {
            if (tz == null || tz.length < 6) {
                return "+08:00"
            }
            if (!tz.contains(":")) {
                return "+08:00"
            }
            symbol = if (tz.contains("+")) {
                "+"
            } else if (tz.contains("-")) {
                "-"
            } else {
                return "+08:00"
            }
            tz = tz.substring(1, tz.length)
            val strs = tz.split(":").toTypedArray()
            var m = strs[0].toInt()
            val n = strs[1].toInt()
            if (dstSavings > 0) {
                m = m + 1
            }
            x = m.toString()
            y = n.toString()
            if (x.length == 1) {
                x = "0$x"
            }
            if (y.length == 1) {
                y = "0$y"
            }
        } catch (e: Exception) {
            return "+08:00"
        }
        return "$symbol$x:$y"
    }

    /**
     * 底下为用到的工具类
     */
    class CustomDate : Serializable {
        var year: Int
        var month: Int
        var day: Int
        var week = 0

        constructor(year: Int, month: Int, day: Int) {
            var year = year
            var month = month
            if (month > 12) {
                month = 1
                year++
            } else if (month < 1) {
                month = 12
                year--
            }
            this.year = year
            this.month = month
            this.day = day
        }


        override fun toString(): String {
            return "$year-$month-$day"
        }

        companion object {
            private const val serialVersionUID = 1L
            fun modifiDayForObject(date: CustomDate, day: Int): CustomDate {
                return CustomDate(date.year, date.month, day)
            }
        }
    }
}