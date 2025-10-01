# Android 通用框架 - 多语言切换功能实施清单

## Phase 1: 多语言架构设计

### 1.1 创建多语言管理器核心类
- [x] 创建 `LocaleManager.kt` 单例类
- [x] 定义支持的语言枚举 `SupportedLanguage`（中文、英文、日文等）
- [x] 实现 `getCurrentLocale()` 获取当前语言方法
- [x] 实现 `setLocale(language: SupportedLanguage)` 切换语言方法
- [x] 实现 `getAvailableLanguages()` 获取可用语言列表方法
- [x] 添加语言切换监听器接口 `OnLocaleChangeListener`
- [x] 实现监听器的注册和注销方法
- [x] 添加语言切换通知机制

### 1.2 创建语言持久化层
- [x] 在 DataStore 中添加语言配置存储字段
- [x] 创建 `LanguagePreferences.kt` 数据类
- [x] 在 `AppDataStore` 中添加 `languageCode: String` 字段
- [x] 实现 `saveLanguage(code: String)` 持久化方法
- [x] 实现 `getLanguage(): Flow<String>` 读取方法
- [x] 设置默认语言为系统语言的逻辑
- [x] 添加语言代码有效性验证
- [x] 处理语言不存在时的降级策略

### 1.3 创建语言配置数据类
- [x] 创建 `data/model/LanguageConfig.kt` 数据类
- [x] 定义 `code: String` 字段（语言代码：zh, en, ja等）
- [x] 定义 `displayName: String` 字段（显示名称）
- [x] 定义 `nativeName: String` 字段（本地化名称）
- [x] 定义 `iconRes: Int` 字段（国旗/语言图标资源ID）
- [x] 添加 `isSelected: Boolean` 字段标识当前选中状态
- [x] 实现相等性比较方法
- [x] 创建预定义的语言配置列表

---

## Phase 2: Application级别集成 

### 2.1 修改 BaseApplication
- [x] 在 `BaseApplication` 中添加 `LocaleManager` 初始化
- [x] 在 `onCreate()` 中从 DataStore 读取保存的语言配置
- [x] 实现 `attachBaseContext()` 方法应用语言配置
- [x] 设置应用级别的 Configuration
- [x] 处理首次启动无保存语言的情况
- [x] 添加语言初始化失败的异常处理
- [x] 确保语言配置在所有组件初始化前完成

### 2.2 实现 Context 语言包装
- [x] 创建 `ContextUtils.kt` 工具类
- [x] 实现 `Context.wrap(locale: Locale)` 扩展方法
- [x] 更新 Configuration 的 locale 配置
- [x] 处理 API 24+ 的兼容性（使用 createConfigurationContext）
- [x] 处理 API 24 以下的兼容性（使用 updateConfiguration）
- [x] 处理 Resources 的正确更新
- [x] 添加空安全检查

### 2.3 处理 Activity 语言附加
- [x] 在 `BaseActivity` 中重写 `attachBaseContext()` 方法
- [x] 从 LocaleManager 获取当前语言配置
- [x] 调用 Context.wrap() 应用语言到 Context
- [x] 确保所有子类 Activity 自动继承此行为
- [x] 处理 Activity 重建时的语言保持
- [x] 添加语言应用失败的日志记录

---

## Phase 3: UI组件开发

### 3.1 创建语言选择器 Dialog
- [x] 创建 `LanguageSelectionDialog.kt` 对话框类
- [x] 设计对话框布局文件 `dialog_language_selection.xml`
- [x] 添加标题栏显示"选择语言"
- [x] 使用 RecyclerView 显示语言列表
- [x] 添加搜索框（可选功能）
- [x] 添加确认按钮
- [x] 添加取消按钮
- [x] 设置对话框圆角和阴影样式
- [x] 适配暗黑模式样式
- [x] 添加对话框显示和隐藏动画

