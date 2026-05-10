## MODIFIED Requirements

### Requirement: 用户可以通过 FloatButton 提交 Bug 报告
系统 SHALL 在所有页面的右下角提供一个悬浮按钮（FloatButton），点击后弹出 Modal 表单，允许用户填写并提交 Bug 报告。

#### Scenario: 访客成功提交 Bug 报告
- **WHEN** 用户点击 FloatButton 并填写描述、上传 2 张截图、输入联系邮箱
- **THEN** 系统成功创建 Bug 报告记录并同步创建 GitHub Issue，返回成功提示包含本地报告 ID，Modal 自动关闭

## ADDED Requirements

### Requirement: 提交成功后返回 GitHub Issue 链接
系统 SHALL 在 Bug 报告提交接口的响应中包含创建的 GitHub Issue URL（如同步成功）。

#### Scenario: 同步成功时返回 GitHub Issue URL
- **WHEN** 用户成功提交 Bug 报告且 GitHub Issue 同步成功
- **THEN** 接口响应中包含 `githubIssueUrl` 字段，指向对应的 GitHub Issue 页面

#### Scenario: 同步失败时不返回 GitHub Issue URL
- **WHEN** 用户成功提交 Bug 报告但 GitHub Issue 同步失败
- **THEN** 接口响应中 `githubIssueUrl` 字段为 null，但提交仍视为成功
