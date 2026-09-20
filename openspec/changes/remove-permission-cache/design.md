## Context

`PermissionCache`（`infrastructure/security/cache/PermissionCache.java`）是一个 `@Component`，`@PostConstruct init()` 在 Spring 上下文启动时把权限与角色权限关系全量读入内存。

写入方是 `PermissionScanner`（`@Component` + `@Order(100)` + `InitializingBean`），它扫描 189 个 `@RequiresPermission` 注解并 `permissionMapper.insert(...)`。

探针实测结论：缓存先加载、扫描器后写入 → 启动后缓存 0 条、`exists("system:health") == false`。由于 `PermissionCache` 的读取是纯内存 map 查询（无 DB 回退），而 `PermissionAspect` 只对 `SUPER_ADMIN` 绕过，非超管角色的全部 `@RequiresPermission` 接口都会被拒。

生产使用面（已逐一核查）：

| 使用点 | 用途 | 处置 |
|---|---|---|
| `JwtAuthenticationFilter:75` | `getPermissionsByRole(roleId)` 构造 `SecurityPrincipal` | **改为直查持久层**（唯一实质改动点） |
| `RolePermissionManageAppServiceImpl:70,90` | 分配/移除权限后 `refresh()` | 删除调用 |
| `PermissionChecker:30,87` | `hasPermission` / `isOrphan` | 类本身**无任何引用**，删除 |
| `UserRepositoryImpl:39` | 仅字段声明，**从未使用** | 删除死依赖 |
| `getAllPermissions` / `getAllPermissionValues` / `getByValue` / `exists` | 无生产调用，仅 `PermissionCacheTest` 使用 | 随类删除 |
| `PermissionAspect` | 走 `principal.hasPermission(...)` | 不改 |
| `PermissionAppServiceImpl` | 不依赖缓存 | 不改 |

已有仓储能力缺口：`RolePermissionRepository.findPermissionIdsByRoleId(roleId)` 只返回权限 ID，需要新增按角色返回权限**值**的能力。

约束：

- 项目分层约定：Mapper 默认返回 DO/PO；允许为性能在 SQL 层投影并返回 VO/字符串，但必须在方法注释中说明。
- `openspec/specs/` 中有 3 个权限域旧格式规范（`### FR-AOP-00X:`），不能参与 delta，需直接编辑。

## Goals / Non-Goals

**Goals:**

- 消除权限校验因启动顺序导致的全面失效
- 使权限数据的读取路径与其规范描述一致（「未命中则从数据库加载」）
- 顺带清除权限域的死代码与死依赖
- 简化 `APIIntegrationTest` 的启动期替身清单

**Non-Goals:**

- 不引入任何新的缓存层
- 不改 `PermissionScanner` 的同步职责与唯一性校验职责
- 不改 `PermissionAspect` / `SecurityPrincipal` 的判定路径
- 不改变权限注解、权限标识的既有语义
- 不做「每次权限检查都查库」的实现（只在认证阶段查一次）

## Decisions

### 决策 1：删除缓存，认证阶段直查，权限集仍随 `SecurityPrincipal` 传递

**选择**：`JwtAuthenticationFilter` 在认证时调用新增的仓储方法取得该角色的权限值集合，写入 `SecurityPrincipal`；`PermissionAspect` 继续读 `SecurityPrincipal` 携带的集合，不再查库。

**理由**：
- 每请求固定一次查询，不随接口数量或权限检查次数放大
- `PermissionAspect` 与 `SecurityPrincipal` 的结构不变，改动面最小
- 与 `security-principal-context` 规范「认证时加载权限并存入 `SecurityPrincipal`」的既有结构一致

**被否决的替代方案**：

| 方案 | 否决原因 |
|---|---|
| 保留缓存，修复加载顺序（`@DependsOn("permissionScanner")` 或在扫描器末尾调 `refresh()`） | 能修好当前的 bug，但保留了「启动期全量预载」这一根本脆弱点：任何新增的启动期写入方都会再次打破顺序假设，而且异常时缓存静默为空、无回退 |
| 请求级懒加载缓存（RequestScope / 首次访问时加载） | 解决了启动顺序，但引入生命周期管理与失效语义的复杂度；当前规模下收益不足 |
| `PermissionAspect` 每次检查都查库 | 查询次数随接口内权限检查次数放大；且 `PermissionAspect` 已依赖 `SecurityPrincipal`，改它属于无必要扩大改动面 |

### 决策 2：复用已有的 `PermissionMapper.selectByRoleId`，不在 `RolePermissionMapper` 重写查询

**选择**：在 `PermissionRepository` 新增 `findValuesByRoleId(Long roleId)` 返回权限值集合，实现直接委派给**已存在的** `PermissionMapper.selectByRoleId`。

**理由**：实施勘查发现 `PermissionMapper.selectByRoleId` 就是所需要的那条查询：

```xml
<select id="selectByRoleId" resultType="java.lang.String">
    SELECT p.value FROM tb_permission p
    INNER JOIN tb_role_permission rp ON p.id = rp.permission_id
    WHERE rp.role_id = #{roleId}
</select>
```

它已声明在 Mapper 接口上、已有 XML，但**主代码与测试中都没有任何调用点**（历史遗留，应为早期缓存预载方案的残留）。直接复用既避免了写一条功能重复的 SQL，又让这段死代码重新有了用途。

