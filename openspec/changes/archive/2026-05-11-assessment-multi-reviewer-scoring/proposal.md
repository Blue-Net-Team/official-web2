## Why

当前考核系统仅支持单个团队成员对文件上传题进行一次性人工评分，缺乏多人协作评审能力。方向管理员无法综合多位成员的意见做出最终评定，评审过程不透明，考生也无法及时获知详细的评审反馈。引入多人评论评分机制，可以让团队成员共同参与评审、互相参考意见，方向管理员据此综合决定最终评分，提升评审的公平性和准确性。

## What Changes

- **新增评论存储与查询链路**：基于已有的 `tb_comment` 表，补全 Repository → Domain Service → Controller 完整链路，支持团队成员对同一文件上传答案添加评论和打分。
- **新增多人评论前端组件**：在管理端评分面板中增加评论列表、评论表单、最终评分确认表单。
- **新增最终评分确认机制**：方向管理员在查看所有成员评论后，手动确认该题的最终评分，确认后的评分以 `JudgementSource.ADMIN_FINALIZED` 存入 `tb_assessment_judgement`。
- **新增结果发布标记**：在 `tb_assessment_time` 上增加 `results_published_at` 字段，控制考生何时可见评论和最终评分。
- **扩展邮件通知**：结果发布时，邮件内容增加最终评分信息（如有）。
- **修改现有评分看板逻辑**：评分看板展示所有评论及最终评分，支持方向管理员在界面上完成确认操作。

## Capabilities

### New Capabilities
- `multi-reviewer-comment`: 团队成员对文件上传答案添加评论与评分，支持按答案查询所有评论，限制同一用户对同一答案只能评论一次。
- `admin-finalized-judgement`: 方向管理员基于所有成员评论手动确认某题最终评分，生成带有 `ADMIN_FINALIZED` 来源的权威评分记录。
- `assessment-result-publication`: 方向管理员通过发布操作设置 `results_published_at`，触发考生可见评论与最终评分，并批量发送邮件通知。

### Modified Capabilities
- `assessment-judgement`: 在人工评分流程中增加 `ADMIN_FINALIZED` 评分来源，修改最终评分存储逻辑，使其与多人评论流程衔接；评分看板需展示评论列表及最终评分确认入口。
- `assessment-decision-publish`: 发布流程需先设置 `results_published_at` 后再发送邮件，且邮件模板可携带最终评分信息。

## Impact

- **数据库**：`tb_assessment_time` 新增 `results_published_at` 字段（可为空）。
- **后端 API**：新增评论 CRUD 接口、最终评分确认接口、修改发布接口以支持设置发布标记。
- **前端页面**：管理端评分面板 (`/admin/assessment/judge/score`) 增加评论列表和最终评分确认 UI。
- **邮件模板**：`ASSESSMENT_DECISION_NOTIFICATION` 模板可选增加评分信息变量。
- **权限**：评论和最终评分确认接口需使用 `@RequiresPermission` 注解并确保权限标识全局唯一。
