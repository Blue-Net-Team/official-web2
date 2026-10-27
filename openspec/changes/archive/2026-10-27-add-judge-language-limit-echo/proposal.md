## Why

管理端「资源限制」页签目前是**只写不读**：管理员确认某语言的正式判题限制后，`tb_judge_language_limit` 已写入，但再次打开考题编辑面板时该页签是一张全空表单（`confirmLanguage` 硬编码 `'python'`，三个数字全为 `null`），既看不到哪些语言已确认，也看不到确认的具体数值。结果是管理员无法判断当前状态、只能凭记忆或去数据库核对，并且极易把某个语言的限制重复确认到错误语言上；benchmark 计算出的 `suggestedTimeLimitMs` 明明就在同一页面，却要人手抄写一遍。

## What Changes

- 管理端判题配置读取接口 `GET /api/v1/admin/judge/questions/{questionId}/config` 的响应新增 `languageLimits` 数组，随判题配置一次性返回该题已持久化的语言资源限制。
- 领域仓储与 Mapper 新增按 `questionId` 查询语言资源限制的能力（当前只有 `existsConfirmed...` 与 `upsertConfirmed`，无任何读取方法）。
- 管理端「资源限制」页签从"只写表单"改为"读 + 写"：展示每种语言的时间/内存/输出限制与确认状态，确认表单在选择语言后回填既有值（无既有值时预填 benchmark 建议限时），语言下拉默认选中该题标准解的第一个语言而非硬编码 `python`。
- 确认成功后刷新配置，页签中的已确认数值同步更新，不再需要关闭重开抽屉。
- 不新增数据库迁移：`tb_judge_language_limit` 表结构不变。
- 不改变判题链路：judge-service 仍自行读取该表，候选人提交准入规则（`starterCode` 含该语言 **且** 存在已确认限制）保持不变。

本次**不含**撤销/删除已确认限制、乐观锁与变更审计；相关语义留待单独的变更评估。

## Capabilities

### New Capabilities
- `judge-language-limit-admin`: 管理端语言资源限制的读取与回显——管理端可通过判题配置响应获得每语言已确认的时间/内存/输出限制与确认状态，并在资源限制页签中看到状态、回填既有值、以 benchmark 建议值作为未确认语言的预填依据。

### Modified Capabilities
<!-- 无：现有 judge-test-data-generation 只约束"持久化并用于正式判题"，本次仅新增管理端读取与展示，不改变既有需求行为 -->

## Impact

- **后端读取模型**：`JudgeLanguageLimitRepository`、`JudgeLanguageLimitMapper`(+XML)、`JudgeProblemConfigResult`、`JudgeProblemConfigDTO`、`JudgeProblemConfigResponseConverter`、`JudgeProblemConfigAdminServiceImpl.findByQuestionId`
- **前端**：`src/frontend/src/apis/schema/assessment.dto.ts`（`JudgeProblemConfigDTO` 及新增限制项类型）、`src/frontend/src/app/admin/assessment/question/QuestionDrawer.tsx`（资源限制页签渲染与回填）
- **接口**：`GET /api/v1/admin/judge/questions/{questionId}/config` 响应体**向后兼容地新增字段**；不新增接口、不新增权限标识
- **数据库**：无迁移，仅新增一条 `SELECT ... WHERE question_id = ?` 查询
- **不受影响**：judge-service、候选人端接口、判题准入与正式判题逻辑
