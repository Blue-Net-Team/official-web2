## Context

考核系统中 `AssessmentQuestion` 的领域层已存在（Entity、VO、Repository 接口/实现、DomainService 接口/实现），但缺少应用层、接口层的实现。前端有考核时间列表页 (`/assessment`)，需要新增考题目录页作为二级页面。

现有代码关键约束：
- Entity `AssessmentQuestion` 映射 `tb_assessment_question` 表，含 JSONB 字段 `content`（多态 `QuestionContent`）
- Repository 仅提供 `save`、`findById`、`updateAttachmentId`、`countByAssessmentTimeId` 四个方法
- 需要扩展 Repository 以支持分页查询、更新、删除、批量查询等操作
- 前端使用 Next.js App Router，暗色毛玻璃风格（CSS Modules），Ant Design 组件

## Goals / Non-Goals

**Goals:**
- 补齐考题 CRUD 后端接口（管理端全量 CRUD + 用户端只读列表）
- 实现前端考题目录展示页，支持分页
- 遵循现有 DDD 分层架构和代码风格
- 前端页面延续暗色毛玻璃设计风格

**Non-Goals:**
- 不做考题答题/提交功能（后续迭代）
- 不做考题内容编辑的前端界面（本次仅后端接口）
- 不做批量导入/导出考题
- 不改变现有 `QuestionContent` 多态结构

## Decisions

### 1. Repository 扩展策略

**决定**：在现有 `AssessmentQuestionRepository` 接口上新增方法，而非创建新接口。

**理由**：现有接口方法少（仅4个），直接扩展即可。新增：`findAllByTimeId(timeId, page, size)`、`update(question)`、`deleteById(id)`、`existsById(id)`。

### 2. 分层实现

沿用现有模式：
- **DTO 层**：`AssessmentQuestionDTO`（响应）、`CreateQuestionRequestDTO`（创建请求）、`UpdateQuestionRequestDTO`（更新请求）
- **Converter**：`AssessmentQuestionConverter` 负责 VO ↔ DTO 转换
- **Application Service**：`AssessmentQuestionService` 接口 + `AssessmentQuestionServiceImpl` 实现
- **Controller**：管理端 `AdminAssessmentQuestionController`（`/api/v1/admin/assessment-questions`）+ 用户端 `AssessmentQuestionController`（`/api/v1/assessment-questions`）

### 3. 前端路由设计

**决定**：使用 `/assessment/[timeId]/questions` 作为考题目录页路由。

**理由**：`/assessment` 已存在为考核列表页，`[timeId]` 动态路由标识具体考核时间，`questions` 子路径明确语义。

### 4. 分页方案

**决定**：后端使用 MyBatis-Plus `Page` 对象分页，前端使用自定义分页组件。

**理由**：与现有 `AssessmentTimeService` 的分页模式保持一致。前端考题列表每页默认 10 条。

### 5. 用户端接口权限

**决定**：用户端考题列表接口使用 `AccessLevel.AUTHENTICATED`，在 Application Service 层校验用户方向/年级是否匹配。

**理由**：考生只能看到自己方向+年级的考题，需要获取当前用户信息进行过滤。管理端使用 `AccessLevel.PROTECTED` + 具体权限标识。

## Risks / Trade-offs

- **[风险] Repository 扩展影响现有功能** → 仅新增方法，不修改已有方法签名，影响范围可控
- **[风险] 前端分页状态管理** → 使用 URL query params（`?page=1`）管理分页状态，支持浏览器后退
- **[取舍] 暂不实现考题排序** → 当前按 `question_no` 升序排列即可，后续可按需扩展
