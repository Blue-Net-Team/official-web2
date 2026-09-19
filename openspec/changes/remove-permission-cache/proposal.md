## Why

`PermissionCache` 通过 `@PostConstruct` 在启动时把权限与角色权限关系全量读入内存，但真正写入权限的是 `PermissionScanner`（`@Order(100)` 的 `InitializingBean`），它扫描 189 个 `@RequiresPermission` 后才落库。**缓存先加载、扫描器后写入**，探针实测启动后缓存为空（`getAllPermissions().size() == 0`），而扫描器随后才报告 `Found 189 controller methods`。

`@Order` 不控制单例 Bean 的初始化顺序，注释未被兑现。缓存读取是纯内存 map 查询、没有 DB 回退，所以启动后 `hasPermission` 对所有角色返回 false。`PermissionAspect` 只对 `SUPER_ADMIN` 绕过，因此**除超管外全部 `@RequiresPermission` 接口都会被拒**。

规范本就要求有 DB 回退（`backend-permission-aop-interceptor/spec.md:152`：「缓存未命中 → 从数据库加载」），实现从未做到。删掉缓存，实现反而与规范一致。

## What Changes

- 删除 `PermissionCache` 及其单元测试 `PermissionCacheTest`
- 新增仓储能力：按角色查询权限值集合（关联权限表取 `value`）
- `JwtAuthenticationFilter` 改为在认证时从仓储加载该角色的权限值集合，写入 `SecurityPrincipal`
- 删除 `RolePermissionManageAppServiceImpl` 中两处 `permissionCache.refresh()`
- 删除死代码：`PermissionChecker`（生产与测试均无任何引用）、`UserRepositoryImpl` 中声明但从未使用的 `PermissionCache` 依赖
- 测试更新：`APIIntegrationTest` 去掉 `@MockitoBean PermissionCache`（启动期需替换的组件少一个）；`RolePermissionManageAppServiceImplIntegrationTest` 与 `JwtAuthenticationFilterTest` 去掉对应 mock

本次变更**不包含**（明确非目标）：

- 不修改 `PermissionScanner`：注解到 `tb_permission` 的同步职责保留，启动时校验权限标识唯一性的职责保留
- 不引入任何新的缓存层。若将来确需缓存，应是带失效机制、且有明确加载顺序保证的实现，而不是 `@PostConstruct` 全量预载
- 不修改 `PermissionAspect` 的判定逻辑（继续读 `SecurityPrincipal` 携带的权限集）
- 不改 `PermissionAppServiceImpl` / `PermissionRepository`（经核查不依赖缓存）
- 不并入 `optimize-db-test-cleanup`（测试清理性能优化）——两件事独立

## Capabilities

### New Capabilities

无。

### Modified Capabilities

- `security-principal-context`: 认证时权限集的来源由 `PermissionCache` 改为仓储直查；明确 `SecurityPrincipal` 仍携带权限集，`PermissionAspect` 的判定路径不变
- `backend-test-base-layering`: `APIIntegrationTest` 启动期需替换的组件清单移除 `PermissionCache`（该项因 `@PostConstruct` 查库而列入，删除缓存后不再需要）
- `backend-permission-aop-interceptor`: 将 FR-AOP-005「权限缓存」改为「权限数据直查」，删除启动预载与刷新策略，使规范与实际实现一致

> ⚠️ 该规范使用旧格式标题（`### FR-AOP-00X:` 而非 `### Requirement:`），**无法参与 delta 机制**（`openspec validate` 会报 “no requirement entries parsed”）。因此本变更对该规范采用**直接编辑**，不生成 delta 文件，并在 tasks 中单独列任务。
>
> 全库共 3 个旧格式规范，均属权限域：`backend-permission-annotation`、`backend-permission-aop-interceptor`、`backend-permission-scanning`（其余 128 个均为标准格式）。是否在本变更内一并迁移为标准格式，见 design 的待定项。

## Impact

- 生产代码：删除 `PermissionCache`、`PermissionChecker` 两个类；修改 `JwtAuthenticationFilter`、`RolePermissionManageAppServiceImpl`；清理 `UserRepositoryImpl` 的死依赖；新增一个仓储查询方法与其 Mapper 查询
- 测试代码：删除 `PermissionCacheTest`；更新 `APIIntegrationTest`、`RolePermissionManageAppServiceImplIntegrationTest`、`JwtAuthenticationFilterTest`
- 性能：认证阶段每请求增加一次数据库查询（加载该角色的权限值集合）。当前系统规模下可接受，且消除了启动期状态依赖
- 风险：失去内存缓存后，权限校验的延迟取决于数据库；若后续出现性能压力，需重新引入缓存，但必须解决加载顺序问题
- 关联：本变更是 `optimize-db-test-cleanup` 的前置清理项之一（后者会随之少一个需要替身的启动期组件）

## 实测勘查结论（本变更的设计依据）

| 勘查项 | 结论 |
|---|---|
| `PermissionCache` 的生产使用点 | `JwtAuthenticationFilter:75`（`getPermissionsByRole`）、`RolePermissionManageAppServiceImpl:70,90`（`refresh`）、`PermissionChecker:30,87`、`UserRepositoryImpl:39` |
| `PermissionChecker` 的引用 | **生产与测试均无引用**，死代码 |
| `UserRepositoryImpl` 的缓存依赖 | 仅有字段声明，**从未使用**，死依赖 |
| `getAllPermissions` / `getAllPermissionValues` / `getByValue` / `exists` | **无生产调用**，仅被 `PermissionCacheTest` 使用 |
| `PermissionAspect` 是否依赖缓存 | 否，走 `principal.hasPermission(...)` |
| `PermissionAppServiceImpl` 是否依赖缓存 | 否 |
| 是否已有「按角色查权限值」的仓储能力 | 否。`RolePermissionRepository.findPermissionIdsByRoleId` 只返回权限 ID |
| 为何没有测试发现 | 接口层测试 mock 了 `PermissionCache` 且用 `@WithSecurityPrincipal` 直接注入权限；DB 层测试每用例清空 `tb_permission` |
