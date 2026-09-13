## MODIFIED Requirements

### Requirement: AI Service 提供软件资源查询工具
AI Service SHALL 向 `ToolRegistry` 注册两个职责分离的工具，供 `RagAgent` 在对话中调用：`software_resource_list`（按方向返回资源索引）与 `software_resource_lookup`（按软件名列表返回完整资源）。原有的 `software_resource_search` SHALL NOT 再注册。

#### Scenario: 工具被注册到注册表
- **WHEN** AI Service 启动并导入 tools 模块
- **THEN** `ToolRegistry` 中存在名为 `software_resource_list` 的工具定义
- **AND** `ToolRegistry` 中存在名为 `software_resource_lookup` 的工具定义
- **AND** `ToolRegistry` 中不存在名为 `software_resource_search` 的工具定义

#### Scenario: 按方向执行列表工具
- **WHEN** 调用 `ToolRegistry.execute("software_resource_list", direction="嵌入式开发")`
- **THEN** 返回该方向的资源索引文本

#### Scenario: 按名称执行查询工具
- **WHEN** 调用 `ToolRegistry.execute("software_resource_lookup", names=["Git"])`
- **THEN** 返回格式化后的完整资源文本，包含下载地址

### Requirement: 工具接受中文方向标签和后端枚举值
两个工具的 `direction` 参数 SHALL 同时接受中文方向标签（如"视觉方向"）和后端枚举值（如 `COMPUTER_VISION`）。无法识别时 SHALL 按无方向处理。

#### Scenario: 中文方向标签映射
- **WHEN** 调用工具参数 `direction="视觉方向"`
- **THEN** 向后端请求时转换为 `direction=COMPUTER_VISION`

#### Scenario: 枚举值直接透传
- **WHEN** 调用工具参数 `direction="STRUCTURAL_DESIGN"`
- **THEN** 向后端请求时保持 `direction=STRUCTURAL_DESIGN`

#### Scenario: 未知方向降级为全库搜索
- **WHEN** 调用工具参数 `direction="不明方向"`
- **THEN** 向后端请求时不传递 `direction` 参数

### Requirement: 工具输出包含下载地址
`software_resource_lookup` 返回的文本 SHALL 包含资源名称、方向、分类、描述和外部下载地址。`software_resource_list` SHALL NOT 返回描述与下载地址。

#### Scenario: 查询工具返回单个资源
- **WHEN** `software_resource_lookup` 查询到 1 个名为 "Git" 的通用资源，下载地址为 "https://git-scm.com"
- **THEN** 返回文本中 SHALL 包含 "Git"、"通用"、"https://git-scm.com"

#### Scenario: 列表工具仅返回索引字段
- **WHEN** `software_resource_list` 返回 "Keil uVision5"
- **THEN** 单项 SHALL 包含资源名称、分类与方向
- **AND** 单项 SHALL NOT 包含其描述与下载地址

### Requirement: 工具输出不截断地覆盖整个方向
`software_resource_list` SHALL 在工具内部自动翻页，直到取回该方向（含 `GENERAL`）的全部启用资源或达到内部总量上限，并 SHALL 返回 `totalElements` 供 Agent 判断结果是否完整。

#### Scenario: 资源数量超过单页上限
- **WHEN** 某方向启用资源数量超过后端单页上限（100）
- **THEN** 工具 SHALL 自动请求后续页并合并结果
- **AND** 返回的索引条目数 SHALL 等于资源总数

#### Scenario: 达到内部总量上限
- **WHEN** 资源总数超过工具内部总量上限
- **THEN** 返回文本 SHALL 显式说明结果被截断以及未展示的条目数量

#### Scenario: 方向查询包含通用资源
- **WHEN** 调用 `software_resource_list(direction="EMBEDDED")`
- **THEN** 结果 SHALL 包含方向为 `EMBEDDED` 与 `GENERAL` 的资源

### Requirement: 系统提示词引导 Agent 使用工具
`RagAgent` 的 system prompt SHALL 包含使用 `software_resource_list` 与 `software_resource_lookup` 的明确指引，并 SHALL 明确禁止在宽泛问题中编造查询关键字。

