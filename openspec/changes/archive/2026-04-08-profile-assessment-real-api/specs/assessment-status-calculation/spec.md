## ADDED Requirements

### Requirement: 系统根据时间动态计算考核状态
系统应当根据当前时间和考核时间范围动态计算考核状态（未开始/进行中/已结束）。

#### Scenario: 考核未开始
- **WHEN** 当前时间早于考核开始时间（`currentTime < startTime`）
- **THEN** 考核状态为 `not-started`，显示"距开始还有 X 天"

#### Scenario: 考核进行中（非限时）
- **WHEN** 当前时间在考核时间范围内（`startTime <= currentTime <= endTime`）且考核为非限时
- **THEN** 考核状态为 `in-progress`，显示剩余时间和答题进度

#### Scenario: 考核进行中（限时）
- **WHEN** 当前时间在考核时间范围内且考核为限时，且当前时间早于会话截止时间（`currentTime < deadline`）
- **THEN** 考核状态为 `in-progress`，显示剩余时间（基于 `deadline` 计算）和答题进度

#### Scenario: 考核已结束（非限时）
- **WHEN** 当前时间晚于考核结束时间（`currentTime > endTime`）且考核为非限时
- **THEN** 考核状态为 `ended`，显示最终得分（如有）

#### Scenario: 考核已结束（限时）
- **WHEN** 当前时间晚于会话截止时间（`currentTime > deadline`）且考核为限时
- **THEN** 考核状态为 `ended`，显示最终得分（如有）

### Requirement: 系统计算剩余时间和倒计时
系统应当根据考核类型（限时/非限时）计算并展示剩余时间。

#### Scenario: 计算非限时考核剩余时间
- **WHEN** 考核为非限时且正在进行时
- **THEN** 剩余时间 = `endTime - currentTime`，格式化为"X 天 Y 小时"

#### Scenario: 计算限时考核剩余时间
- **WHEN** 考核为限时且正在进行时
- **THEN** 剩余时间 = `deadline - currentTime`，格式化为"X 天 Y 小时"

#### Scenario: 计算距离开始的天数
- **WHEN** 考核状态为 `not-started` 时
- **THEN** 显示"距开始还有 X 天"，其中 X = `startTime - currentTime` 的天数
