## Why

当前 Bug 反馈功能只有"问题描述"一个文本字段，同步到 GitHub Issue 时直接将其前 100 字符作为 Issue 标题，导致标题冗长、无法快速识别问题。issue #24 指出需要为 Bug 反馈增加独立的"标题/简述"字段，使 GitHub Issue 标题简洁明了，同时改善管理端列表的可读性。

## What Changes

- 在 `tb_bug_report` 表新增 `title` 字段（`VARCHAR(200)`，非空）。
- 对已有历史数据执行迁移：将现有 `description` 前 100 字符回填到 `title`。
- 后端 `BugReport` 领域模型及各层 DTO/Command/Result/Converter 增加 `title` 字段，并将 `title` 设为必填。
- 前端 `BugReportModal` 增加"Bug 标题"输入框（必填，最多 100 字符）。
- 管理端 Bug 报告列表页将主展示列从"描述"切换为"标题"，详情页同时展示标题和完整描述。
- `GitHubIssueSyncService` 改用 `title` 作为 GitHub Issue 标题；若因历史原因缺失 title，则降级使用 description 前 100 字符。
- 更新对应单元测试。

## Capabilities

### New Capabilities

- 无

### Modified Capabilities

- `bug-report`: 提交 Bug 报告时要求提供 `title` 字段，用于简述问题；原 `description` 仍作为详细描述保留。
- `admin-bug-report-management`: 管理端列表和详情增加 `title` 字段展示，列表以标题替代描述摘要作为主要可读列。

## Impact

- 数据库：`tb_bug_report` 新增 `title` 字段并迁移历史数据。
- 后端：领域模型、DO、DTO、Command、Result、Converter、Repository、应用服务、`GitHubIssueSyncService` 均需调整。
- 前端：用户端反馈弹窗、管理端列表/详情页、TypeScript DTO。
- 测试：`GitHubIssueSyncServiceTest`、`BugReportAppServiceImplTest`。
- 向下兼容：旧接口调用方（如有）将因缺少 `title` 收到 400 校验错误；此为预期行为。