#### Scenario: 用户询问某方向需要什么软件
- **WHEN** 用户输入 "计算机视觉需要安装什么软件"
- **THEN** Agent 在生成最终答案前 SHALL 调用 `software_resource_list` 工具
- **AND** prompt 中 SHALL 存在"不要将方向词与用途词拼接为关键字"的约束

#### Scenario: 用户询问下载地址
- **WHEN** 用户输入 "SolidWorks 在哪里下载"
- **THEN** Agent SHALL 调用 `software_resource_lookup` 工具并在回答中包含下载地址

#### Scenario: 下载地址的呈现格式
- **WHEN** 工具返回了下载地址
- **THEN** Agent SHALL 使用 Markdown 超链接格式 `[资源名称](URL)` 呈现，禁止只输出裸链接

## ADDED Requirements

### Requirement: 按软件名列表批量查询
`software_resource_lookup` SHALL 接受软件名称列表，并在工具内部对每个名称完成查询，避免让 Agent 为每个软件各发起一次工具调用。

#### Scenario: 一次调用查询多个软件
- **WHEN** 调用 `software_resource_lookup(names=["Keil uVision5", "立创EDA"])`
- **THEN** 一次工具调用 SHALL 返回上述两个软件的完整资源文本

#### Scenario: 名称列表超过单项上限
- **WHEN** 传入的名称数量超过单次上限
- **THEN** 工具 SHALL 只处理上限内的名称
- **AND** 返回文本 SHALL 说明被忽略的名称

#### Scenario: names 为空
- **WHEN** 调用 `software_resource_lookup(names=[])`
- **THEN** 工具 SHALL 返回参数错误的提示文本，而不是抛出未处理异常

### Requirement: 工具对软件名做容错匹配
`software_resource_lookup` SHALL 在本地候选集上按 归一化精确匹配 → 双向子串匹配 → 相似度匹配 的顺序尝试命中，并 SHALL 上报未命中的名称。工具 SHALL NOT 内置软件别名数据；同一软件的不同写法 SHALL 由 Agent 在同一次调用的 `names` 中作为多个候选传入。

#### Scenario: 大小写与空白差异
- **WHEN** 传入名称 `"keil uvision5"`，资源库中为 `"Keil uVision5"`
- **THEN** 工具 SHALL 命中该资源

#### Scenario: 多写法候选命中
- **WHEN** 一次调用传入 `names=["vscode", "VS Code", "Visual Studio Code"]`，资源库中为 `"Visual Studio Code"`
- **THEN** 工具 SHALL 命中该资源
- **AND** 未命中的候选 SHALL 出现在未命中清单中

#### Scenario: 工具不推断别名
- **WHEN** 只传入 `names=["vscode"]`，而资源库中仅有 `"Visual Studio Code"`
- **THEN** 工具 SHALL NOT 依赖内置别名数据命中该资源
- **AND** 该名称 SHALL 被上报为未命中

#### Scenario: 未命中名称被显式上报
- **WHEN** 传入名称在资源库中不存在
- **THEN** 返回文本 SHALL 单独列出未命中的名称
- **AND** 已命中的资源 SHALL 正常返回

#### Scenario: 全部未命中
- **WHEN** 传入的所有名称均未命中
- **THEN** 返回文本 SHALL 列出全部未命中名称，并说明资源库中不存在这些软件

### Requirement: 工具对后端故障做降级处理
当后端 API 不可用时，工具 SHALL 返回友好提示，而不是抛出未处理异常。

#### Scenario: 后端超时
- **WHEN** 后端 API 在配置的超时时间内无响应
- **THEN** 工具返回 "软件资源服务暂不可用，请稍后重试" 或类似提示

#### Scenario: 列表工具翻页中途失败
- **WHEN** `software_resource_list` 在请求第 2 页时后端报错
- **THEN** 工具 SHALL NOT 抛出未处理异常
- **AND** 返回文本 SHALL 表明结果不完整
