## Why

当前用户提交的 Bug 报告仅保存在系统数据库中，管理员需要手动将问题搬运到 GitHub Issue 进行跟踪和修复。这个人工步骤容易遗漏，且割裂了用户反馈与开发工作流。将 Bug 报告自动同步到 GitHub Issue 可以打通反馈闭环，让团队直接在 GitHub 上跟踪和修复问题。

## What Changes

- 后端新增 GitHub App 客户端，通过 GitHub REST API 自动创建 Issue
- 用户提交 Bug 报告后，系统在保存数据库记录的同时异步创建对应的 GitHub Issue
- `tb_bug_report` 表新增 `github_issue_url` 和 `github_issue_number` 字段，用于存储同步结果
- GitHub Issue Body 包含 Bug 描述、环境信息、页面 URL 及截图下载链接（走现有 `/api/v1/file/download/{fileId}` 接口）
- 管理端 Bug 报告列表和详情页增加「查看 GitHub Issue」外链
- 新增环境变量配置：GitHub App ID、私钥路径、目标仓库 owner/repo

## Capabilities

### New Capabilities
- `github-issue-sync`: GitHub Issue 自动同步能力，包括 JWT 生成、Installation Token 换取、Issue 创建及错误处理

### Modified Capabilities
- `bug-report`: 提交成功后自动触发 GitHub Issue 同步；创建结果返回中增加 `githubIssueUrl` 字段
- `admin-bug-report-management`: 列表和详情展示 GitHub Issue 链接，支持一键跳转到对应 Issue 页面

## Impact

- 后端新增外部 HTTP 调用依赖（GitHub API），需处理网络异常和限流
- 数据库 Schema 变更（新增两列）
- 新增配置项需补充到 `.env.example`
- 需要创建并安装 GitHub App 到目标仓库
