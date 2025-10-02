package lj.sword.demoappnocompose.data.model

import lj.sword.demoappnocompose.R

/**
 * 应用主题枚举
 */
enum class AppTheme(
    val themeId: String,
    val themeName: String,
    val styleRes: Int,
    val nightStyleRes: Int
) {
    DEFAULT(
        themeId = "default",
        themeName = "默认主题",
        styleRes = R.style.DefaultTheme,
        nightStyleRes = R.style.DefaultTheme  // 暂时使用相同的，因为themes.xml中夜间模式已经通过values-night处理
    ),
    BUSINESS(
        themeId = "business",
        themeName = "商务主题",
        styleRes = R.style.BusinessTheme,
        nightStyleRes = R.style.BusinessTheme
    ),
    VIBRANT(
        themeId = "vibrant",
        themeName = "活力主题",
        styleRes = R.style.VibrantTheme,
        nightStyleRes = R.style.VibrantTheme
    );
    
    companion object {
        /**
         * 根据主题ID获取主题枚举
         */
        fun fromThemeId(themeId: String): AppTheme {
            return values().find { it.themeId == themeId } ?: DEFAULT
        }
    }
}

/**
 * 主题配置数据类
 */
data class ThemeConfig(
    val currentTheme: AppTheme = AppTheme.DEFAULT,
    val isDarkMode: Boolean = false,
    val followSystem: Boolean = true
)
