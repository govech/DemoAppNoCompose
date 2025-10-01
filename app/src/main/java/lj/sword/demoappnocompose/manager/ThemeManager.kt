package lj.sword.demoappnocompose.manager

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.util.TypedValue
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import lj.sword.demoappnocompose.data.local.datastore.ThemeDataStore
import lj.sword.demoappnocompose.data.model.AppTheme
import lj.sword.demoappnocompose.data.model.ThemeConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 主题管理器
 */
@Singleton
class ThemeManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val themeDataStore: ThemeDataStore
) {
    
    /**
     * 获取当前主题配置
     */
    fun getCurrentThemeConfig(): Flow<ThemeConfig> {
        return combine(
            themeDataStore.getThemeId(),
            themeDataStore.getDarkMode(),
            themeDataStore.getFollowSystem()
        ) { themeId, isDarkMode, followSystem ->
            ThemeConfig(
                currentTheme = AppTheme.fromThemeId(themeId),
                isDarkMode = isDarkMode,
                followSystem = followSystem
            )
        }
    }
    
    /**
     * 切换主题
     */
    suspend fun switchTheme(theme: AppTheme) {
        themeDataStore.saveThemeId(theme.themeId)
    }
    
    /**
     * 切换暗黑模式
     */
    suspend fun switchDarkMode(isDarkMode: Boolean) {
        themeDataStore.saveDarkMode(isDarkMode)
    }
    
    /**
     * 设置跟随系统
     */
    suspend fun setFollowSystem(followSystem: Boolean) {
        themeDataStore.saveFollowSystem(followSystem)
    }
    
    /**
     * 应用主题到Activity
     */
    fun applyTheme(activity: Activity, themeConfig: ThemeConfig) {
        val themeRes = if (themeConfig.isDarkMode) {
            themeConfig.currentTheme.nightStyleRes
        } else {
            themeConfig.currentTheme.styleRes
        }
        
        activity.setTheme(themeRes)
    }
    
    /**
     * 判断系统是否处于暗黑模式
     */
    fun isSystemDarkMode(context: Context): Boolean {
        val nightModeFlags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    }
    
    /**
     * 根据属性获取颜色
     */
    fun getThemeColor(context: Context, attrRes: Int): Int {
        val typedValue = TypedValue()
        val theme = context.theme
        theme.resolveAttribute(attrRes, typedValue, true)
        return ContextCompat.getColor(context, typedValue.resourceId)
    }
    
    /**
     * 调整颜色透明度
     */
    fun adjustColorAlpha(color: Int, alpha: Float): Int {
        val alphaInt = (alpha * 255).toInt().coerceIn(0, 255)
        return (color and 0x00FFFFFF) or (alphaInt shl 24)
    }
}
