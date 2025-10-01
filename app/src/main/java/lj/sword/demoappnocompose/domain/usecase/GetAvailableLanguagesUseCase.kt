package lj.sword.demoappnocompose.domain.usecase

import lj.sword.demoappnocompose.data.model.LanguageConfig
import lj.sword.demoappnocompose.manager.LocaleManager
import javax.inject.Inject

/**
 * 获取可用语言用例
 * 
 * @author Sword
 * @since 1.0.0
 */
class GetAvailableLanguagesUseCase @Inject constructor(
    private val localeManager: LocaleManager
) {
    
    /**
     * 获取所有可用的语言列表
     */
    fun execute(): List<LanguageConfig> {
        return localeManager.getAvailableLanguages()
    }
}







