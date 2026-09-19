## MODIFIED Requirements

### Requirement: 系统可以自动创建 GitHub Issue
系统 SHALL 在用户提交 Bug 报告后，自动在配置的 GitHub 仓库中创建 Issue。Issue 的标题和 Body 应从 Bug 报告数据生成。Issue Body 末尾 SHALL 包含隐藏 HTML 注释 `<!-- bluenet-bug-report -->`，用于 Webhook 回调时识别 Issue 来源。创建成功后，系统 SHALL 将 GitHub 返回的 Issue 编号与 URL 写回对应 Bug 报告记录。

#### Scenario: 成功创建 GitHub Issue
- **WHEN** 用户提交一条包含描述、页面 URL、环境信息和 2 张截图的 Bug 报告
- **THEN** GitHub 仓库中新建一条 Issue，标题为 Bug 描述的前 100 字符，Body 包含完整描述、环境信息、页面 URL、截图下载链接和 `<!-- bluenet-bug-report -->` 标记

#### Scenario: 创建 Issue 时截图链接使用项目下载接口
- **WHEN** Bug 报告包含 fileId 为 123 的截图
- **THEN** GitHub Issue Body 中的截图链接格式为 `https://<domain>/api/v1/file/download/123`

#### Scenario: Issue 创建成功后写回编号与链接
- **WHEN** GitHub 返回 HTTP 201 且响应体包含 `number` 与 `html_url`（响应体同时包含 `id`、`url`、`labels`、`user`、`created_at` 等未声明字段）
- **THEN** 系统正常解析响应，将 Issue 编号与 URL 写入该 Bug 报告记录，数据库中 `github_issue_number` 与 `github_issue_url` 均不为空

#### Scenario: GitHub API 调用失败
- **WHEN** 用户提交 Bug 报告但 GitHub API 因网络问题或限流返回非 2xx 状态码
- **THEN** 系统记录错误日志，用户仍收到提交成功反馈，数据库中该记录的 `github_issue_url` 保持为空

#### Scenario: 未上传截图的 Bug 报告同步到 GitHub
- **WHEN** 用户提交的 Bug 报告没有截图
- **THEN** GitHub Issue 正常创建，Body 中截图部分为空或显示"无截图"

## ADDED Requirements

### Requirement: 解析 GitHub API 响应时必须容忍未声明的字段
系统 SHALL 使用忽略未知字段的 JSON 反序列化配置解析 GitHub API 响应，不得因响应体存在未在响应类型中声明的字段而解析失败。该要求 SHALL 同时覆盖创建 Issue 与拉取 Issue 列表两条调用链。

#### Scenario: 创建 Issue 的响应包含大量未声明字段
- **WHEN** GitHub 创建 Issue 成功并返回包含 `id`、`node_id`、`url`、`repository_url`、`labels_url`、`html_url`、`user`、`labels`、`assignee`、`state`、`created_at`、`updated_at` 等字段的真实响应体
- **THEN** 系统解析成功并返回包含 `number`、`html_url`、`title` 的结果对象，不抛出异常

#### Scenario: 拉取 Issue 列表的响应包含大量未声明字段
- **WHEN** GitHub 返回 Issue 列表，每个元素包含 `id`、`url`、`labels`、`user`、`created_at`、`assignee`、`comments` 等未在响应类型中声明的字段
- **THEN** 系统解析成功并返回 Issue 列表，不抛出异常

#### Scenario: 解析失败时错误日志可定位失败阶段
- **WHEN** GitHub API 响应无法解析为预期结构
- **THEN** 系统记录的错误日志能区分"API 调用失败"与"响应解析失败"，包含调用方法名与失败原因

### Requirement: 写回失败不得被静默吞掉
系统 SHALL 在 GitHub Issue 创建成功但编号与 URL 未能写回本地记录时，记录可被运维检索的错误日志，明确指出"已创建但未写回"的状态，以便识别数据不一致。

#### Scenario: 写回阶段异常
- **WHEN** Issue 已在 GitHub 创建成功，但写回本地记录的过程抛出异常
- **THEN** 系统记录错误日志，日志中包含 Bug 报告 ID 与 GitHub Issue 编号，且不影响用户提交成功反馈

#### Scenario: 解析阶段异常导致写回未发生
- **WHEN** Issue 已在 GitHub 创建成功，但响应解析阶段抛出异常导致写回未执行
- **THEN** 系统记录错误日志，日志中包含 Bug 报告 ID、Issue 标题与解析失败原因，便于后续定位与人工回填
