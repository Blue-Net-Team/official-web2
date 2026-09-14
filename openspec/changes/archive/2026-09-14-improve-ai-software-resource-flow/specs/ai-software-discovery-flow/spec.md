## ADDED Requirements

### Requirement: 软件类问题分为点名与宽泛两条分支

系统 SHALL 在 `SOFTWARE_DOWNLOAD` 意图下区分两类问题：用户点名了具体软件（"SolidWorks 在哪下载"），或用户只给出方向/用途（"嵌入式方向需要什么软件"）。两类问题 SHALL 走不同的工具序列。

#### Scenario: 用户点名具体软件
- **WHEN** 用户输入 "SolidWorks 在哪里下载"
- **THEN** Agent SHALL 调用 `software_resource_lookup`，参数 `names` 包含 "SolidWorks"
- **AND** Agent SHALL NOT 先调用 `software_resource_list`

#### Scenario: 用户只给出方向
- **WHEN** 用户输入 "嵌入式方向需要什么软件"
- **THEN** Agent SHALL 先调用 `software_resource_list`，参数 `direction` 为嵌入式方向
- **AND** Agent SHALL 再检索知识库中"各方向所需软件"相关分片
- **AND** Agent SHALL 从检索到的分片正文中抽取软件名称
- **AND** Agent SHALL 调用 `software_resource_lookup` 并传入抽取出的软件名称列表

#### Scenario: 宽泛问题禁止编造关键字
- **WHEN** 用户输入为宽泛的方向类问题，且未点名任何具体软件
- **THEN** Agent SHALL NOT 将方向词与用途词拼接成查询关键字串
- **AND** `software_resource_list` 的调用 SHALL 只携带方向参数

#### Scenario: 跨方向对比
- **WHEN** 用户输入 "三个方向分别需要什么软件"
- **THEN** Agent SHALL 调用一次 `software_resource_list` 且省略 `direction`
- **AND** 返回的索引条目 SHALL 携带各自方向，供 Agent 分组呈现

### Requirement: 资源清单与下载链接的合并规则

Agent SHALL 以知识库文档作为"团队推荐软件"的依据，以软件资源库作为"下载地址"的来源，并按固定规则合并两者。

#### Scenario: 文档与资源库都有
- **WHEN** 某软件同时出现在知识库文档和资源库查询结果中
- **THEN** 回答 SHALL 包含该软件名称、说明，以及 Markdown 超链接格式的下载地址

#### Scenario: 文档提到但资源库未收录
- **WHEN** 某软件出现在知识库文档中，但 `software_resource_lookup` 将其上报为未命中
- **THEN** 回答 SHALL 说明该软件为团队推荐但资源库暂未收录
- **AND** 回答 SHALL NOT 编造下载地址

#### Scenario: 资源库有但文档未提到
- **WHEN** 某软件出现在资源库查询结果中，但未被知识库文档提及
- **THEN** 回答 SHALL NOT 输出该软件

### Requirement: 点名软件未命中时的写法引导

当用户点名的软件未在资源库中命中时，系统 SHALL 考虑用户可能使用了简称、别名或不同写法，并 SHALL 引导用户前往资源库页面自行查找，而不是仅回复"未收录"。

#### Scenario: 点名时提供多种写法候选
- **WHEN** 用户点名了一个无法确定资源库写法的软件
- **THEN** Agent SHALL 在同一次 `software_resource_lookup` 调用的 `names` 中传入多个候选写法（如中文名、英文名、简称、全称）
- **AND** Agent SHALL NOT 依赖工具内置的别名数据进行推断

#### Scenario: 点名软件未命中
- **WHEN** 用户点名了具体软件，但所有候选写法均被 `software_resource_lookup` 上报为未命中
- **THEN** 回答 SHALL 说明该软件在资源库中未找到
- **AND** 回答 SHALL 提示可能是尚未收录或名称写法不同
- **AND** 回答 SHALL 提供资源库页面链接引导用户自行查找
- **AND** 回答 SHALL NOT 编造下载地址

#### Scenario: 已知方向时给出对应标签页
- **WHEN** 未命中的点名问题中可推断出方向（如"嵌入式方向的 SolidWorks 在哪下载"）
- **THEN** 回答提供的链接 SHALL 为 `/resources?tab=<方向key>`
- **AND** `<方向key>` SHALL 取自 `general` / `computer_vision` / `structural_design` / `embedded`

#### Scenario: 未知方向时给出资源库首页
- **WHEN** 未命中的点名问题无法推断方向
- **THEN** 回答提供的链接 SHALL 为 `/resources`

#### Scenario: 点名时一并传入方向
- **WHEN** 用户点名软件且问题中包含方向信息
- **THEN** Agent SHALL 在 `software_resource_lookup` 调用中一并传入 `direction`

### Requirement: 知识库未召回软件清单时的降级

当 Agent 未能从知识库检索到任何"各方向所需软件"相关内容时，系统 SHALL 降级为提供资源库页面跳转链接，而不是回答"未找到"。

#### Scenario: 软件清单文档未召回
- **WHEN** 宽泛问题下 `software_resource_list` 已返回资源索引，但知识库检索未命中任何软件清单分片
- **THEN** 回答 SHALL 提供资源库页面链接 `/resources?tab=<方向key>`
- **AND** 回答 SHALL 说明可前往该页面查看完整资源清单

#### Scenario: 知识库检索完全失败
- **WHEN** chunk 检索工具抛出异常或无任何结果
- **THEN** Agent SHALL 仍提供资源库页面跳转链接作为兜底

### Requirement: 软件资源服务不可用时的降级

当软件资源后端不可用时，Agent SHALL 给出明确的服务不可用提示，并 SHALL NOT 编造资源名或下载地址。

#### Scenario: 资源索引查询失败
- **WHEN** `software_resource_list` 返回服务不可用提示
- **THEN** Agent SHALL 说明软件资源服务暂不可用
- **AND** Agent SHALL NOT 输出任何下载地址
