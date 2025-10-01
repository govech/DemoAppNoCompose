# 登录功能示例说明

## 📱 功能演示

本示例实现了一个完整的登录功能，展示了框架的主要特性：

### ✅ 已实现的功能

1. **MVVM 架构**
   - LoginActivity（View 层）
   - LoginViewModel（ViewModel 层）
   - LoginRepository（数据层）

2. **网络请求**
   - API 接口定义（ApiService）
   - 统一的请求响应封装（BaseResponse）
   - 异常处理（ApiException）

3. **状态管理**
   - UiState 状态管理（Loading/Success/Error/Empty）
   - StateFlow 响应式数据流

4. **本地存储**
   - DataStore 存储用户信息（Token、UserId）

5. **UI 组件**
   - LoadingDialog 加载框
   - Toast 提示
   - 防抖点击（onSingleClick）

6. **埋点统计**
   - 页面浏览埋点
   - 登录按钮点击埋点
   - 登录成功/失败埋点

7. **日志系统**
   - 登录流程日志记录

## 📂 文件结构

```
app/src/main/java/lj/sword/demoappnocompose/
├── data/
│   ├── model/
│   │   ├── LoginRequest.kt       # 登录请求模型
│   │   └── LoginResponse.kt      # 登录响应模型
│   ├── remote/
│   │   └── ApiService.kt         # API 接口（已添加 login 方法）
│   └── repository/
│       └── LoginRepository.kt    # 登录仓库
├── ui/
│   └── activity/
│       ├── LoginActivity.kt      # 登录页面
│       └── LoginViewModel.kt     # 登录 ViewModel
├── di/
│   └── RepositoryModule.kt       # 依赖注入（已添加 LoginRepository）
└── AndroidManifest.xml           # 已注册 LoginActivity

app/src/main/res/layout/
└── activity_login.xml            # 登录页面布局
```

## 🎯 代码详解

### 1. 数据模型

**LoginRequest.kt** - 登录请求参数
```kotlin
data class LoginRequest(
    val username: String,
    val password: String
)
```

**LoginResponse.kt** - 登录响应数据
```kotlin
data class LoginResponse(
    val userId: String,
    val token: String,
    val username: String,
    val avatar: String?,
    val email: String?
)
```

### 2. API 接口

**ApiService.kt**
```kotlin
@POST("auth/login")
suspend fun login(@Body request: LoginRequest): BaseResponse<LoginResponse>
```

### 3. Repository 层

**LoginRepository.kt**
```kotlin
@Singleton
class LoginRepository @Inject constructor(
    private val apiService: ApiService,
    private val dataStoreManager: DataStoreManager
) : BaseRepository() {

    // 登录请求
    fun login(username: String, password: String): Flow<LoginResponse> = executeRequest {
        apiService.login(LoginRequest(username, password))
    }

    // 保存用户信息
    suspend fun saveUserInfo(response: LoginResponse) {
        dataStoreManager.saveToken(response.token)
        dataStoreManager.saveUserId(response.userId)
    }
}
```

**关键点**：
- 继承 `BaseRepository` 使用 `executeRequest` 方法
- 自动处理错误和异常
- 返回 `Flow` 支持响应式编程

### 4. ViewModel 层

**LoginViewModel.kt**
```kotlin
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository
) : BaseViewModel() {

    private val _loginResult = MutableStateFlow<UiState<LoginResponse>>(UiState.Empty())
    val loginResult = _loginResult.asStateFlow()

    fun login(username: String, password: String) {
        launchWithLoading {
            _loginResult.value = UiState.Loading("正在登录...")
            
            loginRepository.login(username, password)
                .catch { e ->
                    _loginResult.value = UiState.Error(message = e.message ?: "登录失败")
                }
                .collect { response ->
                    loginRepository.saveUserInfo(response)
                    _loginResult.value = UiState.Success(response)
                }
        }
    }
}
```

**关键点**：
- 使用 `@HiltViewModel` 和 `@Inject` 自动依赖注入
- 继承 `BaseViewModel` 使用 `launchWithLoading` 自动管理 Loading 状态
- 使用 `UiState` 统一管理 UI 状态

### 5. View 层

**LoginActivity.kt**
```kotlin
@AndroidEntryPoint
class LoginActivity : BaseActivity<ActivityLoginBinding, LoginViewModel>() {

    override val viewModel: LoginViewModel by viewModels()

    override fun createBinding(): ActivityLoginBinding {
        return ActivityLoginBinding.inflate(layoutInflater)
    }

    override fun initView() {
        // 初始化视图
    }

    override fun initData() {
        // 观察登录结果
        lifecycleScope.launch {
            viewModel.loginResult.collect { state ->
                when (state) {
                    is UiState.Loading -> LoadingDialog.show(this@LoginActivity)
                    is UiState.Success -> {
                        LoadingDialog.dismissLoading()
                        toast("登录成功！")
                    }
                    is UiState.Error -> {
                        LoadingDialog.dismissLoading()
                        toast(state.message)
                    }
                    else -> {}
                }
            }
        }
    }

    override fun setListeners() {
        binding.btnLogin.onSingleClick {
            performLogin()
        }
    }
}
```

