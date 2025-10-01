package lj.sword.demoappnocompose.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.FragmentActivity
import lj.sword.demoappnocompose.data.model.LanguageConfig
import lj.sword.demoappnocompose.data.model.SupportedLanguage
import lj.sword.demoappnocompose.databinding.WidgetLanguageSwitcherBinding
import lj.sword.demoappnocompose.ui.dialog.LanguageSelectionDialog

/**
 * 语言切换按钮组件
 * 可以在 Toolbar 或设置页面中使用
 * 
 * @author Sword
 * @since 1.0.0
 */
class LanguageSwitcherView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: WidgetLanguageSwitcherBinding
    private var currentLanguage: LanguageConfig? = null
    private var onLanguageChanged: ((LanguageConfig) -> Unit)? = null

    init {
        binding = WidgetLanguageSwitcherBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
        
        setupClick()
    }

    private fun setupClick() {
        binding.root.setOnClickListener {
            showLanguageSelectionDialog()
        }
    }

    /**
     * 设置当前语言
     */
    fun setCurrentLanguage(languageConfig: LanguageConfig) {
        currentLanguage = languageConfig
        updateDisplay()
    }

    /**
     * 更新显示
     */
    private fun updateDisplay() {
        currentLanguage?.let { language ->
            // 设置语言图标
            binding.ivLanguageIcon.setImageResource(language.getIconRes())
            
            // 设置语言简称
            binding.tvLanguageCode.text = getLanguageShortName(language.language)
            
            // 设置语言全名（可选，用于 Tooltip）
            binding.root.contentDescription = language.getDisplayName()
        }
    }

    /**
     * 获取语言简称
     */
    private fun getLanguageShortName(language: SupportedLanguage): String {
        return when (language) {
            SupportedLanguage.CHINESE_SIMPLIFIED -> "中文"
            SupportedLanguage.CHINESE_TRADITIONAL -> "繁中"
            SupportedLanguage.ENGLISH -> "EN"
            SupportedLanguage.JAPANESE -> "日"
            SupportedLanguage.KOREAN -> "한"
        }
    }

    /**
     * 显示语言选择对话框
     */
    private fun showLanguageSelectionDialog() {
        val activity = context as? FragmentActivity ?: return
        
        val dialog = LanguageSelectionDialog.newInstance(currentLanguage)
        dialog.setOnLanguageSelectedListener { selectedLanguage ->
            onLanguageChanged?.invoke(selectedLanguage)
        }
        
        dialog.show(activity.supportFragmentManager, "LanguageSelectionDialog")
    }

    /**
     * 设置语言变化回调
     */
    fun setOnLanguageChangedListener(listener: (LanguageConfig) -> Unit) {
        onLanguageChanged = listener
    }

    /**
     * 获取当前语言
     */
    fun getCurrentLanguage(): LanguageConfig? = currentLanguage
}
