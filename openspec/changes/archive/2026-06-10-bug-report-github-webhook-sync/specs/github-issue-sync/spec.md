## MODIFIED Requirements

### Requirement: 系统可以自动创建 GitHub Issue
系统 SHALL 在用户提交 Bug 报告后，自动在配置的 GitHub 仓库中创建 Issue。Issue 的标题和 Body 应从 Bug 报告数据生成。Issue Body 末尾 SHALL 包含隐藏 HTML 注释 `<!-- bluenet-bug-report -->`，用于 Webhook 回调时识别 Issue 来源。

#### Scenario: 成功创建 GitHub Issue
- **WHEN** 用户提交一条包含描述、页面 URL、环境信息和 2 张截图的 Bug 报告
- **THEN** GitHub 仓库中新建一条 Issue，标题为 Bug 描述的前 100 字符，Body 包含完整描述、环境信息、页面 URL、截图下载链接和 `<!-- bluenet-bug-report -->` 标记

#### Scenario: 创建 Issue 时截图链接使用项目下载接口
- **WHEN** Bug 报告包含 fileId 为 123 的截图
- **THEN** GitHub Issue Body 中的截图链接格式为 `https://<domain>/api/v1/file/download/123`

#### Scenario: GitHub API 调用失败
- **WHEN** 用户提交 Bug 报告但 GitHub API 因网络问题或限流返回错误
- **THEN** 系统记录错误日志，用户仍收到提交成功反馈，数据库中该记录的 `github_issue_url` 保持为空

#### Scenario: 未上传截图的 Bug 报告同步到 GitHub
- **WHEN** 用户提交的 Bug 报告没有截图
- **THEN** GitHub Issue 正常创建，Body 中截图部分为空或显示"无截图"

#### Scenario: 创建的 Issue Body 包含隐藏标记
- **WHEN** 系统通过 `GitHubIssueSyncService` 创建 Issue
- **THEN** Issue Body 末尾包含 `<!-- bluenet-bug-report -->` HTML 注释（Markdown 渲染不可见）
