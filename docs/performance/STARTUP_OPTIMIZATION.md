# 启动优化指南

本文档详细说明了 Android MVVM 框架中的应用启动优化策略和实施方案。

## 📋 目录

- [启动优化概述](#启动优化概述)
- [启动流程分析](#启动流程分析)
- [Application 优化](#application-优化)
- [Activity 启动优化](#activity-启动优化)
- [资源加载优化](#资源加载优化)
- [启动监控](#启动监控)

## 🎯 启动优化概述

### 启动类型

#### 冷启动 (Cold Start)
- **定义**: 应用进程不存在，需要创建新进程
- **耗时最长**: 包含进程创建、Application 初始化等
- **优化重点**: 减少 Application 和首屏 Activity 的初始化时间

#### 温启动 (Warm Start)
- **定义**: 应用进程存在，但 Activity 需要重新创建
- **耗时中等**: 跳过进程创建，但需要重建 Activity
- **优化重点**: 减少 Activity 创建和布局加载时间

#### 热启动 (Hot Start)
- **定义**: 应用进程和 Activity 都存在，只需要显示
- **耗时最短**: 只涉及 Activity 的 onResume 等生命周期
- **优化重点**: 减少 onResume 中的耗时操作

### 启动性能目标

```kotlin
object StartupTargets {
    const val COLD_START_TARGET = 1500L      // 冷启动目标: 1.5秒
    const val WARM_START_TARGET = 800L       // 温启动目标: 0.8秒
    const val HOT_START_TARGET = 300L        // 热启动目标: 0.3秒
    
    const val FIRST_FRAME_TARGET = 1000L     // 首帧显示目标: 1秒
    const val INTERACTIVE_TARGET = 2000L     // 可交互目标: 2秒
}
```

## 🔍 启动流程分析

### 启动时间线

```
进程创建 → Application.onCreate → Activity.onCreate → Activity.onStart → 
Activity.onResume → 首帧绘制 → 数据加载完成 → 用户可交互
```

### 启动阶段监控

```kotlin
class StartupPhaseTracker {
    
    private val phases = mutableMapOf<StartupPhase, Long>()
    private var processStartTime: Long = 0
    
    init {
        // 记录进程启动时间
        processStartTime = SystemClock.elapsedRealtime()
    }
    
    fun markPhase(phase: StartupPhase) {
        val currentTime = SystemClock.elapsedRealtime()
        phases[phase] = currentTime - processStartTime
        
        Logger.d("Startup phase $phase completed in ${phases[phase]}ms")
        
        // 检查是否超过目标时间
        checkPhaseTarget(phase, phases[phase]!!)
    }
    
    private fun checkPhaseTarget(phase: StartupPhase, duration: Long) {
        val target = when (phase) {
            StartupPhase.APPLICATION_CREATE -> 200L
            StartupPhase.FIRST_ACTIVITY_CREATE -> 500L
            StartupPhase.FIRST_FRAME_DRAWN -> StartupTargets.FIRST_FRAME_TARGET
            StartupPhase.INTERACTIVE -> StartupTargets.INTERACTIVE_TARGET
        }
        
        if (duration > target) {
            Logger.w("Startup phase $phase exceeded target: ${duration}ms > ${target}ms")
            TrackManager.trackEvent("startup_slow_phase", mapOf(
                "phase" to phase.name,
                "duration" to duration.toString(),
                "target" to target.toString()
            ))
        }
    }
    
    fun getStartupReport(): StartupReport {
        return StartupReport(
            totalTime = phases[StartupPhase.INTERACTIVE] ?: 0L,
            applicationTime = phases[StartupPhase.APPLICATION_CREATE] ?: 0L,
            firstActivityTime = phases[StartupPhase.FIRST_ACTIVITY_CREATE] ?: 0L,
            firstFrameTime = phases[StartupPhase.FIRST_FRAME_DRAWN] ?: 0L,
            phases = phases.toMap()
        )
    }
}
```

## 📱 Application 优化

### 延迟初始化策略

```kotlin
class BaseApplication : Application() {
    
    @Inject
    lateinit var startupPhaseTracker: StartupPhaseTracker
    
    override fun onCreate() {
        super.onCreate()
        
        // 1. 立即初始化（关键组件）
        initCriticalComponents()
        
        // 2. 延迟初始化（非关键组件）
        scheduleDelayedInitialization()
        
        // 3. 异步初始化（耗时组件）
        scheduleAsyncInitialization()
        
        startupPhaseTracker.markPhase(StartupPhase.APPLICATION_CREATE)
    }
    
    private fun initCriticalComponents() {
        // 只初始化启动必需的组件
        initDependencyInjection()
        initCrashHandler()
        initLogger()
    }
    
    private fun scheduleDelayedInitialization() {
        // 延迟 100ms 初始化非关键组件
        Handler(Looper.getMainLooper()).postDelayed({
            initNonCriticalComponents()
        }, 100)
    }
    
    private fun scheduleAsyncInitialization() {
        // 异步初始化耗时组件
        GlobalScope.launch(Dispatchers.IO) {
            initHeavyComponents()
        }
    }
    
    private fun initNonCriticalComponents() {
        // 埋点统计
        TrackManager.init(DefaultTracker())
        
        // 图片加载库
        initImageLoader()
        
        // 网络监控
        initNetworkMonitor()
    }
    
    private fun initHeavyComponents() {
        // 数据库预热
        warmupDatabase()
        
        // 缓存预加载
        preloadCache()
        
        // 第三方SDK初始化
        initThirdPartySdks()
    }
}
```

### 组件初始化优先级

```kotlin
enum class InitPriority {
    IMMEDIATE,    // 立即初始化
    DELAYED,      // 延迟初始化
    ASYNC,        // 异步初始化
    LAZY          // 懒加载初始化
}

data class ComponentInitializer(
    val name: String,
    val priority: InitPriority,
    val initAction: () -> Unit,
    val dependencies: List<String> = emptyList()
)

class StartupInitializer {
    
    private val initializers = mutableListOf<ComponentInitializer>()
    private val initialized = mutableSetOf<String>()
    
    fun register(initializer: ComponentInitializer) {
        initializers.add(initializer)
    }
    
    fun initializeByPriority(priority: InitPriority) {
        initializers
            .filter { it.priority == priority }
            .sortedBy { it.dependencies.size }
            .forEach { initializer ->
                if (canInitialize(initializer)) {
                    try {
                        val startTime = SystemClock.elapsedRealtime()
                        initializer.initAction()
                        val duration = SystemClock.elapsedRealtime() - startTime
                        
                        initialized.add(initializer.name)
                        Logger.d("Initialized ${initializer.name} in ${duration}ms")
                        
                        if (duration > 100) {
                            Logger.w("Slow initialization: ${initializer.name} took ${duration}ms")
                        }
                    } catch (e: Exception) {
                        Logger.e("Failed to initialize ${initializer.name}", e)
                    }
                }
            }
    }
    
    private fun canInitialize(initializer: ComponentInitializer): Boolean {
        return initializer.dependencies.all { it in initialized }
    }
}
```

## 🏃 Activity 启动优化

### 布局优化

#### 减少布局层次

```xml
<!-- ❌ 错误：嵌套过深 -->
<LinearLayout>
    <RelativeLayout>
        <LinearLayout>
            <TextView />
            <ImageView />
        </LinearLayout>
    </RelativeLayout>
</LinearLayout>

<!-- ✅ 正确：使用 ConstraintLayout 扁平化 -->
<androidx.constraintlayout.widget.ConstraintLayout>
    <TextView
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintStart_toStartOf="parent" />
    
    <ImageView
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintStart_toEndOf="@id/textView" />
</androidx.constraintlayout.widget.ConstraintLayout>
```

#### ViewStub 延迟加载

```xml
<LinearLayout>
    <!-- 立即显示的内容 -->
    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="主要内容" />
    
    <!-- 延迟加载的内容 -->
    <ViewStub
        android:id="@+id/stubComplexView"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout="@layout/layout_complex_view" />
</LinearLayout>
```

```kotlin
class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>() {
    
    override fun initView() {
        // 主要内容立即显示
        setupMainContent()
        
        // 复杂视图延迟加载
        Handler(Looper.getMainLooper()).postDelayed({
            inflateComplexView()
        }, 500)
    }
    
    private fun inflateComplexView() {
        binding.stubComplexView.inflate()
        setupComplexView()
    }
}
```

### 数据加载优化

#### 分阶段加载

```kotlin
@HiltViewModel
class MainViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val newsRepository: NewsRepository
) : BaseViewModel() {
    
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState = _uiState.asStateFlow()
    
    fun loadData() {
        // 第一阶段：加载关键数据
        loadCriticalData()
        
        // 第二阶段：延迟加载非关键数据
        Handler(Looper.getMainLooper()).postDelayed({
            loadNonCriticalData()
        }, 300)
    }
    
    private fun loadCriticalData() {
        launchSafely {
            try {
                // 并行加载用户信息和基础配置
                val userDeferred = async { userRepository.getCurrentUser() }
                val configDeferred = async { configRepository.getAppConfig() }
                
                val user = userDeferred.await()
                val config = configDeferred.await()
                
                _uiState.value = _uiState.value.copy(
                    user = user,
                    config = config,
                    isCriticalDataLoaded = true
                )
                
                // 标记首屏数据加载完成
                startupPhaseTracker.markPhase(StartupPhase.INTERACTIVE)
                
            } catch (e: Exception) {
                Logger.e("Failed to load critical data", e)
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isCriticalDataLoaded = true
                )
            }
        }
    }
    
    private fun loadNonCriticalData() {
        launchSafely {
            try {
                // 加载新闻列表等非关键数据
                val news = newsRepository.getLatestNews()
                _uiState.value = _uiState.value.copy(
                    newsList = news,
                    isAllDataLoaded = true
                )
            } catch (e: Exception) {
                Logger.e("Failed to load non-critical data", e)
            }
        }
    }
}
```

#### 缓存预热

```kotlin
class CacheWarmupManager @Inject constructor(
    private val userRepository: UserRepository,
    private val configRepository: ConfigRepository
) {
    
    fun warmupCache() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                // 预加载用户信息到缓存
                userRepository.preloadUserCache()
                
                // 预加载配置信息到缓存
                configRepository.preloadConfigCache()
                
                // 预加载常用数据
                preloadCommonData()
                
                Logger.d("Cache warmup completed")
            } catch (e: Exception) {
                Logger.e("Cache warmup failed", e)
            }
        }
    }
    
    private suspend fun preloadCommonData() {
        // 预加载城市列表
        cityRepository.preloadCities()
        
        // 预加载分类信息
        categoryRepository.preloadCategories()
    }
}
```

## 📦 资源加载优化

### 图片资源优化

#### WebP 格式使用

```kotlin
// 在 build.gradle.kts 中启用 WebP 支持
android {
    buildFeatures {
        webp {
            lossless = true
            lossy = true
        }
    }
}
```

#### 图片懒加载

```kotlin
class ImageLazyLoader {
    
    fun loadImageWhenVisible(imageView: ImageView, url: String) {
        // 使用 ViewTreeObserver 监听视图可见性
        imageView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                imageView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                
                if (imageView.isVisible()) {
                    imageView.load(url) {
                        placeholder(R.drawable.placeholder)
                        error(R.drawable.error)
                    }
                }
            }
        })
    }
    
    private fun View.isVisible(): Boolean {
        val rect = Rect()
        return getGlobalVisibleRect(rect) && rect.height() > 0 && rect.width() > 0
    }
}
```

### 字体资源优化

```kotlin
// 使用系统字体避免自定义字体加载时间
<TextView
    android:fontFamily="sans-serif-medium"  <!-- 系统字体 -->
    android:text="标题文本" />

// 如果必须使用自定义字体，考虑异步加载
class FontLoader {
    
    fun loadCustomFontAsync(context: Context, callback: (Typeface?) -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val typeface = ResourcesCompat.getFont(context, R.font.custom_font)
                withContext(Dispatchers.Main) {
                    callback(typeface)
                }
            } catch (e: Exception) {
                Logger.e("Failed to load custom font", e)
                withContext(Dispatchers.Main) {
                    callback(null)
                }
            }
        }
    }
}
```

## 📊 启动监控

### 启动性能指标收集

```kotlin
data class StartupMetrics(
    val coldStartTime: Long,
    val warmStartTime: Long,
    val firstFrameTime: Long,
    val interactiveTime: Long,
    val memoryUsage: Long,
    val cpuUsage: Float,
    val timestamp: Long = System.currentTimeMillis()
)

class StartupMetricsCollector {
    
    fun collectStartupMetrics(): StartupMetrics {
        return StartupMetrics(
            coldStartTime = startupPhaseTracker.getColdStartTime(),
            warmStartTime = startupPhaseTracker.getWarmStartTime(),
            firstFrameTime = startupPhaseTracker.getFirstFrameTime(),
            interactiveTime = startupPhaseTracker.getInteractiveTime(),
            memoryUsage = getCurrentMemoryUsage(),
            cpuUsage = getCurrentCpuUsage()
        )
    }
    
    private fun getCurrentMemoryUsage(): Long {
        val runtime = Runtime.getRuntime()
        return runtime.totalMemory() - runtime.freeMemory()
    }
    
    private fun getCurrentCpuUsage(): Float {
        // 获取 CPU 使用率的简化实现
        return 0.0f // 实际实现需要读取 /proc/stat
    }
    
    fun reportStartupMetrics(metrics: StartupMetrics) {
        // 本地存储
        saveMetricsToLocal(metrics)
        
        // 上报到服务器
        if (shouldReportToServer()) {
            reportMetricsToServer(metrics)
        }
        
        // 性能警告
        checkPerformanceThresholds(metrics)
    }
    
    private fun checkPerformanceThresholds(metrics: StartupMetrics) {
        if (metrics.coldStartTime > StartupTargets.COLD_START_TARGET) {
            Logger.w("Cold start time exceeded target: ${metrics.coldStartTime}ms")
            TrackManager.trackEvent("startup_performance_warning", mapOf(
                "type" to "cold_start_slow",
                "duration" to metrics.coldStartTime.toString()
            ))
        }
        
        if (metrics.memoryUsage > 100 * 1024 * 1024) { // 100MB
            Logger.w("High memory usage during startup: ${metrics.memoryUsage / 1024 / 1024}MB")
        }
    }
}
```

### 启动优化效果评估

```kotlin
class StartupOptimizationAnalyzer {
    
    fun analyzeStartupTrends(): StartupTrendReport {
        val recentMetrics = getRecentStartupMetrics(days = 7)
        
        return StartupTrendReport(
            averageColdStart = recentMetrics.map { it.coldStartTime }.average(),
            averageWarmStart = recentMetrics.map { it.warmStartTime }.average(),
            p95ColdStart = recentMetrics.map { it.coldStartTime }.percentile(95),
            improvementRate = calculateImprovementRate(recentMetrics),
            recommendations = generateOptimizationRecommendations(recentMetrics)
        )
    }
    
    private fun generateOptimizationRecommendations(metrics: List<StartupMetrics>): List<String> {
        val recommendations = mutableListOf<String>()
        
        val avgColdStart = metrics.map { it.coldStartTime }.average()
        if (avgColdStart > StartupTargets.COLD_START_TARGET) {
            recommendations.add("冷启动时间过长，建议优化 Application 初始化")
        }
        
        val avgMemory = metrics.map { it.memoryUsage }.average()
        if (avgMemory > 80 * 1024 * 1024) { // 80MB
            recommendations.add("启动时内存使用过高，建议延迟非关键组件初始化")
        }
        
        return recommendations
    }
}
```

## 🎯 启动优化最佳实践

### 1. Application 优化原则
- **最小化 onCreate**: 只初始化启动必需的组件
- **延迟初始化**: 非关键组件延迟到首屏显示后
- **异步初始化**: 耗时操作放到后台线程
- **懒加载**: 按需初始化，避免预加载过多内容

### 2. Activity 优化原则
- **简化布局**: 减少布局层次，使用 ConstraintLayout
- **延迟加载**: 使用 ViewStub 延迟加载复杂视图
- **分阶段加载**: 优先加载关键数据，延迟加载次要数据
- **缓存利用**: 充分利用缓存减少网络请求

### 3. 资源优化原则
- **图片优化**: 使用 WebP 格式，合理的图片尺寸
- **字体优化**: 优先使用系统字体
- **资源压缩**: 启用资源压缩和混淆
- **按需加载**: 避免一次性加载过多资源

### 4. 监控和分析
- **持续监控**: 建立启动性能监控体系
- **数据分析**: 定期分析启动性能数据
- **问题定位**: 快速定位性能瓶颈
- **效果评估**: 评估优化效果并持续改进

---

*启动优化是一个持续的过程，需要在开发过程中时刻关注启动性能，及时发现和解决性能问题。*