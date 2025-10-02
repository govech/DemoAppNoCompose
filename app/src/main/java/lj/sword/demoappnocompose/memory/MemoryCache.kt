package lj.sword.demoappnocompose.memory

import android.util.LruCache
import lj.sword.demoappnocompose.config.AppConfig
import lj.sword.demoappnocompose.manager.Logger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 内存缓存管理器
 * 基于LRU算法的内存缓存实现
 * 
 * @author Sword
 * @since 1.0.0
 */
@Singleton
class MemoryCache @Inject constructor(
    private val appConfig: AppConfig
) {

    companion object {
        private const val TAG = "MemoryCache"
        private const val DEFAULT_CACHE_SIZE = 50 * 1024 * 1024 // 50MB
    }

    // 主缓存
    private val cache: LruCache<String, CacheEntry<*>>

    // 缓存统计
    private var hitCount = 0
    private var missCount = 0
    private var putCount = 0
    private var evictionCount = 0

    init {
        val maxSize = if (appConfig.enableCache) {
            (appConfig.memoryCacheSize * 1024 * 1024).toInt()
        } else {
            DEFAULT_CACHE_SIZE
        }

        cache = object : LruCache<String, CacheEntry<*>>(maxSize) {
            override fun sizeOf(key: String, value: CacheEntry<*>): Int {
                return value.size
            }

            override fun entryRemoved(
                evicted: Boolean,
                key: String,
                oldValue: CacheEntry<*>,
                newValue: CacheEntry<*>?
            ) {
                if (evicted) {
                    evictionCount++
                    Logger.d("缓存项被移除: $key, 大小: ${oldValue.size}字节", TAG)
                }
            }
        }

        Logger.d("内存缓存初始化完成，最大大小: ${maxSize / 1024 / 1024}MB", TAG)
    }

    /**
     * 存储数据到缓存
     */
    fun <T> put(key: String, value: T, ttl: Long = appConfig.cacheExpireTime * 60 * 60 * 1000): T? {
        if (!appConfig.enableCache) {
            return null
        }

        val size = calculateSize(value)
        val entry = CacheEntry(
            data = value,
            size = size,
            createTime = System.currentTimeMillis(),
            ttl = ttl
        )

        val previous = cache.put(key, entry)
        putCount++

        Logger.d("缓存存储: $key, 大小: ${size}字节", TAG)
        
        @Suppress("UNCHECKED_CAST")
        return previous?.data as? T
    }

    /**
     * 从缓存获取数据
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: String): T? {
        if (!appConfig.enableCache) {
            return null
        }

        val entry = cache.get(key)
        
        if (entry == null) {
            missCount++
            Logger.d("缓存未命中: $key", TAG)
            return null
        }

        // 检查是否过期
        if (entry.isExpired()) {
            cache.remove(key)
            missCount++
            Logger.d("缓存已过期: $key", TAG)
            return null
        }

        hitCount++
        Logger.d("缓存命中: $key", TAG)
        return entry.data as? T
    }

    /**
     * 移除缓存项
     */
    fun remove(key: String): Boolean {
        val removed = cache.remove(key) != null
        if (removed) {
            Logger.d("缓存移除: $key", TAG)
        }
        return removed
    }

    /**
     * 清空缓存
     */
    fun clear() {
        cache.evictAll()
        hitCount = 0
        missCount = 0
        putCount = 0
        evictionCount = 0
        Logger.d("缓存已清空", TAG)
    }

    /**
     * 检查缓存中是否包含指定key
     */
    fun contains(key: String): Boolean {
        val entry = cache.get(key)
        return entry != null && !entry.isExpired()
    }

    /**
     * 获取缓存大小
     */
    fun size(): Int {
        return cache.size()
    }

    /**
     * 获取最大缓存大小
     */
    fun maxSize(): Int {
        return cache.maxSize()
    }

    /**
     * 获取缓存命中率
     */
    fun getHitRate(): Float {
        val total = hitCount + missCount
        return if (total > 0) {
            hitCount.toFloat() / total
        } else 0f
    }

    /**
     * 清理过期缓存
     */
    fun cleanupExpired() {
        val iterator = cache.snapshot().iterator()
        var cleanedCount = 0
        
        while (iterator.hasNext()) {
            val (key, entry) = iterator.next()
            if (entry.isExpired()) {
                cache.remove(key)
                cleanedCount++
            }
        }
        
        if (cleanedCount > 0) {
            Logger.d("清理过期缓存: ${cleanedCount}项", TAG)
        }
    }

    /**
     * 获取缓存统计信息
     */
    fun getStats(): CacheStats {
        return CacheStats(
            hitCount = hitCount,
            missCount = missCount,
            putCount = putCount,
            evictionCount = evictionCount,
            size = cache.size(),
            maxSize = cache.maxSize(),
            hitRate = getHitRate()
        )
    }

    /**
     * 获取缓存报告
     */
    fun getCacheReport(): String {
        val stats = getStats()
        return buildString {
            appendLine("=== 内存缓存报告 ===")
            appendLine("当前大小: ${stats.size / 1024 / 1024}MB")
            appendLine("最大大小: ${stats.maxSize / 1024 / 1024}MB")
            appendLine("使用率: ${String.format("%.1f", (stats.size.toFloat() / stats.maxSize) * 100)}%")
            appendLine("命中次数: ${stats.hitCount}")
            appendLine("未命中次数: ${stats.missCount}")
            appendLine("存储次数: ${stats.putCount}")
            appendLine("移除次数: ${stats.evictionCount}")
            appendLine("命中率: ${String.format("%.1f", stats.hitRate * 100)}%")
        }
    }

    /**
     * 计算对象大小（简单估算）
     */
    private fun calculateSize(value: Any?): Int {
        return when (value) {
            null -> 0
            is String -> value.length * 2 // 每个字符2字节
            is ByteArray -> value.size
            is Int -> 4
            is Long -> 8
            is Float -> 4
            is Double -> 8
            is Boolean -> 1
            else -> 64 // 默认估算64字节
        }
    }

    /**
     * 缓存项
     */
    private data class CacheEntry<T>(
        val data: T,
        val size: Int,
        val createTime: Long,
        val ttl: Long
    ) {
        fun isExpired(): Boolean {
            return System.currentTimeMillis() - createTime > ttl
        }
    }

    /**
     * 缓存统计信息
     */
    data class CacheStats(
        val hitCount: Int,
        val missCount: Int,
        val putCount: Int,
        val evictionCount: Int,
        val size: Int,
        val maxSize: Int,
        val hitRate: Float
    )
}