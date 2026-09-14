> 说明：本变更为 AI Service 不新增单元测试，变更期间产生的测试文件已清理。验证依赖既有测试回归 + OpenSpec 严格校验 + 手动对话确认。

## 1. 工具层实现

- [x] 1.1 在 `src/ai-service/tools/software_resource_search.py` 中抽出共用的 HTTP 客户端构建、URL 拼装、方向解析与格式化辅助函数
- [x] 1.2 实现 `software_resource_list(direction=None)`：内部翻页取全量，应用总量上限，投影为 `name` / `category` / `direction`，返回 `totalElements` 与截断提示
- [x] 1.3 实现 `software_resource_lookup(names, direction=None)`：获取候选集（指定方向或全量），对每个名称按 归一化精确 → 双向子串 → 相似度 的顺序匹配
- [x] 1.4 工具内不内置别名表；同一软件的不同写法由 Agent 在同一次调用的 `names` 中作为多个候选传入
- [x] 1.5 实现未命中名单的收集与格式化输出；实现名称数量上限与 `names` 为空的参数校验
- [x] 1.6 统一两个工具的异常降级路径，复用现有"服务暂不可用 / 查询异常"话术

## 2. 工具注册与描述

- [x] 2.1 在 `src/ai-service/tools/__init__.py` 中移除 `software_resource_search` 的注册，改为注册 `software_resource_list` 与 `software_resource_lookup`
- [x] 2.2 为 `software_resource_list` 编写工具描述与参数定义：`direction` 可选，说明"省略 direction 可一次获取全部方向"
- [x] 2.3 为 `software_resource_lookup` 编写工具描述与参数定义：`names` 为字符串数组且必填，`direction` 可选，支持一次传入多个名称
- [x] 2.4 更新模块导出与 `__all__`，确保无 `software_resource_search` 残留引用

## 3. 提示词与对话流程

- [x] 3.1 重写 `src/ai-service/agent/prompts.py` 中 `TAG_RETRIEVAL_SYSTEM_PROMPT` 的软件资源查询指引段落，写入两分支流程
- [x] 3.2 加入硬约束：用户未点名具体软件时禁止将方向词与用途词拼接为查询关键字，只传 `direction`
- [x] 3.3 加入合并规则：文档提到但资源库未收录 → 说明"暂未收录"；资源库有但文档未提到 → 不输出
- [x] 3.4 加入降级规则：未检索到软件清单文档时提供 `/resources?tab=<方向key>` 跳转链接
- [x] 3.5 保留并强化下载地址的 Markdown 超链接呈现要求
- [x] 3.6 检查 `agent/graph.py` 的预披露提示文本，确认宽泛软件类问题会被引导检索"各方向所需软件"相关分片
- [x] 3.7 加入点名未命中的写法引导：工具不推断别名，要求一次传入多种写法候选；全部候选未命中时给出资源库页面链接（已知方向 `/resources?tab=<方向key>`，否则 `/resources`）

## 4. 状态图轮次上限

- [x] 4.1 在 `src/ai-service/agent/graph.py` 的 `AgentState` 中新增 `software_list_rounds` 与 `software_lookup_rounds`
- [x] 4.2 在 `tool_executor_node` 中新增两个工具的分支：`software_resource_list` 上限 1 轮、`software_resource_lookup` 上限 2 轮，超限返回提示文本
- [x] 4.3 在 `src/ai-service/agent/agent.py` 的 `_build_initial_state` 中初始化上述两个计数字段

## 5. 验证

- [x] 5.1 运行 `src/ai-service` 既有测试（intent / stream / intent guard）确认无回归
- [x] 5.2 运行 `ruff check` 确认工具层与 agent 层无静态检查问题
- [x] 5.3 手动构造宽泛问题（"嵌入式方向需要什么软件"）走一次流式对话，确认工具调用序列为 `software_resource_list` → chunk 检索 → `software_resource_lookup`，且回答包含下载超链接
- [x] 5.4 手动构造点名问题（"SolidWorks 在哪里下载"）走一次对话，确认直接调用 `software_resource_lookup` 且返回下载地址
- [x] 5.5 构造点名但未命中的软件，确认回答说明"未找到"并给出资源库页面链接，且不编造下载地址
- [x] 5.6 运行 `openspec validate improve-ai-software-resource-flow --strict` 确认变更文档合法
