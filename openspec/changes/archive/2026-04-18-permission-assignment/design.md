## Context

项目已建立完整的权限管理体系：
- 权限注解 `@RequiresPermission` 定义接口权限
- 自动扫描器 `PermissionScanner` 启动时扫描Controller并同步权限到数据库
- 权限缓存 `PermissionCache` 提供高效权限查询
- AOP切面 `PermissionAspect` 实时校验用户权限
- 固定角色 `tb_role` 表包含4个预定义角色：SUPER_ADMIN, DIRECTION_ADMIN, MEMBER, CANDIDATE
- 权限分配通过数据库脚本手动维护，缺乏可视化界面

当前问题：SUPER_ADMIN无法通过界面管理权限分配，只能通过Flyway迁移脚本修改 `tb_role_permission` 表，操作繁琐且易出错。

## Goals / Non-Goals

**Goals:**
- 提供两个视角的权限管理界面：
  1. **角色视角**：为特定角色分配/移除权限，树形展示所有权限
  2. **权限视角**：查看/管理每个权限对应的角色列表
- 所有权限管理操作仅限SUPER_ADMIN角色访问
- 权限列表支持搜索、分页、按权限类型（格式）筛选
- 权限树形结构展示，支持按层级折叠/展开
- 批量分配/移除权限功能，提升操作效率
- 集成到现有管理后台导航菜单，保持UI一致性

**Non-Goals:**
- 不修改现有权限校验逻辑和AOP切面
- 不改变角色固定特性（4个预定义角色不可增删改）
- 不支持自定义角色或角色属性修改
- 不提供权限创建/删除功能（权限通过注解自动生成）
- 不影响SUPER_ADMIN权限绕过机制（SUPER_ADMIN仍绕过权限检查）

## Decisions

### D1: 后端API分层结构

遵循现有DDD四层架构：
- **API层**：`PermissionAdminController` 提供REST接口，使用 `@RequiresPermission` 保护，仅限SUPER_ADMIN
- **应用层**：`PermissionApplicationService` 协调权限分配用例，处理DTO/VO转换
- **领域层**：`PermissionDomainService` 实现权限分配领域逻辑，验证角色和权限有效性
- **基础设施层**：复用现有 `PermissionMapper`, `RoleMapper`, `RolePermissionMapper`，新增 `PermissionRepository` 接口

**理由**：保持与项目架构一致，职责分离清晰。

### D2: 权限查询API设计

- `GET /admin/permissions` - 权限列表查询，支持分页、搜索、按权限格式筛选
- `GET /admin/permissions/{permissionId}` - 权限详情，包含已分配角色列表
- `GET /admin/permissions/tree` - 权限树形结构，按 `:` 分隔符构建层级

**权限格式处理**：权限标识符格式不统一（`resource:action`, `resource:subresource:action`, `resource-action:action`），按 `:` 和 `-` 分隔符构建树形结构。

### D3: 角色权限管理API设计

- `GET /admin/roles/{roleName}/permissions` - 获取角色当前权限列表
- `POST /admin/roles/{roleName}/permissions/batch` - 批量分配权限给角色
- `DELETE /admin/roles/{roleName}/permissions/batch` - 批量从角色移除权限

**角色标识**：使用角色名（`SUPER_ADMIN`, `DIRECTION_ADMIN`, `MEMBER`, `CANDIDATE`）而非ID，前端只知道角色名。

### D4: 权限角色管理API设计

- `GET /admin/permissions/{permissionId}/roles` - 获取拥有该权限的角色列表
- `POST /admin/permissions/{permissionId}/roles/batch` - 批量添加角色到权限
- `DELETE /admin/permissions/{permissionId}/roles/batch` - 批量从权限移除角色

**双向管理**：提供权限->角色的管理视角，方便按权限控制访问。

### D5: 前端页面结构

管理后台新增两个页面：
1. **角色权限管理** (`/admin/permissions/role`) - 左侧角色选择，右侧权限树，支持搜索和批量操作
2. **权限角色管理** (`/admin/permissions/permission`) - 左侧权限树/列表，右侧角色多选框

**集成方式**：添加到管理后台导航菜单，权限管理作为顶级菜单项。

### D6: 权限树组件设计

权限树组件特性：
- 按 `:` 分隔符构建层级结构（如 `assessment:create` → 节点 `assessment` → 子节点 `create`）
- 支持搜索高亮和过滤
- 支持全选/取消全选某个节点及其子节点
- 支持展开/折叠层级
- 已分配权限与未分配权限视觉区分

### D7: 批量操作事务性

批量分配/移除操作在单个事务中执行，确保数据一致性。失败时全部回滚。

### D8: SUPER_ADMIN特殊处理

SUPER_ADMIN角色在权限分配界面中可见，但：
- 不能通过界面为SUPER_ADMIN分配/移除权限（因为SUPER_ADMIN绕过权限检查）
- 权限分配操作本身受权限注解保护，需要SUPER_ADMIN权限
- 前端对SUPER_ADMIN角色显示提示信息

## Risks / Trade-offs

- **权限树性能**：权限数量可能较多（100+），树形构建和渲染需要优化。采用懒加载或虚拟滚动。
- **权限格式不一致**：权限标识符格式多样，树形结构可能不够规整。接受现有格式，按通用规则处理。
- **SUPER_ADMIN权限绕过**：SUPER_ADMIN绕过权限检查，但权限分配界面仍显示其角色。明确区分权限校验和权限管理。
- **并发修改**：多个SUPER_ADMIN同时修改权限可能冲突。权限修改频率低，冲突概率小，暂不加锁。
- **前端复杂性**：权限树组件和双向管理界面复杂度较高。复用Ant Design组件，分阶段实现。