package lj.sword.demoappnocompose.utils

import android.content.Context
import kotlinx.coroutines.runBlocking
import lj.sword.demoappnocompose.R
import lj.sword.demoappnocompose.data.model.SupportedLanguage
import lj.sword.demoappnocompose.manager.LocaleManager
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 日期时间格式化工具类
 * 根据当前语言选择日期格式
 * 
 * @author Sword
 * @since 1.0.0
 */
@Singleton
class DateTimeFormatter @Inject constructor(
    private val localeManager: LocaleManager
) {
    
    companion object {
        // 日期格式模板
        private const val DATE_FORMAT_ZH = "yyyy年MM月dd日"
        private const val DATE_FORMAT_ZH_TW = "yyyy年MM月dd日"
        private const val DATE_FORMAT_EN = "MM/dd/yyyy"
        private const val DATE_FORMAT_JA = "yyyy年MM月dd日"
        private const val DATE_FORMAT_KO = "yyyy년 MM월 dd일"
        
        // 时间格式模板
        private const val TIME_FORMAT_12H = "h:mm a"
        private const val TIME_FORMAT_24H = "HH:mm"
        
        // 日期时间格式模板
        private const val DATETIME_FORMAT_ZH = "yyyy年MM月dd日 HH:mm"
        private const val DATETIME_FORMAT_ZH_TW = "yyyy年MM月dd日 HH:mm"
        private const val DATETIME_FORMAT_EN = "MM/dd/yyyy h:mm a"
        private const val DATETIME_FORMAT_JA = "yyyy年MM月dd日 HH:mm"
        private const val DATETIME_FORMAT_KO = "yyyy년 MM월 dd일 HH:mm"
    }
    
    /**
     * 格式化日期
     */
    fun formatDate(date: Date, use24Hour: Boolean = true): String {
        val locale = getCurrentLocale()
        val languageCode = runBlocking { localeManager.getCurrentLanguageCode() }
        val format = when (languageCode) {
            SupportedLanguage.CHINESE_SIMPLIFIED.code -> DATE_FORMAT_ZH
            SupportedLanguage.CHINESE_TRADITIONAL.code -> DATE_FORMAT_ZH_TW
            SupportedLanguage.ENGLISH.code -> DATE_FORMAT_EN
            SupportedLanguage.JAPANESE.code -> DATE_FORMAT_JA
            SupportedLanguage.KOREAN.code -> DATE_FORMAT_KO
            else -> DATE_FORMAT_ZH
        }
        
        return SimpleDateFormat(format, locale).format(date)
    }
    
    /**
     * 格式化时间
     */
    fun formatTime(date: Date, use24Hour: Boolean = true): String {
        val locale = getCurrentLocale()
        val format = if (use24Hour) TIME_FORMAT_24H else TIME_FORMAT_12H
        
        return SimpleDateFormat(format, locale).format(date)
    }
    
    /**
     * 格式化日期时间
     */
    fun formatDateTime(date: Date, use24Hour: Boolean = true): String {
        val locale = getCurrentLocale()
        val languageCode = runBlocking { localeManager.getCurrentLanguageCode() }
        val format = when (languageCode) {
            SupportedLanguage.CHINESE_SIMPLIFIED.code -> DATETIME_FORMAT_ZH
            SupportedLanguage.CHINESE_TRADITIONAL.code -> DATETIME_FORMAT_ZH_TW
            SupportedLanguage.ENGLISH.code -> if (use24Hour) "MM/dd/yyyy HH:mm" else DATETIME_FORMAT_EN
            SupportedLanguage.JAPANESE.code -> DATETIME_FORMAT_JA
            SupportedLanguage.KOREAN.code -> DATETIME_FORMAT_KO
            else -> DATETIME_FORMAT_ZH
        }
        
        return SimpleDateFormat(format, locale).format(date)
    }
    
    /**
     * 格式化相对时间（刚刚、5分钟前、1小时前等）
     */
    fun formatRelativeTime(date: Date): String {
        val now = Date()
        val diff = now.time - date.time
        
        return when {
            diff < 60 * 1000 -> getString(R.string.just_now)
            diff < 60 * 60 * 1000 -> {
                val minutes = (diff / (60 * 1000)).toInt()
                getString(R.string.minutes_ago, minutes)
            }
            diff < 24 * 60 * 60 * 1000 -> {
                val hours = (diff / (60 * 60 * 1000)).toInt()
                getString(R.string.hours_ago, hours)
            }
            diff < 7 * 24 * 60 * 60 * 1000 -> {
                val days = (diff / (24 * 60 * 60 * 1000)).toInt()
                getString(R.string.days_ago, days)
            }
            else -> formatDate(date)
        }
    }
    
    /**
     * 格式化星期几
     */
    fun formatWeekday(date: Date): String {
        val locale = getCurrentLocale()
        val calendar = Calendar.getInstance(locale)
        calendar.time = date
        
        val weekdayFormat = SimpleDateFormat("EEEE", locale)
        return weekdayFormat.format(date)
    }
    
    /**
     * 格式化星期几（简短形式）
     */
    fun formatWeekdayShort(date: Date): String {
        val locale = getCurrentLocale()
        val weekdayFormat = SimpleDateFormat("EEE", locale)
        return weekdayFormat.format(date)
    }
    
    /**
     * 获取当前Locale
     */
    private fun getCurrentLocale(): Locale {
        return localeManager.getCurrentLocale()
    }
    
    /**
     * 获取字符串资源
     */
    private fun getString(resId: Int): String {
        return StringResourceUtils.getString(resId)
    }
    
    /**
     * 获取格式化字符串资源
     */
    private fun getString(resId: Int, vararg args: Any): String {
        return StringResourceUtils.getString(resId, *args)
    }
}
