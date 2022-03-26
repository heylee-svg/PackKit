package com.dhg.packkit.utils

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class TimeUtils {
    val DAY_TIME = 24 * 60 * 60 * 1000L
    val HOUR_TIME = 60 * 60 * 1000L

    /**
     * 6- >"06"格式化输出
     * @param time
     * @return
     */
    fun StringFormat(time: Int): String? {
        return String.format("%02d", time)
    }

    /**
     * "02:00" -> 获取分的时间int 2
     */
    fun string2Int(str: String): Int {
        return str.split(":")[0].toInt()
    }

    /**
     * string style time ->long time
     *  "23:59" ->86340秒 long
     *
     */
    fun DateString2long(time: String): Long {
        var df = SimpleDateFormat("MM-dd") //这个格式根据
        return df.parse(time).time
    }

    /**
     * string style time ->long time
     * 86340秒 ->"23:59"
     *
     */
    fun Datelong2String(time: Long): String {
        var df = SimpleDateFormat("MM-dd") //这个格式根据
        return df.format(time)
    }

    /**
     * 获取当天0点时间戳
     *
     * @return
     */
    fun getTodayStartTime(): Long {
        val calendar = Calendar.getInstance()
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        return calendar.timeInMillis
    }

    /**
     * 获取当天结束的时间点
     */
    fun getTodayEndTime(): Long {
        val calendar = Calendar.getInstance()
        calendar[Calendar.HOUR_OF_DAY] = 23
        calendar[Calendar.MINUTE] = 59
        calendar[Calendar.SECOND] = 59
        return calendar.timeInMillis
    }


    /**
     * 获取具体某一天的结束时间
     */
    fun getSpecificDayEndTime(time: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time
        calendar[Calendar.HOUR_OF_DAY] = 23
        calendar[Calendar.MINUTE] = 59
        calendar[Calendar.SECOND] = 59
        return calendar.timeInMillis
    }

    /**
     * 给定时间格式化为离当前时间多远
     */
    fun getVisitorTime(lastTime: Long): String? {
        val todayStartTime: Long = getTodayStartTime()
        val currentTimeMillis = System.currentTimeMillis()
        val distance = lastTime - todayStartTime
        if (distance < 0 && Math.abs(distance) < DAY_TIME * 7) {
            val day: String =
                Math.ceil(Math.abs(distance) / DAY_TIME as Double) as String
            return "${day}天前"
        }
        val second = (currentTimeMillis - lastTime) / 1000
        val minute = second / 60
        if (minute < 60) {
            return "${minute}分钟前"
        }
        val hour = minute / 60
        return if (hour < 24) {
            "${hour}小时前"
        } else getYHM(lastTime)
    }

    private val SimpleDateFormatYYYYHHMM: ThreadLocal<SimpleDateFormat?> =
        object : ThreadLocal<SimpleDateFormat?>() {
            override fun initialValue(): SimpleDateFormat {
                return SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
            }
        }

    fun getYHM(time: Long): String? {
        return SimpleDateFormatYYYYHHMM.get()?.format(Date(time))
    }
}