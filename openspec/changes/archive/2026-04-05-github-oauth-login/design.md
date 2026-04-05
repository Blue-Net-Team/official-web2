## Context

当前系统使用学号+密码登录，JWT Token 存储在 HttpOnly Cookie 中，配合 CSRF Token 防护。前端登录页已预留 GitHub 登录按钮（当前点击提示"暂未开放"），后端 `tb_user` 表已有 `github_id` 和 `github_username` 字段。

GitHub OAuth App 已在组织级别创建，Client ID 为 `Ov23liUx8DSqRIo6WT86`，Client Secret 存储在 `.env` 中。

## Goals / Non-Goals

**Goals:**

- 实现完整的 GitHub OAuth2 授权码流程（Authorization Code Flow）
- 已登录用户可在个人设置中绑定/解绑 GitHub 账号
- 绑定后可通过 GitHub 直接登录（复用现有 JWT Cookie 认证机制）
- 前后端完整实现

**Non-Goals:**

- 不支持通过 GitHub 直接注册新账号（必须先有学号账号）
- 不同步 GitHub 邮箱到用户 email 字段
- 不实现 GitHub App（Webhook、仓库操作等功能）
- 不支持 GitHub 组织成员自动同步

## Decisions

### Decision 1: 手动实现 OAuth 流程 vs 使用 Spring Security OAuth2 Client

**选择：手动实现 OAuth 流程（使用 RestClient 调用 GitHub API）**

理由：
- 我们的认证体系是自定义的 JWT Cookie 方案，不是标准 Spring Security Session
- Spring Security OAuth2 Client 会引入大量自动配置，与现有自定义安全链冲突
- 我们只需要两个 GitHub API 调用（换 token + 获取用户信息），手写更可控
- 避免引入 `spring-boot-starter-oauth2-client` 的重量级依赖

### Decision 2: OAuth 回调直接走后端接口

**选择：Callback URL 指向后端接口 `GET /api/v1/auth/github/callback`**

理由：
- JWT Token 由后端设置到 HttpOnly Cookie，前端无法直接操作
- 后端完成 OAuth 换码 → 获取用户信息 → 设置 Cookie → 302 重定向到前端
- 与现有学号登录的 Cookie 设置逻辑一致

流程：
```
前端点击 → window.location.href 到 GitHub 授权页
    → GitHub 回调后端 /api/v1/auth/github/callback?code=xxx
    → 后端完成认证，设置 Cookie
    → 302 重定向到前端 /login?github=success 或 /login?github=failed
```

### Decision 3: state 参数防 CSRF

**选择：使用 state 参数防止 CSRF 攻击**

理由：
- OAuth 回调容易受到 CSRF 攻击（攻击者构造恶意 callback URL）
- state 参数在发起授权时生成，回调时验证，确保请求来自同一用户
- state 值使用随机 UUID，存储在 Redis 中（TTL 10 分钟），回调时取出验证并删除

### Decision 4: 前端绑定页面设计

**选择：在个人设置页的邮箱绑定下方新增 GitHub 绑定区域**

理由：
- 功能性质类似（都是外部账号关联），放在一起符合用户心智模型
- 不需要单独页面，一个设置卡片即可
- 展示：当前绑定状态（已绑定显示 GitHub 用户名 + 解绑按钮 / 未绑定显示绑定按钮）

## Risks / Trade-offs

- **[GitHub API 限流]** → GitHub API 未认证请求 60 次/小时，认证后 5000 次/小时。我们使用 OAuth token 调用 API，限流足够
- **[GitHub 账号被一个用户绑定后，其他用户无法绑定同一个]** → 在绑定接口中校验 githubId 唯一性，返回友好提示
- **[用户更改 GitHub 用户名]** → 每次登录时更新 githubUsername 字段
- **[Client Secret 泄露]** → 仅存储在后端 .env 文件中，不提交到 Git，不暴露给前端
