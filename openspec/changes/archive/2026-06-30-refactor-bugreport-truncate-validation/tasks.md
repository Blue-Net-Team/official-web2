## 1. 新增通用字符串工具类及测试

- [x] 1.1 创建 `com.bluenet.web.common.util.StringUtils`，实现 `truncateWithEllipsis(String, int)`
- [x] 1.2 创建 `StringUtilsTest`，覆盖 null、空串、短串、正好长度、超长、`maxLength <= 3`、负数等边界

## 2. 重构 BugReport 领域校验

- [x] 2.1 在 `BugReport` 中提取私有静态方法 `requireNonBlankMaxLength(String value, String fieldName, int maxLength)`
- [x] 2.2 在 `BugReport.create` 中复用该校验方法处理 `title` 和 `description`
- [x] 2.3 创建 `BugReportTest`，覆盖创建成功、title/description 为空/空白/超长、fileIds 超过 3 张、trim 行为

## 3. 替换应用层截断逻辑

- [x] 3.1 修改 `GitHubIssueSyncService.buildTitle`，删除本地 `MAX_TITLE_LENGTH` 常量，改用 `BugReport.MAX_TITLE_LENGTH` 和 `StringUtils.truncateWithEllipsis`
- [x] 3.2 修改 `BugReportAdminAppServiceImpl.toBriefResult`，移除 description 的 `substring(0, 100) + "..."` 截断，直接返回完整 description

## 4. 清理命令对象与 DTO 描述

- [x] 4.1 简化 `BugReportCommands.CreateBugReportCommand` compact constructor 中的重复 `trim`
- [x] 4.2 修改 `BugReportResult.Brief` 的注释，从"摘要"改为完整描述
- [x] 4.3 修改 `BugReportBriefDTO` 的 `@Schema` 描述和示例，去掉"摘要"和 `...`

## 5. 验证与回归

- [x] 5.1 运行 `BugReportAppServiceImplTest` 确认现有测试通过
- [x] 5.2 运行新增 `StringUtilsTest` 和 `BugReportTest`
- [x] 5.3 运行后端全量测试 `mvnw test` 确认无副作用
