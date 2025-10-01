# Android 多语言切换功能使用指南

## 功能概述

本框架提供了完整的Android多语言切换解决方案，支持以下特性：

- 🌍 支持多种语言（简体中文、繁体中文、英文、日文、韩文）
- ⚡ 实时语言切换，无需重启应用
- 💾 语言选择持久化保存
- 🔄 自动Activity重建和Fragment更新
- 🌐 网络请求自动添加语言标识
- 📅 日期时间多语言格式化
- 💰 货币数字多语言格式化
- 🎨 主题与多语言联动
- 📱 RTL（从右到左）语言支持

## 架构图

```
┌─────────────────────────────────────────────────────────────┐
│                    Android 多语言框架                        │
├─────────────────────────────────────────────────────────────┤
│  UI Layer                                                   │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐          │
│  │ BaseActivity│ │BaseFragment │ │LanguageUI   │          │
│  │             │ │             │ │Components   │          │
│  └─────────────┘ └─────────────┘ └─────────────┘          │
├─────────────────────────────────────────────────────────────┤
│  Business Layer                                             │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐          │
│  │LocaleManager│ │LanguageMgr  │ │UseCases     │          │
│  │             │ │             │ │             │          │
│  └─────────────┘ └─────────────┘ └─────────────┘          │
├─────────────────────────────────────────────────────────────┤
│  Data Layer                                                 │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐          │
│  │DataStore    │ │Resources    │ │Network      │          │
│  │Manager      │ │(strings.xml)│ │Interceptor  │          │
│  └─────────────┘ └─────────────┘ └─────────────┘          │
└─────────────────────────────────────────────────────────────┘
```

## 如何添加新语言

### 1. 创建语言资源文件夹

在 `app/src/main/res/` 目录下创建新的语言资源文件夹：

```
res/
├── values/           # 默认语言（简体中文）
├── values-zh/        # 简体中文
├── values-zh-rTW/    # 繁体中文
├── values-en/        # 英文
├── values-ja/        # 日文
├── values-ko/        # 韩文
└── values-[新语言代码]/  # 新语言文件夹
```

### 2. 添加语言配置到 LanguageConfig

在 `SupportedLanguage` 枚举中添加新语言：

```kotlin
enum class SupportedLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String,
    val iconRes: Int
) {
    // 现有语言...
    
    // 新语言示例：法语
    FRENCH(
        code = "fr-FR",
        displayName = "Français",
        nativeName = "Français",
        iconRes = R.drawable.ic_language_french
    );
}
```

### 3. 创建字符串资源文件

在新语言文件夹中创建 `strings.xml`：

```xml
<!-- values-fr/strings.xml -->
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">Mon App</string>
    <string name="settings_language">Langue</string>
    <string name="language_chinese_simplified">Chinois Simplifié</string>
    <string name="language_english">Anglais</string>
    <!-- 添加更多翻译... -->
</resources>
```

### 4. 添加语言图标

在 `drawable` 文件夹中添加语言图标：

```xml
<!-- drawable/ic_language_french.xml -->
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <!-- 法国国旗图标或其他标识 -->
</vector>
```

## 如何翻译字符串资源

### 1. 使用Android Studio翻译工具

1. 打开 `strings.xml` 文件
2. 点击右上角的"Open Editor"按钮
3. 在翻译编辑器中添加新语言
4. 逐行翻译字符串资源

### 2. 推荐翻译服务

- **Google Translate API**: 自动化翻译，适合大量文本
- **Microsoft Translator**: 支持多种语言对
- **DeepL**: 高质量翻译，特别适合欧洲语言
- **人工翻译**: 对于重要UI文本，建议使用专业翻译

### 3. 翻译最佳实践

- 保持字符串ID不变，只翻译值
- 注意字符串中的占位符（如 `%s`, `%d`）
- 考虑不同语言的文本长度差异
- 测试UI布局在不同语言下的显示效果

## 如何使用语言切换 API

### 1. 基本语言切换

```kotlin
// 在Activity或Fragment中
class MainActivity : BaseActivity<ActivityMainBinding>() {
    
    private fun switchToEnglish() {
        // 使用UseCase切换语言
        switchLanguageUseCase(SupportedLanguage.ENGLISH)
    }
    
    private fun switchToJapanese() {
        // 直接使用LocaleManager
        localeManager.setLocale(SupportedLanguage.JAPANESE)
    }
}
```

### 2. 监听语言变化

```kotlin
class MyFragment : BaseFragment<FragmentMyBinding>() {
    
    override fun onLocaleChanged(language: SupportedLanguage) {
        super.onLocaleChanged(language)
        // 更新Fragment中的UI
        updateUI()
    }
    
    private fun updateUI() {
        binding.textView.text = getString(R.string.hello_world)
        // 刷新RecyclerView等
        adapter.notifyDataSetChanged()
    }
}
```

### 3. 在自定义组件中使用

