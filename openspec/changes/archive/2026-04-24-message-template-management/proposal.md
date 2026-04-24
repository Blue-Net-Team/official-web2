## Why

当前系统的消息模板全部硬编码在 Java 类中，运营人员无法在线调整邮件文案。同时，多个验证码场景（登录、重置密码、修改邮箱）各自维护独立的模板类，HTML 结构高度重复，新增场景成本高。此外，报名拒绝和考核最终录取/淘汰等关键业务节点缺少邮件通知，影响用户体验。

## What Changes

- **验证码模板场景化重构**：将 `LoginVerificationCodeTemplate`、`ResetPasswordVerificationCodeTemplate`、`ChangeEmailVerificationCodeTemplate` 三个独立模板合并为通用 `EmailVerificationCodeTemplate`，通过 `VerificationCodeScene` 枚举区分场景文案
- **新增报名拒绝邮件通知**：在 `rejectEnrollment()` 中触发发送包含拒绝原因的邮件
- **改造考核结果通知**：`publishDecisions()` 中判断当前轮次是否为最后一轮，最后一轮使用「录取/淘汰」文案，中间轮次保持「通过/未通过」文案
- **新增消息模板管理后台**：提供模板列表查询、详情查看、内容编辑、启禁用、预览等 Admin API
- **修复数据库文档**：`docs/数据库设计.md` 中 `tb_message_template` 字段定义与实际表结构不一致

## Capabilities

### New Capabilities
- `message-template-admin`: 消息模板管理后台，支持模板的 CRUD、启禁用开关、内容编辑和实时预览
- `email-verification-scene`: 邮箱验证码场景化通用模板，通过枚举统一配置各场景的标题、说明和底部文案

### Modified Capabilities
- `assessment-decision-publish`: 发布决策邮件时增加「最后一轮判断」逻辑，最后一轮通过/淘汰使用录取/淘汰文案（而非通过/未通过）
- `backend-enrollment`: 报名拒绝状态流转时新增发送拒绝通知邮件的要求
- `backend-message-notification`: 验证码模板从独立类实现改为场景化通用模板实现，要求保持现有发送行为不变

## Impact

- **代码层**：`application/message/template/` 下 3 个验证码模板类将被删除，新增 1 个通用模板 + 1 个场景枚举；新增 1 个报名拒绝模板；`AssessmentDecisionNotificationTemplate` 增加场景判断能力
- **API 层**：新增 `/api/v1/admin/message-templates` 系列接口（列表、详情、更新、启禁用、预览）
- **数据库层**：无表结构变更（`tb_message_template` 已存在），仅需修复文档描述
- **测试层**：需更新 `AuthAppServiceImplTest`、`ResetPasswordAppServiceImplTest`、`UserInfoAppServiceImplTest` 中的模板 Mock
