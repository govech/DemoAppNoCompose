package lj.sword.demoappnocompose.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import lj.sword.demoappnocompose.data.model.LanguageConfig
import lj.sword.demoappnocompose.data.model.SupportedLanguage
import lj.sword.demoappnocompose.manager.LocaleManager
import javax.inject.Inject

/**
 * 切换语言用例
 * 
 * @author Sword
 * @since 1.0.0
 */
class SwitchLanguageUseCase @Inject constructor(
    private val localeManager: LocaleManager
) {
    
    /**
     * 切换语言
     * 
     * @param language 目标语言
     * @return 切换结果
     */
    suspend fun execute(language: SupportedLanguage): Flow<LanguageSwitchResult> = flow {
        try {
            // 保存语言到 DataStore
            localeManager.setLocale(language)
            
            // 发送语言切换事件
            // 这里可以通过 EventBus 或其他方式发送事件
            
            emit(LanguageSwitchResult.Success(language))
        } catch (e: Exception) {
            emit(LanguageSwitchResult.Error(e.message ?: "Language switch failed"))
        }
    }
    
    /**
     * 语言切换结果
     */
    sealed class LanguageSwitchResult {
        data class Success(val language: SupportedLanguage) : LanguageSwitchResult()
        data class Error(val message: String) : LanguageSwitchResult()
    }
}







