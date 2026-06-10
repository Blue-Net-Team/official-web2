## ADDED Requirements

### Requirement: 系统可以定时从 GitHub 拉取最近更新的 Issue 列表
系统 SHALL 提供一个 Spring `@Scheduled` 定时任务，按配置的 cron 表达式执行，调用 GitHub REST API 获取指定时间范围内有更新的 Issue 列表。

#### Scenario: 定时任务在配置的时间触发
- **WHEN** 到达配置的 cron 时间点（默认每天凌晨 3:00）
- **THEN** 系统执行定时任务，开始拉取 GitHub Issue

#### Scenario: 定时任务被禁用时不执行
- **WHEN** 配置 `github.app.polling-enabled=false`
- **THEN** 定时任务跳过执行，记录 debug 日志

#### Scenario: GitHub App 未启用时不执行
- **WHEN** `github.app` 基本配置（app-id / private-key / owner / repo）未完整配置
- **THEN** 定时任务跳过执行，记录 warn 日志

### Requirement: 系统可以将拉取的 Issue 与本地 Bug 报告进行状态对账
系统 SHALL 遍历拉取的 Issue 列表，对每条 Issue 检查本地是否存在对应的 Bug 报告。不存在则创建；存在则对比状态，仅在有差异时更新。

#### Scenario: 本地不存在对应的 Bug 报告时反向同步创建
- **WHEN** 拉取的 Issue #42 在平台无对应记录，且 body 不包含 `<!-- bluenet-bug-report -->`
- **THEN** 系统创建新 Bug 报告，title=Issue.title，description=Issue.body，status 根据 Issue.state 映射（open→PENDING，closed→RESOLVED），并记录 github_issue_number 和 github_issue_url

#### Scenario: 本地已存在时状态一致则跳过
- **WHEN** 拉取的 Issue #42 在平台已有对应 Bug 报告，且状态与 Issue 当前状态一致
- **THEN** 系统不做任何更新，记录 debug 日志

#### Scenario: 本地已存在时状态不一致则更新
- **WHEN** 拉取的 Issue #42 在平台状态为 PENDING，但 GitHub 上已 closed
- **THEN** 系统将对应 Bug 报告状态更新为 RESOLVED，并记录 info 日志

#### Scenario: 单条 Issue 处理失败不影响其他条目
- **WHEN** Issue #42 处理过程中数据库异常
- **THEN** 系统记录 error 日志，继续处理列表中的下一条 Issue

### Requirement: 系统状态映射遵循 GitHub Issue 状态
系统 SHALL 将 GitHub Issue 的 `state` 字段映射为平台 BugReportStatus：
- `open` → `PENDING`（除非已被 assigned，但 polling 只读 state，不读 assignee）
- `closed` → `RESOLVED`

#### Scenario: open 状态的 Issue 映射为 PENDING
- **WHEN** 拉取的 Issue state 为 `open`
- **THEN** 创建的或更新的 Bug 报告状态为 PENDING

#### Scenario: closed 状态的 Issue 映射为 RESOLVED
- **WHEN** 拉取的 Issue state 为 `closed`
- **THEN** 创建的或更新的 Bug 报告状态为 RESOLVED
