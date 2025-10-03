# 内存优化指南

本文档详细说明了 Android MVVM 框架中的内存管理和优化策略。

## 📋 目录

- [内存管理概述](#内存管理概述)
- [对象池优化](#对象池优化)
- [缓存策略](#缓存策略)
- [内存泄漏防护](#内存泄漏防护)
- [图片内存优化](#图片内存优化)
- [监控和诊断](#监控和诊断)

## 🎯 内存管理概述

### 内存管理目标

- **减少内存占用**: 优化内存使用，避免不必要的内存分配
- **防止内存泄漏**: 及时释放不再使用的对象引用
- **提升性能**: 减少 GC 频率，提升应用流畅度
- **稳定性**: 避免 OOM 崩溃，提升应用稳定性

### 内存管理架构

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   对象池管理     │    │   缓存管理       │    │   监控预警       │
│                 │    │                 │    │                 │
│ NetworkPool     │    │ MemoryCache     │    │ MemoryMonitor   │
│ ObjectPool      │    │ ImageCache      │    │ LeakDetector    │
│ ViewPool        │    │ DatabaseCache   │    │ GCMonitor       │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 🔄 对象池优化

### NetworkObjectPool

#### 网络请求对象复用

```kotlin
object NetworkObjectPool {
    
    private val requestBuilderPool = ArrayDeque<Request.Builder>()
    private val stringBuilderPool = ArrayDeque<StringBuilder>()
    private val byteArrayPool = ArrayDeque<ByteArray>()
    
    private const val MAX_POOL_SIZE = 10
    
    fun acquireRequestBuilder(): Request.Builder {
        return synchronized(requestBuilderPool) {
            requestBuilderPool.pollFirst() ?: Request.Builder()
        }
    }
    
    fun releaseRequestBuilder(builder: Request.Builder) {
        synchronized(requestBuilderPool) {
            if (requestBuilderPool.size < MAX_POOL_SIZE) {
                // 重置 builder 状态
                builder.removeHeader("Authorization")
                builder.removeHeader("Content-Type")
                requestBuilderPool.offerLast(builder)
            }
        }
    }
    
    fun acquireStringBuilder(): StringBuilder {
        return synchronized(stringBuilderPool) {
            val sb = stringBuilderPool.pollFirst() ?: StringBuilder()
            sb.clear()
            sb
        }
    }
    
    fun releaseStringBuilder(stringBuilder: StringBuilder) {
        synchronized(stringBuilderPool) {
            if (stringBuilderPool.size < MAX_POOL_SIZE && stringBuilder.length < 1024) {
                stringBuilderPool.offerLast(stringBuilder)
            }
        }
    }
    
    fun acquireByteArray(size: Int = 1024): ByteArray {
        return synchronized(byteArrayPool) {
            byteArrayPool.pollFirst()?.takeIf { it.size >= size } 
                ?: ByteArray(size)
        }
    }
    
    fun releaseByteArray(byteArray: ByteArray) {
        synchronized(byteArrayPool) {
            if (byteArrayPool.size < MAX_POOL_SIZE && byteArray.size <= 4096) {
                byteArrayPool.offerLast(byteArray)
            }
        }
    }
    
    fun clear() {
        synchronized(requestBuilderPool) { requestBuilderPool.clear() }
        synchronized(stringBuilderPool) { stringBuilderPool.clear() }
        synchronized(byteArrayPool) { byteArrayPool.clear() }
    }
}
```

#### 使用示例

```kotlin
class ApiService {
    
    fun createRequest(url: String): Request {
        val builder = NetworkObjectPool.acquireRequestBuilder()
        
        try {
            return builder
                .url(url)
                .addHeader("Content-Type", "application/json")
                .build()
        } finally {
            NetworkObjectPool.releaseRequestBuilder(builder)
        }
    }
    
    fun buildJsonString(data: Map<String, Any>): String {
        val sb = NetworkObjectPool.acquireStringBuilder()
        
        try {
            sb.append("{")
            data.entries.forEachIndexed { index, entry ->
                if (index > 0) sb.append(",")
                sb.append("\"${entry.key}\":\"${entry.value}\"")
            }
            sb.append("}")
            return sb.toString()
        } finally {
            NetworkObjectPool.releaseStringBuilder(sb)
        }
    }
}
```

### ViewHolder 对象池

```kotlin
class ViewHolderPool<VH : RecyclerView.ViewHolder> {
    
    private val pool = ArrayDeque<VH>()
    private val maxPoolSize: Int
    
    constructor(maxPoolSize: Int = 5) {
        this.maxPoolSize = maxPoolSize
    }
    
    fun acquire(): VH? {
        return synchronized(pool) {
            pool.pollFirst()
        }
    }
    
    fun release(viewHolder: VH) {
        synchronized(pool) {
            if (pool.size < maxPoolSize) {
                // 清理 ViewHolder 状态
                clearViewHolderState(viewHolder)
                pool.offerLast(viewHolder)
            }
        }
    }
    
    private fun clearViewHolderState(viewHolder: VH) {
        // 清理点击监听器
        viewHolder.itemView.setOnClickListener(null)
        viewHolder.itemView.setOnLongClickListener(null)
        
        // 重置标签
        viewHolder.itemView.tag = null
    }
    
    fun clear() {
        synchronized(pool) {
            pool.clear()
        }
    }
}
```

## 💾 缓存策略

### MemoryCache (LRU缓存)

#### 实现原理

```kotlin
class MemoryCache<K, V>(
    private val maxSize: Int,
    private val expireTimeMs: Long = Long.MAX_VALUE
) {
    
    private val cache = object : LinkedHashMap<K, CacheEntry<V>>(
        16, 0.75f, true
    ) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, CacheEntry<V>>?): Boolean {
            return size > maxSize
        }
    }
    
    private data class CacheEntry<V>(
        val value: V,
        val createTime: Long = System.currentTimeMillis()
    ) {
        fun isExpired(expireTimeMs: Long): Boolean {
            return expireTimeMs != Long.MAX_VALUE && 
                   System.currentTimeMillis() - createTime > expireTimeMs
        }
    }
    
    @Synchronized
    fun put(key: K, value: V) {
        cache[key] = CacheEntry(value)
    }
    
    @Synchronized
    fun get(key: K): V? {
        val entry = cache[key] ?: return null
        
        return if (entry.isExpired(expireTimeMs)) {
            cache.remove(key)
            null
        } else {
            entry.value
        }
    }
    
    @Synchronized
    fun remove(key: K): V? {
        return cache.remove(key)?.value
    }
    
    @Synchronized
    fun clear() {
        cache.clear()
    }
    
    @Synchronized
    fun size(): Int = cache.size
    
    @Synchronized
    fun evictExpired() {
        val iterator = cache.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.value.isExpired(expireTimeMs)) {
                iterator.remove()
            }
        }
    }
    
    @Synchronized
    fun evictAll() {
        cache.clear()
    }
}
```

#### 缓存配置

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object CacheModule {
    
    @Provides
    @Singleton
    @Named("UserCache")
    fun provideUserCache(): MemoryCache<String, User> {
        return MemoryCache(
            maxSize = 100,
            expireTimeMs = TimeUnit.MINUTES.toMillis(30)
        )
    }
    
    @Provides
    @Singleton
    @Named("ApiCache")
    fun provideApiCache(): MemoryCache<String, Any> {
        return MemoryCache(
            maxSize = 200,
            expireTimeMs = TimeUnit.MINUTES.toMillis(10)
        )
    }
}
```

### 多级缓存策略

```kotlin
class MultiLevelCache<K, V>(
    private val memoryCache: MemoryCache<K, V>,
    private val diskCache: DiskCache<K, V>? = null,
    private val networkLoader: suspend (K) -> V
) {
    
    suspend fun get(key: K): V? {
        // 1. 尝试从内存缓存获取
        memoryCache.get(key)?.let { return it }
        
        // 2. 尝试从磁盘缓存获取
        diskCache?.get(key)?.let { value ->
            // 回写到内存缓存
            memoryCache.put(key, value)
            return value
        }
        
        // 3. 从网络加载
        return try {
            val value = networkLoader(key)
            
            // 保存到缓存
            memoryCache.put(key, value)
            diskCache?.put(key, value)
            
            value
        } catch (e: Exception) {
            null
        }
    }
    
    fun put(key: K, value: V) {
        memoryCache.put(key, value)
        diskCache?.put(key, value)
    }
    
    fun remove(key: K) {
        memoryCache.remove(key)
        diskCache?.remove(key)
    }
    
    fun clear() {
        memoryCache.clear()
        diskCache?.clear()
    }
}
```

## 🚫 内存泄漏防护

### 常见内存泄漏场景

#### 1. Activity/Fragment 泄漏

```kotlin
// ❌ 错误：静态引用导致泄漏
class MainActivity : AppCompatActivity() {
    companion object {
        var instance: MainActivity? = null  // 泄漏风险
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this  // 泄漏
    }
}

// ✅ 正确：使用弱引用
class MainActivity : AppCompatActivity() {
    companion object {
        private var instanceRef: WeakReference<MainActivity>? = null
        
        fun getInstance(): MainActivity? = instanceRef?.get()
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instanceRef = WeakReference(this)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        instanceRef?.clear()
    }
}
```

#### 2. Handler 泄漏

```kotlin
// ❌ 错误：非静态内部类持有外部引用
class MainActivity : AppCompatActivity() {
    
    private val handler = Handler(Looper.getMainLooper()) // 泄漏风险
    
    private fun delayedOperation() {
        handler.postDelayed({
            // 操作可能在 Activity 销毁后执行
            updateUI()
        }, 5000)
    }
}

// ✅ 正确：使用静态内部类 + 弱引用
class MainActivity : AppCompatActivity() {
    
    private val handler = DelayedHandler(this)
    
    private class DelayedHandler(activity: MainActivity) : Handler(Looper.getMainLooper()) {
        private val activityRef = WeakReference(activity)
        
        override fun handleMessage(msg: Message) {
            activityRef.get()?.let { activity ->
                // 安全地操作 Activity
                activity.updateUI()
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
```

#### 3. 监听器泄漏

```kotlin
// ❌ 错误：忘记注销监听器
class UserFragment : Fragment() {
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 注册监听器但忘记注销
        EventBus.getDefault().register(this)
        userRepository.addUserChangeListener(userChangeListener)
    }
}

// ✅ 正确：及时注销监听器
class UserFragment : Fragment() {
    
    private val userChangeListener = object : UserChangeListener {
        override fun onUserChanged(user: User) {
            updateUserUI(user)
        }
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        EventBus.getDefault().register(this)
        userRepository.addUserChangeListener(userChangeListener)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        
        EventBus.getDefault().unregister(this)
        userRepository.removeUserChangeListener(userChangeListener)
    }
}
```

### 内存泄漏检测工具

#### LeakCanary 集成

```kotlin
// 在 Application 中初始化
class BaseApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        if (BuildConfig.DEBUG) {
            // LeakCanary 自动检测内存泄漏
            if (LeakCanary.isInAnalyzerProcess(this)) {
                return
            }
            LeakCanary.install(this)
        }
    }
}
```

#### 自定义泄漏检测

```kotlin
class MemoryLeakDetector {
    
    private val activityRefs = mutableSetOf<WeakReference<Activity>>()
    private val fragmentRefs = mutableSetOf<WeakReference<Fragment>>()
    
    fun trackActivity(activity: Activity) {
        activityRefs.add(WeakReference(activity))
    }
    
    fun trackFragment(fragment: Fragment) {
        fragmentRefs.add(WeakReference(fragment))
    }
    
    fun checkForLeaks() {
        // 强制 GC
        System.gc()
        System.runFinalization()
        System.gc()
        
        // 检查是否有未释放的引用
        val leakedActivities = activityRefs.filter { it.get() != null }
        val leakedFragments = fragmentRefs.filter { it.get() != null }
        
        if (leakedActivities.isNotEmpty() || leakedFragments.isNotEmpty()) {
            Logger.w("Potential memory leaks detected:")
            Logger.w("Leaked activities: ${leakedActivities.size}")
            Logger.w("Leaked fragments: ${leakedFragments.size}")
        }
        
        // 清理已释放的引用
        activityRefs.removeAll { it.get() == null }
        fragmentRefs.removeAll { it.get() == null }
    }
}
```

## 🖼️ 图片内存优化

### ImageCacheManager

```kotlin
@Singleton
class ImageCacheManager @Inject constructor(
    private val context: Context
) {
    
    private var memoryPressureListener: ((MemoryPressureLevel) -> Unit)? = null
    
    fun clearMemoryCache() {
        Coil.imageLoader(context).memoryCache?.clear()
        Logger.d("Image memory cache cleared")
    }
    
    fun clearDiskCache() {
        GlobalScope.launch(Dispatchers.IO) {
            Coil.imageLoader(context).diskCache?.clear()
            Logger.d("Image disk cache cleared")
        }
    }
    
    fun getMemoryCacheSize(): Long {
        return Coil.imageLoader(context).memoryCache?.size ?: 0L
    }
    
    fun getDiskCacheSize(): Long {
        return Coil.imageLoader(context).diskCache?.size ?: 0L
    }
    
    fun setMemoryPressureListener(listener: (MemoryPressureLevel) -> Unit) {
        this.memoryPressureListener = listener
    }
    
    fun onMemoryPressure(level: MemoryPressureLevel) {
        when (level) {
            MemoryPressureLevel.MEDIUM -> {
                // 清理部分缓存
                Coil.imageLoader(context).memoryCache?.trimToSize(
                    (getMemoryCacheSize() * 0.5).toLong()
                )
            }
            MemoryPressureLevel.HIGH -> {
                // 清理大部分缓存
                clearMemoryCache()
            }
            MemoryPressureLevel.CRITICAL -> {
                // 清理所有缓存
                clearMemoryCache()
                clearDiskCache()
            }
            else -> {}
        }
        
        memoryPressureListener?.invoke(level)
    }
}
```

### 图片加载优化

```kotlin
// 配置 Coil 图片加载器
@Module
@InstallIn(SingletonComponent::class)
object ImageModule {
    
    @Provides
    @Singleton
    fun provideImageLoader(@ApplicationContext context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.25) // 使用 25% 的可用内存
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(50 * 1024 * 1024) // 50MB
                    .build()
            }
            .respectCacheHeaders(false)
            .build()
    }
}

// 使用示例
fun ImageView.loadOptimized(url: String?) {
    this.load(url) {
        // 根据 ImageView 尺寸调整图片大小
        size(this@loadOptimized.width, this@loadOptimized.height)
        
        // 设置占位图和错误图
        placeholder(R.drawable.placeholder)
        error(R.drawable.error)
        
        // 启用内存缓存
        memoryCachePolicy(CachePolicy.ENABLED)
        
        // 启用磁盘缓存
        diskCachePolicy(CachePolicy.ENABLED)
        
        // 图片变换
        transformations(RoundedCornersTransformation(8.dp))
    }
}
```

## 📊 监控和诊断

### 内存使用监控

```kotlin
class MemoryUsageMonitor {
    
    fun getCurrentMemoryUsage(): MemoryUsage {
        val runtime = Runtime.getRuntime()
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        
        return MemoryUsage(
            usedMemoryMB = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024),
            maxMemoryMB = runtime.maxMemory() / (1024 * 1024),
            availableMemoryMB = memInfo.availMem / (1024 * 1024),
            isLowMemory = memInfo.lowMemory
        )
    }
    
    fun logMemoryUsage() {
        val usage = getCurrentMemoryUsage()
        Logger.d("Memory Usage: ${usage.usedMemoryMB}MB / ${usage.maxMemoryMB}MB " +
                "(${(usage.usedMemoryMB.toFloat() / usage.maxMemoryMB * 100).toInt()}%)")
        
        if (usage.isLowMemory) {
            Logger.w("Low memory detected!")
        }
    }
}

data class MemoryUsage(
    val usedMemoryMB: Long,
    val maxMemoryMB: Long,
    val availableMemoryMB: Long,
    val isLowMemory: Boolean
)
```

### 内存优化最佳实践

#### 1. 及时释放资源
```kotlin
class MyActivity : AppCompatActivity() {
    
    private var bitmap: Bitmap? = null
    private var mediaPlayer: MediaPlayer? = null
    
    override fun onDestroy() {
        super.onDestroy()
        
        // 释放 Bitmap
        bitmap?.recycle()
        bitmap = null
        
        // 释放 MediaPlayer
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
```

#### 2. 使用合适的数据结构
```kotlin
// ✅ 使用 SparseArray 替代 HashMap<Integer, Object>
val sparseArray = SparseArray<String>()

// ✅ 使用 ArrayMap 替代 HashMap（小数据集）
val arrayMap = ArrayMap<String, String>()

// ✅ 使用原始类型数组
val intArray = IntArray(100)  // 而不是 Array<Int>
```

#### 3. 避免内存抖动
```kotlin
// ❌ 错误：在循环中创建对象
fun processItems(items: List<String>) {
    for (item in items) {
        val result = StringBuilder()  // 每次循环都创建新对象
        result.append(item)
        // ...
    }
}

// ✅ 正确：复用对象
fun processItems(items: List<String>) {
    val result = StringBuilder()  // 复用 StringBuilder
    for (item in items) {
        result.clear()
        result.append(item)
        // ...
    }
}
```

---

*内存优化是一个持续的过程，需要在开发过程中时刻关注内存使用情况，及时发现和解决内存问题。*