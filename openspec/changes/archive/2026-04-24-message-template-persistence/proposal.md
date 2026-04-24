## Why

当前 `MessageTemplateRegistry` 是纯内存实现，管理后台编辑的模板内容覆盖 (`contentOverrides`) 和启禁用状态 (`disabledCodes`) 仅在 JVM 内存中维护。应用重启后所有修改丢失，多节点部署时各实例状态不一致。数据库表 `tb_message_template`、`MessageTemplateDO` 和 `MessageTemplateMapper` 已存在但从未被使用。

## What Changes

- 改造 `MessageTemplateRegistry` 为**内存默认模板 + 数据库覆盖层**的混合架构：
  - 模板元数据（编码、名称、描述、变量列表、默认 HTML）保留在内存中，代码硬编码为默认值
  - 运行时覆盖的 `content`、`subject` 和 `enabled` 状态持久化到 `tb_message_template`
  - 启动时从数据库加载已有的覆盖内容和启禁用状态
  - 编辑时同步 upsert 数据库记录
- `MessageTemplateAppServiceImpl.updateTemplate()` 开始实际保存前端传入的 `subject`（当前被忽略）
- 新增集成测试验证持久化行为

## Capabilities

### New Capabilities
<!-- 无新增独立能力 -->

### Modified Capabilities
- `backend-message-notification`: 新增模板运行时状态持久化需求。模板管理后台已支持编辑内容和启禁用，但此前仅在内存生效；本次变更要求这些运行时修改在重启后保留。

## Impact

- `MessageTemplateRegistry`: 注入 `MessageTemplateMapper`，启动加载逻辑，upsert 写库
- `MessageTemplateAppServiceImpl`: `updateTemplate()` 传递 `subject`
- 单元测试: `EmailVerificationCodeTemplateTest`、`EnrollmentRejectionTemplateTest` 需要适配构造方式（注入 mock mapper）
- 前端/API: 无变更，完全兼容
