package lj.sword.demoappnocompose.app

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import lj.sword.demoappnocompose.BuildConfig
import lj.sword.demoappnocompose.manager.LocaleManager
import javax.inject.Inject

/**
 * Application 基类
 * 集成 Hilt 依赖注入框架
 * 初始化全局配置和第三方库
 * 
 * @author Sword
 * @since 1.0.0
 */
@HiltAndroidApp
class BaseApplication : Application() {

    @Inject
    lateinit var localeManager: LocaleManager

    /** 应用级别的协程作用域 */
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    companion object {
        /**
         * 全局 Application 实例
         */
        @JvmStatic
        lateinit var instance: BaseApplication
            private set

        /**
         * 全局 Context
         */
        @JvmStatic
        val context: Context
            get() = instance.applicationContext
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // 初始化语言设置（必须在其他初始化之前）
        initLanguage()
        
        // 初始化日志系统
        initLogger()
        
        // 初始化全局异常捕获
        initCrashHandler()
        
        // 初始化埋点系统
        initTracker()
        
        // 初始化网络状态监听
        initNetworkMonitor()
    }

    /**
     * 初始化日志系统
     */
    private fun initLogger() {
        lj.sword.demoappnocompose.manager.Logger.isEnabled = BuildConfig.DEBUG
        lj.sword.demoappnocompose.manager.Logger.d("Application initialized")
    }

    /**
     * 初始化全局异常捕获
     */
    private fun initCrashHandler() {
        lj.sword.demoappnocompose.manager.CrashHandler.getInstance().init(this)
    }

    /**
     * 初始化埋点系统
     */
    private fun initTracker() {
        lj.sword.demoappnocompose.manager.TrackManager.init(
            lj.sword.demoappnocompose.manager.DefaultTracker()
        )
    }

    /**
     * 初始化网络状态监听
     */
    private fun initNetworkMonitor() {
        // 可选：监听网络状态变化
        // NetworkUtil.registerNetworkCallback(this, onAvailable = {}, onLost = {})
    }

    /**
     * 初始化语言设置
     */
    private fun initLanguage() {
        applicationScope.launch {
            try {
                localeManager.initializeLanguage()
                lj.sword.demoappnocompose.manager.Logger.d("Language initialized successfully")
            } catch (e: Exception) {
                lj.sword.demoappnocompose.manager.Logger.e("Failed to initialize language", e)
            }
        }
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        // 在 attachBaseContext 中应用语言配置
        // 注意：这里不能使用 Hilt 注入，因为 Hilt 还没有初始化
        // 语言配置会在 onCreate 中通过 LocaleManager 处理
    }
}
