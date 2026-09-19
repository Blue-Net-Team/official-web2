## 1. 新增按角色查询权限值的仓储能力

- [ ] 1.1 在 `RolePermissionRepository`（领域仓储接口）新增 `findPermissionValuesByRoleId(Long roleId)`，返回权限值集合，并在 Javadoc 中说明用途与「认证阶段每请求调用一次」的调用频次
- [ ] 1.2 在 `RolePermissionMapper` 新增投影查询：一条 join `tb_role_permission` 与 `tb_permission` 的 SQL 直接返回权限 `value` 列表；**必须在方法注释中说明这是分层约定允许的「为性能在 SQL 层做投影」例外**
- [ ] 1.3 实现 `RolePermissionRepositoryImpl.findPermissionValuesByRoleId`，将查询结果转换为领域层期望的类型
- [ ] 1.4 为该查询补充 Repository 层集成测试（继承 `DBIntegrationTest`）：覆盖有绑定、无绑定、多角色区分三种情形，断言返回的权限值集合内容正确
- [ ] 1.5 补充 `RoleFixture.roleId` 式的角色获取方式，避免测试自造角色（沿用既有 `RoleFixture`，不新增写 `tb_role` 的路径）

## 2. 改造认证路径

- [ ] 2.1 修改 `JwtAuthenticationFilter`：将 `permissionCache.getPermissionsByRole(user.getRoleId())` 替换为仓储查询；查询失败或结果为空时权限集为空集合，不抛出异常
- [ ] 2.2 保持 `SecurityPrincipal` 的结构与 `UserCTX` 的写入方式不变，确保下游 `PermissionAspect` 无需改动
- [ ] 2.3 更新 `JwtAuthenticationFilterTest`：改为 mock 仓储（而非 `PermissionCache`），断言权限集被正确写入 `SecurityPrincipal`；补充「角色无权限绑定时权限集为空」的用例
- [ ] 2.4 新增一个走**真实认证路径**的集成测试用例，断言认证后 `SecurityPrincipal.permissions` 非空——用于守护本 bug。注意：现有接口层测试用 `@WithSecurityPrincipal` 直接注入权限，绕过了真实加载路径，无法捕获此缺陷

## 3. 移除缓存使用点

- [ ] 3.1 删除 `RolePermissionManageAppServiceImpl` 中两处 `permissionCache.refresh()` 调用及 `PermissionCache` 依赖与 import
- [ ] 3.2 更新 `RolePermissionManageAppServiceImplIntegrationTest`：移除 `@MockitoBean PermissionCache`，确认用例语义不依赖刷新动作
- [ ] 3.3 从 `UserRepositoryImpl` 移除 `PermissionCache` 字段、构造参数与 import（经核查该依赖从未被使用）
- [ ] 3.4 删除 `PermissionChecker` 类（经核查生产与测试均无任何引用）

## 4. 删除缓存类本身

- [ ] 4.1 检索确认 `PermissionCache` 已无生产引用：`grep -rn "PermissionCache" src/main/java` 应只剩待删除项
- [ ] 4.2 删除 `infrastructure/security/cache/PermissionCache.java`
- [ ] 4.3 删除 `PermissionCacheTest.java`
- [ ] 4.4 确认 `infrastructure/security/cache/` 目录已空并移除该目录

## 5. 更新测试基类

- [ ] 5.1 从 `APIIntegrationTest` 移除 `@MockitoBean PermissionCache` 字段、import 与 Javadoc 中关于它的条目
- [ ] 5.2 更新 `APIIntegrationTest` 的 Javadoc：启动期查库组件清单现在只有 `MessageTemplateRegistry` 与 `SystemUserInitializer`
- [ ] 5.3 运行一个继承 `APIIntegrationTest` 的类，确认去掉该替身后上下文仍能启动、断言仍通过

## 6. 规范与文档

- [ ] 6.1 直接编辑 `openspec/specs/backend-permission-aop-interceptor/spec.md` 的 `### FR-AOP-005: 权限缓存` 小节：改写为「权限数据直查」，删除启动预载与刷新策略
- [ ] 6.2 同步修正同一文件中「错误处理」表格的「缓存未命中 → 从数据库加载」行，使其与新的直查语义一致
- [ ] 6.3 检查该文件「拦截器伪代码」示例中是否仍引用 `PermissionCache`，如有则更新
- [ ] 6.4 检查 `docs/` 与 `CLAUDE.md` 中是否有权限缓存的描述需要同步

## 7. 验证

- [ ] 7.1 执行 `./mvnw -B clean test-compile` 确认编译通过
- [ ] 7.2 执行 `./mvnw -B spotless:check` 确认格式合规
- [ ] 7.3 全量运行后端测试确认 0 失败，与改动前基线对比（基线：1914 个用例 0 失败）
- [ ] 7.4 缺陷回归验证：确认新的集成测试用例在「仓储返回空集合」时会失败（即该用例确实能捕获本类缺陷），再确认在正常情形下通过
- [ ] 7.5 确认 `src/main/java` 中不再有 `PermissionCache` / `PermissionChecker` 的任何残留引用
- [ ] 7.6 记录认证路径的查询次数：确认每请求仅新增一次数据库查询（可通过日志或断点确认），未出现按权限检查次数放大

## 8. 收尾

- [ ] 8.1 用实测结果更新 proposal.md 中关于性能影响的描述
- [ ] 8.2 若采纳 design 待定项，决定是否移除 `PermissionScanner` 上无效的 `@Order(100)` 并纠正其误导性注释
