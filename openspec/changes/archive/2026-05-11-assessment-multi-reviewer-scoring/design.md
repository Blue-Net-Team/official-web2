## Context

当前考核系统中，`tb_comment` 表已在数据库定义但没有任何业务代码支撑（有表无业务）。人工评分通过 `tb_assessment_judgement` 存储，设计上每题每考生仅保留最新一条记录（`latest` 语义），由 `ROW_NUMBER() OVER` 取 `display_rank=1` 展示。这种设计适合单人快速评分，但不支持多人协作评审。

现有发布流程由 `decideAssessment`（保存决策）+ `publishDecisions`（发邮件通知）组成，但缺乏“结果已发布”的状态标记，导致考生无法明确获知何时可以查看评审详情。

## Goals / Non-Goals

**Goals:**
- 实现团队成员（含方向管理员）对文件上传答案添加评论与参考评分。
- 同一用户针对同一答案只能评论一次。
- 方向管理员可查看所有评论，并手动确认该题的最终评分。
- 考生只有在方向管理员确认并发布后，才能查看评论和最终评分。
- 结果发布时发送邮件通知考生。

**Non-Goals:**
- 改造 `tb_assessment_judgement` 为多人记录存储（保持 `latest` 语义不变，最终评分作为单条权威记录）。
- 修改客观题（选择、算法）的自动评分机制。
- 淘汰考生的账号保留期功能（独立 feature）。
- 方向管理员修改或删除他人评论。

## Decisions

### Decision 1: `tb_comment` 独立存储参考意见，不替代 `tb_assessment_judgement`
- **Rationale**: `tb_assessment_judgement` 的 `latest` 语义和 scoreboard 查询逻辑改动面过大，会破坏现有决策和看板。`tb_comment` 表结构已满足基础需求（answer_id + user_id + content + score），只需补全业务链路即可。
- **Alternative**: 扩展 `tb_assessment_judgement` 存储多条评审记录。Rejected，因为会颠覆现有 `display_rank=1` 的看板设计和 `decideAssessment` 逻辑。

### Decision 2: 最终评分以 `ADMIN_FINALIZED` 来源写入 `tb_assessment_judgement`
- **Rationale**: 方向管理员确认的最终评分仍属于“该题该考生的权威评分”，应存储在 `tb_assessment_judgement` 中，与现有 `AUTO`、`MANUAL` 来源并列，便于 scoreboard 和决策流程直接消费。
- **Alternative**: 在 `tb_comment` 中增加 `is_final` 标记。Rejected，因为 scoreboard 和决策链路已强依赖 `tb_assessment_judgement`。

### Decision 3: `tb_assessment_time` 增加 `results_published_at` 控制可见性
- **Rationale**: 需要一个明确的时间点标记考生可见评论和评分。使用 `TIMESTAMP` 而非布尔值，便于扩展未来可能的定时发布需求，且与现有 `end_time` 等时间字段风格一致。
- **Alternative**: 在 `tb_assessment_decision` 上增加 `published_at`。Rejected，因为决策是按考生维度的，而发布是按考核场次维度的操作。

### Decision 4: 评论者可以修改或删除自己的评论，方向管理员无此权限
- **Rationale**: 评论者（包括团队成员和方向管理员本人）对自己的评论拥有完全控制权，可以修改内容/分数或删除；方向管理员只能查看他人评论，不能修改或删除，保证评审过程的公正性和严肃性。
- **Alternative**: 评论一旦提交不可修改或删除。Rejected，因为实际操作中评论者可能有笔误或需要补充说明。

### Decision 5: 前端在评分面板内嵌评论列表和最终评分表单
- **Rationale**: 管理端评分面板 (`/admin/assessment/judge/score`) 是方向管理员日常评审的核心页面，在其内部直接展示所有评论和最终评分确认入口，体验最连贯。
- **Alternative**: 独立评论页面。Rejected，会增加页面切换成本。

## Risks / Trade-offs

| Risk | Mitigation |
|------|------------|
| `tb_comment` 从零补全 Repository/Service/Controller 链路，工作量较大 | 利用 MyBatis-Plus BaseMapper 减少样板代码，关注领域行为封装 |
| 新增 `results_published_at` 后，考生端查询接口需增加过滤逻辑，可能遗漏 | 在 `AssessmentTime` 领域实体中增加 `isResultsPublished()` 方法，统一判断 |
| 方向管理员确认最终评分后，旧有的 `MANUAL` 评分记录仍存在于 `tb_assessment_judgement`，看板展示需明确区分 | scoreboard 查询继续取 `display_rank=1`，该条记录即为最终记录（无论是 MANUAL 还是 ADMIN_FINALIZED） |
| 多人同时评论同一答案可能产生竞态条件 | 在 `tb_comment` 上建立 `(answer_id, user_id)` 唯一索引，数据库层保证幂等 |

## Migration Plan

1. 执行 Flyway 迁移脚本：
   - `tb_assessment_time` 新增 `results_published_at TIMESTAMP`。
   - `tb_comment` 新增唯一索引 `UNIQUE (answer_id, user_id)`。
2. 后端代码按 Domain → Application → Infrastructure → API 顺序增量部署。
3. 前端同步更新评分面板。
4. 无需回滚策略（纯新增字段和接口，不影响旧数据）。

## Open Questions

- 评论列表是否需要分页？如果文件上传题评论数通常不超过 20 条，第一期可不分页。
- 最终评分确认时，是否必须要求方向管理员本人也先在 `tb_comment` 中留一条评论？（根据用户要求：是，方向管理员自己也应在 tb_comment 留一条个人评论）
