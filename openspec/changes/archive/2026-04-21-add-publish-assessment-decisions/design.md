## Context

考核决策系统已完成核心功能：管理员可为考生设置通过/淘汰决策（`tb_assessment_decision`），前端决策页面展示候选人列表和统计。前端"发布本轮结果"按钮当前仅弹占位提示，未接入后端。

项目已有 `EmailSender` 基础设施（`sendHtmlAsync` 支持异步 HTML 邮件），`User` 实体含 `email` 字段，可直接复用。

## Goals / Non-Goals

**Goals:**
- 按 `assessmentTimeId` 向该轮全部已决策考生（`passed IS NOT NULL`）异步发送 HTML 邮件
- 邮件内容包含：考生姓名、考核方向、轮次、通过/淘汰结果
- 前端按钮调用后端 API 并展示发送结果（成功数量）
- 复用已有 `EmailSender` 基础设施

**Non-Goals:**
- 不修改数据库（无新表、新字段）
- 不实现取消发布/撤回
- 不实现邮件模板管理（硬编码 HTML 模板）
- 不实现重发失败的邮件（首次发送失败仅日志记录）

## Decisions

### D1: API 设计

`POST /api/v1/admin/assessment-judgements/decisions/publish?assessmentTimeId={id}`

- 权限：`assessment-decision:set`（与决策设置同级，方向管理员及以上）
- 请求：`assessmentTimeId` 作为 query parameter
- 响应：`ResponseMessage<Integer>` 返回成功发送邮件数量
- 校验：assessmentTimeId 必须存在；无已决策考生时返回 0

### D2: 邮件发送流程

```
1. AssessmentTimeRepository.findById → 获取 direction, epoch
2. AssessmentDecisionRepository.findByAssessmentTimeId → 获取全部决策
3. 过滤 passed != null 的决策
4. 批量获取用户信息（UserRepository.findById 循环查询）
5. 遍历：构建 HTML 内容 → EmailSender.sendHtmlAsync 发送
6. 返回发送总数
```

采用 `sendHtmlAsync` 异步发送，不阻塞请求返回。单个邮件发送失败仅记录日志，不影响其他邮件。

### D3: HTML 邮件模板

简单内联 HTML 模板，硬编码在 Service 层常量中：

```
主题：[蓝网] 考核结果通知
内容：{nickname} 你好，{direction}方向第{epoch}轮考核结果：{result}
```

通过/淘汰分别显示不同文本。

### D4: 分层设计

遵循 DDD 四层架构：
- **Controller**：新增 `publishDecisions` 端点，接收请求
- **Application Service**：`AssessmentJudgementServiceImpl.publishDecisions()` 编排流程
- **Domain**：无新增（复用已有 Repository）
- **Infrastructure**：无新增（复用已有 EmailSender、Repository）

无需新增 Repository 方法或 Mapper SQL。

### D5: 前端对接

- `admin-assessmentJudgementService` 新增 `publishDecisions(assessmentTimeId)` 方法
- decision 页面和 score 页面的 `showPublishNotice()` 替换为实际 API 调用
- 调用成功后展示发送数量；失败展示错误信息

## Risks / Trade-offs

- **邮件发送失败不重试**：异步发送失败仅日志记录，需管理员手动检查日志。可接受——首次发布，后续迭代可加重发机制
- **无发布状态追踪**：不在数据库记录"已发布"状态，管理员可多次点击发布导致重复发送。可接受——邮件内容为通知性质，重复发送影响有限
- **循环查用户**：当前 `UserRepository` 无批量查询方法，逐个 `findById`。考生数量通常 < 100，性能可接受
