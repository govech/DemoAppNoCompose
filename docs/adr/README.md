# 架构决策记录 (ADR) 索引

本目录包含了项目的所有架构决策记录。ADR 记录了重要的架构决策、背景、理由和后果。

## ADR 列表

| 编号 | 标题 | 状态 | 日期 |
|------|------|------|------|
| [ADR-0001](./0001-mvvm-architecture-choice.md) | 选择 MVVM 架构模式 | 已接受 | 2024-01-01 |
| [ADR-0002](./0002-hilt-dependency-injection.md) | 选择 Hilt 作为依赖注入框架 | 已接受 | 2024-01-01 |
| [ADR-0003](./0003-retrofit-network-framework.md) | 选择 Retrofit + OkHttp 作为网络框架 | 已接受 | 2024-01-01 |
| [ADR-0004](./0004-room-database-choice.md) | 选择 Room 作为本地数据库 | 已接受 | 2024-01-01 |
| [ADR-0005](./0005-cicd-github-actions.md) | 选择 GitHub Actions 作为 CI/CD 平台 | 已接受 | 2024-01-01 |

## ADR 状态说明

- **提议中 (Proposed)**: 正在讨论中的决策
- **已接受 (Accepted)**: 已经采纳的决策
- **已废弃 (Deprecated)**: 不再推荐使用的决策
- **已替代 (Superseded)**: 被新决策替代的决策

## 如何创建新的 ADR

1. 复制 ADR 模板文件
2. 按照编号顺序命名文件 (例: `0007-new-decision.md`)
3. 填写决策内容
4. 更新本索引文件
5. 提交 Pull Request

## ADR 模板

```markdown
# ADR-XXXX: 决策标题

## 状态
[提议中/已接受/已废弃/已替代]

## 背景
描述需要做出决策的背景和问题

## 决策
描述我们选择的解决方案

## 理由
解释为什么选择这个方案，包括优缺点分析

## 后果
描述这个决策带来的影响，包括正面和负面影响

## 相关决策
列出相关的其他 ADR

---
*创建日期: YYYY-MM-DD*  
*最后更新: YYYY-MM-DD*  
*决策者: 决策者姓名*
```

## 参考资料

- [Architecture Decision Records](https://adr.github.io/)
- [Documenting Architecture Decisions](https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions)
- [ADR Tools](https://github.com/npryce/adr-tools)