package lj.sword.demoappnocompose.app

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp
import lj.sword.demoappnocompose.BuildConfig
import lj.sword.demoappnocompose.config.AppConfig
import lj.sword.demoappnocompose.config.AppConfigProvider
import lj.sword.demoappnocompose.memory.MemoryManager
import lj.sword.demoappnocompose.monitor.PerformanceMonitor
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
    lateinit var performanceMonitor: PerformanceMonitor

    @Inject
    lateinit var appConfigProvider: AppConfigProvider

    @Inject
    lateinit var memoryManager: MemoryManager

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

        // 初始化应用配置
        initAppConfig()

        // 初始化性能监控
        initPerformanceMonitor()

        // 初始化内存管理
        initMemoryManager()

        // 初始化日志系统
        initLogger()

        // 初始化全局异常捕获
        initCrashHandler()

        // 初始化埋点系统
        initTracker()

        // 初始化网络状态监听
        initNetworkMonitor()

        // 标记Application创建完成
        if (::performanceMonitor.isInitialized) {
            performanceMonitor.markApplicationCreated()
        }
    }

    /**
     * 初始化应用配置
     */
    private fun initAppConfig() {
        val config =
            AppConfig.init(this) {
                // 网络配置
                network {
                    baseUrl = BuildConfig.BASE_URL
                    connectTimeout = 30
                    readTimeout = 30
                    writeTimeout = 30
                    enableNetworkLog = BuildConfig.DEBUG
                    maxRetryCount = 3
                }

                // UI配置
                ui {
                    enableDarkMode = false
                    enableAnimation = true
                    animationDuration = 300
                }

                // 缓存配置
                cache {
                    memoryCacheSize = 50
                    diskCacheSize = 200
                    cacheExpireTime = 24
                    enableCache = true
                }

                // 日志配置
                log {
                    enableLog = BuildConfig.DEBUG
                    logLevel = if (BuildConfig.DEBUG) AppConfig.LogLevel.DEBUG else AppConfig.LogLevel.ERROR
                    saveLogToFile = false
                    maxLogFileSize = 10
                }

                // 数据库配置
                database {
                    databaseName = "demo_app_database"
                    databaseVersion = 1
                    enableWalMode = true
                }

                // 性能配置
                performance {
                    enablePerformanceMonitor = BuildConfig.DEBUG
                    startupTimeout = 10000
                    memoryWarningThreshold = 100
                    enableCrashCollection = !BuildConfig.DEBUG
                }

                // 安全配置
                security {
                    enableCertificatePinning = !BuildConfig.DEBUG
                    enableObfuscation = !BuildConfig.DEBUG
                    encryptionKey = "demo_app_encryption_key_2024"
                }
            }

        // 更新AppConfigProvider中的配置
        if (::appConfigProvider.isInitialized) {
            appConfigProvider.setConfig(config)
        }
    }

    /**
     * 初始化日志系统
     */
    private fun initLogger() {
        val config = AppConfig.getInstance()
        lj.sword.demoappnocompose.manager.Logger
            .init(config)
        lj.sword.demoappnocompose.manager.Logger
            .d("Application initialized with config")
    }

    /**
     * 初始化全局异常捕获
     */
    private fun initCrashHandler() {
        lj.sword.demoappnocompose.manager.CrashHandler
            .getInstance()
            .init(this)
    }

    /**
     * 初始化埋点系统
     */
    private fun initTracker() {
        lj.sword.demoappnocompose.manager.TrackManager.init(
            lj.sword.demoappnocompose.manager
                .DefaultTracker(),
        )
    }

    /**
     * 初始化性能监控
     */
    private fun initPerformanceMonitor() {
        if (::performanceMonitor.isInitialized) {
            performanceMonitor.initialize(this)
        }
    }

    /**
     * 初始化内存管理
     */
    private fun initMemoryManager() {
        if (::memoryManager.isInitialized) {
            memoryManager.initialize(this)
        }
    }

    /**
     * 初始化网络状态监听
     */
    private fun initNetworkMonitor() {
        // 可选：监听网络状态变化
        // NetworkUtil.registerNetworkCallback(this, onAvailable = {}, onLost = {})
    }
}
