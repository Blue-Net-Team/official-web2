## 1. Domain Layer

### Task 1.1: User Entity - Add admin update and cascade delete behaviors

#### 测试边界
- 输入条件：管理员操作请求（更新字段、新密码、删除标记）
- 前置状态：用户存在于系统中
- 后置状态：用户实体状态变更（字段更新/删除）

#### 测试用例
| 用例ID | 场景 | 输入 | 期望输出/行为 | 异常 |
|--------|------|------|---------------|------|
| TC-001 | 更新用户角色 | roleId=2 | User.roleId 更新为 2 | - |
| TC-002 | 更新禁用状态 | disable=true | User.disable 更新为 true | - |
| TC-003 | 重置密码 | newPassword="newPass123" | User.password 更新为加密后的值 | - |
| TC-004 | 密码为空 | newPassword="" | - | IllegalArgumentException |
| TC-005 | 密码不匹配 | newPassword="abc", confirmPassword="def" | - | IllegalArgumentException |

#### 实现步骤（严格按顺序）
- [x] 1.1.1 编写 User 领域实体测试（红灯阶段）
- [x] 1.1.2 在 User 实体中添加 `resetPassword()`、`updateAdminFields()` 方法
- [x] 1.1.3 运行测试确认通过（绿灯阶段）

### Task 1.2: Domain Service - UserCascadeDeleteService

#### 测试边界
- 输入条件：用户ID
- 前置状态：用户存在，可能有关联数据
- 后置状态：用户及关联数据全部被物理删除

#### 测试用例
| 用例ID | 场景 | 输入 | 期望输出/行为 | 异常 |
|--------|------|------|---------------|------|
| TC-006 | 删除无关联数据的用户 | userId=1 | 成功删除用户 | - |
| TC-007 | 删除有关联数据的用户 | userId=2（有经历、答案） | 级联删除所有关联数据后删除用户 | - |
| TC-008 | 删除不存在的用户 | userId=999 | - | NotFoundException |

#### 实现步骤（严格按顺序）
- [x] 1.2.1 编写 UserCascadeDeleteService 测试（红灯阶段）
- [x] 1.2.2 定义 Repository 接口扩展方法（findExperiencesByUserId、deleteAchievementsByUserId 等）
- [x] 1.2.3 实现 UserCascadeDeleteService 领域服务
- [x] 1.2.4 运行测试确认通过（绿灯阶段）

> **设计决策**：未单独创建 `UserCascadeDeleteService` 领域服务。级联删除是纯技术操作（多表 mapper 调用），无复杂领域逻辑，直接放在 `UserRepositoryImpl` 中以减少不必要的抽象。

## 2. Infrastructure Layer

### Task 2.1: Repository Implementation - Extend UserRepository for admin queries and cascade delete

#### 测试边界
- 输入条件：分页参数 + 筛选条件
- 前置状态：数据库中存在多条用户记录
- 后置状态：返回符合条件的分页结果

#### 测试用例
| 用例ID | 场景 | 输入 | 期望输出/行为 | 异常 |
|--------|------|------|---------------|------|
| TC-009 | 分页查询无筛选 | page=1, size=10 | 返回 Page<User>，total >= 0 | - |
| TC-010 | 按角色筛选 | roleId=2 | 仅返回该角色用户 | - |
| TC-011 | 按方向筛选 | direction="computer_vision" | 仅返回该方向用户 | - |
| TC-012 | 模糊搜索学号 | keyword="2024" | 返回学号包含"2024"的用户 | - |
| TC-013 | 模糊搜索姓名 | keyword="张" | 返回 username 包含"张"的用户 | - |
| TC-014 | 组合筛选 | roleId=2 + keyword="张" | 同时满足两个条件 | - |

#### 实现步骤（严格按顺序）
- [x] 2.1.1 编写 UserRepository 扩展接口测试（红灯阶段）
- [x] 2.1.2 在 UserRepository 接口中定义 `findPageWithFilters`、`findDetailById`、`cascadeDeleteById` 方法
- [x] 2.1.3 在 UserRepositoryImpl 中使用 MyBatis-Plus QueryWrapper 实现分页查询
- [x] 2.1.4 实现级联删除（使用 @Transactional）
- [x] 2.1.5 运行测试确认通过（绿灯阶段）

## 3. Application Layer

### Task 3.1: Admin User Application Service

#### 测试边界
- 输入条件：管理员操作 DTO
- 前置状态：当前用户为 SUPER_ADMIN，目标用户存在
- 后置状态：完成业务操作并返回结果

