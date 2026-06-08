## Context

当前考核系统同时存在两套文本反馈机制：

1. `AssessmentJudgement.comment` — 评分评语，在 `finalizeScore` 时由方向管理员填写，随评判记录持久化
2. `Comment` 表 — 团队评论，由团队成员（含管理员）对答案发表，支持参考评分

这两套机制职责重叠，导致概念混淆。方向管理员确认最终评分时既要写 Comment 又要写评语，而考生端同时显示 `judgement.comment` 和 `comments` 列表，体验冗余。

## Goals / Non-Goals

**Goals:**
- 统一文本反馈入口：所有评语通过 Comment 系统表达
- 最终评分只保留分数，不再接受或保存评语文本
- 考生端查看评论时不再过滤管理员评论，所有评论均可见
- 自动判题不再写入评语

**Non-Goals:**
- 不删除数据库 `tb_assessment_judgement.comment` 字段（兼容历史数据）
- 不修改 Comment 系统的 CRUD 接口
- 不引入新的权限模型（依赖现有 `@RequiresPermission`）
- 不改写选择题/算法题的自动评判逻辑（仅移除评语写入）

## Decisions

### 1. 保留数据库字段，仅从流程中移除

**决策**：`tb_assessment_judgement.comment` 列保留，新记录不再写入。

**理由**：
- 历史数据中已有 comment 值，删除列会导致数据丢失
- 避免数据库迁移带来的部署风险
- 该字段不再被任何新代码读取，实际效果等同于废弃

**替代方案**：数据库迁移删除列。拒绝原因：收益有限，风险高于收益。

### 2. 直接删除 `filterMemberCommentsOnly` 方法

**决策**：`AssessmentAnswerAppServiceImpl.getMyAnswer()` 直接返回 `commentDomainService.listCommentsByAnswerId()` 的原始结果，不做任何角色过滤。

**理由**：
- 管理员的评论本身就是对考生的反馈，应当可见
- 简化代码，消除隐藏逻辑
- 权限控制已在接口层通过 `@RequiresPermission` 实现（CANDIDATE 无法调用评论 API）

**替代方案**：将过滤逻辑改为"仅过滤 CANDIDATE"。拒绝原因：CANDIDATE 本就没有评论权限，不会产生需要过滤的数据。

### 3. 同时清理自动判题评语

**决策**：`FormalJudgeWorkflow.buildJudgement()` 中移除 `.comment("自动判题完成，结果：...")`。

**理由**：
- 用户明确"自动判题不会有评语"
- 该评语信息价值低（`resultCode` 已单独展示）
- 保持一致性：所有评判来源都不写 comment

### 4. 前端同步移除 `judgement.comment` 显示

**决策**：用户端 `QuestionSidebar.tsx` 和管理端 `score/page.tsx` 同时清理。

**理由**：
- 后端不再返回有意义的 comment 值，前端显示无意义
- 避免用户端展示 null/空评语造成困惑
- 管理端移除输入框可防止管理员继续填写（虽然后端已拒绝）

## Risks / Trade-offs

| Risk | Mitigation |
|------|------------|
| 历史数据中的 comment 值不再被展示 | 可接受。旧评语信息价值有限，且 Comment 系统已提供替代反馈渠道 |
| 前端类型 `AssessmentJudgementDTO.comment` 仍存在但始终为 null | 可接受。后续可选清理前端类型定义 |
| 管理端评分页面 UI 变化需要用户适应 | 评分页面移除评语输入后，管理员通过 Comment 卡片表达意见，流程更自然 |

## Migration Plan

1. 部署后端：新代码不再写入 comment，不再过滤管理员评论
2. 部署前端：移除评语输入和显示
3. 无需数据迁移，无需回滚策略（旧数据保留，新行为幂等）
