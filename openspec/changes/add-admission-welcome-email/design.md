## Context

结果发布由 `AssessmentDecisionPublicationService.publish()` 处理：每个考生独立事务，通过全局最终轮时升级 MEMBER 角色 + 异步 GitHub 组织邀请 + 发送 `ASSESSMENT_DECISION_NOTIFICATION` 结果通知邮件（最终轮通过时文本为"录取"）。所有邮件模板经 `MessageTemplateRegistry` 注册（code 硬编码元数据 + 数据库覆盖），管理后台可动态编辑。

本次变更：最终轮通过（录取）不再发送结果通知，改为发送独立的 `ADMISSION_WELCOME` 录取欢迎邮件。

## Goals / Non-Goals

**Goals:**
- 新增 `ADMISSION_WELCOME` 模板（纯欢迎内容 + 提示留意 GitHub 组织邀请），纳入现有模板注册/覆盖体系。
- `sendDecisionEmail` 按"最终轮 + 通过"分流：走欢迎邮件模板；其余分支（淘汰、非最终轮通过/未通过）行为完全不变。
- 清理结果通知模板中不再使用的"录取"文本分支。

**Non-Goals:**
- 不改动 GitHub 组织邀请流程（仍为异步触发、失败仅记日志）。
- 不改动角色升级逻辑、淘汰邮件、非最终轮邮件。
- 不改变邮件发送管道（`MessageDispatcher`）本身。
- 前端无需改动（模板管理界面自动列出新模板）。

## Decisions

**D1: 欢迎邮件完全取代结果通知，而非追加一封**
最终轮通过的考生只收到一封欢迎邮件。两封同发会让"录取结果"和"欢迎"信息重复，且考生旅程中录取瞬间就是欢迎时刻。淘汰场景仍需明确的结果告知，故保留结果通知用于淘汰。

**D2: 新增独立模板而非扩展现有模板变量**
`ASSESSMENT_DECISION_NOTIFICATION` 服务多轮次场景，若在其中叠加欢迎分支，管理后台动态编辑时难以维护。独立 `ADMISSION_WELCOME` 模板职责单一，与 `ENROLL_APPROVAL_CREDENTIAL`、`ENROLL_REJECTION` 的既有模式一致。

**D3: GitHub 邀请仅"提示留意"，不带入邀请结果**
`inviteAsync` 失败静默。将邀请结果注入邮件需要把异步邀请改为同步编排，改动面扩大且拖慢发布事务。文案采用"我们已向你发出 GitHub 组织邀请，请留意查收"——即使邀请失败，该提示仍引导考生发现问题后联系管理员，不产生明显错误信息。

**D4: 欢迎邮件变量最小集：`nickname`、`directionLabel`、`epoch`**
纯欢迎内容（用户已确认不需要下一步指引/登录提示/联系方式）。epoch 便于考生确认轮次。

## Risks / Trade-offs

- [管理后台已覆盖过含"录取"文案的自定义 `ASSESSMENT_DECISION_NOTIFICATION` 内容] → 覆盖内容仍可用于淘汰场景，无迁移风险；"录取"文案自然不再渲染。
- [GitHub 邀请实际失败但邮件提示"已发出邀请"] → 文案用"请留意查收"降低承诺强度；失败有日志可供管理员排查补邀（管理后台已有手动批量邀请功能）。
- [最终轮通过考生若依赖结果邮件中的"录取"字样作为凭证] → 欢迎邮件本身即录取凭证，包含方向与轮次信息。

## Migration Plan

无数据迁移。模板元数据为代码硬编码注册，启动即生效；数据库 `tb_message_template` 仅在管理员主动覆盖时写入。回滚 = 还原代码即可，无状态残留。

## Open Questions

无。
