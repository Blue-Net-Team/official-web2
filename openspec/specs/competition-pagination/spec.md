## ADDED Requirements

### Requirement: 竞赛分页查询接口
系统 SHALL 提供 `GET /api/v1/competitions/page` 接口，接受 `page`（页码，默认0）和 `size`（每页数量，默认10）查询参数，返回 `ResponseMessage<PageDTO<CompetitionResponseDTO>>` 格式的分页数据。

#### Scenario: 默认参数分页查询
- **WHEN** 发送 `GET /api/v1/competitions/page` 请求（不传参数）
- **THEN** 返回 HTTP 200，data 为 PageDTO 格式，page=0，size=10，content 为竞赛列表，按 sort_order DESC, created_at DESC 排序

#### Scenario: 自定义分页参数查询
- **WHEN** 发送 `GET /api/v1/competitions/page?page=1&size=5` 请求
- **THEN** 返回 HTTP 200，data.page=1，data.size=5，content 为第2页的5条竞赛记录

#### Scenario: size 超过上限时自动限制
- **WHEN** 发送 `GET /api/v1/competitions/page?page=0&size=100` 请求
- **THEN** 返回 HTTP 200，实际 size 被 clamp 为 50，返回不超过50条记录

#### Scenario: 无竞赛数据时返回空页
- **WHEN** 数据库中无竞赛记录时发送分页请求
- **THEN** 返回 HTTP 200，content 为空列表，totalElements=0，totalPages=0

#### Scenario: 页码超出范围时返回空页
- **WHEN** 发送 `GET /api/v1/competitions/page?page=999&size=10` 请求且总数据不足
- **THEN** 返回 HTTP 200，content 为空列表，totalElements 为实际总数

### Requirement: 分页接口为公开访问
分页查询接口 SHALL 使用 `@RequiresPermission(access = AccessLevel.PUBLIC)` 注解，无需认证即可访问。

#### Scenario: 未登录用户访问分页接口
- **WHEN** 未携带认证信息发送 `GET /api/v1/competitions/page` 请求
- **THEN** 正常返回分页数据，HTTP 200

### Requirement: 分页返回数据包含完整竞赛信息
分页查询返回的每条 CompetitionResponseDTO SHALL 包含以下字段：id、name、shortName、level、month、organizer、summary、logoFileId、coverFileId。

#### Scenario: 验证返回字段完整性
- **WHEN** 分页查询返回竞赛数据
- **THEN** 每条记录包含 id、name、shortName、level、month、organizer、summary、logoFileId、coverFileId 字段，非空字段有值

### Requirement: 分页排序规则
分页查询结果 SHALL 按 sort_order 降序排列，相同 sort_order 时按 created_at 降序排列。

#### Scenario: 验证排序正确性
- **WHEN** 存在多条不同 sort_order 的竞赛记录
- **THEN** 返回结果中 sort_order 大的排在前面，相同 sort_order 时新创建的排在前面
