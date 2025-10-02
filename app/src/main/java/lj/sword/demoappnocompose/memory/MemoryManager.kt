package lj.sword.demoappnocompose.memory

import android.app.Application
import android.content.ComponentCallbacks2
import android.content.res.Configuration
import lj.sword.demoappnocompose.config.AppConfig
import lj.sword.demoappnocompose.manager.Logger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 内存管理器
 * 统一管理应用的内存使用和优化
 * 
 * @author Sword
 * @since 1.0.0
 */
@Singleton
class MemoryManager @Inject constructor(
    private val appConfig: AppConfig,
    private val memoryCache: MemoryCache,
    private val networkObjectPool: NetworkObjectPool,
    private val imageCacheManager: ImageCacheManager
) : ComponentCallbacks2 {

    companion object {
        private const val TAG = "MemoryManager"
    }

    private var isInitialized = false

    /**
     * 初始化内存管理器
     */
    fun initialize(application: Application) {
        if (isInitialized) {
            Logger.d("内存管理器已初始化", TAG)
            return
        }

        Logger.d("初始化内存管理器", TAG)
        
        // 注册内存回调
        application.registerComponentCallbacks(this)
        
        isInitialized = true
        Logger.i("内存管理器初始化完成", TAG)
    }

    /**
     * 执行内存优化
     */
    fun performMemoryOptimization() {
        Logger.d("执行内存优化", TAG)
        
        try {
            // 清理过期缓存
            memoryCache.cleanupExpired()
            
            // 清理对象池
            cleanupObjectPools()
            
            // 建议GC
            System.gc()
            
            Logger.d("内存优化完成", TAG)
        } catch (e: Exception) {
            Logger.e("内存优化失败", e, TAG)
        }
    }

    /**
     * 处理内存压力
     */
    fun handleMemoryPressure(level: Int) {
        Logger.d("处理内存压力，级别: $level", TAG)
        
        when (level) {
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE -> {
                // 中等内存压力
                Logger.d("中等内存压力，清理部分缓存", TAG)
                memoryCache.cleanupExpired()
                imageCacheManager.onMemoryPressure()
            }
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW -> {
                // 低内存
                Logger.d("低内存，清理更多缓存", TAG)
                clearNonEssentialCache()
                imageCacheManager.onMemoryPressure()
            }
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL -> {
                // 严重低内存
                Logger.w("严重低内存，清理所有可清理的缓存", TAG)
                clearAllNonEssentialCache()
                imageCacheManager.onLowMemory()
            }
            ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> {
                // UI隐藏
                Logger.d("UI隐藏，清理UI相关缓存", TAG)
                clearUiCache()
            }
            ComponentCallbacks2.TRIM_MEMORY_BACKGROUND -> {
                // 应用进入后台
                Logger.d("应用进入后台，清理后台缓存", TAG)
                clearBackgroundCache()
            }
            ComponentCallbacks2.TRIM_MEMORY_MODERATE,
            ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> {
                // 系统内存不足
                Logger.w("系统内存不足，清理所有缓存", TAG)
                clearAllCache()
            }
        }
    }

    /**
     * 清理非必要缓存
     */
    private fun clearNonEssentialCache() {
        // 清理过期缓存
        memoryCache.cleanupExpired()
        
        // 清理对象池的一半
        cleanupObjectPools(0.5f)
    }

    /**
     * 清理所有非必要缓存
     */
    private fun clearAllNonEssentialCache() {
        // 清理内存缓存
        memoryCache.clear()
        
        // 清理对象池
        cleanupObjectPools()
        
        // 清理图片内存缓存
        imageCacheManager.clearMemoryCache()
    }

    /**
     * 清理UI缓存
     */
    private fun clearUiCache() {
        // 清理图片内存缓存
        imageCacheManager.clearMemoryCache()
    }

    /**
     * 清理后台缓存
     */
    private fun clearBackgroundCache() {
        // 清理过期缓存
        memoryCache.cleanupExpired()
        
        // 清理部分对象池
        cleanupObjectPools(0.7f)
    }

    /**
     * 清理所有缓存
     */
    private fun clearAllCache() {
        memoryCache.clear()
        networkObjectPool.clearAll()
        imageCacheManager.clearMemoryCache()
        ObjectPoolManager.clearAll()
    }

    /**
     * 清理对象池
     */
    private fun cleanupObjectPools(ratio: Float = 1.0f) {
        if (ratio >= 1.0f) {
            // 清理所有对象池
            networkObjectPool.clearAll()
            ObjectPoolManager.clearAll()
        } else {
            // 部分清理（这里简化处理，实际可以更精细）
            Logger.d("部分清理对象池，比例: $ratio", TAG)
        }
    }

    /**
     * 获取内存使用报告
     */
    fun getMemoryReport(): String {
        return buildString {
            appendLine("=== 内存管理报告 ===")
            appendLine("生成时间: ${System.currentTimeMillis()}")
            appendLine()
            
            // 内存缓存报告
            appendLine(memoryCache.getCacheReport())
            appendLine()
            
            // 图片缓存报告
            appendLine(imageCacheManager.getCacheReport())
            appendLine()
            
            // 对象池报告
            appendLine("--- 对象池统计 ---")
            val poolStats = ObjectPoolManager.getStats()
            poolStats.forEach { (name, stats) ->
                appendLine("$name: ${stats.currentSize}/${stats.maxSize} (${String.format("%.1f", stats.usagePercentage)}%)")
            }
            appendLine()
            
            // 网络对象池报告
            appendLine(networkObjectPool.getPoolStats().getReport())
        }
    }

    /**
     * 获取内存使用统计
     */
    fun getMemoryStats(): MemoryStats {
        val runtime = Runtime.getRuntime()
        val maxMemory = runtime.maxMemory()
        val totalMemory = runtime.totalMemory()
        val freeMemory = runtime.freeMemory()
        val usedMemory = totalMemory - freeMemory

        return MemoryStats(
            maxMemoryMB = maxMemory / 1024 / 1024,
            totalMemoryMB = totalMemory / 1024 / 1024,
            usedMemoryMB = usedMemory / 1024 / 1024,
            freeMemoryMB = freeMemory / 1024 / 1024,
            usagePercentage = (usedMemory.toFloat() / maxMemory) * 100f,
            cacheStats = memoryCache.getStats(),
            imageCacheStats = imageCacheManager.getCacheStats(),
            objectPoolStats = ObjectPoolManager.getStats()
        )
    }

    // ComponentCallbacks2 实现
    override fun onTrimMemory(level: Int) {
        handleMemoryPressure(level)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        // 配置变化时不需要特殊处理
    }

    override fun onLowMemory() {
        Logger.w("收到低内存回调", TAG)
        handleMemoryPressure(ComponentCallbacks2.TRIM_MEMORY_COMPLETE)
    }

    /**
     * 内存统计信息
     */
    data class MemoryStats(
        val maxMemoryMB: Long,
        val totalMemoryMB: Long,
        val usedMemoryMB: Long,
        val freeMemoryMB: Long,
        val usagePercentage: Float,
        val cacheStats: MemoryCache.CacheStats,
        val imageCacheStats: ImageCacheManager.ImageCacheStats,
        val objectPoolStats: Map<String, ObjectPoolManager.PoolStats>
    )
}