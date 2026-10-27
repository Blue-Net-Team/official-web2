## Context

管理端算法题的判题配置通过 `GET /api/v1/admin/judge/questions/{questionId}/config` 一次性读取，响应体为 `JudgeProblemConfigDTO`，包含 generator、标准解（含 benchmark 结果 `p95TimeMs` / `maxTimeMs` / `peakMemoryKb` / `suggestedTimeLimitMs`）与测试用例生成配置。

但"正式资源限制"是另一张表 `tb_judge_language_limit`（`question_id` + `language` 唯一，字段 `time_limit_ms` / `memory_limit_kb` / `output_limit_kb` / `confirmed` / `confirmed_at` / `source_config_id`），它在管理端目前**只有写入路径**：

- 仓储接口 `JudgeLanguageLimitRepository` 仅暴露 `existsConfirmedByQuestionIdAndLanguage(...)` 与 `upsertConfirmedLimit(...)`；
- Mapper 仅有 `countConfirmedByQuestionIdAndLanguage` 与 `upsertConfirmedLimit`（`ON CONFLICT (question_id, language) DO UPDATE`）；
- `JudgeProblemConfigResult` / `JudgeProblemConfigDTO` 均无限制字段；
- 前端 `QuestionDrawer` 的「资源限制」页签只渲染 benchmark 表格 + 一张默认值全空的确认表单（`confirmLanguage: 'python'`，三个数值 `null`）。

表的两处真实读者都在管理端之外：候选人提交准入 `AlgorithmJudgeAppServiceImpl`（`starterCode` 含该语言 **且** `existsConfirmed...` 为真）与 judge-service `FormalJudgeWorkflow`（无已确认限制则标记人工复核）。

技术约束：判题链路不能改；接口响应字段只增不改；不新增数据库迁移；`@RequiresPermission` 沿用现有 `judge-problem-config:read`。

## Goals / Non-Goals

**Goals:**

- 管理端读取判题配置时，一并获得该题每种语言已持久化的资源限制（数值 + 确认状态 + 确认时间）。
- 资源限制页签可直观看出"哪些语言已确认、确认成什么值"，确认表单能回填既有值，未确认语言以 benchmark 建议限时预填。
- 确认成功后页签数值即时刷新，无需关闭重开抽屉。
- 读取按语言稳定排序，保证渲染与测试可预期。

**Non-Goals:**

- 不提供撤销/删除已确认限制的能力（当前唯一"禁止提交"的手段是从 `starterCode` 移除语言；该产品语义另行评估）。
- 不做乐观锁、版本号或确认变更的差异审计（项目已有全局 `AuditAspect` 记录请求）。
- 不新增独立 `GET .../language-limits` 接口。
- 不改动 `tb_judge_language_limit` 表结构，不清理题面 JSONB 中历史遗留的 `AlgorithmContent.timeLimit` / `memoryLimit` 字段。
- 不改动候选人端展示与 judge-service 读取逻辑。

## Decisions

### 决策 1：回显数据随现有 config 响应返回，而非新增独立接口

`JudgeProblemConfigDTO` 新增 `List<JudgeLanguageLimitDTO> languageLimits`。

理由：资源限制的"可确认性"完全依赖同一响应里的 `suggestedTimeLimitMs`——确认动作本质是"参考建议值 → 定值 → 落库"的三步闭环。拆成两个接口会让前端必须自己把 `standardSolutions` 与 `languageLimits` 按语言 join，而"已确认值 vs 建议值"的对比正是本页签的核心信息。同时前端 `refreshJudgeConfig` / 打开抽屉的加载逻辑已经统一走 `adminJudgeProblemConfigService.get`，字段内联后无需改动请求编排。

替代方案（已否决）：`GET .../config/language-limits` 独立接口——职责更干净、可单语言刷新，但增加一次请求与前端 join 成本，且本页签不存在独立刷新的需求。

### 决策 2：按 `question_id` 读取，不按 `source_config_id`

`tb_judge_language_limit.source_config_id` 指向 `tb_judge_problem_config.id`，但行本身以 `question_id` + `language` 为唯一键，且题目与当前判题配置是一对一（`uk_judge_problem_config_question`）。因此查询条件只用 `question_id`，避免"配置被原地更新但限制行仍指向旧 config"这类实现细节渗入读模型。

