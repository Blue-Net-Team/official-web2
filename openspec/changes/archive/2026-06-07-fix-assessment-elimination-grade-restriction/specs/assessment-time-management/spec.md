## MODIFIED Requirements

### Requirement: 创建考核时间
系统 SHALL 提供管理员接口创建考核时间，需要 `assessment-time:create` 权限。

创建时 SHALL 校验：
- `startTime` MUST 早于 `endTime`
- `timeLimit` 为 `true` 时，`timeLimitMinutes` MUST 不为空且大于 0
- `(direction, epoch, grade)` 组合 MUST 唯一
- **同方向同轮次 grade 形式 MUST 互斥**：若同方向同轮次已存在 `grade=null` 的记录，则 MUST NOT 创建 `grade!=null` 的记录；反之亦然
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

#### Scenario: 同方向同轮次 grade 形式冲突（已有 null 创建具体值）
- **WHEN** 已存在 direction=COMPUTER_VISION, epoch=1, grade=null 的考核时间
- **AND** 管理员尝试创建 direction=COMPUTER_VISION, epoch=1, grade=2024
- **THEN** 系统 SHALL 返回 400 错误，提示该方向轮次已存在不限年级的考核时间，不能创建限年级的考核时间

#### Scenario: 同方向同轮次 grade 形式冲突（已有具体值创建 null）
- **WHEN** 已存在 direction=COMPUTER_VISION, epoch=1, grade=2024 的考核时间
- **AND** 管理员尝试创建 direction=COMPUTER_VISION, epoch=1, grade=null
- **THEN** 系统 SHALL 返回 400 错误，提示该方向轮次已存在限年级的考核时间，不能创建不限年级的考核时间

### Requirement: 更新考核时间
系统 SHALL 提供管理员接口更新考核时间，需要 `assessment-time:update` 权限。

更新时 SHALL 校验：
- `startTime` MUST 早于 `endTime`（如果提供了时间字段）
- `timeLimit` 为 `true` 时，`timeLimitMinutes` MUST 不为空且大于 0
- 如果考核已开始（数据库中 `startTime <= now()`），则 MUST NOT 修改 `startTime`，但允许修改 `endTime`
- DIRECTION_ADMIN 角色 MUST 只能更新自己方向的考核时间
- **同方向同轮次 grade 形式 MUST 互斥**：更新后的 `(direction, epoch)` 若与已有记录冲突，同样 MUST 拒绝

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

#### Scenario: 更新导致同方向同轮次 grade 形式冲突
- **WHEN** 已存在 direction=COMPUTER_VISION, epoch=1, grade=null 的考核时间
- **AND** 管理员将另一个 COMPUTER_VISION, epoch=1 的考核时间更新为 grade=2024
- **THEN** 系统 SHALL 返回 400 错误，提示该方向轮次已存在不限年级的考核时间
