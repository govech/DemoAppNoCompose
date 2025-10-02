package lj.sword.demoappnocompose.ui.settings

import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import lj.sword.demoappnocompose.R
import lj.sword.demoappnocompose.base.BaseActivity
import lj.sword.demoappnocompose.data.model.AppTheme
import lj.sword.demoappnocompose.data.model.ThemeConfig
import lj.sword.demoappnocompose.databinding.ActivityThemeSettingsBinding
import lj.sword.demoappnocompose.manager.ThemeManager
import javax.inject.Inject

/**
 * 主题设置页面
 */
@AndroidEntryPoint
class ThemeSettingsActivity : BaseActivity<ActivityThemeSettingsBinding>() {

    override val bindingInflater: (LayoutInflater) -> ActivityThemeSettingsBinding = ActivityThemeSettingsBinding::inflate

    private val settingsViewModel: ThemeSettingsViewModel by viewModels()

    @Inject
    lateinit var injectedThemeManager: ThemeManager

    // 添加标志位防止无限循环
    private var isUpdatingUI = false

    override fun initView() {
        setupToolbar()
        setupThemeSelection()
        setupDarkModeSwitch()
        setupFollowSystemSwitch()
    }

    override fun initData() {
        observeThemeConfig()
    }

    private fun setupToolbar() {
        binding.titleBar.tvTitle.text = getString(R.string.settings_theme)
        binding.titleBar.apply {
            this.ivBack.setOnClickListener { finish() }
        }
        binding.titleBar.apply {
            setTitle("主题设置")
            // TitleBar默认就有返回按钮的点击事件，会调用finish()
        }
    }

    private fun setupThemeSelection() {
        binding.radioGroupTheme.setOnCheckedChangeListener { _, checkedId ->
            // 如果正在更新UI，忽略这次事件
            if (isUpdatingUI) {
                android.util.Log.d("ThemeSettings", "Ignoring theme selection change (updating UI)")
                return@setOnCheckedChangeListener
            }
            
            val theme = when (checkedId) {
                R.id.rbDefault -> AppTheme.DEFAULT
                R.id.rbBusiness -> AppTheme.BUSINESS
                R.id.rbVibrant -> AppTheme.VIBRANT
                else -> AppTheme.DEFAULT
            }
            android.util.Log.d("ThemeSettings", "User selected theme: ${theme.themeName}")
            lifecycleScope.launch {
                settingsViewModel.switchTheme(theme)
            }
        }
    }

    private fun setupDarkModeSwitch() {
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            // 如果正在更新UI，忽略这次事件
            if (isUpdatingUI) return@setOnCheckedChangeListener
            
            lifecycleScope.launch {
                settingsViewModel.switchDarkMode(isChecked)
            }
        }
    }

    private fun setupFollowSystemSwitch() {
        binding.switchFollowSystem.setOnCheckedChangeListener { _, isChecked ->
            // 如果正在更新UI，忽略这次事件
            if (isUpdatingUI) return@setOnCheckedChangeListener
            
            lifecycleScope.launch {
                settingsViewModel.setFollowSystem(isChecked)
            }
        }
    }

    private fun observeThemeConfig() {
        lifecycleScope.launch {
            settingsViewModel.themeConfig.collect { themeConfig ->
                updateUI(themeConfig)
            }
        }
    }

    /**
     * 统一更新UI，使用标志位防止无限循环
     */
    private fun updateUI(themeConfig: ThemeConfig) {
        android.util.Log.d("ThemeSettings", "Updating UI: theme=${themeConfig.currentTheme.themeName}, dark=${themeConfig.isDarkMode}, followSystem=${themeConfig.followSystem}")
        isUpdatingUI = true
        try {
            updateThemeSelection(themeConfig.currentTheme)
            updateDarkModeSwitch(themeConfig.isDarkMode)
            updateFollowSystemSwitch(themeConfig.followSystem)
        } finally {
            isUpdatingUI = false
        }
    }

    private fun updateThemeSelection(theme: AppTheme) {
        val radioButtonId = when (theme) {
            AppTheme.DEFAULT -> R.id.rbDefault
            AppTheme.BUSINESS -> R.id.rbBusiness
            AppTheme.VIBRANT -> R.id.rbVibrant
        }
        binding.radioGroupTheme.check(radioButtonId)
    }

    private fun updateDarkModeSwitch(isDarkMode: Boolean) {
        binding.switchDarkMode.isChecked = isDarkMode
    }

    private fun updateFollowSystemSwitch(followSystem: Boolean) {
        binding.switchFollowSystem.isChecked = followSystem
        // 当跟随系统时，禁用暗黑模式开关
        binding.switchDarkMode.isEnabled = !followSystem
    }
}
