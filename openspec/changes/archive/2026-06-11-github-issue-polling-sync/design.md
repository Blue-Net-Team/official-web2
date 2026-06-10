## Context

Webhook 双向同步已上线（`bug-report-github-webhook-sync`），可实时处理 `issues.opened/assigned/closed/reopened` 事件。但 Webhook 是"增量"机制，无法覆盖以下场景：

1. Webhook 配置前仓库中已存在的 Issue
2. 服务器宕机 / 网络抖动期间丢失的 Webhook 投递
3. Webhook Secret 轮换期间拒收的事件

因此需要定时任务作为兜底，每天从 GitHub API 拉取最近有更新的 Issue，与平台数据进行对账。

现有基础设施：
- `GitHubIssueClient`：已有 `createIssue`，需扩展 `listIssues`
- `GitHubAppTokenService`：提供 Installation Access Token
- `BugReportRepository`：已有 `findByGithubIssueNumber` + `save` + `updateStatus`
- 数据库 `github_issue_number` 唯一约束（V21）保证幂等性
- Spring `@Scheduled` 已启用（`@EnableScheduling` 在主类）

## Goals / Non-Goals

**Goals：**
- 每天定时拉取最近 N 天有更新的 GitHub Issue
- 本地不存在的 Issue → 反向同步创建 BugReport
- 本地已存在的 Issue → 仅同步状态差异（不覆盖 title/description）
- 全程异常捕获，单条失败不影响其他 Issue 处理
- 可配置开关、定时表达式、回溯天数

**Non-Goals：**
- 不同步 Issue 的标题、描述、标签等非状态字段（避免双向覆盖冲突）
- 不处理 GitHub 上已删除的 Issue（罕见场景，且 webhook 也不处理删除）
- 不做全量历史同步（首次部署时可手动触发一次全量）
- 不引入新中间件（仅用现有 GitHub API + 数据库）

## Decisions

### 1. 只拉取最近 7 天更新的 Issue（而非全量）
- **理由**：GitHub API `since` 参数支持按更新时间过滤，7 天窗口足够覆盖日常遗漏，API 调用量固定为 1 次/天（per_page=100）
- **替代方案**：全量分页拉取所有历史 Issue。 rejected：Issue 多了之后 API 调用次数不可控，且历史数据通常无需追补

### 2. 不同步 title / description，只做状态对账
- **理由**：GitHub 上用户可能编辑了 Issue 标题，平台不应该用旧数据覆盖。状态是双向同步的核心契约（assigned→处理中、closed→已解决、reopened→待处理）
- **替代方案**：对比 `updated_at` 决定谁覆盖谁。 rejected：需要平台记录 BugReport 更新时间戳，当前模型无此字段，改动成本高于收益

### 3. 使用 GitHub REST API 而非 GraphQL
- **理由**：REST API 的 `GET /repos/{owner}/{repo}/issues` 已满足需求，团队对现有 `RestTemplate` 调用模式熟悉
- **替代方案**：GitHub GraphQL API。 rejected：现有代码栈基于 REST，GraphQL 引入额外依赖和复杂度

### 4. 配置沿用 `job.*.cron` 模式
- **理由**：与现有 `OrphanFileCleanupJob`、`EliminatedUserDisableJob` 配置风格一致
- **具体**：`job.github-issue-sync.cron: 0 0 3 * * *`，`github.app.polling-enabled: true`，`github.app.polling-since-days: 7`

## Risks / Trade-offs

| 风险 | 缓解措施 |
|------|----------|
| GitHub API 限流（5000/小时） | 每次同步仅 1 次 API 调用，远低于限流 |
| 并发执行时与 Webhook 同时修改同一条记录 | 数据库唯一约束 + 状态对比跳过无变化更新，天然幂等 |
| 定时任务节点多实例同时执行 | Spring `@Scheduled` 默认无分布式锁，但幂等查询+唯一约束保证重复执行无害 |
| Issue 数量激增超过 100 条/7天 | per_page 可提升到 100（GitHub 最大值），仍不够再加分页逻辑 |
