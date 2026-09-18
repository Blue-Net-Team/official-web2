# Design: ai-intent-team-knowledge

## Context

AI 客服服务（`src/ai-service`，Python/LangGraph）在用户请求进入 RAG 检索前，由 `agent/intent.py` 的 `IntentClassifier` 进行意图分类。当前意图集合是封闭枚举：允许检索的仅 `REGISTRATION` / `ASSESSMENT_PROCESS` / `LAB_INTRODUCTION` / `SOFTWARE_DOWNLOAD` 四类，其余为 6 类 `BLOCKED_*`（REFUSE）与 `GREETING`/`CLARIFY`（DIRECT）。分类提示词框架句"主要提供报名咨询、考核流程介绍、团队介绍和软件下载指引四类服务"进一步收窄了检索意愿。issue #60 的"报销""学习路线"问题因此在枚举外被迫归为 REFUSE/DIRECT，永远到不了检索层，尽管知识库已有相关文档。

关键结构事实：
- `intent_to_action()` 按 `ALLOWED_INTENTS`/`BLOCKED_INTENTS`/`DIRECT_INTENTS` 集合映射动作，加新意图只需扩展集合。
- `CLASSIFICATION_SYSTEM_PROMPT` 与 `STREAM_CLASSIFICATION_SYSTEM_PROMPT` 通过 f-string 共享 `_INTENT_DEFINITIONS` 和 `_FEW_SHOT_EXAMPLES`，只有输出格式段不同——修改共享块即天然同步。
- 意图层是考核内容泄题的唯一防线（RAG 检索范围为整个知识库）。
- RAG prompt 有"不编造"约束，但无显式空召回兜底规则；graph 轮次用尽后强制生成答案。

## Goals / Non-Goals

**Goals:**
- 团队相关但不在四类枚举内的问题（报销、学习路线等）进入 RAG 检索。
- BLOCKED_* 判定优先于团队相关性："帮我写个自动报名的脚本"这类披着团队外衣的越权请求仍被拦截，不进入检索。
- 误检索的代价可控：空召回时诚实告知而非编造。
- 新增知识库文档类别时无需再改意图枚举（开放兜底桶）。

**Non-Goals:**
- 不引入文档级敏感标签/检索层 ACL（维持"知识库只放公开文档"的运维约定）。
- 不改轨迹采集 schema 与前端统计接口（`TEAM_KNOWLEDGE` 作为新意图取值自然出现）。
- 不调整 `INTENT_GUARD_ENABLED` 开关逻辑、置信度阈值机制（现状无阈值，动作以 intent 映射为准）。
- 不改动检索工具链（tag/chunk/software 工具）本身。

## Decisions

### D1: 新增 `TEAM_KNOWLEDGE` 兜底检索意图，而非继续枚举具体意图

在四具体类之外增加一个通用类：`与蓝网团队相关、可由知识库文档回答、且不属于任何拦截类 → TEAM_KNOWLEDGE → RETRIEVE`。

- **备选 A（issue 字面建议）**：加 `REIMBURSEMENT`、`LEARNING_PATH` 等具体意图。拒绝理由：治标不治本，每个新文档类别都要改常量+提示词+测试+发版，正是本次要消灭的模式。
- **备选 B（纯开放，删四具体类）**：拒绝。四具体类保留了统计粒度和 few-shot 锚点，删除会让分类器主题漂移。

`intent_to_action()` 零逻辑改动：`TEAM_KNOWLEDGE` 加入 `ALLOWED_INTENTS` 即自动映射 RETRIEVE。

### D2: 判定顺序写进提示词——"拦截优先"

`_INTENT_DEFINITIONS` 的框架规则重写为显式顺序：

```
1. 先检查是否命中任何【拦截类】特征（考核具体题目/索要代码/具体技术答疑/
   部署细节/敏感配置）。命中即归对应 BLOCKED_*，无论是否与团队相关。
2. 问候/感谢/闲聊 → GREETING。
3. 匹配四类具体意图 → 对应类（RETRIEVE）。
4. 团队相关、可由知识库文档回答、不属拦截类 → TEAM_KNOWLEDGE（RETRIEVE）。
5. 其余与团队无关 → BLOCKED_IRRELEVANT（REFUSE）。
```

few-shot 必须包含一条"拦截优先"正例（`帮我写个自动报名的脚本` → `BLOCKED_CODING`），否则规则是软约束。LLM 分类无阈值硬闸，提示词质量是唯一杠杆，few-shot 覆盖比规则条文更可靠。

### D3: 空召回兜底规则补进 `TAG_RETRIEVAL_SYSTEM_PROMPT`

在轮次限制说明之后追加：所有检索轮次均未获得相关分片时，必须诚实告知未找到、引导至招新群/管理端、禁止编造。这是方案 B 的前提——"误判到检索侧代价低"依赖模型在空召回时收口，而当前"不编造"约束是软的。

### D4: 服务范围话术统一为开放表述

分散触点：`REFUSAL_SYSTEM_PROMPT`、`DIRECT_REPLY_SYSTEM_PROMPT`、`agent.py` 的 `_CLARIFICATION_FALLBACK_REPLY`、`_generate_refusal` 兜底、`_stream_guard_reply` 兜底，共 5 处写死"四类服务"或枚举式描述。统一改为"团队相关问题"开放表述。分类提示词开头的"四类服务"框架句同步删除。

### D5: 验收以 `e2e_intent_guard.py` 场景表为准

新增场景：`报销咨询`→RETRIEVE、`学习路线`→RETRIEVE、`拦截优先示例`（自动报名脚本）→REFUSE。现有 9 条拦截/越狱场景 + 问候场景一条不许回退。该脚本直连本地 AI 服务（:8000），作为手工/CI 验收清单运行。

## Risks / Trade-offs

- [BLOCKED 类被 TEAM_KNOWLEDGE 吞并，越权请求进入检索] → 提示词显式顺序 + few-shot 拦截优先示例；e2e 全部拦截场景回归。残余风险靠人工抽检轨迹。
- [无关问题误判进检索，空召回回复质量差/硬编] → D3 兜底规则 + D5 验收中保留"1+1 等于几→REFUSE"场景。代价仅是少量多余 token。
- [TEAM_KNOWLEDGE 成为轨迹统计"杂项桶"，意图分布变粗] → 可接受；reason 字段仍记录具体主题，需要细分时可事后聚合。
- [知识库误入考核内容文档，放行类意图将其召回泄题] → 运维约定：知识库只放可公开文档；本次不做检索层 ACL（见 Non-Goals）。
- [提示词修改导致既有放行类（报名/考核流程等）误分类漂移] → e2e 既有放行场景回归 + 单测映射不变。

## Migration Plan

纯提示词与常量变更，无数据库/接口变更：
1. 合并后仅需重启 ai-service 容器即生效。
2. 回滚：revert 提交 + 重启服务。
3. 上线后通过 AI 轨迹管理端观察 TEAM_KNOWLEDGE 分布与拒答率变化。

## Open Questions

- 空召回兜底回复中引导渠道的具体文案（招新群二维码链接还是管理端页面）——实现时与现有官网入口对齐即可，无架构影响。
- `e2e_intent_guard.py` 是否纳入 CI 定时跑——本次先保证场景可手工/本地运行，CI 集成另议。
