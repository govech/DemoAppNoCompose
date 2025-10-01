package lj.sword.demoappnocompose.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import lj.sword.demoappnocompose.data.model.AppTheme
import lj.sword.demoappnocompose.data.model.ThemeConfig
import lj.sword.demoappnocompose.manager.ThemeManager
import javax.inject.Inject

/**
 * 主题设置ViewModel
 */
@HiltViewModel
class ThemeSettingsViewModel @Inject constructor(
    private val themeManager: ThemeManager
) : ViewModel() {

    private val _themeConfig = MutableStateFlow(ThemeConfig())
    val themeConfig: StateFlow<ThemeConfig> = _themeConfig.asStateFlow()

    init {
        observeThemeConfig()
    }

    private fun observeThemeConfig() {
        viewModelScope.launch {
            themeManager.getCurrentThemeConfig().collect { config ->
                _themeConfig.value = config
            }
        }
    }

    /**
     * 切换主题
     */
    fun switchTheme(theme: AppTheme) {
        viewModelScope.launch {
            try {
                themeManager.switchTheme(theme)
            } catch (e: Exception) {
                // 处理主题切换异常
                e.printStackTrace()
            }
        }
    }

    /**
     * 切换暗黑模式
     */
    fun switchDarkMode(isDarkMode: Boolean) {
        viewModelScope.launch {
            try {
                themeManager.switchDarkMode(isDarkMode)
            } catch (e: Exception) {
                // 处理暗黑模式切换异常
                e.printStackTrace()
            }
        }
    }

    /**
     * 设置跟随系统
     */
    fun setFollowSystem(followSystem: Boolean) {
        viewModelScope.launch {
            try {
                themeManager.setFollowSystem(followSystem)
            } catch (e: Exception) {
                // 处理跟随系统设置异常
                e.printStackTrace()
            }
        }
    }
}
