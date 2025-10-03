# IDE 配置指南

本文档详细说明了如何配置 Android Studio 以获得最佳的开发体验。

## 📋 目录

- [Android Studio 基础配置](#android-studio-基础配置)
- [插件推荐](#插件推荐)
- [代码模板](#代码模板)
- [快捷键配置](#快捷键配置)
- [调试配置](#调试配置)
- [性能优化](#性能优化)

## 🛠️ Android Studio 基础配置

### 1. 外观和主题

#### 设置主题
1. **File** → **Settings** → **Appearance & Behavior** → **Appearance**
2. 选择主题：
   - **Darcula**: 暗色主题（推荐）
   - **IntelliJ Light**: 亮色主题
   - **High contrast**: 高对比度主题

#### 字体配置
1. **File** → **Settings** → **Editor** → **Font**
2. 推荐字体：
   - **JetBrains Mono**: 专为编程设计
   - **Fira Code**: 支持连字符
   - **Source Code Pro**: Adobe 开源字体

```
Font: JetBrains Mono
Size: 14
Line spacing: 1.2
Enable font ligatures: ✓ (如果使用 Fira Code)
```

### 2. 编辑器配置

#### 代码风格
1. **File** → **Settings** → **Editor** → **Code Style** → **Kotlin**
2. 导入 Kotlin 官方代码风格：
   - 点击 **Set from...** → **Predefined Style** → **Kotlin style guide**

#### 自动导入
1. **File** → **Settings** → **Editor** → **General** → **Auto Import**
2. 配置如下：
```
✓ Add unambiguous imports on the fly
✓ Optimize imports on the fly
✓ Show import popup for classes
✓ Show import popup for static methods and fields
```

#### 行号和空白字符
1. **File** → **Settings** → **Editor** → **General** → **Appearance**
2. 启用以下选项：
```
✓ Show line numbers
✓ Show method separators
✓ Show whitespaces
✓ Show indent guides
```

### 3. 构建配置

#### Gradle 设置
1. **File** → **Settings** → **Build, Execution, Deployment** → **Build Tools** → **Gradle**
2. 配置如下：
```
Build and run using: Gradle
Run tests using: Gradle
Gradle JVM: Project SDK (Java 17)
```

#### 编译器设置
1. **File** → **Settings** → **Build, Execution, Deployment** → **Compiler**
2. 配置如下：
```
Build process heap size (Mbytes): 2048
✓ Compile independent modules in parallel
✓ Automatically show first error in editor
```

### 4. 版本控制

#### Git 配置
1. **File** → **Settings** → **Version Control** → **Git**
2. 配置如下：
```
Path to Git executable: /usr/bin/git (或 Windows 路径)
✓ Use credential helper
✓ Update branches information
```

#### 提交配置
1. **File** → **Settings** → **Version Control** → **Commit**
2. 启用以下选项：
```
✓ Use non-modal commit interface
✓ Analyze code before commit
✓ Check TODO (Show TODO)
✓ Perform code analysis
✓ Reformat code
✓ Optimize imports
```

## 🔌 插件推荐

### 必装插件

#### 1. Kotlin Multiplatform Mobile
- **功能**: Kotlin 多平台开发支持
- **安装**: **File** → **Settings** → **Plugins** → 搜索 "Kotlin Multiplatform Mobile"

#### 2. Rainbow Brackets
- **功能**: 彩色括号匹配
- **安装**: **File** → **Settings** → **Plugins** → 搜索 "Rainbow Brackets"

#### 3. GitToolBox
- **功能**: 增强 Git 功能
- **特性**:
  - 显示 Git 状态
  - 自动拉取
  - 提交模板

#### 4. SonarLint
- **功能**: 代码质量检查
- **特性**:
  - 实时代码分析
  - 安全漏洞检测
  - 代码异味提醒

#### 5. ADB Idea
- **功能**: ADB 命令快捷操作
- **特性**:
  - 一键卸载应用
  - 清除应用数据
  - 重启应用

### 推荐插件

#### 1. Material Theme UI
- **功能**: Material Design 主题
- **特性**: 多种精美主题选择

#### 2. Indent Rainbow
- **功能**: 彩色缩进指示
- **特性**: 更清晰的代码层次

#### 3. String Manipulation
- **功能**: 字符串处理工具
- **特性**:
  - 大小写转换
  - 编码解码
  - 格式化

#### 4. JSON Viewer
- **功能**: JSON 格式化查看
- **特性**: 美化 JSON 显示

#### 5. Database Navigator
- **功能**: 数据库管理工具
- **特性**: 直接在 IDE 中操作数据库

### 代码生成插件

#### 1. GsonFormatPlus
- **功能**: JSON 转 Kotlin 数据类
- **使用**: 右键 → **Generate** → **GsonFormatPlus**

#### 2. Parcelable Code Generator
- **功能**: 自动生成 Parcelable 代码
- **使用**: 右键 → **Generate** → **Parcelable**

#### 3. DTO generator
- **功能**: 生成数据传输对象
- **使用**: 右键 → **Generate** → **DTO**

## 📝 代码模板

### 1. Live Templates

#### Android Activity 模板
1. **File** → **Settings** → **Editor** → **Live Templates**
2. 点击 **+** → **Template Group** → 输入 "Android Custom"
3. 选择组，点击 **+** → **Live Template**
4. 配置如下：

**Abbreviation**: `activity`
**Description**: `Android Activity with MVVM`
**Template text**:
```kotlin
@AndroidEntryPoint
class $NAME$Activity : BaseActivity<Activity$NAME$Binding, $NAME$ViewModel>() {

    override val viewModel: $NAME$ViewModel by viewModels()

    override fun createBinding(): Activity$NAME$Binding {
        return Activity$NAME$Binding.inflate(layoutInflater)
    }

    override fun initView() {
        $END$
    }

    override fun initData() {
        
    }

    override fun setListeners() {
        
    }
}
```

#### ViewModel 模板
**Abbreviation**: `viewmodel`
**Description**: `Android ViewModel with Hilt`
**Template text**:
```kotlin
@HiltViewModel
class $NAME$ViewModel @Inject constructor(
    private val $REPOSITORY$: $REPOSITORY_TYPE$
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<UiState<$DATA_TYPE$>>(UiState.Empty())
    val uiState: StateFlow<UiState<$DATA_TYPE$>> = _uiState.asStateFlow()

    fun load$DATA_NAME$() {
        launchWithLoading {
            $REPOSITORY$.get$DATA_NAME$()
                .catch { e ->
                    _uiState.value = UiState.Error(message = e.message ?: "加载失败")
                }
                .collect { data ->
                    _uiState.value = UiState.Success(data)
                }
        }
    }
    $END$
}
```

#### Repository 模板
**Abbreviation**: `repository`
**Description**: `Repository with BaseRepository`
**Template text**:
```kotlin
@Singleton
class $NAME$Repository @Inject constructor(
    private val apiService: ApiService,
    private val $DAO$: $DAO_TYPE$
) : BaseRepository() {

    fun get$DATA_NAME$(): Flow<$DATA_TYPE$> = executeRequest {
        apiService.get$DATA_NAME$()
    }

    suspend fun save$DATA_NAME$(data: $DATA_TYPE$) {
        $DAO$.insert(data.toEntity())
    }
    $END$
}
```

### 2. File Templates

#### Activity + ViewModel + Layout 模板
1. **File** → **Settings** → **Editor** → **File and Code Templates**
2. 点击 **+** 创建新模板

**Name**: `Android Activity Set`
**Extension**: `kt`
**File name**: `${NAME}Activity`

```kotlin
#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "")package ${PACKAGE_NAME}

#end
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ${PACKAGE_NAME}.base.BaseActivity
import ${PACKAGE_NAME}.base.UiState
import ${PACKAGE_NAME}.databinding.Activity${NAME}Binding

@AndroidEntryPoint
class ${NAME}Activity : BaseActivity<Activity${NAME}Binding, ${NAME}ViewModel>() {

    override val viewModel: ${NAME}ViewModel by viewModels()

    override fun createBinding(): Activity${NAME}Binding {
        return Activity${NAME}Binding.inflate(layoutInflater)
    }

    override fun initView() {
        // TODO: Initialize views
    }

    override fun initData() {
        // TODO: Load data
        observeUiState()
    }

    override fun setListeners() {
        // TODO: Set listeners
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is UiState.Loading -> {
                        // Show loading
                    }
                    is UiState.Success -> {
                        // Handle success
                    }
                    is UiState.Error -> {
                        // Handle error
                        toast(state.message)
                    }
                    is UiState.Empty -> {
                        // Handle empty
                    }
                }
            }
        }
    }
}
```

## ⌨️ 快捷键配置

### 1. 自定义快捷键

#### 常用操作快捷键
1. **File** → **Settings** → **Keymap**
2. 搜索并设置以下快捷键：

| 操作 | 推荐快捷键 | 说明 |
|------|------------|------|
| Generate | `Alt + Insert` | 生成代码 |
| Reformat Code | `Ctrl + Alt + L` | 格式化代码 |
| Optimize Imports | `Ctrl + Alt + O` | 优化导入 |
| Show Intention Actions | `Alt + Enter` | 显示意图操作 |
| Quick Documentation | `Ctrl + Q` | 快速文档 |
| Go to Declaration | `Ctrl + B` | 跳转到声明 |
| Find Usages | `Alt + F7` | 查找用法 |
| Rename | `Shift + F6` | 重命名 |
| Extract Method | `Ctrl + Alt + M` | 提取方法 |
| Surround With | `Ctrl + Alt + T` | 包围代码 |

#### Android 特定快捷键
| 操作 | 快捷键 | 说明 |
|------|--------|------|
| Run App | `Shift + F10` | 运行应用 |
| Debug App | `Shift + F9` | 调试应用 |
| Build Project | `Ctrl + F9` | 构建项目 |
| Clean Project | `Ctrl + Shift + F9` | 清理项目 |
| Sync Project | `Ctrl + Shift + A` → "Sync" | 同步项目 |

### 2. 代码导航快捷键

| 操作 | 快捷键 | 说明 |
|------|--------|------|
| Go to Class | `Ctrl + N` | 跳转到类 |
| Go to File | `Ctrl + Shift + N` | 跳转到文件 |
| Go to Symbol | `Ctrl + Alt + Shift + N` | 跳转到符号 |
| Recent Files | `Ctrl + E` | 最近文件 |
| File Structure | `Ctrl + F12` | 文件结构 |
| Navigate Back | `Ctrl + Alt + Left` | 导航返回 |
| Navigate Forward | `Ctrl + Alt + Right` | 导航前进 |

## 🐛 调试配置

### 1. 断点配置

#### 条件断点
1. 在代码行号处右键
2. 选择 **Add Conditional Breakpoint**
3. 输入条件表达式，如：`user.id == "123"`

#### 日志断点
1. 在断点上右键
2. 取消勾选 **Suspend**
3. 勾选 **Log message to console**
4. 输入日志消息

### 2. 调试模板

#### 网络请求调试
在 `NetworkModule` 中添加：
```kotlin
@Provides
@Singleton
fun provideLoggingInterceptor(): HttpLoggingInterceptor {
    return HttpLoggingInterceptor { message ->
        Log.d("OkHttp", message)
    }.apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }
}
```

#### 数据库调试
添加 Room 日志：
```kotlin
@Provides
@Singleton
fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
    return Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "app_database"
    ).apply {
        if (BuildConfig.DEBUG) {
            setQueryCallback({ sqlQuery, bindArgs ->
                Log.d("RoomQuery", "SQL Query: $sqlQuery SQL Args: $bindArgs")
            }, Executors.newSingleThreadExecutor())
        }
    }.build()
}
```

### 3. 性能分析

#### CPU Profiler 配置
1. **Run** → **Profile 'app'**
2. 选择 **CPU Profiler**
3. 配置采样方式：
   - **Sampled (Java/Kotlin Method Trace)**: 低开销
   - **Instrumented (Java/Kotlin Method Trace)**: 高精度

#### Memory Profiler 配置
1. **Run** → **Profile 'app'**
2. 选择 **Memory Profiler**
3. 监控内存分配和 GC 事件

## 🚀 性能优化

### 1. IDE 性能配置

#### 内存设置
1. **Help** → **Edit Custom VM Options**
2. 添加以下配置：
```
-Xms2g
-Xmx8g
-XX:ReservedCodeCacheSize=1g
-XX:+UseConcMarkSweepGC
-XX:SoftRefLRUPolicyMSPerMB=50
-ea
-XX:CICompilerCount=2
-Dsun.io.useCanonPrefixCache=false
-Djdk.http.auth.tunneling.disabledSchemes=""
-XX:+HeapDumpOnOutOfMemoryError
-XX:-OmitStackTraceInFastThrow
-Djb.vmOptionsFile=${idea.paths.selector}/studio.vmoptions
-Djava.system.class.loader=com.intellij.util.lang.PathClassLoader
-Xverify:none
```

#### 禁用不必要的插件
1. **File** → **Settings** → **Plugins**
2. 禁用以下插件（如果不需要）：
   - CVS Integration
   - hg4idea
   - Subversion Integration
   - Task Management
   - Time Tracking

### 2. 项目性能优化

#### Gradle 配置
在 `gradle.properties` 中添加：
```properties
# 启用并行编译
org.gradle.parallel=true

# 启用守护进程
org.gradle.daemon=true

# 配置 JVM 参数
org.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=512m -XX:+HeapDumpOnOutOfMemoryError

# 启用配置缓存
org.gradle.configuration-cache=true

# 启用构建缓存
org.gradle.caching=true

# Kotlin 编译优化
kotlin.incremental=true
kotlin.parallel.tasks.in.project=true
kotlin.caching.enabled=true
```

#### 模块化配置
将大型项目拆分为多个模块：
```
app/
├── core/
│   ├── common/
│   ├── network/
│   └── database/
├── feature/
│   ├── auth/
│   ├── user/
│   └── settings/
└── shared/
    ├── ui/
    └── resources/
```

### 3. 代码分析优化

#### 配置代码检查
1. **File** → **Settings** → **Editor** → **Inspections**
2. 启用以下检查：
   - **Android** → **Lint** → **Performance**
   - **Kotlin** → **Performance**
   - **Java** → **Memory issues**

#### 自定义检查规则
创建 `lint.xml` 配置文件：
```xml
<?xml version="1.0" encoding="UTF-8"?>
<lint>
    <!-- 错误级别 -->
    <issue id="HardcodedText" severity="error" />
    <issue id="MissingTranslation" severity="error" />
    
    <!-- 警告级别 -->
    <issue id="UnusedResources" severity="warning" />
    <issue id="IconMissingDensityFolder" severity="warning" />
    
    <!-- 忽略 -->
    <issue id="GoogleAppIndexingWarning" severity="ignore" />
</lint>
```

## 📚 参考资源

- [Android Studio 用户指南](https://developer.android.com/studio/intro)
- [IntelliJ IDEA 文档](https://www.jetbrains.com/help/idea/)
- [Kotlin 插件文档](https://kotlinlang.org/docs/kotlin-ide.html)
- [Android 开发者工具](https://developer.android.com/studio/debug)

---

*通过合理配置 IDE，可以显著提升开发效率和代码质量。建议根据个人习惯和项目需求进行调整。* 🎯