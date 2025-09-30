# Android MVVM 开发框架

> 一个可复用、易维护、结构清晰的 Android 应用开发框架，用于快速启动新项目。

## 📋 技术栈

- **语言**: Kotlin
- **架构**: MVVM
- **响应式**: Flow + StateFlow
- **UI**: 传统View体系（XML布局 + ViewBinding）
- **依赖注入**: Hilt
- **网络请求**: Retrofit + OkHttp
- **本地存储**: Room + DataStore
- **图片加载**: Coil
- **最低版本**: API 23 (Android 6.0)

## 🏗️ 项目结构

```
lj.sword.demoappnocompose/
├── app/                    # Application 类
├── base/                   # Base 基类
│   ├── BaseActivity        # Activity 基类
│   ├── BaseFragment        # Fragment 基类
│   ├── BaseViewModel       # ViewModel 基类
│   ├── BaseRepository      # Repository 基类
│   ├── BaseAdapter         # RecyclerView Adapter 基类
│   └── UiState             # UI 状态密封类
├── ui/                     # UI 层
│   ├── activity/           # Activity
│   ├── fragment/           # Fragment
│   └── adapter/            # Adapter
├── data/                   # Data 层
│   ├── repository/         # Repository
│   ├── remote/             # 远程数据源（API）
│   ├── local/              # 本地数据源（Room/DataStore）
│   └── model/              # 数据模型
├── domain/                 # Domain 层（可选）
│   └── usecase/            # UseCase
├── di/                     # Hilt 依赖注入模块
│   ├── NetworkModule       # 网络层依赖
│   ├── DatabaseModule      # 数据库依赖
│   └── DataStoreModule     # DataStore 依赖
├── utils/                  # 工具类
├── ext/                    # Kotlin 扩展函数
├── widget/                 # 自定义 UI 组件
├── constant/               # 常量定义
└── manager/                # 管理类（日志、埋点等）
```

## 🚀 核心功能

### 1. 网络层

- ✅ 统一请求响应封装（`BaseResponse`）
- ✅ 全局错误处理（网络异常、业务异常、Token过期等）
- ✅ 请求/响应日志拦截器（仅Debug环境）
- ✅ 超时配置、重试机制
- ✅ 请求头统一管理（Token、语言、版本号等）
- ✅ 支持多BaseUrl切换

**使用示例**:
```kotlin
// 定义 API 接口
interface ApiService {
    @GET("user/info")
    suspend fun getUserInfo(): BaseResponse<UserInfo>
}

// Repository 中调用
class UserRepository @Inject constructor(
    private val apiService: ApiService
) : BaseRepository() {
    
    fun getUserInfo(): Flow<UserInfo> = executeRequest {
        apiService.getUserInfo()
    }
}
```

### 2. 本地存储

#### DataStore
```kotlin
class MyViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager
) : BaseViewModel() {
    
    fun saveToken(token: String) {
        launchSafely {
            dataStoreManager.saveToken(token)
        }
    }
}
```

#### Room 数据库
```kotlin
@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String
)

@Dao
interface UserDao {
    @Query("SELECT * FROM user")
    fun getAllUsers(): Flow<List<UserEntity>>
}
```

### 3. Base 基类体系

#### BaseActivity
```kotlin
@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>() {
    
    override val viewModel: MainViewModel by viewModels()
    
    override fun createBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }
    
    override fun initView() {
        // 初始化视图
    }
    
    override fun initData() {
        // 加载数据
    }
}
```

#### BaseViewModel
```kotlin
@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: UserRepository
) : BaseViewModel() {
    
    private val _userList = MutableStateFlow<List<User>>(emptyList())
    val userList = _userList.asStateFlow()
    
    fun loadUsers() {
        launchWithLoading {
            repository.getUsers().collect { users ->
                _userList.value = users
            }
        }
    }
}
```

### 4. 自定义 UI 组件

#### StateLayout - 状态布局
```xml
<lj.sword.demoappnocompose.widget.StateLayout
    android:id="@+id/stateLayout"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    
    <!-- 内容视图 -->
    <RecyclerView ... />
    
</lj.sword.demoappnocompose.widget.StateLayout>
```

```kotlin
// 显示加载中
stateLayout.showLoading()

// 显示错误
stateLayout.showError("加载失败")

// 显示空数据
stateLayout.showEmpty("暂无数据")

// 显示内容
stateLayout.showContent()
```

#### LoadingDialog - 加载对话框
```kotlin
LoadingDialog.show(this, "加载中...")
LoadingDialog.dismiss()
```

#### CommonDialog - 通用对话框
```kotlin
CommonDialog.builder(this)
    .setTitle("提示")
    .setContent("确定要删除吗？")
    .setOnConfirmListener {
        // 确认操作
    }
    .show()
```

### 5. 工具类

