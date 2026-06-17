## MODIFIED Requirements

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

#### Scenario: SUPER_ADMIN 创建全局考核
- **WHEN** SUPER_ADMIN 提交 direction=null, grade=null, epoch=3, 有效 startTime 和 endTime
- **THEN** 系统 SHALL 创建考核时间记录，direction 和 grade 为 null，返回 200 及 DTO

#### Scenario: DIRECTION_ADMIN 创建全局考核被拒绝
- **WHEN** DIRECTION_ADMIN 提交 direction=null 的考核时间
- **THEN** 系统 SHALL 返回 403 错误，提示方向管理员不能创建跨方向考核

#### Scenario: SUPER_ADMIN 创建任意方向考核成功
- **WHEN** SUPER_ADMIN 创建任意方向或全局的考核时间
- **THEN** 系统 SHALL 创建成功并返回 200

- ~~`grade` MUST 为有效的入学年份（建议范围：当前年份往前推5年内）~~（改为允许 null）

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

### Requirement: 考核时间 DTO 包含全局考核字段支持

考核时间 DTO SHALL 包含以下字段：`id`、`direction`（可为 null）、`epoch`、`grade`（可为 null）、`startTime`、`endTime`、`timeLimit`、`timeLimitMinutes`。

创建和更新请求 DTO SHALL 包含：`direction`（可为 null）、`epoch`、`grade`（可为 null）、`startTime`、`endTime`、`timeLimit`、`timeLimitMinutes`（timeLimit 为 true 时必填）。

#### Scenario: 响应 DTO 字段完整（含全局考核）
- **WHEN** 创建或查询全局考核成功
- **THEN** 响应 SHALL 包含 id、direction（null）、epoch、grade（null）、startTime、endTime、timeLimit、timeLimitMinutes 字段
