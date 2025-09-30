# Android MVVM 框架使用指南

## 📚 目录

1. [快速开始](#快速开始)
2. [网络请求](#网络请求)
3. [本地存储](#本地存储)
4. [界面开发](#界面开发)
5. [状态管理](#状态管理)
6. [自定义组件](#自定义组件)
7. [工具类使用](#工具类使用)
8. [最佳实践](#最佳实践)

---

## 快速开始

### 1. 创建一个新的功能模块

假设我们要创建一个用户登录功能，需要以下步骤：

#### 步骤 1：定义数据模型

在 `data/model/` 目录下创建请求和响应模型：

```kotlin
// LoginRequest.kt
data class LoginRequest(
    val username: String,
    val password: String
)

// LoginResponse.kt
data class LoginResponse(
    val userId: String,
    val token: String,
    val username: String,
    val avatar: String?
)
```

#### 步骤 2：定义 API 接口

在 `ApiService` 中添加接口方法：

```kotlin
interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): BaseResponse<LoginResponse>
}
```

#### 步骤 3：创建 Repository

```kotlin
@Singleton
class LoginRepository @Inject constructor(
    private val apiService: ApiService,
    private val dataStoreManager: DataStoreManager
) : BaseRepository() {

    fun login(username: String, password: String): Flow<LoginResponse> = executeRequest {
        apiService.login(LoginRequest(username, password))
    }

    suspend fun saveUserInfo(response: LoginResponse) {
        dataStoreManager.saveToken(response.token)
        dataStoreManager.saveUserId(response.userId)
    }
}
```

#### 步骤 4：创建 ViewModel

```kotlin
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository
) : BaseViewModel() {

    private val _loginResult = MutableStateFlow<UiState<LoginResponse>>(UiState.Empty())
    val loginResult = _loginResult.asStateFlow()

    fun login(username: String, password: String) {
        launchWithLoading {
            loginRepository.login(username, password)
                .catch { e ->
                    _loginResult.value = UiState.Error(message = e.message ?: "登录失败")
                }
                .collect { response ->
                    // 保存用户信息
                    loginRepository.saveUserInfo(response)
                    _loginResult.value = UiState.Success(response)
                }
        }
    }
}
```

#### 步骤 5：创建 Activity

```kotlin
@AndroidEntryPoint
class LoginActivity : BaseActivity<ActivityLoginBinding, LoginViewModel>() {

    override val viewModel: LoginViewModel by viewModels()

    override fun createBinding(): ActivityLoginBinding {
        return ActivityLoginBinding.inflate(layoutInflater)
    }

    override fun initView() {
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()
            
            if (username.isEmpty()) {
                toast("请输入用户名")
                return@setOnClickListener
            }
            
            if (password.isEmpty()) {
                toast("请输入密码")
                return@setOnClickListener
            }
            
            viewModel.login(username, password)
        }
    }

    override fun initData() {
        lifecycleScope.launch {
            viewModel.loginResult.collect { state ->
                when (state) {
                    is UiState.Success -> {
                        toast("登录成功")
                        // 跳转到主页
                        startActivity<MainActivity>()
                        finish()
                    }
                    is UiState.Error -> {
                        toast(state.message)
                    }
                    else -> {}
                }
            }
        }
    }
}
```

---

## 网络请求

### 基本用法

#### 1. 定义 API 接口

```kotlin
interface ApiService {
    // GET 请求
    @GET("user/{id}")
    suspend fun getUserInfo(@Path("id") userId: String): BaseResponse<User>
    
    // POST 请求
    @POST("user/update")
    suspend fun updateUser(@Body user: User): BaseResponse<User>
    
    // 带查询参数
    @GET("news/list")
    suspend fun getNewsList(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): BaseResponse<List<News>>
}
```

#### 2. Repository 中调用

```kotlin
class UserRepository @Inject constructor(
    private val apiService: ApiService
) : BaseRepository() {

    // 返回 Flow
    fun getUserInfo(userId: String): Flow<User> = executeRequest {
        apiService.getUserInfo(userId)
    }

    // 返回直接结果
    suspend fun updateUser(user: User): User = executeRequestDirect {
        apiService.updateUser(user)
    }
}
```

#### 3. ViewModel 中使用

```kotlin
@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository
) : BaseViewModel() {

    fun loadUserInfo(userId: String) {
        launchWithLoading {
            userRepository.getUserInfo(userId)
                .catch { e ->
                    showError(e.message ?: "加载失败")
                }
                .collect { user ->
                    // 处理数据
                }
        }
    }
}
```

### 高级用法

#### 缓存策略

```kotlin
fun getUserInfo(userId: String): Flow<User> = executeCacheFirst(
    cacheBlock = {
        // 从数据库读取缓存
        userDao.getUserById(userId)
    },
    networkBlock = {
        // 网络请求
        apiService.getUserInfo(userId)
    },
    saveBlock = { user ->
        // 保存到数据库
        userDao.insert(user.toEntity())
    }
)
```

---

## 本地存储

### DataStore 使用

#### 保存和读取数据

```kotlin
// 保存
lifecycleScope.launch {
    dataStoreManager.saveToken("your_token")
    dataStoreManager.saveUserId("user_id")
}

// 读取（Flow）
lifecycleScope.launch {
    dataStoreManager.getTokenFlow().collect { token ->
        // 使用 token
    }
}
```

### Room 数据库使用

#### 1. 定义 Entity

```kotlin
@Entity(tableName = "news")
data class NewsEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val createTime: Long
)
```

#### 2. 定义 Dao

```kotlin
@Dao
interface NewsDao {
    @Query("SELECT * FROM news ORDER BY createTime DESC")
    fun getAllNews(): Flow<List<NewsEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(news: List<NewsEntity>)
}
```

#### 3. 更新 AppDatabase

```kotlin
@Database(
    entities = [
        UserEntity::class,
        NewsEntity::class  // 添加新的 Entity
    ],
    version = 2  // 增加版本号
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun newsDao(): NewsDao  // 添加新的 Dao
}
```

---

## 界面开发

### Activity 开发

```kotlin
@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>() {

    override val viewModel: MainViewModel by viewModels()

    override fun createBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun initView() {
        // 初始化视图
        binding.toolbar.setTitle("首页")
    }

    override fun initData() {
        // 加载数据
        viewModel.loadData()
        
        // 观察数据变化
        lifecycleScope.launch {
            viewModel.dataList.collect { list ->
                // 更新 UI
            }
        }
    }
    
    override fun setListeners() {
        binding.btnRefresh.setOnClickListener {
            viewModel.refresh()
        }
    }
}
```

### Fragment 开发

```kotlin
@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding, HomeViewModel>() {

    override val viewModel: HomeViewModel by viewModels()

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        // 设置 RecyclerView
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = homeAdapter
        }
    }

    override fun lazyLoad() {
        // 懒加载数据（首次可见时调用）
        viewModel.loadData()
    }
}
```

### RecyclerView Adapter

```kotlin
class NewsAdapter : BaseAdapter<NewsItem, ItemNewsBinding>() {

    override fun createBinding(parent: ViewGroup): ItemNewsBinding {
        return ItemNewsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    }

    override fun bind(binding: ItemNewsBinding, item: NewsItem, position: Int) {
        binding.tvTitle.text = item.title
        binding.tvTime.text = DateUtil.formatTimestamp(item.createTime)
        
        // 加载图片
        Coil.load(binding.ivCover) {
            data(item.coverUrl)
            placeholder(R.drawable.placeholder)
            error(R.drawable.error)
        }
    }
}

// 使用
val adapter = NewsAdapter()
adapter.setItems(newsList)

adapter.setOnItemClickListener { item, position ->
    // 点击事件
}
```

---

## 状态管理

### UiState 使用

```kotlin
sealed class UiState<out T> {
    data class Loading(val message: String = "加载中...") : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val code: Int = -1, val message: String) : UiState<Nothing>()
    data class Empty(val message: String = "暂无数据") : UiState<Nothing>()
}

// ViewModel 中
private val _uiState = MutableStateFlow<UiState<List<News>>>(UiState.Empty())
val uiState = _uiState.asStateFlow()

fun loadNews() {
    launchWithLoading {
        newsRepository.getNewsList()
            .catch { e ->
                _uiState.value = UiState.Error(message = e.message ?: "加载失败")
            }
            .collect { list ->
                if (list.isEmpty()) {
                    _uiState.value = UiState.Empty()
                } else {
                    _uiState.value = UiState.Success(list)
                }
            }
    }
}

// Activity 中
lifecycleScope.launch {
    viewModel.uiState.collect { state ->
        when (state) {
            is UiState.Loading -> {
                // 显示加载中
                stateLayout.showLoading()
            }
            is UiState.Success -> {
                // 显示内容
                stateLayout.showContent()
                adapter.setItems(state.data)
            }
            is UiState.Error -> {
                // 显示错误
                stateLayout.showError(state.message)
            }
            is UiState.Empty -> {
                // 显示空数据
                stateLayout.showEmpty()
            }
        }
    }
}
```

---

## 自定义组件

### StateLayout

```xml
<lj.sword.demoappnocompose.widget.StateLayout
    android:id="@+id/stateLayout"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    
    <RecyclerView
        android:id="@+id/recyclerView"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
        
</lj.sword.demoappnocompose.widget.StateLayout>
```

```kotlin
// 显示不同状态
stateLayout.showLoading()
stateLayout.showError("加载失败")
stateLayout.showEmpty("暂无数据")
stateLayout.showContent()

// 设置重试监听
stateLayout.setOnRetryListener {
    viewModel.retry()
}
```

### LoadingDialog

```kotlin
// 显示
val dialog = LoadingDialog.show(this, "正在登录...")

// 隐藏
LoadingDialog.dismissLoading()
```

### CommonDialog

```kotlin
CommonDialog.builder(this)
    .setTitle("提示")
    .setContent("确定要退出登录吗？")
    .setConfirmText("确定")
    .setCancelText("取消")
    .setOnConfirmListener {
        // 确认操作
    }
    .show()

// 单按钮对话框
CommonDialog.builder(this)
    .setTitle("提示")
    .setContent("网络连接失败")
    .setSingleButton(true)
    .show()
```

### TitleBar

```xml
<lj.sword.demoappnocompose.widget.TitleBar
    android:id="@+id/titleBar"
    android:layout_width="match_parent"
    android:layout_height="56dp" />
```

```kotlin
titleBar.setTitle("个人中心")
titleBar.setRightText("编辑")
titleBar.setOnRightTextClickListener {
    // 右侧按钮点击
}
```

---

## 工具类使用

### 扩展函数

```kotlin
// View 扩展
view.visible()
view.gone()
view.onSingleClick { /* 防抖点击 */ }

// Context 扩展
toast("提示信息")
startActivity<MainActivity>()
val px = dp2px(16)

// String 扩展
"13800138000".isMobile()  // true
"test@example.com".isEmail()  // true
val user = jsonString.fromJson<User>()

// Flow 扩展
flow.asUiState()
flow.catchError { error -> }
```

### 工具类

```kotlin
// 网络判断
if (NetworkUtil.isNetworkAvailable(context)) {
    // 有网络
}

// 日期格式化
val timeStr = DateUtil.formatTimestamp(timestamp)
val friendlyTime = DateUtil.getFriendlyTime(timestamp)

// JSON 解析
val json = JsonUtil.toJson(user)
val user = JsonUtil.fromJson<User>(json)

// 加密
val md5 = EncryptUtil.md5("password")
val encrypted = EncryptUtil.aesEncrypt("data", "key1234567890123")

// 文件操作
FileUtil.writeFile(path, content)
val content = FileUtil.readFile(path)
val cacheSize = FileUtil.getCacheSize(context)
```

---

## 最佳实践

### 1. 错误处理

```kotlin
// 在 ViewModel 中
launchWithLoading {
    repository.getData()
        .catch { e ->
            when (e) {
                is ApiException -> {
                    when (e.code) {
                        ApiException.CODE_TOKEN_EXPIRED -> {
                            // Token 过期，跳转登录
                        }
                        else -> {
                            showError(e.msg)
                        }
                    }
                }
                else -> {
                    showError("未知错误")
                }
            }
        }
        .collect { data ->
            // 处理数据
        }
}
```

### 2. 生命周期感知

```kotlin
// 使用 lifecycleScope
lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.uiState.collect { state ->
            // 只在 STARTED 状态时收集
        }
    }
}
```

### 3. 内存泄漏预防

```kotlin
// Fragment 中使用 viewLifecycleOwner
viewLifecycleOwner.lifecycleScope.launch {
    viewModel.data.collect { }
}

// ViewBinding 及时释放
override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
}
```

### 4. 埋点统计

```kotlin
// 页面浏览
override fun onResume() {
    super.onResume()
    TrackManager.trackPageView("MainActivity")
}

// 事件埋点
binding.btnLogin.setOnClickListener {
    TrackManager.trackEvent("click_login_button", mapOf(
        "page" to "LoginActivity"
    ))
    // 执行登录逻辑
}
```

### 5. 日志输出

```kotlin
Logger.d("Debug message")
Logger.i("Info message")
Logger.e("Error occurred", exception)
Logger.json(jsonString)
Logger.network(url, "POST", params)
```

---

## 常见问题

### Q: 如何切换环境？

A: 在 Build Variants 中选择对应的构建变体：
- `debug` - 开发环境
- `staging` - 测试环境
- `release` - 生产环境

### Q: 如何添加新的依赖？

A: 在 `gradle/libs.versions.toml` 中添加版本号，然后在 `app/build.gradle.kts` 中添加依赖。

### Q: 如何实现下拉刷新？

A: 使用 SmartRefreshLayout：

```xml
<com.scwang.smart.refresh.layout.SmartRefreshLayout
    android:id="@+id/refreshLayout"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    
    <RecyclerView ... />
    
</com.scwang.smart.refresh.layout.SmartRefreshLayout>
```

```kotlin
binding.refreshLayout.setOnRefreshListener {
    viewModel.refresh()
}

binding.refreshLayout.setOnLoadMoreListener {
    viewModel.loadMore()
}

// 完成刷新
binding.refreshLayout.finishRefresh()
binding.refreshLayout.finishLoadMore()
```

---

## 更多资源

- [README.md](README.md) - 项目说明
- [Android MVVM 开发框架需求文档.md](Android%20MVVM开发框架需求文档.md) - 需求文档
- [官方文档](https://developer.android.com/jetpack/guide)

---

**祝你使用愉快！** 🎉
