## Why

当前前端使用 localStorage 存储 JWT token，存在 XSS 攻击窃取 token 的安全风险。根据 Next.js 和现代 Web 安全最佳实践，应迁移到 HttpOnly Cookie 认证方案，前端通过 `withCredentials` 让浏览器自动管理 Cookie。

## What Changes

- **BREAKING**: 移除所有 localStorage token 存储相关代码
- axios 客户端配置 `withCredentials: true`，让浏览器自动携带 Cookie
- 登录响应处理变更：从响应体获取 `csrfToken`，不再存储 `token`
- 新增 `/auth/me` API 调用，用于页面刷新后恢复登录状态
- 修改请求拦截器：对需要认证的 POST/PUT/DELETE/PATCH 请求添加 `X-CSRF-Token` header
- authStore 简化：移除 `token` 状态，`csrfToken` 存储在内存（不持久化）

## Capabilities

### New Capabilities

- `auth-session-client`: 前端认证会话管理，包括登录状态检查、CSRF Token 管理、Cookie 自动携带

### Modified Capabilities

- `auth-store`: Zustand authStore 简化，移除 token 存储，添加 csrfToken 内存存储

## Impact

**前端代码模块**:
- `src/apis/client.ts`: 添加 `withCredentials: true`，添加 CSRF Token header 拦截器
- `src/apis/services/auth.service.ts`: 新增 `getAuthMe()` 方法
- `src/stores/authStore.ts`: 简化状态，移除 token 持久化
- `src/app/layout.tsx` 或 `_app.tsx`: 应用初始化时检查登录状态

**API 调用变更**:
- 登录: `POST /auth/login` → 响应体包含 `{ userInfo, csrfToken }`，浏览器自动设置 Cookie
- 登出: `POST /auth/logout` → 需要携带 `X-CSRF-Token` header
- 新增: `GET /auth/me` → 返回 `{ userInfo, csrfToken }`

**依赖变更**:
- 后端需要配合修改（参见后端变更 `migrate-auth-to-httponly-cookies`）

**部署顺序**:
1. 先部署后端（向后兼容 Authorization Header）
2. 再部署前端
3. 验证功能正常后，后端移除 Header 认证支持
