## ADDED Requirements

### Requirement: tag_generate 优先返回已有标签
`tag_generate` SHALL 在生成标签时优先从数据库已有标签中选择，只有用户查询明确不涉及已有标签时才生成新标签。

#### Scenario: 用户查询涉及已有标签
- **WHEN** 用户输入"蓝网怎么报名"
- **THEN** `tag_generate` 返回的标签列表中包含"招新"

#### Scenario: 用户查询不涉及已有标签
- **WHEN** 用户输入一个与所有已有标签无关的问题
- **THEN** `tag_generate` 可以返回不在标签库中的新标签

### Requirement: tag_search_detailed 淘汰噪声标签
`tag_search_detailed` SHALL 在返回前过滤掉低相关度标签，只保留与查询显著相关的标签。

#### Scenario: 高相关查询的噪声过滤
- **WHEN** 用户输入"蓝网参加过什么比赛"
- **THEN** 返回结果中包含"比赛"标签
- **AND** 返回结果中不包含"STM32"或"团队简介"等无关标签

#### Scenario: 至少保留一个结果
- **WHEN** 用户输入任意查询
- **THEN** 如果过滤后无标签满足阈值， SHALL 至少保留分数最高的一个标签

### Requirement: chunk_search_by_tags 提供检索诊断
`chunk_search_by_tags` SHALL 在返回结果中包含一段检索诊断信息，明确告知哪些标签在库中命中、哪些未命中、精确匹配召回数量。

#### Scenario: 标签均不在库
- **WHEN** 传入的标签均不在数据库标签库中
- **THEN** 返回的诊断信息中包含"标签均不在库中"或等效描述
- **AND** 诊断信息建议"使用 chunk_search 直接搜索"

#### Scenario: 部分标签在库
- **WHEN** 传入的标签部分在库、部分不在
- **THEN** 诊断信息明确列出"在库标签"和"不在库标签"

### Requirement: Agent 支持兜底语义搜索
RagAgent SHALL 在 system prompt 中包含兜底策略指导，当标签检索失效时能自主调用 `chunk_search` 进行直接语义搜索。

#### Scenario: 标签失效触发兜底
- **WHEN** `chunk_search_by_tags` 返回的诊断显示"标签均不在库中"
- **THEN** Agent SHALL 调用 `chunk_search(query)` 进行直接语义搜索
- **AND** 将兜底搜索结果纳入最终答案的素材

#### Scenario: 兜底轮次限制
- **WHEN** Agent 已调用过 `chunk_search` 进行兜底搜索
- **THEN** 不再重复调用兜底搜索
