## MODIFIED Requirements

### Requirement: 管理员可以分页查询 Bug 报告列表
系统 SHALL 在 Admin 后台提供 Bug 报告列表页，支持分页展示、按状态筛选，仅 ROLE_MEMBER 及以上角色可访问。

#### Scenario: 管理员查看列表
- **WHEN** ROLE_MEMBER 用户访问 Admin Bug 报告列表页
- **THEN** 系统展示分页的 Bug 报告列表，包含报告 ID、描述摘要、状态、提交时间、页面 URL，以及 GitHub Issue 链接（如有）

### Requirement: 管理员可以查看 Bug 报告详情
系统 SHALL 提供 Bug 报告详情接口和详情页，展示完整描述、环境信息、关联图片及提交时间。

#### Scenario: 查看详情
- **WHEN** 管理员点击列表中的某条报告
- **THEN** 系统展示该报告的完整信息，包括环境信息 JSON、可点击预览的截图列表，以及「查看 GitHub Issue」外链按钮（如已同步）

## ADDED Requirements

### Requirement: 管理端支持跳转到 GitHub Issue
系统 SHALL 在 Bug 报告列表和详情中提供跳转到对应 GitHub Issue 的入口，仅当该报告已成功同步到 GitHub 时显示。

#### Scenario: 已同步报告显示跳转链接
- **WHEN** 管理员查看一条 `github_issue_url` 不为空的 Bug 报告
- **THEN** 列表行和详情页均显示可点击的「查看 GitHub Issue」链接，点击后在新标签页打开

#### Scenario: 未同步报告不显示跳转链接
- **WHEN** 管理员查看一条 `github_issue_url` 为空的 Bug 报告
- **THEN** 页面不显示 GitHub Issue 相关入口
