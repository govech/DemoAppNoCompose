package lj.sword.demoappnocompose.monitor

import android.app.Application
import android.content.Context
import lj.sword.demoappnocompose.config.AppConfig
import lj.sword.demoappnocompose.manager.Logger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 性能监控管理器
 * 统一管理各种性能监控组件
 * 
 * @author Sword
 * @since 1.0.0
 */
@Singleton
class PerformanceMonitor @Inject constructor(
    private val context: Context,
    private val appConfig: AppConfig,
    private val startupTimeTracker: StartupTimeTracker,
    private val memoryMonitor: MemoryMonitor,
    private val networkPerformanceInterceptor: NetworkPerformanceInterceptor
) {

    companion object {
        private const val TAG = "PerformanceMonitor"
    }

    private var isInitialized = false

    /**
     * 初始化性能监控
     */
    fun initialize(application: Application) {
        if (!appConfig.enablePerformanceMonitor) {
            Logger.d("性能监控已禁用", TAG)
            return
        }

        if (isInitialized) {
            Logger.d("性能监控已初始化", TAG)
            return
        }

        Logger.d("初始化性能监控", TAG)

        try {
            // 初始化启动时间追踪
            initStartupTimeTracker(application)

            // 初始化内存监控
            initMemoryMonitor()

            // 初始化网络性能监控
            initNetworkPerformanceMonitor()

            isInitialized = true
            Logger.i("性能监控初始化完成", TAG)
        } catch (e: Exception) {
            Logger.e("性能监控初始化失败", e, TAG)
        }
    }

    /**
     * 初始化启动时间追踪
     */
    private fun initStartupTimeTracker(application: Application) {
        Logger.d("初始化启动时间追踪", TAG)
        
        // 注册Activity生命周期回调
        application.registerActivityLifecycleCallbacks(startupTimeTracker)
        
        // 添加启动监听器
        startupTimeTracker.addStartupListener(object : StartupTimeTracker.StartupListener {
            override fun onStartupPhaseCompleted(phase: StartupTimeTracker.StartupPhase, duration: Long) {
                Logger.d("启动阶段完成: $phase, 耗时: ${duration}ms", TAG)
            }

            override fun onStartupCompleted(totalDuration: Long) {
                Logger.i("应用启动完成，总耗时: ${totalDuration}ms", TAG)
                
                // 检查启动性能
                if (totalDuration > appConfig.startupTimeout) {
                    Logger.w("应用启动性能警告：耗时${totalDuration}ms，超过阈值${appConfig.startupTimeout}ms", TAG)
                }
            }
        })
        
        // 开始追踪
        startupTimeTracker.startTracking()
    }

    /**
     * 初始化内存监控
     */
    private fun initMemoryMonitor() {
        Logger.d("初始化内存监控", TAG)
        
        // 添加内存监听器
        memoryMonitor.addMemoryListener(object : MemoryMonitor.MemoryListener {
            override fun onMemoryWarning(memoryInfo: MemoryMonitor.MemoryInfo) {
                Logger.w("内存使用警告: ${memoryInfo.usagePercentage}%", TAG)
            }

            override fun onMemoryPressure(memoryInfo: MemoryMonitor.MemoryInfo, level: MemoryMonitor.MemoryPressureLevel) {
                Logger.w("内存压力: $level, 使用率: ${memoryInfo.usagePercentage}%", TAG)
                
                // 高压力时执行内存清理
                if (level == MemoryMonitor.MemoryPressureLevel.HIGH || 
                    level == MemoryMonitor.MemoryPressureLevel.CRITICAL) {
                    memoryMonitor.performMemoryCleanup()
                }
            }

            override fun onLowMemory(memoryInfo: MemoryMonitor.MemoryInfo) {
                Logger.w("可用内存不足: ${memoryInfo.availableMemoryMB}MB", TAG)
                memoryMonitor.performMemoryCleanup()
            }
        })
        
        // 开始监控
        memoryMonitor.startMonitoring()
    }

    /**
     * 初始化网络性能监控
     */
    private fun initNetworkPerformanceMonitor() {
        Logger.d("初始化网络性能监控", TAG)
        
        // 添加网络性能监听器
        networkPerformanceInterceptor.addPerformanceListener(object : NetworkPerformanceInterceptor.NetworkPerformanceListener {
            override fun onRequestCompleted(performanceInfo: NetworkPerformanceInterceptor.NetworkPerformanceInfo) {
                // 记录到统计中
                NetworkPerformanceInterceptor.NetworkPerformanceStats.addRequest(performanceInfo)
            }

            override fun onSlowRequest(performanceInfo: NetworkPerformanceInterceptor.NetworkPerformanceInfo) {
                Logger.w("慢请求检测: ${performanceInfo.url}, 耗时: ${performanceInfo.duration}ms", TAG)
            }

            override fun onRequestFailed(performanceInfo: NetworkPerformanceInterceptor.NetworkPerformanceInfo, error: Throwable) {
                Logger.e("网络请求失败: ${performanceInfo.url}", error, TAG)
            }
        })
    }

    /**
     * 获取性能报告
     */
    fun getPerformanceReport(): String {
        if (!appConfig.enablePerformanceMonitor) {
            return "性能监控已禁用"
        }

        return buildString {
            appendLine("=== 应用性能报告 ===")
            appendLine("生成时间: ${System.currentTimeMillis()}")
            appendLine()
            
            // 启动性能
            appendLine("--- 启动性能 ---")
            val startupStats = startupTimeTracker.getStartupStats()
            if (startupStats.isStartupCompleted) {
                appendLine("启动总耗时: ${startupStats.totalDuration}ms")
                appendLine("Application创建耗时: ${startupStats.getApplicationCreateDuration()}ms")
                appendLine("首个Activity创建耗时: ${startupStats.getFirstActivityCreateDuration()}ms")
            } else {
                appendLine("应用尚未完成启动")
            }
            appendLine()
            
            // 内存性能
            appendLine("--- 内存性能 ---")
            appendLine(memoryMonitor.getMemoryReport())
            appendLine()
            
            // 网络性能
            appendLine("--- 网络性能 ---")
            appendLine(NetworkPerformanceInterceptor.NetworkPerformanceStats.getPerformanceReport())
        }
    }

    /**
     * 标记启动完成
     */
    fun markStartupCompleted() {
        startupTimeTracker.markStartupCompleted()
    }

    /**
     * 标记Application创建完成
     */
    fun markApplicationCreated() {
        startupTimeTracker.onApplicationCreated()
    }

    /**
     * 执行性能优化
     */
    fun performOptimization() {
        if (!appConfig.enablePerformanceMonitor) {
            Logger.d("性能监控已禁用，跳过性能优化", TAG)
            return
        }

        Logger.d("执行性能优化", TAG)
        
        try {
            // 内存优化
            memoryMonitor.performMemoryCleanup()
            
            // 清理网络性能历史记录（避免内存泄漏）
            NetworkPerformanceInterceptor.NetworkPerformanceStats.clearHistory()
            
            Logger.d("性能优化完成", TAG)
        } catch (e: Exception) {
            Logger.e("性能优化失败", e, TAG)
        }
    }

    /**
     * 停止性能监控
     */
    fun shutdown() {
        if (!isInitialized) {
            return
        }

        Logger.d("停止性能监控", TAG)
        
        try {
            // 停止内存监控
            memoryMonitor.stopMonitoring()
            
            // 清理资源
            NetworkPerformanceInterceptor.NetworkPerformanceStats.clearHistory()
            
            isInitialized = false
            Logger.d("性能监控已停止", TAG)
        } catch (e: Exception) {
            Logger.e("停止性能监控失败", e, TAG)
        }
    }
}