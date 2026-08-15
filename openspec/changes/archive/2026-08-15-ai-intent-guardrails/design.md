## Context

当前 `src/ai-service` 的 `RagAgent` 对所有用户消息都直接进入 LangGraph 检索流程：`pre_disclose` 生成标签、向量检索、LLM 决策、工具执行、最终生成。该流程没有运行时过滤，导致考核具体内容、代码实现、技术答疑、部署细节、密钥等请求也会被检索并可能得到回答。

本次变更在 `RagAgent` 入口增加一层**意图识别与安全围栏**：只有与蓝网团队客服目标明确相关的请求才进入 RAG；其余请求直接由 LLM 生成拒绝或简短回复。

## Goals / Non-Goals

**Goals：**
- 为 AI 客服增加运行时的意图分类能力。
- 明确放行四类请求：报名、考核流程、实验室介绍、软件下载。
- 明确拦截：考核具体内容、代码编写、技术答疑、部署细节、密钥/安全、无关内容。
- 问候/闲聊类请求直接简短回复，不检索。
- 使用结构化输出（JSON / Pydantic）保证分类结果可解析。
- 分类失败时按"体验优先"策略降级，避免误伤正常用户。
- 分类模型可独立配置，便于使用更轻量或更快的模型。

**Non-Goals：**
- 不修改现有 LangGraph 内部节点逻辑（`pre_disclose`、`agent`、`tool_executor`）。
- 不新增第三方依赖（复用现有 `langchain-openai`）。
- 不在前端的 UI 或 API 协议层面做改动（请求/响应字段不变）。
- 不实现持久化的分类模型训练或微调；prompt + few-shot 工程足够支撑首次上线。

## Decisions

### 1. 闸门放在 `RagAgent` 入口，而非 LangGraph 内部节点
- **选择**：在 `RagAgent.chat()` / `chat_stream()` 调用 Graph 之前先进行意图分类。
- **理由**：被拒绝请求无需初始化 Graph、checkpointer、embedding 或任何检索工具，延迟和成本最低；API 层保持薄，逻辑内聚在 Agent 层。
- **替代方案**：作为 LangGraph 第一个节点。该方案把判断过程纳入状态，便于观测，但 Graph 已编译、状态已初始化，短circuit 不够自然。当前选择更直接。

### 2. 纯 LLM 分类器，而非规则 + LLM 混合
- **选择**：完全由 LLM 根据 system prompt + few-shot 示例输出结构化意图。
- **理由**：规则维护成本高，且"考核流程"与"考核内容"、"软件下载"与"软件怎么用"等边界很难用规则精确区分；LLM 对语义更鲁棒。
- **替代方案**：关键词预过滤 + LLM 兜底。关键词能快速拦截明显的安全词，但容易误伤（如"报名密码"中的"密码"），且会增加维护负担。

### 3. 使用 LangChain `with_structured_output` 而非 `instructor` / `outlines`
- **选择**：使用 `langchain-openai` 已提供的 `ChatOpenAI.with_structured_output(PydanticModel)`。
- **理由**：项目已依赖 `langchain-openai`，不引入新依赖；对 OpenAI 兼容端点（SiliconFlow / DeepSeek）可用 function calling 或 JSON mode 实现结构化输出。
- **替代方案**：`instructor` 更成熟、内置重试；`outlines` 提供约束解码保证。若后续线上发现 JSON 解析失败率高，可平滑迁移到 `instructor`。

### 4. 保留 `DIRECT` 类别
- **选择**：问候、感谢、再见等闲聊归入 `DIRECT`，直接生成简短回复。
- **理由**：提升体验、减少不必要的检索调用；避免把正常礼貌用语误判为"无关内容"而僵硬拒绝。
- **替代方案**：所有非检索请求都 `REFUSE`。会让客服显得冷淡，且把"你好"也拦截不合理。

