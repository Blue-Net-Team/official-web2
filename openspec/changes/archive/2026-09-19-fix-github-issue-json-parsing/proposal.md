## Why

提交 Bug 报告后，GitHub Issue 能成功创建，但后台始终不显示 Issue 编号和链接（issue #62）。生产日志堆栈显示根因在 `GitHubIssueClient` 的响应解析：

```
GitHubIssueClient.createIssue(GitHubIssueClient.java:77)
  └─ Caused by: UnrecognizedPropertyException: Unrecognized field "url"
     (class ...GitHubIssueClient$GitHubIssueCreateResponse)

GitHubIssueClient.parseIssueList(GitHubIssueClient.java:142)
  └─ Caused by: UnrecognizedPropertyException: Unrecognized field "url"
     (class ...GitHubIssueClient$GitHubIssueRaw,
      not marked as ignorable (6 known properties: ...))
```

`GitHubIssueClient` 自建 `new ObjectMapper()`（默认 `FAIL_ON_UNKNOWN_PROPERTIES = true`），而真实 GitHub Issue JSON 有约 40 个字段，两个响应类型只声明了 3 个 / 6 个组件且未标注忽略未知字段，因此**任何真实响应都会解析失败**。

后果：POST 请求在 GitHub 侧已经成功（Issue 已创建），但异常在解析阶段抛出并被 `catch` 吞掉，本地 `github_issue_number` / `github_issue_url` 永远为空；又因为本地没有编号，后续 `findByGithubIssueNumber` 永远查不到记录，状态自动同步一并失效。

回归点为 `8ee38de8`（2026-06-11，"类型安全"重构将 `Map<String,Object>` 换成类型化 record），此前的 Map 反序列化天然忽略未知字段。生产实证：issue #60 `created_at` = 2026-09-17T16:58:16Z 与失败日志 `00:58:16.921+08:00` 同秒，issue #61 同样同秒，证明失败发生在收到响应后的解析阶段。

## What Changes

- `GitHubIssueClient` 不再自建 `new ObjectMapper()`，改用容忍未知字段的 ObjectMapper（注入 Spring 容器实例，或显式关闭 `FAIL_ON_UNKNOWN_PROPERTIES`）
- `GitHubIssueCreateResponse` / `GitHubIssueRaw` 等响应类型补齐忽略未知字段的声明，避免同类回归
- 新增失败可观测性：解析失败时日志需能区分"GitHub API 调用失败"与"响应解析失败"
- 测试用例改用真实 GitHub API 响应载荷（含 `id` / `url` / `labels` / `user` / `created_at` 等字段），替代当前只含 3～6 个字段的精简 JSON
- 补充回归测试：以真实响应载荷断言 `createIssue` 能返回 `number` 与 `html_url`，`listIssues` 能返回列表并正确过滤 PR

本次变更**不包含**（明确非目标）：

- 写回失败的重试与历史孤儿 Issue 回填（#60 → bugReport 11、#61 → bugReport 12）
- `@Async` 在 `@Transactional` 内触发导致的事务可见性隐患
- `RestTemplate` 超时配置
- Webhook 与轮询对账逻辑的调整

## Capabilities

### New Capabilities

无新增能力。本次为既有能力的缺陷修复。

### Modified Capabilities

- `bug-report`: 明确"GitHub Issue 创建成功后必须将编号与 URL 写回本地记录"为可验收要求；明确"GitHub API 响应解析必须容忍未知字段"，并修正原「GitHub API 调用失败」场景中把解析失败与 API 调用失败混为一谈的语义
- `admin-bug-report-management`: 新增"列表与详情展示 GitHub Issue 编号与跳转链接"要求，作为本缺陷的端到端验收点

## Impact

- 代码：`src/backend/src/main/java/com/bluenet/web/infrastructure/github/GitHubIssueClient.java`（ObjectMapper 与响应类型）、`GitHubIssueCreateResult` 相关解析路径
- 测试：`src/backend/src/test/java/com/bluenet/web/infrastructure/github/GitHubIssueClientTest.java`（改为真实载荷）、必要时新增解析回归测试
- 依赖：`jackson-databind`（无需新增依赖，仅调整配置方式），Spring `ObjectMapper` 注入
- 系统影响：修复后写回链路恢复，`github_issue_number` 写回成功；轮询任务 `GitHubIssuePollingJob` 不再在 `listIssues` 阶段失败，状态对账恢复
- 数据影响：本次不修复历史数据，生产上已产生的孤儿 Issue（如 #60 / #61）编号仍为空，需另行处理
- 关联 Issue：#62
