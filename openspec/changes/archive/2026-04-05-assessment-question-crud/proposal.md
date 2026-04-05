## Why

考核系统中，`AssessmentQuestion` 的领域层（Entity、VO、Repository接口、DomainService）已存在，但缺少上层实现。管理员无法通过 API 对考题进行增删改查操作，考生也无法在前端查看考题目录。需要补齐后端 CRUD 接口并实现前端考题目录展示页，使考核系统形成完整的闭环。

## What Changes

- 新增管理端考题 CRUD 接口（`/api/v1/admin/assessment-questions`），支持创建、更新、删除、分页查询考题
- 新增用户端考题目录查询接口（`/api/v1/assessment-questions`），按考核时间ID分页查询考题列表
- 新增前端考题目录展示页（`/assessment/[timeId]/questions`），延续暗色毛玻璃风格，支持分页浏览
- 权限控制：考生仅看自己方向+年级的考题；团队成员可看所有；方向管理员可看所有并修改自己方向；超级管理员全部权限
- 前端考核列表页卡片点击跳转到考题目录页

## Capabilities

### New Capabilities

- `assessment-question-crud`: 考题后端 CRUD 接口（管理端 + 用户端），包含 Application Service、DTO、Converter、Controller
- `frontend-assessment-question-page`: 前端考题目录展示页，含分页、题型展示、答题状态

### Modified Capabilities

- `assessment-time-management`: 前端考核列表页卡片需增加跳转链接，导航至考题目录页

## Impact

- **后端 API**：新增 2 个 Controller（Admin + User），1 个 Application Service，DTO/Converter
- **前端页面**：新增 1 个动态路由页面 + API Service + 类型定义
- **数据库**：无变更，`tb_assessment_question` 表已存在
- **权限**：需复用现有 `@RequiresPermission` 注解体系
