## Why

考生（Candidate 角色）登录后，导航栏已提供"考核中心"入口，但 `/assessment` 页面尚未实现。考生需要一个集中查看自己可参加的考核时间安排的页面，了解每轮考核的状态（未开始/进行中/已结束）、时间限制和答题进度。

## What Changes

- 后端扩展现有 `AssessmentTimeDTO`，增加 `totalQuestions` 和 `completedQuestions` 字段，使列表接口直接返回进度数据
- 后端新增 `GET /api/v1/assessment-times/{id}/progress` 端点，返回单个考核的进度信息（供未来其他功能使用）
- 前端新增 `/assessment` 页面（客户端组件），按设计稿 `assessment time.pen` 实现暗色主题考核时间卡片列表
- 前端新增 `assessment-time.service.ts` API 服务层和相关类型定义
- 前端根据 `startTime`/`endTime` 与当前时间计算考核状态（未开始/进行中/已结束）
- 分数展示暂不实现，待评审系统完成后接入

## Capabilities

### New Capabilities
- `assessment-schedule-page`: 考生考核时间列表页面，包含后端进度接口扩展和前端页面实现

### Modified Capabilities
- `assessment-time-management`: 扩展 AssessmentTimeDTO 增加 totalQuestions 和 completedQuestions 字段

## Impact

- **后端 API**: 修改 `GET /api/v1/assessment-times` 响应结构（新增字段，向后兼容）；新增 `GET /api/v1/assessment-times/{id}/progress` 端点
- **后端 Repository**: AssessmentQuestionRepository 新增按 assessmentTimeId 统计题数的方法；AssessmentAnswerRepository 新增按 userId + assessmentTimeId 统计已完成题数的方法
- **前端路由**: 新增 `src/app/(public)/(other)/assessment/page.tsx`
- **前端类型**: 新增 `src/types/assessment.ts`
- **前端 API**: 新增 `src/apis/services/assessment-time.service.ts`
- **设计稿参考**: `docs/UI/assessment time.pen`
