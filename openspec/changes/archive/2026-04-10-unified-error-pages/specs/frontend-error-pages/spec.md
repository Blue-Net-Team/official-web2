## ADDED Requirements

### Requirement: ErrorPageConfig 类型定义
系统 SHALL 定义 `ErrorPageConfig` 类型，包含三个字段：`icon`（ReactNode，大图标）、`statusCode`（number，HTTP 错误码）、`description`（string，描述文案）。系统 SHALL 提供预定义的配置集合 `ERROR_CONFIGS`，以 statusCode 为 key，至少包含 403 和 404 的配置。

#### Scenario: 查找 404 配置
- **WHEN** 通过 `ERROR_CONFIGS[404]` 查找配置
- **THEN** 返回包含 statusCode=404、icon 为 FileSearchOutlined 图标、description 为"您访问的页面不存在"的配置对象

#### Scenario: 查找 403 配置
- **WHEN** 通过 `ERROR_CONFIGS[403]` 查找配置
- **THEN** 返回包含 statusCode=403、icon 为 StopOutlined 图标、description 为"您没有权限访问此页面"的配置对象

### Requirement: 共享 ErrorPage 组件
系统 SHALL 提供共享的 `ErrorPage` 组件，接受 `config: ErrorPageConfig` 作为 props，渲染居中布局的错误页面：垂直居中排列大图标、错误码数字、描述文案。组件 SHALL 为 Client Component。

#### Scenario: 渲染 404 错误页
- **WHEN** 传入 `config={ERROR_CONFIGS[404]}`
- **THEN** 页面垂直居中显示 FileSearchOutlined 图标、数字 404、"您访问的页面不存在"文案

#### Scenario: 渲染 403 错误页
- **WHEN** 传入 `config={ERROR_CONFIGS[403]}`
- **THEN** 页面垂直居中显示 StopOutlined 图标、数字 403、"您没有权限访问此页面"文案

### Requirement: 根级 404 页面
系统 SHALL 在 `app/not-found.tsx` 提供自定义 404 页面，替换 Next.js 默认页面。页面 SHALL 包含 PublicNavBar 和 ErrorPage(404) 组件。当用户访问不存在的路由或 `notFound()` 被调用时 SHALL 显示此页面。

#### Scenario: 访问不存在的路由
- **WHEN** 用户访问系统中不存在的 URL（如 `/abc`）
- **THEN** 显示带 PublicNavBar 的 404 错误页面

#### Scenario: 代码调用 notFound()
- **WHEN** 页面组件中调用 `notFound()` 函数（如 direction/[slug] 中无效 slug）
- **THEN** 显示带 PublicNavBar 的 404 错误页面

### Requirement: Admin 403 权限拦截
系统 SHALL 在 admin layout 中检查用户角色等级。当用户 `roleLevel < 1`（CANDIDATE 或未登录）时，SHALL 渲染 PublicNavBar + ErrorPage(403) 替代 admin 布局内容。当 `roleLevel >= 1`（MEMBER 及以上）时 SHALL 正常渲染 admin 布局。角色等级通过已有的 `getRoleLevel` 函数和 Zustand authStore 中的 `roleName` 获取。

#### Scenario: 未登录用户访问 admin
- **WHEN** 未登录用户（无 roleName）访问 `/admin/*`
- **THEN** getRoleLevel 返回 -1，页面显示 PublicNavBar + 403 错误页

#### Scenario: CANDIDATE 访问 admin
- **WHEN** CANDIDATE（roleLevel=0）用户访问 `/admin/*`
- **THEN** 页面显示 PublicNavBar + 403 错误页

#### Scenario: MEMBER 访问 admin
- **WHEN** MEMBER（roleLevel=1）用户访问 `/admin/*`
- **THEN** 正常渲染 admin 布局（AdminHeadBar + AdminSideBar + Content）

#### Scenario: SUPER_ADMIN 访问 admin
- **WHEN** SUPER_ADMIN（roleLevel=3）用户访问 `/admin/*`
- **THEN** 正常渲染 admin 布局
