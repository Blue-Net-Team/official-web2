# bug-report Specification

## Purpose
TBD - created by archiving change add-bug-report-feature. Update Purpose after archive.
## Requirements
### Requirement: 用户可以通过 FloatButton 提交 Bug 报告
系统 SHALL 在所有页面的右下角提供一个悬浮按钮（FloatButton），点击后弹出 Modal 表单，允许用户填写并提交 Bug 报告。

#### Scenario: 访客成功提交 Bug 报告
- **WHEN** 用户点击 FloatButton 并填写标题、描述、上传 2 张截图、输入联系邮箱
- **THEN** 系统成功创建 Bug 报告记录，返回成功提示，Modal 自动关闭

#### Scenario: 提交时仅填写必填项
- **WHEN** 用户仅填写 Bug 标题和描述（不填邮箱、不上传图片）
- **THEN** 系统仍成功创建报告，联系邮箱和图片列表为空

#### Scenario: 描述字段为空
- **WHEN** 用户未填写 Bug 描述直接点击提交
- **THEN** 系统拒绝提交，提示"请填写问题描述"

#### Scenario: 标题字段为空
- **WHEN** 用户未填写 Bug 标题直接点击提交
- **THEN** 系统拒绝提交，提示"请填写 Bug 标题"

#### Scenario: 标题超过 100 字符
- **WHEN** 用户填写的 Bug 标题超过 100 字符
- **THEN** 系统拒绝提交，提示"Bug 标题最多 100 字符"

### Requirement: Bug 报告自动捕获页面与环境信息
系统 SHALL 在提交 Bug 报告时自动记录当前页面 URL 和浏览器环境信息。

#### Scenario: 自动附加环境信息
- **WHEN** 用户在任何页面提交 Bug 报告
- **THEN** 系统记录当前 `window.location.href`、`navigator.userAgent`、屏幕分辨率、视口大小

### Requirement: Bug 报告支持上传截图（最多 3 张）
系统 SHALL 允许用户在提交 Bug 报告时上传截图，最多 3 张，截图通过现有 MinIO 文件系统存储。

#### Scenario: 上传多张截图（上限内）
- **WHEN** 用户在表单中上传 3 张截图
- **THEN** 每张截图通过 `POST /api/v1/file/upload` 上传后获得 fileId，提交时携带 fileId 列表，后端建立关联

#### Scenario: 不上传截图
- **WHEN** 用户不上传任何截图
- **THEN** 系统创建无图片关联的 Bug 报告

#### Scenario: 上传超过 3 张截图
- **WHEN** 用户尝试上传第 4 张截图
- **THEN** 系统阻止上传，提示"最多上传 3 张截图"

#### Scenario: 提交时携带超过 3 个 fileId
- **WHEN** 调用提交接口时 fileIds 数量超过 3 个
- **THEN** 系统返回 400 和"截图数量不能超过 3 张"错误

### Requirement: 提交后给出成功反馈
系统 SHALL 在用户成功提交 Bug 报告后，通过 Ant Design App 的 message 组件显示成功提示。

#### Scenario: 提交成功提示
- **WHEN** Bug 报告提交成功
- **THEN** 页面显示 "提交成功，感谢反馈！" 的 message 提示，持续 3 秒

### Requirement: 后端提供公开 Bug 报告提交接口
系统 SHALL 提供 `POST /api/v1/bug-reports` 接口，允许任何用户（含未登录访客）提交 Bug 报告。

#### Scenario: 访客调用提交接口
- **WHEN** 未登录用户调用提交接口并传入有效标题和描述
- **THEN** 系统返回 200 和创建成功的报告 ID

#### Scenario: 提交接口参数校验失败
- **WHEN** 调用提交接口时 description 为空字符串或长度超过 2000 字符
- **THEN** 系统返回 400 和参数校验错误信息

#### Scenario: 提交接口 title 为空
- **WHEN** 调用提交接口时 title 为空字符串或 null
- **THEN** 系统返回 400 和"Bug 标题不能为空"错误