### 3.2 创建语言选择 Adapter
- [x] 创建 `LanguageAdapter.kt` RecyclerView 适配器
- [x] 创建 `item_language.xml` 列表项布局
- [x] 显示语言图标（国旗或语言符号）
- [x] 显示语言的显示名称
- [x] 显示语言的原生名称
- [x] 添加 RadioButton 或 CheckBox 显示选中状态
- [x] 实现单选逻辑（点击后更新选中项）
- [x] 高亮显示当前选中的语言项
- [x] 添加列表项点击事件监听
- [x] 实现列表项点击动画效果
- [x] 处理列表项的焦点状态

### 3.3 创建语言切换按钮组件
- [x] 创建 `LanguageSwitcherView.kt` 自定义View
- [x] 设计按钮布局显示当前语言图标
- [x] 显示当前语言简称（如：中文、EN）
- [x] 添加下拉箭头图标
- [x] 实现点击弹出语言选择对话框
- [x] 支持在 Toolbar 中使用
- [x] 支持在设置页面中使用
- [x] 添加按钮点击涟漪效果
- [x] 语言切换后自动更新按钮显示
- [x] 适配不同尺寸和主题

---

## Phase 4: 字符串资源管理

### 4.1 创建多语言资源文件
- [x] 在 `res/` 下创建 `values/strings.xml` (默认/英文)
- [x] 创建 `values-zh-rCN/strings.xml` (简体中文)
- [x] 创建 `values-zh-rTW/strings.xml` (繁体中文)
- [x] 创建 `values-ja/strings.xml` (日文)
- [x] 创建 `values-ko/strings.xml` (韩文)
- [x] 根据需要添加其他语言文件夹
- [x] 确保每个文件夹的字符串 key 完全一致
- [x] 为不同语言添加对应的 strings.xml 文件

### 4.2 整理现有字符串资源
- [x] 审查项目中所有硬编码的字符串
- [x] 列出所有需要多语言支持的文本
- [x] 将硬编码字符串迁移到 `strings.xml`
- [x] 为每个字符串定义清晰的命名规范（如：`btn_confirm`, `msg_error`）
- [x] 按模块或功能对字符串进行分组注释
- [x] 翻译所有基础字符串到各支持语言
- [x] 使用翻译工具或翻译服务确保质量
- [x] 标注需要专业翻译的特殊术语

### 4.3 创建字符串资源工具类
- [x] 创建 `StringResourceUtils.kt` 工具类
- [x] 提供全局 `getString(resId: Int)` 方法
- [x] 提供格式化 `getString(resId: Int, vararg args)` 方法
- [x] 实现复数形式字符串处理（Plurals）
- [x] 处理字符串数组资源
- [x] 添加字符串资源不存在时的降级处理
- [x] 提供在非 Activity/Fragment 中获取字符串的方法
- [x] 添加字符串缓存机制（可选优化）

---

## Phase 5: ViewModel层集成

### 5.1 创建语言设置 ViewModel
- [x] 创建 `LanguageViewModel.kt` 视图模型类
- [x] 定义 `currentLanguage: StateFlow<LanguageConfig>` 当前语言状态
- [x] 定义 `availableLanguages: StateFlow<List<LanguageConfig>>` 可用语言列表
- [x] 定义 `isLoading: StateFlow<Boolean>` 加载状态
- [x] 实现 `switchLanguage(language: LanguageConfig)` 切换方法
- [x] 实现 `loadAvailableLanguages()` 加载语言列表方法
- [x] 添加语言切换成功/失败的事件通知
- [x] 处理语言切换过程中的异常情况

### 5.2 创建 UseCase
- [x] 创建 `domain/usecase` 包
- [x] 创建 `GetCurrentLanguageUseCase` 获取当前语言用例
- [x] 创建 `GetAvailableLanguagesUseCase` 获取可用语言用例
- [x] 创建 `SwitchLanguageUseCase` 切换语言用例
- [x] 在 `SwitchLanguageUseCase` 中保存语言到 DataStore
- [x] 在 `SwitchLanguageUseCase` 中发送语言切换事件
- [x] 在 `SwitchLanguageUseCase` 中更新 LocaleManager 配置
- [x] 添加用例的单元测试

---

## Phase 6: 设置页面实现

