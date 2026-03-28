---
alwaysApply: false
description: 涉及认证授权、登录权限、CSRF、JWT 时使用
---

# 认证安全规范

使用 **HttpOnly Cookie + CSRF Token** 认证方案。

## Cookie 配置

| Cookie | 属性 | 用途 |
|--------|------|------|
| `auth_token` | HttpOnly, Secure, SameSite=Lax | JWT Token |
| `csrf_token` | Secure, SameSite=Lax | CSRF 防护 |

## CSRF 防护

- 验证范围：POST/PUT/DELETE/PATCH（已登录用户）
- 验证方式：Double Submit Cookie
- 白名单：`/api/v1/auth/login/**`、`/api/v1/enrollments`、`/api/v1/file/upload/avatar`

## 认证流程

1. **登录** `POST /api/v1/auth/login/student-id` → 设置 Cookie，返回 `{ csrfToken, userInfo }`
2. **状态** `GET /api/v1/auth/me` → 返回 `{ authenticated, userInfo, csrfToken }`
3. **登出** `POST /api/v1/auth/logout` → 清除 Cookie

## 前端对接

- 请求携带：`withCredentials: true` / `credentials: 'include'`
- 状态修改 Header：`X-CSRF-Token`
