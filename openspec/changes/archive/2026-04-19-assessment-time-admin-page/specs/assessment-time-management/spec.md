## MODIFIED Requirements

### Requirement: 创建考核时间
系统 SHALL 提供管理员接口创建考核时间，需要 `assessment-time:create` 权限。

创建时 SHALL 校验：
- `startTime` MUST 早于 `endTime`
- `timeLimit` 为 `true` 时，`timeLimitMinutes` MUST 不为空且大于 0
- `(direction, epoch, grade)` 组合 MUST 唯一
- `grade` MUST 为有效的入学年份（建议范围：当前年份往前推5年内）
- DIRECTION_ADMIN 角色 MUST 只能创建自己方向的考核时间（`request.direction == currentUser.direction`）

#### Scenario: 成功创建
- **WHEN** 管理员提交有效的考核时间数据（direction, epoch, grade=2024, startTime, endTime, timeLimit 等）
- **THEN** 系统 SHALL 创建考核时间记录并返回 200 及完整的考核时间 DTO

#### Scenario: DIRECTION_ADMIN 创建其他方向被拒绝
- **WHEN** DIRECTION_ADMIN（direction=COMPUTER_VISION）尝试创建 direction=STRUCTURAL_DESIGN 的考核时间
- **THEN** 系统 SHALL 返回 403 错误，提示只能创建本方向的考核时间

#### Scenario: SUPER_ADMIN 创建任意方向成功
- **WHEN** SUPER_ADMIN 创建任意方向的考核时间
- **THEN** 系统 SHALL 创建成功并返回 200

#### Scenario: grade 不是有效的入学年份
- **WHEN** 管理员提交 grade=1（旧格式）
- **THEN** 系统 SHALL 返回 400 错误，提示 grade 必须为有效的入学年份

#### Scenario: 开始时间不早于结束时间
- **WHEN** 管理员提交 startTime >= endTime
- **THEN** 系统 SHALL 返回 400 错误，提示开始时间必须早于结束时间

#### Scenario: 限时考核缺少限时分钟数
- **WHEN** 管理员提交 timeLimit=true 但 timeLimitMinutes 为空或小于等于 0
- **THEN** 系统 SHALL 返回 400 错误，提示限时考核必须设置有效的限时分钟数

### Requirement: 更新考核时间
系统 SHALL 提供管理员接口更新考核时间，需要 `assessment-time:update` 权限。

更新时 SHALL 校验：
- `startTime` MUST 早于 `endTime`（如果提供了时间字段）
- `timeLimit` 为 `true` 时，`timeLimitMinutes` MUST 不为空且大于 0
- 如果考核已开始（数据库中 `startTime <= now()`），则 MUST NOT 修改 `startTime`，但允许修改 `endTime`
- DIRECTION_ADMIN 角色 MUST 只能更新自己方向的考核时间

#### Scenario: 成功更新未开始的考核时间
- **WHEN** 管理员更新一个未开始的考核时间的所有字段
- **THEN** 系统 SHALL 更新记录并返回 200 及更新后的考核时间 DTO

#### Scenario: DIRECTION_ADMIN 更新其他方向被拒绝
- **WHEN** DIRECTION_ADMIN（direction=COMPUTER_VISION）尝试更新 direction=STRUCTURAL_DESIGN 的考核时间
- **THEN** 系统 SHALL 返回 403 错误，提示只能更新本方向的考核时间

#### Scenario: 已开始考核修改开始时间被拒绝
- **WHEN** 管理员尝试修改已开始考核的 startTime（数据库 startTime <= 当前时间）
- **THEN** 系统 SHALL 返回 400 错误，提示已开始的考核不允许修改开始时间

#### Scenario: 已开始考核修改结束时间成功
- **WHEN** 管理员仅修改已开始考核的 endTime（不修改 startTime）
- **THEN** 系统 SHALL 更新 endTime 并返回 200 及更新后的考核时间 DTO

### Requirement: 删除考核时间
系统 SHALL 提供管理员接口删除考核时间，需要 `assessment-time:delete` 权限。

删除时 SHALL 检查是否有关联的考核题目（`tb_assessment_question.assessment_time_id`）。
DIRECTION_ADMIN 角色 MUST 只能删除自己方向的考核时间。

#### Scenario: 成功删除无关联题目的考核时间
- **WHEN** 管理员删除一个没有关联题目的考核时间
- **THEN** 系统 SHALL 删除该记录并返回 200

#### Scenario: DIRECTION_ADMIN 删除其他方向被拒绝
- **WHEN** DIRECTION_ADMIN（direction=COMPUTER_VISION）尝试删除 direction=STRUCTURAL_DESIGN 的考核时间
- **THEN** 系统 SHALL 返回 403 错误，提示只能删除本方向的考核时间

#### Scenario: 删除有关联题目的考核时间被拒绝
- **WHEN** 管理员删除一个存在关联题目的考核时间
- **THEN** 系统 SHALL 返回 409 Conflict 错误，提示存在关联的考核题目，需先删除相关题目
