# ADR-0002: 选择 Hilt 作为依赖注入框架

## 状态
已接受

## 背景
在 MVVM 架构中，需要一个依赖注入框架来管理对象的创建和依赖关系，减少样板代码，提高代码的可测试性。

## 决策
我们决定使用 Hilt 作为项目的依赖注入框架。

## 理由

### 优势
1. **官方支持**: Google 官方推荐的 Android 依赖注入解决方案
2. **基于 Dagger**: 继承了 Dagger 的性能优势，编译时生成代码
3. **Android 集成**: 原生支持 Android 组件（Activity、Fragment、Service等）
4. **简化配置**: 相比 Dagger，配置更简单，学习成本更低
5. **作用域管理**: 提供完善的作用域管理机制

### 与其他框架的比较
- **Dagger**: 配置复杂，学习成本高
- **Koin**: 运行时反射，性能较差
- **Kodein**: 社区支持有限

## 实现细节

### 核心注解
- `@HiltAndroidApp`: 应用程序入口
- `@AndroidEntryPoint`: Android 组件注入点
- `@Module`: 依赖提供模块
- `@InstallIn`: 模块安装范围
- `@Provides`: 依赖提供方法
- `@Binds`: 接口绑定实现

### 模块结构
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideAppConfig(context: Context): AppConfig {
        return AppConfig.getInstance()
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository
}
```

### 作用域管理
- `@Singleton`: 应用级单例
- `@ActivityScoped`: Activity 级作用域
- `@FragmentScoped`: Fragment 级作用域
- `@ViewModelScoped`: ViewModel 级作用域

## 后果

### 正面影响
- 减少样板代码，提高开发效率
- 提高代码的可测试性
- 统一的依赖管理机制
- 编译时检查，减少运行时错误

### 负面影响
- 增加编译时间
- 生成的代码较多，增加 APK 大小
- 调试时难以追踪依赖关系

### 风险缓解
- 合理设计模块结构，避免循环依赖
- 使用 Hilt 测试工具进行单元测试
- 定期清理无用的依赖

## 配置示例

### Application 配置
```kotlin
@HiltAndroidApp
class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 初始化配置
    }
}
```

### Activity 配置
```kotlin
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
}
```

### ViewModel 配置
```kotlin
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {
    // ViewModel 实现
}
```

## 相关决策
- ADR-0001: 选择 MVVM 架构模式
- ADR-0003: 选择 Retrofit + OkHttp 作为网络框架

---
*创建日期: 2024-01-01*  
*最后更新: 2024-01-01*  
*决策者: 开发团队*