## Context

当前考核系统的"录用决策"（`AssessmentDecision`）仅记录通过/淘汰结果，没有利用这些结果限制考生后续行为。Issue #23 报告：第一轮被淘汰的考生仍能看到第二轮考核卡片并进入答题。

同时，后端 `AssessmentQuestionAppServiceImpl` 对 `CANDIDATE` 角色在考核结束后抛出 `SecurityException("考核已结束")`，导致考生无法查看已结束考核的题目和成员评论，与前端行为（`isExpired` 仅禁用提交按钮，仍展示页面）不一致。

## Goals / Non-Goals

**Goals:**
- 淘汰决策后立即限制 CANDIDATE 参与后续轮次考核（列表过滤 + 接口拦截）
- 7 天后自动禁用被淘汰考生账号
- 修复考核结束后 CANDIDATE 无法查看题目和评论的问题

**Non-Goals:**
- 不修改淘汰决策的录入流程
- 不新增数据库表或字段（复用现有 `tb_assessment_decision.decided_at` 作为淘汰时间）
- 不影响 MEMBER/DIRECTION_ADMIN/SUPER_ADMIN 角色的考核查询和答题
- 不新增前端 UI（纯后端行为变更）

## Decisions

### Decision 1: SQL 层 + 应用层双重检查
- **选择**: 考核列表在 SQL 层过滤（`NOT EXISTS` 子查询），单个资源访问在 AppService 层校验
- **理由**: SQL 过滤保证分页正确，AppService 校验防止用户通过直接 URL 绕过列表（如直接访问 `/assessment/{timeId}/questions`）

### Decision 2: 使用 `tb_assessment_decision.decided_at` 作为淘汰时间
- **选择**: 不新增字段，复用 `decided_at`
- **理由**: `decided_at` 在创建/更新决策时自动设置，语义上就是决策生效时间。7 天缓冲期从该时间点起算
- **替代方案**: 在 `tb_user` 新增 `eliminated_at` 字段 —  rejected，避免过度设计

### Decision 3: 淘汰决策可逆
- **选择**: 当 `passed` 从 `false` 改为 `true` 时，限制立即解除
- **理由**: 管理员可能误操作或后续改变决策。SQL `NOT EXISTS` 天然支持（只检查 `passed = false`）

### Decision 4: 考核结束后的时间拦截移除
- **选择**: 移除 `listQuestionsForUser` 和 `getQuestionDetailForUser` 中对 CANDIDATE 的 `"考核已结束"` 抛异常
- **理由**: 前端已有 `isExpired` 状态控制，答题提交时 `validateTimeNotEnded` 也会拦截。查询层拦截阻塞了"查看评论"的合理需求
- **风险**: CANDIDATE 考核结束后可以看到题目内容（包括选择题选项、算法题 starter code）— 但这是合理的，考核已结束且结果已发布

### Decision 5: 固定 7 天，不支持配置
- **选择**: 硬编码 7 天
- **理由**: 用户明确"暂不支持配置"，避免引入配置项增加复杂度

## Risks / Trade-offs

- [Risk] SQL `NOT EXISTS` 子查询增加列表查询复杂度 → Mitigation: `tb_assessment_decision` 有 `(assessment_time_id, user_id)` 索引，且该查询只在用户端列表调用
- [Risk] 定时任务在高并发下批量更新 `tb_user.disable` 可能有锁竞争 → Mitigation: 每次只更新少量记录（每天淘汰人数有限），且 `disable` 字段无唯一约束
- [Risk] 已签发 JWT token 在账号禁用后仍然有效直到过期 → Mitigation: 当前系统已有此行为（`AuthDomainServiceImpl` 只在登录时检查 `disable`）。如需立即失效，需引入 token 黑名单机制，超出本次范围
- [Risk] 考核结束后移除时间拦截，CANDIDATE 可以看到题目内容（包括选择题答案配置）→ Mitigation: 题目内容中的 `correctAnswer` 等敏感字段在 `sanitizeQuestionForUser` 中已过滤，不影响公平性

## Migration Plan

- 无需数据库迁移（不新增表/字段）
- 部署后新做的淘汰决策立即生效
- 历史已淘汰记录：从 `decided_at` 起算，如果已超过 7 天，下次定时任务执行时会自动禁用

## Open Questions

- 无