```kotlin
class CustomView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    
    private val localeManager by lazy { 
        (context.applicationContext as MyApplication).localeManager 
    }
    
    private fun updateText() {
        // 根据当前语言更新文本
        val currentLanguage = runBlocking { localeManager.getCurrentLanguage() }
        text = when (currentLanguage) {
            SupportedLanguage.CHINESE_SIMPLIFIED -> "你好"
            SupportedLanguage.ENGLISH -> "Hello"
            SupportedLanguage.JAPANESE -> "こんにちは"
            else -> "Hello"
        }
    }
}
```

## 网络请求多语言处理

### 1. 自动添加语言标识

框架已自动配置 `LanguageInterceptor`，所有网络请求会自动添加 `Accept-Language` 请求头：

```kotlin
// 无需额外配置，自动生效
@GET("api/data")
suspend fun getData(): Response<DataResponse>
```

### 2. 手动指定语言

```kotlin
@GET("api/data")
suspend fun getData(@Query("lang") lang: String? = null): Response<DataResponse>

// 使用
val currentLanguage = runBlocking { localeManager.getCurrentLanguageCode() }
apiService.getData(currentLanguage)
```

## 日期时间多语言格式化

### 1. 使用 DateTimeFormatter

```kotlin
@Inject
lateinit var dateTimeFormatter: DateTimeFormatter

private fun formatDate() {
    val date = Date()
    
    // 格式化日期
    val formattedDate = dateTimeFormatter.formatDate(date)
    
    // 格式化时间
    val formattedTime = dateTimeFormatter.formatTime(date)
    
    // 格式化日期时间
    val formattedDateTime = dateTimeFormatter.formatDateTime(date)
    
    // 相对时间（刚刚、5分钟前等）
    val relativeTime = dateTimeFormatter.formatRelativeTime(date)
}
```

### 2. 自定义格式

```kotlin
private fun customDateFormat() {
    val locale = localeManager.getCurrentLocale()
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", locale)
    val formatted = formatter.format(Date())
}
```

## 货币数字格式化

### 1. 使用 NumberFormatUtils

```kotlin
@Inject
lateinit var numberFormatUtils: NumberFormatUtils

private fun formatNumbers() {
    val amount = 1234567.89
    
    // 格式化数字
    val formattedNumber = numberFormatUtils.formatNumber(amount)
    
    // 格式化货币
    val formattedCurrency = numberFormatUtils.formatCurrency(amount)
    
    // 格式化百分比
    val formattedPercentage = numberFormatUtils.formatPercentage(0.15)
}
```

## 主题与多语言联动

### 1. 语言特定资源

创建语言特定的drawable资源：

```
res/
├── drawable/           # 默认资源
├── drawable-zh/        # 中文特定资源
├── drawable-en/        # 英文特定资源
└── drawable-ja/        # 日文特定资源
```

### 2. 在代码中使用

```kotlin
// 根据语言选择不同的资源
val welcomeImage = when (currentLanguage) {
    SupportedLanguage.CHINESE_SIMPLIFIED -> R.drawable.ic_welcome_chinese
    SupportedLanguage.ENGLISH -> R.drawable.ic_welcome_english
    else -> R.drawable.ic_welcome_default
}
```

## RTL语言支持

### 1. 布局方向支持

```xml
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:layoutDirection="locale">
    <!-- 内容会根据语言自动调整方向 -->
</LinearLayout>
```

### 2. 文本方向

```xml
<TextView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:gravity="start"
    android:text="@string/rtl_text_example" />
```

## 常见问题解答

### Q: 语言切换后某些文本没有更新？

A: 确保在 `onLocaleChanged()` 方法中重新设置文本，或者使用 `getString()` 方法获取最新翻译。

### Q: 如何添加新的语言资源？

A: 按照上述"如何添加新语言"章节的步骤，创建新的语言文件夹和资源文件。

### Q: 网络请求没有携带语言标识？

A: 检查是否正确配置了 `LanguageInterceptor`，确保在 `NetworkModule` 中添加了该拦截器。

### Q: 日期时间格式不正确？

A: 使用 `DateTimeFormatter` 工具类，它会根据当前语言自动选择合适的格式。

### Q: 如何测试多语言功能？

A: 在设备设置中切换系统语言，或者在应用内使用语言设置页面进行切换。

### Q: 支持哪些语言？

A: 目前支持简体中文、繁体中文、英文、日文、韩文。可以通过添加新的 `SupportedLanguage` 枚举值来支持更多语言。

## 版本历史

- **v1.0.0**: 初始版本，支持基本的多语言切换功能
- **v1.1.0**: 添加网络请求多语言支持
- **v1.2.0**: 添加日期时间和数字格式化功能
- **v1.3.0**: 添加RTL语言支持和主题联动

## 贡献指南

欢迎贡献代码和翻译！请遵循以下步骤：

1. Fork 项目
2. 创建功能分支
3. 提交更改
4. 创建 Pull Request

## 许可证

本项目采用 MIT 许可证。详见 [LICENSE](LICENSE) 文件。