#### Scenario: 提交接口 title 超长
- **WHEN** 调用提交接口时 title 长度超过 100 字符
- **THEN** 系统返回 400 和"Bug 标题最多 100 字符"错误信息

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

### Requirement: 平台可以接收 GitHub Webhook 事件并自动同步状态
系统 SHALL 提供一个公开 HTTP 端点接收 GitHub `issues` 类型 Webhook 事件，通过 HMAC-SHA256 签名验证请求来源，并根据事件类型自动更新对应 Bug 报告的状态。

#### Scenario: 成功接收 issues 事件
- **WHEN** GitHub 发送 `issues` 类型 Webhook 请求到端点
- **THEN** 系统接收并解析请求体中的事件类型和 Issue 数据

#### Scenario: 接收非 issues 类型事件
- **WHEN** GitHub 发送 `pull_request` 或其他类型 Webhook 请求
- **THEN** 系统返回 HTTP 200 但不进行任何业务处理

#### Scenario: 签名验证通过
- **WHEN** Webhook 请求携带有效的 `X-Hub-Signature-256` header
- **THEN** 系统通过验证并继续处理事件

#### Scenario: 签名验证失败
- **WHEN** Webhook 请求携带的签名与计算结果不匹配
- **THEN** 系统返回 HTTP 401 Unauthorized，不处理事件

#### Scenario: Issue 被分配时状态更新为处理中
- **WHEN** GitHub 发送 `issues.assigned` 事件，issue_number=42
- **THEN** 平台查找 `github_issue_number=42` 的 Bug 报告并将其状态更新为 `IN_PROGRESS`

#### Scenario: Issue 被关闭时状态更新为已解决
- **WHEN** GitHub 发送 `issues.closed` 事件，issue_number=42
- **THEN** 平台查找 `github_issue_number=42` 的 Bug 报告并将其状态更新为 `RESOLVED`

#### Scenario: Issue 被重新打开时状态更新为待处理
- **WHEN** GitHub 发送 `issues.reopened` 事件，issue_number=42
- **THEN** 平台查找 `github_issue_number=42` 的 Bug 报告并将其状态更新为 `PENDING`

#### Scenario: 分配事件对应的 BugReport 不存在
- **WHEN** GitHub 发送 `issues.assigned` 事件，但平台无对应记录
- **THEN** 系统记录警告日志，不做任何状态变更

#### Scenario: Webhook 业务处理异常不影响响应
- **WHEN** 接收到 `issues.closed` 事件后数据库查询失败
- **THEN** 系统记录错误日志，仍返回 HTTP 200，GitHub 不会重试

### Requirement: GitHub 直接创建的 Issue 可以反向同步到平台
系统 SHALL 在接收到 GitHub `issues.opened` 事件时，识别该 Issue 是否由平台创建。如果不是平台创建的，则在平台创建一条新的 Bug 报告记录。

#### Scenario: GitHub 直接新建 Issue 反向同步到平台
- **WHEN** GitHub 发送 `issues.opened` 事件，Issue body 中**不包含** `<!-- bluenet-bug-report -->` 标记
- **THEN** 系统在平台创建新 Bug 报告，title=Issue.title，description=Issue.body，status=`PENDING`，`github_issue_number` 和 `github_issue_url` 同步填写

#### Scenario: 平台创建的 Issue 被 webhook 收到时忽略
- **WHEN** GitHub 发送 `issues.opened` 事件，Issue body 中**包含** `<!-- bluenet-bug-report -->` 标记
- **THEN** 系统不做任何操作（平台创建时已同步）

#### Scenario: 反向同步时 description 为空
- **WHEN** GitHub 直接新建的 Issue body 为空
- **THEN** 平台创建的 Bug 报告 description 使用 title 作为降级值

#### Scenario: 反向同步时 title 为空
- **WHEN** GitHub 直接新建的 Issue title 为空（极罕见）
- **THEN** 系统记录警告日志，不创建 Bug 报告（title 是平台必填字段）

