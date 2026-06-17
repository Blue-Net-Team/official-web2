## Requirements

### Requirement: 跨方向跨年级全局考核的定义
系统 SHALL 支持创建 `direction = null` 且 `grade = null` 的全局考核。全局考核表示该考核面向**所有方向、所有年级**的考生。

全局考核的 `(epoch, grade)` 组合 SHALL 具有唯一性约束（应用层控制），避免 `direction = null` 时重复创建。数据库 `UNIQUE (direction, epoch, grade)` 约束对 `(null, ...)` 行不生效（PostgreSQL null != null），唯一性 SHALL 由应用层 `countByEpochGrade` 保证。

#### Scenario: 创建全局考核（SUPER_ADMIN）
- **WHEN** SUPER_ADMIN 提交 direction=null, grade=null, epoch=3, startTime, endTime 等数据
- **THEN** 系统 SHALL 创建考核时间记录，direction 和 grade 字段为 null，并返回 200

#### Scenario: DIRECTION_ADMIN 无法创建全局考核
- **WHEN** DIRECTION_ADMIN 尝试创建 direction=null 的考核时间
- **THEN** 系统 SHALL 返回 403 错误，提示方向管理员不能创建跨方向考核

#### Scenario: 全局考核唯一性约束
- **WHEN** SUPER_ADMIN 创建 direction=null, epoch=3, grade=null 的考核时间后，再次提交相同 epoch=3, grade=null 的请求
- **THEN** 系统 SHALL 返回 400 错误，提示该轮次年级的全局考核时间已存在

### Requirement: 考生对全局考核的可见性
考生 SHALL 能在考核列表中看到全局考核（`direction = null, grade = null`），不受自身方向和年级限制。

管理端查询（`GET /api/v1/admin/assessment-times`）SHALL 根据角色过滤：
- CANDIDATE、MEMBER：显示本方向考核 + 全局考核
- DIRECTION_ADMIN 及以上：显示全部考核（含全局考核）

#### Scenario: 考生看到全局考核
- **WHEN** CANDIDATE 角色用户（direction=COMPUTER_VISION, 学号=2024xxx）请求考核列表
- **THEN** 结果 SHALL 包含 COMPUTER_VISION 方向专属考核 + 全局考核（direction=null, grade=null）

#### Scenario: 团队成员看到全局考核
- **WHEN** MEMBER 角色用户（direction=COMPUTER_VISION）请求考核列表
- **THEN** 结果 SHALL 包含 COMPUTER_VISION 方向考核 + 全局考核

#### Scenario: 方向管理员可以看到全局考核
- **WHEN** DIRECTION_ADMIN 请求考核列表
- **THEN** 结果 SHALL 包含所有考核（含全局考核）

### Requirement: 全局考核的题目访问控制
全局考核的题目 SHALL 对**所有考生**可见，不限制方向和年级。

系统 SHALL 在考生查询考题目录和题目详情时，跳过方向校验（仅当考核 `direction = null` 时不校验）。

#### Scenario: 不同方向考生查看全局考核题目
- **WHEN** COMPUTER_VISION 方向的考生查看全局考核的题目列表
- **THEN** 系统 SHALL 返回完整的考题目录

#### Scenario: 不同方向考生查看全局考核题目详情
- **WHEN** STRUCTURAL_DESIGN 方向的考生查看全局考核的某道题目详情
- **THEN** 系统 SHALL 返回该题目的完整详情

### Requirement: 全局考核的答题
全局考核的答案提交 SHALL 跳过方向匹配校验（当前 `validateDirectionMatch` 已处理 `time.getDirection() == null` 的短路逻辑）。已提交过个人答案的考生不可组队（与现有逻辑一致）。

#### Scenario: 考生提交全局考核答案
- **WHEN** 任意方向考生在全局考核时间段内提交答案
- **THEN** 系统 SHALL 正常接收并保存答案

### Requirement: 全局考核的组队
全局考核的组队功能 SHALL 与现有组队逻辑一致，组队按 `assessment_time_id` 隔离。不同方向的考生可加入同一队伍（无方向限制）。仅队长可提交文件上传题答案（与现有逻辑一致）。

#### Scenario: 跨方向组队
- **WHEN** COMPUTER_VISION 方向考生创建队伍后，STRUCTURAL_DESIGN 方向考生通过邀请码加入
- **THEN** 系统 SHALL 允许该考生加入队伍并全员归属于同一考核轮次
