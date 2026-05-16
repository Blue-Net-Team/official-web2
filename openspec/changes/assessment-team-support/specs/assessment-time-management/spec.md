## ADDED Requirements

### Requirement: 考核时间支持允许组队配置
系统 SHALL 在 `tb_assessment_time` 表中增加 `allow_team` 字段（BOOLEAN, DEFAULT FALSE），用于标识该考核是否允许组队答题。

创建和更新考核时间时，管理端接口 SHALL 支持传入 `allowTeam` 参数。响应 DTO SHALL 包含 `allowTeam` 字段。

#### Scenario: 创建允许组队的考核时间
- **WHEN** 管理员创建考核时间并设置 `allowTeam = true`
- **THEN** 系统 SHALL 保存 `allow_team = true`，并返回包含 `allowTeam` 字段的 DTO

#### Scenario: 响应 DTO 包含 allowTeam
- **WHEN** 查询或创建考核时间成功
- **THEN** 响应 SHALL 包含 `allowTeam` 字段

### Requirement: 跨方向考核时间对所有方向可见
当 `tb_assessment_time.direction` 为 `null` 时，该考核时间 SHALL 对所有方向的已登录用户可见（仍需匹配 `grade`）。

用户端查询接口 `GET /api/v1/assessment-times` 的过滤逻辑 SHALL 增加对 `direction IS NULL` 的支持。

#### Scenario: 跨方向考核对 CV 方向考生可见
- **WHEN** CV 方向考生查询考核时间列表，且存在 `direction = null, grade = 2025` 的考核
- **THEN** 系统 SHALL 返回该考核时间

#### Scenario: 跨方向考核对电控方向成员可见
- **WHEN** 电控方向成员查询考核时间列表，且存在 `direction = null` 的考核
- **THEN** 系统 SHALL 返回该考核时间

#### Scenario: 普通方向考核仍按原逻辑过滤
- **WHEN** CV 方向考生查询考核时间列表，且存在 `direction = STRUCTURAL_DESIGN` 的考核
- **THEN** 系统 SHALL NOT 返回该考核时间

## MODIFIED Requirements

### Requirement: 分页查询考核时间（管理端）
系统 SHALL 提供管理员分页查询接口 `GET /api/v1/admin/assessment-times`，支持 `page`（默认 0）和 `size`（默认 5）参数。

查询结果 SHALL 根据当前用户角色过滤：
- CANDIDATE：只返回当前用户方向 + 当前用户入学年份的考核时间，以及 `direction IS NULL` 且 `grade` 匹配的跨方向考核
- MEMBER：返回当前用户方向的全部考核时间，以及 `direction IS NULL` 的跨方向考核
- DIRECTION_ADMIN 及以上：返回全部考核时间

#### Scenario: 方向管理员查看全部考核时间
- **WHEN** DIRECTION_ADMIN 角色用户请求分页查询
- **THEN** 系统 SHALL 返回所有方向的考核时间，不按方向或年级过滤

#### Scenario: 团队成员查看自己方向的考核时间
- **WHEN** MEMBER 角色用户请求分页查询
- **THEN** 系统 SHALL 只返回该用户方向的全部考核时间（不限年级），以及 `direction IS NULL` 的跨方向考核

#### Scenario: 考生查看自己方向和入学年份的考核时间
- **WHEN** CANDIDATE 角色用户（学号=2024xxx）请求分页查询
- **THEN** 系统 SHALL 只返回该用户方向 + grade=2024 的考核时间，以及 `direction IS NULL` 且 `grade = 2024` 的跨方向考核

### Requirement: 考核时间数据模型包含年级字段
系统 SHALL 在 `tb_assessment_time` 表中包含 `grade` 字段（INTEGER, NOT NULL），取值为入学年份（如 2024、2025）。`(direction, epoch, grade)` 组合 SHALL 具有唯一约束，其中 `direction` 为 `null` 时按 `null` 值参与唯一约束。

#### Scenario: 唯一约束生效（含跨方向）
- **WHEN** 尝试创建 direction=null, epoch=1, grade=2024 的考核时间，且该组合已存在
- **THEN** 系统 SHALL 返回 400 错误，提示该方向轮次年级的考核时间已存在

#### Scenario: 跨方向和普通方向不冲突
- **WHEN** 已存在 direction=null, epoch=1, grade=2024 的考核，再创建 direction=COMPUTER_VISION, epoch=1, grade=2024 的考核
- **THEN** 系统 SHALL 允许创建成功