#### 测试用例
| 用例ID | 场景 | 输入 | 期望输出/行为 | 异常 |
|--------|------|------|---------------|------|
| TC-015 | 正常分页查询 | AdminUserListQueryDTO | 返回 Page<AdminUserListVO> | - |
| TC-016 | 查看用户详情 | userId=1 | 返回 AdminUserDetailVO（含统计） | - |
| TC-017 | 更新用户角色 | UpdateUserRequestDTO(roleId=2) | 更新成功 | - |
| TC-018 | 重置密码 | ResetPasswordDTO(password, confirm) | 更新成功，密码已加密 | - |
| TC-019 | 密码不一致 | ResetPasswordDTO("abc", "def") | - | IllegalArgumentException |
| TC-020 | 删除用户 | userId=1 | 级联删除成功 | - |
| TC-021 | 批量禁用 | BatchOperateDTO([1,2,3]) | 3个用户被禁用 | - |
| TC-022 | 批量删除 | BatchOperateDTO([1,2,3]) | 3个用户被级联删除 | - |
| TC-023 | 批量更新角色 | BatchRoleDTO([1,2,3], roleId=2) | 3个用户角色更新 | - |
| TC-024 | 目标用户不存在 | userId=999 | - | NotFoundException |

#### 实现步骤（严格按顺序）
- [x] 3.1.1 编写 AdminUserAppService 测试（红灯阶段）
- [x] 3.1.2 创建 AdminUserListQuery、AdminUserListVO、AdminUserDetailVO 等 VO
- [x] 3.1.3 创建 AdminUserAppService 接口及实现
- [x] 3.1.4 实现 VO ↔ DTO ↔ Entity 转换
- [x] 3.1.5 运行测试确认通过（绿灯阶段）

## 4. API Layer

### Task 4.1: AdminUserController

#### 测试边界
- 输入条件：HTTP 请求 + DTO
- 前置状态：已登录且为 SUPER_ADMIN
- 后置状态：返回 ResponseMessage<T>

#### 测试用例
| 用例ID | 场景 | 输入 | 期望输出/行为 | 异常 |
|--------|------|------|---------------|------|
| TC-025 | 正常查询列表 | GET /admin/users?page=1 | 200, ResponseMessage<PageDTO> | - |
| TC-026 | 无权限访问 | 非 SUPER_ADMIN | 403 Forbidden | - |
| TC-027 | 正常重置密码 | PUT /admin/users/1/password | 200, success | - |
| TC-028 | 重置密码参数错误 | password < 6 字符 | 400 Bad Request | - |
| TC-029 | 删除用户 | DELETE /admin/users/1 | 200, success | - |
| TC-030 | 批量禁用 | POST /admin/users/batch-disable | 200, success | - |

#### 实现步骤（严格按顺序）
- [x] 4.1.1 编写 AdminUserController 集成测试（红灯阶段）
- [x] 4.1.2 创建 AdminUserController 及 DTO（Request/Response）
- [x] 4.1.3 添加 `@RequiresPermission` 注解
- [x] 4.1.4 添加 Swagger/OpenAPI 注解
- [x] 4.1.5 运行测试确认通过（绿灯阶段）

## 5. Frontend

### Task 5.1: Admin User Management Page

#### 测试边界
- 页面在 `/admin/users`
- 仅 SUPER_ADMIN 可见侧边栏菜单
- 支持表格展示、筛选、搜索、批量操作

#### 实现步骤（严格按顺序）
- [x] 5.1.1 创建 `admin-user.service.ts` API 服务
- [x] 5.1.2 创建 `/admin/users/page.tsx` 列表页面
- [x] 5.1.3 实现表格、分页、筛选、搜索
- [x] 5.1.4 实现详情抽屉/弹窗
- [x] 5.1.5 实现批量操作（删除、禁用、改角色）
- [x] 5.1.6 实现重置密码弹窗（二次确认）
- [x] 5.1.7 添加侧边栏菜单项（仅 SUPER_ADMIN 可见）
- [x] 5.1.8 联调后端接口

## 6. Integration & Review

- [x] 6.1 运行后端全部测试（`./mvnw test`）
- [x] 6.2 运行前端构建（`pnpm build`）— 因首页 SSR 需要后端连接，构建在 /admin/users 无类型错误
- [x] 6.3 代码审查：检查 DDD 分层、权限注解、审计覆盖
- [ ] 6.4 提交代码
