package lj.sword.demoappnocompose.manager

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * 语言管理器
 * 支持应用内语言切换
 * 
 * @author Sword
 * @since 1.0.0
 */
object LanguageManager {

    const val LANGUAGE_CHINESE = "zh"
    const val LANGUAGE_ENGLISH = "en"

    /**
     * 更新语言设置
     */
    @JvmStatic
    fun updateLanguage(context: Context, language: String): Context {
        val locale = when (language) {
            LANGUAGE_CHINESE -> Locale.CHINESE
            LANGUAGE_ENGLISH -> Locale.ENGLISH
            else -> Locale.getDefault()
        }
        
        Locale.setDefault(locale)
        
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        
        return context.createConfigurationContext(configuration)
    }

    /**
     * 获取当前语言
     */
    @JvmStatic
    fun getCurrentLanguage(): String {
        return Locale.getDefault().language
    }
}
