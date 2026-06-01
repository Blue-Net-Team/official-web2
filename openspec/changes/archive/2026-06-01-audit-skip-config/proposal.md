## Why

健康检查接口（`/api/v1/health`）通常被监控探针高频调用（每数秒一次），当前所有带 `@RequiresPermission` 注解的接口都会被 `AuditAspect` 记录审计日志，导致审计表被无意义的健康检查记录刷屏。需要一种声明式机制让特定接口跳过审计记录，同时保留权限注解的完整性。

## What Changes

- **`@RequiresPermission` 注解增加 `audit` 属性**：布尔类型，默认 `true`。当设为 `false` 时，`AuditAspect` 跳过该接口的审计日志记录。
- **`AuditAspect` 增加跳过逻辑**：在切面入口处检查注解的 `audit` 属性，若为 `false` 则直接 `proceed()`，不走审计记录流程。
- **`HealthController.health()` 设置 `audit = false`**：健康检查接口声明为不记录审计。

## Capabilities

### New Capabilities

- `audit-skip`：允许通过 `@RequiresPermission(audit = false)` 声明式地跳过特定接口的审计日志记录。

### Modified Capabilities

- （无现有 spec 需要修改，本次变更仅涉及审计切面的行为扩展，不改变任何接口契约或业务需求。）

## Impact

| 受影响项 | 说明 |
|---------|------|
| `@RequiresPermission` 注解定义 | 增加 `audit` 属性 |
| `AuditAspect` 审计切面 | 增加 `audit` 属性检查逻辑 |
| `HealthController` | `health()` 方法增加 `audit = false` |
| `PermissionScanner` | 不受影响，只扫描 `value` 唯一性 |
| 审计日志表（`tb_audit`） | 健康检查记录不再写入 |
| 其他接口 | 默认行为不变（`audit` 默认 `true`） |
