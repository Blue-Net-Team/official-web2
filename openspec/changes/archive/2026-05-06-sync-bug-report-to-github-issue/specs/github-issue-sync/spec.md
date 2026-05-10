## ADDED Requirements

### Requirement: 系统可以通过 GitHub App 认证并调用 GitHub API
系统 SHALL 使用 GitHub App 的 JWT + Installation Access Token 机制完成认证，并具备调用 GitHub REST API 的能力。

#### Scenario: 成功生成 Installation Access Token
- **WHEN** 系统使用有效的 GitHub App 私钥和 App ID 生成 JWT
- **THEN** 系统能通过 JWT 向 GitHub 换取指定 Installation 的 Access Token

#### Scenario: 使用 Access Token 调用 GitHub API
- **WHEN** 系统携带 Installation Access Token 调用 GitHub REST API
- **THEN** 请求成功通过认证，返回预期响应

#### Scenario: GitHub App 配置缺失
- **WHEN** 系统未配置 GitHub App ID 或私钥路径
- **THEN** 同步逻辑被跳过，系统不报错，Bug 报告正常保存到本地数据库

### Requirement: 系统可以自动创建 GitHub Issue
系统 SHALL 在用户提交 Bug 报告后，自动在配置的 GitHub 仓库中创建 Issue。Issue 的标题和 Body 应从 Bug 报告数据生成。

#### Scenario: 成功创建 GitHub Issue
- **WHEN** 用户提交一条包含描述、页面 URL、环境信息和 2 张截图的 Bug 报告
- **THEN** GitHub 仓库中新建一条 Issue，标题为 Bug 描述的前 100 字符，Body 包含完整描述、环境信息、页面 URL 和截图下载链接

#### Scenario: 创建 Issue 时截图链接使用项目下载接口
- **WHEN** Bug 报告包含 fileId 为 123 的截图
- **THEN** GitHub Issue Body 中的截图链接格式为 `https://<domain>/api/v1/file/download/123`

#### Scenario: GitHub API 调用失败
- **WHEN** 用户提交 Bug 报告但 GitHub API 因网络问题或限流返回错误
- **THEN** 系统记录错误日志，用户仍收到提交成功反馈，数据库中该记录的 `github_issue_url` 保持为空

#### Scenario: 未上传截图的 Bug 报告同步到 GitHub
- **WHEN** 用户提交的 Bug 报告没有截图
- **THEN** GitHub Issue 正常创建，Body 中截图部分为空或显示"无截图"

### Requirement: 同步结果回写到 Bug 报告记录
系统 SHALL 在成功创建 GitHub Issue 后，将 Issue URL 和 Issue Number 更新到对应的 `tb_bug_report` 记录中。

#### Scenario: 同步成功后更新记录
- **WHEN** GitHub Issue 创建成功，返回 issue_number=42 和 html_url
- **THEN** 系统更新 `tb_bug_report` 的 `github_issue_number=42` 和 `github_issue_url` 为返回的 URL

#### Scenario: 同步失败后记录保持原样
- **WHEN** GitHub Issue 创建失败
- **THEN** `tb_bug_report` 的 `github_issue_url` 和 `github_issue_number` 保持 NULL
