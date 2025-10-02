package lj.sword.demoappnocompose.config

import android.content.Context
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat

/**
 * 配置扩展工具类
 * 提供便捷的配置访问方法
 * 
 * @author Sword
 * @since 1.0.0
 */

/**
 * 获取应用配置
 */
fun Context.getAppConfig(): AppConfig = AppConfig.getInstance()

/**
 * 获取主题色
 */
fun Context.getPrimaryColor(): Int {
    return getAppConfig().primaryColor
}

/**
 * 获取强调色
 */
fun Context.getAccentColor(): Int {
    return getAppConfig().accentColor
}

/**
 * 获取默认字体大小
 */
fun Context.getDefaultFontSize(): Float {
    return getAppConfig().defaultFontSize
}

/**
 * 是否启用动画
 */
fun Context.isAnimationEnabled(): Boolean {
    return getAppConfig().enableAnimation
}

/**
 * 获取动画持续时间
 */
fun Context.getAnimationDuration(): Long {
    return getAppConfig().animationDuration
}

/**
 * 是否启用暗黑模式
 */
fun Context.isDarkModeEnabled(): Boolean {
    return getAppConfig().enableDarkMode
}

/**
 * 是否启用缓存
 */
fun Context.isCacheEnabled(): Boolean {
    return getAppConfig().enableCache
}

/**
 * 获取内存缓存大小（字节）
 */
fun Context.getMemoryCacheSize(): Long {
    return getAppConfig().memoryCacheSize * 1024 * 1024L
}

/**
 * 获取磁盘缓存大小（字节）
 */
fun Context.getDiskCacheSize(): Long {
    return getAppConfig().diskCacheSize * 1024 * 1024L
}

/**
 * 是否启用性能监控
 */
fun Context.isPerformanceMonitorEnabled(): Boolean {
    return getAppConfig().enablePerformanceMonitor
}

/**
 * 获取内存警告阈值（字节）
 */
fun Context.getMemoryWarningThreshold(): Long {
    return getAppConfig().memoryWarningThreshold * 1024 * 1024L
}

/**
 * 是否启用崩溃收集
 */
fun Context.isCrashCollectionEnabled(): Boolean {
    return getAppConfig().enableCrashCollection
}

/**
 * 配置构建器扩展
 */
object ConfigBuilder {
    
    /**
     * 开发环境配置
     */
    fun development(context: Context): AppConfig {
        return AppConfig.init(context) {
            network {
                baseUrl = "https://dev-api.example.com/"
                connectTimeout = 30
                readTimeout = 30
                writeTimeout = 30
                enableNetworkLog = true
                maxRetryCount = 5
            }
            
            log {
                enableLog = true
                logLevel = AppConfig.LogLevel.DEBUG
                saveLogToFile = true
                maxLogFileSize = 50
            }
            
            performance {
                enablePerformanceMonitor = true
                enableCrashCollection = false
            }
            
            security {
                enableCertificatePinning = false
                enableObfuscation = false
            }
        }
    }
    
    /**
     * 测试环境配置
     */
    fun testing(context: Context): AppConfig {
        return AppConfig.init(context) {
            network {
                baseUrl = "https://test-api.example.com/"
                connectTimeout = 20
                readTimeout = 20
                writeTimeout = 20
                enableNetworkLog = true
                maxRetryCount = 3
            }
            
            log {
                enableLog = true
                logLevel = AppConfig.LogLevel.INFO
                saveLogToFile = true
                maxLogFileSize = 20
            }
            
            performance {
                enablePerformanceMonitor = true
                enableCrashCollection = true
            }
            
            security {
                enableCertificatePinning = true
                enableObfuscation = false
            }
        }
    }
    
    /**
     * 生产环境配置
     */
    fun production(context: Context): AppConfig {
        return AppConfig.init(context) {
            network {
                baseUrl = "https://api.example.com/"
                connectTimeout = 15
                readTimeout = 15
                writeTimeout = 15
                enableNetworkLog = false
                maxRetryCount = 3
            }
            
            log {
                enableLog = false
                logLevel = AppConfig.LogLevel.ERROR
                saveLogToFile = false
                maxLogFileSize = 10
            }
            
            performance {
                enablePerformanceMonitor = false
                enableCrashCollection = true
            }
            
            security {
                enableCertificatePinning = true
                enableObfuscation = true
                encryptionKey = "production_encryption_key_2024"
            }
        }
    }
}