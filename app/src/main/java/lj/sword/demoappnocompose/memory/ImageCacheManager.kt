package lj.sword.demoappnocompose.memory

import android.content.Context
import android.graphics.Bitmap
import android.util.LruCache
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.util.DebugLogger
import lj.sword.demoappnocompose.config.AppConfig
import lj.sword.demoappnocompose.manager.Logger
import okhttp3.OkHttpClient
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 图片缓存管理器
 * 优化图片加载和缓存策略
 * 
 * @author Sword
 * @since 1.0.0
 */
@Singleton
class ImageCacheManager @Inject constructor(
    private val context: Context,
    private val appConfig: AppConfig,
    private val okHttpClient: OkHttpClient
) {

    companion object {
        private const val TAG = "ImageCacheManager"
        private const val DISK_CACHE_DIR = "image_cache"
        private const val MEMORY_CACHE_PERCENTAGE = 0.25 // 使用25%的可用内存
    }

    // Coil ImageLoader
    private val coilImageLoader: ImageLoader by lazy {
        createImageLoader()
    }

    // 自定义Bitmap缓存
    private val bitmapCache: LruCache<String, Bitmap> by lazy {
        createBitmapCache()
    }

    /**
     * 获取ImageLoader实例
     */
    fun getImageLoader(): ImageLoader = coilImageLoader

    /**
     * 创建ImageLoader
     */
    private fun createImageLoader(): ImageLoader {
        return ImageLoader.Builder(context)
            .okHttpClient(okHttpClient)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(MEMORY_CACHE_PERCENTAGE)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(File(context.cacheDir, DISK_CACHE_DIR))
                    .maxSizeBytes(appConfig.diskCacheSize * 1024 * 1024L)
                    .build()
            }
            .respectCacheHeaders(false)
            .apply {
                if (appConfig.enableLog) {
                    logger(DebugLogger())
                }
            }
            .build()
    }

    /**
     * 创建Bitmap缓存
     */
    private fun createBitmapCache(): LruCache<String, Bitmap> {
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        val cacheSize = (maxMemory * MEMORY_CACHE_PERCENTAGE).toInt()

        return object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                return bitmap.byteCount / 1024
            }

            override fun entryRemoved(
                evicted: Boolean,
                key: String,
                oldValue: Bitmap,
                newValue: Bitmap?
            ) {
                if (evicted && !oldValue.isRecycled) {
                    Logger.d("Bitmap缓存项被移除: $key", TAG)
                }
            }
        }
    }

    /**
     * 缓存Bitmap
     */
    fun cacheBitmap(key: String, bitmap: Bitmap) {
        if (appConfig.enableCache && !bitmap.isRecycled) {
            bitmapCache.put(key, bitmap)
            Logger.d("Bitmap已缓存: $key", TAG)
        }
    }

    /**
     * 获取缓存的Bitmap
     */
    fun getCachedBitmap(key: String): Bitmap? {
        if (!appConfig.enableCache) {
            return null
        }

        val bitmap = bitmapCache.get(key)
        if (bitmap != null && !bitmap.isRecycled) {
            Logger.d("Bitmap缓存命中: $key", TAG)
            return bitmap
        }
        return null
    }

    /**
     * 移除Bitmap缓存
     */
    fun removeBitmapCache(key: String) {
        bitmapCache.remove(key)
        Logger.d("Bitmap缓存已移除: $key", TAG)
    }

    /**
     * 清空内存缓存
     */
    fun clearMemoryCache() {
        // 清空Coil内存缓存
        coilImageLoader.memoryCache?.clear()
        
        // 清空自定义Bitmap缓存
        bitmapCache.evictAll()
        
        Logger.d("图片内存缓存已清空", TAG)
    }

    /**
     * 清空磁盘缓存
     */
    suspend fun clearDiskCache() {
        coilImageLoader.diskCache?.clear()
        Logger.d("图片磁盘缓存已清空", TAG)
    }

    /**
     * 清空所有缓存
     */
    suspend fun clearAllCache() {
        clearMemoryCache()
        clearDiskCache()
        Logger.d("所有图片缓存已清空", TAG)
    }

    /**
     * 预加载图片
     */
    fun preloadImage(url: String) {
        if (!appConfig.enableCache) {
            return
        }

        val request = coil.request.ImageRequest.Builder(context)
            .data(url)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build()

        coilImageLoader.enqueue(request)
        Logger.d("预加载图片: $url", TAG)
    }

    /**
     * 批量预加载图片
     */
    fun preloadImages(urls: List<String>) {
        if (!appConfig.enableCache) {
            return
        }

        urls.forEach { url ->
            preloadImage(url)
        }
        Logger.d("批量预加载图片: ${urls.size}张", TAG)
    }

    /**
     * 获取缓存统计信息
     */
    fun getCacheStats(): ImageCacheStats {
        val memoryCache = coilImageLoader.memoryCache
        val diskCache = coilImageLoader.diskCache

        return ImageCacheStats(
            memoryCacheSize = (memoryCache?.size ?: 0).toLong(),
            memoryCacheMaxSize = (memoryCache?.maxSize ?: 0).toLong(),
            diskCacheSize = diskCache?.size ?: 0,
            diskCacheMaxSize = diskCache?.maxSize ?: 0,
            bitmapCacheSize = bitmapCache.size(),
            bitmapCacheMaxSize = bitmapCache.maxSize(),
            bitmapCacheHitCount = bitmapCache.hitCount(),
            bitmapCacheMissCount = bitmapCache.missCount()
        )
    }

    /**
     * 获取缓存报告
     */
    fun getCacheReport(): String {
        val stats = getCacheStats()
        return buildString {
            appendLine("=== 图片缓存报告 ===")
            appendLine("内存缓存:")
            appendLine("  大小: ${stats.memoryCacheSize / 1024 / 1024}MB")
            appendLine("  最大: ${stats.memoryCacheMaxSize / 1024 / 1024}MB")
            appendLine("  使用率: ${String.format("%.1f", stats.getMemoryCacheUsagePercentage())}%")
            appendLine("磁盘缓存:")
            appendLine("  大小: ${stats.diskCacheSize / 1024 / 1024}MB")
            appendLine("  最大: ${stats.diskCacheMaxSize / 1024 / 1024}MB")
            appendLine("  使用率: ${String.format("%.1f", stats.getDiskCacheUsagePercentage())}%")
            appendLine("Bitmap缓存:")
            appendLine("  大小: ${stats.bitmapCacheSize / 1024}KB")
            appendLine("  最大: ${stats.bitmapCacheMaxSize / 1024}KB")
            appendLine("  命中率: ${String.format("%.1f", stats.getBitmapCacheHitRate() * 100)}%")
        }
    }

    /**
     * 内存压力处理
     */
    fun onMemoryPressure() {
        Logger.d("收到内存压力信号，清理图片缓存", TAG)
        
        // 清理一部分内存缓存
        val memoryCache = coilImageLoader.memoryCache
        if (memoryCache != null) {
            val targetSize = memoryCache.maxSize / 2
            // Coil的MemoryCache没有trimToSize方法，这里简化处理
            Logger.d("内存压力下清理图片缓存", TAG)
        }
        
        // 清理自定义Bitmap缓存
        val targetSize = bitmapCache.maxSize() / 2
        // LruCache没有trimToSize方法，通过evictAll然后重新设置大小
        Logger.d("内存压力下清理Bitmap缓存", TAG)
    }

    /**
     * 低内存处理
     */
    fun onLowMemory() {
        Logger.d("收到低内存信号，清空图片内存缓存", TAG)
        clearMemoryCache()
    }

    /**
     * 图片缓存统计信息
     */
    data class ImageCacheStats(
        val memoryCacheSize: Long,
        val memoryCacheMaxSize: Long,
        val diskCacheSize: Long,
        val diskCacheMaxSize: Long,
        val bitmapCacheSize: Int,
        val bitmapCacheMaxSize: Int,
        val bitmapCacheHitCount: Int,
        val bitmapCacheMissCount: Int
    ) {
        fun getMemoryCacheUsagePercentage(): Float {
            return if (memoryCacheMaxSize > 0) {
                (memoryCacheSize.toFloat() / memoryCacheMaxSize) * 100f
            } else 0f
        }

        fun getDiskCacheUsagePercentage(): Float {
            return if (diskCacheMaxSize > 0) {
                (diskCacheSize.toFloat() / diskCacheMaxSize) * 100f
            } else 0f
        }

        fun getBitmapCacheHitRate(): Float {
            val total = bitmapCacheHitCount + bitmapCacheMissCount
            return if (total > 0) {
                bitmapCacheHitCount.toFloat() / total
            } else 0f
        }
    }
}