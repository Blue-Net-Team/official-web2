## 1. 基础设施层 - Cookie 服务

- [x] 1.1 创建 `CookieService` 接口和实现类，封装 Cookie 操作
- [x] 1.2 支持环境感知的 Cookie 配置（开发/生产环境的 Secure 和 Domain 差异）
- [ ] 1.3 编写 `CookieServiceTest` 单元测试

## 2. 基础设施层 - CSRF Token 服务

- [x] 2.1 创建 `CsrfTokenService`，实现 CSRF Token 生成逻辑
- [x] 2.2 CSRF Token 存储到 Cookie（非 HttpOnly）
- [x] 2.3 实现 CSRF Token 验证逻辑
- [ ] 2.4 编写 `CsrfTokenServiceTest` 单元测试

## 3. 基础设施层 - JWT 认证过滤器改造

- [x] 3.1 修改 `JwtAuthenticationFilter.extractJwtFromRequest()`，优先从 Cookie 提取
- [x] 3.2 保留 Authorization Header 提取作为 fallback（过渡期）
- [ ] 3.3 编写 `JwtAuthenticationFilterTest` 测试 Cookie 提取场景

## 4. 基础设施层 - CSRF 过滤器

- [x] 4.1 创建 `CsrfTokenFilter`，验证 POST/PUT/DELETE/PATCH 请求的 CSRF Token
- [x] 4.2 实现 Double Submit Cookie 验证：Cookie vs X-CSRF-Token Header
- [x] 4.3 配置公开接口白名单（登录、报名等）
- [ ] 4.4 编写 `CsrfTokenFilterTest` 单元测试

## 5. 安全配置更新

- [x] 5.1 修改 `SecurityConfig`，添加 `CsrfTokenFilter` 到过滤器链
- [x] 5.2 更新 CORS 配置，从环境变量读取 `allowedOrigins`
- [x] 5.3 确保 `allowCredentials(true)` 配置正确

## 6. 应用层 - AuthController 改造

- [x] 6.1 修改 `AuthService.login()` 返回值，包含 `csrfToken`
- [x] 6.2 修改登录接口，设置 `auth_token` 和 `csrf_token` Cookie
- [x] 6.3 修改登录接口响应 DTO，移除 `token` 字段，添加 `csrfToken` 字段
- [x] 6.4 修改登出接口，清除 Cookie 并设置 `Max-Age=0`
- [x] 6.5 新增 `GET /auth/me` 接口，返回 `userInfo` 和 `csrfToken`

## 7. 应用层 - DTO 更新

- [x] 7.1 修改 `UserAuthResponseDTO`，移除 `token` 字段，添加 `csrfToken` 字段
- [x] 7.2 新增 `AuthMeResponseDTO`，包含 `userInfo` 和 `csrfToken`
- [x] 7.3 更新 API 文档注解（Swagger）

## 8. 集成测试

- [ ] 8.1 编写 `AuthControllerCookieIntegrationTest`，测试登录设置 Cookie
- [ ] 8.2 编写 `AuthMeIntegrationTest`，测试 `/auth/me` 接口
- [ ] 8.3 编写 `CsrfProtectionIntegrationTest`，测试 CSRF Token 验证
- [ ] 8.4 编写 `LogoutCookieClearIntegrationTest`，测试登出清除 Cookie
- [ ] 8.5 测试跨域场景（CORS + Cookie）

## 9. 配置文件更新

- [x] 9.1 添加 Cookie 配置项到 `application.yml`（domain, secure 等）
- [x] 9.2 添加 CORS 配置项 `cors.allowed-origins`
- [x] 9.3 更新 `application-dev.yml` 开发环境配置
- [x] 9.4 更新 `application-prod.yml` 生产环境配置

## 10. 文档更新

- [x] 10.1 更新 `CLAUDE.md`，记录新的认证机制
- [x] 10.2 更新 API 文档说明 Cookie 和 CSRF 机制
- [x] 10.3 添加环境变量配置说明
