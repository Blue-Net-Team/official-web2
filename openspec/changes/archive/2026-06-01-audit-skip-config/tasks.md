## 1. 注解扩展

- [x] 1.1 在 `@RequiresPermission` 注解中增加 `audit` 布尔属性，默认值为 `true`

## 2. 审计切面改造

- [x] 2.1 在 `AuditAspect.audit()` 方法入口处增加 `requiresPermission.audit()` 检查
- [x] 2.2 当 `audit = false` 时直接 `return pjp.proceed()`，跳过计时、参数序列化、日志写入等全部审计逻辑

## 3. 健康检查接口配置

- [x] 3.1 在 `HealthController.health()` 的 `@RequiresPermission` 注解上增加 `audit = false`

## 4. 验证

- [x] 4.1 编译后端项目，确认无编译错误
- [x] 4.2 启动应用，确认 `PermissionScanner` 正常扫描（`value` 唯一性校验不受影响）
- [x] 4.3 调用 `/api/v1/health`，确认返回正常且审计日志表无新增记录
- [x] 4.4 调用一个 `audit = true`（默认）的接口，确认审计日志正常记录
