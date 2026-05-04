## Why

当前系统需要邮件发送基础设施来支持后续的业务场景（邮箱验证码登录、密码重置、考核结果通知、报名结果通知等）。虽然系统已引入 `spring-boot-starter-mail` 依赖并配置了SMTP参数，但尚未实现邮件发送服务。此变更将搭建邮件发送的基础设施层。

## What Changes

- 创建邮件发送服务接口和实现类
- 封装Spring Mail的JavaMailSender
- 支持简单邮件和HTML邮件发送
- 支持模板变量替换（`{{variable}}`格式）
- 支持异步邮件发送

## Capabilities

### New Capabilities

- `email-sender`: 邮件发送核心基础设施，封装Spring Mail实现邮件发送，支持HTML邮件和模板变量替换

## Impact

- **新增服务**: EmailSender接口和实现类
- **新增异常**: EmailSendException邮件发送异常
- **配置**: 已有SMTP配置，无需新增
- **无数据库变更**: 本次仅为基础设施搭建
