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
