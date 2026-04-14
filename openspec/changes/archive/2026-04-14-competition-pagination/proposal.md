## Why

当前竞赛列表接口 `GET /api/v1/competitions` 仅支持 `limit` 参数返回固定数量的记录，无法支持分页浏览。前端需要分页能力来支持大量竞赛数据的展示和管理，且项目中 Member 和 Achievement 模块已有成熟的分页模式（PageDTO + Pageable），竞赛模块应保持一致。

## What Changes

- 新增 `GET /api/v1/competitions/page` 分页查询接口，接受 `page`（页码，默认0）和 `size`（每页数量，默认10，上限50）参数
- 返回值使用 `ResponseMessage<PageDTO<CompetitionResponseDTO>>`，包含分页元数据（totalElements、totalPages 等）
- 排序规则：按 `sort_order DESC, created_at DESC`
- 在 DDD 各层新增对应的分页方法：
  - 基础设施层：Mapper 新增分页查询 SQL
  - 领域层：Repository 接口新增分页方法
  - 应用层：Service 新增分页查询方法
  - 接口层：Controller 新增分页端点
- 保留现有 `GET /api/v1/competitions` 接口不变，分页为独立的新接口

## Capabilities

### New Capabilities

- `competition-pagination`: 竞赛列表分页查询能力，支持 page/size 参数，返回 PageDTO 包装的分页数据

### Modified Capabilities

（无，现有接口不变）

## Impact

- **后端代码**：
  - `CompetitionController` - 新增分页端点
  - `CompetitionService` / `CompetitionServiceImpl` - 新增分页查询方法
  - `CompetitionDomainService` / `CompetitionDomainServiceImpl` - 新增分页领域方法
  - `CompetitionRepository` / `CompetitionRepositoryImpl` - 新增分页仓储方法
  - `CompetitionMapper` - 新增分页查询方法和 XML SQL
- **API**：新增公开接口 `GET /api/v1/competitions/page`，使用 `@RequiresPermission(access = AccessLevel.PUBLIC)`
- **依赖**：使用 Spring Data `Pageable` 和项目已有的 `PageDTO`
- **数据库**：无 schema 变更，仅新增分页 SQL（使用 OFFSET/LIMIT）
