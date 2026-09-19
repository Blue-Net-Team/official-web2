## Context

`GitHubIssueClient` 是 Bug 报告与 GitHub Issue 双向同步的唯一出口，承担两条调用链：

```
BugReportAppServiceImpl.submitBugReport
  └─ GitHubIssueSyncService.sync (@Async)
       └─ GitHubIssueClient.createIssue  → POST /repos/{owner}/{repo}/issues

GitHubIssuePollingJob.sync (@Scheduled 每天 03:00)
  └─ GitHubIssueClient.listIssues  → GET /repos/{owner}/{repo}/issues?...
```

两条链路的响应反序列化都使用类内自建的 ObjectMapper：

```java
private final ObjectMapper objectMapper = new ObjectMapper();   // 默认 FAIL_ON_UNKNOWN_PROPERTIES = true
```

当前响应类型只声明了极少数字段：

| 响应类型 | 声明字段数 | 真实 GitHub 响应字段数 |
|---|---|---|
| `GitHubIssueCreateResponse` | 3（number / html_url / title） | ≈ 40 |
| `GitHubIssueRaw` | 6（number / title / body / state / html_url / pull_request） | ≈ 30 |

默认配置下遇到未知字段立即抛 `UnrecognizedPropertyException`，且没有任何 `@JsonIgnoreProperties` 兜底，导致**任何真实响应都必然解析失败**。

生产证据（2026-09-18）：

| 事件 | 时间 | 说明 |
|---|---|---|
| issue #60 created_at | 2026-09-17T16:58:16Z | POST 成功 |
| bugReportId=11 失败日志 | 2026-09-18T00:58:16.921+08:00 | 同秒，解析阶段抛错 |
| issue #61 created_at | 2026-09-17T16:59:01Z | POST 成功 |
| bugReportId=12 失败日志 | 2026-09-18T00:59:01.936+08:00 | 同秒，解析阶段抛错 |

回归点：`8ee38de8`（2026-06-11）把 `Map<String, Object>` / `List<Map<String, Object>>` 换成类型化 record。Map 反序列化天然忽略未知字段，因此此前写回正常。

对照证据：`GitHubWebhookService` 注入的是 Spring 容器里的 ObjectMapper（Spring Boot 自动配置将 `FAIL_ON_UNKNOWN_PROPERTIES` 置为 false），因此 webhook 链路一直正常 —— 问题只出在自建的那份 ObjectMapper。

约束：

- 项目测试规范要求单元测试覆盖该类，且现有 `GitHubIssueClientTest` 使用 Mockito spy + mock RestTemplate 的模式，改动需沿用
- 分层约定：`GitHubIssueClient` 属基础设施层，可依赖 Spring 容器能力
- 不为本次修复引入新依赖

## Goals / Non-Goals

**Goals:**

- 消除"GitHub Issue 已创建但本地编号/URL 未写回"的确定性失败路径
- 让 `createIssue` 与 `listIssues` 对真实 GitHub 响应解析成功
- 让测试用例使用真实响应载荷，使同类回归在 CI 阶段即被捕获
- 让解析失败与 API 调用失败在日志中可区分

**Non-Goals:**

- 不实现写回失败的重试与历史孤儿 Issue（#60 / #61）回填
- 不调整 webhook 反向同步与轮询对账的业务逻辑
- 不处理 `@Async` 在 `@Transactional` 内触发导致的事务可见性隐患
- 不给 `RestTemplate` 增加超时配置
- 不修改前端展示逻辑（现有渲染已满足验收要求）

## Decisions

### 决策 1：以"注入 Spring 容器的 ObjectMapper"作为主方案，并显式关闭 `FAIL_ON_UNKNOWN_PROPERTIES` 作为兜底

方案对比：

| 方案 | 做法 | 优点 | 缺点 |
|---|---|---|---|
| A（采用） | 构造注入 Spring `ObjectMapper`，并在客户端内副本上显式 `configure(FAIL_ON_UNKNOWN_PROPERTIES, false)` | 与项目其余部分（webhook）行为一致；显式声明意图，不依赖 Boot 默认值 | 需调整构造函数，测试需适配 |
| B | 给响应类型加 `@JsonIgnoreProperties(ignoreUnknown = true)` | 改动最小，局部生效 | 新增响应类型时容易再次遗漏；未解决"自建 Mapper 偏离项目配置"的根因 |
| C | 保留 `new ObjectMapper()`，仅 `configure(FAIL_ON_UNKNOWN_PROPERTIES, false)` | 改动最小且能修好 | 仍与项目其余 JSON 处理配置分裂，未来其他配置项（时间格式、命名策略）继续不一致 |

