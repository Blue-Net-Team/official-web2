## ADDED Requirements

### Requirement: 算法题 AI 草稿入口
系统 SHALL 在算法题新增和编辑模式的「判题配置」页签提供单按钮 AI 草稿入口，且 MUST NOT 要求打开配置 Modal。

#### Scenario: 编辑算法题时显示入口
- **WHEN** 出题人新增或编辑算法题并打开「判题配置」页签
- **THEN** 系统显示 AI 补全按钮，点击后直接基于当前表单生成草稿

#### Scenario: 非算法题或查看模式
- **WHEN** 用户查看非算法题或以只读模式打开考题
- **THEN** 系统不显示可用的 AI 草稿入口

### Requirement: 使用当前未保存题面生成
系统 SHALL 使用当前表单中的题干、输入说明、输出说明和数据范围生成草稿，并 SHALL 支持尚未保存且没有题目 ID 的新题。

#### Scenario: 新题直接生成
- **WHEN** 出题人在新增算法题表单中完整填写四类题面字段但尚未保存题目
- **THEN** 系统无需先创建题目即可请求并返回 AI 草稿

#### Scenario: 使用未保存修改
- **WHEN** 出题人修改已有题目的题面后尚未保存并点击 AI 补全
- **THEN** 请求使用修改后的表单值而非数据库中的旧题面

#### Scenario: 题面信息不完整
- **WHEN** 题干、输入说明、输出说明或数据范围任一为空时点击 AI 补全
- **THEN** 系统指出缺失字段且不发送生成请求

### Requirement: AI 草稿内容边界
AI 草稿 SHALL 仅包含书面用例、默认运行用例、Python Generator 和正式用例配置，且 MUST NOT 生成或修改标准解及其他人工配置。

#### Scenario: 成功返回完整草稿
- **WHEN** AI Service 接受合法题面并完成生成
- **THEN** 响应包含 2～3 个书面用例、2～4 个默认运行用例、Python Generator 源码和 6～10 个正式用例配置

#### Scenario: 保留人工配置
- **WHEN** 前端应用 AI 草稿
- **THEN** 标准解源码、主标准解语言、语言模板、Benchmark 参数、资源限制及非目标题面字段保持原值

### Requirement: 书面用例与默认运行用例
AI 生成的书面用例和默认运行用例 SHALL 符合现有算法题内容结构，书面用例用于解释题意，默认运行用例 SHALL 使用适合即时运行的小规模数据。

#### Scenario: 回填题面页签
- **WHEN** 前端应用生成结果
- **THEN** 书面用例写入 `examples`，默认运行用例写入 `runTestCases`，并可在「题面信息」页签继续编辑

#### Scenario: 样例字段保持类型
- **WHEN** 书面用例包含输入、期望输出或说明
- **THEN** 输入和期望输出保持纯文本，说明保持可选文本，不改变现有保存契约

### Requirement: Generator 与正式用例配置一致性
AI 生成的 Generator SHALL 从 stdin 读取单个 JSON 参数对象并仅向 stdout 输出测试输入，正式用例参数 SHALL 使用该 Generator 识别的字段。

#### Scenario: 正式用例参数结构
- **WHEN** 草稿包含正式用例配置
- **THEN** 每个 `generatorArgs` 都是 JSON 对象，`caseNo` 从 1 开始连续递增，分类属于系统支持的测试用例分类

#### Scenario: 正式用例默认属性
- **WHEN** 草稿包含正式用例配置
- **THEN** 每个用例权重为 1、`hidden` 为 true、`sample` 为 false，并包含说明其覆盖场景的描述

#### Scenario: 可复现随机数据
- **WHEN** Generator 生成随机测试数据
- **THEN** 对应用例参数包含固定随机种子，使相同参数重跑得到相同输入

#### Scenario: 用例覆盖
- **WHEN** 草稿生成成功
- **THEN** 正式用例集合覆盖题目适用的最小、边界、常规、随机和最大规模场景，并包含可从题面识别的特殊或最坏情况

### Requirement: 原子回填且不持久化
前端 SHALL 在校验完整响应后一次性应用全部目标字段，且 MUST NOT 因 AI 生成而自动保存题目、保存判题配置或发布测试数据生成任务。

#### Scenario: 应用完整草稿
- **WHEN** 完整草稿通过响应校验
- **THEN** 前端一次性更新四类目标内容并提示生成数量，保留当前页签供出题人检查

#### Scenario: 后续保存
- **WHEN** 出题人检查草稿后点击现有保存或保存并生成测试数据按钮
- **THEN** 系统沿用既有题目保存、判题配置保存和测试数据生成流程

### Requirement: 重复生成覆盖确认
当任一 AI 目标字段已有内容时，系统 SHALL 在再次生成前使用轻量确认说明覆盖范围。

#### Scenario: 取消覆盖
- **WHEN** 目标字段已有内容且出题人取消覆盖确认
- **THEN** 系统不发送请求且所有字段保持原值

#### Scenario: 确认覆盖
- **WHEN** 出题人确认覆盖并生成成功
- **THEN** 新草稿仅替换书面用例、默认运行用例、Generator 和正式用例配置

### Requirement: 前端直连独立 AI 链路
Frontend SHALL 与现有 AI 客服一致直接调用专用 AI Service 草稿路由，且 MUST NOT 通过 Backend 转发或复用知识库聊天与意图围栏。

#### Scenario: 出题人生成草稿
- **WHEN** 出题人提交合法题面
- **THEN** Frontend 直接调用 `POST /ai/v1/judge-config-drafts` 并接收结构化 JSON 草稿

#### Scenario: 应用草稿
- **WHEN** AI Service 返回草稿
- **THEN** Frontend 校验后直接映射到表单，不经过 Backend DTO 转换或二次响应包装

### Requirement: 生成状态与失败保护
系统 SHALL 在生成期间防止重复提交，并 SHALL 在失败、超时或响应无效时保留现有表单内容。

#### Scenario: 生成进行中
- **WHEN** 一次生成请求尚未完成
- **THEN** AI 按钮显示加载状态且不能再次发起请求

#### Scenario: 上游失败
- **WHEN** AI Service 不可用、请求超时或返回无法通过校验的结果
- **THEN** 系统显示明确错误信息，所有目标字段保持请求前的值

#### Scenario: 部分响应无效
- **WHEN** 响应仅缺少任一目标部分或用例编号、分类、参数类型不符合契约
- **THEN** 系统拒绝整份草稿且不应用部分字段
