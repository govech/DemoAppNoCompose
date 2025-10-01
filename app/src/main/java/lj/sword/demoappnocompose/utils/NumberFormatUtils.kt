package lj.sword.demoappnocompose.utils

import kotlinx.coroutines.runBlocking
import lj.sword.demoappnocompose.data.model.SupportedLanguage
import lj.sword.demoappnocompose.manager.LocaleManager
import java.text.NumberFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 数字格式化工具类
 * 根据当前语言格式化数字、货币、百分比等
 * 
 * @author Sword
 * @since 1.0.0
 */
@Singleton
class NumberFormatUtils @Inject constructor(
    private val localeManager: LocaleManager
) {
    
    companion object {
        // 货币代码映射
        private val CURRENCY_CODES = mapOf(
            SupportedLanguage.CHINESE_SIMPLIFIED.code to "CNY",
            SupportedLanguage.CHINESE_TRADITIONAL.code to "TWD",
            SupportedLanguage.ENGLISH.code to "USD",
            SupportedLanguage.JAPANESE.code to "JPY",
            SupportedLanguage.KOREAN.code to "KRW"
        )
    }
    
    /**
     * 格式化数字
     */
    fun formatNumber(number: Number): String {
        val locale = getCurrentLocale()
        val formatter = NumberFormat.getInstance(locale)
        return formatter.format(number)
    }
    
    /**
     * 格式化数字（指定小数位数）
     */
    fun formatNumber(number: Number, minFractionDigits: Int, maxFractionDigits: Int): String {
        val locale = getCurrentLocale()
        val formatter = NumberFormat.getInstance(locale)
        formatter.minimumFractionDigits = minFractionDigits
        formatter.maximumFractionDigits = maxFractionDigits
        return formatter.format(number)
    }
    
    /**
     * 格式化整数
     */
    fun formatInteger(number: Number): String {
        val locale = getCurrentLocale()
        val formatter = NumberFormat.getIntegerInstance(locale)
        return formatter.format(number)
    }
    
    /**
     * 格式化货币
     */
    fun formatCurrency(amount: Number): String {
        val locale = getCurrentLocale()
        val formatter = NumberFormat.getCurrencyInstance(locale)
        return formatter.format(amount)
    }
    
    /**
     * 格式化货币（指定货币代码）
     */
    fun formatCurrency(amount: Number, currencyCode: String): String {
        val locale = getCurrentLocale()
        val formatter = NumberFormat.getCurrencyInstance(locale)
        formatter.currency = Currency.getInstance(currencyCode)
        return formatter.format(amount)
    }
    
    /**
     * 格式化当前语言对应的货币
     */
    fun formatCurrencyForCurrentLanguage(amount: Number): String {
        val currencyCode = getCurrencyCodeForCurrentLanguage()
        return formatCurrency(amount, currencyCode)
    }
    
    /**
     * 格式化百分比
     */
    fun formatPercentage(value: Double): String {
        val locale = getCurrentLocale()
        val formatter = NumberFormat.getPercentInstance(locale)
        return formatter.format(value)
    }
    
    /**
     * 格式化百分比（指定小数位数）
     */
    fun formatPercentage(value: Double, minFractionDigits: Int, maxFractionDigits: Int): String {
        val locale = getCurrentLocale()
        val formatter = NumberFormat.getPercentInstance(locale)
        formatter.minimumFractionDigits = minFractionDigits
        formatter.maximumFractionDigits = maxFractionDigits
        return formatter.format(value)
    }
    
    /**
     * 格式化科学计数法
     */
    fun formatScientific(number: Number): String {
        val locale = getCurrentLocale()
        val formatter = NumberFormat.getNumberInstance(locale)
        formatter.isGroupingUsed = false
        return formatter.format(number)
    }
    
    /**
     * 解析数字字符串
     */
    fun parseNumber(numberString: String): Number? {
        val locale = getCurrentLocale()
        val formatter = NumberFormat.getInstance(locale)
        return try {
            formatter.parse(numberString)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 解析货币字符串
     */
    fun parseCurrency(currencyString: String): Number? {
        val locale = getCurrentLocale()
        val formatter = NumberFormat.getCurrencyInstance(locale)
        return try {
            formatter.parse(currencyString)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 获取当前语言对应的货币代码
     */
    private fun getCurrencyCodeForCurrentLanguage(): String {
        val languageCode = runBlocking { localeManager.getCurrentLanguageCode() }
        return CURRENCY_CODES[languageCode] ?: "USD"
    }
    
    /**
     * 获取当前Locale
     */
    private fun getCurrentLocale(): Locale {
        return localeManager.getCurrentLocale()
    }
}