### 6.1 创建语言设置页面
- [x] 创建 `LanguageSettingsActivity.kt` 活动类
- [x] 设计布局文件 `activity_language_settings.xml`
- [x] 添加 Toolbar 显示"语言设置"标题
- [x] 添加返回按钮
- [x] 显示当前选中的语言（高亮显示）
- [x] 使用 RecyclerView 显示所有可用语言列表
- [x] 添加搜索框支持语言搜索（可选）
- [x] 绑定 LanguageViewModel
- [x] 监听语言列表数据变化并更新UI
- [x] 实现语言项点击事件处理

### 6.2 集成到应用设置
- [x] 在主设置页面添加"语言 / Language"菜单项
- [x] 设计语言菜单项布局（图标+文字+当前语言）
- [x] 显示当前语言的简称或图标
- [x] 添加右箭头指示符
- [x] 点击菜单项跳转到语言设置页面
- [x] 确保设置项在合适的分组中（通用设置区域）
- [x] 适配不同屏幕尺寸的显示

### 6.3 实现语言切换效果
- [x] 监听语言切换事件（通过 ViewModel）
- [x] 显示语言切换确认对话框（可选）
- [x] 显示"正在切换语言..."加载提示
- [x] 调用 Activity 的 `recreate()` 方法重建界面
- [x] 显示切换成功的 Toast 提示
- [x] 处理切换失败的情况并提示用户
- [x] 确保返回上一页面时语言已生效
- [x] 保存用户的语言选择到数据库

### 6.4 编译验证和安装验证
- [x] 修复编译错误（R引用、toast方法等）
- [x] 成功编译项目
- [x] 安装到设备验证功能正常

---

## Phase 7: 实时生效机制

### 7.1 实现 Activity 重建机制
- [x] 定义语言切换广播常量 `ACTION_LOCALE_CHANGED`
- [x] 创建 `LocaleChangeBroadcastReceiver` 广播接收器
- [x] 在 BaseActivity 的 `onResume()` 中注册广播接收器
- [x] 在 BaseActivity 的 `onPause()` 中注销广播接收器
- [x] 接收到广播后检查语言是否真的改变
- [x] 调用 `recreate()` 重建当前 Activity
- [x] 使用 LocalBroadcastManager 避免安全问题
- [x] 处理 Activity 栈中多个 Activity 的刷新

### 7.2 实现平滑切换动画
- [x] 创建淡入动画资源 `anim/fade_in.xml`
- [x] 创建淡出动画资源 `anim/fade_out.xml`
- [x] 在 `recreate()` 前调用 `overridePendingTransition()`
- [x] 设置进入和退出动画
- [x] 保存 Activity 当前状态到 Bundle
- [x] 在 `onCreate()` 中恢复保存的状态
- [x] 确保动画流畅不卡顿
- [x] 处理横竖屏切换时的状态保持

### 7.3 处理 Fragment 语言更新
- [x] 在 BaseFragment 中监听语言变化事件
- [x] 提供 `onLocaleChanged()` 回调方法供子类重写
- [x] 在语言变化时刷新 Fragment 的 UI
- [x] 更新 TextView、Button 等控件的文本
- [x] 刷新 RecyclerView Adapter 的数据
- [x] 重新加载图片资源（如果有语言特定图片）
- [x] 处理 ViewPager 中 Fragment 的刷新
- [x] 确保嵌套 Fragment 也能正确更新

### 7.4 编译验证和安装验证
- [x] 成功编译项目
- [x] 安装到设备验证功能正常

---

## Phase 8: 特殊场景处理 

### 8.1 处理日期时间格式化
- [ ] 创建 `DateTimeFormatter` 工具类
- [ ] 根据当前语言选择日期格式（如：中文 yyyy年MM月dd日，英文 MM/dd/yyyy）
- [ ] 实现相对时间显示（刚刚、5分钟前、1小时前等）
- [ ] 为不同语言定义相对时间文案
- [ ] 支持自定义日期时间格式模板
- [ ] 处理12小时制和24小时制差异
- [ ] 处理星期几的本地化显示
- [ ] 添加时区处理支持（如果需要）