索引方面：`tb_role_permission` 上的 `UNIQUE(role_id, permission_id)` 已提供以 `role_id` 为前导列的索引，该查询可命中索引。

**与原设计的差异**：原设计拟在 `RolePermissionRepository` 上新增方法、并在 `RolePermissionMapper` 写一条新的 join 查询。改为复用后，既无新增 SQL、也不再需要分层约定的「Mapper 返回投影」例外说明（该例外已由既有代码的 `resultType="java.lang.String"` 体现）。

**被否决的替代方案**：

| 方案 | 否决原因 |
|---|---|
| 在 `RolePermissionMapper` 新增一条功能相同的 join 查询 | 制造两条重复 SQL，后续修改易漏 |
| 先查 `findPermissionIdsByRoleId` 再查权限值 | 两次数据库往返，而一条 join 即可 |

### 决策 3：删除 `PermissionChecker` 与 `UserRepositoryImpl` 的死依赖

**选择**：直接删除 `PermissionChecker` 整个类；从 `UserRepositoryImpl` 移除 `PermissionCache` 字段与构造参数。

**理由**：`PermissionChecker` 自称「供业务代码使用」，但生产与测试中均无任何引用；`UserRepositoryImpl` 的缓存依赖只有字段声明、从未使用。两者都是死代码，留着会在删除缓存时产生无意义的改造成本（例如把 `PermissionChecker` 改成查库，却依然无人调用）。

### 决策 4：旧格式规范采用直接编辑，不在本变更内做格式迁移

**选择**：`backend-permission-aop-interceptor` 的 FR-AOP-005 由实现阶段直接编辑该文件，不生成 delta。

**理由**：delta 机制要求 `### Requirement:` 标题，该文件使用 `### FR-AOP-00X:`，`openspec validate` 直接报错。把 3 个旧格式规范的格式迁移塞进一个 bugfix 变更，会让评审无法区分「行为变更」与「文档重排版」。

**代价**：本变更对该规范的行为变更不会出现在 delta 中，只在实现提交里可见。已列为待定项，供后续决定是否单独做格式迁移。

## Risks / Trade-offs

- **每请求多一次数据库查询**：认证阶段查一次角色权限。→ 当前系统规模可接受（已确认）；实现时确认该查询走了 `tb_role_permission` 与 `tb_permission` 的索引，避免全表扫描。
- **权限校验的可用性从此依赖数据库**：数据库不可用时认证即失败。→ 这与「数据库不可用时应用本就不可用」一致；且当前实现虽然「不依赖数据库」，但其结果是**拒绝一切非超管请求**，严格劣于直查。
- **`PermissionScanner` 写入与请求并发的竞态**：扫描器在上下文启动阶段完成写入，早于应用开始接受请求。→ 实现时确认启动流程未变（扫描器仍是 `InitializingBean`）。
- **删除缓存后 `refresh()` 调用点消失**：若将来有人期望「改权限后立即生效」的旧语义，需注意新语义是「下次请求即生效」，比旧语义更强。→ 在规范中明确。
- **`PermissionCacheTest` 删除会降低权限读取逻辑的测试覆盖**：→ 新增仓储方法的查询应有 Repository 层集成测试覆盖；`JwtAuthenticationFilterTest` 改为 mock 仓储并断言权限集被正确写入 `SecurityPrincipal`。

## Migration Plan

1. 新增 `RolePermissionRepository.findPermissionValuesByRoleId` 与其 Mapper 投影查询，补 Repository 层集成测试
2. 改 `JwtAuthenticationFilter` 使用新方法；更新 `JwtAuthenticationFilterTest`
3. 删除 `RolePermissionManageAppServiceImpl` 的两处 `refresh()`；更新对应集成测试
4. 删除 `PermissionChecker`、`UserRepositoryImpl` 的死依赖
5. 删除 `PermissionCache` 与 `PermissionCacheTest`
6. 更新 `APIIntegrationTest` 的替身清单
7. 直接编辑 `backend-permission-aop-interceptor` 的 FR-AOP-005
8. 全量运行后端测试确认 0 失败

**回滚策略**：改动集中在认证与权限读取路径。`git revert` 即可恢复缓存实现。无数据迁移、无 schema 变更，因此不涉及数据回滚。

## Open Questions

- 是否要顺带把 3 个旧格式权限规范（`backend-permission-annotation`、`backend-permission-aop-interceptor`、`backend-permission-scanning`）迁移到 `### Requirement:` 标准格式？迁移后权限域的后续变更才能真正走 delta 评审。倾向单独一个文档变更，不混入本次。
- 是否需要新增一个「认证后 `SecurityPrincipal.permissions` 非空」的集成测试作为本 bug 的回归守护？当前接口层测试用 `@WithSecurityPrincipal` 直接注入权限，**绕过了真实加载路径**，因此即使修复了也不会被它捕获。建议至少补一个走真实认证路径的用例。
- 是否保留 `PermissionScanner` 的 `@Order(100)`？该注解对单例初始化顺序无效，其注释「在其他组件初始化之后执行」是误导。删除缓存后该注解已无实际作用，可考虑移除或在注释中纠正说明。
