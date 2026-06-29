## Context

Bug 报告模块当前在多处手写字符串截断与字段校验：

- `GitHubIssueSyncService.buildTitle` 使用 `substring(0, MAX_TITLE_LENGTH) + "..."` 截断标题。
- `BugReportAdminAppServiceImpl.toBriefResult` 使用 `substring(0, 100) + "..."` 截断描述。
- `BugReport.create` 对 `title` 和 `description` 重复编写 `null/blank` 校验 + 最大长度校验。
- `BugReportCommands.CreateBugReportCommand` 的 compact constructor 对多个字段重复 `if (x != null) x = x.trim()`。

这些重复导致维护困难，且 `BugReportAdminAppServiceImpl` 的后端截断与前端表格组件的 CSS 截断重复。

## Goals / Non-Goals

**Goals:**
- 将字符串截断逻辑收敛到单一工具方法。
- 将 `BugReport` 中重复的"非空 + 最大长度"校验收敛到私有方法。
- 移除管理端 brief 接口中多余的后端 description 截断，由前端负责展示控制。
- 保持现有 API 行为不变（除 brief 的 `description` 改为完整返回外）。
- 通过单元测试覆盖重构后的工具方法和领域校验行为。

**Non-Goals:**
- 不合并 brief 和 detail 接口。
- 不修改前端页面代码。
- 不引入新的第三方依赖（如 Apache Commons Lang）。
- 不改变 GitHub Issue 同步的截断语义。

## Decisions

### 1. 通用截断工具放在 `com.bluenet.web.common.util.StringUtils`

项目已有 `domain.util`（领域工具）和 `infrastructure.util`（基础设施工具），通用字符串操作不属于任何一层，因此新增 `common.util` 包。

替代方案：放在 `infrastructure.util`。未采纳，因为字符串截断与基础设施无关，放在 common 更符合语义。

### 2. `truncateWithEllipsis` 对 `maxLength <= 3` 直接返回前 `maxLength` 个字符

当最大长度不足以容纳 `"..."` 时，无法同时满足"截断到 maxLength"和"追加省略号"。选择优先满足长度限制，直接截取前 `maxLength` 字符。

替代方案：抛出异常或返回空串。未采纳，因为工具方法应尽可能宽容，调用方通常期望返回不超过 maxLength 的字符串。

### 3. `BugReport.create` 的校验方法同时完成 `trim`

提取 `requireNonBlankMaxLength(String value, String fieldName, int maxLength)`，在校验通过后返回 `value.trim()`。这样 `create` 方法内部不再散落 `trim()` 调用。

替代方案：校验和 trim 分开。未采纳，因为非空校验、长度校验、空白规范化是同一组输入清理动作，放在一起可减少遗漏。

### 4. 保留 brief 接口，但移除 description 截断

brief 接口存在的价值是列表不需要 `environmentJson` 和完整 `fileIds` 数组，只需要 `imageCount`。合并到 detail 会让列表背负不必要的数据传输。

替代方案：合并 brief 和 detail。未采纳，因为会增加网络开销并破坏列表/详情的职责分离。

## Risks / Trade-offs

| Risk | Mitigation |
|------|------------|
| `BugReportBriefDTO` 的描述/示例仍写"摘要"，导致文档与实际不符 | 同步修改 Swagger `@Schema` 描述和 example |
| 去掉后端截断后，某些旧客户端可能依赖 100 字符 description | 管理端列表只有前端一个调用方，且前端已做 CSS 截断；如其它调用方存在，需单独评估 |
| `StringUtils.truncateWithEllipsis` 只有一个使用方，抽象过早 | 该工具通用且明确，未来通知标题、日志摘要等场景可复用；如团队约定"少于两方不抽象"，可改为 `GitHubIssueSyncService` 私有方法 |
| 重构后错误信息格式变化导致测试失败 | 新增 `BugReportTest` 覆盖所有校验场景，并运行现有 `BugReportAppServiceImplTest` 回归 |
