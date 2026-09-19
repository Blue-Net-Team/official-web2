# admin-bug-report-management Specification

## Purpose
TBD - created by archiving change add-bug-report-feature. Update Purpose after archive.
## Requirements
### Requirement: 管理员可以分页查询 Bug 报告列表
系统 SHALL 在 Admin 后台提供 Bug 报告列表页，支持分页展示、按状态筛选，仅 ROLE_MEMBER 及以上角色可访问。

#### Scenario: 管理员查看列表
- **WHEN** ROLE_MEMBER 用户访问 Admin Bug 报告列表页
- **THEN** 系统展示分页的 Bug 报告列表，包含报告 ID、标题、状态、提交时间、页面 URL

#### Scenario: 按状态筛选列表
- **WHEN** 管理员选择筛选条件"状态 = 未解决"
- **THEN** 列表仅展示状态为 PENDING 的报告

#### Scenario: 无权限用户访问列表
- **WHEN** CANDIDATE 角色用户尝试访问 Bug 报告管理接口
- **THEN** 系统返回 403 拒绝访问

### Requirement: 管理员可以查看 Bug 报告详情
系统 SHALL 提供 Bug 报告详情接口和详情页，展示完整描述、环境信息、关联图片及提交时间。

#### Scenario: 查看详情
- **WHEN** 管理员点击列表中的某条报告
- **THEN** 系统展示该报告的标题、完整信息（包括环境信息 JSON、可点击预览的截图列表）

#### Scenario: 查看不存在的报告
- **WHEN** 管理员请求 ID 为 99999 的报告详情
- **THEN** 系统返回 404 和"报告不存在"错误

### Requirement: 管理员可以更新 Bug 报告处理状态
系统 SHALL 允许管理员更新 Bug 报告的状态，支持 PENDING → IN_PROGRESS → RESOLVED 的流转。

#### Scenario: 标记为处理中
- **WHEN** 管理员将状态为 PENDING 的报告更新为 IN_PROGRESS
- **THEN** 系统成功更新状态并返回更新后的报告

#### Scenario: 标记为已解决
- **WHEN** 管理员将状态为 IN_PROGRESS 的报告更新为 RESOLVED
- **THEN** 系统成功更新状态，列表页该报告状态变为已解决

#### Scenario: 更新无效状态
- **WHEN** 管理员传入不存在的枚举值作为状态
- **THEN** 系统返回 400 和"无效的状态值"错误

### Requirement: 后端提供管理接口并受权限保护
系统 SHALL 提供以下管理接口，所有接口要求 ROLE_MEMBER 及以上权限：
- `GET /api/v1/admin/bug-reports` — 分页查询列表
- `GET /api/v1/admin/bug-reports/{id}` — 查看详情
- `PUT /api/v1/admin/bug-reports/{id}/status` — 更新状态

#### Scenario: 调用管理接口无权限
- **WHEN** 未登录用户或 CANDIDATE 调用管理接口
- **THEN** 系统返回 401 或 403


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