### 8.2 处理货币和数字格式
- [ ] 创建 `NumberFormatUtils` 工具类
- [ ] 使用 `NumberFormat.getInstance(locale)` 格式化数字
- [ ] 处理千位分隔符差异（中文逗号、英文逗号等）
- [ ] 处理小数点符号差异（点号、逗号）
- [ ] 使用 `NumberFormat.getCurrencyInstance(locale)` 格式化货币
- [ ] 处理货币符号位置差异（前置、后置）
- [ ] 处理不同地区的货币代码（CNY、USD、JPY等）
- [ ] 添加百分比格式化支持

---

## Phase 9: 主题与多语言联动 

### 9.1 创建语言特定资源
- [ ] 创建 `drawable-zh/` 文件夹存放中文特定图片
- [ ] 创建 `drawable-en/` 文件夹存放英文特定图片
- [ ] 为不同语言准备带文字的图片资源
- [ ] 处理文本长度差异导致的布局问题
- [ ] 使用 ConstraintLayout 确保不同语言下布局不错乱
- [ ] 设置合理的 maxLines 和 ellipsize
- [ ] 为长文本语言（如德语）预留更多空间
- [ ] 测试不同语言下的UI显示效果

### 9.2 RTL语言支持准备
- [ ] 在布局文件中使用 `start/end` 替代 `left/right`
- [ ] 使用 `marginStart/marginEnd` 替代 `marginLeft/marginRight`
- [ ] 使用 `paddingStart/paddingEnd` 替代 `paddingLeft/paddingRight`
- [ ] 在 AndroidManifest.xml 中添加 `android:supportsRtl="true"`
- [ ] 测试阿拉伯语、希伯来语等 RTL 语言（如果支持）
- [ ] 检查图标方向是否需要镜像
- [ ] 确保 RecyclerView 滑动方向正确
- [ ] 处理自定义 View 的 RTL 适配

---

## Phase 10: 网络请求多语言

### 10.1 在网络层添加语言头
- [ ] 在 OkHttp Interceptor 中添加语言拦截器
- [ ] 添加 `Accept-Language` 请求头
- [ ] 从 LocaleManager 获取当前语言代码
- [ ] 格式化语言代码为标准格式（如：zh-CN, en-US）
- [ ] 在每个请求中自动添加语言头
- [ ] 在 API 接口的 Query 参数中添加 `lang` 字段（如果后端需要）
- [ ] 处理服务端不支持某语言时的降级策略

### 10.2 创建多语言响应解析
- [ ] 定义多语言响应数据类 `MultiLangText`
- [ ] 添加各语言字段（zh、en、ja 等）
- [ ] 创建扩展函数自动选择当前语言文本
- [ ] 实现 `MultiLangText.getText(locale: Locale)` 方法
- [ ] 处理某语言文本为空时的降级（使用默认语言）
- [ ] 在数据层自动转换多语言字段
- [ ] 更新现有 API 响应模型支持多语言
- [ ] 添加单元测试验证多语言解析逻辑

---

## Phase 11: 测试与优化 

### 11.1 单元测试
- [ ] 创建 `LocaleManagerTest` 测试类
- [ ] 测试 `getCurrentLocale()` 方法
- [ ] 测试 `setLocale()` 语言切换逻辑
- [ ] 测试 `getAvailableLanguages()` 返回正确列表
- [ ] 创建 `DataStore` 语言保存和读取测试
- [ ] 测试语言持久化是否正确
- [ ] 测试默认语言设置逻辑
- [ ] 测试非法语言代码的处理
- [ ] 创建 UseCase 单元测试
- [ ] 测试字符串资源加载是否正确

### 11.2 UI测试
- [ ] 创建 Espresso UI 测试类
- [ ] 测试语言设置页面是否正常显示
- [ ] 测试点击语言项是否能切换语言
- [ ] 测试语言切换后所有页面文字是否改变
- [ ] 测试 Activity 重建后语言是否保持
- [ ] 测试多个 Activity 栈的语言同步
- [ ] 测试横竖屏切换后语言是否保持
- [ ] 测试快速切换语言是否正常
- [ ] 测试语言选择对话框的交互
- [ ] 测试返回键和导航的行为

