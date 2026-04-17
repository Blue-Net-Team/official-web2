## ADDED Requirements

### Requirement: 权限列表查询
系统 SHALL 提供权限列表查询接口，支持分页、搜索和权限格式筛选，返回权限详情及已分配角色信息。

#### Scenario: 分页查询权限列表
- **WHEN** SUPER_ADMIN 用户请求 `GET /admin/permissions?page=1&size=20`
- **THEN** 返回第1页共20条权限记录，每条包含 `id`、`permission`、`name`、`description`、`accessLevel`、`createdAt` 字段

#### Scenario: 搜索权限标识符
- **WHEN** SUPER_ADMIN 用户请求 `GET /admin/permissions?keyword=assessment`
- **THEN** 返回权限标识符包含 "assessment" 的所有权限（如 `assessment:create`、`assessment:delete`）

#### Scenario: 按权限格式筛选
- **WHEN** SUPER_ADMIN 用户请求 `GET /admin/permissions?format=resource:action`
- **THEN** 返回权限格式为 "resource:action" 的所有权限（如 `user:create`、`file:delete`）

#### Scenario: 权限详情包含已分配角色
- **WHEN** SUPER_ADMIN 用户请求 `GET /admin/permissions/{permissionId}`
- **THEN** 返回权限详情，包含 `assignedRoles` 字段，列出拥有该权限的角色名列表

### Requirement: 权限树形结构
系统 SHALL 提供权限树形结构接口，按权限标识符分隔符（`:` 和 `-`）构建层级树。

#### Scenario: 获取权限树
- **WHEN** SUPER_ADMIN 用户请求 `GET /admin/permissions/tree`
- **THEN** 返回树形结构，如 `assessment` 节点包含子节点 `create`、`read`、`update`、`delete`

#### Scenario: 多级权限树
- **WHEN** 权限标识符为 `assessment:question:create`
- **THEN** 树形结构中 `assessment` → `question` → `create` 三级结构

### Requirement: 角色权限管理
系统 SHALL 提供角色权限管理接口，支持查询角色当前权限、批量分配权限、批量移除权限。

#### Scenario: 查询角色权限
- **WHEN** SUPER_ADMIN 用户请求 `GET /admin/roles/{roleName}/permissions`
- **THEN** 返回该角色当前拥有的所有权限列表

#### Scenario: 批量分配权限给角色
- **WHEN** SUPER_ADMIN 用户请求 `POST /admin/roles/{roleName}/permissions/batch`，body 包含权限ID数组
- **THEN** 系统为指定角色添加所有指定权限，返回成功数量

#### Scenario: 批量从角色移除权限
- **WHEN** SUPER_ADMIN 用户请求 `DELETE /admin/roles/{roleName}/permissions/batch`，body 包含权限ID数组
- **THEN** 系统从指定角色移除所有指定权限，返回成功数量

#### Scenario: SUPER_ADMIN角色特殊处理
- **WHEN** 尝试为 SUPER_ADMIN 角色分配或移除权限
- **THEN** 返回错误提示：SUPER_ADMIN 角色绕过权限检查，无需分配权限

### Requirement: 权限角色管理
系统 SHALL 提供权限角色管理接口，支持查询权限对应角色、批量添加角色、批量移除角色。

#### Scenario: 查询权限对应角色
- **WHEN** SUPER_ADMIN 用户请求 `GET /admin/permissions/{permissionId}/roles`
- **THEN** 返回拥有该权限的所有角色名列表

#### Scenario: 批量添加角色到权限
- **WHEN** SUPER_ADMIN 用户请求 `POST /admin/permissions/{permissionId}/roles/batch`，body 包含角色名数组
- **THEN** 系统为指定权限添加所有指定角色，返回成功数量

#### Scenario: 批量从权限移除角色
- **WHEN** SUPER_ADMIN 用户请求 `DELETE /admin/permissions/{permissionId}/roles/batch`，body 包含角色名数组
- **THEN** 系统从指定权限移除所有指定角色，返回成功数量

### Requirement: 前端权限管理界面
系统 SHALL 提供前端权限管理界面，集成到管理后台导航菜单，仅对SUPER_ADMIN用户可见。

#### Scenario: 导航菜单添加权限管理
- **WHEN** SUPER_ADMIN 用户登录后访问管理后台
- **THEN** 侧边栏导航菜单显示"权限管理"菜单项，包含子菜单"角色权限"和"权限角色"

#### Scenario: 角色权限管理页面
- **WHEN** SUPER_ADMIN 用户访问 `/admin/permissions/role`
- **THEN** 显示角色权限管理页面：左侧角色选择器（4个固定角色），右侧权限树组件

#### Scenario: 权限角色管理页面
- **WHEN** SUPER_ADMIN 用户访问 `/admin/permissions/permission`
- **THEN** 显示权限角色管理页面：左侧权限树/列表，右侧角色多选框

### Requirement: 权限树组件
系统 SHALL 提供权限树组件，支持层级展示、搜索过滤、节点选择和批量操作。

#### Scenario: 权限树层级展示
- **WHEN** 权限树组件加载权限数据
- **THEN** 按权限标识符分隔符构建树形结构，支持展开/折叠节点

#### Scenario: 权限树搜索过滤
- **WHEN** 用户在权限树搜索框输入关键词
- **THEN** 只显示匹配的节点，父节点自动展开以显示匹配的子节点

#### Scenario: 节点选择和批量操作
- **WHEN** 用户选择权限树节点
- **THEN** 支持全选/取消全选某个节点及其所有子节点，支持批量分配/移除操作

#### Scenario: 已分配权限视觉区分
- **WHEN** 查看角色权限时
- **THEN** 已分配给该角色的权限在树中高亮显示或标记选中状态

### Requirement: 安全性控制
所有权限管理操作 SHALL 仅限 SUPER_ADMIN 角色访问，使用现有权限注解体系保护。

#### Scenario: 非SUPER_ADMIN用户访问权限管理API
- **WHEN** DIRECTION_ADMIN 用户尝试访问 `GET /admin/permissions`
- **THEN** 权限切面抛出 `ForbiddenException`，返回 403 状态码

#### Scenario: 非SUPER_ADMIN用户访问权限管理页面
- **WHEN** MEMBER 用户尝试访问 `/admin/permissions/role`
- **THEN** 页面显示无权限访问或重定向到无权限页面

#### Scenario: API权限注解保护
- **WHEN** 查看权限管理Controller
- **THEN** 所有方法使用 `@RequiresPermission(value="permission:manage", name="权限管理", access=AccessLevel.PROTECTED)` 注解

### Requirement: 数据一致性
权限分配操作 SHALL 在单个事务中执行，确保数据一致性。

#### Scenario: 批量分配事务性
- **WHEN** SUPER_ADMIN 批量分配10个权限给角色，第5个权限ID不存在
- **THEN** 所有操作回滚，不分配任何权限，返回错误信息

#### Scenario: 批量移除事务性
- **WHEN** SUPER_ADMIN 批量从角色移除10个权限，中间发生数据库异常
- **THEN** 所有操作回滚，权限状态保持不变，返回错误信息