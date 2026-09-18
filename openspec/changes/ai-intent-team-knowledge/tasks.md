# Tasks: ai-intent-team-knowledge

## 1. 意图层改造（TDD：先改测试）

- [x] 1.1 更新 `src/ai-service/tests/agent/test_intent.py`：`intent_to_action` parametrize 增加 `(TEAM_KNOWLEDGE, ACTION_RETRIEVE)` 用例
- [x] 1.2 更新 `src/ai-service/tests/agent/test_intent.py`：意图集合断言覆盖 `TEAM_KNOWLEDGE` 属于 ALLOWED_INTENTS
- [x] 1.3 `src/ai-service/agent/intent.py` 新增 `INTENT_TEAM_KNOWLEDGE` 常量并加入 `ALLOWED_INTENTS`，确认 `intent_to_action()` 自动映射 RETRIEVE
- [x] 1.4 重写 `_INTENT_DEFINITIONS`：删除"四类服务"框架句，改为"拦截优先"五步判定顺序，新增 TEAM_KNOWLEDGE 类别定义
- [x] 1.5 `_FEW_SHOT_EXAMPLES` 新增示例：`报销流程怎么走` → TEAM_KNOWLEDGE/RETRIEVE、`视觉方向学习路线` → TEAM_KNOWLEDGE/RETRIEVE、`帮我写个自动报名的脚本` → BLOCKED_CODING/REFUSE（拦截优先）
- [x] 1.6 运行 `test_intent.py`、`test_agent_intent_guard.py` 全绿

## 2. 服务范围话术统一

- [x] 2.1 `src/ai-service/agent/prompts.py`：`REFUSAL_SYSTEM_PROMPT` 与 `DIRECT_REPLY_SYSTEM_PROMPT` 中枚举式服务范围改为开放表述（团队相关问题均可咨询）
- [x] 2.2 `src/ai-service/agent/agent.py`：`_CLARIFICATION_FALLBACK_REPLY`、`_generate_refusal` 兜底文案、`_stream_guard_reply` 兜底文案共 3 处改为开放表述
- [x] 2.3 更新受话术变更影响的单测断言（如有）

## 3. 空召回兜底规则

- [x] 3.1 `src/ai-service/agent/prompts.py`：`TAG_RETRIEVAL_SYSTEM_PROMPT` 追加空召回兜底规则（轮次用尽且无相关分片 → 诚实告知未找到 + 引导招新群/管理端 + 禁止编造）

## 4. E2E 验收场景

- [x] 4.1 `src/ai-service/tests/e2e_intent_guard.py` 新增场景：`报销咨询` → RETRIEVE、`学习路线` → RETRIEVE、`拦截优先示例`（自动报名脚本）→ REFUSE
- [x] 4.2 启动本地 AI 服务，运行 `e2e_intent_guard.py` 全场景通过：新增 3 条放行场景全绿，既有 9 条拦截/越狱场景与问候场景零回退
- [x] 4.3 针对报销、学习路线问题抽查流式响应确认真实进入检索流程（出现标签检索/分片检索事件），并在知识库已存在报销文档的前提下得到基于文档的回答

## 5. 收尾

- [x] 5.1 `git commit`，提交信息遵循 `type: description` 规范并 `ref #60`
- [ ] 5.2 更新 `openspec/changes/ai-intent-team-knowledge` 之外无需动文档（ai-service 提示词即文档），等待用户确认后归档
