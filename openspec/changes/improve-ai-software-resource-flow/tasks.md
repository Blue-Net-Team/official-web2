## 1. 测试先行（TDD 红灯）

- [ ] 1.1 重写 `src/ai-service/tests/tools/test_software_resource_search.py` 中的方向映射测试，覆盖 `software_resource_list` 与 `software_resource_lookup` 两个入口
- [ ] 1.2 新增 `software_resource_list` 测试：单方向返回索引字段（名称/分类/方向），且断言不含描述与下载地址
- [ ] 1.3 新增 `software_resource_list` 测试：自动翻页合并（mock 后端分页返回 2 页以上），断言条目数等于 `totalElements`
- [ ] 1.4 新增 `software_resource_list` 测试：超过内部总量上限时返回截断说明与未展示数量
- [ ] 1.5 新增 `software_resource_list` 测试：请求 URL 中 `direction` 按枚举透传、中文标签转换、未知方向不带 `direction`
- [ ] 1.6 新增 `software_resource_lookup` 测试：`names` 传入多个名称时一次调用返回多条完整资源（含 `externalUrl`）
- [ ] 1.7 新增 `software_resource_lookup` 测试：归一化精确匹配（`"keil uvision5"` 命中 `"Keil uVision5"`）
- [ ] 1.8 新增 `software_resource_lookup` 测试：别名命中（`"vscode"` → `"Visual Studio Code"`）与双向子串命中
- [ ] 1.9 新增 `software_resource_lookup` 测试：相似度匹配命中（拼写近似名）
- [ ] 1.10 新增 `software_resource_lookup` 测试：部分未命中时同时返回命中详情与未命中名单；全部未命中时返回明确提示
- [ ] 1.11 新增 `software_resource_lookup` 测试：`names` 为空返回参数错误提示而非抛异常；名称数超过单次上限时截断并说明
- [ ] 1.12 保留并适配降级测试：超时、HTTP 错误、未知异常、翻页中途失败均返回友好提示
- [ ] 1.13 新增测试：`ToolRegistry` 中存在 `software_resource_list` 与 `software_resource_lookup`，且不存在 `software_resource_search`

## 2. 工具层实现

- [ ] 2.1 在 `src/ai-service/tools/software_resource_search.py` 中抽出共用的 HTTP 客户端构建、URL 拼装、方向解析与格式化辅助函数
- [ ] 2.2 实现 `software_resource_list(direction=None)`：内部翻页取全量，应用总量上限，投影为 `name` / `category` / `direction`，返回 `totalElements` 与截断提示
- [ ] 2.3 实现 `software_resource_lookup(names, direction=None)`：获取候选集（指定方向或全量），对每个名称按 归一化精确 → 别名 → 双向子串 → 相似度 的顺序匹配
- [ ] 2.4 在工具模块内定义 `_ALIASES` 别名表，至少覆盖 `vscode` / `vs code` → `Visual Studio Code`、`ad` → `Altium Designer`、`vs` → `Visual Studio`
- [ ] 2.5 实现未命中名单的收集与格式化输出；实现名称数量上限与 `names` 为空的参数校验
- [ ] 2.6 统一两个工具的异常降级路径，复用现有"服务暂不可用 / 查询异常"话术

## 3. 工具注册与描述

- [ ] 3.1 在 `src/ai-service/tools/__init__.py` 中移除 `software_resource_search` 的注册，改为注册 `software_resource_list` 与 `software_resource_lookup`
- [ ] 3.2 为 `software_resource_list` 编写工具描述与参数定义：`direction` 可选，说明"省略 direction 可一次获取全部方向"
- [ ] 3.3 为 `software_resource_lookup` 编写工具描述与参数定义：`names` 为字符串数组且必填，`direction` 可选
- [ ] 3.4 更新 `software_resource_search` 相关的导出与 `__all__`，确保无残留引用

## 4. 提示词与对话流程

- [ ] 4.1 重写 `src/ai-service/agent/prompts.py` 中 `TAG_RETRIEVAL_SYSTEM_PROMPT` 的软件资源查询指引段落，写入两分支流程
- [ ] 4.2 在提示词中加入硬约束：用户未点名具体软件时禁止将方向词与用途词拼接为查询关键字，只传 `direction`
- [ ] 4.3 在提示词中加入合并规则：文档提到但资源库未收录 → 说明"暂未收录"；资源库有但文档未提到 → 不输出
- [ ] 4.4 在提示词中加入降级规则：未检索到软件清单文档时提供 `/resources?tab=<方向key>` 跳转链接
- [ ] 4.5 在提示词中保留并强化下载地址的 Markdown 超链接呈现要求
- [ ] 4.6 检查 `graph.py` 的预披露提示文本，确认宽泛软件类问题会被引导检索"各方向所需软件"相关分片

## 5. 状态图轮次上限

- [ ] 5.1 在 `src/ai-service/agent/graph.py` 的 `AgentState` 中新增 `software_list_rounds` 与 `software_lookup_rounds`
- [ ] 5.2 在 `tool_executor_node` 中新增两个工具的分支：`software_resource_list` 上限 1 轮、`software_resource_lookup` 上限 2 轮，超限返回提示文本
- [ ] 5.3 在 `src/ai-service/agent/agent.py` 的 `_build_initial_state` 中初始化上述两个计数字段
- [ ] 5.4 新增/扩展状态图测试，验证两种上限触发时返回提示且不执行工具、以及两个计数器互不影响

## 6. 验证

- [ ] 6.1 运行 `src/ai-service` 的单元测试，确认新增与既有测试全部通过
- [ ] 6.2 手动构造宽泛问题（"嵌入式方向需要什么软件"）走一次流式对话，确认工具调用序列为 `software_resource_list` → chunk 检索 → `software_resource_lookup`，且回答包含下载超链接
- [ ] 6.3 手动构造点名问题（"SolidWorks 在哪里下载"）走一次对话，确认直接调用 `software_resource_lookup` 且返回下载地址
- [ ] 6.4 构造文档提到但资源库缺失的软件，确认回答说明"暂未收录"且不编造链接
- [ ] 6.5 运行 `openspec validate improve-ai-software-resource-flow --strict` 确认变更文档合法
