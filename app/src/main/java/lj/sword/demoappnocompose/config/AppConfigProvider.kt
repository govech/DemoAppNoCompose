package lj.sword.demoappnocompose.config

import android.content.Context
import lj.sword.demoappnocompose.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 应用配置提供者
 * 解决依赖注入时的初始化顺序问题
 *
 * @author Sword
 * @since 1.0.0
 */
@Singleton
class AppConfigProvider
    @Inject
    constructor(
        private val context: Context,
    ) {
        @Volatile
        private var config: AppConfig? = null

        /**
         * 获取配置实例
         * 如果未初始化则使用默认配置初始化
         */
        fun getConfig(): AppConfig {
            return config ?: synchronized(this) {
                config ?: initDefaultConfig().also { config = it }
            }
        }

        /**
         * 设置配置实例
         * 由Application调用
         */
        fun setConfig(appConfig: AppConfig) {
            synchronized(this) {
                config = appConfig
            }
        }

        /**
         * 初始化默认配置
         */
        private fun initDefaultConfig(): AppConfig {
            return AppConfig.init(context) {
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
        }
    }
