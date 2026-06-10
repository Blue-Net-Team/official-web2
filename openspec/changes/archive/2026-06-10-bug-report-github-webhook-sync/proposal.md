## Why

当前 Bug 同步机器人仅实现了**单向同步**（平台提交 Bug → GitHub 创建 Issue）。当开发者在 GitHub 上直接新建 Issue、关闭 Issue 或分配 Issue 时，平台侧 Bug 状态不会自动更新，导致管理平台与 GitHub 状态不一致，管理员需要两边手动维护。此外，GitHub 上直接创建的 Issue 完全不会出现在平台管理页中，形成信息孤岛。

## What Changes

- **新增 GitHub Webhook 接收端**：`POST /api/v1/github/webhook`，接收 GitHub `issues` 事件
- **新增 Webhook 安全验证**：HMAC-SHA256 签名验证，防止伪造请求
- **新增状态自动更新逻辑**：根据 GitHub Issue 事件自动流转平台 Bug 状态
  - `assigned` → `IN_PROGRESS`
  - `closed` → `RESOLVED`
  - `reopened` → `PENDING`
- **新增反向同步**：GitHub 直接创建的 Issue 自动同步到平台 Bug 报告（仅 title + description，其余字段为空）
- **更新平台→GitHub 同步标记**：在 Issue body 中插入隐藏 HTML 注释 `<!-- bluenet-bug-report -->`，用于 webhook 区分 Issue 来源
- **新增配置项**：`github.app.webhook-secret`，用于 webhook 签名验证
- **新增 Repository 查询方法**：通过 `github_issue_number` 查找 Bug 报告

## Capabilities

### New Capabilities
- `github-webhook-handler`: 接收 GitHub Webhook 事件、验证签名、分发处理
- `bug-report-auto-sync`: Bug 报告状态自动更新与 GitHub Issue 反向同步

### Modified Capabilities
- `github-issue-sync`: 平台创建 Issue 时需在 body 末尾添加隐藏标记 `<!-- bluenet-bug-report -->`，以便 webhook 区分来源

## Impact

- **后端代码**：
  - 新增 `GitHubWebhookController`、`GitHubWebhookService`、`GitHubWebhookVerifier`
  - 修改 `GitHubIssueSyncService`（buildBody 添加标记）
  - 修改 `GitHubAppProperties`（新增 webhookSecret）
  - 修改 `BugReportRepository` / `BugReportRepositoryImpl`（新增按 issue number 查询）
- **数据库**：无需变更（现有 `github_issue_url` / `github_issue_number` 字段复用）
- **配置**：需新增 `GITHUB_APP_WEBHOOK_SECRET` 环境变量
- **前端**：无变更（管理端列表自然展示反向同步的 Bug 报告）
- **GitHub App**：需在仓库设置中配置 Webhook URL 和 Secret
