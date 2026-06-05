## MODIFIED Requirements

### Requirement: 用户端"我的考核"按个人参与视角过滤
系统 SHALL 通过 `GET /api/v1/assessment-times` 接口返回当前用户的考核列表，过滤逻辑基于个人参与视角而非角色权限视角。

过滤条件 SHALL 为以下两者的并集：
1. **分配给我的考核**: `direction = 当前用户方向` AND `grade = 当前用户入学年份`
2. **我参与过的考核**: 该考核时间下存在当前用户的 `tb_assessment_answer` 记录

**新增**: 对于 `CANDIDATE` 角色，上述结果 SHALL 保留被后续淘汰限制命中的考核，但标记 `eliminated = true`，前端卡片显示 "已被淘汰" 且不可进入。如果该用户在相同 `direction+grade` 组合的某个更早 epoch 中被淘汰（`tb_assessment_decision.passed = false`），或该用户在方向考核被淘汰后尝试访问全局考核（epoch=0），则对应考核 SHALL 标记 `eliminated = true`。

`入学年份` SHALL 从用户学号前4位提取（即 `GradeCalculator.extractEnrollmentYear(studentId)`）。

#### Scenario: CANDIDATE 看到分配给自己的考核
- **WHEN** CANDIDATE 用户（方向=COMPUTER_VISION, 学号=2024xxx）请求考核列表
- **THEN** 系统 SHALL 返回 `direction=COMPUTER_VISION AND grade=2024` 的考核时间

#### Scenario: CANDIDATE 看到自己参与过的跨方向考核
- **WHEN** CANDIDATE 用户在 direction=STRUCTURAL_DESIGN 的考核中提交过答案
- **THEN** 系统 SHALL 同时返回该考核时间（即使方向不匹配）

#### Scenario: MEMBER 看到分配给自己的考核
- **WHEN** MEMBER 用户（方向=COMPUTER_VISION, 学号=2023xxx）请求考核列表
- **THEN** 系统 SHALL 返回 `direction=COMPUTER_VISION AND grade=2023` 的考核时间

#### Scenario: ADMIN 看到个人相关的考核而非全部
- **WHEN** ADMIN 用户请求考核列表
- **THEN** 系统 SHALL 返回匹配其个人方向+入学年份的考核，以及他有 answer 记录的考核，而非全部考核

#### Scenario: 考核列表包含答题进度
- **WHEN** 用户请求考核列表
- **THEN** 每个考核时间 DTO SHALL 包含 `totalQuestions`（题目总数）和 `completedQuestions`（当前用户已完成题数）

#### Scenario: 学号无法提取入学年份时只返回有 answer 的考核
- **WHEN** 用户的学号长度不足4位或格式异常
- **THEN** 系统 SHALL 仅返回该用户有 answer 记录的考核时间（不应用 direction+grade 过滤）

#### Scenario: Eliminated CANDIDATE can see next round but marked eliminated
- **WHEN** CANDIDATE 用户在 epoch=1 的考核中被淘汰（`passed = false`）
- **AND** 系统存在 epoch=2 的考核（相同 direction+grade）
- **THEN** 该用户请求考核列表时 SHALL 看到 epoch=2 的考核，但 `eliminated = true`
- **AND** 前端卡片显示 "已被淘汰" 且不可进入
- **AND** SHALL still see epoch=1 with `eliminated = false` (the round they were eliminated from)

#### Scenario: Eliminated CANDIDATE can see final round but marked eliminated
- **WHEN** CANDIDATE 用户在 epoch=1 的考核中被淘汰（`passed = false`）
- **AND** 系统存在 epoch=0（最终考核）的考核（相同 direction+grade）
- **THEN** 该用户请求考核列表时 SHALL 看到 epoch=0 的考核，但 `eliminated = true`
- **AND** 前端卡片显示 "已被淘汰" 且不可进入
