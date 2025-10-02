# 贡献指南

感谢您对本项目的关注！本文档将指导您如何为项目做出贡献。

## 📋 目录

- [开发环境设置](#开发环境设置)
- [代码规范](#代码规范)
- [提交规范](#提交规范)
- [分支策略](#分支策略)
- [Pull Request 流程](#pull-request-流程)
- [代码审查](#代码审查)
- [测试要求](#测试要求)
- [发布流程](#发布流程)

## 🛠️ 开发环境设置

### 必需工具

- **Android Studio**: Arctic Fox 或更高版本
- **JDK**: 17 或更高版本
- **Kotlin**: 1.9.0 或更高版本
- **Gradle**: 8.0 或更高版本
- **Git**: 2.30 或更高版本

### 项目设置

1. **克隆项目**
   ```bash
   git clone https://github.com/your-repo/DemoAppNoCompose.git
   cd DemoAppNoCompose
   ```

2. **安装依赖**
   ```bash
   ./gradlew build
   ```

3. **运行代码质量检查**
   ```bash
   ./scripts/code-quality.sh
   ```

4. **运行测试**
   ```bash
   ./gradlew test
   ```

## 📝 代码规范

### Kotlin 代码风格

我们使用 [ktlint](https://ktlint.github.io/) 来保证代码格式的一致性。

**自动格式化代码：**
```bash
./gradlew ktlintFormat
```

**检查代码格式：**
```bash
./gradlew ktlintCheck
```

### 代码质量

我们使用 [detekt](https://detekt.github.io/) 进行静态代码分析。

**运行 detekt：**
```bash
./gradlew detekt
```

### 命名规范

- **类名**: PascalCase (例: `UserRepository`)
- **函数名**: camelCase (例: `getUserInfo`)
- **变量名**: camelCase (例: `userName`)
- **常量**: UPPER_SNAKE_CASE (例: `MAX_RETRY_COUNT`)
- **包名**: 小写，用点分隔 (例: `com.example.feature`)

### 文档注释

所有公共 API 都应该有 KDoc 注释：

```kotlin
/**
 * 用户仓库接口
 * 提供用户相关的数据操作
 * 
 * @author Your Name
 * @since 1.0.0
 */
interface UserRepository {
    
    /**
     * 根据用户ID获取用户信息
     * 
     * @param userId 用户ID
     * @return 用户信息流
     * @throws UserNotFoundException 当用户不存在时
     */
    suspend fun getUserById(userId: String): Flow<User>
}
```

## 📋 提交规范

我们使用 [Conventional Commits](https://www.conventionalcommits.org/) 规范。

### 提交消息格式

```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

### 提交类型

- `feat`: 新功能
- `fix`: 修复bug
- `docs`: 文档更新
- `style`: 代码格式调整（不影响功能）
- `refactor`: 代码重构
- `perf`: 性能优化
- `test`: 测试相关
- `chore`: 构建过程或辅助工具的变动
- `ci`: CI/CD 相关

### 示例

```bash
feat(auth): add user login functionality

- Implement login API integration
- Add form validation
- Update UI components

Closes #123
```

## 🌿 分支策略

我们使用 Git Flow 分支策略：

### 主要分支

- **main/master**: 生产环境代码
- **develop**: 开发环境代码
- **feature/***: 功能开发分支
- **release/***: 发布准备分支
- **hotfix/***: 紧急修复分支

### 分支命名规范

- `feature/user-authentication`
- `feature/issue-123-add-login`
- `bugfix/fix-memory-leak`
- `hotfix/critical-crash-fix`
- `release/v1.2.0`

## 🔄 Pull Request 流程

### 创建 PR 前

1. **确保代码质量**
   ```bash
   ./scripts/code-quality.sh
   ```

2. **运行所有测试**
   ```bash
   ./gradlew test
   ```

3. **更新文档**（如果需要）

### PR 模板

创建 PR 时，请使用以下模板：

```markdown
## 📝 变更描述
简要描述此 PR 的变更内容

## 🎯 变更类型
- [ ] 新功能
- [ ] Bug 修复
- [ ] 代码重构
- [ ] 文档更新
- [ ] 性能优化
- [ ] 其他

## 🧪 测试
- [ ] 单元测试已通过
- [ ] 集成测试已通过
- [ ] 手动测试已完成

## 📋 检查清单
- [ ] 代码遵循项目规范
- [ ] 已添加必要的测试
- [ ] 文档已更新
- [ ] 无破坏性变更
- [ ] 已自测功能

## 🔗 相关 Issue
Closes #123

## 📸 截图（如适用）
[添加截图或GIF]

## 📝 额外说明
[任何额外的说明或注意事项]
```

## 👀 代码审查

### 审查要点

1. **功能正确性**: 代码是否实现了预期功能
2. **代码质量**: 是否遵循最佳实践
3. **性能**: 是否有性能问题
4. **安全性**: 是否存在安全隐患
5. **可维护性**: 代码是否易于理解和维护
6. **测试覆盖**: 是否有足够的测试

### 审查流程

1. **自动检查**: CI/CD 流水线自动运行
2. **人工审查**: 至少需要一个团队成员审查
3. **修改反馈**: 根据反馈修改代码
4. **最终批准**: 审查通过后合并

## 🧪 测试要求

### 测试类型

1. **单元测试**: 测试单个组件或函数
2. **集成测试**: 测试组件间的交互
3. **UI 测试**: 测试用户界面

### 测试覆盖率

- 新代码的测试覆盖率应达到 80% 以上
- 核心业务逻辑必须有测试覆盖

### 运行测试

```bash
# 运行所有测试
./gradlew test

# 运行特定测试
./gradlew test --tests "UserRepositoryTest"

# 生成测试报告
./gradlew test jacocoTestReport
```

## 🚀 发布流程

### 版本号规范

我们使用 [语义化版本](https://semver.org/) (SemVer)：

- **主版本号**: 不兼容的 API 修改
- **次版本号**: 向下兼容的功能性新增
- **修订号**: 向下兼容的问题修正

### 发布步骤

1. **创建发布分支**
   ```bash
   git checkout -b release/v1.2.0
   ```

2. **运行发布脚本**
   ```bash
   ./scripts/release.sh patch
   ```

3. **创建 GitHub Release**
   - 上传 APK 文件
   - 填写发布说明
   - 标记为正式发布

## 🆘 获取帮助

如果您在贡献过程中遇到问题，可以通过以下方式获取帮助：

- **GitHub Issues**: 报告 bug 或提出功能请求
- **GitHub Discussions**: 讨论想法或提问
- **代码审查**: 在 PR 中请求帮助

## 📄 许可证

通过贡献代码，您同意您的贡献将在与项目相同的许可证下发布。

---

感谢您的贡献！🎉