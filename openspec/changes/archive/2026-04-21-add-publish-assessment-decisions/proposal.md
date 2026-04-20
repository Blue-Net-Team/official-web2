## Why

考核决策页面（`/admin/assessment/judge/decision`）已有"发布本轮结果"按钮，但点击后仅弹出占位提示，未接入后端。管理员完成所有考生的通过/淘汰决策后，需要一键向已决策考生发送邮件通知，告知考核方向、轮次及最终结果。

## What Changes

- 新增后端 API `POST /api/v1/admin/assessment-judgements/decisions/publish`，按 `assessmentTimeId` 向该轮全部已决策考生异步发送 HTML 邮件
- 邮件内容包含：考生姓名、考核方向、轮次、通过/淘汰结果
- 前端移除占位 `Modal.info()`，改为调用后端 API 并展示发送结果
- 不修改数据库，不新增表或字段

## Capabilities

### New Capabilities

- `assessment-decision-publish`: 考核决策邮件发布功能——按考核轮次向已决策考生异步发送结果通知邮件

### Modified Capabilities

- `assessment-judgement`: 新增决策发布 API 端点，扩展 `AssessmentJudgementService` 接口

## Impact

- **后端 API**：`AdminAssessmentJudgementController` 新增 publish 端点
- **应用层**：`AssessmentJudgementService` / `AssessmentJudgementServiceImpl` 新增 `publishDecisions` 方法
- **前端**：`admin-assessment-judgement.service` 新增 `publishDecisions` 调用；decision 页面和 score 页面更新发布按钮逻辑
- **依赖**：复用已有 `EmailSender.sendHtmlAsync` 基础设施，无新外部依赖
