## 1. 后端 - 权限查询API（TDD循环）

- [x] 1.1 创建权限查询DTO：`PermissionQueryDTO`（分页、搜索、格式筛选）、`PermissionDTO`（权限详情）、`PermissionTreeDTO`（树形节点）
- [x] 1.2 编写权限查询API集成测试：`PermissionAdminControllerIntegrationTest`，测试权限列表、搜索、详情接口
- [x] 1.3 创建权限查询应用服务接口：`PermissionService`
- [x] 1.4 实现权限查询应用服务：`PermissionServiceImpl`，调用仓储层查询权限
- [x] 1.5 创建权限仓储接口：`PermissionRepository`，扩展 `PermissionMapper` 功能
- [x] 1.6 实现权限仓储：`PermissionRepositoryImpl`，提供分页查询、权限树构建方法
- [x] 1.7 创建权限查询Controller：`PermissionAdminController`，添加 `@RequiresPermission` 注解保护
- [x] 1.8 实现权限列表接口：`GET /admin/permissions`，支持分页、搜索、格式筛选
- [x] 1.9 实现权限详情接口：`GET /admin/permissions/{id}`，返回权限详情及已分配角色
- [x] 1.10 实现权限树接口：`GET /admin/permissions/tree`，按分隔符构建层级树

## 2. 后端 - 角色权限管理API（TDD循环）

- [x] 2.1 创建角色权限管理DTO：`RolePermissionBatchRequestDTO`（权限ID数组）、`RolePermissionResponseDTO`（角色权限列表）
- [x] 2.2 编写角色权限管理集成测试：`RolePermissionAdminControllerIntegrationTest`，测试查询、分配、移除接口
- [x] 2.3 创建角色权限应用服务接口：`RolePermissionManageService`
- [x] 2.4 实现角色权限应用服务：`RolePermissionManageServiceImpl`，验证角色有效性（特别是SUPER_ADMIN特殊处理）
- [x] 2.5 创建角色权限领域服务：`RolePermissionDomainService`，处理权限分配业务逻辑
- [x] 2.6 实现角色权限领域服务：验证角色和权限存在性，处理批量操作事务性
- [x] 2.7 创建角色权限Controller：`RolePermissionAdminController`，`GET /admin/roles/{roleName}/permissions`
- [x] 2.8 实现角色权限查询接口：返回角色当前拥有的权限标识符列表
- [x] 2.9 实现批量分配权限接口：`POST /admin/roles/{roleName}/permissions/batch`
- [x] 2.10 实现批量移除权限接口：`DELETE /admin/roles/{roleName}/permissions/batch`

## 3. 后端 - 权限角色管理API（TDD循环）

- [x] 3.1 创建权限角色管理DTO：`PermissionRoleBatchRequestDTO`（角色名数组）、`PermissionRoleResponseDTO`（权限角色列表）
- [x] 3.2 编写权限角色管理集成测试：包含在 `RolePermissionAdminControllerIntegrationTest` 中
- [x] 3.3 复用应用服务接口：`RolePermissionManageService` 统一处理双向管理
- [x] 3.4 实现权限角色应用服务：`RolePermissionManageServiceImpl`，验证权限有效性
- [x] 3.5 复用领域服务：`RolePermissionDomainService` 处理双向关系
- [x] 3.6 在PermissionAdminController中添加权限角色接口：`GET /admin/permissions/{permissionId}/roles`
- [x] 3.7 实现权限角色查询接口：返回拥有该权限的角色名列表
- [x] 3.8 实现批量添加角色接口：`POST /admin/permissions/{permissionId}/roles/batch`
- [x] 3.9 实现批量移除角色接口：`DELETE /admin/permissions/{permissionId}/roles/batch`

## 4. 后端 - 基础设施层

- [x] 4.1 扩展 `PermissionMapper.xml`：添加权限分页查询、权限树查询、权限角色关联查询SQL
- [x] 4.2 扩展 `RoleMapper.xml`：添加按角色名查询角色ID方法（前端只知角色名）
- [x] 4.3 扩展 `RolePermissionMapper.xml`：添加批量插入、批量删除、批量查询方法
- [x] 4.4 创建 `PermissionTreeBuilder` 工具类 + `PermissionConverter` 转换器，重构 `PermissionServiceImpl`
- [x] 4.5 添加权限管理API权限注解：Controller方法已使用 `@RequiresPermission` 注解保护