选择 A 并要求 B 作为防御性补充（响应类型同时标注 `@JsonIgnoreProperties(ignoreUnknown = true)`），理由是：根因是"自建 ObjectMapper 偏离了项目统一配置"，只修症状（B/C）会在下一次重构中复发；同时双保险可避免未来某处又退回自建 Mapper。

`GitHubIssueClient` 构造函数变更后，需同步调整 `GitHubIssueClientTest` 的实例化方式（当前为 `new GitHubIssueClient(properties, tokenService)`）。

### 决策 2：`GitHubIssueRaw` 保留 `Number` + 手工转 `Integer` 的安全转换，不做类型简化

`GitHubIssueRaw.number` 目前是 `Number`，配合 `toInteger()` 做范围校验（已有 TC-006 覆盖 `Integer.MAX_VALUE` 场景）。本次不改变该设计，只调整未知字段容忍性，避免把修复范围扩大到类型重构。

### 决策 3：测试载荷改为真实 GitHub API 响应快照

现状问题：`TC-008` 使用 `{"number":42,"html_url":"...","title":"..."}` 三字段载荷，`listIssues` 系列使用五～六字段载荷，恰好等于响应类型的声明字段，因此**测试全绿但生产必炸**。

改为：以 issue #60 / #61 的真实响应体（来自 `GET /repos/Blue-Net-Team/official-web2/issues/{n}`）为快照，裁剪为可维护的固定 JSON 常量（保留 `id`、`url`、`repository_url`、`labels`、`user`、`state`、`created_at`、`assignee` 等代表性未知字段），用于：

- `createIssue` 成功用例：断言解析出 `number` 与 `html_url`
- `listIssues` 成功用例：断言列表解析成功、PR 过滤（`pull_request` 字段）仍生效

同时新增一条**断言解析器配置**的用例：以含大量未知字段的响应体调用 `createIssue`，断言不抛 `UnrecognizedPropertyException`（该用例即本缺陷的回归测试）。

### 决策 4：日志区分"API 调用失败"与"响应解析失败"

`GitHubIssueClient` 现有 catch 结构是 `catch (RuntimeException e)` → 包装为 `"Failed to create GitHub issue"`。改为：

- 状态码/网络异常：`"Failed to call GitHub API"`（含状态码）
- Jackson 解析异常：`"Failed to parse GitHub API response"`（含方法名与字段信息）

`GitHubIssueSyncService` 层在 Issue 已创建但未写回时，日志需明确表达"已创建但未写回"，便于运维检索数据不一致（对应 spec 中「写回失败不得被静默吞掉」）。

## Risks / Trade-offs

- [放宽未知字段容忍度可能掩盖 GitHub API 的破坏性变更（如字段重命名）] → 保留对已声明字段的存在性断言（`number` 与 `html_url` 必须非空，否则视为解析失败），并在测试中固化这些断言
- [注入 Spring ObjectMapper 后，全局 Jackson 配置变更会连带影响该客户端] → 视为收益而非风险：与项目其余部分保持一致，且客户端内仍显式声明解析容错
- [本次不修复历史数据，#60 / #61 编号仍为空] → 验收时以"新提交的报告编号正确显示"为准；历史回填另行立项
- [`@Async` 事务可见性隐患仍在] → 本次修复后，若该隐患与竞态叠加仍可能出现偶发写回失败；已列入非目标，需在后续变更中处理
- [测试载荷快照可能随 GitHub API 演进过时] → 快照仅用于解析健壮性验证，不追求与线上响应逐字节一致，允许裁剪

## Migration Plan

1. 修改 `GitHubIssueClient` 的 ObjectMapper 获取方式与响应类型注解
2. 调整 `GitHubIssueClientTest` 实例化方式，替换为真实载荷快照，新增解析回归用例
3. 构建镜像并部署后端
4. 端到端验证：在 `https://www.gdou-bluenet.cn` 提交一条 Bug 报告 → 确认 GitHub 出现对应 Issue → 确认后台列表与详情展示该 Issue 编号与链接
5. 回归确认：观察日志中不再出现 `UnrecognizedPropertyException`；确认次日 03:00 轮询任务不再报"拉取 GitHub Issue 列表失败"

回滚策略：该变更仅影响解析容错与日志文案，回滚即恢复旧镜像；回滚后写入 `github_issue_number` 的能力再次失效，无数据破坏风险（不涉及表结构变更）。

## Open Questions

- 历史孤儿 Issue（#60 → bugReport 11、#61 → bugReport 12）是否在本变更内一并回填？当前按非目标处理，建议单独变更
- 是否需要在 `GitHubIssueSyncService` 中补充重试？当前按非目标处理
