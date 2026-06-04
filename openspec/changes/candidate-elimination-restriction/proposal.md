## Why

Issue #23 反馈第一轮被淘汰的考生仍能看到并参与第二轮考核，录用决策（`AssessmentDecision.passed = false`）没有实际限制考生的后续权限。同时，当前系统存在前后端不一致：后端在考核结束后拦截 CANDIDATE 查看题目详情，导致考生无法查看已完成考核的成员评论。

## What Changes

- **新增淘汰后 7 天限制机制**：管理员做淘汰决策后，CANDIDATE 角色考生立即被限制参与后续轮次考核（无法看到后续考核卡片、无法进入考题列表、无法答题），但已参加考核的评论查看不受影响
- **新增定时禁用任务**：7 天后自动将淘汰考生账号禁用（`tb_user.disable = true`）
- **修复考核结束后查看评论**：移除 `listQuestionsForUser` 和 `getQuestionDetailForUser` 中对 CANDIDATE 的"考核已结束"拦截，让考生考核结束后仍能查看题目和评论（前端已有 `isExpired` 控制 + 后端答题提交有 `validateTimeNotEnded` 保护）

## Capabilities

### New Capabilities
- `candidate-elimination-restriction`: 考核淘汰后考生的权限限制与 7 天后自动禁用账号

### Modified Capabilities
- `my-assessments-query`: 考生查询考核列表时需过滤掉被淘汰后的后续轮次
- `assessment-decision-publish`: 淘汰决策（`passed = false`）后需立即触发权限限制生效

## Impact

- **后端**：`AssessmentTimeMapper.xml`、`AssessmentQuestionAppServiceImpl`、`AssessmentAnswerAppServiceImpl`、`AssessmentDecisionRepository`、新增 `EliminatedUserDisableJob`
- **数据库**：使用现有 `tb_assessment_decision` 和 `tb_user` 表，不新增表
- **前端**：无需修改，后端直接返回 403/过滤列表
