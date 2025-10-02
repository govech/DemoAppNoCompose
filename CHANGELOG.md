# 更新日志

本文档记录了项目的所有重要变更。

格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/)，
并且本项目遵循 [语义化版本](https://semver.org/lang/zh-CN/)。

## [未发布]

### 新增
- 添加了Domain层架构，实现清晰的分层设计
- 完善的异常处理机制，支持智能异常恢复
- 统一配置管理系统，支持环境切换
- 性能监控集成，包括启动时间、内存、网络监控
- 内存管理优化，包括对象池、LRU缓存、图片缓存
- CI/CD 配置，支持自动化构建和发布
- 完整的项目文档体系，包括架构文档、API文档、ADR等

### 改进
- 优化了应用启动性能
- 提升了内存使用效率
- 增强了错误处理和用户体验
- 完善了代码质量检查工具
- 升级CI/CD配置，修复已弃用的GitHub Actions

### 修复
- 修复了配置初始化顺序问题
- 解决了内存泄漏问题
- 修复了网络请求异常处理
- 修复CI/CD构建失败问题（升级actions/upload-artifact到v4）

## [1.0.0] - 2024-01-01

### 新增
- 初始版本发布
- 基础 MVVM 架构实现
- 用户登录功能
- 网络请求封装
- 数据库集成
- 基础 UI 组件

### 技术栈
- Kotlin
- Android Architecture Components
- Hilt 依赖注入
- Retrofit 网络请求
- Room 数据库
- Coroutines & Flow
- ViewBinding

---

## 版本说明

### 版本类型
- **主版本号**: 不兼容的 API 修改
- **次版本号**: 向下兼容的功能性新增  
- **修订号**: 向下兼容的问题修正

### 变更类型
- **新增**: 新功能
- **改进**: 对现有功能的改进
- **修复**: Bug 修复
- **移除**: 移除的功能
- **安全**: 安全相关的修复
- **废弃**: 即将移除的功能

### 链接格式
[未发布]: https://github.com/your-repo/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/your-repo/releases/tag/v1.0.0