**关键点**：
- 使用 `@AndroidEntryPoint` 启用 Hilt 依赖注入
- 继承 `BaseActivity` 自动集成 ViewBinding
- 使用 Kotlin 扩展函数（`onSingleClick`、`toast`）
- 观察 `StateFlow` 响应 UI 状态变化

## 🚀 如何测试

### 方式一：Mock 数据测试（推荐用于演示）

由于没有真实的后端服务器，你可以：

1. **修改 BaseUrl**（在 `app/build.gradle.kts` 中）
   ```kotlin
   buildConfigField("String", "BASE_URL", "\"https://jsonplaceholder.typicode.com/\"")
   ```

2. **使用 Mock 服务器**
   - 使用 [MockAPI](https://mockapi.io/)
   - 使用 [JSON Placeholder](https://jsonplaceholder.typicode.com/)
   - 使用 [Postman Mock Server](https://learning.postman.com/docs/designing-and-developing-your-api/mocking-data/setting-up-mock/)

3. **修改 Repository 返回 Mock 数据**（临时测试）
   ```kotlin
   fun login(username: String, password: String): Flow<LoginResponse> = flow {
       // 模拟网络延迟
       delay(1500)
       
       // 模拟登录成功
       if (username == "admin" && password == "123456") {
           emit(LoginResponse(
               userId = "1001",
               token = "mock_token_${System.currentTimeMillis()}",
               username = username,
               avatar = null,
               email = "admin@example.com"
           ))
       } else {
           throw ApiException(ApiException.CODE_SERVER_ERROR, "用户名或密码错误")
       }
   }.flowOn(Dispatchers.IO)
   ```

### 方式二：搭建本地测试服务器

1. 使用 Node.js + Express 快速搭建
2. 使用 Python Flask/FastAPI
3. 使用 Spring Boot

## 📊 测试用例

### 1. 成功登录
- **输入**：用户名 `admin`，密码 `123456`
- **预期**：显示 Loading → 显示"登录成功" Toast → 保存 Token

### 2. 用户名为空
- **输入**：用户名为空
- **预期**：提示"请输入用户名"

### 3. 密码为空
- **输入**：密码为空
- **预期**：提示"请输入密码"

### 4. 密码长度不足
- **输入**：密码 `12345`（少于6位）
- **预期**：提示"密码长度不能少于6位"

### 5. 登录失败
- **输入**：错误的用户名或密码
- **预期**：显示 Loading → 显示错误提示 Toast

## 📝 日志输出

运行应用后，可以在 Logcat 中看到以下日志：

```
D/DemoApp: Application initialized
D/DemoApp: LoginActivity initialized
D/DemoApp: [Track] PageView: LoginActivity, params: null
D/DemoApp: [Track] Event: click_login, params: {username=admin}
D/DemoApp: Login success: userId=1001, username=admin
D/DemoApp: [Track] Event: login_success, params: {userId=1001, username=admin}
```

## 🎨 UI 界面

登录页面包含：
- ✅ Logo 图标
- ✅ 标题"欢迎登录"
- ✅ 用户名输入框
- ✅ 密码输入框（支持显示/隐藏密码）
- ✅ 记住密码复选框
- ✅ 忘记密码链接
- ✅ 登录按钮
- ✅ 注册提示
- ✅ 第三方登录图标（微信、QQ）

## 🔧 扩展建议

### 1. 添加表单验证
- 使用正则表达式验证手机号/邮箱
- 使用 `RegexUtil.isMobile()` 或 `RegexUtil.isEmail()`

### 2. 添加记住密码功能
- 使用 DataStore 保存用户名和密码（加密存储）
- 在 `initView()` 中读取并填充

### 3. 添加自动登录
- 在启动时检查 Token 是否有效
- 直接跳转到主页

### 4. 添加生物识别登录
- 集成指纹识别
- 集成面部识别

### 5. 添加验证码
- 图形验证码
- 短信验证码

## 📚 相关文档

- [USAGE_GUIDE.md](../USAGE_GUIDE.md) - 框架使用指南
- [README.md](../../README.md) - 项目说明文档

---

**祝你使用愉快！** 🎉

如有问题，欢迎提 Issue！
