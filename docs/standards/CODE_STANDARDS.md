# 代码规范

本文档定义了项目的代码编写规范和最佳实践，确保代码的一致性、可读性和可维护性。

## 📋 目录

- [Kotlin 代码规范](#kotlin-代码规范)
- [Android 特定规范](#android-特定规范)
- [架构规范](#架构规范)
- [注释规范](#注释规范)
- [资源文件规范](#资源文件规范)
- [代码质量工具](#代码质量工具)

## 🎯 Kotlin 代码规范

### 命名规范

#### 类和接口
```kotlin
// ✅ 正确：使用 PascalCase
class UserRepository
interface ApiService
data class LoginRequest

// ❌ 错误
class userRepository
interface apiService
```

#### 函数和变量
```kotlin
// ✅ 正确：使用 camelCase
fun getUserInfo()
val userName: String
var isLoading: Boolean

// ❌ 错误
fun GetUserInfo()
val user_name: String
```

#### 常量
```kotlin
// ✅ 正确：使用 UPPER_SNAKE_CASE
const val MAX_RETRY_COUNT = 3
const val DEFAULT_TIMEOUT = 30L

// ❌ 错误
const val maxRetryCount = 3
const val defaultTimeout = 30L
```

#### 包名
```kotlin
// ✅ 正确：全小写，用点分隔
package lj.sword.demoappnocompose.data.repository
package lj.sword.demoappnocompose.ui.activity

// ❌ 错误
package lj.sword.demoappnocompose.Data.Repository
```

### 代码格式

#### 缩进和空格
- 使用 4 个空格缩进，不使用 Tab
- 操作符前后加空格
- 逗号后加空格

```kotlin
// ✅ 正确
val result = a + b
val list = listOf(1, 2, 3)
if (condition) {
    doSomething()
}

// ❌ 错误
val result=a+b
val list = listOf(1,2,3)
if(condition){
    doSomething()
}
```

#### 行长度
- 每行最多 120 个字符
- 超长行需要换行

```kotlin
// ✅ 正确：参数换行
fun longFunctionName(
    parameter1: String,
    parameter2: Int,
    parameter3: Boolean
): String {
    return "result"
}

// ✅ 正确：链式调用换行
repository.getUserInfo()
    .map { user -> user.name }
    .catch { e -> handleError(e) }
    .collect { name -> updateUI(name) }
```

#### 大括号
- 左大括号不换行
- 右大括号单独一行

```kotlin
// ✅ 正确
if (condition) {
    doSomething()
} else {
    doSomethingElse()
}

// ❌ 错误
if (condition)
{
    doSomething()
}
else {
    doSomethingElse()
}
```

### 语言特性

#### 可空性
```kotlin
// ✅ 正确：明确可空性
fun processUser(user: User?) {
    user?.let { 
        // 处理非空用户
    }
}

// ✅ 正确：使用安全调用
val name = user?.profile?.name ?: "Unknown"

// ❌ 错误：强制解包
val name = user!!.profile!!.name
```

#### 数据类
```kotlin
// ✅ 正确：使用数据类
data class User(
    val id: String,
    val name: String,
    val email: String?
)

// ✅ 正确：参数换行
data class LoginRequest(
    val username: String,
    val password: String,
    val rememberMe: Boolean = false,
    val deviceId: String? = null
)
```

#### 扩展函数
```kotlin
// ✅ 正确：有意义的扩展
fun String.isEmail(): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun View.visible() {
    visibility = View.VISIBLE
}

// ❌ 错误：无意义的扩展
fun String.doNothing(): String = this
```

## 📱 Android 特定规范

### Activity 和 Fragment

#### 生命周期方法顺序
```kotlin
class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>() {
    
    // 1. 伴生对象和常量
    companion object {
        private const val EXTRA_USER_ID = "user_id"
    }
    
    // 2. 属性声明
    override val viewModel: MainViewModel by viewModels()
    private lateinit var adapter: UserAdapter
    
    // 3. 生命周期方法（按调用顺序）
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    
    override fun onStart() {
        super.onStart()
    }
    
    override fun onResume() {
        super.onResume()
    }
    
    // 4. 重写的抽象方法
    override fun createBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }
    
    override fun initView() {
        // 初始化视图
    }
    
    override fun initData() {
        // 加载数据
    }
    
    // 5. 私有方法
    private fun setupRecyclerView() {
        // 设置 RecyclerView
    }
}
```

### ViewModel

#### 状态管理
```kotlin
@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository
) : BaseViewModel() {
    
    // ✅ 正确：使用私有 MutableStateFlow
    private val _uiState = MutableStateFlow<UiState<List<User>>>(UiState.Empty())
    val uiState: StateFlow<UiState<List<User>>> = _uiState.asStateFlow()
    
    // ✅ 正确：使用 launchWithLoading
    fun loadUsers() {
        launchWithLoading {
            userRepository.getUsers()
                .catch { e ->
                    _uiState.value = UiState.Error(message = e.message ?: "加载失败")
                }
                .collect { users ->
                    _uiState.value = if (users.isEmpty()) {
                        UiState.Empty()
                    } else {
                        UiState.Success(users)
                    }
                }
        }
    }
}
```

### Repository

#### 数据访问模式
```kotlin
@Singleton
class UserRepository @Inject constructor(
    private val apiService: ApiService,
    private val userDao: UserDao,
    private val memoryCache: MemoryCache<String, User>
) : BaseRepository() {
    
    // ✅ 正确：缓存优先策略
    fun getUser(userId: String): Flow<User> = flow {
        // 1. 检查内存缓存
        memoryCache.get(userId)?.let { cachedUser ->
            emit(cachedUser)
            return@flow
        }
        
        try {
            // 2. 网络请求
            val response = apiService.getUser(userId)
            if (response.isSuccess()) {
                val user = response.data!!
                // 更新缓存
                memoryCache.put(userId, user)
                userDao.insertUser(user.toEntity())
                emit(user)
            } else {
                throw ApiException(response.code, response.message)
            }
        } catch (e: Exception) {
            // 3. 降级到本地数据
            val localUser = userDao.getUser(userId)?.toDomainModel()
            if (localUser != null) {
                emit(localUser)
            } else {
                throw e
            }
        }
    }.flowOn(Dispatchers.IO)
}
```

## 🏗️ 架构规范

### 分层原则

#### 依赖方向
```
UI Layer (Activity/Fragment/ViewModel)
    ↓
Domain Layer (UseCase/Repository Interface)
    ↓
Data Layer (Repository Implementation/DataSource)
```

#### 数据流
```kotlin
// ✅ 正确：单向数据流
UI → ViewModel → UseCase → Repository → DataSource
UI ← ViewModel ← UseCase ← Repository ← DataSource
```

### 错误处理

#### 统一异常处理
```kotlin
// ✅ 正确：使用 BaseRepository 的 executeRequest
fun getUsers(): Flow<List<User>> = executeRequest {
    apiService.getUsers()
}

// ✅ 正确：在 ViewModel 中处理异常
fun loadUsers() {
    launchWithLoading {
        userRepository.getUsers()
            .catch { e ->
                when (e) {
                    is NetworkException -> handleNetworkError(e)
                    is BusinessException -> handleBusinessError(e)
                    else -> handleUnknownError(e)
                }
            }
            .collect { users ->
                _uiState.value = UiState.Success(users)
            }
    }
}
```

## 📝 注释规范

### KDoc 注释

#### 类注释
```kotlin
/**
 * 用户仓库
 * 
 * 负责用户相关数据的获取、缓存和存储。
 * 实现了三级缓存策略：内存缓存 → 网络请求 → 本地数据库
 * 
 * @author Sword
 * @since 1.0.0
 */
@Singleton
class UserRepository @Inject constructor(
    private val apiService: ApiService,
    private val userDao: UserDao
) : BaseRepository()
```

#### 函数注释
```kotlin
/**
 * 获取用户信息
 * 
 * @param userId 用户ID
 * @param forceRefresh 是否强制刷新，默认为 false
 * @return 用户信息流
 * @throws UserNotFoundException 当用户不存在时
 * @throws NetworkException 当网络请求失败时
 */
suspend fun getUser(
    userId: String, 
    forceRefresh: Boolean = false
): Flow<User>
```

#### 复杂逻辑注释
```kotlin
fun processUserData(users: List<User>): List<User> {
    return users
        // 过滤掉已删除的用户
        .filter { !it.isDeleted }
        // 按最后登录时间排序
        .sortedByDescending { it.lastLoginTime }
        // 只取前20个活跃用户
        .take(20)
        // 补充用户头像信息
        .map { user ->
            user.copy(
                avatar = user.avatar ?: getDefaultAvatar(user.gender)
            )
        }
}
```

### TODO 注释
```kotlin
// TODO: 优化缓存策略，考虑使用 LRU 算法
// FIXME: 修复在低内存设备上的崩溃问题
// HACK: 临时解决方案，等待后端接口修复
// NOTE: 这里的逻辑比较复杂，需要仔细理解业务需求
```

## 📁 资源文件规范

### 布局文件

#### 命名规范
```
activity_main.xml       # Activity 布局
fragment_user.xml       # Fragment 布局
item_user.xml          # RecyclerView Item 布局
layout_loading.xml     # 可复用的布局
dialog_confirm.xml     # 对话框布局
```

#### 布局结构
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    tools:context=".ui.activity.MainActivity">

    <!-- 标题栏 -->
    <include layout="@layout/layout_title_bar" />

    <!-- 内容区域 -->
    <FrameLayout
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1">

        <!-- 主要内容 -->
        <RecyclerView
            android:id="@+id/recyclerView"
            android:layout_width="match_parent"
            android:layout_height="match_parent" />

        <!-- 状态布局 -->
        <include layout="@layout/layout_state_loading" />

    </FrameLayout>

</LinearLayout>
```

### 资源命名

#### 颜色资源
```xml
<!-- colors.xml -->
<resources>
    <!-- 主题色 -->
    <color name="colorPrimary">#2196F3</color>
    <color name="colorPrimaryDark">#1976D2</color>
    <color name="colorAccent">#03DAC5</color>
    
    <!-- 文本颜色 -->
    <color name="textColorPrimary">#212121</color>
    <color name="textColorSecondary">#757575</color>
    <color name="textColorHint">#BDBDBD</color>
    
    <!-- 背景颜色 -->
    <color name="backgroundColor">#FAFAFA</color>
    <color name="cardBackgroundColor">#FFFFFF</color>
    
    <!-- 状态颜色 -->
    <color name="colorSuccess">#4CAF50</color>
    <color name="colorWarning">#FF9800</color>
    <color name="colorError">#F44336</color>
</resources>
```

#### 字符串资源
```xml
<!-- strings.xml -->
<resources>
    <string name="app_name">Demo App</string>
    
    <!-- 通用 -->
    <string name="confirm">确定</string>
    <string name="cancel">取消</string>
    <string name="loading">加载中…</string>
    <string name="retry">重试</string>
    
    <!-- 错误提示 -->
    <string name="error_network">网络连接失败</string>
    <string name="error_server">服务器错误</string>
    <string name="error_unknown">未知错误</string>
    
    <!-- 登录相关 -->
    <string name="login_title">登录</string>
    <string name="login_username_hint">请输入用户名</string>
    <string name="login_password_hint">请输入密码</string>
</resources>
```

#### 尺寸资源
```xml
<!-- dimens.xml -->
<resources>
    <!-- 间距 -->
    <dimen name="spacing_tiny">4dp</dimen>
    <dimen name="spacing_small">8dp</dimen>
    <dimen name="spacing_medium">16dp</dimen>
    <dimen name="spacing_large">24dp</dimen>
    <dimen name="spacing_huge">32dp</dimen>
    
    <!-- 字体大小 -->
    <dimen name="text_size_small">12sp</dimen>
    <dimen name="text_size_medium">14sp</dimen>
    <dimen name="text_size_large">16sp</dimen>
    <dimen name="text_size_title">18sp</dimen>
    
    <!-- 组件尺寸 -->
    <dimen name="button_height">48dp</dimen>
    <dimen name="toolbar_height">56dp</dimen>
    <dimen name="card_corner_radius">8dp</dimen>
</resources>
```

## 🔧 代码质量工具

### ktlint 配置

#### 自动格式化
```bash
# 格式化所有 Kotlin 文件
./gradlew ktlintFormat

# 检查代码格式
./gradlew ktlintCheck
```

#### IDE 集成
在 Android Studio 中安装 ktlint 插件，并配置自动格式化。

### detekt 配置

#### 静态代码分析
```bash
# 运行 detekt 分析
./gradlew detekt

# 生成报告
./gradlew detektMain
```

#### 自定义规则
在 `config/detekt/detekt.yml` 中配置规则：

```yaml
complexity:
  CyclomaticComplexMethod:
    threshold: 15
  LongMethod:
    threshold: 60
  LongParameterList:
    threshold: 6

naming:
  ClassNaming:
    classPattern: '[A-Z$][a-zA-Z0-9$]*'
  FunctionNaming:
    functionPattern: '^([a-z$][a-zA-Z$0-9]*)|(`.*`)$'
```

### 代码检查清单

#### 提交前检查
- [ ] 代码格式符合 ktlint 规范
- [ ] 通过 detekt 静态分析
- [ ] 所有单元测试通过
- [ ] 没有编译警告
- [ ] 添加了必要的注释
- [ ] 更新了相关文档

#### 代码审查要点
- [ ] 架构设计合理
- [ ] 异常处理完善
- [ ] 性能考虑充分
- [ ] 安全性检查
- [ ] 可测试性良好
- [ ] 代码可读性强

## 📚 参考资源

- [Kotlin 官方编码规范](https://kotlinlang.org/docs/coding-conventions.html)
- [Android 开发者指南](https://developer.android.com/guide)
- [ktlint 规则文档](https://ktlint.github.io/)
- [detekt 规则文档](https://detekt.github.io/detekt/)

---

*本文档会随着项目发展持续更新，请定期查看最新版本。*