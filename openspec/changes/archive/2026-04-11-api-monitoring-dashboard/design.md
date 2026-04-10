## Context

审计系统（V26 重构后）已在 `tb_audit` 中记录了所有 `@RequiresPermission` 注解接口的 HTTP 请求信息，包括 `request_method`、`request_uri`、`duration_ms`、`http_status`、`success_state`、`action_time` 等字段。审计通过 `AuditAspect`（AOP `@Around`）自动采集，异步写入数据库。

当前状态：**只写不读**——审计数据写入 `tb_audit` 后无法被查询或可视化。管理后台侧边栏已有"仪表盘"入口（`/admin/panel`），但该页面尚未创建。

数据量估算：高校团队管理平台，日均几百至几千请求，年累积约 60 万~180 万条审计记录。

## Goals / Non-Goals

**Goals:**

- 在管理后台提供 API 监控仪表盘，展示请求量趋势、接口访问排名、接口响应时间排名
- 在审计写入时规范化 URI（存储路径模板），使聚合统计直接可用
- 设计可扩展的统计 Service 接口，未来可切换为预聚合实现而无需改动 API 层

**Non-Goals:**

- 不做实时告警/通知
- 不做预聚合缓存（Phase 1 直接查 PostgreSQL，后续按需加入）
- 不做审计日志的查看/搜索/筛选（本变更聚焦聚合统计）
- 不引入外部监控系统（ELK、Grafana、Prometheus 等）

## Decisions

### D1: URI 规范化在写入时完成

**选择**：在 `AuditAspect` 中通过 `request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE)` 提取 Spring 路径模板，存入新列 `request_uri_pattern`。

**替代方案**：查询时用 SQL 正则/REPLACE 处理——SQL 复杂、性能差、边界情况多。

**理由**：Spring 在路由匹配阶段已自动生成模板字符串（如 `/api/v1/file/download/{fileId}`、`/api/v1/admin/competitions/{id}/images/{imageId}`），直接获取零成本。写入一次，查询端零负担。

### D2: Phase 1 直接查 PostgreSQL

**选择**：统计查询直接对 `tb_audit` 执行 SQL 聚合（GROUP BY + 时间分桶）。

**替代方案**：Redis 实时计数器 / 定时任务预聚合统计表 / 时序数据库。

**理由**：当前数据量（百万级以内）下，PG 带 `action_time` + `request_uri_pattern` 索引的聚合查询性能完全足够。避免过早引入额外复杂度。

### D3: 统计 Service 使用接口抽象

**选择**：定义 `AuditStatisticsService` 接口，当前实现 `DirectAuditStatisticsService` 直接查 SQL。

**理由**：为未来预聚合扩展预留切换点。通过 `@Profile` 或 `@ConditionalOnProperty` 切换实现类，Controller 和前端零改动。

### D4: 前端图表库选择

**选择**：使用 `@ant-design/charts`（基于 G2）。

**理由**：项目已使用 Ant Design 6，`@ant-design/charts` 是官方图表解决方案，风格一致，API 简洁。

### D5: 统计 API 设计

三个独立端点，各司其职：

| 端点 | 用途 | 核心参数 |
|------|------|---------|
| `GET /api/v1/admin/audit/statistics/trends` | 请求量趋势 | `period`（24h/7d/30d） |
| `GET /api/v1/admin/audit/statistics/endpoints` | 接口访问排名 | `period`, `limit`（默认 20） |
| `GET /api/v1/admin/audit/statistics/latency` | 接口响应时间排名 | `period`, `limit`（默认 20） |

### D6: 修复 AuditMapper.xml stale resultMap

在本次变更中一并修复 `AuditMapper.xml` 中引用已删除列（`action`、`remarks`）的 resultMap，补充 V26 新增列的映射。这是一个已存在的技术债务。

## Risks / Trade-offs

**[审计表增长导致查询变慢]** → `action_time` 和 `request_uri_pattern` 上的索引可支撑百万级数据。当数据量超过千万时可引入预聚合（D3 已预留扩展点）。

**[统计查询影响审计写入性能]** → 统计查询走 `@Transactional(readOnly = true)`，PostgreSQL MVCC 机制下读写互不阻塞。

**[BEST_MATCHING_PATTERN_ATTRIBUTE 为 null 的边界情况]** → AuditAspect 仅拦截 `@RequiresPermission` 方法，这些方法一定匹配到了 Controller，pattern 不会为 null。但代码中加 fallback（`pattern != null ? pattern : requestUri`）做防御性处理。
