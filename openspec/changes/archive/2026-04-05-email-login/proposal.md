## Why

当前系统仅支持学号+密码登录，但部分用户可能忘记密码或尚未设置密码。邮箱验证码登录提供了一种无密码的快捷登录方式，提升了用户体验和登录便利性。前端登录页面的邮箱登录 Tab UI 已就绪，但未对接后端接口。

## What Changes

- 新增发送邮箱验证码接口（6 位数字，5 分钟有效，60 秒发送间隔限制）
- 新增邮箱验证码登录接口（登录成功后设置 Cookie + CSRF Token，行为与学号登录一致）
- 后端完善验证码生成、存储、发送、校验的完整流程
- 前端对接发送验证码和邮箱登录 API，替换当前模拟逻辑
- 新增 `EmailLoginRequestDTO` 和对应的前端类型定义

## Capabilities

### New Capabilities
- `email-verification-login`: 邮箱验证码登录能力，包含验证码生成、邮件发送、验证码校验、邮箱登录认证的完整流程

### Modified Capabilities
- `backend-student-login`: 扩展现有认证服务，新增邮箱登录方法
- `frontend-login-integration`: 扩展前端登录集成，新增邮箱登录 API 对接

## Impact

- **后端 API**: 新增 2 个公开接口（发送验证码、邮箱登录）
- **数据库**: 使用已有 `tb_verify_code` 表，无需新建表
- **认证流程**: 复用现有 JWT + Cookie + CSRF 机制
- **邮件服务**: 复用已有 `EmailSender` 基础设施
- **前端**: 修改登录页面、authStore、authService
