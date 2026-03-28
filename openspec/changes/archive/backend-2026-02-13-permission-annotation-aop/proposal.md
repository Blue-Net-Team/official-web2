# 权限注解AOP系统设计提案

## Why

当前项目需要一个灵活且安全的权限控制系统，支持基于角色的访问控制（RBAC）。随着系统功能增加，手动维护权限与接口的映射关系变得困难且容易出错。通过注解驱动的方式自动扫描和同步权限，可以降低开发成本，确保权限定义与代码同步，并提供统一的权限校验机制。

## What Changes

### 新增功能

1. **权限注解 `@Permission`**
   - 支持 `value`（权限唯一标识，如 `user:create`）
   - 支持 `name`（权限显示名称）
   - 支持 `access`（访问级别：PUBLIC/AUTHENTICATED/PROTECTED）

2. **启动时权限扫描器 `PermissionScanner`**
   - 自动扫描所有 Controller 方法上的 `@Permission` 注解
   - 提取 URL 路径和 HTTP 方法
   - 批量同步到数据库（INSERT/UPDATE）
   - 物理删除代码中已不存在的权限（幽灵数据清理）

3. **AOP 权限拦截器 `PermissionAspect`**
   - 拦截所有带 `@Permission` 注解的请求
   - 根据访问级别执行不同校验逻辑：
     - PUBLIC：直接放行
     - AUTHENTICATED：仅检查是否登录
     - PROTECTED：检查用户角色是否拥有该权限
   - 孤儿权限（无角色关联）视为公开访问

4. **全局无注解拦截**（可选安全增强）
   - 无 `@Permission` 注解的 `/api/**` 接口默认返回 403
   - 登录相关接口（`/login`, `/register` 等）除外

5. **初始角色与权限数据**
   - 添加 CANDIDATE（考生）角色
   - 提供基础权限初始化 SQL

### 文件权限处理

文件接口（`/files/{id}`）不使用 `@Permission` 注解控制，在业务层根据文件类型动态判断权限：
- work（考生答案）：考生本人或团队成员以上可访问
- evaluation_attachment（考题附件）：同方向用户可访问
- avatar（头像）：团队成员以上或本人可访问
- normal_img/qrcode：公开访问

## Capabilities

### New Capabilities

- `permission-annotation`: 权限注解定义与元数据支持
- `permission-scanning`: 启动时自动扫描和同步权限到数据库
- `permission-aop-interceptor`: AOP 权限校验拦截器
- `rbac-role-management`: 基于角色的权限控制（新增 CANDIDATE 角色）

### Modified Capabilities

无现有 capabilities 需要修改

## Impact

### 受影响组件

- **Controller 层**：需要为受保护接口添加 `@Permission` 注解
- **数据库**：Permission、Role、RolePermission 表
- **启动流程**：增加权限扫描阶段（约几百毫秒）
- **文件接口**：保持现有业务逻辑判断，不添加注解

### 兼容性

- **无注解接口**：默认公开访问（或通过全局拦截器拒绝）
- **现有权限数据**：通过扫描自动更新
- **角色体系**：新增 CANDIDATE 角色，不影响现有 SUPER_ADMIN、DIRECTION_ADMIN、MEMBER 角色

### 性能考虑

- 权限数据缓存到内存（启动时加载），避免每次请求查询数据库
- 扫描过程使用批量操作，减少数据库往返

## 风险评估

- **幽灵数据删除**：物理删除策略无法回滚，需确保代码版本与数据库同步
- **权限遗漏**：开发者忘记添加 `@Permission` 注解可能导致接口意外公开（可通过全局拦截器缓解）
- **文件权限**：复杂的业务逻辑判断需要仔细测试，避免权限绕过
