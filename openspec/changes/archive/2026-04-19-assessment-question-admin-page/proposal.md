## Why

后端考题管理 CRUD API 已完整实现（`/api/v1/admin/assessment-questions`），但前端缺少管理页面。导航菜单中"考核题目"入口已配置但指向的 `/admin/assessment/question` 页面不存在，管理员无法通过界面进行考题的创建、编辑、删除操作。

## What Changes

- 新增管理端考题 API 服务（`admin-assessment-question.service.ts`），对接后端管理端接口
- 新增考题管理页面（`/admin/assessment/question`），使用 Steps 组件引导选择方向 → 考核时间 → 考题列表
- 新增考题编辑抽屉（`QuestionDrawer`），支持 4 种题型（单选、多选、文件上传、算法）的多态内容编辑
- 修正前端 DTO 类型定义，与后端实际结构对齐（`QuestionContent` 多态结构）
- 权限控制：DIRECTION_ADMIN 仅管理自己方向的考题，SUPER_ADMIN 管理全部

## Capabilities

### New Capabilities

- `assessment-question-admin-ui`: 管理端考题管理页面，包含 Steps 导航、考题表格、多态内容编辑抽屉、4 种题型支持

### Modified Capabilities

- `frontend-assessment-question-page`: 修正 DTO 类型定义与后端对齐

## Impact

- **前端页面**：新增 1 个管理页面 + 1 个抽屉组件
- **前端 API**：新增 1 个管理端 API 服务文件
- **前端 DTO**：修正 `assessment.dto.ts` 中题型 Content 类型定义
- **后端**：无变更，API 已就绪
- **数据库**：无变更
