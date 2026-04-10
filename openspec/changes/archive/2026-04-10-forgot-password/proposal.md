## Why

当前登录页面存在"忘记密码？"链接指向 `/forgot-password`，但该页面尚未实现。用户忘记密码后无法自助重置，需要联系管理员手动处理。需要实现一个安全的自助密码重置流程，通过学号验证 + 邮箱验证码的方式确认用户身份。

## What Changes

- 新增"忘记密码"前端页面，包含 4 步引导式表单：输入学号 → 验证邮箱 → 输入验证码 → 设置新密码
- 新增后端密码重置相关 API 接口（学号验证、邮箱验证、发送验证码、重置密码）
- 集成现有邮件服务发送验证码
- 使用 Redis 存储验证码及流程状态，设置过期时间
- 步骤切换使用前端路由状态管理，无页面刷新感

## Capabilities

### New Capabilities
- `forgot-password`: 忘记密码完整流程，包含前端多步表单页面和后端密码重置 API

### Modified Capabilities
- `backend-auth-session`: 登出后需清除密码重置相关 Redis 状态

## Impact

- **前端**：新增 `/forgot-password` 页面及路由，复用登录页的暗色主题和玻璃态风格
- **后端**：新增密码重置相关 Controller、Service、Redis 操作
- **API**：新增 4 个公开接口（无需认证）
- **依赖**：复用现有邮件服务和 Redis 基础设施
- **设计稿**：已创建 `docs/UI/forget-pwd.pen` 包含 8 个屏幕（4 桌面端 + 4 移动端）
