## ADDED Requirements

### Requirement: 公开接口支持按关键字搜索软件资源
后端 `/api/v1/software-resources` 接口 SHALL 支持通过 `keyword` 查询参数对已启用的软件资源进行不区分大小写的模糊搜索。

#### Scenario: 按名称关键字搜索
- **WHEN** 调用方请求 `GET /api/v1/software-resources?keyword=git`
- **THEN** 返回名称、分类或描述中包含 "git"（不区分大小写）的已启用资源

#### Scenario: 按方向加关键字组合搜索
- **WHEN** 调用方请求 `GET /api/v1/software-resources?direction=COMPUTER_VISION&keyword=python`
- **THEN** 返回计算机视觉方向（含通用方向）中，名称、分类或描述包含 "python" 的已启用资源

#### Scenario: 无关键字时保持原行为
- **WHEN** 调用方请求 `GET /api/v1/software-resources?direction=COMPUTER_VISION`
- **THEN** 返回计算机视觉方向（含通用方向）的所有已启用资源，与变更前行为一致

#### Scenario: 关键字为空字符串时忽略
- **WHEN** 调用方请求 `GET /api/v1/software-resources?keyword=`
- **THEN** 返回所有已启用资源，忽略该参数

### Requirement: 搜索范围限定在名称、分类和描述
关键字搜索 SHALL 仅匹配 `tb_software_resource` 的 `name`、`category`、`description` 字段。

#### Scenario: 外部链接不匹配关键字
- **WHEN** 调用方请求 `GET /api/v1/software-resources?keyword=jetbrains`
- **THEN** 不返回 `external_url` 包含 "jetbrains" 但其他字段不包含该词的资源

### Requirement: 搜索结果保持分页与排序
带关键字的搜索 SHALL 保持原有的分页参数和排序规则（按 `sort_order` 升序、`id` 升序）。

#### Scenario: 分页返回搜索结果
- **WHEN** 调用方请求 `GET /api/v1/software-resources?keyword=ide&size=5&page=0`
- **THEN** 返回最多 5 条匹配记录，响应包含 `totalElements`、`totalPages`、`content` 等分页字段
