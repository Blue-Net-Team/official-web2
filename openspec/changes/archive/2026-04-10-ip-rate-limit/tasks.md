## 1. 新增限频基础设施

- [x] 1.1 创建 `TooManyRequests` 异常类，继承 `GlobalException`，`code = HttpStatus.TOO_MANY_REQUESTS`，放置于 `domain/exception/`
- [x] 1.2 创建 `IpUtils` 工具类，实现 `getClientIp(HttpServletRequest)` 方法，按优先级提取 `X-Forwarded-For` → `X-Real-IP` → `getRemoteAddr()`，放置于 `infrastructure/security/util/`
- [x] 1.3 创建 `@RateLimit` 注解（`@Target(METHOD)`, `@Retention(RUNTIME)`），包含 `int interval()` 参数，放置于 `infrastructure/security/annotation/`
- [x] 1.4 创建 `RateLimitAspect` 切面，使用 `@Around("@annotation(rateLimit)")` 拦截，从 `RequestContextHolder` 获取请求，通过 `IpUtils` 提取 IP，构造 Redis Key `rate_limit:{ip}:{method}:{uri}`，调用 `setIfAbsent` 原子操作，Redis 异常时降级放行并记录 ERROR 日志，放置于 `infrastructure/security/aspect/`
- [x] 1.5 编写 `RateLimitAspect` 单元测试：验证首次请求放行、重复请求拒绝、不同接口独立计算、Redis 异常降级放行

## 2. 应用限频注解到验证码发送接口

- [x] 2.1 在 `AuthController.sendVerificationCode()` 方法上添加 `@RateLimit(interval = 60)`
- [x] 2.2 在 `ResetPasswordController.sendCode()` 方法上添加 `@RateLimit(interval = 60)`
- [x] 2.3 在 `UserProfileController.sendEmailVerificationCode()` 方法上添加 `@RateLimit(interval = 60)`

## 3. 清理死代码 — 领域层

- [x] 3.1 从 `VerificationCodeDomainService` 接口中删除 `generateCode(String email, String ipaddress)` 单参数重载方法
- [x] 3.2 从 `VerificationCodeDomainService` 接口中将 `generateCode(String email, String ipaddress, String scene)` 修改为 `generateCode(String email, String scene)`，移除 IP 参数
- [x] 3.3 同步修改 `VerificationCodeDomainServiceImpl` 实现，移除 `ipaddress` 参数
- [x] 3.4 从 `VerificationCodeRepository` 接口中删除 `findLatestByEmailWithinSeconds(String email, int seconds)` 方法
- [x] 3.5 从 `VerificationCodeRepository` 接口中删除 `findLatestByEmailAndSceneWithinSeconds(String email, String scene, int seconds)` 方法
- [x] 3.6 同步修改 `VerificationCodeRepositoryImpl` 实现，删除上述两个方法的实现
- [x] 3.7 从 `VerifyCode` 实体中删除 `ipAddress` 字段

## 4. 清理死代码 — 应用层

- [x] 4.1 修改 `AuthServiceImpl.sendVerificationCode()`：删除 `findLatestByEmailWithinSeconds` 调用和限频判断逻辑，将 `generateCode(email, null, scene)` 改为 `generateCode(email, scene)`
- [x] 4.2 修改 `ResetPasswordServiceImpl.sendCode()`：删除 `findLatestByEmailAndSceneWithinSeconds` 调用和限频判断逻辑，将 `generateCode(email, clientIp, SCENE)` 改为 `generateCode(email, SCENE)`，移除 `clientIp` 参数
- [x] 4.3 修改 `ResetPasswordService` 接口：从 `sendCode()` 方法签名中移除 `clientIp` 参数
- [x] 4.4 修改 `UserInfoServiceImpl.sendEmailVerificationCode()`：删除 `findLatestByEmailAndSceneWithinSeconds` 调用和限频判断逻辑，将 `generateCode(email, null, scene)` 改为 `generateCode(email, scene)`

## 5. 清理死代码 — 接口层

- [x] 5.1 修改 `ResetPasswordController.sendCode()`：移除 `HttpServletRequest` 参数和 `clientIp` 提取逻辑，更新服务调用

## 6. 数据库迁移

- [x] 6.1 创建 Flyway 迁移文件，执行 `ALTER TABLE tb_verify_code DROP COLUMN ip_address`；删除 V4 中对应的列注释（通过新迁移覆盖注释）
- [x] 6.2 更新 `VerifyCodeMapper.xml` 的 `BaseResultMap`，移除 `ip_address` 列映射

## 7. 测试更新

- [x] 7.1 更新 `VerificationCodeDomainServiceImplTest`：移除 IP 参数相关测试用例
- [x] 7.2 更新 `AuthServiceImplTest`：移除限频查询相关的 mock 和断言
- [x] 7.3 更新 `ResetPasswordServiceImplTest`：移除限频查询和 clientIp 相关的 mock 和断言
- [x] 7.4 更新 `ResetPasswordControllerTest`：移除 `HttpServletRequest` 相关的 mock 和断言
