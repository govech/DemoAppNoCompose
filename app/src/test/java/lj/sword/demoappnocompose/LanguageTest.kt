package lj.sword.demoappnocompose

import lj.sword.demoappnocompose.data.model.SupportedLanguage
import org.junit.Test
import org.junit.Assert.*

/**
 * 语言功能基本测试
 */
class LanguageTest {

    @Test
    fun testSupportedLanguageValues() {
        // 测试支持的语言枚举
        val languages = SupportedLanguage.values()
        assertTrue("Should have supported languages", languages.isNotEmpty())
        
        // 测试默认语言
        val defaultLanguage = SupportedLanguage.getDefault()
        assertEquals("Default language should be Chinese Simplified", 
            SupportedLanguage.CHINESE_SIMPLIFIED, defaultLanguage)
    }

    @Test
    fun testLanguageCodeMapping() {
        // 测试语言代码映射
        val chineseSimplified = SupportedLanguage.fromCode("zh-CN")
        assertEquals("zh-CN should map to Chinese Simplified", 
            SupportedLanguage.CHINESE_SIMPLIFIED, chineseSimplified)
        
        val english = SupportedLanguage.fromCode("en-US")
        assertEquals("en-US should map to English", 
            SupportedLanguage.ENGLISH, english)
        
        val invalid = SupportedLanguage.fromCode("invalid")
        assertNull("Invalid code should return null", invalid)
    }

    @Test
    fun testLanguageConfig() {
        // 测试语言配置
        val language = SupportedLanguage.JAPANESE
        val config = lj.sword.demoappnocompose.data.model.LanguageConfig(
            language = language,
            isSelected = true
        )
        
        assertEquals("Language code should match", "ja-JP", config.getLanguageCode())
        assertEquals("Display name should match", "日本語", config.getDisplayName())
        assertTrue("Should be selected", config.isSelected)
    }

    @Test
    fun testAllLanguagesList() {
        // 测试所有语言列表
        val allLanguages = SupportedLanguage.getAllLanguages()
        assertTrue("Should have multiple languages", allLanguages.size > 1)
        
        // 验证包含所有支持的语言
        assertTrue("Should contain Chinese Simplified", 
            allLanguages.contains(SupportedLanguage.CHINESE_SIMPLIFIED))
        assertTrue("Should contain English", 
            allLanguages.contains(SupportedLanguage.ENGLISH))
        assertTrue("Should contain Japanese", 
            allLanguages.contains(SupportedLanguage.JAPANESE))
    }
}
