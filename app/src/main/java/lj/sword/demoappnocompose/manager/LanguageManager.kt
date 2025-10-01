package lj.sword.demoappnocompose.manager

import android.content.Context
import android.content.res.Configuration
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import lj.sword.demoappnocompose.data.local.DataStoreManager
import lj.sword.demoappnocompose.data.model.LanguageConfig
import lj.sword.demoappnocompose.data.model.SupportedLanguage
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 语言切换监听器接口
 */
interface OnLocaleChangeListener {
    fun onLocaleChanged(locale: Locale)
}

/**
 * 语言管理器
 * 支持应用内语言切换和多语言管理
 * 
 * @author Sword
 * @since 1.0.0
 */
@Singleton
class LocaleManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStoreManager: DataStoreManager
) {
    
    private val listeners = mutableListOf<OnLocaleChangeListener>()
    private var currentLocale: Locale = Locale.getDefault()

    /**
     * 获取当前语言配置
     */
    fun getCurrentLanguageConfig(): Flow<LanguageConfig> {
        return dataStoreManager.getLanguageFlow().map { languageCode ->
            val supportedLanguage = SupportedLanguage.fromCode(languageCode) 
                ?: SupportedLanguage.getDefault()
            LanguageConfig(language = supportedLanguage, isSelected = true)
        }
    }

    /**
     * 获取当前语言
     */
    suspend fun getCurrentLanguage(): SupportedLanguage {
        val languageCode = dataStoreManager.getLanguage()
        return SupportedLanguage.fromCode(languageCode) ?: SupportedLanguage.getDefault()
    }

    /**
     * 获取当前语言代码
     */
    suspend fun getCurrentLanguageCode(): String {
        return dataStoreManager.getLanguage()
    }

    /**
     * 获取当前 Locale
     */
    fun getCurrentLocale(): Locale {
        return currentLocale
    }

    /**
     * 切换语言
     */
    suspend fun setLocale(language: SupportedLanguage) {
        // 保存语言设置
        dataStoreManager.saveLanguage(language.code)
        
        // 更新当前 Locale
        currentLocale = Locale(language.code.split("-")[0], 
            language.code.split("-").getOrNull(1) ?: "")
        
        // 通知监听器
        notifyLocaleChanged(currentLocale)
        
        // 发送广播通知所有 Activity
        LanguageChangeBroadcastReceiver.sendLanguageChangedBroadcast(context, language.code)
    }

    /**
     * 获取可用语言列表
     */
    fun getAvailableLanguages(): List<LanguageConfig> {
        return SupportedLanguage.getAllLanguages().map { language ->
            LanguageConfig(language = language, isSelected = false)
        }
    }

    /**
     * 注册语言切换监听器
     */
    fun addOnLocaleChangeListener(listener: OnLocaleChangeListener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }

    /**
     * 注销语言切换监听器
     */
    fun removeOnLocaleChangeListener(listener: OnLocaleChangeListener) {
        listeners.remove(listener)
    }

    /**
     * 通知所有监听器语言已改变
     */
    private fun notifyLocaleChanged(locale: Locale) {
        listeners.forEach { listener ->
            try {
                listener.onLocaleChanged(locale)
            } catch (e: Exception) {
                // 处理监听器异常，避免影响其他监听器
                e.printStackTrace()
            }
        }
    }

    /**
     * 初始化语言设置
     */
    suspend fun initializeLanguage() {
        val savedLanguage = dataStoreManager.getLanguage()
        val supportedLanguage = SupportedLanguage.fromCode(savedLanguage) 
            ?: SupportedLanguage.getDefault()
        
        currentLocale = Locale(supportedLanguage.code.split("-")[0], 
            supportedLanguage.code.split("-").getOrNull(1) ?: "")
    }

    /**
     * 验证语言代码是否有效
     */
    fun isValidLanguageCode(code: String): Boolean {
        return SupportedLanguage.fromCode(code) != null
    }

    /**
     * 获取系统语言
     */
    fun getSystemLanguage(): SupportedLanguage {
        val systemLocale = Locale.getDefault()
        val systemCode = "${systemLocale.language}-${systemLocale.country}"
        
        return SupportedLanguage.fromCode(systemCode) ?: SupportedLanguage.getDefault()
    }
}
