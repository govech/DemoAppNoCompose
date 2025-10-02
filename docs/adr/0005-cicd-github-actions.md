# ADR-0005: 选择 GitHub Actions 作为 CI/CD 平台

## 状态
已接受

## 背景
项目需要建立持续集成和持续部署（CI/CD）流水线，以自动化代码质量检查、测试执行、构建和发布流程。

## 决策
我们决定使用 GitHub Actions 作为项目的 CI/CD 平台。

## 理由

### GitHub Actions 优势
1. **原生集成**: 与 GitHub 仓库无缝集成，无需额外配置
2. **免费额度**: 公共仓库免费使用，私有仓库有充足的免费额度
3. **丰富生态**: 大量现成的 Actions 可以直接使用
4. **YAML 配置**: 简单直观的配置语法
5. **并行执行**: 支持多个 Job 并行运行，提高效率
6. **矩阵构建**: 支持多环境、多版本的矩阵构建

### 与其他平台的比较
- **Jenkins**: 需要自建服务器，维护成本高
- **Travis CI**: 免费额度有限，配置相对复杂
- **CircleCI**: 功能强大但学习成本较高
- **GitLab CI**: 需要使用 GitLab 平台

## 实现细节

### CI 工作流 (.github/workflows/ci.yml)
```yaml
name: CI

on:
  push:
    branches: [ main, master, develop ]
  pull_request:
    branches: [ main, master, develop ]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
    - name: Set up JDK 17
      uses: actions/setup-java@v4
    - name: Run tests
      run: ./gradlew test
    - name: Upload test results
      uses: actions/upload-artifact@v4
```

### 发布工作流 (.github/workflows/release.yml)
```yaml
name: Release

on:
  push:
    tags:
      - 'v*'

jobs:
  build-release:
    runs-on: ubuntu-latest
    steps:
    - name: Build release APK
      run: ./gradlew assembleRelease
    - name: Create Release
      uses: softprops/action-gh-release@v1
```

### 工作流程设计

#### CI 流程
1. **代码检出**: 获取最新代码
2. **环境准备**: 设置 JDK 17 和 Gradle 缓存
3. **代码质量检查**: 运行 ktlint 和 detekt
4. **单元测试**: 执行所有单元测试
5. **构建验证**: 构建 Debug APK
6. **结果上传**: 上传测试报告和构建产物

#### 发布流程
1. **触发条件**: 推送版本标签（如 v1.0.0）
2. **构建发布版**: 构建 Release APK
3. **APK 签名**: 使用密钥对 APK 进行签名
4. **创建发布**: 自动创建 GitHub Release
5. **上传产物**: 上传签名后的 APK

### 缓存策略
```yaml
- name: Cache Gradle packages
  uses: actions/cache@v4
  with:
    path: |
      ~/.gradle/caches
      ~/.gradle/wrapper
    key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*') }}
    restore-keys: |
      ${{ runner.os }}-gradle-
```

### 安全配置
- 使用 GitHub Secrets 存储敏感信息
- APK 签名密钥通过 Secrets 管理
- 限制工作流权限，遵循最小权限原则

## 后果

### 正面影响
- 自动化代码质量检查，提高代码质量
- 自动化测试执行，及早发现问题
- 自动化构建和发布，减少人工错误
- 提高开发效率和发布频率
- 完整的构建历史和产物管理

### 负面影响
- 依赖 GitHub 平台，存在供应商锁定风险
- 复杂的工作流可能导致构建时间较长
- 需要学习 GitHub Actions 的语法和最佳实践

### 风险缓解
- 定期备份工作流配置
- 监控构建时间，优化缓存策略
- 建立工作流配置的最佳实践文档
- 定期更新 Actions 版本，避免使用已弃用的版本

## 配置最佳实践

### 1. 版本管理
- 使用具体版本号而非 latest 标签
- 定期更新 Actions 版本
- 关注 GitHub 的弃用通知

### 2. 缓存优化
- 合理配置 Gradle 缓存
- 使用版本化的缓存键
- 定期清理过期缓存

### 3. 安全实践
- 敏感信息使用 Secrets 管理
- 限制工作流权限
- 定期轮换密钥

### 4. 性能优化
- 并行执行独立的 Job
- 使用矩阵构建减少重复配置
- 合理设置超时时间

## 维护计划

### 定期维护任务
- 每季度检查 Actions 版本更新
- 监控构建性能和成功率
- 审查和优化工作流配置
- 更新文档和最佳实践

### 监控指标
- 构建成功率
- 平均构建时间
- 测试覆盖率
- 发布频率

## 相关决策
- ADR-0001: 选择 MVVM 架构模式
- ADR-0009: 代码质量工具选择

---
*创建日期: 2024-01-01*  
*最后更新: 2024-01-01*  
*决策者: 开发团队*