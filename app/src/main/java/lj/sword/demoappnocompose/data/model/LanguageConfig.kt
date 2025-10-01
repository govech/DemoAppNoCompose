package lj.sword.demoappnocompose.data.model

import lj.sword.demoappnocompose.R

/**
 * 支持的语言枚举
 */
enum class SupportedLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String,
    val iconRes: Int
) {
    CHINESE_SIMPLIFIED(
        code = "zh-CN",
        displayName = "简体中文",
        nativeName = "简体中文",
        iconRes = R.drawable.ic_language_chinese
    ),
    CHINESE_TRADITIONAL(
        code = "zh-TW",
        displayName = "繁体中文",
        nativeName = "繁體中文",
        iconRes = R.drawable.ic_language_chinese
    ),
    ENGLISH(
        code = "en-US",
        displayName = "English",
        nativeName = "English",
        iconRes = R.drawable.ic_language_english
    ),
    JAPANESE(
        code = "ja-JP",
        displayName = "日本語",
        nativeName = "日本語",
        iconRes = R.drawable.ic_language_japanese
    ),
    KOREAN(
        code = "ko-KR",
        displayName = "한국어",
        nativeName = "한국어",
        iconRes = R.drawable.ic_language_korean
    );

    companion object {
        /**
         * 根据语言代码获取支持的语言
         */
        fun fromCode(code: String): SupportedLanguage? {
            return values().find { it.code == code }
        }

        /**
         * 获取所有可用的语言列表
         */
        fun getAllLanguages(): List<SupportedLanguage> {
            return values().toList()
        }

        /**
         * 获取默认语言（简体中文）
         */
        fun getDefault(): SupportedLanguage = CHINESE_SIMPLIFIED
    }
}

/**
 * 语言配置数据类
 */
data class LanguageConfig(
    val language: SupportedLanguage = SupportedLanguage.getDefault(),
    val isSelected: Boolean = false
) : java.io.Serializable {
    /**
     * 获取语言代码
     */
    fun getLanguageCode(): String = language.code

    /**
     * 获取显示名称
     */
    fun getDisplayName(): String = language.displayName

    /**
     * 获取原生名称
     */
    fun getNativeName(): String = language.nativeName

    /**
     * 获取图标资源ID
     */
    fun getIconRes(): Int = language.iconRes

    /**
     * 创建选中状态的副本
     */
    fun copyWithSelected(selected: Boolean): LanguageConfig {
        return copy(isSelected = selected)
    }
}
