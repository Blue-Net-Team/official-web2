## 1. 配置层扩展

- [x] 1.1 `GitHubAppProperties` 新增 `webhookSecret` 字段及 getter
- [x] 1.2 `GitHubAppProperties.isEnabled()` 更新：webhook 相关功能仅在 `webhookSecret` 配置时启用
- [x] 1.3 更新 `docker/.env` 示例配置，添加 `GITHUB_APP_WEBHOOK_SECRET` 注释说明

## 2. 领域层与 Repository 扩展

- [x] 2.1 `BugReportRepository` 接口新增 `Optional<BugReport> findByGithubIssueNumber(Integer issueNumber)`
- [x] 2.2 `BugReportRepositoryImpl` 实现 `findByGithubIssueNumber`，通过 MyBatis wrapper 按 `github_issue_number` 查询
- [x] 2.3 编写 Repository 层单元测试（使用内存数据库或 Mock）

## 3. Webhook 签名验证组件

- [x] 3.1 创建 `GitHubWebhookVerifier`，实现 HMAC-SHA256 签名验证逻辑
- [x] 3.2 支持读取 `X-Hub-Signature-256` header，提取 sha256= 后的签名值
- [x] 3.3 支持配置未启用时拒绝所有请求
- [x] 3.4 编写 `GitHubWebhookVerifierTest` 单元测试：
  - [x] TC-001: 正确签名通过验证
  - [x] TC-002: 错误签名抛出异常
  - [x] TC-003: 缺失 header 抛出异常
  - [x] TC-004: 配置未启用时抛出异常

## 4. Webhook 业务处理服务

- [x] 4.1 创建 `GitHubWebhookService`，注入 `BugReportRepository`
- [x] 4.2 实现 `processIssuesEvent(String payload)` 方法，解析 `action` 字段分发处理
- [x] 4.3 实现 `handleIssueOpened(IssuePayload)`：检查 body 是否含 `<!-- bluenet-bug-report -->` 标记，无标记则反向同步创建 BugReport
- [x] 4.4 实现 `handleIssueAssigned/Closed/Reopened(Integer issueNumber)`：按 issue number 查找并更新状态
- [x] 4.5 所有异常捕获并记录日志，不向上抛出
- [x] 4.6 编写 `GitHubWebhookServiceTest` 单元测试：
  - [x] TC-005: `opened` + 无标记 → 创建新 BugReport（PENDING）
  - [x] TC-006: `opened` + 有标记 → 忽略不处理
  - [x] TC-007: `assigned` → 更新状态为 IN_PROGRESS
  - [x] TC-008: `closed` → 更新状态为 RESOLVED
  - [x] TC-009: `reopened` → 更新状态为 PENDING
  - [x] TC-010: 事件对应的 BugReport 不存在 → 记录日志不抛异常
  - [x] TC-011: `opened` + description 为空 → 使用 title 作为 description
  - [x] TC-012: `opened` + title 为空 → 记录日志不创建
  - [x] TC-013: 不支持的事件类型（如 labeled）→ 忽略

## 5. Webhook Controller

- [x] 5.1 创建 `GitHubWebhookController`，`POST /api/v1/github/webhook`
- [x] 5.2 Controller 读取原始 request body 和 `X-Hub-Signature-256` header
- [x] 5.3 先调用 `GitHubWebhookVerifier.verify()`，失败返回 401
- [x] 5.4 验证通过后调用 `GitHubWebhookService.processIssuesEvent()`
- [x] 5.5 始终返回 200（业务异常不向外传播）
- [x] 5.6 不使用 `@RequiresPermission`（由签名验证保护）→ 使用 `AccessLevel.PUBLIC`
- [x] 5.7 编写 `GitHubWebhookControllerTest` 集成测试：
  - [x] TC-014: 有效签名 + 有效 payload → 200
  - [x] TC-015: 无效签名 → 401
  - [x] TC-016: 非 issues 事件类型 → 200 但不处理

## 6. 现有同步服务更新

- [x] 6.1 修改 `GitHubIssueSyncService.buildBody()`，在 body 末尾追加 `

<!-- bluenet-bug-report -->`
- [x] 6.2 更新 `GitHubIssueSyncServiceTest`：
  - [x] TC-017: 验证同步后的 Issue body 包含隐藏标记
  - [x] TC-018: 确保标记不影响现有 body 结构（截图链接、环境信息等正常）

## 7. 测试验证与收尾

- [x] 7.1 运行全部 Bug 报告相关单元测试（`BugReport*Test`、`GitHub*Test`）确认通过
- [x] 7.2 运行全部后端编译（`mvnw test compile`）确认无编译错误
- [x] 7.3 检查新增类是否符合 DDD 分层规范（无跨层泄漏）
- [x] 7.4 检查 `PermissionScanner` 不会因新增 Controller 而报错（webhook Controller 使用 `@RequiresPermission(access = PUBLIC)`）
