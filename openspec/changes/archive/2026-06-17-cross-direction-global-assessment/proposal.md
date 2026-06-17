## Why

当前考核系统按 `(方向, 轮次, 年级)` 三维拆分，每个组合必须独立配置考核时间与题目。但"最终考核"（最后一轮）业务上需要所有方向、所有年级的考生共用同一套考题、在同一时间段答题，并且可以跨方向组队。当前系统不支持这种全局考核模式。

## What Changes

- 允许创建 `direction = null`、`grade = null` 的全局考核（仅 SUPER_ADMIN 可操作）
- 全局考核对所有考生可见，答题时跳过方向/年级校验
- 全局考核题目对所有考生可见（无方向限制）
- 全局考核支持跨方向组队（组队天然按 `assessment_time_id` 隔离）
- 团队成员及以上均可评审全局考核（跳过方向校验）
- 全局考核的最终轮次判定不受方向限制
- 前端考核时间创建/编辑界面支持"全局"和"不限年级"选项
- 数据库迁移：允许 `tb_assessment_time.grade` 为 null

## Capabilities

### New Capabilities

- `cross-direction-assessment`: 跨方向跨年级全局考核的创建、管理与答题支持

### Modified Capabilities

- `assessment-time-management`: 考核时间创建允许 direction 和 grade 为 null，添加唯一性校验逻辑
- `assessment-time-admin-ui`: 前端考核时间抽屉表单支持"全局"和"不限年级"选项
- `assessment-question-crud`: 题目访问控制跳过方向/年级校验（全局考核时）
- `assessment-judgement`: 评审权限控制跳过方向校验（全局考核时）
- `assessment-result-publication`: 最终轮次判定适应无方向/年级约束

## Impact

- 后端：AssessmentTime、AssessmentQuestion、AssessmentJudgement、AssessmentDecision 相关服务层和权限校验
- 前端：`AssessmentTimeDrawer.tsx` 方向/年级选择增加全局选项
- 数据库：`tb_assessment_time` 表 `grade` 字段去掉 NOT NULL 约束
- 仅 SUPER_ADMIN 可创建全局考核，DIRECTION_ADMIN 不受影响