### 11.3 边界情况测试
- [ ] 测试系统语言与应用语言不一致的情况
- [ ] 测试应用后台运行时系统语言变化
- [ ] 测试语言资源文件缺失的降级处理
- [ ] 测试不支持的语言代码输入
- [ ] 测试网络请求失败时的语言处理
- [ ] 测试快速连续切换语言（防抖处理）
- [ ] 测试低内存设备的语言切换
- [ ] 测试杀掉应用后重新打开语言是否保持
- [ ] 测试 App 更新后语言设置是否保留

### 11.4 性能测试
- [ ] 使用 Android Profiler 测试语言切换响应时间
- [ ] 测试大量字符串资源加载的性能
- [ ] 检查语言切换时的内存使用
- [ ] 使用 LeakCanary 检查内存泄漏
- [ ] 优化 Activity 重建的性能（减少不必要的操作）
- [ ] 测试首次启动的语言初始化耗时
- [ ] 优化字符串资源加载（考虑缓存）
- [ ] 确保语言切换不影响应用流畅度

---

## Phase 12: 文档与发布 

### 12.1 编写使用文档
- [ ] 创建 `MULTI_LANGUAGE.md` 文档
- [ ] 编写"功能概述"章节
- [ ] 编写"如何添加新语言"步骤说明
- [ ] 说明如何创建新语言资源文件夹
- [ ] 说明如何添加语言配置到 LanguageConfig
- [ ] 编写"如何翻译字符串资源"指南
- [ ] 提供翻译工具和服务推荐
- [ ] 编写"如何使用语言切换 API"示例
- [ ] 提供代码示例展示各种使用场景
- [ ] 编写"常见问题解答"章节
- [ ] 添加架构图和流程图

### 12.2 创建示例代码
- [ ] 在 Demo 模块中创建语言切换示例页面
- [ ] 展示手动切换语言的方法
- [ ] 展示监听语言变化的方法
- [ ] 展示在不同组件中使用多语言的方法
- [ ] 展示网络请求多语言处理示例
- [ ] 展示日期时间多语言格式化示例
- [ ] 展示货币数字格式化示例
- [ ] 添加示例代码的注释说明

### 12.3 代码审查与重构
- [ ] 检查代码命名是否符合规范
- [ ] 检查代码注释是否完整
- [ ] 优化代码结构和设计模式
- [ ] 移除调试代码和无用注释
- [ ] 统一代码风格（使用 ktlint 或 detekt）
- [ ] 添加必要的文档注释（KDoc）
- [ ] 检查资源文件命名规范
- [ ] 进行 Code Review
- [ ] 修复 Review 中发现的问题
- [ ] 提交代码并创建 Pull Request
- [ ] 更新版本号和 Changelog

### 

---

## 关键成功指标

### 功能性指标
- ✅ 支持至少 3-5 种常用语言
- ✅ 语言切换无需重启应用即时生效
- ✅ 所有页面和对话框文字正确切换
- ✅ 网络请求携带正确的语言标识
- ✅ 用户语言选择持久化保存

### 性能指标
- ✅ 语言切换响应时间 < 500ms
- ✅ Activity 重建时间 < 300ms
- ✅ 首次启动语言初始化 < 100ms
- ✅ 无内存泄漏
- ✅ 应用包体积增加 < 2MB

### 用户体验指标
- ✅ 语言切换过渡动画流畅
- ✅ 所有文本布局无错乱
- ✅ 长文本自动省略显示
- ✅ 语言设置入口明显易找
- ✅ 支持搜索快速查找语言

## 

### 开发规范
1. 所有字符串必须放在 strings.xml 中，严禁硬编码
2. 使用 `start/end` 替代 `left/right` 为 RTL 做准备
3. 语言代码遵循 ISO 639-1 标准（zh、en、ja）
4. 地区代码遵循 ISO 3166-1 标准（CN、US、JP）

### 兼容性要求
- 最低 API Level: 21 (Android 5.0)
- 目标 API Level: 34 (Android 14)
- 支持 AndroidX
- 支持 Kotlin 1.9+

---

## ✅ 验收清单

### 必须完成项
- [ ] 所有 Phase 1-7 任务完成
- [ ] 通过所有单元测试
- [ ] 通过所有 UI 测试
- [ ] 无明显性能问题
- [ ] 无内存泄漏
- [ ] 使用文档完整

