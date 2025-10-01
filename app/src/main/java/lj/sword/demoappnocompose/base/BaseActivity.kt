package lj.sword.demoappnocompose.base

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import lj.sword.demoappnocompose.manager.ThemeManager
import lj.sword.demoappnocompose.manager.LocaleManager
import lj.sword.demoappnocompose.data.model.ThemeConfig
import lj.sword.demoappnocompose.data.model.SupportedLanguage
import lj.sword.demoappnocompose.utils.ContextUtils
import lj.sword.demoappnocompose.R
import javax.inject.Inject



/**
 * 无反射的 ViewBinding 扩展函数
 */
fun <VB : ViewBinding> AppCompatActivity.inflateBinding(
    inflater: (LayoutInflater) -> VB
): VB = inflater(layoutInflater)



/**
 * Activity 基类
 * 提供统一的 ViewBinding、ViewModel 绑定、状态栏管理、Loading 显示、主题管理等功能
 *
 * @param VB ViewBinding 类型
 * @author Sword
 * @since 1.0.0
 */
abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {

    /** ViewBinding 实例 */
    protected lateinit var binding: VB

    /** 子类提供的 bindingInflater，例如 ActivityMainBinding::inflate */
    protected abstract val bindingInflater: (LayoutInflater) -> VB

    /** 可选的 ViewModel */
    protected open val viewModel: BaseViewModel? = null

    /** 是否第一次加载 */
    private var isFirstLoad = true

    /** 主题管理器 - 子类需要注入 */
    @Inject
    internal lateinit var themeManager: ThemeManager

    /** 语言管理器 - 子类需要注入 */
    @Inject
    internal lateinit var localeManager: LocaleManager

    /** 当前主题配置 */
    private var currentThemeConfig: ThemeConfig? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // 在super.onCreate之前应用主题和语言
        applyTheme()
        super.onCreate(savedInstanceState)

        // 使用扩展函数自动创建 ViewBinding（无反射）
        binding = inflateBinding(bindingInflater)
        setContentView(binding.root)

        // 初始化流程
        initStatusBar()
        initView()
        initData()
        setListeners()
        observeViewModel()
        observeThemeChanges()
        observeLanguageChanges()
    }

    /** 初始化状态栏 */
    protected open fun initStatusBar() {
        // 使用主题颜色设置状态栏
        val statusBarColor = getPrimaryColor()
        window.statusBarColor = statusBarColor
        
        // 根据状态栏颜色亮度设置状态栏图标颜色
        val isLightStatusBar = isLightColor(statusBarColor)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = isLightStatusBar
        }
    }

    /**
     * 获取主色
     */
    protected fun getPrimaryColor(): Int {
        return if (::themeManager.isInitialized) {
            themeManager.getThemeColor(this, R.attr.colorPrimary)
        } else {
            ContextCompat.getColor(this, android.R.color.white)
        }
    }

    /**
     * 判断颜色是否为浅色
     */
    private fun isLightColor(color: Int): Boolean {
        val red = (color shr 16) and 0xFF
        val green = (color shr 8) and 0xFF
        val blue = color and 0xFF
        
        // 使用相对亮度公式
        val luminance = (0.299 * red + 0.587 * green + 0.114 * blue) / 255
        return luminance > 0.5
    }

    /** 初始化视图 */
    protected abstract fun initView()

    /** 初始化数据（可选） */
    protected open fun initData() {}

    /** 设置监听器（可选） */
    protected open fun setListeners() {}

    /** 观察 ViewModel 状态 */
    private fun observeViewModel() {
        val vm = viewModel ?: return
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { vm.loading.collect { if (it) showLoading() else hideLoading() } }
                launch { vm.error.collect { it?.let { msg -> showError(msg); vm.clearError() } } }
            }
        }
    }

    /** 显示 Loading */
    protected open fun showLoading() {}

    /** 隐藏 Loading */
    protected open fun hideLoading() {}

    /** 显示错误信息 */
    protected open fun showError(message: String) {}

    /** 懒加载 */
    protected open fun lazyLoad() {}

    override fun onResume() {
        super.onResume()
        if (isFirstLoad) {
            lazyLoad()
            isFirstLoad = false
        }
    }

    /**
     * 应用主题
     */
    private fun applyTheme() {
        if (!::themeManager.isInitialized) {
            return
        }
        
        lifecycleScope.launch {
            themeManager.getCurrentThemeConfig().collect { themeConfig ->
                currentThemeConfig = themeConfig
                
                // 判断是否跟随系统
                val isDarkMode = if (themeConfig.followSystem) {
                    themeManager.isSystemDarkMode(this@BaseActivity)
                } else {
                    themeConfig.isDarkMode
                }
                
                // 应用主题
                val finalThemeConfig = themeConfig.copy(isDarkMode = isDarkMode)
                themeManager.applyTheme(this@BaseActivity, finalThemeConfig)
            }
        }
    }

    /**
     * 观察主题变化
     */
    private fun observeThemeChanges() {
        if (!::themeManager.isInitialized) {
            return
        }
        
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                themeManager.getCurrentThemeConfig().collect { themeConfig ->
                    // 如果主题配置发生变化，重新应用主题
                    if (currentThemeConfig != themeConfig) {
                        recreate()
                    }
                }
            }
        }
    }

    /**
     * 判断系统是否处于暗黑模式
     */
    private fun isSystemDarkMode(): Boolean {
        val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    }

    /**
     * 配置变化监听
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        
        // 检测暗黑模式配置变化
        val isDarkMode = (newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        
        // 如果跟随系统，重新应用主题
        currentThemeConfig?.let { themeConfig ->
            if (themeConfig.followSystem && themeConfig.isDarkMode != isDarkMode) {
                recreate()
            }
        }
    }

    /**
     * 重写 attachBaseContext 以应用语言配置
     */
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase)
        // 语言配置会在 onCreate 中通过 LocaleManager 处理
        // 这里暂时不处理，因为 Hilt 注入在 attachBaseContext 时还不可用
    }

    /**
     * 观察语言变化
     */
    private fun observeLanguageChanges() {
        if (!::localeManager.isInitialized) {
            return
        }
        
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                localeManager.getCurrentLanguageConfig().collect { languageConfig ->
                    // 语言变化时重建 Activity
                    onLocaleChanged(languageConfig.language)
                }
            }
        }
    }

    /**
     * 语言变化回调 - 子类可以重写此方法
     */
    protected open fun onLocaleChanged(language: SupportedLanguage) {
        // 默认行为：重建 Activity
        recreate()
    }

    /**
     * 获取当前语言
     */
    protected suspend fun getCurrentLanguage(): SupportedLanguage {
        return if (::localeManager.isInitialized) {
            localeManager.getCurrentLanguage()
        } else {
            SupportedLanguage.getDefault()
        }
    }

    /**
     * 切换语言
     */
    protected suspend fun switchLanguage(language: SupportedLanguage) {
        if (::localeManager.isInitialized) {
            localeManager.setLocale(language)
        }
    }
}