排序：`ORDER BY language ASC`，与前端表格行序一致，同时让集成测试断言稳定。

### 决策 3：仓储返回 `List<JudgeLanguageLimit>`（全量行），`confirmed` 原样透出

虽然现有唯一写入路径（`confirmLanguageLimit`）总是写 `confirmed = TRUE`，仍按表语义返回 `confirmed` / `confirmedAt` 并透出到 DTO，让前端渲染"已确认 / 待确认"标签而不必假设"有行即已确认"。这样后续若引入"取消确认"或"生成时写入待确认草稿"，读模型无需再改。

层间约定遵循项目规范：Mapper 返回 DO 列表 → RepositoryImpl 转换后返回领域实体列表 → Application 组装进 `JudgeProblemConfigResult` → Converter 转 DTO。

### 决策 4：前端以"已确认值优先，建议值兜底"回填确认表单

- 资源限制页签新增一张已确认限制表格（语言 / 时间 / 内存 / 输出 / 确认状态 / 确认时间），数据源 `judgeConfig.languageLimits`。
- 确认表单的 `confirmLanguage` 下拉选项仍取 `standardSolutions` 的语言集合（与 benchmark 表格同源），默认值改为该题标准解的第一个语言，不再硬编码 `'python'`。
- 语言切换时用 `form.setFieldsValue` 回填：若该语言存在已确认限制，填其 `timeLimitMs` / `memoryLimitKb` / `outputLimitKb`；否则填 `suggestedTimeLimitMs`、`peakMemoryKb`（缺省时回退到宽松默认 5000 / 262144 / 1024），让"确认"退化为一次确认而非从零手抄。
- 抽屉打开与 `refreshJudgeConfig` 完成后都触发上述回填。

替代方案（已否决）：保持单表单、仅在旁边显示只读列表——仍会让人手抄，且表单默认值与列表值可能不一致。

### 决策 5：不改动 `AdminJudgeProblemConfigService` 的写入路径与状态机

`confirmLanguageLimit` 内部仍执行 `upsertConfirmedLimit` + `markReadyIfGenerated`；`findByQuestionId` 仅增加一个查询并在组装时附带。确认成功后前端调用已有的 `refreshJudgeConfig(questionId)` 即可拿到新值，不需要新增接口。

## Risks / Trade-offs

- [`GET .../config` 每次多一次 SELECT] → 单题维度的查询，命中 `(question_id, language)` 唯一索引，且仅在管理端抽屉打开/刷新时触发；不引入 N+1（一次查全量行后按语言分组）。
- [DTO 字段新增导致前端类型与后端不同步] → 字段可选（前端类型声明为 `languageLimits?: JudgeLanguageLimitDTO[]`），旧前端不受影响；同时补齐前端 DTO 类型。
- [上限一致性风险：行的语言集合可能包含已从 `starterCode`/`standardSolutions` 移除的"孤儿"语言] → UI 对不在当前标准解集合中的限制行显示为"历史遗留"样式提示，仅展示不参与确认下拉；提交准入仍由后端双条件把关，孤儿行不影响判题。
- [回填逻辑覆盖用户正在编辑的值] → 仅在抽屉打开、语言切换、确认成功刷新三个时机回填；不影响用户在其他字段的输入。
- [建议值缺失导致预填为空] → 显式回退到宽松默认值（5000ms / 262144KB / 1024KB），与 `FormalJudgeWorkflow` 的 RUN 类型默认限制保持一致口径。

## Migration Plan

1. 后端先发布（响应仅新增字段，向后兼容），前端随后发布。
2. 无数据库迁移；回滚只需回退前后端代码，已存在的 `tb_judge_language_limit` 数据无需处理。
3. 验证：以已有确认记录的算法题打开编辑抽屉，确认页签显示既有数值；确认新语言后刷新即见新值。

## Open Questions

- 无开放问题。撤销/删除已确认限制、以及"禁止某语言提交但保留模板"的产品语义，已明确排除在本次范围之外，留待单独评估。
