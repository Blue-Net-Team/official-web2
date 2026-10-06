## 1. 模板注册

- [ ] 1.1 在 `MessageTemplateRegistry` 中注册 `ADMISSION_WELCOME` 模板（名称"录取欢迎通知"、主题"[蓝网] 欢迎加入蓝网团队"、描述、变量列表 `nickname/directionLabel/epoch`、默认欢迎 HTML，含 GitHub 组织邀请提示）

## 2. 欢迎邮件模板类（TDD）

- [ ] 2.1 为 `AdmissionWelcomeTemplate` 编写单元测试：验证变量替换（nickname、directionLabel、epoch 渲染到 HTML）、null 值兜底
- [ ] 2.2 实现 `AdmissionWelcomeTemplate`（`@Component`，从 `MessageTemplateRegistry` 读取模板内容并替换变量，风格参照 `EnrollmentApprovalCredentialTemplate`）

## 3. 发送分流（TDD）

- [ ] 2.3 修改 `AssessmentDecisionPublicationServiceTest` / `AssessmentDecisionPublicationService` 相关测试：最终轮 + 通过 → 断言发送 `ADMISSION_WELCOME` 欢迎邮件且**不发送**结果通知邮件
- [ ] 2.4 保留既有测试：非最终轮通过/未通过、最终轮淘汰 → 仍发送 `ASSESSMENT_DECISION_NOTIFICATION` 结果通知
- [ ] 2.5 实现分流逻辑：`sendDecisionEmail` 中最终轮 + passed 分支改用 `AdmissionWelcomeTemplate` 构建 HTML、主题取自模板；其余分支不变
- [ ] 2.6 清理 `AssessmentDecisionNotificationTemplate` 中"录取"相关的颜色判断分支（`resultText` 仅剩 通过/未通过/淘汰）

## 4. 集成测试

- [ ] 4.1 更新 `AdminAssessmentJudgementControllerIntegrationTest`（或结果发布相关集成测试）：最终轮通过考生收到欢迎邮件，验证邮件主题与内容包含欢迎语、方向、轮次及 GitHub 邀请提示
- [ ] 4.2 集成测试覆盖：最终轮淘汰仍收到"淘汰"结果通知邮件

## 5. 验证与打包

- [ ] 5.1 `cd src/backend && ./mvnw clean compile package` 编译打包通过
- [ ] 5.2 构建镜像 `docker build -t bluenet-api-service:latest src/backend` 并重启后端容器
- [ ] 5.3 通过管理后台模板列表确认 `ADMISSION_WELCOME` 出现，可预览/覆盖内容与主题
- [ ] 5.4 端到端验证：构造最终轮通过考生并发布结果，确认收到欢迎邮件且不再收到结果通知邮件
