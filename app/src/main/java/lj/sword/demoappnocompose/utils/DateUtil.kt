package lj.sword.demoappnocompose.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * 日期工具类
 * 提供日期格式化、转换等功能
 * 
 * @author Sword
 * @since 1.0.0
 */
object DateUtil {

    const val PATTERN_DEFAULT = "yyyy-MM-dd HH:mm:ss"
    const val PATTERN_DATE = "yyyy-MM-dd"
    const val PATTERN_TIME = "HH:mm:ss"
    const val PATTERN_DATE_TIME_CN = "yyyy年MM月dd日 HH:mm:ss"
    const val PATTERN_DATE_CN = "yyyy年MM月dd日"

    /**
     * 获取当前时间戳（毫秒）
     */
    @JvmStatic
    fun currentTimeMillis(): Long = System.currentTimeMillis()

    /**
     * 获取当前时间戳（秒）
     */
    @JvmStatic
    fun currentTimeSeconds(): Long = System.currentTimeMillis() / 1000

    /**
     * 时间戳转字符串
     */
    @JvmStatic
    fun formatTimestamp(
        timestamp: Long,
        pattern: String = PATTERN_DEFAULT
    ): String {
        return try {
            val sdf = SimpleDateFormat(pattern, Locale.getDefault())
            sdf.format(Date(timestamp))
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * 字符串转时间戳
     */
    @JvmStatic
    fun parseToTimestamp(
        dateString: String,
        pattern: String = PATTERN_DEFAULT
    ): Long {
        return try {
            val sdf = SimpleDateFormat(pattern, Locale.getDefault())
            sdf.parse(dateString)?.time ?: 0
        } catch (e: Exception) {
            0
        }
    }

    /**
     * 格式化 Date 对象
     */
    @JvmStatic
    fun formatDate(
        date: Date,
        pattern: String = PATTERN_DEFAULT
    ): String {
        return try {
            val sdf = SimpleDateFormat(pattern, Locale.getDefault())
            sdf.format(date)
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * 获取今天的日期字符串
     */
    @JvmStatic
    fun getTodayString(pattern: String = PATTERN_DATE): String {
        return formatDate(Date(), pattern)
    }

    /**
     * 判断是否是今天
     */
    @JvmStatic
    fun isToday(timestamp: Long): Boolean {
        val today = getTodayString()
        val target = formatTimestamp(timestamp, PATTERN_DATE)
        return today == target
    }

    /**
     * 判断是否是昨天
     */
    @JvmStatic
    fun isYesterday(timestamp: Long): Boolean {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        val yesterday = formatDate(calendar.time, PATTERN_DATE)
        val target = formatTimestamp(timestamp, PATTERN_DATE)
        return yesterday == target
    }

    /**
     * 获取友好的时间显示
     * 例如：刚刚、1分钟前、1小时前、昨天、日期
     */
    @JvmStatic
    fun getFriendlyTime(timestamp: Long): String {
        val now = currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 60 * 1000 -> "刚刚"
            diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}分钟前"
            diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}小时前"
            isYesterday(timestamp) -> "昨天 ${formatTimestamp(timestamp, PATTERN_TIME)}"
            else -> formatTimestamp(timestamp, PATTERN_DATE_TIME_CN)
        }
    }

    /**
     * 计算两个时间戳之间的天数差
     */
    @JvmStatic
    fun daysBetween(startTimestamp: Long, endTimestamp: Long): Long {
        return (endTimestamp - startTimestamp) / (24 * 60 * 60 * 1000)
    }
}
