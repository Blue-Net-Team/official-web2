## ADDED Requirements

### Requirement: 注解支持声明式跳过审计记录
`@RequiresPermission` 注解 SHALL 提供一个 `audit` 布尔属性，默认值为 `true`。当 `audit` 为 `false` 时，调用该接口 SHALL 不触发审计日志记录。

#### Scenario: 默认情况下接口记录审计日志
- **WHEN** 调用一个未显式设置 `audit` 属性的 `@RequiresPermission` 接口
- **THEN** 系统 SHALL 记录该请求的审计日志

#### Scenario: 显式跳过审计记录
- **WHEN** 调用一个标注了 `@RequiresPermission(audit = false)` 的接口
- **THEN** 系统 SHALL 不记录该请求的审计日志

### Requirement: 审计跳过不影响权限校验
当接口的 `@RequiresPermission` 注解设置 `audit = false` 时，权限校验逻辑 SHALL 保持不变，仍按 `access` 属性执行 PUBLIC / AUTHENTICATED / PROTECTED 校验。

#### Scenario: 跳过审计的受保护接口仍需权限
- **WHEN** 一个 `access = PROTECTED` 且 `audit = false` 的接口被无权限用户访问
- **THEN** 系统 SHALL 返回 403 禁止访问，且不记录审计日志

### Requirement: 健康检查接口不记录审计日志
`/api/v1/health` 健康检查接口 SHALL 被配置为 `audit = false`，不再向 `tb_audit` 表写入记录。

#### Scenario: 健康检查调用
- **WHEN** 监控探针调用 `/api/v1/health`
- **THEN** 系统 SHALL 返回健康状态
- **AND** 系统 SHALL 不向审计日志表写入任何记录
