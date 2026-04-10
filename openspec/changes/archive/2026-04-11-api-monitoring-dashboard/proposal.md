## Why

当前审计系统（`tb_audit`）已记录所有带 `@RequiresPermission` 注解的 API 请求，包含请求路径、耗时、状态码等丰富数据，但这些数据只能写入、无法查看和分析。需要一个监控仪表盘，将审计数据转化为可视化的 API 运维指标（请求量趋势、接口访问排名、平均响应时间），帮助管理员掌握系统运行状况。

## What Changes

- 数据库：`tb_audit` 表新增 `request_uri_pattern` 列（VARCHAR(500)），存储 Spring 路径模板（如 `/api/v1/file/download/{fileId}`），用于接口级别的聚合统计
- 后端：`AuditAspect` 在审计写入时通过 `HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE` 提取路径模板并一并持久化
- 后端：新增 `AuditStatisticsService` 接口及其直接查 PostgreSQL 的实现（Phase 1），提供趋势、排名、耗时三类统计查询
- 后端：新增管理端统计 API 端点（`/api/v1/admin/audit/statistics/*`）
- 后端：修复 `AuditMapper.xml` 中引用已删除列（`action`、`remarks`）的 stale resultMap
- 前端：新增 `/admin/panel` 监控仪表盘页面，包含请求量趋势折线图、接口访问排名表、接口响应时间排名表

## Capabilities

### New Capabilities

- `api-statistics`: API 统计查询能力，基于审计数据提供请求量趋势、接口访问排名、接口响应时间排名等聚合统计，支持按时间范围（24h/7d/30d）和粒度（小时/天）筛选

### Modified Capabilities

- `backend-audit-logging`: 审计写入逻辑增加 `request_uri_pattern` 字段的提取与持久化

## Impact

- **数据库**：`tb_audit` 表结构变更（新增列 + 索引），通过 Flyway 迁移实现
- **后端 API**：新增 3 个统计查询端点（`GET /api/v1/admin/audit/statistics/{trends,endpoints,latency}`）
- **后端代码**：修改 AuditAspect、AuditMapper.xml、Audit 实体；新增统计 Service 层和 Controller
- **前端页面**：新增 `/admin/panel` 页面（侧边栏入口已存在）
- **依赖**：前端可能需要图表库（如 @ant-design/charts 或 @ant-design/plots）
