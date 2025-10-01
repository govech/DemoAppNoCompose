package lj.sword.demoappnocompose.domain.usecase

import kotlinx.coroutines.flow.Flow
import lj.sword.demoappnocompose.data.model.LanguageConfig
import lj.sword.demoappnocompose.manager.LocaleManager
import javax.inject.Inject

/**
 * 获取当前语言用例
 * 
 * @author Sword
 * @since 1.0.0
 */
class GetCurrentLanguageUseCase @Inject constructor(
    private val localeManager: LocaleManager
) {
    
    /**
     * 获取当前语言配置
     */
    fun execute(): Flow<LanguageConfig> {
        return localeManager.getCurrentLanguageConfig()
    }
    
    /**
     * 获取当前语言（suspend）
     */
    suspend fun getCurrentLanguage() = localeManager.getCurrentLanguage()
}







