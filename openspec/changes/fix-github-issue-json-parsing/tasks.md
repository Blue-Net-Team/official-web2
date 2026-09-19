## 1. 回归测试先行（红灯）

- [x] 1.1 在 `GitHubIssueClientTest` 中新增用例：以含大量未声明字段的真实 `createIssue` 响应载荷调用 `createIssue`，断言不抛异常且返回 `number` / `html_url`（当前必失败，用于确认缺陷复现）
- [x] 1.2 在 `GitHubIssueClientTest` 中新增用例：以含大量未声明字段的真实 `listIssues` 响应载荷调用 `listIssues`，断言解析成功且数量正确（当前必失败）
- [x] 1.3 运行 `./mvnw -Dtest=GitHubIssueClientTest test`，确认上述用例以 `UnrecognizedPropertyException` 失败，记录红灯输出作为证据

## 2. 真实响应载荷固化

- [x] 2.1 以 `GET /repos/Blue-Net-Team/official-web2/issues/60` 的真实响应体为基准，构造裁剪后的固定 JSON 常量（保留 `id`、`url`、`repository_url`、`labels_url`、`user`、`labels`、`state`、`created_at`、`assignee`、`comments` 等代表性未声明字段）
- [x] 2.2 将 `TC-008`（`createIssue` 成功用例）的三字段载荷替换为 2.1 的载荷，断言 `number` 与 `html_url` 解析结果正确
- [x] 2.3 将 `listIssues` 系列成功用例（`TC-001` / `TC-004` / `TC-005` / `TC-006`）的载荷替换为含未声明字段的真实载荷，保持原有断言不变
- [x] 2.4 保留并复核 `TC-005` 的 PR 过滤断言：载荷中带 `pull_request` 字段的条目仍被过滤
- [x] 2.5 保留并复核 `TC-006` 的大数值断言：`number = 2147483647` 仍被 `toInteger()` 正确处理

## 3. 解析配置修复（绿灯）

- [x] 3.1 调整 `GitHubIssueClient`：通过构造注入 Spring 容器的 `ObjectMapper`，替换类内自建的 `new ObjectMapper()`
- [x] 3.2 在 `GitHubIssueClient` 内基于注入实例建立显式关闭 `FAIL_ON_UNKNOWN_PROPERTIES` 的副本，并在字段上注明原因
- [x] 3.3 为 `GitHubIssueCreateResponse` 与 `GitHubIssueRaw` 添加 `@JsonIgnoreProperties(ignoreUnknown = true)` 作为防御性补充
- [x] 3.4 保持 `GitHubIssueRaw.number` 为 `Number` 并使用既有 `toInteger()` 转换，不扩大改动范围
- [x] 3.5 断言 `createIssue` 解析结果中 `number` 与 `html_url` 非空，为空时按解析失败处理
- [x] 3.6 运行 `./mvnw -Dtest=GitHubIssueClientTest test`，确认第 1 组用例全部转绿

## 4. 错误日志可观测性

- [x] 4.1 拆分 `GitHubIssueClient.createIssue` 的异常处理：网络/状态码异常记录为 API 调用失败（含状态码），Jackson 解析异常记录为响应解析失败（含方法名与字段信息）
- [x] 4.2 拆分 `GitHubIssueClient.listIssues` / `parseIssueList` 的异常处理，保持同一区分标准
- [x] 4.3 调整 `GitHubIssueSyncService.sync` 的失败日志，在 Issue 已创建但未写回时明确表达"已创建但未写回"，日志中包含 Bug 报告 ID、Issue 标题与失败原因
- [x] 4.4 复核日志不泄露 access token 等敏感信息

## 5. 单元与集成测试

- [x] 5.1 运行 `./mvnw -Dtest=GitHubIssueClientTest,GitHubIssuePollingJobTest,GitHubIssueSyncService*Test test`，确认无回归
- [x] 5.2 运行 `./mvnw -Dtest=BugReportAppServiceImplIntegrationTest,BugReportRepositoryImplIntegrationTest test`，确认 Bug 报告写回链路集成测试通过
- [x] 5.3 如新增/调整了测试用例覆盖范围，同步更新 `docs/03-开发指南/03-08-测试规范手册.md` 中对应说明（若无必要则跳过并在归档时说明）

