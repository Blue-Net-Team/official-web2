# Proposal: ai-intent-team-knowledge

## Why

GitHub issue #60：用户向 AI 客服询问"报销相关问题"和"学习路线相关问题"时未进入检索状态，尽管知识库中已有报销相关文档。根因是意图分类的允许检索类别是封闭枚举（仅 REGISTRATION / ASSESSMENT_PROCESS / LAB_INTRODUCTION / SOFTWARE_DOWNLOAD 四类），团队相关但不在枚举内的问题被强制归入 BLOCKED_IRRELEVANT（REFUSE）或 CLARIFY（DIRECT），永远到不了检索层。分类提示词还以"主要提供四类服务"的框架句主动收窄了模型的检索意愿。每次知识库新增文档类别都要改代码发版，不可持续。

## What Changes

- 意图分类器新增 `TEAM_KNOWLEDGE` 意图：与蓝网团队相关、可由知识库文档回答、且不属于任何拦截类的问题（报销流程、学习路线、日常制度、活动安排等）归入此类并放行检索（RETRIEVE）。
- 分类判定顺序重构为"拦截优先"：先检查是否命中任何 BLOCKED_* 特征，命中即 REFUSE，无论问题是否与团队相关（如"帮我写个自动报名的脚本"仍归 BLOCKED_CODING）；再判问候；再匹配四类具体意图；最后落入 TEAM_KNOWLEDGE 或 BLOCKED_IRRELEVANT。
- 重写分类提示词框架：删除"主要提供四类服务"的收窄表述，改为"团队相关→检索 / 无关、敏感、越权→拦截"的开放表述；few-shot 补充报销、学习路线（RETRIEVE）与拦截优先（REFUSE）示例。
- 检索回答提示词补充空召回兜底规则：所有检索轮次均未获得相关分片时，必须诚实告知用户未找到相关信息并引导至招新群/管理端，禁止编造答案。
- 统一所有"服务范围"话术（拒绝话术、直接回复、兜底文案）为开放表述，不再写死四类服务。
- 测试：意图单测新增映射与场景；`e2e_intent_guard.py` 新增报销、学习路线放行场景与拦截优先场景，现有拦截/越狱场景一条不许回退。

## Capabilities

### New Capabilities

（无新增能力域）

### Modified Capabilities

- `ai-intent-recognition`: 意图集合从封闭枚举扩展为"四类具体意图 + TEAM_KNOWLEDGE 兜底检索类"；新增"拦截优先"判定顺序要求；分类提示词框架句与 few-shot 更新。
- `rag-retrieval`: 新增空召回兜底要求——检索轮次用尽仍未获得相关分片时，回答必须诚实说明未找到并给出引导，不得编造。

## Impact

- **代码**：`src/ai-service/agent/intent.py`（意图常量、`_INTENT_DEFINITIONS`、`_FEW_SHOT_EXAMPLES`、ALLOWED_INTENTS）、`src/ai-service/agent/prompts.py`（RAG 空召回兜底规则、REFUSAL/DIRECT 话术服务范围表述）、`src/ai-service/agent/agent.py`（3 处写死"四类服务"的兜底文案）。
- **测试**：`src/ai-service/tests/agent/test_intent.py`、`src/ai-service/tests/agent/test_agent_intent_guard.py`、`src/ai-service/tests/e2e_intent_guard.py`。
- **运行影响**：AI 客服服务范围的语义变化（更多团队相关问题进入检索，消耗检索 token）；AI 轨迹统计中意图分布将新增 TEAM_KNOWLEDGE 分组（既有统计接口与 schema 不变，仅取值增多）。
- **运维约束**：知识库仅存放可公开文档。意图层是考核内容泄题的唯一防线，RAG 检索范围为整个知识库；若知识库中存在考核具体题目文档，任何放行类意图都可能将其召回。
- **无 API / 数据库变更**，前后端接口契约不变。
