## Requirements

### Requirement: 考核时间数据模型包含年级字段
系统 SHALL 在 `tb_assessment_time` 表中包含 `grade` 字段（INTEGER, NOT NULL），取值为 1（大一）、2（大二）、3（大三）。`(direction, epoch, grade)` 组合 SHALL 具有唯一约束。

#### Scenario: 唯一约束生效
- **WHEN** 尝试创建 direction=COMPUTER_VISION, epoch=1, grade=1 的考核时间，且该组合已存在
- **THEN** 系统 SHALL 返回 400 错误，提示该方向轮次年级的考核时间已存在

### Requirement: 创建考核时间
系统 SHALL 提供管理员接口创建考核时间，需要 `assessment-time:create` 权限。

创建时 SHALL 校验：
- `startTime` MUST 早于 `endTime`
- `timeLimit` 为 `true` 时，`timeLimitMinutes` MUST 不为空且大于 0
- `(direction, epoch, grade)` 组合 MUST 唯一

#### Scenario: 成功创建
- **WHEN** 管理员提交有效的考核时间数据（direction, epoch, grade, startTime, endTime, timeLimit 等）
- **THEN** 系统 SHALL 创建考核时间记录并返回 200 及完整的考核时间 DTO

#### Scenario: 开始时间不早于结束时间
- **WHEN** 管理员提交 startTime >= endTime
- **THEN** 系统 SHALL 返回 400 错误，提示开始时间必须早于结束时间

#### Scenario: 限时考核缺少限时分钟数
- **WHEN** 管理员提交 timeLimit=true 但 timeLimitMinutes 为空或小于等于 0
- **THEN** 系统 SHALL 返回 400 错误，提示限时考核必须设置有效的限时分钟数

### Requirement: 分页查询考核时间（管理端）
系统 SHALL 提供管理员分页查询接口 `GET /api/v1/admin/assessment-times`，支持 `page`（默认 0）和 `size`（默认 5）参数。

查询结果 SHALL 根据当前用户角色过滤：
- CANDIDATE：只返回当前用户方向 + 当前用户年级的考核时间
- MEMBER：返回当前用户方向的全部考核时间
- DIRECTION_ADMIN 及以上：返回全部考核时间

#### Scenario: 方向管理员查看全部考核时间
- **WHEN** DIRECTION_ADMIN 角色用户请求分页查询
- **THEN** 系统 SHALL 返回所有方向的考核时间，不按方向或年级过滤

#### Scenario: 团队成员查看自己方向的考核时间
- **WHEN** MEMBER 角色用户请求分页查询
- **THEN** 系统 SHALL 只返回该用户方向的全部考核时间（不限年级）

#### Scenario: 考生查看自己方向和年级的考核时间
- **WHEN** CANDIDATE 角色用户请求分页查询
- **THEN** 系统 SHALL 只返回该用户方向 + 该用户年级的考核时间

### Requirement: 查询考核时间（用户端）
系统 SHALL 提供已登录用户查询接口 `GET /api/v1/assessment-times`，返回当前用户可见的考核时间列表。访问级别为 AUTHENTICATED。

角色过滤规则与管理端查询相同。

#### Scenario: 考生查看自己的考核时间
- **WHEN** CANDIDATE 用户请求考核时间列表
- **THEN** 系统 SHALL 返回该用户方向和年级对应的考核时间

### Requirement: 更新考核时间
系统 SHALL 提供管理员接口更新考核时间，需要 `assessment-time:update` 权限。

更新时 SHALL 校验：
- `startTime` MUST 早于 `endTime`（如果提供了时间字段）
- `timeLimit` 为 `true` 时，`timeLimitMinutes` MUST 不为空且大于 0
- 如果考核已开始（数据库中 `startTime <= now()`），则 MUST NOT 修改 `startTime`，但允许修改 `endTime`

#### Scenario: 成功更新未开始的考核时间
- **WHEN** 管理员更新一个未开始的考核时间的所有字段
- **THEN** 系统 SHALL 更新记录并返回 200 及更新后的考核时间 DTO

#### Scenario: 已开始考核修改开始时间被拒绝
- **WHEN** 管理员尝试修改已开始考核的 startTime（数据库 startTime <= 当前时间）
- **THEN** 系统 SHALL 返回 400 错误，提示已开始的考核不允许修改开始时间

#### Scenario: 已开始考核修改结束时间成功
- **WHEN** 管理员仅修改已开始考核的 endTime（不修改 startTime）
- **THEN** 系统 SHALL 更新 endTime 并返回 200 及更新后的考核时间 DTO

### Requirement: 删除考核时间
系统 SHALL 提供管理员接口删除考核时间，需要 `assessment-time:delete` 权限。

删除时 SHALL 检查是否有关联的考核题目（`tb_assessment_question.assessment_time_id`）。

#### Scenario: 成功删除无关联题目的考核时间
- **WHEN** 管理员删除一个没有关联题目的考核时间
- **THEN** 系统 SHALL 删除该记录并返回 200

#### Scenario: 删除有关联题目的考核时间被拒绝
- **WHEN** 管理员删除一个存在关联题目的考核时间
- **THEN** 系统 SHALL 返回 409 Conflict 错误，提示存在关联的考核题目，需先删除相关题目

### Requirement: 考核时间 DTO 包含完整信息
考核时间 DTO SHALL 包含以下字段：`id`、`direction`、`epoch`、`grade`、`startTime`、`endTime`、`timeLimit`、`timeLimitMinutes`。

创建和更新请求 DTO SHALL 包含：`direction`、`epoch`、`grade`、`startTime`、`endTime`、`timeLimit`、`timeLimitMinutes`（timeLimit 为 true 时必填）。

#### Scenario: 响应 DTO 字段完整
- **WHEN** 创建或查询考核时间成功
- **THEN** 响应 SHALL 包含 id、direction、epoch、grade、startTime、endTime、timeLimit、timeLimitMinutes 字段

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