```kotlin
// 网络状态
NetworkUtil.isNetworkAvailable(context)
NetworkUtil.isWifiConnected(context)

// 屏幕尺寸
ScreenUtil.dp2px(context, 16f)
ScreenUtil.getScreenWidth(context)

// 键盘管理
KeyboardUtil.showKeyboard(editText)
KeyboardUtil.hideKeyboard(activity)

// JSON 解析
val user = JsonUtil.fromJson<User>(jsonString)
val json = JsonUtil.toJson(user)

// 日期格式化
DateUtil.formatTimestamp(timestamp)
DateUtil.getFriendlyTime(timestamp)

// 加密
EncryptUtil.md5("password")
EncryptUtil.aesEncrypt("data", "key")

// 正则验证
RegexUtil.isMobile("13800138000")
RegexUtil.isEmail("test@example.com")

// 文件操作
FileUtil.writeFile(path, content)
FileUtil.readFile(path)
FileUtil.getCacheSize(context)
```

### 6. Kotlin 扩展函数

```kotlin
// View 扩展
view.visible()
view.gone()
view.onSingleClick { /* 防抖点击 */ }

// Context 扩展
context.toast("提示信息")
context.startActivity<MainActivity>()
context.dp2px(16)

// String 扩展
"13800138000".isMobile()
"test@example.com".isEmail()
jsonString.fromJson<User>()

// Flow 扩展
flow.asUiState()
flow.catchError { error -> }
```

### 7. 日志系统

```kotlin
Logger.d("Debug message")
Logger.i("Info message")
Logger.w("Warning message")
Logger.e("Error message", throwable)
Logger.json(jsonString)
Logger.network(url, "GET", params)
```

### 8. 埋点统计

```kotlin
// 初始化（在 Application 中）
TrackManager.init(DefaultTracker())

// 页面浏览
TrackManager.trackPageView("MainActivity")

// 事件埋点
TrackManager.trackEvent("click_button", mapOf("button" to "login"))

// 异常埋点
TrackManager.trackException(throwable, "登录失败")
```

### 9. 全局异常处理

框架已自动初始化全局异常捕获，崩溃日志会自动保存到本地。

```kotlin
// 获取崩溃日志
val logs = CrashHandler.getInstance().getCrashLogFiles()

// 清除崩溃日志
CrashHandler.getInstance().clearCrashLogs()
```

### 10. 国际化支持

框架已配置中英文资源，可动态切换语言：

```kotlin
// 切换语言
LanguageManager.updateLanguage(context, LanguageManager.LANGUAGE_CHINESE)
LanguageManager.updateLanguage(context, LanguageManager.LANGUAGE_ENGLISH)

// 获取当前语言
val currentLanguage = LanguageManager.getCurrentLanguage()
```

### 11. 多环境配置

框架已配置三个环境：

- **Debug**: 开发环境
- **Staging**: 测试环境
- **Release**: 生产环境

```kotlin
// 在代码中使用
val baseUrl = BuildConfig.BASE_URL
val environment = BuildConfig.ENVIRONMENT
```

## 📦 如何使用

### 1. 克隆项目
```bash
git clone <repository-url>
```

### 2. 修改包名和应用名
- 修改 `app/build.gradle.kts` 中的 `namespace` 和 `applicationId`
- 全局替换包名 `lj.sword.demoappnocompose`
- 修改 `strings.xml` 中的 `app_name`

### 3. 配置 BaseUrl
在 `app/build.gradle.kts` 中修改各环境的 `BASE_URL`

### 4. 开始开发
按照 MVVM 架构模式开发新功能

## 🎯 开发规范

### 命名规范
- **Activity**: `XxxActivity`
- **Fragment**: `XxxFragment`
- **ViewModel**: `XxxViewModel`
- **Repository**: `XxxRepository`
- **Adapter**: `XxxAdapter`

### 资源文件规范
- **Layout**: `activity_xxx` / `fragment_xxx` / `item_xxx`
- **Drawable**: `ic_xxx` / `bg_xxx` / `shape_xxx`
- **Color**: 统一在 `colors.xml` 中管理
- **String**: 统一在 `strings.xml` 中管理，支持国际化

### 代码注释规范
关键类和方法需要 KDoc 注释：

```kotlin
/**
 * 用户仓库
 * 负责用户相关数据的获取和存储
 * 
 * @author Sword
 * @since 1.0.0
 */
class UserRepository { }
```

## 🔧 依赖版本

| 依赖 | 版本 |
|------|------|
| Kotlin | 2.0.21 |
| AGP | 8.10.1 |
| Hilt | 2.52 |
| Retrofit | 2.11.0 |
| Room | 2.6.1 |
| Coil | 2.7.0 |
| Coroutines | 1.9.0 |

## 📄 License

MIT License

## 👨‍💻 作者

Sword

---

**祝你开发愉快！** 🎉
