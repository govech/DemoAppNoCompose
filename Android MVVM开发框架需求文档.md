## Android MVVM开发框架需求文档

### 一、技术栈确认 

- **语言**：Kotlin
- **架构**：MVVM
- **响应式**：Flow + StateFlow
- **UI**：传统View体系（XML布局）
- **依赖注入**：Hilt
- **图片加载**：Coil
- **最低版本**：API 23 (Android 6.0)

------

### 二、核心功能模块

#### 1. 架构分层

```
- Presentation层：Activity/Fragment + ViewModel
- Domain层：UseCase（可选，简单项目可省略）
- Data层：Repository + DataSource (Remote + Local)
```

#### 2. 网络层

- **基础框架**：Retrofit + OkHttp + Flow
- 功能需求：
  - 统一请求响应封装（BaseResponse）
  - 全局错误处理（网络异常、业务异常、Token过期等）
  - 请求/响应日志拦截器（仅Debug环境）
  - 超时配置、重试机制
  - 请求头统一管理（Token、语言、版本号等）
  - 支持多BaseUrl切换

#### 3. 本地存储

- **Room数据库**：复杂数据持久化
- **DataStore**：替代SharedPreferences，存储配置信息
- **文件存储**：图片、文档缓存管理

#### 4. Base基类体系

- BaseActivity：
  - 统一生命周期管理
  - 沉浸式状态栏
  - Loading加载框
  - Toast统一显示
  - 权限请求封装
  - 事件总线（可选）
- BaseFragment：
  - 懒加载机制
  - 数据预加载
  - 状态保存恢复
- BaseViewModel：
  - 统一Loading状态管理
  - 统一异常捕获
  - 生命周期感知
  - Flow异常处理封装
- BaseRepository：
  - 统一数据源切换逻辑
  - 网络/本地数据融合
- BaseAdapter：
  - RecyclerView万能适配器
  - 多类型布局支持
  - 点击事件封装
  - 数据差异更新（DiffUtil）

#### 5. 状态管理

- **UiState密封类**：

```
  - Loading（加载中）
  - Success（成功）
  - Error（失败 + 错误信息）
  - Empty（空数据）
```

- **统一状态布局**：可切换的Loading/Error/Empty/Content视图

####  6. 通用工具类

- 网络相关：
  - 网络状态监听（实时）
  - 网络类型判断（WiFi/4G/5G）
- UI相关：
  - 屏幕尺寸、密度获取
  - dp/px转换
  - 软键盘显示/隐藏
  - 状态栏/导航栏颜色管理
- 数据处理：
  - JSON解析工具
  - 日期格式化
  - 加密工具（MD5、AES）
  - 正则验证（手机号、邮箱等）
- 文件相关：
  - 文件读写
  - 缓存管理
  - 图片压缩
- 权限管理：
  - 统一权限请求封装
  - 权限说明对话框

#### 7. Kotlin扩展函数

- View扩展（可见性、点击防抖）
- Context扩展（Toast、跳转）
- String扩展（判空、格式化）
- Flow扩展（统一异常处理）
- LiveData/StateFlow扩展

#### 8. 自定义UI组件

- **StateLayout**：状态切换布局
- **LoadingDialog**：全局加载框
- **CommonDialog**：通用对话框
- **下拉刷新 + 上拉加载**：SmartRefreshLayout或自定义
- **TitleBar**：通用标题栏

####  9. 埋点统计系统

- 基础能力：
  - 页面浏览埋点（自动）
  - 点击事件埋点（手动）
  - 接口调用埋点（自动）
  - 异常埋点（自动）
- 设计模式：
  - 策略模式支持多平台（友盟、神策等）
  - 离线缓存 + 批量上报
  - 埋点开关控制

#### 10. 国际化支持

- 多语言资源：
  - 中文（默认）
  - 英文
  - 可扩展其他语言
- 动态切换：
  - 应用内语言切换（不跟随系统）
  - 配置持久化

#### 11. 暗黑模式

- 适配方案：
  - values-night资源适配
  - 动态切换（不跟随系统可选）
  - 主题色统一管理
- **配置保存**：用户选择持久化

#### 12. 全局配置管理

- 环境配置：
  - 开发环境（Dev）
  - 测试环境（Test）
  - 生产环境（Production）
  - BuildConfig自动切换BaseUrl
- 应用配置：
  - 版本信息管理
  - 渠道号管理
  - 全局常量定义

#### 13. 异常处理

- 崩溃捕获：
  - 全局UncaughtExceptionHandler
  - 崩溃日志本地保存
  - 崩溃信息上报
- 业务异常：
  - 统一异常分类
  - 友好提示信息映射

#### 14. 日志系统

- **分级日志**：Debug/Info/Warn/Error
- **格式化输出**：JSON美化、线程信息、位置信息
- **日志开关**：Release环境自动关闭
- **文件日志**：可选本地保存

#### 15. 混淆配置

- 保留必要的类和方法
- 第三方库混淆规则
- 实体类防混淆

------

### 三、项目规范

#### 1. 包结构规范

按功能模块划分，清晰的分层结构

####  2. 命名规范

- Activity：`XxxActivity`
- Fragment：`XxxFragment`
- ViewModel：`XxxViewModel`
- Repository：`XxxRepository`
- Adapter：`XxxAdapter`

#### 3. 资源文件规范

- Layout：`activity_xxx / fragment_xxx / item_xxx`
- Drawable：`ic_xxx / bg_xxx / shape_xxx`
- Color：统一colors.xml管理
- String：统一strings.xml管理，支持国际化

#### 📖 4. 代码注释规范

关键类和方法需要KDoc注释