### 5. 分类失败采用"体验优先"兜底
- **选择**：结构化输出解析失败时，返回一段询问用户澄清的话术，例如询问问题是否属于报名、考核流程、团队介绍或软件下载。
- **理由**：避免安全优先策略把正常咨询直接拒绝，影响转化率；同时不泄露任何知识库内容。
- **替代方案**：安全优先，失败即拒绝。更保守，但可能误伤。

### 6. 分类模型支持独立配置
- **选择**：新增 `TBD_RAG_INTENT_LLM_PROVIDER`、`TBD_RAG_INTENT_LLM_MODEL` 等环境变量；未配置时复用主 LLM。
- **理由**：分类任务对模型能力要求低于 RAG 主链路，可用更小更快更便宜的模型；也便于 A/B 测试不同模型效果。

### 7. 拒绝话术由 LLM 生成
- **选择**：根据分类结果（intent + reason）调用一次轻量 LLM 生成礼貌拒绝话术。
- **理由**：比固定模板更自然、可按类别微调语气；同时 prompt 严格约束其不得回答原问题。
- **替代方案**：固定模板。更稳定、零成本，但语气单一。

## Risks / Trade-offs

| Risk | Mitigation |
|---|---|
| 假阳性：把"考核流程"误判为"考核内容" | 在分类 prompt 中加入大量 few-shot 示例；上线后根据日志持续调优；对 `ASSESSMENT_PROCESS` 与 `BLOCKED_ASSESSMENT_CONTENT` 给出明确区分标准 |
| 额外 LLM 调用增加延迟 | 使用轻量模型；分类 prompt 简短；被拒绝请求跳过 RAG，整体链路可能更短 |
| 额外 LLM 调用增加成本 | 独立配置便宜模型；监控拦截比例；必要时对常见拦截类型加缓存 |
| Prompt injection / 越狱绕过围栏 | 分类 prompt 中加入指令完整性检查；系统 prompt 明确要求即使知道答案也不得回答被拦截问题；持续收集攻击样例 |
| 多轮上下文导致分类漂移 | 分类器只取最近 1-2 轮上下文作为辅助信号，当前消息权重最高 |
| 模型输出不稳定导致 JSON 解析失败 | 使用 `with_structured_output`；外层 try/except；失败时降级为澄清话术 |
| 新配置项遗漏导致线上使用默认主模型 | 配置项提供默认值（空=复用主模型）；部署文档明确列出新增环境变量 |

## Migration Plan

1. **代码变更**：
   - 新增 `agent/intent.py`。
   - 修改 `agent/agent.py`、`agent/prompts.py`、`agent/types.py`（如需要）。
   - 修改 `setting.py` 增加配置项。
2. **配置变更**：
   - 在 `docker/.env` 或部署环境增加 `TBD_RAG_INTENT_LLM_PROVIDER`、`TBD_RAG_INTENT_LLM_MODEL`（可选，留空则复用主模型）。
3. **测试**：
   - 本地单元测试覆盖 allowed / blocked / direct / 边界 / 解析失败场景。
   - 可选：小流量观察分类结果日志，人工抽检误判 case。
4. **部署**：
   - 重新构建 `bluenet-ai-service` 镜像。
   - 滚动更新 ai-service 容器。
   - 观察日志中 `intent` / `action` / `confidence` 分布。
5. **回滚**：
   - 若出现大量误判，可通过环境变量临时关闭（建议预留 `TBD_RAG_INTENT_GUARD_ENABLED` 开关，默认 true）。
   - 或回滚到上一版镜像。

## Open Questions

1. 是否需要预留 `TBD_RAG_INTENT_GUARD_ENABLED` 开关以便线上快速关闭？
2. 分类模型默认 temperature 设为 0 是否足够稳定，还是需要进一步调参？
3. 是否需要把分类日志接入到某个监控或告警（如拦截率突降/突升）？
4. 拒绝话术生成是否与分类使用同一个 LLM 实例，还是也允许独立配置？
5. 后续是否需要收集线上误分类样本，做 prompt few-shot 迭代流程？
