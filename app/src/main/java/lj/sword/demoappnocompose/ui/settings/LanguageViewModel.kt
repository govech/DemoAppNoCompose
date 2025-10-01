package lj.sword.demoappnocompose.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import lj.sword.demoappnocompose.data.model.LanguageConfig
import lj.sword.demoappnocompose.data.model.SupportedLanguage
import lj.sword.demoappnocompose.domain.usecase.GetAvailableLanguagesUseCase
import lj.sword.demoappnocompose.domain.usecase.GetCurrentLanguageUseCase
import lj.sword.demoappnocompose.domain.usecase.SwitchLanguageUseCase
import javax.inject.Inject

/**
 * 语言设置 ViewModel
 * 
 * @author Sword
 * @since 1.0.0
 */
@HiltViewModel
class LanguageViewModel @Inject constructor(
    private val getCurrentLanguageUseCase: GetCurrentLanguageUseCase,
    private val getAvailableLanguagesUseCase: GetAvailableLanguagesUseCase,
    private val switchLanguageUseCase: SwitchLanguageUseCase
) : ViewModel() {

    // 当前语言状态
    private val _currentLanguage = MutableStateFlow<LanguageConfig?>(null)
    val currentLanguage: StateFlow<LanguageConfig?> = _currentLanguage.asStateFlow()

    // 可用语言列表状态
    private val _availableLanguages = MutableStateFlow<List<LanguageConfig>>(emptyList())
    val availableLanguages: StateFlow<List<LanguageConfig>> = _availableLanguages.asStateFlow()

    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 语言切换事件
    private val _languageSwitchEvent = MutableStateFlow<LanguageSwitchEvent?>(null)
    val languageSwitchEvent: StateFlow<LanguageSwitchEvent?> = _languageSwitchEvent.asStateFlow()

    init {
        loadLanguageData()
    }

    /**
     * 加载语言数据
     */
    private fun loadLanguageData() {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                // 加载可用语言列表
                val languages = getAvailableLanguagesUseCase.execute()
                _availableLanguages.value = languages
                
                // 获取当前语言（一次性获取，不监听）
                val currentLanguage = getCurrentLanguageUseCase.getCurrentLanguage()
                val currentLanguageConfig = LanguageConfig(language = currentLanguage, isSelected = true)
                _currentLanguage.value = currentLanguageConfig
                
                // 更新可用语言列表中的选中状态
                val updatedLanguages = languages.map { language ->
                    if (language.language == currentLanguage) {
                        language.copy(isSelected = true)
                    } else {
                        language.copy(isSelected = false)
                    }
                }
                _availableLanguages.value = updatedLanguages
                
            } catch (e: Exception) {
                _languageSwitchEvent.value = LanguageSwitchEvent.Error(e.message ?: "Failed to load language data")
            } finally {
                _isLoading.value = false
            }
        }
        
        // 单独启动语言变化监听（不影响loading状态）
        observeLanguageChanges()
    }

    /**
     * 监听语言变化
     */
    private fun observeLanguageChanges() {
        viewModelScope.launch {
            getCurrentLanguageUseCase.execute().collect { languageConfig ->
                _currentLanguage.value = languageConfig
                
                // 更新可用语言列表中的选中状态
                val currentLanguages = _availableLanguages.value
                val updatedLanguages = currentLanguages.map { language ->
                    if (language.language == languageConfig.language) {
                        language.copy(isSelected = true)
                    } else {
                        language.copy(isSelected = false)
                    }
                }
                _availableLanguages.value = updatedLanguages
            }
        }
    }

    /**
     * 切换语言
     */
    fun switchLanguage(language: SupportedLanguage) {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                switchLanguageUseCase.execute(language).collect { result ->
                    when (result) {
                        is SwitchLanguageUseCase.LanguageSwitchResult.Success -> {
                            _languageSwitchEvent.value = LanguageSwitchEvent.Success(language)
                        }
                        is SwitchLanguageUseCase.LanguageSwitchResult.Error -> {
                            _languageSwitchEvent.value = LanguageSwitchEvent.Error(result.message)
                        }
                    }
                }
            } catch (e: Exception) {
                _languageSwitchEvent.value = LanguageSwitchEvent.Error(e.message ?: "Language switch failed")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 清除语言切换事件
     */
    fun clearLanguageSwitchEvent() {
        _languageSwitchEvent.value = null
    }

    /**
     * 语言切换事件
     */
    sealed class LanguageSwitchEvent {
        data class Success(val language: SupportedLanguage) : LanguageSwitchEvent()
        data class Error(val message: String) : LanguageSwitchEvent()
    }
}







