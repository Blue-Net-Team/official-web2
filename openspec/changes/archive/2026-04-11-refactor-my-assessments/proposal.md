## Why

个人主页"我的考核"tab 当前使用管理端的角色权限过滤逻辑（ADMIN 看全部、MEMBER 按方向、CANDIDATE 按方向+年级），而非"个人参与"视角。同时 `tb_assessment_time.grade` 存储的是年级序号（1/2/3），新一轮考核需要物理删除旧数据，导致晋级后的成员无法追溯历史考核。

## What Changes

- **BREAKING**: `tb_assessment_time.grade` 语义从年级序号（1/2/3）改为入学年份（如 2024），使不同年份的考核数据天然隔离，不再需要在新一轮开始时删除旧考核数据
- **BREAKING**: 用户端 `GET /api/v1/assessment-times` 的过滤逻辑从"角色权限视角"改为"个人参与视角"，所有角色统一按 `(direction + grade) OR EXISTS(answer)` 过滤
- `GradeCalculator` 从计算"当前年级序号"改为提取"入学年份"
- 管理端 `GET /api/v1/admin/assessment-times` 保持现有角色权限过滤逻辑不变
- 前端 Admin 创建考核时间的年级选择 UI 从 1/2/3 下拉调整为入学年份输入

## Capabilities

### New Capabilities
- `my-assessments-query`: 用户端"我的考核"查询能力，基于个人参与视角（分配给我的 + 我参与过的），与管理端的权限过滤逻辑解耦

### Modified Capabilities
- `assessment-time-management`: `grade` 字段语义从年级序号（1/2/3）改为入学年份，影响创建、更新、查询逻辑及 Flyway 迁移
- `backend-evaluation-system`: `GradeCalculator` 逻辑变更，从计算当前年级序号改为提取入学年份
- `frontend-user-profile`: 个人主页"我的考核"tab 使用新的用户端查询接口

## Impact

- **数据库**: 需要 Flyway 迁移转换现有 `tb_assessment_time.grade` 数据
- **后端 API**: `GET /api/v1/assessment-times` 响应数据范围变化（管理端接口不变）
- **后端服务层**: `listAssessmentTimesForUser()` 重写，`AssessmentTimeRepository` 新增查询方法
- **前端**: Admin 创建考核时间的年级输入 UI 需调整；个人主页前端基本不动（数据结构不变）
