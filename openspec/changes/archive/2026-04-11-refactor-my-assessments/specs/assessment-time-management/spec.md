## MODIFIED Requirements

### Requirement: 考核时间数据模型包含年级字段
系统 SHALL 在 `tb_assessment_time` 表中包含 `grade` 字段（INTEGER, NOT NULL），取值为入学年份（如 2024、2025）。`(direction, epoch, grade)` 组合 SHALL 具有唯一约束。

#### Scenario: 唯一约束生效
- **WHEN** 尝试创建 direction=COMPUTER_VISION, epoch=1, grade=2024 的考核时间，且该组合已存在
- **THEN** 系统 SHALL 返回 400 错误，提示该方向轮次年级的考核时间已存在

### Requirement: 创建考核时间
系统 SHALL 提供管理员接口创建考核时间，需要 `assessment-time:create` 权限。

创建时 SHALL 校验：
- `startTime` MUST 早于 `endTime`
- `timeLimit` 为 `true` 时，`timeLimitMinutes` MUST 不为空且大于 0
- `(direction, epoch, grade)` 组合 MUST 唯一
- `grade` MUST 为有效的入学年份（建议范围：当前年份往前推5年内）

#### Scenario: 成功创建
- **WHEN** 管理员提交有效的考核时间数据（direction, epoch, grade=2024, startTime, endTime, timeLimit 等）
- **THEN** 系统 SHALL 创建考核时间记录并返回 200 及完整的考核时间 DTO

#### Scenario: grade 不是有效的入学年份
- **WHEN** 管理员提交 grade=1（旧格式）
- **THEN** 系统 SHALL 返回 400 错误，提示 grade 必须为有效的入学年份

### Requirement: 分页查询考核时间（管理端）
系统 SHALL 提供管理员分页查询接口 `GET /api/v1/admin/assessment-times`，支持 `page`（默认 0）和 `size`（默认 5）参数。

查询结果 SHALL 根据当前用户角色过滤：
- CANDIDATE：只返回当前用户方向 + 当前用户入学年份的考核时间
- MEMBER：返回当前用户方向的全部考核时间
- DIRECTION_ADMIN 及以上：返回全部考核时间

#### Scenario: 方向管理员查看全部考核时间
- **WHEN** DIRECTION_ADMIN 角色用户请求分页查询
- **THEN** 系统 SHALL 返回所有方向的考核时间，不按方向或年级过滤

#### Scenario: 团队成员查看自己方向的考核时间
- **WHEN** MEMBER 角色用户请求分页查询
- **THEN** 系统 SHALL 只返回该用户方向的全部考核时间（不限年级）

#### Scenario: 考生查看自己方向和入学年份的考核时间
- **WHEN** CANDIDATE 角色用户（学号=2024xxx）请求分页查询
- **THEN** 系统 SHALL 只返回该用户方向 + grade=2024 的考核时间

## REMOVED Requirements

### Requirement: 查询考核时间（用户端）
**Reason**: 用户端查询逻辑已由新能力 `my-assessments-query` 替代，不再复用管理端的角色权限过滤
**Migration**: `GET /api/v1/assessment-times` 改为调用 `listAssessmentTimesForUser()` 的独立实现，使用个人参与视角过滤
