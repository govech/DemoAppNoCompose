package lj.sword.demoappnocompose.base

import android.content.Context
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.launch
import lj.sword.demoappnocompose.manager.ThemeManager
import lj.sword.demoappnocompose.manager.LocaleManager
import lj.sword.demoappnocompose.manager.LanguageChangeBroadcastReceiver
import lj.sword.demoappnocompose.manager.LanguageChangeConstants
import lj.sword.demoappnocompose.data.model.ThemeConfig
import lj.sword.demoappnocompose.data.model.AppTheme
import lj.sword.demoappnocompose.data.model.SupportedLanguage
import lj.sword.demoappnocompose.data.model.LanguageConfig
import lj.sword.demoappnocompose.utils.ContextUtils
import lj.sword.demoappnocompose.R
import java.util.Locale
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

    /** 当前语言配置 */
    private var currentLanguageConfig: LanguageConfig? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // 在super.onCreate之前同步应用主题
        applyThemeSync()
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
        // 启动主题监听
        observeThemeChanges()
        observeLanguageChanges() // 重新启用语言变化监听
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
     * 同步应用主题（在onCreate之前）
     */
    private fun applyThemeSync() {
        try {
            // 检查是否有Intent传递的主题信息（Activity重建时）
            val hasIntentTheme = intent?.hasExtra("theme_id") == true
            
            if (hasIntentTheme) {
                // 从Intent中获取主题信息（Activity重建时）
                val themeId = intent.getStringExtra("theme_id") ?: "default"
                val isDarkMode = intent.getBooleanExtra("is_dark_mode", false)
                val followSystem = intent.getBooleanExtra("follow_system", true)
                
                val theme = AppTheme.fromThemeId(themeId)
                val finalIsDarkMode = if (followSystem && ::themeManager.isInitialized) {
                    themeManager.isSystemDarkMode(this)
                } else {
                    isDarkMode
                }
                
                // 只设置主题样式，夜间模式由Application统一管理
                setTheme(theme.styleRes)
                currentThemeConfig = ThemeConfig(theme, finalIsDarkMode, followSystem)
                
                android.util.Log.d("BaseActivity", "Applying theme from Intent: ${theme.themeName}, isDark: $finalIsDarkMode, followSystem: $followSystem")
            } else {
                // 首次启动，使用默认主题，让observeThemeChanges处理真实的DataStore数据
                setTheme(R.style.DefaultTheme)
                android.util.Log.d("BaseActivity", "First launch, using default theme, will be updated by DataStore")
            }
            
        } catch (e: Exception) {
            android.util.Log.e("BaseActivity", "Failed to apply theme sync", e)
            setTheme(R.style.DefaultTheme)
        }
    }

    /**
     * 观察主题变化并重建Activity
     */
    private fun observeThemeChanges() {
        if (!::themeManager.isInitialized) {
            return
        }
        
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                themeManager.getCurrentThemeConfig().collect { themeConfig ->
                    // 判断最终的暗黑模式状态
                    val finalIsDarkMode = if (themeConfig.followSystem) {
                        themeManager.isSystemDarkMode(this@BaseActivity)
                    } else {
                        themeConfig.isDarkMode
                    }
                    
                    val finalThemeConfig = themeConfig.copy(isDarkMode = finalIsDarkMode)
                    
                    // 检查是否需要应用主题
                    val needsThemeChange = currentThemeConfig == null || currentThemeConfig != finalThemeConfig
                    
                    if (needsThemeChange) {
                        android.util.Log.d("BaseActivity", "Theme config changed: ${finalThemeConfig.currentTheme.themeName}, dark: ${finalThemeConfig.isDarkMode}, followSystem: ${finalThemeConfig.followSystem}")
                        
                        val isFirstTime = currentThemeConfig == null
                        val oldThemeConfig = currentThemeConfig
                        
                        // 更新当前主题配置
                        currentThemeConfig = finalThemeConfig
                        
                        // 检查是否只是夜间模式变化
                        val onlyNightModeChanged = oldThemeConfig != null && 
                            oldThemeConfig.currentTheme == finalThemeConfig.currentTheme &&
                            (oldThemeConfig.isDarkMode != finalThemeConfig.isDarkMode || 
                             oldThemeConfig.followSystem != finalThemeConfig.followSystem)
                        
                        if (onlyNightModeChanged) {
                            // 只是夜间模式变化，Application会处理夜间模式，Activity不需要重建
                            android.util.Log.d("BaseActivity", "Only night mode changed, no recreation needed")
                        } else if (isFirstTime) {
                            // 首次加载，检查是否需要重建
                            val isDefaultTheme = finalThemeConfig.currentTheme == AppTheme.DEFAULT
                            
                            if (isDefaultTheme) {
                                // 是默认主题，不需要重建
                                android.util.Log.d("BaseActivity", "First time with default theme, no recreation needed")
                            } else {
                                // 不是默认主题，需要重建
                                android.util.Log.d("BaseActivity", "First time with non-default theme, recreating")
                                lifecycleScope.launch {
                                    kotlinx.coroutines.delay(100)
                                    recreateWithTheme(finalThemeConfig)
                                }
                            }
                        } else {
                            // 主题变化，需要重建
                            android.util.Log.d("BaseActivity", "Theme changed, recreating with theme")
                            lifecycleScope.launch {
                                kotlinx.coroutines.delay(100)
                                recreateWithTheme(finalThemeConfig)
                            }
                        }
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
        // 应用语言配置到Context
        val contextWithLanguage = applyLanguageToContext(newBase)
        super.attachBaseContext(contextWithLanguage)
    }

    /**
     * 应用语言配置到Context
     */
    private fun applyLanguageToContext(context: Context): Context {
        return try {
            // 由于Hilt注入在attachBaseContext时不可用，我们需要直接读取语言设置
            val sharedPrefs = context.getSharedPreferences("language_prefs", Context.MODE_PRIVATE)
            val languageCode = sharedPrefs.getString("current_language", "zh") ?: "zh"
            
            // 创建对应的Locale
            val locale = createLocaleFromLanguageCode(languageCode)
            
            // 应用语言到Context
            ContextUtils.run { context.wrap(locale) }
        } catch (e: Exception) {
            e.printStackTrace()
            context
        }
    }

    /**
     * 根据语言代码创建Locale（与LocaleManager中的逻辑保持一致）
     */
    private fun createLocaleFromLanguageCode(code: String): Locale {
        return when (code) {
            "zh" -> Locale("zh", "CN")
            "zh-rTW" -> Locale("zh", "TW")
            "en" -> Locale("en", "US")
            "ja" -> Locale("ja", "JP")
            "ko" -> Locale("ko", "KR")
            else -> {
                val parts = code.split("-")
                if (parts.size >= 2) {
                    val country = parts[1].removePrefix("r")
                    Locale(parts[0], country)
                } else {
                    Locale(parts[0])
                }
            }
        }
    }

    /** 语言变化广播接收器 */
    private var languageChangeReceiver: LanguageChangeBroadcastReceiver? = null

    /**
     * 观察语言变化
     */
    private fun observeLanguageChanges() {
        if (!::localeManager.isInitialized) {
            android.util.Log.d("BaseActivity", "LocaleManager not initialized in ${this::class.simpleName}")
            return
        }
        
        android.util.Log.d("BaseActivity", "Setting up language change observer in ${this::class.simpleName}")
        
        // 初始化当前语言配置
        lifecycleScope.launch {
            val initialLanguage = localeManager.getCurrentLanguage()
            currentLanguageConfig = LanguageConfig(language = initialLanguage, isSelected = true)
            android.util.Log.d("BaseActivity", "Initial language: ${initialLanguage.code}")
        }
        
        // 注册语言变化广播接收器
        languageChangeReceiver = LanguageChangeBroadcastReceiver { newLanguage ->
            android.util.Log.d("BaseActivity", "Received language change broadcast: ${newLanguage.code} in ${this::class.simpleName}")
            val newLanguageConfig = LanguageConfig(language = newLanguage, isSelected = true)
            if (currentLanguageConfig != newLanguageConfig) {
                currentLanguageConfig = newLanguageConfig
                onLocaleChanged(newLanguage)
            }
        }
        
        val intentFilter = IntentFilter(LanguageChangeConstants.ACTION_LOCALE_CHANGED)
        LocalBroadcastManager.getInstance(this).registerReceiver(
            languageChangeReceiver!!,
            intentFilter
        )
        android.util.Log.d("BaseActivity", "Language change receiver registered in ${this::class.simpleName}")
    }

    /**
     * 语言变化回调 - 子类可以重写此方法
     */
    protected open fun onLocaleChanged(language: SupportedLanguage) {
        android.util.Log.d("BaseActivity", "onLocaleChanged called with ${language.code} in ${this::class.simpleName}")
        // 默认行为：重建 Activity 并添加动画效果
        recreateWithAnimation()
    }
    
    /**
     * 带主题信息的Activity重建
     */
    private fun recreateWithTheme(themeConfig: ThemeConfig) {
        // 将主题信息保存到Intent中，以便重建后的Activity能够同步获取
        intent.putExtra("theme_id", themeConfig.currentTheme.themeId)
        intent.putExtra("is_dark_mode", themeConfig.isDarkMode)
        intent.putExtra("follow_system", themeConfig.followSystem)
        
        // 重建Activity
        recreate()
    }

    /**
     * 带动画的Activity重建
     */
    private fun recreateWithAnimation() {
        // 设置退出动画
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        // 重建Activity
        recreate()
        // 设置进入动画
        overridePendingTransition(lj.sword.demoappnocompose.R.anim.fade_in, lj.sword.demoappnocompose.R.anim.fade_out)
    }

    override fun onDestroy() {
        super.onDestroy()
        // 注销语言变化广播接收器
        languageChangeReceiver?.let { receiver ->
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
        }
        languageChangeReceiver = null
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

