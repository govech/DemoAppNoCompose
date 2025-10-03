package lj.sword.demoappnocompose.config

import android.content.Context
import lj.sword.demoappnocompose.BuildConfig
import javax.inject.Singleton

/**
 * 应用配置管理类
 * 统一管理应用的所有配置信息
 *
 * @author Sword
 * @since 1.0.0
 */
@Singleton
class AppConfig private constructor(
    private val context: Context,
    private val builder: Builder,
) {
    companion object {
        @Suppress("ktlint:standard:property-naming")
        @Volatile
        private var INSTANCE: AppConfig? = null

        /**
         * 获取配置实例
         */
        fun getInstance(): AppConfig = INSTANCE ?: throw IllegalStateException("AppConfig未初始化，请先调用init()方法")

        /**
         * 初始化配置
         */
        fun init(
            context: Context,
            block: Builder.() -> Unit = {},
        ): AppConfig =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Builder(context).apply(block).build().also { INSTANCE = it }
            }
    }

    // ==================== 网络配置 ====================

    /**
     * 基础URL
     */
    val baseUrl: String = builder.baseUrl

    /**
     * 连接超时时间（秒）
     */
    val connectTimeout: Long = builder.connectTimeout

    /**
     * 读取超时时间（秒）
     */
    val readTimeout: Long = builder.readTimeout

    /**
     * 写入超时时间（秒）
     */
    val writeTimeout: Long = builder.writeTimeout

    /**
     * 是否启用网络日志
     */
    val enableNetworkLog: Boolean = builder.enableNetworkLog

    /**
     * 最大重试次数
     */
    val maxRetryCount: Int = builder.maxRetryCount

    // ==================== UI配置 ====================

    /**
     * 是否启用暗黑模式
     */
    val enableDarkMode: Boolean = builder.enableDarkMode

    /**
     * 主题色
     */
    val primaryColor: Int = builder.primaryColor

    /**
     * 强调色
     */
    val accentColor: Int = builder.accentColor

    /**
     * 默认字体大小
     */
    val defaultFontSize: Float = builder.defaultFontSize

    /**
     * 是否启用动画
     */
    val enableAnimation: Boolean = builder.enableAnimation

    /**
     * 动画持续时间（毫秒）
     */
    val animationDuration: Long = builder.animationDuration

    // ==================== 缓存配置 ====================

    /**
     * 内存缓存大小（MB）
     */
    val memoryCacheSize: Int = builder.memoryCacheSize

    /**
     * 磁盘缓存大小（MB）
     */
    val diskCacheSize: Int = builder.diskCacheSize

    /**
     * 缓存过期时间（小时）
     */
    val cacheExpireTime: Long = builder.cacheExpireTime

    /**
     * 是否启用缓存
     */
    val enableCache: Boolean = builder.enableCache

    // ==================== 日志配置 ====================

    /**
     * 是否启用日志
     */
    val enableLog: Boolean = builder.enableLog

    /**
     * 日志级别
     */
    val logLevel: LogLevel = builder.logLevel

    /**
     * 是否保存日志到文件
     */
    val saveLogToFile: Boolean = builder.saveLogToFile

    /**
     * 日志文件最大大小（MB）
     */
    val maxLogFileSize: Int = builder.maxLogFileSize

    // ==================== 数据库配置 ====================

    /**
     * 数据库名称
     */
    val databaseName: String = builder.databaseName

    /**
     * 数据库版本
     */
    val databaseVersion: Int = builder.databaseVersion

    /**
     * 是否启用WAL模式
     */
    val enableWalMode: Boolean = builder.enableWalMode

    /**
     * 数据库页面大小
     */
    val databasePageSize: Int = builder.databasePageSize

    // ==================== 性能配置 ====================

    /**
     * 是否启用性能监控
     */
    val enablePerformanceMonitor: Boolean = builder.enablePerformanceMonitor

    /**
     * 启动超时时间（毫秒）
     */
    val startupTimeout: Long = builder.startupTimeout

    /**
     * 内存警告阈值（MB）
     */
    val memoryWarningThreshold: Int = builder.memoryWarningThreshold

    /**
     * 是否启用崩溃收集
     */
    val enableCrashCollection: Boolean = builder.enableCrashCollection

    // ==================== 安全配置 ====================

    /**
     * 是否启用证书固定
     */
    val enableCertificatePinning: Boolean = builder.enableCertificatePinning

    /**
     * 是否启用混淆
     */
    val enableObfuscation: Boolean = builder.enableObfuscation

    /**
     * 加密密钥
     */
    val encryptionKey: String = builder.encryptionKey

    /**
     * 配置构建器
     */
    class Builder(
        private val context: Context,
    ) {
        // 网络配置
        var baseUrl: String = "https://api.example.com/"
        var connectTimeout: Long = 30
        var readTimeout: Long = 30
        var writeTimeout: Long = 30
        var enableNetworkLog: Boolean = BuildConfig.DEBUG
        var maxRetryCount: Int = 3

        // UI配置
        var enableDarkMode: Boolean = false
        var primaryColor: Int = 0xFF2196F3.toInt()
        var accentColor: Int = 0xFF03DAC5.toInt()
        var defaultFontSize: Float = 14f
        var enableAnimation: Boolean = true
        var animationDuration: Long = 300

        // 缓存配置
        var memoryCacheSize: Int = 50 // MB
        var diskCacheSize: Int = 200 // MB
        var cacheExpireTime: Long = 24 // 小时
        var enableCache: Boolean = true

        // 日志配置
        var enableLog: Boolean = BuildConfig.DEBUG
        var logLevel: LogLevel = if (BuildConfig.DEBUG) LogLevel.DEBUG else LogLevel.ERROR
        var saveLogToFile: Boolean = false
        var maxLogFileSize: Int = 10 // MB

        // 数据库配置
        var databaseName: String = "app_database"
        var databaseVersion: Int = 1
        var enableWalMode: Boolean = true
        var databasePageSize: Int = 4096

        // 性能配置
        var enablePerformanceMonitor: Boolean = BuildConfig.DEBUG
        var startupTimeout: Long = 10000 // 10秒
        var memoryWarningThreshold: Int = 100 // MB
        var enableCrashCollection: Boolean = !BuildConfig.DEBUG

        // 安全配置
        var enableCertificatePinning: Boolean = !BuildConfig.DEBUG
        var enableObfuscation: Boolean = !BuildConfig.DEBUG
        var encryptionKey: String = "default_key_change_in_production"

        /**
         * 设置网络配置
         */
        fun network(block: NetworkConfig.() -> Unit): Builder {
            val config = NetworkConfig()
            config.block()
            baseUrl = config.baseUrl
            connectTimeout = config.connectTimeout
            readTimeout = config.readTimeout
            writeTimeout = config.writeTimeout
            enableNetworkLog = config.enableNetworkLog
            maxRetryCount = config.maxRetryCount
            return this
        }

        /**
         * 设置UI配置
         */
        fun ui(block: UiConfig.() -> Unit): Builder {
            val config = UiConfig()
            config.block()
            enableDarkMode = config.enableDarkMode
            primaryColor = config.primaryColor
            accentColor = config.accentColor
            defaultFontSize = config.defaultFontSize
            enableAnimation = config.enableAnimation
            animationDuration = config.animationDuration
            return this
        }

        /**
         * 设置缓存配置
         */
        fun cache(block: CacheConfig.() -> Unit): Builder {
            val config = CacheConfig()
            config.block()
            memoryCacheSize = config.memoryCacheSize
            diskCacheSize = config.diskCacheSize
            cacheExpireTime = config.cacheExpireTime
            enableCache = config.enableCache
            return this
        }

        /**
         * 设置日志配置
         */
        fun log(block: LogConfig.() -> Unit): Builder {
            val config = LogConfig()
            config.block()
            enableLog = config.enableLog
            logLevel = config.logLevel
            saveLogToFile = config.saveLogToFile
            maxLogFileSize = config.maxLogFileSize
            return this
        }

        /**
         * 设置数据库配置
         */
        fun database(block: DatabaseConfig.() -> Unit): Builder {
            val config = DatabaseConfig()
            config.block()
            databaseName = config.databaseName
            databaseVersion = config.databaseVersion
            enableWalMode = config.enableWalMode
            databasePageSize = config.databasePageSize
            return this
        }

        /**
         * 设置性能配置
         */
        fun performance(block: PerformanceConfig.() -> Unit): Builder {
            val config = PerformanceConfig()
            config.block()
            enablePerformanceMonitor = config.enablePerformanceMonitor
            startupTimeout = config.startupTimeout
            memoryWarningThreshold = config.memoryWarningThreshold
            enableCrashCollection = config.enableCrashCollection
            return this
        }

        /**
         * 设置安全配置
         */
        fun security(block: SecurityConfig.() -> Unit): Builder {
            val config = SecurityConfig()
            config.block()
            enableCertificatePinning = config.enableCertificatePinning
            enableObfuscation = config.enableObfuscation
            encryptionKey = config.encryptionKey
            return this
        }

        /**
         * 构建配置
         */
        fun build(): AppConfig = AppConfig(context, this)
    }

    /**
     * 网络配置
     */
    data class NetworkConfig(
        var baseUrl: String = "https://api.example.com/",
        var connectTimeout: Long = 30,
        var readTimeout: Long = 30,
        var writeTimeout: Long = 30,
        var enableNetworkLog: Boolean = BuildConfig.DEBUG,
        var maxRetryCount: Int = 3,
    )

    /**
     * UI配置
     */
    data class UiConfig(
        var enableDarkMode: Boolean = false,
        var primaryColor: Int = 0xFF2196F3.toInt(),
        var accentColor: Int = 0xFF03DAC5.toInt(),
        var defaultFontSize: Float = 14f,
        var enableAnimation: Boolean = true,
        var animationDuration: Long = 300,
    )

    /**
     * 缓存配置
     */
    data class CacheConfig(
        var memoryCacheSize: Int = 50,
        var diskCacheSize: Int = 200,
        var cacheExpireTime: Long = 24,
        var enableCache: Boolean = true,
    )

    /**
     * 日志配置
     */
    data class LogConfig(
        var enableLog: Boolean = BuildConfig.DEBUG,
        var logLevel: LogLevel = if (BuildConfig.DEBUG) LogLevel.DEBUG else LogLevel.ERROR,
        var saveLogToFile: Boolean = false,
        var maxLogFileSize: Int = 10,
    )

    /**
     * 数据库配置
     */
    data class DatabaseConfig(
        var databaseName: String = "app_database",
        var databaseVersion: Int = 1,
        var enableWalMode: Boolean = true,
        var databasePageSize: Int = 4096,
    )

    /**
     * 性能配置
     */
    data class PerformanceConfig(
        var enablePerformanceMonitor: Boolean = BuildConfig.DEBUG,
        var startupTimeout: Long = 10000,
        var memoryWarningThreshold: Int = 100,
        var enableCrashCollection: Boolean = !BuildConfig.DEBUG,
    )

    /**
     * 安全配置
     */
    data class SecurityConfig(
        var enableCertificatePinning: Boolean = !BuildConfig.DEBUG,
        var enableObfuscation: Boolean = !BuildConfig.DEBUG,
        var encryptionKey: String = "default_key_change_in_production",
    )

    /**
     * 日志级别
     */
    enum class LogLevel {
        VERBOSE,
        DEBUG,
        INFO,
        WARN,
        ERROR,
    }
}