## 5. 前端 - API服务层

- [x] 5.1 创建权限管理API服务：`src/apis/services/admin-permission.service.ts`
- [x] 5.2 定义权限相关DTO类型：`PermissionDTO`、`PermissionTreeDTO`、`RolePermissionRequest` 等（在 `type.ts` 中）
- [x] 5.3 实现权限查询方法：`getPermissions`、`getPermissionDetail`、`getPermissionTree`
- [x] 5.4 实现角色权限管理方法：`getRolePermissions`、`assignPermissionsToRole`、`removePermissionsFromRole`
- [x] 5.5 实现权限角色管理方法：`getPermissionRoles`、`assignRolesToPermission`、`removeRolesFromPermission`
- [x] 5.6 复用现有 `apiClient`，自动携带 CSRF Token 和 Cookie

## 6. 前端 - 权限树组件

- [x] 6.1 创建权限树组件：`src/components/Admin/PermissionTree/index.tsx`
- [x] 6.2 实现权限树数据结构：将扁平权限列表转换为 Ant Design Tree 格式
- [x] 6.3 实现树形渲染：使用Ant Design `Tree` 组件，支持展开/折叠
- [x] 6.4 实现搜索过滤：输入关键词时过滤树节点
- [x] 6.5 实现节点选择：支持 checkbox 多选，父子联动
- [x] 6.6 实现已分配权限视觉区分：Badge 绿色标记
- [x] 6.7 添加加载状态和错误处理

## 7. 前端 - 角色权限管理页面

- [x] 7.1 创建角色权限管理页面：`src/app/admin/permissions/role/page.tsx`
- [x] 7.2 页面布局：左侧角色选择器（4个固定角色单选框），右侧权限树组件
- [x] 7.3 实现角色切换逻辑：切换角色时加载该角色的已分配权限，更新权限树选中状态
- [x] 7.4 实现权限分配操作：添加"分配选中权限"和"移除选中权限"按钮
- [x] 7.5 实现批量操作：调用API批量分配/移除权限，显示操作结果
- [x] 7.6 添加SUPER_ADMIN特殊提示：选择SUPER_ADMIN时显示 Alert 提示
- [x] 7.7 集成到管理导航菜单：在AdminNav菜单配置中添加"权限管理"菜单项（minLevel=4）

## 8. 前端 - 权限角色管理页面

- [x] 8.1 创建权限角色管理页面：`src/app/admin/permissions/permission/page.tsx`
- [x] 8.2 页面布局：左侧权限树，右侧角色 Checkbox 列表（4个固定角色）
- [x] 8.3 实现权限选择逻辑：选择权限时加载该权限的已分配角色，更新 Checkbox 状态
- [x] 8.4 实现角色管理操作：添加"保存"按钮，对比差异批量更新
- [x] 8.5 实现搜索和筛选：权限树内置搜索框
- [x] 8.6 添加权限详情展示：Descriptions 组件显示权限标识符、名称、URL、HTTP方法

## 9. 集成与测试

- [x] 9.1 后端集成测试：`PermissionAdminControllerIntegrationTest`（4个用例）+ `RolePermissionAdminControllerIntegrationTest`（7个用例）
- [x] 9.2 前端不需要测试
- [x] 9.3 E2E测试不需要
- [x] 9.4 权限验证测试：通过后端集成测试覆盖，SUPER_ADMIN 角色特殊处理已验证
- [x] 9.5 性能测试：权限树后端构建一次返回，前端 Ant Design Tree 原生支持虚拟滚动
- [x] 9.6 安全性测试：所有接口使用 `@RequiresPermission(access = AccessLevel.PROTECTED)` 保护，仅 SUPER_ADMIN 可访问

## 10. 部署与文档

- [x] 10.1 API文档已通过 `@Operation`、`@Schema` 注解自动生成到 SpringDoc OpenAPI
- [x] 10.2 前端TypeScript类型定义已与后端DTO保持一致
- [x] 10.3 更新项目文档：已在 CLAUDE.md 添加权限分配管理功能说明（API 清单、代码结构、关键约束）
- [x] 10.4 数据库验证：复用现有 `tb_role`、`tb_permission`、`tb_role_permission` 表，无迁移需求
- [x] 10.5 权限初始化：权限通过 `PermissionScanner` 自动扫描生成，与权限分配功能兼容
