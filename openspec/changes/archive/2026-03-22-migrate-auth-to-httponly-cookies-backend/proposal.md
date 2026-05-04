## Why

当前认证系统使用 localStorage 存储 JWT token，存在 XSS 攻击窃取 token 的安全风险。根据 Next.js 和现代 Web 安全最佳实践，应将认证 token 迁移到 HttpOnly Cookie 存储，并配合 CSRF Token 保护状态修改请求。

## What Changes

- **BREAKING**: 登录接口不再在响应体中返回 `token` 字段，改为通过 `Set-Cookie` header 设置 HttpOnly Cookie
- 新增 `GET /auth/me` 接口，用于检查当前登录状态并返回用户信息和 CSRF Token
- 登录接口响应体新增 `csrfToken` 字段
- 登出接口通过 `Set-Cookie` 清除 Cookie
- JWT 认证过滤器支持从 Cookie 读取 token（兼容 Header 方式过渡期后移除）
- CSRF Token 验证：对需要认证的 POST/PUT/DELETE/PATCH 请求验证 CSRF Token
- Cookie 配置：`HttpOnly=true`, `SameSite=Lax`, 生产环境 `Secure=true`, 跨子域共享 `Domain=.example.com`

## Capabilities

### New Capabilities

- `auth-session`: 基于 HttpOnly Cookie 的认证会话管理，包括登录状态检查、CSRF Token 生成与验证
- `csrf-protection`: CSRF Token 保护机制，防止跨站请求伪造攻击

### Modified Capabilities

- `jwt-authentication`: JWT 认证方式变更，支持从 Cookie 读取 token，配合 CSRF 保护

## Impact

**后端代码模块**:
- `AuthController`: 登录/登出接口修改，新增 `/auth/me` 接口
- `JwtAuthenticationFilter`: 支持从 Cookie 读取 token
- `SecurityConfig`: CORS 配置调整，CSRF 验证过滤器
- `AuthService`: 生成 CSRF Token，Cookie 操作
- 新增 `CsrfTokenFilter`: CSRF Token 验证过滤器
- 新增 `CookieService`: Cookie 操作封装

**配置变更**:
- CORS allowedOrigins 需要配置具体域名（不能用 `*`）
- 新增 Cookie 相关配置项（domain, secure 等）

**依赖变更**:
- 前端需要配合修改（参见前端变更 `migrate-auth-to-httponly-cookies`）

**API 变更**:
- `POST /auth/login`: 响应体移除 `token`，新增 `csrfToken`
- `POST /auth/logout`: 设置清除 Cookie 的响应头
- `GET /auth/me`: 新增接口，返回 `{ userInfo, csrfToken }`
