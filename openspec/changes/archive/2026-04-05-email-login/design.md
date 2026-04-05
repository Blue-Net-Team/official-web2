## Context

当前系统使用学号+密码的单一登录方式。前端登录页已预留邮箱登录 Tab，包含邮箱输入、验证码输入和发送验证码按钮的完整 UI，但未对接后端。

后端已有以下基础设施：
- `EmailSender` 邮件发送服务（支持同步/异步、HTML/模板邮件）
- `tb_verify_code` 数据库表（target、code、expire_at、used_at、ip_address）
- `VerificationCodeRepository`（仅 `findByEmailAndCode`）
- `LocalLoginType.EMAIL` 枚举
- `AuthDomainServiceImpl` 中已实现 `findByEmail` + `verifyCode` 分支
- `User` 实体包含 `email` 字段，数据库有 `idx_user_email` 索引
- 完整的 JWT + Cookie + CSRF 认证机制

## Goals / Non-Goals

**Goals:**
- 实现邮箱验证码发送和登录的完整后端流程
- 前端对接真实 API，替换模拟逻辑
- 复用现有认证机制（JWT + Cookie + CSRF），邮箱登录后行为与学号登录一致
- 6 位数字验证码，5 分钟有效，60 秒发送间隔
- 防止滥用：基于邮箱和 IP 的频率限制

**Non-Goals:**
- 不实现验证码的 Redis 缓存（直接使用数据库存储）
- 不实现 GitHub OAuth 登录
- 不实现邮箱绑定/修改功能（邮箱由报名流程自动写入）
- 不实现验证码重试次数限制（后续可扩展）
- 不修改现有的学号登录流程

## Decisions

### 1. 验证码存储：数据库而非 Redis

**选择**：使用现有 `tb_verify_code` 表存储验证码。

**理由**：项目已有该表和基础 Repository，无需引入新依赖。验证码数据量小，数据库完全胜任。Redis 主要用于 JWT 白名单缓存，不增加额外复杂度。

**替代方案**：Redis 存储（优势是自动过期，但增加了新依赖和复杂度）。

### 2. 验证码生成：领域服务

**选择**：在领域层新增 `VerificationCodeDomainService`，负责验证码生成逻辑。

**理由**：验证码生成属于业务逻辑（6 位随机数字 + 5 分钟有效期），应位于领域层。领域服务生成 `VerifyCodeVO`，由基础设施层持久化。

### 3. 发送频率限制：数据库查询

**选择**：在发送验证码前查询数据库，检查 60 秒内是否已发送。

**理由**：简单可靠，无需引入额外缓存机制。基于 `tb_verify_code.expire_at` 和 `target` 查询即可。

### 4. API 路径设计

**选择**：
- `POST /api/v1/auth/login/email` — 邮箱验证码登录
- `POST /api/v1/auth/verification-code/send` — 发送验证码

**理由**：登录接口遵循 `/api/v1/auth/login/*` 模式（CSRF 白名单已覆盖 `/api/v1/auth/login/**`）。发送验证码接口需加入 CSRF 白名单。

### 5. 验证码使用后标记

**选择**：验证码验证通过后，将 `used_at` 设置为当前时间。

**理由**：防止同一验证码被重复使用。`AuthDomainServiceImpl.verifyCode` 已检查 `isUsed()`。

### 6. 前端发送验证码接口错误处理

**选择**：发送验证码接口返回具体错误信息（频率限制、邮箱未注册等），前端直接展示。

**理由**：用户体验优先，明确告知用户无法发送的原因。但邮箱是否注册的判断由登录接口处理，发送接口不做注册校验（避免邮箱枚举攻击）。

## Risks / Trade-offs

- **[邮箱枚举风险]** → 发送验证码接口不校验邮箱是否已注册，无论邮箱是否存在都返回成功提示，防止攻击者枚举用户邮箱。登录时才校验邮箱和验证码。
- **[验证码暴力破解]** → 6 位数字有 100 万种组合，5 分钟有效期。当前不做重试次数限制，后续可扩展。
- **[邮件发送失败]** → 使用异步发送，发送失败不影响接口响应。用户可在 60 秒后重新发送。
- **[CSRF 白名单]** → 发送验证码接口需加入 CSRF 白名单，因为未登录用户调用。
