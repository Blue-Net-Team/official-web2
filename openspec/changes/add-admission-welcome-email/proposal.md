## Why

考生通过全局最终轮考核（录取）时，目前只会收到一封与常规轮次无异的结果通知邮件（内容为"录取"二字），角色升级为 MEMBER 和 GitHub 组织邀请都在后台静默完成，考生完全没有"加入团队"的感知，体验割裂。

## What Changes

- 新增独立的「录取欢迎邮件」模板 `ADMISSION_WELCOME`（纯欢迎内容：祝贺、欢迎加入蓝网团队、提示留意 GitHub 组织邀请邮件），注册进 `MessageTemplateRegistry`，支持管理后台动态覆盖内容与主题。
- 最终结果发布时，全局最终轮 + 通过的考生**不再发送**结果通知邮件，改为发送录取欢迎邮件；淘汰及非最终轮的通过/未通过仍沿用现有 `ASSESSMENT_DECISION_NOTIFICATION` 结果通知模板，行为不变。
- 清理 `AssessmentDecisionNotificationTemplate` / `AssessmentDecisionPublicationService` 中"录取"结果文本分支（该场景由欢迎邮件完全取代）。

## Capabilities

### New Capabilities
- `admission-welcome-email`: 录取（全局最终轮通过）时发送的独立欢迎邮件，含模板注册、变量替换与按最终轮+通过条件触发。

### Modified Capabilities
- `assessment-result-publication`: 最终结果发布时的邮件触发规则变更——全局最终轮通过不再发送"录取"结果通知，改为发送独立的录取欢迎邮件；淘汰与非最终轮行为不变。

## Impact

- 后端：`MessageTemplateRegistry`（新增模板注册）、`AssessmentDecisionPublicationService`（发送分流逻辑）、新增 `AdmissionWelcomeTemplate` 模板类；`AssessmentDecisionNotificationTemplate` 的 `resultText` 颜色判断逻辑可简化。
- 管理后台消息模板管理：模板列表将多出一个 `ADMISSION_WELCOME` 条目（自动出现，无需前端改动）。
- 数据库：`tb_message_template` 表仅在管理员覆盖时新增记录，无迁移。
- 不影响：GitHub 组织邀请流程（仍为异步触发、失败仅记日志）、角色升级逻辑、非最终轮结果通知。
