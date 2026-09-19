## ADDED Requirements

### Requirement: 管理员可以查看 Bug 报告关联的 GitHub Issue 编号与链接
系统 SHALL 在 Admin Bug 报告管理页的列表与详情中展示该报告关联的 GitHub Issue 编号，并提供可点击跳转的 Issue 链接。展示数据 SHALL 以本地持久化的 `github_issue_number` 与 `github_issue_url` 为准。

#### Scenario: 列表展示已关联 Issue 的报告
- **WHEN** 管理员查看 Bug 报告列表，某条记录的 `github_issue_number` 为 60 且 `github_issue_url` 为对应 Issue 地址
- **THEN** 该行展示 `#60` 形式的编号，且为可点击链接，点击后在新标签页打开对应 GitHub Issue

#### Scenario: 列表展示未关联 Issue 的报告
- **WHEN** 管理员查看 Bug 报告列表，某条记录的 `github_issue_number` 为空
- **THEN** 该行以占位符（如 `-`）展示，不渲染失效链接

#### Scenario: 详情展示 Issue 编号与链接
- **WHEN** 管理员打开某条已关联 Issue 的报告详情
- **THEN** 详情中展示该报告的 GitHub Issue 编号与可直接跳转的链接

#### Scenario: 提交后编号回写可见
- **WHEN** 用户提交 Bug 报告、GitHub Issue 创建成功且编号已写回，管理员随后刷新 Bug 报告列表
- **THEN** 该条报告展示对应的 Issue 编号与链接
