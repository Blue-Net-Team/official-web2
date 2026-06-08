## Why

当前考核系统存在两套并行的"评论"机制：`AssessmentJudgement.comment`（评分评语）和独立的 `Comment` 表（团队评论）。方向管理员确认最终评分时需填写评语，但这条评语与 Comment 系统中的评论重复，造成概念混淆。应将评语统一纳入 Comment 系统，最终评分只保留分数，简化流程。

## What Changes

- **BREAKING** 后端：移除 `FinalizeScoreRequestDTO` / `FinalizeScoreCommand` 的 `comment` 字段，确认最终评分时不再接受或保存评语
- **BREAKING** 后端：移除 `FormalJudgeWorkflow` 中自动判题的评语写入
- 后端：删除 `AssessmentAnswerAppServiceImpl.filterMemberCommentsOnly()` 方法，考生端返回评论时不再过滤管理员评论
- 前端管理端：移除评分 Drawer 中"评语"表单输入框及提交逻辑
- 前端用户端：移除 `QuestionSidebar` 中 `answer.judgement.comment` 的显示块
- 后端：保留 `AssessmentJudgement.comment` 数据库字段兼容历史数据，不再写入新值

## Capabilities

### New Capabilities

（无）

### Modified Capabilities

- `admin-finalized-judgement`: 确认最终评分 API 不再接受 `comment` 参数；评语通过 Comment 系统表达
- `assessment-judgement`: 自动评判不再写入 `comment`；考生端查询答案时不再返回 `judgement.comment`
- `multi-reviewer-comment`: 考生端展示评论时取消管理员过滤，所有角色的评论均可见

## Impact

- **API 变更**：`POST /api/v1/admin/assessment-judgements/finalize` 请求体移除 `comment` 字段
- **前端类型**：`AssessmentJudgementDTO` 的 `comment` 字段将始终为 `null`
- **数据兼容**：历史数据中已有的 `tb_assessment_judgement.comment` 值保留，新记录不再写入
- **权限**：`AdminCommentController` 的 `@RequiresPermission` 已自然拦截 CANDIDATE 角色，无需额外变更
