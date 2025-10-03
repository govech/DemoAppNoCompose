package lj.sword.demoappnocompose.monitor

import android.app.ActivityManager
import android.content.Context
import android.os.Debug
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import lj.sword.demoappnocompose.config.AppConfig
import lj.sword.demoappnocompose.manager.Logger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 内存监控器
 * 监控应用内存使用情况
 *
 * @author Sword
 * @since 1.0.0
 */
@Singleton
class MemoryMonitor
    @Inject
    constructor(
        private val context: Context,
        private val appConfig: AppConfig,
    ) {
        companion object {
            private const val TAG = "MemoryMonitor"
            private const val MONITOR_INTERVAL = 30000L // 30秒监控一次
            private const val MB = 1024 * 1024L
        }

        private var monitorJob: Job? = null
        private val mainHandler = Handler(Looper.getMainLooper())
        private val listeners = mutableListOf<MemoryListener>()

        // 内存监听器
        interface MemoryListener {
            fun onMemoryWarning(memoryInfo: MemoryInfo)

            fun onMemoryPressure(
                memoryInfo: MemoryInfo,
                level: MemoryPressureLevel,
            )

            fun onLowMemory(memoryInfo: MemoryInfo)
        }

        // 内存压力级别
        enum class MemoryPressureLevel {
            LOW, // 内存使用正常
            MEDIUM, // 内存使用较高
            HIGH, // 内存使用很高
            CRITICAL, // 内存使用临界
        }

        // 内存信息
        data class MemoryInfo(
            val usedMemoryMB: Long,
            val totalMemoryMB: Long,
            val availableMemoryMB: Long,
            val maxMemoryMB: Long,
            val usagePercentage: Float,
            val nativeHeapSizeMB: Long,
            val nativeHeapAllocatedMB: Long,
            val timestamp: Long = System.currentTimeMillis(),
        ) {
            fun isMemoryWarning(): Boolean {
                return usagePercentage > 80f
            }

            fun getMemoryPressureLevel(): MemoryPressureLevel {
                return when {
                    usagePercentage < 50f -> MemoryPressureLevel.LOW
                    usagePercentage < 70f -> MemoryPressureLevel.MEDIUM
                    usagePercentage < 85f -> MemoryPressureLevel.HIGH
                    else -> MemoryPressureLevel.CRITICAL
                }
            }
        }

        /**
         * 开始内存监控
         */
        fun startMonitoring() {
            if (!appConfig.enablePerformanceMonitor) {
                Logger.d("性能监控已禁用，跳过内存监控", TAG)
                return
            }

            if (monitorJob?.isActive == true) {
                Logger.d("内存监控已在运行", TAG)
                return
            }

            Logger.d("开始内存监控", TAG)
            monitorJob =
                CoroutineScope(Dispatchers.IO).launch {
                    while (isActive) {
                        try {
                            val memoryInfo = getCurrentMemoryInfo()
                            checkMemoryStatus(memoryInfo)
                            delay(MONITOR_INTERVAL)
                        } catch (e: Exception) {
                            Logger.e("内存监控异常", e, TAG)
                            delay(MONITOR_INTERVAL)
                        }
                    }
                }
        }

        /**
         * 停止内存监控
         */
        fun stopMonitoring() {
            Logger.d("停止内存监控", TAG)
            monitorJob?.cancel()
            monitorJob = null
        }

        /**
         * 添加内存监听器
         */
        fun addMemoryListener(listener: MemoryListener) {
            listeners.add(listener)
        }

        /**
         * 移除内存监听器
         */
        fun removeMemoryListener(listener: MemoryListener) {
            listeners.remove(listener)
        }

        /**
         * 获取当前内存信息
         */
        fun getCurrentMemoryInfo(): MemoryInfo {
            val runtime = Runtime.getRuntime()
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)

            val maxMemory = runtime.maxMemory()
            val totalMemory = runtime.totalMemory()
            val freeMemory = runtime.freeMemory()
            val usedMemory = totalMemory - freeMemory
            val availableMemory = maxMemory - usedMemory

            val nativeHeapSize = Debug.getNativeHeapSize()
            val nativeHeapAllocated = Debug.getNativeHeapAllocatedSize()

            return MemoryInfo(
                usedMemoryMB = usedMemory / MB,
                totalMemoryMB = totalMemory / MB,
                availableMemoryMB = availableMemory / MB,
                maxMemoryMB = maxMemory / MB,
                usagePercentage = (usedMemory.toFloat() / maxMemory.toFloat()) * 100f,
                nativeHeapSizeMB = nativeHeapSize / MB,
                nativeHeapAllocatedMB = nativeHeapAllocated / MB,
            )
        }

        /**
         * 检查内存状态
         */
        private fun checkMemoryStatus(memoryInfo: MemoryInfo) {
            Logger.d(
                "内存使用情况: ${memoryInfo.usedMemoryMB}MB/${memoryInfo.maxMemoryMB}MB " +
                    "(${String.format("%.1f", memoryInfo.usagePercentage)}%)",
                TAG,
            )

            // 检查内存警告
            if (memoryInfo.isMemoryWarning()) {
                Logger.w("内存使用警告: ${memoryInfo.usagePercentage}%", TAG)
                notifyMemoryWarning(memoryInfo)
            }

            // 检查内存压力
            val pressureLevel = memoryInfo.getMemoryPressureLevel()
            if (pressureLevel != MemoryPressureLevel.LOW) {
                Logger.w("内存压力: $pressureLevel", TAG)
                notifyMemoryPressure(memoryInfo, pressureLevel)

                // 触发内存优化
                if (pressureLevel == MemoryPressureLevel.HIGH || pressureLevel == MemoryPressureLevel.CRITICAL) {
                    performMemoryCleanup()
                }
            }

            // 检查低内存
            if (memoryInfo.availableMemoryMB < appConfig.memoryWarningThreshold) {
                Logger.w("可用内存不足: ${memoryInfo.availableMemoryMB}MB", TAG)
                notifyLowMemory(memoryInfo)
            }
        }

        /**
         * 执行内存清理
         */
        fun performMemoryCleanup() {
            Logger.d("执行内存清理", TAG)

            try {
                // 建议GC
                System.gc()

                // 清理内存缓存（如果有的话）
                // 这里可以添加具体的缓存清理逻辑

                Logger.d("内存清理完成", TAG)
            } catch (e: Exception) {
                Logger.e("内存清理失败", e, TAG)
            }
        }

        /**
         * 获取内存使用报告
         */
        fun getMemoryReport(): String {
            val memoryInfo = getCurrentMemoryInfo()
            return buildString {
                appendLine("=== 内存使用报告 ===")
                appendLine("已使用内存: ${memoryInfo.usedMemoryMB}MB")
                appendLine("总内存: ${memoryInfo.totalMemoryMB}MB")
                appendLine("最大内存: ${memoryInfo.maxMemoryMB}MB")
                appendLine("可用内存: ${memoryInfo.availableMemoryMB}MB")
                appendLine("使用率: ${String.format("%.1f", memoryInfo.usagePercentage)}%")
                appendLine("Native堆大小: ${memoryInfo.nativeHeapSizeMB}MB")
                appendLine("Native堆已分配: ${memoryInfo.nativeHeapAllocatedMB}MB")
                appendLine("内存压力级别: ${memoryInfo.getMemoryPressureLevel()}")
                appendLine("时间戳: ${memoryInfo.timestamp}")
            }
        }

        /**
         * 通知内存警告
         */
        private fun notifyMemoryWarning(memoryInfo: MemoryInfo) {
            mainHandler.post {
                listeners.forEach { listener ->
                    try {
                        listener.onMemoryWarning(memoryInfo)
                    } catch (e: Exception) {
                        Logger.e("内存警告监听器回调失败", e, TAG)
                    }
                }
            }
        }

        /**
         * 通知内存压力
         */
        private fun notifyMemoryPressure(
            memoryInfo: MemoryInfo,
            level: MemoryPressureLevel,
        ) {
            mainHandler.post {
                listeners.forEach { listener ->
                    try {
                        listener.onMemoryPressure(memoryInfo, level)
                    } catch (e: Exception) {
                        Logger.e("内存压力监听器回调失败", e, TAG)
                    }
                }
            }
        }

        /**
         * 通知低内存
         */
        private fun notifyLowMemory(memoryInfo: MemoryInfo) {
            mainHandler.post {
                listeners.forEach { listener ->
                    try {
                        listener.onLowMemory(memoryInfo)
                    } catch (e: Exception) {
                        Logger.e("低内存监听器回调失败", e, TAG)
                    }
                }
            }
        }
    }
