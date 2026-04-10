## Context

当前登录页有"忘记密码？"链接指向 `/forgot-password`，但该页面不存在。系统已有：
- 验证码基础设施：`VerificationCodeDomainService` 支持多场景（scene），当前仅用于 `login` 场景
- 邮件服务：`EmailSender` 支持同步/异步发送、HTML 模板
- 密码编码：`BCryptPasswordEncoder`
- Redis：`StringRedisTemplate` 用于 JWT 白名单和 OAuth 状态存储
- 前端认证栈：Zustand store + Ant Design + 暗色主题

设计稿已完成：`docs/UI/forget-pwd.pen`（8 屏幕：4 桌面端 + 4 移动端）

## Goals / Non-Goals

**Goals:**
- 实现 4 步密码重置流程：学号验证 → 邮箱验证 → 验证码输入 → 设置新密码
- 复用现有验证码基础设施（扩展 scene 为 `reset_password`）
- 使用 Redis 管理密码重置流程状态，保证步骤顺序和时效
- 前端无刷新步骤切换，平滑过渡体验
- 所有接口为公开接口（无需认证），加入 CSRF 白名单

**Non-Goals:**
- 不实现短信验证码方式
- 不实现安全问题验证方式
- 不修改现有登录流程
- 不实现管理员强制重置密码（已有独立用户管理功能）

## Decisions

### 1. 使用 Redis 存储流程状态而非数据库

**选择**：用 Redis Hash 存储密码重置流程状态（学号、邮箱、当前步骤、验证状态），TTL 15 分钟。

**理由**：
- 密码重置是短时临时操作，不需要持久化
- Redis 天然支持 TTL 过期清理
- 与现有 OAuth 状态管理（`oauth:state:`）模式一致
- 避免新增数据库表

**Key 设计**：`reset_pwd:{uuid}` → `{studentId, email, step, verified}`

**备选方案**：
- JWT Token 携带状态 → 前端可篡改 step 字段，不安全
- 数据库表 → 需要定时清理过期记录，增加复杂度

### 2. 复用验证码基础设施，扩展 scene 字段

**选择**：在现有 `VerificationCodeDomainService.generateCode()` 中传入 `scene="reset_password"`。

**理由**：
- 现有验证码已支持 scene 字段（`tb_verify_code` 表有 scene 列）
- 发送频率限制（60 秒）和过期时间（5 分钟）与登录场景一致
- 无需新建验证码生成逻辑

### 3. 前端步骤状态由 URL query 管理

**选择**：使用 `?step=1|2|3|4` query 参数管理当前步骤，配合 React state 做动画过渡。

**理由**：
- 用户可直接通过 URL 回到特定步骤（如刷新页面）
- 前端路由无需新增页面文件，单页面内切换
- 配合 CSS transition 实现无刷新过渡

### 4. 后端 API 设计：4 个接口，无状态链式验证

**选择**：
| 接口 | 路径 | 说明 |
|------|------|------|
| 学号验证 | `POST /api/v1/auth/reset-password/verify-student` | 验证学号存在，返回 resetToken |
| 邮箱验证 | `POST /api/v1/auth/reset-password/verify-email` | 验证邮箱匹配学号，返回 resetToken |
| 发送验证码 | `POST /api/v1/auth/reset-password/send-code` | 向已验证邮箱发送验证码 |
| 重置密码 | `POST /api/v1/auth/reset-password/reset` | 验证验证码 + 设置新密码 |

每个接口接收 `resetToken`，后端从 Redis 校验流程状态。

**理由**：
- 链式验证防止跳步攻击
- resetToken 为一次性 UUID，验证失败不泄露信息

### 5. 密码修改通过现有 UserRepository

**选择**：直接使用 `UserRepository.updatePassword(userId, encodedPassword)` 更新密码。

**理由**：密码编码使用现有 `BCryptPasswordEncoder`，与注册和初始化流程一致。

## Risks / Trade-offs

- **[验证码复用攻击]** → 验证码使用后立即标记为已使用，Redis 流程状态标记 verified=true
- **[学号枚举]** → 学号验证失败返回统一错误"学号不存在或格式错误"，不区分具体原因
- **[邮箱枚举]** → 邮箱验证失败返回"邮箱与学号不匹配"，但攻击者需先知道有效学号
- **[Redis 宕机]** → Redis 不可用时密码重置功能不可用，返回服务错误提示，不影响登录
- **[并发重置]** → 同一学号允许多次发起重置，后发起的覆盖前一次的流程状态
