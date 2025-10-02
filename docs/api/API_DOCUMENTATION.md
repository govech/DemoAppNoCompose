# API 文档

## 概述
本文档描述了 Android MVVM 框架中的核心 API 接口和使用方法。

## 核心 API

### 1. 配置管理 API

#### AppConfig
应用配置管理类，提供统一的配置管理功能。

```kotlin
class AppConfig private constructor()
```

##### 初始化
```kotlin
// 基础初始化
AppConfig.init(context)

// 自定义配置初始化
AppConfig.init(context) {
    network {
        baseUrl = "https://api.example.com/"
        connectTimeout = 30
        enableNetworkLog = true
    }
    ui {
        enableDarkMode = true
        primaryColor = 0xFF2196F3.toInt()
    }
    cache {
        memoryCacheSize = 50
        diskCacheSize = 200
    }
}
```

##### 获取配置
```kotlin
val config = AppConfig.getInstance()
val baseUrl = config.baseUrl
val cacheSize = config.memoryCacheSize
```

##### 配置选项

###### 网络配置
| 属性 | 类型 | 默认值 | 描述 |
|------|------|--------|------|
| baseUrl | String | "https://api.example.com/" | API 基础URL |
| connectTimeout | Long | 30 | 连接超时时间（秒） |
| readTimeout | Long | 30 | 读取超时时间（秒） |
| writeTimeout | Long | 30 | 写入超时时间（秒） |
| enableNetworkLog | Boolean | BuildConfig.DEBUG | 是否启用网络日志 |
| maxRetryCount | Int | 3 | 最大重试次数 |

###### UI配置
| 属性 | 类型 | 默认值 | 描述 |
|------|------|--------|------|
| enableDarkMode | Boolean | false | 是否启用暗黑模式 |
| primaryColor | Int | 0xFF2196F3 | 主题色 |
| accentColor | Int | 0xFF03DAC5 | 强调色 |
| defaultFontSize | Float | 14f | 默认字体大小 |
| enableAnimation | Boolean | true | 是否启用动画 |
| animationDuration | Long | 300 | 动画持续时间（毫秒） |

### 2. 异常处理 API

#### AppException
应用异常基类，使用密封类定义所有异常类型。

```kotlin
sealed class AppException(
    override val message: String,
    override val cause: Throwable? = null,
    val code: Int = 0
) : Exception(message, cause)
```

##### 网络异常
```kotlin
// 连接错误
NetworkException.ConnectionError(cause)

// 超时错误
NetworkException.TimeoutError(cause)

// 服务器错误
NetworkException.ServerError(message, cause, code)

// HTTP错误
NetworkException.HttpError(httpCode, message, cause)

// 解析错误
NetworkException.ParseError(cause)

// 无网络连接
NetworkException.NoNetworkError()
```

##### 数据库异常
```kotlin
// 连接错误
DatabaseException.ConnectionError(cause)

// 插入错误
DatabaseException.InsertError(cause)

// 查询错误
DatabaseException.QueryError(cause)

// 更新错误
DatabaseException.UpdateError(cause)

// 删除错误
DatabaseException.DeleteError(cause)
```

##### 业务异常
```kotlin
// 用户未登录
BusinessException.NotLoginError()

// 权限不足
BusinessException.PermissionDeniedError(message)

// 参数错误
BusinessException.InvalidParameterError(message)

// 用户不存在
BusinessException.UserNotFoundError()

// 密码错误
BusinessException.WrongPasswordError()
```

##### 异常处理方法
```kotlin
// 获取用户友好的错误消息
exception.getUserFriendlyMessage(): String

// 判断是否可以重试
exception.isRetryable(): Boolean

// 获取重试延迟时间
exception.getRetryDelay(): Long
```

### 3. 性能监控 API

#### PerformanceMonitor
性能监控管理器，统一管理各种性能监控组件。

```kotlin
@Singleton
class PerformanceMonitor @Inject constructor()
```

##### 初始化
```kotlin
performanceMonitor.initialize(application)
```

##### 启动性能监控
```kotlin
// 标记Application创建完成
performanceMonitor.markApplicationCreated()

// 标记启动完成
performanceMonitor.markStartupCompleted()
```

##### 获取性能报告
```kotlin
val report = performanceMonitor.getPerformanceReport()
```

##### 执行性能优化
```kotlin
performanceMonitor.performOptimization()
```

#### StartupTimeTracker
启动时间追踪器。

```kotlin
class StartupTimeTracker : Application.ActivityLifecycleCallbacks
```

##### 启动监听器
```kotlin
interface StartupListener {
    fun onStartupPhaseCompleted(phase: StartupPhase, duration: Long)
    fun onStartupCompleted(totalDuration: Long)
}

startupTimeTracker.addStartupListener(listener)
```

##### 启动阶段
```kotlin
enum class StartupPhase {
    APPLICATION_CREATE,    // Application创建
    FIRST_ACTIVITY_CREATE, // 首个Activity创建
    FIRST_ACTIVITY_RESUME, // 首个Activity恢复
    STARTUP_COMPLETED      // 启动完成
}
```

#### MemoryMonitor
内存监控器。

```kotlin
@Singleton
class MemoryMonitor @Inject constructor()
```

##### 内存监控
```kotlin
// 开始监控
memoryMonitor.startMonitoring()

// 停止监控
memoryMonitor.stopMonitoring()

// 执行内存清理
memoryMonitor.performMemoryCleanup()
```

