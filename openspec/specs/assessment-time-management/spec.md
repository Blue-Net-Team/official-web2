## Requirements

### Requirement: 考核时间数据模型包含年级字段
系统 SHALL 在 `tb_assessment_time` 表中包含 `grade` 字段（INTEGER, 可为 null），取值为入学年份（如 2024、2025）或 null（不限年级）。`(direction, epoch, grade)` 组合 SHALL 具有唯一约束。当 `direction = null` 时，唯一性由应用层按 `(epoch, grade)` 保证。

#### Scenario: 唯一约束生效（非空 direction）
- **WHEN** 尝试创建 direction=COMPUTER_VISION, epoch=1, grade=2024 的考核时间，且该组合已存在
- **THEN** 系统 SHALL 返回 400 错误，提示该方向轮次年级的考核时间已存在

#### Scenario: 唯一约束生效（全局考核）
- **WHEN** 尝试创建 direction=null, epoch=3, grade=null 的全局考核，且 (epoch=3, grade=null) 组合已存在
- **THEN** 系统 SHALL 返回 400 错误，提示该轮次的全局考核时间已存在

### Requirement: 创建考核时间
系统 SHALL 提供管理员接口创建考核时间，需要 `assessment-time:create` 权限。

创建时 SHALL 校验：
- `startTime` MUST 早于 `endTime`
- `timeLimit` 为 `true` 时，`timeLimitMinutes` MUST 不为空且大于 0
- `direction` 为 null 时，按 `(epoch, grade)` 校验唯一性；`direction` 非 null 时，按 `(direction, epoch, grade)` 校验唯一性
- `grade` 可为 null（仅当 `direction` 也为 null 时），非 null 时 MUST 为有效的入学年份
- DIRECTION_ADMIN 角色 MUST 只能创建自己方向的考核时间；MUST NOT 创建 `direction = null` 的考核
- SUPER_ADMIN 角色 SHALL 可创建全局考核（`direction = null, grade = null`）

#### Scenario: 成功创建
- **WHEN** 管理员提交有效的考核时间数据（direction, epoch, grade=2024, startTime, endTime, timeLimit 等）
- **THEN** 系统 SHALL 创建考核时间记录并返回 200 及完整的考核时间 DTO

#### Scenario: SUPER_ADMIN 创建全局考核
- **WHEN** SUPER_ADMIN 提交 direction=null, grade=null, epoch=3, 有效 startTime 和 endTime
- **THEN** 系统 SHALL 创建考核时间记录，direction 和 grade 为 null，返回 200 及 DTO

#### Scenario: DIRECTION_ADMIN 创建其他方向被拒绝
- **WHEN** DIRECTION_ADMIN（direction=COMPUTER_VISION）尝试创建 direction=STRUCTURAL_DESIGN 的考核时间
- **THEN** 系统 SHALL 返回 403 错误，提示只能创建本方向的考核时间

#### Scenario: DIRECTION_ADMIN 创建全局考核被拒绝
- **WHEN** DIRECTION_ADMIN 提交 direction=null 的考核时间
- **THEN** 系统 SHALL 返回 403 错误，提示方向管理员不能创建跨方向考核

#### Scenario: SUPER_ADMIN 创建任意方向考核成功
- **WHEN** SUPER_ADMIN 创建任意方向或全局的考核时间
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

### Requirement: 分页查询考核时间（管理端）
系统 SHALL 提供管理员分页查询接口 `GET /api/v1/admin/assessment-times`，支持 `page`（默认 0）和 `size`（默认 5）参数。

查询结果 SHALL 根据当前用户角色过滤：
- CANDIDATE：返回当前用户方向的考核时间 + 全局考核（direction=null, grade=null）
- MEMBER：返回当前用户方向的考核时间 + 全局考核
- DIRECTION_ADMIN 及以上：返回全部考核时间（含全局考核）

#### Scenario: 方向管理员查看全部考核时间
- **WHEN** DIRECTION_ADMIN 角色用户请求分页查询
- **THEN** 系统 SHALL 返回所有方向的考核时间，包含全局考核

#### Scenario: 团队成员查看自己方向含全局考核
- **WHEN** MEMBER 角色用户请求分页查询
- **THEN** 系统 SHALL 返回该用户方向的考核时间 + 全局考核（不限年级）

#### Scenario: 考生查看含全局考核
- **WHEN** CANDIDATE 角色用户（学号=2024xxx）请求分页查询
- **THEN** 系统 SHALL 返回该用户方向 + grade=2024 的考核时间 + 全局考核

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

### Requirement: 考核时间 DTO 包含全局考核字段支持
考核时间 DTO SHALL 包含以下字段：`id`、`direction`（可为 null）、`epoch`、`grade`（可为 null）、`startTime`、`endTime`、`timeLimit`、`timeLimitMinutes`。

创建和更新请求 DTO SHALL 包含：`direction`（可为 null）、`epoch`、`grade`（可为 null）、`startTime`、`endTime`、`timeLimit`、`timeLimitMinutes`（timeLimit 为 true 时必填）。

#### Scenario: 响应 DTO 字段完整（含全局考核）
- **WHEN** 创建或查询全局考核成功
- **THEN** 响应 SHALL 包含 id、direction（null）、epoch、grade（null）、startTime、endTime、timeLimit、timeLimitMinutes 字段

### Requirement: 考核卡片操作按钮跳转
考核列表页卡片的操作按钮 SHALL 根据考核状态跳转到考题目录页：
- 进行中：点击"继续答题"按钮跳转到 `/assessment/{timeId}/questions`
- 已结束：点击"查看详情"按钮跳转到 `/assessment/{timeId}/questions`
- 未开始：按钮保持禁用状态，不可点击

#### Scenario: 点击进行中考核的操作按钮
- **WHEN** 用户点击状态为"进行中"的考核卡片的"继续答题"按钮
- **THEN** 系统跳转到 `/assessment/{item.id}/questions` 页面

#### Scenario: 点击已结束考核的操作按钮
- **WHEN** 用户点击状态为"已结束"的考核卡片的"查看详情"按钮
- **THEN** 系统跳转到 `/assessment/{item.id}/questions` 页面

#### Scenario: 点击未开始考核的操作按钮
- **WHEN** 用户点击状态为"未开始"的考核卡片的"暂不可进入"按钮
- **THEN** 按钮无响应，不发生跳转
