# Git 使用规范

本文档定义了项目的 Git 使用规范和工作流，确保代码版本管理的规范性和协作效率。

## 📋 目录

- [分支策略](#分支策略)
- [提交规范](#提交规范)
- [工作流程](#工作流程)
- [代码审查](#代码审查)
- [版本标签](#版本标签)
- [最佳实践](#最佳实践)

## 🌿 分支策略

我们采用 **Git Flow** 分支策略，适合有计划发布周期的项目。

### 主要分支

#### main/master 分支
- **用途**: 生产环境代码
- **特点**: 始终保持稳定可发布状态
- **保护**: 禁止直接推送，只能通过 PR 合并
- **命名**: `main` 或 `master`

#### develop 分支
- **用途**: 开发环境代码
- **特点**: 包含最新的开发功能
- **来源**: 从 main 分支创建
- **命名**: `develop`

### 辅助分支

#### feature 分支
- **用途**: 功能开发
- **来源**: 从 develop 分支创建
- **合并到**: develop 分支
- **生命周期**: 功能完成后删除

```bash
# 创建功能分支
git checkout develop
git pull origin develop
git checkout -b feature/user-authentication

# 开发完成后合并
git checkout develop
git pull origin develop
git merge --no-ff feature/user-authentication
git branch -d feature/user-authentication
git push origin develop
```

#### release 分支
- **用途**: 发布准备
- **来源**: 从 develop 分支创建
- **合并到**: main 和 develop 分支
- **生命周期**: 发布完成后删除

```bash
# 创建发布分支
git checkout develop
git pull origin develop
git checkout -b release/v1.2.0

# 发布完成后合并到 main
git checkout main
git pull origin main
git merge --no-ff release/v1.2.0
git tag -a v1.2.0 -m "Release version 1.2.0"

# 合并到 develop
git checkout develop
git pull origin develop
git merge --no-ff release/v1.2.0
git branch -d release/v1.2.0
```

#### hotfix 分支
- **用途**: 紧急修复生产问题
- **来源**: 从 main 分支创建
- **合并到**: main 和 develop 分支
- **生命周期**: 修复完成后删除

```bash
# 创建热修复分支
git checkout main
git pull origin main
git checkout -b hotfix/critical-bug-fix

# 修复完成后合并
git checkout main
git pull origin main
git merge --no-ff hotfix/critical-bug-fix
git tag -a v1.2.1 -m "Hotfix version 1.2.1"

git checkout develop
git pull origin develop
git merge --no-ff hotfix/critical-bug-fix
git branch -d hotfix/critical-bug-fix
```

### 分支命名规范

#### 功能分支
```bash
feature/user-authentication      # 用户认证功能
feature/issue-123-add-login     # 关联 Issue 的功能
feature/payment-integration      # 支付集成功能
```

#### 修复分支
```bash
bugfix/fix-memory-leak          # 修复内存泄漏
bugfix/issue-456-crash-fix      # 修复崩溃问题
hotfix/critical-security-fix    # 紧急安全修复
```

#### 发布分支
```bash
release/v1.2.0                  # 版本发布
release/v2.0.0-beta             # Beta 版本
```

#### 其他分支
```bash
chore/update-dependencies       # 更新依赖
docs/update-readme             # 文档更新
refactor/user-service          # 重构用户服务
```

## 📝 提交规范

我们使用 [Conventional Commits](https://www.conventionalcommits.org/) 规范。

### 提交消息格式

```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

### 提交类型 (type)

| 类型 | 描述 | 示例 |
|------|------|------|
| `feat` | 新功能 | `feat(auth): add user login functionality` |
| `fix` | 修复 bug | `fix(api): resolve null pointer exception` |
| `docs` | 文档更新 | `docs(readme): update installation guide` |
| `style` | 代码格式调整 | `style: fix code formatting issues` |
| `refactor` | 代码重构 | `refactor(user): extract user service` |
| `perf` | 性能优化 | `perf(image): optimize image loading` |
| `test` | 测试相关 | `test(auth): add login unit tests` |
| `chore` | 构建/工具变动 | `chore: update gradle dependencies` |
| `ci` | CI/CD 相关 | `ci: add automated testing workflow` |
| `build` | 构建系统 | `build: update build configuration` |
| `revert` | 回滚提交 | `revert: revert commit abc123` |

### 作用域 (scope)

作用域表示提交影响的模块或功能：

```bash
feat(auth): add login validation
fix(network): handle timeout errors
docs(api): update endpoint documentation
test(user): add user repository tests
```

### 描述 (description)

- 使用现在时态："add" 而不是 "added"
- 首字母小写
- 结尾不加句号
- 简洁明了，不超过 50 个字符

### 正文 (body)

- 详细说明变更的原因和内容
- 与描述之间空一行
- 每行不超过 72 个字符

### 页脚 (footer)

- 关联 Issue：`Closes #123`
- 破坏性变更：`BREAKING CHANGE: API endpoint changed`

### 提交示例

#### 简单提交
```bash
feat(auth): add user login functionality

git commit -m "feat(auth): add user login functionality"
```

#### 详细提交
```bash
feat(auth): add user login functionality

- Implement login API integration
- Add form validation for username and password
- Update UI components for login screen
- Add error handling for authentication failures

Closes #123
```

#### 破坏性变更
```bash
feat(api): update user endpoint structure

BREAKING CHANGE: User API response format has changed.
The 'userInfo' field is now nested under 'profile'.

Before: { "userInfo": { "name": "John" } }
After: { "profile": { "userInfo": { "name": "John" } } }

Closes #456
```

## 🔄 工作流程

### 日常开发流程

#### 1. 开始新功能
```bash
# 切换到 develop 分支并更新
git checkout develop
git pull origin develop

# 创建功能分支
git checkout -b feature/new-feature

# 开发代码...
```

#### 2. 提交代码
```bash
# 添加文件
git add .

# 提交代码
git commit -m "feat(feature): add new functionality"

# 推送到远程
git push origin feature/new-feature
```

#### 3. 创建 Pull Request
- 在 GitHub/GitLab 上创建 PR
- 填写 PR 模板
- 指定审查者
- 等待代码审查

#### 4. 合并代码
```bash
# 审查通过后，合并到 develop
git checkout develop
git pull origin develop
git merge --no-ff feature/new-feature

# 删除功能分支
git branch -d feature/new-feature
git push origin --delete feature/new-feature
```

### 发布流程

#### 1. 创建发布分支
```bash
git checkout develop
git pull origin develop
git checkout -b release/v1.2.0
```

#### 2. 发布准备
- 更新版本号
- 更新 CHANGELOG.md
- 修复发现的 bug
- 运行完整测试

#### 3. 完成发布
```bash
# 合并到 main
git checkout main
git pull origin main
git merge --no-ff release/v1.2.0

# 创建标签
git tag -a v1.2.0 -m "Release version 1.2.0"
git push origin main --tags

# 合并回 develop
git checkout develop
git pull origin develop
git merge --no-ff release/v1.2.0
git push origin develop

# 删除发布分支
git branch -d release/v1.2.0
```

## 👀 代码审查

### Pull Request 模板

```markdown
## 📝 变更描述
简要描述此 PR 的变更内容

## 🎯 变更类型
- [ ] 新功能 (feature)
- [ ] Bug 修复 (fix)
- [ ] 代码重构 (refactor)
- [ ] 文档更新 (docs)
- [ ] 性能优化 (perf)
- [ ] 其他 (chore)

## 🧪 测试
- [ ] 单元测试已通过
- [ ] 集成测试已通过
- [ ] 手动测试已完成
- [ ] 代码覆盖率达标

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

### 审查要点

#### 代码质量
- [ ] 代码逻辑正确
- [ ] 遵循编码规范
- [ ] 没有明显的性能问题
- [ ] 异常处理完善
- [ ] 安全性考虑

#### 架构设计
- [ ] 符合项目架构
- [ ] 模块职责清晰
- [ ] 接口设计合理
- [ ] 依赖关系正确

#### 测试覆盖
- [ ] 核心逻辑有测试
- [ ] 边界条件考虑
- [ ] 异常场景测试
- [ ] 测试用例充分

### 审查流程

1. **自动检查**: CI/CD 自动运行测试和代码检查
2. **人工审查**: 至少一个团队成员审查
3. **修改反馈**: 根据反馈修改代码
4. **再次审查**: 确认修改满足要求
5. **合并代码**: 审查通过后合并

## 🏷️ 版本标签

### 语义化版本

我们使用 [语义化版本](https://semver.org/) (SemVer)：

```
MAJOR.MINOR.PATCH
```

- **MAJOR**: 不兼容的 API 修改
- **MINOR**: 向下兼容的功能性新增
- **PATCH**: 向下兼容的问题修正

### 标签命名

```bash
v1.0.0          # 正式版本
v1.0.0-alpha.1  # Alpha 版本
v1.0.0-beta.1   # Beta 版本
v1.0.0-rc.1     # Release Candidate
```

### 创建标签

```bash
# 创建带注释的标签
git tag -a v1.2.0 -m "Release version 1.2.0"

# 推送标签到远程
git push origin v1.2.0

# 推送所有标签
git push origin --tags
```

### 标签管理

```bash
# 查看所有标签
git tag

# 查看特定标签信息
git show v1.2.0

# 删除本地标签
git tag -d v1.2.0

# 删除远程标签
git push origin --delete v1.2.0
```

## 💡 最佳实践

### 提交频率

#### 建议做法
- 小步快跑，频繁提交
- 每个提交只包含一个逻辑变更
- 提交前确保代码可编译
- 提交前运行相关测试

#### 避免做法
- 一次提交包含多个不相关的变更
- 提交未完成的功能
- 提交编译失败的代码
- 提交包含调试代码的版本

### 分支管理

#### 建议做法
- 及时删除已合并的分支
- 保持分支名称简洁明了
- 定期同步上游分支
- 使用 `--no-ff` 合并保留分支历史

#### 避免做法
- 长期存在的功能分支
- 分支名称不规范
- 忘记删除无用分支
- 直接在主分支上开发

### 冲突解决

#### 预防冲突
```bash
# 开发前先同步
git checkout develop
git pull origin develop

# 开发过程中定期同步
git fetch origin
git rebase origin/develop
```

#### 解决冲突
```bash
# 拉取最新代码
git pull origin develop

# 解决冲突后
git add .
git commit -m "resolve merge conflicts"
```

### Git 配置

#### 全局配置
```bash
# 设置用户信息
git config --global user.name "Your Name"
git config --global user.email "your.email@example.com"

# 设置默认编辑器
git config --global core.editor "code --wait"

# 设置默认分支名
git config --global init.defaultBranch main

# 启用颜色输出
git config --global color.ui auto
```

#### 项目配置
```bash
# 设置换行符处理
git config core.autocrlf input  # Linux/Mac
git config core.autocrlf true   # Windows

# 忽略文件权限变更
git config core.filemode false
```

### Git Hooks

#### pre-commit 钩子
```bash
#!/bin/sh
# 运行代码格式检查
./gradlew ktlintCheck

# 运行单元测试
./gradlew test

# 检查通过才允许提交
if [ $? -ne 0 ]; then
    echo "Pre-commit checks failed. Please fix the issues before committing."
    exit 1
fi
```

#### commit-msg 钩子
```bash
#!/bin/sh
# 检查提交消息格式
commit_regex='^(feat|fix|docs|style|refactor|perf|test|chore|ci|build|revert)(\(.+\))?: .{1,50}'

if ! grep -qE "$commit_regex" "$1"; then
    echo "Invalid commit message format!"
    echo "Please use: type(scope): description"
    exit 1
fi
```

## 🔧 工具推荐

### Git 客户端
- **命令行**: Git CLI
- **图形界面**: SourceTree, GitKraken, GitHub Desktop
- **IDE 集成**: Android Studio Git 工具

### 辅助工具
- **commitizen**: 交互式提交消息生成
- **husky**: Git hooks 管理
- **lint-staged**: 只对暂存文件运行检查

### 安装 commitizen
```bash
npm install -g commitizen
npm install -g cz-conventional-changelog

# 使用
git cz
```

## 📚 参考资源

- [Git Flow 工作流](https://nvie.com/posts/a-successful-git-branching-model/)
- [Conventional Commits](https://www.conventionalcommits.org/)
- [语义化版本](https://semver.org/)
- [Git 官方文档](https://git-scm.com/doc)

---

*遵循这些规范将帮助团队更好地协作，保持代码历史的清晰和可追溯性。*