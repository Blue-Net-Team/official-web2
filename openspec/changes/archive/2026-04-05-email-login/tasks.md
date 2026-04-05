## 1. 领域层：验证码生成与仓储扩展

- [x] 1.1 扩展 `VerificationCodeRepository` 新增 `save`、`markAsUsed`、`findLatestByEmailWithinSeconds` 方法
- [x] 1.2 实现 `VerificationCodeRepositoryImpl` 新增方法（save 存储、markAsUsed 标记已用、findLatestByEmailWithinSeconds 60秒内查询）
- [x] 1.3 新增 `VerificationCodeDomainService` 接口和实现（generateCode 生成6位验证码 + 5分钟有效期）

## 2. 应用层：发送验证码与邮箱登录服务

- [x] 2.1 新增 `EmailLoginRequestDTO` 和 `SendVerificationCodeRequestDTO`
- [x] 2.2 扩展 `AuthService` 接口新增 `sendVerificationCode`、`loginWithEmail` 方法
- [x] 2.3 实现 `AuthServiceImpl.sendVerificationCode`（频率校验 → 生成验证码 → 存储 → 异步发送邮件）
- [x] 2.4 实现 `AuthServiceImpl.loginWithEmail`（调用 authDomainService.checkLocalValid → 设置 Cookie/CSRF → 返回响应）

## 3. 接口层：Controller 与 CSRF 配置

- [x] 3.1 在 `AuthController` 新增 `POST /api/v1/auth/login/email` 邮箱登录端点
- [x] 3.2 在 `AuthController` 新增 `POST /api/v1/auth/verification-code/send` 发送验证码端点
- [x] 3.3 将 `/api/v1/auth/verification-code/send` 加入 CSRF 白名单（CsrfTokenFilter）

## 4. 前端：API 对接

- [x] 4.1 在 `apis/schema/type.ts` 新增 `EmailLoginRequestDTO` 和 `SendVerificationCodeRequestDTO` 类型
- [x] 4.2 在 `apis/services/auth.service.ts` 新增 `sendVerificationCode` 和 `loginWithEmail` 方法
- [x] 4.3 在 `stores/authStore.ts` 新增 `loginWithEmail` 和 `sendVerificationCode` 方法
- [x] 4.4 修改 `login/page.tsx` 的 `handleSendCode` 调用真实 API
- [x] 4.5 修改 `login/page.tsx` 的 `handleSubmit` 邮箱登录分支调用真实 API
