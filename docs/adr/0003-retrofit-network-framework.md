# ADR-0003: 选择 Retrofit + OkHttp 作为网络框架

## 状态
已接受

## 背景
Android 应用需要与后端 API 进行网络通信，需要选择一个稳定、高效、易用的网络请求框架。

## 决策
我们决定使用 Retrofit + OkHttp 作为项目的网络请求框架。

## 理由

### Retrofit 优势
1. **类型安全**: 编译时检查 API 接口定义
2. **注解驱动**: 使用注解定义 HTTP 请求，代码简洁
3. **转换器支持**: 内置支持 Gson、Moshi 等 JSON 转换器
4. **协程支持**: 原生支持 Kotlin 协程，异步编程友好
5. **拦截器机制**: 支持请求/响应拦截器，便于统一处理

### OkHttp 优势
1. **高性能**: 连接池复用，HTTP/2 支持
2. **缓存机制**: 内置 HTTP 缓存支持
3. **拦截器链**: 强大的拦截器机制
4. **WebSocket 支持**: 支持 WebSocket 长连接
5. **广泛使用**: 业界标准，生态完善

### 与其他框架的比较
- **Volley**: Google 官方框架，但功能相对简单，不支持协程
- **HttpURLConnection**: 原生 API，使用复杂，功能有限
- **Ktor Client**: Kotlin 原生，但生态不如 Retrofit 成熟

## 实现细节

### 基础配置
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(LoggingInterceptor())
            .addInterceptor(HeaderInterceptor())
            .build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
```

### API 接口定义
```kotlin
interface ApiService {
    @GET("users/{id}")
    suspend fun getUser(@Path("id") userId: String): Response<User>
    
    @POST("users")
    suspend fun createUser(@Body user: CreateUserRequest): Response<User>
    
    @PUT("users/{id}")
    suspend fun updateUser(
        @Path("id") userId: String,
        @Body user: UpdateUserRequest
    ): Response<User>
}
```

### 统一响应处理
```kotlin
data class BaseResponse<T>(
    val code: Int,
    val message: String,
    val data: T?
)

abstract class BaseRepository {
    protected suspend fun <T> executeRequest(
        request: suspend () -> Response<BaseResponse<T>>
    ): Flow<T> = flow {
        val response = request()
        if (response.isSuccessful) {
            val body = response.body()
            if (body?.code == 200) {
                body.data?.let { emit(it) }
            } else {
                throw BusinessException(body?.message ?: "Unknown error")
            }
        } else {
            throw NetworkException.HttpError(response.code())
        }
    }.flowOn(Dispatchers.IO)
}
```

### 拦截器实现
```kotlin
class HeaderInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")
            .addHeader("User-Agent", "Android/${BuildConfig.VERSION_NAME}")
            .build()
        return chain.proceed(request)
    }
}

class LoggingInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val startTime = System.nanoTime()
        
        Logger.d("Request: ${request.method} ${request.url}")
        
        val response = chain.proceed(request)
        val endTime = System.nanoTime()
        
        Logger.d("Response: ${response.code} in ${(endTime - startTime) / 1e6}ms")
        
        return response
    }
}
```

## 后果

### 正面影响
- 网络请求代码简洁易维护
- 类型安全，减少运行时错误
- 性能优秀，支持连接池复用
- 拦截器机制便于统一处理
- 协程支持，异步编程友好

### 负面影响
- 增加了依赖库的大小
- 学习成本相对较高
- 需要处理网络异常和错误

### 风险缓解
- 建立统一的网络请求基类
- 实现完善的异常处理机制
- 提供网络请求的最佳实践文档
- 定期更新依赖版本

## 配置示例

### Gradle 依赖
```kotlin
implementation("com.squareup.retrofit2:retrofit:2.11.0")
implementation("com.squareup.retrofit2:converter-gson:2.11.0")
implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
```

### ProGuard 配置
```proguard
# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

# OkHttp
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
```

## 相关决策
- ADR-0001: 选择 MVVM 架构模式
- ADR-0002: 选择 Hilt 作为依赖注入框架
- ADR-0005: 异常处理策略设计

---
*创建日期: 2024-01-01*  
*最后更新: 2024-01-01*  
*决策者: 开发团队*