## ADDED Requirements

### Requirement: Verify current password
系统 SHALL 提供接口 `POST /api/v1/user/password/verify`，允许已登录用户验证当前密码。验证通过后，系统 SHALL 在 Redis 中创建状态 token 并返回给前端。

#### Scenario: 密码验证成功
- **WHEN** 已登录用户提交正确的当前密码
- **THEN** 系统创建 Redis key `change_pwd:{uuid}`（TTL 15 分钟），存储 userId、step=1、verified=true，返回 `{ token }`

#### Scenario: 密码验证失败
- **WHEN** 已登录用户提交错误的当前密码
- **THEN** 系统返回错误响应"当前密码不正确"，不创建 Redis 状态

#### Scenario: 未登录用户访问
- **WHEN** 未认证用户调用此接口
- **THEN** 系统返回 401 未授权响应

### Requirement: Submit new password
系统 SHALL 提供接口 `PUT /api/v1/user/password`，允许已登录用户通过验证 token 提交新密码。新密码 MUST 至少 8 个字符。

#### Scenario: 密码修改成功
- **WHEN** 用户提交有效的 token 和符合要求的新密码（两次输入一致）
- **THEN** 系统更新用户密码（编码后存储），吊销该用户所有已登录 Token，删除 Redis key，返回成功响应

#### Scenario: Token 过期或无效
- **WHEN** 用户提交的 token 已过期或不存在
- **THEN** 系统返回错误响应"验证已过期，请重新开始"，不修改密码

#### Scenario: 新密码不一致
- **WHEN** 用户提交的两次新密码不一致
- **THEN** 系统返回错误响应"两次输入的密码不一致"，不修改密码

#### Scenario: 跳过验证步骤
- **WHEN** 用户未完成原密码验证（token 不存在或 step < 1）直接提交新密码
- **THEN** 系统返回错误响应"请先验证当前密码"

### Requirement: Redis state management
系统 SHALL 使用 Redis Hash 管理 `change_pwd:{token}` 的两步流程状态，TTL 为 15 分钟，每次操作刷新 TTL。

#### Scenario: 状态创建
- **WHEN** 原密码验证通过
- **THEN** Redis 存储 `change_pwd:{uuid}` Hash，字段：userId、step="1"、verified="true"，TTL 15 分钟

#### Scenario: 状态消费
- **WHEN** 密码修改成功
- **THEN** Redis key 被删除

#### Scenario: 状态过期
- **WHEN** 15 分钟内未完成第二步
- **THEN** Redis key 自动过期，用户需重新从第一步开始

### Requirement: Change password page
系统 SHALL 在 `/change-password` 路径提供修改密码页面，使用 Ant Design Steps 组件展示两步向导（验证原密码 → 设置新密码）。页面 SHALL 使用与 Profile 页面一致的应用内布局（顶部 NavBar + 内容区）。

#### Scenario: Step 1 - 验证原密码
- **WHEN** 用户进入修改密码页面
- **THEN** 显示 Step 1（激活状态），包含"当前密码"输入框和"下一步"按钮，底部显示"忘记密码？请退出登录后在登录页点击「忘记密码」"提示

#### Scenario: Step 1 - 密码错误
- **WHEN** 用户输入错误的当前密码并点击"下一步"
- **THEN** 显示错误提示"当前密码不正确"，停留在 Step 1

#### Scenario: Step 2 - 设置新密码
- **WHEN** 原密码验证通过
- **THEN** 进入 Step 2，Step 1 显示为完成状态，显示"新密码"和"确认新密码"两个输入框及"确认修改"按钮

#### Scenario: 修改成功
- **WHEN** 用户成功提交新密码
- **THEN** 显示成功提示，自动跳转到登录页，用户需重新登录

#### Scenario: Token 过期
- **WHEN** 用户在 Step 2 停留超过 15 分钟后提交
- **THEN** 显示错误提示"验证已过期，请重新开始"，跳回 Step 1
