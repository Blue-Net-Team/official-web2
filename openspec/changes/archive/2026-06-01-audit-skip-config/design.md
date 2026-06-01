## Context

当前 `AuditAspect` 通过 `@Around("@annotation(requiresPermission)")` 拦截所有带 `@RequiresPermission` 注解的方法，无差别记录审计日志。健康检查接口 `/api/v1/health` 被监控探针高频调用（每数秒一次），导致 `tb_audit` 表被大量无意义记录填充，影响审计日志的查询性能和可读性。

## Goals / Non-Goals

**Goals:**
- 提供声明式机制，允许特定接口跳过审计日志记录
- 健康检查接口不再写入审计日志
- 默认行为保持向后兼容（现有接口不受影响）

**Non-Goals:**
- 不引入 `application.yml` 级别的全局排除配置（阶段二扩展方向）
- 不改变权限校验逻辑（`PermissionAspect` 不受影响）
- 不修改 `PermissionScanner` 的扫描行为
- 不提供运行时动态开关审计的能力

## Decisions

**1. 在 `@RequiresPermission` 注解上增加 `audit` 属性（默认 `true`）**

- **Rationale**: 声明式配置与权限注解同处一处，阅读 Controller 代码时一眼可知该接口是否记录审计。相比在 `application.yml` 中配置路径排除，更不容易因路径重构而遗漏。
- **Alternatives considered**: `application.yml` 路径排除（与代码分离，重构易遗漏）；在 `AuditAspect` 中硬编码 `/api/v1/health`（不灵活，技术债）。

**2. `AuditAspect` 在切面入口处检查 `audit` 属性，而非在 `finally` 块中跳过**

- **Rationale**: 若 `audit = false`，整个审计记录流程（计时、序列化参数、捕获异常等）都不需要执行，减少不必要的开销。
- **实现**: 在 `audit()` 方法开头检查 `requiresPermission.audit()`，若为 `false` 直接 `return pjp.proceed()`。

**3. `audit` 属性仅影响 `AuditAspect`，不影响 `PermissionAspect`**

- **Rationale**: 审计跳过和权限校验是两个独立的横切关注点。即使不记录审计，权限校验（PUBLIC / AUTHENTICATED / PROTECTED）仍然正常执行。

## Risks / Trade-offs

| 风险 | 缓解措施 |
|------|---------|
| 开发者误将敏感接口设为 `audit = false` 导致操作无迹可寻 | `audit` 默认 `true`，需显式设为 `false`；Code Review 时重点关注 |
| 未来需要批量调整审计策略时，需逐个改注解 | 作为已知限制，阶段二可引入 `application.yml` 全局覆盖配置 |
| `PermissionScanner` 启动校验可能受影响 | 验证方案：只扫描 `value` 唯一性，`audit` 属性不在扫描范围内 |
