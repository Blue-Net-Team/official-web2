## Why

当前考核评判能力已经能保存单题评判记录、文件上传题人工评分和单个考生最终通过决策，但管理端缺少完整的评分工作台和录用决策工作台。管理员无法按考核方向、考核时间、题目或考生维度集中查看提交与评分，也无法在一个页面中确认本轮通过/淘汰名单并准备发布结果。

本变更将把现有底层评判能力扩展为可实际运营的管理端流程：先完成题目评分，再基于汇总成绩做录用决策，最后预留发布本轮结果的入口。

## What Changes

- 新增管理端“题目评分”页面，支持按考核方向和考核时间筛选考题，并提供题目视图与人员视图。
- 题目视图中，管理员点击提交记录行打开右侧 AntD Drawer，输入分数和评论完成文件上传题人工评分。
- 人员视图中，管理员按考生维度查看每名考生的各题得分、总分、未评分项和提交状态。
- 新增管理端“录用决策”页面，展示候选人、待决策、通过、淘汰统计，并支持对单个考生点击“通过”或“淘汰”后自动保存最终决策。
- 新增或扩展后端聚合查询接口，返回评分页和决策页所需的考核时间、题目、提交、最新评判、候选人表现和已有决策数据。
- 保留现有人工评分和最终决策写接口语义，不改变算法题、单选题、多选题自动评判规则。
- 新增“发布本轮结果”前端入口，但本次不实现邮件发送后端逻辑；按钮应以待接入状态呈现或只完成界面占位。

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `assessment-judgement`: 扩展考核评判管理端需求，覆盖评分工作台、人员视图、录用决策工作台、聚合查询接口和发布结果入口。

## Impact

- 后端：
  - `AdminAssessmentJudgementController` 及其 application service 需要新增聚合查询接口。
  - `AssessmentJudgementRepository`、`AssessmentAnswerRepository`、`AssessmentDecisionRepository` 或对应 mapper 需要支持按 `assessmentTimeId`、`questionId`、`userId` 聚合查询最新提交、最新评判和最终决策。
  - 可能需要新增 DTO 用于评分题目列表、题目提交列表、考生评分矩阵、决策候选人列表和决策统计。
- 前端：
  - 新增或实现 `/admin/assessment/judge` 下的评分与录用决策页面。
  - 扩展 `adminAssessmentJudgementService` 和 `assessment.dto.ts` 类型定义。
  - 使用 AntD `Tabs`、`Table`、`Drawer`、`Form`、`InputNumber`、`Input.TextArea`、`Select`、`Card`、`Statistic`、`Tag`、`Empty`、`Spin` 等组件。
- 权限：
  - 评分查询与人工评分沿用 `assessment-judgement:list/query/manual-review` 相关权限。
  - 最终决策沿用 `assessment-decision:set`，只有方向管理员及以上可设置通过/淘汰。
- 不引入新的第三方依赖。
- 不修改已应用 Flyway 迁移；如需要数据库结构调整，必须新增迁移。本变更预期主要通过新增查询实现，不要求新增表。
