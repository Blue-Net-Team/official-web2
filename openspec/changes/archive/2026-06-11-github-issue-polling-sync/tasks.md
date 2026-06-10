## 1. 配置层扩展

- [x] 1.1 `GitHubAppProperties` 新增 `pollingEnabled`、`pollingCron`、`pollingSinceDays` 字段及 getter
- [x] 1.2 `application.yml` 新增 `github.app.polling-enabled`、`github.app.polling-since-days` 和 `job.github-issue-sync.cron` 配置项
- [x] 1.3 更新 `docker/.env` 示例配置，添加新环境变量注释

## 2. GitHub API Client 扩展

- [x] 2.1 创建 `GitHubIssueListResult` record（number / title / body / state / htmlUrl）
- [x] 2.2 `GitHubIssueClient` 新增 `listIssues(Instant since)` 方法，调用 `GET /repos/{owner}/{repo}/issues?state=all&sort=updated&since=...`
- [x] 2.3 处理 GitHub API 分页（Link header 或 page 参数），per_page=100
- [x] 2.4 编写 `GitHubIssueClientTest` 单元测试：
  - [x] TC-001: 正常返回 Issue 列表
  - [x] TC-002: 返回空列表
  - [x] TC-003: GitHub API 返回 403/401 时抛出异常
  - [x] TC-004: 分页场景正确合并多页结果

## 3. 定时任务 Job 实现

- [x] 3.1 创建 `GitHubIssuePollingJob`，`@Scheduled(cron = "${job.github-issue-sync.cron:0 0 3 * * *}")`
- [x] 3.2 实现配置检查逻辑：pollingEnabled=false 或 GitHub App 未启用时跳过
- [x] 3.3 实现对账逻辑：遍历 Issue 列表 → findByGithubIssueNumber → 创建或更新状态
- [x] 3.4 状态映射逻辑：open→PENDING，closed→RESOLVED
- [x] 3.5 异常隔离：单条 Issue 处理失败不影响其他条目
- [x] 3.6 任务执行日志：开始/结束/成功数/失败数/跳过数

## 4. Job 层单元测试

- [x] 4.1 编写 `GitHubIssuePollingJobTest`：
  - [x] TC-005: 配置禁用时跳过执行
  - [x] TC-006: 空列表时直接结束
  - [x] TC-007: 本地不存在 → 调用 save 创建
  - [x] TC-008: 本地存在且状态一致 → 跳过更新
  - [x] TC-009: 本地存在且状态不一致 → 调用 updateStatus
  - [x] TC-010: 单条处理异常 → 继续处理后续条目
  - [x] TC-011: state=open → 映射为 PENDING
  - [x] TC-012: state=closed → 映射为 RESOLVED

## 5. 集成测试与验证

- [x] 5.1 运行全部相关单元测试（`GitHubIssuePollingJobTest`、`GitHubIssueClientTest`、`GitHubWebhookServiceTest`）确认通过 — **16 个测试全部通过，0 失败，0 错误**
- [x] 5.2 运行后端编译确认无编译错误
- [x] 5.3 检查新增类是否符合 DDD 分层规范
- [ ] 5.4 手动验证：临时修改 cron 为每分钟执行，观察日志输出是否正确
