package lj.sword.demoappnocompose.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

/**
 * Context 语言包装工具类
 * 提供语言切换的 Context 包装功能
 * 
 * @author Sword
 * @since 1.0.0
 */
object ContextUtils {

    /**
     * 包装 Context 以应用指定的语言
     * 
     * @param context 原始 Context
     * @param locale 目标语言 Locale
     * @return 包装后的 Context
     */
    fun Context.wrap(locale: Locale): Context {
        return try {
            val configuration = Configuration(resources.configuration)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // API 24+ 使用 createConfigurationContext
                configuration.setLocale(locale)
                createConfigurationContext(configuration)
            } else {
                // API 24 以下使用 updateConfiguration
                @Suppress("DEPRECATION")
                configuration.locale = locale
                @Suppress("DEPRECATION")
                resources.updateConfiguration(configuration, resources.displayMetrics)
                this
            }
        } catch (e: Exception) {
            // 如果语言包装失败，返回原始 Context
            e.printStackTrace()
            this
        }
    }

    /**
     * 安全地获取字符串资源
     * 在非 Activity/Fragment 中也能正确获取多语言字符串
     * 
     * @param context Context
     * @param resId 字符串资源 ID
     * @param vararg args 格式化参数
     * @return 本地化后的字符串
     */
    fun getString(context: Context, resId: Int, vararg args: Any): String {
        return try {
            if (args.isEmpty()) {
                context.getString(resId)
            } else {
                context.getString(resId, *args)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "String not found"
        }
    }

    /**
     * 获取当前 Context 的语言代码
     * 
     * @param context Context
     * @return 语言代码，如 "zh-CN", "en-US"
     */
    fun getCurrentLanguageCode(context: Context): String {
        val locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0]
        } else {
            @Suppress("DEPRECATION")
            context.resources.configuration.locale
        }
        
        return "${locale.language}-${locale.country}"
    }

    /**
     * 检查 Context 是否支持指定的语言
     * 
     * @param context Context
     * @param languageCode 语言代码
     * @return 是否支持
     */
    fun isLanguageSupported(context: Context, languageCode: String): Boolean {
        return try {
            val locale = Locale.forLanguageTag(languageCode)
            val configuration = Configuration(context.resources.configuration)
            configuration.setLocale(locale)
            
            // 尝试创建配置上下文，如果成功则支持该语言
            context.createConfigurationContext(configuration)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 获取系统默认语言
     * 
     * @return 系统默认 Locale
     */
    fun getSystemLocale(): Locale {
        return Locale.getDefault()
    }

    /**
     * 比较两个语言代码是否相同
     * 
     * @param code1 语言代码1
     * @param code2 语言代码2
     * @return 是否相同
     */
    fun isSameLanguage(code1: String, code2: String): Boolean {
        val locale1 = Locale.forLanguageTag(code1)
        val locale2 = Locale.forLanguageTag(code2)
        return locale1.language == locale2.language
    }
}
