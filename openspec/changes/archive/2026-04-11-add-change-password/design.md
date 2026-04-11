## Context

系统已有完整的"忘记密码"流程（`ResetPasswordService` + `ResetPasswordStateService`），面向未登录用户通过邮箱验证重置密码。现在需要为已登录用户提供"修改密码"功能。导航栏已预留入口（`/change-password`），但页面和后端接口均未实现。

底层基础设施已就绪：`UserRepository.updatePassword()`、`UserMapper.updatePassword()`、`PasswordEncoder`、`AuthTokenService.revokeAllUserTokens()`。

## Goals / Non-Goals

**Goals:**
- 已登录用户通过验证原密码安全地修改密码
- 两步向导式交互（Ant Design Steps）：验证原密码 → 设置新密码
- 修改成功后强制重新登录（吊销所有 Token）
- 与忘记密码流程复用相同的 Redis 状态管理模式

**Non-Goals:**
- 不修改忘记密码流程的任何代码
- 不新增数据库表或迁移脚本
- 不支持邮箱/手机验证码修改密码（仅原密码验证）

## Decisions

### 1. Redis 状态管理 — 参照 `ResetPasswordStateService` 模式

**选择**：新建 `ChangePasswordStateService`，使用 `StringRedisTemplate` 管理 `change_pwd:{token}` Hash。

**理由**：与 `ResetPasswordStateService` 保持一致的架构风格。两步流程需要状态追踪，防止用户跳过验证步骤直接提交新密码。

**Key 结构**：`change_pwd:{token}`（token = UUID）
**TTL**：15 分钟，每次操作刷新
**Hash 字段**：`userId`、`step`（"1"=已验证）、`verified`（"true"）

### 2. 两个独立接口而非一个

**选择**：`POST /verify` + `PUT /password`，而非单接口 `PUT /password`（一次性提交旧密码+新密码）。

**理由**：前端是分步向导，第一步验证原密码后才能进入第二步。两个接口让每一步独立校验，用户体验更好（原密码错误时不需要填写新密码）。

### 3. 接口放在 `UserProfileController` 下

**选择**：路径为 `/api/v1/user/password/verify` 和 `/api/v1/user/password`，归属 `UserProfileController`。

**理由**：这是已登录用户操作自己账号的功能，语义上属于"用户资料管理"，不是"认证"（auth）范畴。`AuthController` 负责登录/登出/OAuth，`ResetPasswordController` 负责未登录的密码重置。

### 4. 页面布局 — 应用内页面而非全屏分割

**选择**：使用 `(public)/(other)` 路由组，保留顶部 NavBar，内容区居中显示表单卡片。

**理由**：修改密码是从 NavBar 下拉菜单进入的，用户仍处于已登录的应用环境中。不应像登录/忘记密码页面那样使用全屏分割布局。

### 5. 权限级别 — `AUTHENTICATED`

**选择**：两个接口均使用 `@RequiresPermission(access = AccessLevel.AUTHENTICATED)`。

**理由**：任何已登录用户都可以修改自己的密码，不需要特定权限。

## Risks / Trade-offs

- **[Redis Key 过期]** → 用户在第一步验证后停留超过 15 分钟，第二步提交时 token 失效。前端应处理 401/过期错误，提示用户重新开始流程。
- **[并发修改]** → 用户在多个设备上同时修改密码。后端使用原子性操作更新密码 + 吊销 Token，不存在竞态问题。
- **[暴力破解原密码]** → 验证接口可能被用于暴力尝试。后续可考虑添加频率限制（Rate Limit），但当前不在范围内。
