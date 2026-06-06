## Why

全局考核（`direction=null, grade=null`）已支持创建和答题，但**评分页面和录用决策页面无法选择全局考核**，前端方向选择器缺少"全局"选项。更关键的是，全局考核的核心场景是"跨方向组队"，而当前**组队基础逻辑存在多个缺口**：队长提交后不会自动为组员创建 answer 记录，评分查询 SQL 完全不认识 `team_id`，导致组员在评分和录用链路中完全不可见。同时，组队生命周期管理缺失（提交后未锁定、解散未清理数据），前端也没有向用户声明组队规则。

## What Changes

- **前端**：评分页面（`/admin/assessment/judge/score`）和录用决策页面（`/admin/assessment/judge/decision`）的方向选择器增加"全局"选项
- **前端**：`TeamPanel.tsx` 增加组队规则声明和风险提示
- **后端**：队长提交 FILE_UPLOAD 答案后，自动为所有组员创建 answer 记录（内容复制队长）
- **后端**：修改 `AssessmentJudgementMapper.xml` 中的评分查询，支持通过 `team_id` 展开组员数据
- **后端**：队长提交答案后锁定队伍（禁止退出、转让、解散）
- **后端**：解散队伍时级联删除关联的 answer 和 judgement 记录
- **后端**：加入队伍时检查是否已有队伍答案，避免重复继承
- **后端**：录用决策和评分统计支持全局考核考生（含跨方向组队组员）

## Capabilities

### New Capabilities
- `assessment-team-scoring`: 组队评分数据链路支持，包括组员 answer 自动创建、评分查询 team_id 展开、按队伍聚合展示
- `assessment-team-lifecycle`: 组队生命周期管理，包括提交锁定、解散清理、跨队伍限制

### Modified Capabilities
- `assessment-judgement`: 评分查询需支持 `team_id` 展开以包含组员；人工评分时支持全队同分或单独调整
- `assessment-result-publication`: 录用决策需支持全局考核考生（不限方向），决策按考生独立
- `assessment-time-admin-ui`: 前端考核时间管理界面已支持"全局"选项，需扩展至评分和决策页面

## Impact

- 前端：`/admin/assessment/judge/score/page.tsx`、`/admin/assessment/judge/decision/page.tsx`、`TeamPanel.tsx`
- 后端：`AssessmentAnswerAppServiceImpl`、`AssessmentJudgementAppServiceImpl`、`AssessmentTeamAppServiceImpl`、`AssessmentJudgementMapper.xml`
- 数据库：无 schema 变更（复用现有 `team_id` 字段）
- 关联 Issue：#43