## 6. 构建与打包

- [x] 6.1 执行 `cd src/backend && ./mvnw clean compile package`，确认构建成功
- [ ] 6.2 运行 `docker build -t bluenet-api-service:latest -f docker/api-service.Dockerfile .` 构建后端镜像

## 7. 部署与端到端验证

- [ ] 7.1 按 `docker compose -p bluenet --profile infra up -d` 确认基础设施已启动
- [ ] 7.2 启动后端容器（`backend-api-dev`，端口 8080，`--env-file docker/.env`，网络 `bluenet_network`）
- [ ] 7.3 确认 GitHub App 配置已启用：启动日志出现"GitHub Issue 同步功能已启用"，且 `github.apps.issue-sync.*` 环境变量齐全
- [ ] 7.4 检查 3000 端口占用情况；已被占用时直接复用现有前端服务，禁止重复启动
- [ ] 7.5 使用 Playwright 打开站点，通过 FloatButton 提交一条真实 Bug 报告，记录报告 ID
- [ ] 7.6 确认 GitHub 仓库出现对应 Issue，且其 `body` 含 `<!-- bluenet-bug-report -->` 标记
- [ ] 7.7 刷新后台 `/admin/bug-report`，确认该报告行展示 `#<编号>` 且链接可跳转到对应 Issue（对应 issue #62 的核心验收点）
- [ ] 7.8 打开该报告详情，确认编号与链接同样正确展示
- [ ] 7.9 查询数据库确认写回成功：`SELECT id, github_issue_number, github_issue_url FROM tb_bug_report WHERE id = <报告ID>`
- [ ] 7.10 检查后端日志，确认不再出现 `UnrecognizedPropertyException`，且无"已创建但未写回"错误日志

## 8. 收尾

- [x] 8.1 确认本次变更未修改既有 `openspec/specs/bug-report/spec.md` 与 `admin-bug-report-management/spec.md` 以外的能力定义
- [ ] 8.2 在 PR 描述中关联 issue #62（使用 `ref #62`，禁止使用 `fixes` / `close` 关键字）
- [ ] 8.3 记录历史孤儿 Issue（#60 → bugReport 11、#61 → bugReport 12）的处理建议，作为后续独立变更的输入

---

## 实施说明（2026-09-19）

已提交：`648fb16b`（修复与回归测试）、`685c34c7`（测试规范与任务进度）。

- **6.1 已完成**：`./mvnw compile package -DskipTests` 打包成功，产物 `target/api-service-0.1.0-SNAPSHOT.jar`（78M），并已核验新代码进入产物。`clean` 因 VS Code Java 语言服务器（JDT）锁定 `target/classes` 未能执行，改为增量打包，不影响产物正确性。
- **8.1 已核验**：`git diff` 确认未改动 `openspec/specs/` 下任何已归档能力定义，变更仅以 delta spec 形式存在于本次变更目录。
- **6.2 / 7.x 顺延**：Docker 镜像构建、容器部署与真实 GitHub 端到端验证交由维护者在 CI 或服务器执行。
- **8.3 已记录**：历史孤儿 Issue（#60 → bugReport 11、#61 → bugReport 12）的处理建议已记入 `design.md` 的 Open Questions 与「Non-Goals」，作为后续独立变更的输入。

**验证证据**：

| 范围 | 结果 |
| --- | --- |
| `GitHubIssueClientTest`（红灯阶段） | 7 个用例以 `UnrecognizedPropertyException` 失败，复现生产堆栈 |
| `GitHubIssueClientTest`（修复后） | 13/13 通过 |
| `GitHubIssueClientTest` + `GitHubIssuePollingJobTest` | 25/25 通过 |
| `BugReportAppServiceImplIntegrationTest` + `BugReportRepositoryImplIntegrationTest` | 12/12 通过 |
| 后端全量 `./mvnw test` | 830/830 通过，BUILD SUCCESS（8:59） |