##### 内存监听器
```kotlin
interface MemoryListener {
    fun onMemoryWarning(memoryInfo: MemoryInfo)
    fun onMemoryPressure(memoryInfo: MemoryInfo, level: MemoryPressureLevel)
    fun onLowMemory(memoryInfo: MemoryInfo)
}

memoryMonitor.addMemoryListener(listener)
```

##### 内存信息
```kotlin
data class MemoryInfo(
    val totalMemoryMB: Long,      // 总内存
    val availableMemoryMB: Long,  // 可用内存
    val usedMemoryMB: Long,       // 已用内存
    val usagePercentage: Float,   // 使用率
    val isLowMemory: Boolean      // 是否低内存
)
```

### 4. 内存管理 API

#### MemoryCache
LRU内存缓存。

```kotlin
class MemoryCache<K, V>(
    maxSize: Int,
    expireTimeMs: Long = Long.MAX_VALUE
)
```

##### 缓存操作
```kotlin
// 创建缓存
val cache = MemoryCache<String, User>(maxSize = 100, expireTimeMs = 60000)

// 存储数据
cache.put(key, value)

// 获取数据
val value = cache.get(key)

// 移除数据
cache.remove(key)

// 清空缓存
cache.clear()

// 获取缓存大小
val size = cache.size()
```

#### NetworkObjectPool
网络对象池。

```kotlin
object NetworkObjectPool
```

##### 对象池操作
```kotlin
// 获取Request.Builder
val builder = NetworkObjectPool.acquireRequestBuilder()

// 释放Request.Builder
NetworkObjectPool.releaseRequestBuilder(builder)

// 获取StringBuilder
val stringBuilder = NetworkObjectPool.acquireStringBuilder()

// 释放StringBuilder
NetworkObjectPool.releaseStringBuilder(stringBuilder)
```

#### ImageCacheManager
图片缓存管理器。

```kotlin
@Singleton
class ImageCacheManager @Inject constructor()
```

##### 缓存管理
```kotlin
// 清理内存缓存
imageCacheManager.clearMemoryCache()

// 清理磁盘缓存
imageCacheManager.clearDiskCache()

// 获取缓存大小
val memorySize = imageCacheManager.getMemoryCacheSize()
val diskSize = imageCacheManager.getDiskCacheSize()

// 设置内存压力监听
imageCacheManager.setMemoryPressureListener { level ->
    // 处理内存压力
}
```

### 5. 异常恢复 API

#### ExceptionHandler
统一异常处理器。

```kotlin
object ExceptionHandler
```

##### 异常处理
```kotlin
// 处理异常
val result = ExceptionHandler.handle(exception) { recoveryAction ->
    // 执行恢复操作
}

// 注册异常恢复策略
ExceptionHandler.registerRecoveryStrategy(
    NetworkException::class,
    NetworkExceptionRecovery()
)
```

#### NetworkExceptionRecovery
网络异常恢复策略。

```kotlin
class NetworkExceptionRecovery : ExceptionRecoveryStrategy<NetworkException>
```

##### 恢复操作
```kotlin
// 自动重试
val result = networkExceptionRecovery.recover(exception) {
    // 重试操作
    performNetworkRequest()
}
```

## 使用示例

### 完整的 ViewModel 示例
```kotlin
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val exceptionHandler: ExceptionHandler
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val result = loginUseCase(username, password)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    user = result
                )
            } catch (exception: AppException) {
                val errorMessage = exceptionHandler.handle(exception) {
                    // 异常恢复逻辑
                    loginUseCase(username, password)
                }
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = errorMessage
                )
            }
        }
    }
}
```

### Repository 实现示例
```kotlin
@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userApi: UserApi,
    private val userDao: UserDao,
    private val memoryCache: MemoryCache<String, User>
) : UserRepository {
    
    override suspend fun getUser(userId: String): Result<User> {
        return try {
            // 先从缓存获取
            memoryCache.get(userId)?.let { cachedUser ->
                return Result.success(cachedUser)
            }
            
            // 从网络获取
            val response = userApi.getUser(userId)
            if (response.isSuccessful) {
                val user = response.body()!!
                
                // 缓存数据
                memoryCache.put(userId, user)
                userDao.insertUser(user.toEntity())
                
                Result.success(user)
            } else {
                throw NetworkException.HttpError(response.code())
            }
        } catch (e: Exception) {
            // 从本地数据库获取
            val localUser = userDao.getUser(userId)?.toDomainModel()
            if (localUser != null) {
                Result.success(localUser)
            } else {
                Result.failure(e)
            }
        }
    }
}
```

## 错误码参考

### 网络异常错误码
| 错误码 | 异常类型 | 描述 |
|--------|----------|------|
| 1001 | ConnectionError | 网络连接失败 |
| 1002 | TimeoutError | 请求超时 |
| 1003 | ServerError | 服务器错误 |
| 1004 | ParseError | 数据解析失败 |
| 1005 | NoNetworkError | 无网络连接 |

### 数据库异常错误码
| 错误码 | 异常类型 | 描述 |
|--------|----------|------|
| 2001 | ConnectionError | 数据库连接失败 |
| 2002 | InsertError | 数据插入失败 |
| 2003 | QueryError | 数据查询失败 |
| 2004 | UpdateError | 数据更新失败 |
| 2005 | DeleteError | 数据删除失败 |

### 业务异常错误码
| 错误码 | 异常类型 | 描述 |
|--------|----------|------|
| 3001 | NotLoginError | 用户未登录 |
| 3002 | PermissionDeniedError | 权限不足 |
| 3003 | InvalidParameterError | 参数错误 |
| 3004 | UserNotFoundError | 用户不存在 |
| 3005 | WrongPasswordError | 密码错误 |

---
*最后更新: 2024-01-01*