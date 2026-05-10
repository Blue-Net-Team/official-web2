## Context

项目已实现完整的 Bug 报告功能：用户通过 FloatButton 提交报告，数据保存在 `tb_bug_report` 和 `tb_bug_report_image` 表中，管理端可查看和处理。团队使用 GitHub 管理开发任务，但目前 Bug 报告与 GitHub Issue 之间是人工搬运，容易遗漏。

现有代码中已有调用 GitHub API 的先例：`GitHubOAuthServiceImpl` 使用 `RestTemplate` 调用 GitHub REST API。本次集成可复用此模式，保持技术栈一致性。

## Goals / Non-Goals

**Goals:**
- 用户提交 Bug 报告后，系统自动在指定 GitHub 仓库创建对应 Issue
- GitHub Issue Body 包含完整的 Bug 描述、环境信息、页面 URL 和截图下载链接
- 管理端可一键跳转到对应 GitHub Issue
- 同步失败不影响用户提交体验

**Non-Goals:**
- 双向同步（GitHub Issue 的修改不回写系统）
- 支持多个 GitHub 仓库
- 自定义 Issue 模板（使用固定 Markdown 格式）
- 在 GitHub 上自动关闭 Issue（管理员需手动操作）

## Decisions

### 使用 GitHub App 而非 Personal Access Token
- **选择**: GitHub App
- **理由**: 独立 Bot 身份、短效 Token 自动刷新、不绑定个人账号、更高 API 限流（15k/h vs 5k/h）。生产环境自动化场景官方推荐。
- **替代方案**: Fine-grained PAT — 配置简单但绑定个人身份，人员离职会失效

### 使用 RestTemplate 而非 WebClient
- **选择**: `RestTemplate`
- **理由**: 项目已有 `GitHubOAuthServiceImpl` 使用 `RestTemplate` 的先例，保持风格一致；该场景无需高并发非阻塞
- **替代方案**: `WebClient` / `RestClient` — 更现代，但引入不一致性

### 同步策略：先保存数据库，再通过 `@Async` 异步调用 GitHub API
- **选择**: 事务内保存数据库，事务提交后通过 `@Async` 将 GitHub API 调用放入独立线程池执行
- **理由**: 用户提交不应被 GitHub API 网络问题阻塞；项目已有 `@Async` 先例（`AuditAsyncConfig` + `@Async("auditExecutor")`），复用 Spring 异步机制即可，无需引入消息队列
- **实现**: 在 `BugReportAppServiceImpl.submitBugReport()` 中，先 `repository.save()`，再 `@Async` 调用 `githubIssueSyncService.sync(bugReport)`，由异步方法内部调用 GitHub API 并 `repository.updateGithubIssueInfo()`

### 截图链接使用项目下载接口而非 MinIO 直链
- **选择**: `https://<domain>/api/v1/file/download/{fileId}`
- **理由**: 不暴露 MinIO endpoint 和 bucket 结构；保留权限控制；链接更稳定
- **替代方案**: MinIO 预签名 URL — 可直接访问但暴露存储层细节

### Issue Body 使用 Markdown 固定模板
- **选择**: 后端硬编码 Markdown 模板
- **理由**: 简单直接，无需引入模板引擎；内容结构固定（描述、环境、URL、截图）

## Risks / Trade-offs

| 风险 | 缓解措施 |
|------|---------|
| GitHub API 限流（每小时 15k 次对于 App 足够，但需考虑突发） | 当前 Bug 报告量极低，暂不加限流保护；如后续增长可引入本地队列 |
| GitHub App 私钥泄露 | 私钥通过 `.env` 文件注入，不提交到仓库；生产环境使用 Secret 管理 |
| 网络抖动导致同步失败 | 失败只打日志不抛异常给用户；数据库记录保留，管理端可识别未同步的报告（`github_issue_url` 为空） |
| GitHub Issue 创建成功但数据库更新失败 | 极小概率；如发生会导致重复 Issue；后续可添加幂等检查 |
| 截图下载链接需要登录才能访问 | GitHub 上点击链接时如会话过期会 403；权衡后接受，管理员通常有账号 |

## Migration Plan

1. 创建 GitHub App 并安装到目标仓库
2. 配置环境变量（App ID、私钥路径、仓库信息）
3. 执行 Flyway 迁移 `V9__add_github_issue_to_bug_report.sql`
4. 部署后端代码
5. 提交一条测试 Bug 报告，验证 Issue 自动创建
6. 检查管理端 GitHub Issue 链接是否正常跳转

**Rollback**: 如 GitHub 集成出现问题，移除配置中的 App ID（置空），代码中判断配置缺失时跳过同步逻辑，系统降级为纯本地 Bug 报告。

## Open Questions

- Issue 标签：创建时自动打上 `Bug` 标签
- 是否需要把 Bug 报告状态变更同步到 GitHub Issue（如标记为 RESOLVED 时关闭 Issue）？→ 当前不同步，非目标范围
