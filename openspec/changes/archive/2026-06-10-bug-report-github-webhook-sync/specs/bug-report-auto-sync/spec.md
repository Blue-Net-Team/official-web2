## ADDED Requirements

### Requirement: 平台可以根据 GitHub Issue assigned 事件自动更新状态为处理中
系统 SHALL 在接收到 GitHub `issues.assigned` 事件时，将对应 Bug 报告的状态更新为 `IN_PROGRESS`。

#### Scenario: Issue 被分配时状态更新为处理中
- **WHEN** GitHub 发送 `issues.assigned` 事件，issue_number=42
- **THEN** 平台查找 `github_issue_number=42` 的 Bug 报告并将其状态更新为 `IN_PROGRESS`

#### Scenario: 分配事件对应的 BugReport 不存在
- **WHEN** GitHub 发送 `issues.assigned` 事件，但平台无对应记录
- **THEN** 系统记录警告日志，不做任何状态变更

### Requirement: 平台可以根据 GitHub Issue closed 事件自动更新状态为已解决
系统 SHALL 在接收到 GitHub `issues.closed` 事件时，将对应 Bug 报告的状态更新为 `RESOLVED`。

#### Scenario: Issue 被关闭时状态更新为已解决
- **WHEN** GitHub 发送 `issues.closed` 事件，issue_number=42
- **THEN** 平台查找 `github_issue_number=42` 的 Bug 报告并将其状态更新为 `RESOLVED`

### Requirement: 平台可以根据 GitHub Issue reopened 事件自动更新状态为待处理
系统 SHALL 在接收到 GitHub `issues.reopened` 事件时，将对应 Bug 报告的状态更新为 `PENDING`。

#### Scenario: Issue 被重新打开时状态更新为待处理
- **WHEN** GitHub 发送 `issues.reopened` 事件，issue_number=42
- **THEN** 平台查找 `github_issue_number=42` 的 Bug 报告并将其状态更新为 `PENDING`

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
