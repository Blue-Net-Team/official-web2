## 1. Database Migration

- [x] 1.1 Create Flyway migration to add `request_uri_pattern` VARCHAR(500) column to `tb_audit` table
- [x] 1.2 Add index `idx_audit_uri_pattern` on `request_uri_pattern` column

## 2. Audit Entity & Mapper Update

- [x] 2.1 Add `requestUriPattern` field to `Audit` entity class
- [x] 2.2 Update `AuditMapper.xml` resultMap: remove stale columns (`action`, `remarks`), add V26 columns (`request_method`, `request_uri`, `http_status`, `response_message`, `stack_trace`, `duration_ms`) and new `request_uri_pattern`

## 3. AuditAspect URI Pattern Extraction

- [x] 3.1 In `AuditAspect.audit()`, extract `HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE` from HttpServletRequest
- [x] 3.2 Set `requestUriPattern` on the Audit entity before saving (with null fallback to raw `requestUri`)

## 4. Statistics Service Layer

- [x] 4.1 Create `AuditStatisticsService` interface with methods: `getTrends(period)`, `getEndpointRanking(period, limit)`, `getEndpointLatencyRanking(period, limit)`
- [x] 4.2 Create `DirectAuditStatisticsService` implementation with SQL aggregation queries against `tb_audit`
- [x] 4.3 Create request/response DTOs for statistics endpoints (trends, endpoints, latency)
- [x] 4.4 Create corresponding Repository methods in `AuditRepository` for statistics queries

## 5. Statistics API Controller

- [x] 5.1 Create `AuditStatisticsController` under admin controller package
- [x] 5.2 Add `@RequiresPermission` annotation with admin-level access control
- [x] 5.3 Implement `GET /api/v1/admin/audit/statistics/trends` endpoint
- [x] 5.4 Implement `GET /api/v1/admin/audit/statistics/endpoints` endpoint
- [x] 5.5 Implement `GET /api/v1/admin/audit/statistics/latency` endpoint

## 6. Frontend Monitoring Dashboard

- [x] 6.1 Install `@ant-design/charts` dependency
- [x] 6.2 Create `/admin/panel/page.tsx` with monitoring dashboard layout
- [x] 6.3 Implement request volume trend chart component with period switching (24h/7d/30d)
- [x] 6.4 Implement endpoint access ranking table component
- [x] 6.5 Implement endpoint latency ranking table component
- [x] 6.6 Create frontend API service for statistics endpoints
