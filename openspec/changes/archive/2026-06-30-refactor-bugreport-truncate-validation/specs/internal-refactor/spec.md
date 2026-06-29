## ADDED Requirements

无。本次变更为内部代码重构，不引入新的业务能力或 API 需求。

## MODIFIED Requirements

无。现有 `admin-bug-report-management` 能力的需求保持不变：
- 管理端仍可分页查询 Bug 报告列表。
- 管理端仍可查询 Bug 报告详情。
- 列表接口仍返回 brief 视图，详情接口仍返回 detail 视图。

实现层面仅调整 `description` 字段在 brief 视图中的返回策略：由后端截断改为完整返回，展示控制已在前端实现。

## REMOVED Requirements

无。
