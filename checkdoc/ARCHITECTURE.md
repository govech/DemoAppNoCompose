# Android MVVM 框架架构文档

## 概述

本项目采用现代化的 Android MVVM（Model-View-ViewModel）架构模式，结合 Clean Architecture 原则，构建了一个高度模块化、可测试、可维护的 Android 应用框架。

## 架构层次

### 1. 表现层（Presentation Layer）
- **Activity/Fragment**: UI 控制器，负责用户交互和生命周期管理
- **ViewModel**: 业务逻辑处理，状态管理，连接 View 和 Domain 层
- **UI State**: 使用 StateFlow/LiveData 管理 UI 状态

### 2. 领域层（Domain Layer）
- **UseCase**: 封装业务逻辑，单一职责原则
- **Repository Interface**: 数据访问抽象接口
- **Domain Model**: 业务实体模型

### 3. 数据层（Data Layer）
- **Repository Implementation**: 数据访问具体实现
- **Data Source**: 本地数据源（Room）和远程数据源（Retrofit）
- **Data Model**: 数据传输对象（DTO）

## 核心组件

### 配置管理（AppConfig）
统一的应用配置管理系统，支持：
- 网络配置（超时、重试、日志等）
- UI配置（主题、动画、字体等）
- 缓存配置（内存缓存、磁盘缓存等）
- 数据库配置（WAL模式、页面大小等）
- 性能配置（监控开关、阈值设置等）
- 安全配置（证书固定、加密等）

```kotlin
// 配置示例
AppConfig.init(context) {
    network {
        baseUrl = "https://api.example.com/"
        connectTimeout = 30
        enableNetworkLog = BuildConfig.DEBUG
    }
    performance {
        enablePerformanceMonitor = true
        memoryWarningThreshold = 100
    }
}
```

### 异常处理系统
基于密封类的分层异常处理机制：

```
AppException
├── NetworkException (网络异常)
│   ├── ConnectionError
│   ├── TimeoutError
│   ├── ServerError
│   └── ParseError
├── DatabaseException (数据库异常)
│   ├── ConnectionError
│   ├── QueryError
│   └── UpdateError
├── BusinessException (业务异常)
│   ├── NotLoginError
│   ├── PermissionDeniedError
│   └── InvalidParameterError
└── SystemException (系统异常)
    ├── OutOfMemoryError
    ├── FileOperationError
    └── UnknownError
```

### 性能监控系统
全方位的性能监控解决方案：

#### 启动性能监控（StartupTimeTracker）
- 追踪应用启动各个阶段的耗时
- 支持冷启动、温启动、热启动监控
- 提供启动性能报告和优化建议

#### 内存监控（MemoryMonitor）
- 实时监控内存使用情况
- 内存压力检测和自动清理
- 内存泄漏预警机制

#### 网络性能监控（NetworkPerformanceInterceptor）
- 网络请求耗时统计
- 慢请求检测和报告
- 网络错误率统计

### 内存管理系统
高效的内存管理和缓存机制：

#### 对象池（NetworkObjectPool）
- 网络请求对象复用
- 减少对象创建和GC压力
- 支持自定义对象池配置

#### LRU缓存（MemoryCache）
- 基于LRU算法的内存缓存
- 支持过期时间设置
- 内存压力时自动清理

#### 图片缓存管理（ImageCacheManager）
- 集成Coil图片加载库
- 内存和磁盘缓存优化
- 低内存时自动清理策略

## 数据流

```
UI (Activity/Fragment)
    ↕ (UI Events / UI State)
ViewModel
    ↕ (Domain Models)
UseCase
    ↕ (Domain Models)
Repository Interface
    ↕ (Domain Models)
Repository Implementation
    ↕ (Data Models)
Data Sources (Local/Remote)
```

## 依赖注入

使用 Hilt 进行依赖注入，模块化管理：

- **AppModule**: 应用级别的依赖
- **NetworkModule**: 网络相关依赖
- **DatabaseModule**: 数据库相关依赖
- **RepositoryModule**: Repository 实现绑定

## 线程管理

- **Main Thread**: UI 更新和用户交互
- **IO Thread**: 网络请求和文件操作
- **Default Thread**: CPU 密集型计算
- **Unconfined**: 协程调度器

## 错误处理策略

### 1. 异常恢复机制
- **NetworkExceptionRecovery**: 网络异常自动重试
- **DatabaseExceptionRecovery**: 数据库异常恢复
- **ExceptionHandler**: 统一异常处理入口

### 2. 用户友好提示
- **ErrorMessageMapper**: 异常消息映射
- 支持多语言错误提示
- 区分技术错误和业务错误

## 测试策略

### 单元测试
- ViewModel 测试：业务逻辑验证
- UseCase 测试：用例场景测试
- Repository 测试：数据访问测试

### 集成测试
- 数据库操作测试
- 网络请求测试
- 端到端流程测试

### UI测试
- 用户交互流程测试
- 界面状态验证
- 异常场景处理测试

## 性能优化

### 启动优化
- 延迟初始化非关键组件
- 异步加载资源
- 启动时间监控和分析

### 内存优化
- 对象池复用
- LRU缓存管理
- 内存泄漏检测

### 网络优化
- 请求合并和缓存
- 连接池复用
- 压缩和优化传输

## 安全考虑

### 网络安全
- HTTPS 强制使用
- 证书固定（Certificate Pinning）
- 请求签名验证

### 数据安全
- 敏感数据加密存储
- SQL注入防护
- 数据传输加密

## 扩展性设计

### 模块化架构
- 功能模块独立
- 接口抽象解耦
- 插件化扩展支持

### 配置化管理
- 运行时配置调整
- A/B测试支持
- 特性开关控制

## 监控和日志

### 日志系统
- 分级日志记录
- 文件日志持久化
- 敏感信息过滤

### 性能监控
- 实时性能指标收集
- 异常情况自动上报
- 性能趋势分析

## 版本兼容性

- **最低支持版本**: Android 5.0 (API 21)
- **目标版本**: Android 14 (API 34)
- **编译版本**: Android 14 (API 34)

## 技术栈

### 核心框架
- **Kotlin**: 主要开发语言
- **Coroutines**: 异步编程
- **Flow**: 响应式编程

### UI框架
- **View System**: 传统View系统
- **ViewBinding**: 视图绑定
- **Material Design**: UI设计规范

### 网络框架
- **Retrofit**: HTTP客户端
- **OkHttp**: 网络请求库
- **Gson**: JSON解析

### 数据库
- **Room**: 本地数据库
- **SQLite**: 底层数据库引擎

### 依赖注入
- **Hilt**: 依赖注入框架

### 图片加载
- **Coil**: 图片加载库

### 测试框架
- **JUnit**: 单元测试
- **Mockito**: Mock框架
- **Espresso**: UI测试

## 最佳实践

### 代码规范
- 遵循 Kotlin 官方编码规范
- 使用 ktlint 进行代码格式化
- 使用 detekt 进行静态代码分析

### 架构原则
- 单一职责原则
- 依赖倒置原则
- 开闭原则
- 接口隔离原则

### 性能原则
- 避免内存泄漏
- 合理使用缓存
- 异步处理耗时操作
- 优化启动性能

## 未来规划

### 短期目标
- 完善单元测试覆盖率
- 优化启动性能
- 增强错误处理机制

### 长期目标
- 模块化重构
- Compose UI 迁移
- 微服务架构支持
- 跨平台扩展

---

*本文档会随着项目的发展持续更新，请关注版本变更记录。*