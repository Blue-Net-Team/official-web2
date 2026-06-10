## Why

Webhook 实时同步（`issues.opened/assigned/closed/reopened`）是增量的，存在天然盲区：Webhook 配置前已有的 Issue 永远不会触发事件；服务器宕机或网络抖动期间丢失的 Webhook 无法自动回补。需要一个轻量级定时任务作为兜底，每天拉取最近有更新的 Issue，查漏补缺。

## What Changes

- 新增 `GitHubIssuePollingJob`：Spring `@Scheduled` 定时任务，每天凌晨执行
- 扩展 `GitHubIssueClient`：新增 `listIssues(Instant since)` 方法，调用 GitHub REST API 获取最近更新的 Issue 列表
- 新增 `GitHubIssueListResult`：Issue 列表项 DTO（number / title / body / state / htmlUrl）
- 新增 `application.yml` 配置项：`job.github-issue-sync.cron`、`github.app.polling-enabled`、`github.app.polling-since-days`
- 对账逻辑：遍历拉取的 Issue，本地不存在则反向同步创建 BugReport；存在则仅对比状态，有差异时更新
- **不覆盖 title / description**：避免 GitHub 上的编辑与平台数据互相冲突

## Capabilities

### New Capabilities
- `github-issue-polling`：定时从 GitHub 拉取最近更新的 Issue 列表，与平台 Bug 报告进行状态对账和补缺

### Modified Capabilities
- （无现有 spec 级别需求变更，仅新增能力）

## Impact

- 新增 2 个 Java 类（`GitHubIssuePollingJob`、`GitHubIssueListResult`）
- 修改 `GitHubIssueClient`（新增方法）、`GitHubAppProperties`（新增配置字段）
- `application.yml` 新增定时任务配置
- 新增单元测试和集成测试
- 依赖现有 `BugReportRepository.findByGithubIssueNumber` 和 `github_issue_number` 唯一约束保证幂等性
