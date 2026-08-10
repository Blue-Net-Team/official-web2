## Why

Bug 报告模块中存在多处手写字符串截断与重复字段校验：`GitHubIssueSyncService` 与 `BugReportAdminAppServiceImpl` 各自实现 `substring(0, N) + "..."`，`BugReport.create` 对 `title` 和 `description` 重复编写"非空 + 最大长度"校验，`BugReportCommands.CreateBugReportCommand` 的 compact constructor 也对多个字段重复 `trim`。这些重复实现导致省略号格式、边界处理、错误信息不一致，新增字段时容易 copy-paste 出错，维护成本高。

## What Changes

- 新增通用字符串工具类 `StringUtils`，提供 `truncateWithEllipsis(String, int)` 统一截断逻辑。
- 重构 `BugReport.create`，提取私有方法 `requireNonBlankMaxLength(...)` 统一 `title` / `description` 的非空、最大长度校验与 `trim`。
- `GitHubIssueSyncService.buildTitle` 改用 `StringUtils.truncateWithEllipsis` 并复用 `BugReport.MAX_TITLE_LENGTH`。
- **移除 `BugReportAdminAppServiceImpl.toBriefResult` 中的 description 截断**，由前端表格组件通过 CSS 控制展示，brief 接口返回完整 `description`。
- 简化 `BugReportCommands.CreateBugReportCommand` 中重复的 `trim` 代码。
- 修正 `BugReportResult.Brief` 与 `BugReportBriefDTO` 的 Swagger 描述和示例，去掉"摘要"相关表述以匹配实际行为。
- 新增 `StringUtilsTest` 与 `BugReportTest` 单元测试，覆盖工具方法边界和领域校验行为。

## Capabilities

### New Capabilities

- 无（本次为内部实现重构，不引入新的业务能力）。

### Modified Capabilities

- 无（现有 `admin-bug-report-management` 能力的需求不变，仅实现方式优化；列表接口仍返回 brief 视图，但 `description` 字段由后端截断改为完整返回，前端展示逻辑已自行处理）。

## Impact

- **后端代码**：`BugReport.java`、`BugReportCommands.java`、`GitHubIssueSyncService.java`、`BugReportAdminAppServiceImpl.java`、`BugReportResult.java`、`BugReportBriefDTO.java`。
- **新增文件**：`common/util/StringUtils.java`、`StringUtilsTest.java`、`BugReportTest.java`。
- **API 行为**：管理端列表接口的 `description` 字段从 100 字符截断改为返回完整内容；字段名、接口路径、其它响应结构不变。
- **前端**：无需修改，现有 `admin/bug-report/page.tsx` 已通过 CSS `-webkit-line-clamp: 3` 控制描述展示。
- **数据库**：无变更。
