## Context

### 当前状态

当前认证流程：
1. 用户登录 → 后端生成 JWT → 响应体返回 `token`
2. 前端将 `token` 存储到 `localStorage`
3. 后续请求通过 `Authorization: Bearer <token>` header 携带
4. 后端 `JwtAuthenticationFilter` 从 header 提取并验证

### 安全风险

| 风险 | localStorage 方案 | HttpOnly Cookie 方案 |
|------|------------------|---------------------|
| XSS 窃取 token | ❌ 容易被窃取 | ✅ JS 无法读取 |
| CSRF 攻击 | ✅ 不受影响 | ⚠️ 需要防护 |

### 部署场景

| 环境 | 前端域名 | 后端域名 | Cookie Domain |
|------|---------|---------|---------------|
| 本地开发 | localhost:3000 | localhost:8080 | 不设置 |
| 开发服务器 | dev.example.com | dev.api.example.com | .example.com |
| 生产环境 | example.com | api.example.com | .example.com |

## Goals / Non-Goals

**Goals:**
- 将 JWT token 从 localStorage 迁移到 HttpOnly Cookie
- 实现 CSRF Token 保护状态修改请求
- 新增 `/auth/me` 接口用于检查登录状态
- 支持跨子域 Cookie 共享

**Non-Goals:**
- Rate limiting（单独变更）
- CAPTCHA 验证（后续按需添加）
- Session 管理（继续使用 JWT 无状态方案）
- OAuth/第三方登录

## Decisions

### D1: Cookie 存储策略

**决定**: 使用双 Cookie 方案
- `auth_token`: HttpOnly Cookie 存储 JWT
- `csrf_token`: 非 HttpOnly Cookie 存储 CSRF Token（同时通过响应体返回）

**理由**:
- HttpOnly 防止 XSS 窃取 JWT
- CSRF Token 需要前端 JS 读取放入请求头
- Double Submit Cookie 模式：Cookie + Header 双重验证

**配置**:
```
auth_token Cookie:
  - HttpOnly: true
  - Secure: true (生产), false (开发)
  - SameSite: Lax
  - Domain: .example.com (生产), 不设置 (本地)
  - Path: /
  - Max-Age: 43200 (12小时，与 JWT 一致)

csrf_token Cookie:
  - HttpOnly: false (JS 需要读取)
  - Secure: true (生产), false (开发)
  - SameSite: Lax
  - Domain: .example.com (生产), 不设置 (本地)
  - Path: /
  - Max-Age: 43200
```

### D2: CSRF Token 传递方式

**决定**: 登录响应体 + `/auth/me` 响应体返回 CSRF Token

**理由**:
- 避免前端解析 `document.cookie` 的复杂性
- 页面刷新后通过 `/auth/me` 重新获取
- Cookie 仍然设置用于后端验证

**替代方案**: 仅从 Cookie 读取
- ❌ 需要处理 Cookie 解析、URL 编码等问题
- ❌ 不同浏览器行为可能不一致

### D3: CSRF 验证范围

**决定**: 仅对需要认证的 POST/PUT/DELETE/PATCH 请求验证

**不验证**:
- GET 请求（幂等）
- 公开接口（如 `POST /enrollments`，`POST /auth/login`）

**理由**:
- 公开接口没有用户身份可被利用，CSRF 攻击无意义
- GET 请求应该是只读的
- 登录接口是认证起点，此时还没有 CSRF Token

### D4: JWT 提取策略

**决定**: 优先从 Cookie 提取，fallback 到 Authorization Header（过渡期后移除）

**理由**:
- 平滑过渡，避免一次性大改动
- 过渡期后移除 Header 方式，强制使用 Cookie

### D5: CORS 配置

**决定**: 使用环境变量配置允许的域名列表

```yaml
# application.yml
cors:
  allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:3000}
```

**理由**:
- `allowCredentials(true)` 时不能使用 `*`
- 支持多环境配置
- 多个域名用逗号分隔

## Risks / Trade-offs

### R1: 跨域 Cookie 丢失
**风险**: 跨子域部署时 Cookie 可能不生效
**缓解**:
- 正确设置 `Domain=.example.com`
- 测试所有部署场景
- 文档记录配置要求

### R2: 开发环境 HTTPS 问题
**风险**: `Secure=true` 需要 HTTPS，本地开发无法使用
**缓解**:
- 通过环境变量控制 `Secure` 属性
- 开发环境使用 `Secure=false`
- 提供 Docker Compose 本地 HTTPS 方案（可选）

### R3: 前后端必须同步部署
**风险**: 前后端变更需要同步上线，否则认证失败
**缓解**:
- 过渡期支持两种认证方式
- 部署顺序：先后端（向后兼容），再前端
- 准备回滚方案

### R4: CSRF Token 刷新策略
**风险**: 长时间使用后 CSRF Token 过期
**缓解**:
- CSRF Token 有效期与 JWT 一致
- 每次调用 `/auth/me` 可返回新 Token（可选）
- Token 失效时前端提示重新登录

## Migration Plan

### Phase 1: 后端准备
1. 新增 CookieService
2. 新增 CsrfTokenService
3. 新增 `/auth/me` 接口
4. 修改 JwtAuthenticationFilter 支持 Cookie
5. 新增 CsrfTokenFilter

### Phase 2: 后端接口改造
1. 修改登录接口：设置 Cookie + 返回 csrfToken
2. 修改登出接口：清除 Cookie
3. 更新 CORS 配置

### Phase 3: 前端改造
1. 新增 `/auth/me` 调用
2. 修改 axios 配置：`withCredentials: true`
3. 移除 localStorage token 相关代码
4. 修改 authStore

### Phase 4: 清理
1. 移除 Authorization Header 认证支持
2. 移除响应体中的 token 字段（如果还有）

### 部署顺序
1. 部署后端（Phase 1-2）
2. 部署前端（Phase 3）
3. 验证功能正常
4. 执行清理（Phase 4）

### 回滚方案
1. 前端回滚：恢复 localStorage 方案
2. 后端回滚：恢复 Authorization Header 认证
3. 数据库：无变更，无需回滚

## Open Questions

1. **CSRF Token 刷新策略**: 是否每次请求都刷新 CSRF Token？还是只在登录和 `/auth/me` 时获取？
   - **建议**: 仅在登录和 `/auth/me` 时获取，避免并发请求问题

2. **Cookie 前缀**: 是否使用 `__Host-` 或 `__Secure-` 前缀？
   - **建议**: 生产环境使用 `__Secure-` 前缀，增强安全性
