## Context

后端考题管理 API 已完整实现：
- `GET /admin/assessment-questions?assessmentTimeId=X` — 分页查询（必须指定考核时间）
- `POST /admin/assessment-questions` — 创建
- `PUT /admin/assessment-questions/{id}` — 更新
- `DELETE /admin/assessment-questions/{id}` — 删除
- `PUT /admin/assessment-questions/{id}/attachment` — 更新附件

前端考核时间管理页（`/admin/assessment/time`）已实现，使用 Table + Drawer 模式。导航菜单已配置"考核题目"入口指向 `/admin/assessment/question`，但页面不存在。

现有技术约束：
- 暗色主题，AntD ConfigProvider 全局配置，不自定义 style 样式
- 布局用 Tailwind class（`flex`、`gap-4` 等）
- 权限：DIRECTION_ADMIN 只能操作自己方向，SUPER_ADMIN 全部权限

## Goals / Non-Goals

**Goals:**
- 管理员通过 Steps 引导（方向 → 考核时间）筛选后管理考题
- 支持 4 种题型（单选、多选、文件上传、算法）的完整 CRUD
- 多态内容编辑：Drawer 表单根据题型动态渲染不同字段
- 前端 DTO 与后端 QuestionContent 多态结构对齐
- DIRECTION_ADMIN 权限隔离

**Non-Goals:**
- 考核评判页面（`/admin/assessment/judge`）不在此次范围
- 用户端答题界面的题型扩展（选择/算法题的答题 UI）
- 后端 API 变更（API 已就绪，不修改）

## Decisions

### D1: 使用 Steps 组件引导筛选

**选择**: AntD Steps 组件，Step 1 选方向 → Step 2 选考核时间 → Step 3 考题表格

**理由**:
- 后端 API 要求 `assessmentTimeId` 参数，必须先确定考核时间
- 考核时间数量有限（每方向每届几次），两步筛选体验优于级联下拉
- Steps 视觉上清晰表达层级关系

**替代方案**:
- 级联 Select：方向少时可用，但考核时间显示信息少（需要显示时间范围、状态等）
- 左侧树形菜单：过度设计，数据量小不需要

### D2: Drawer 表单 + 多态内容编辑

**选择**: 复用已有 Drawer 模式（与考核时间管理一致），内部使用条件渲染处理不同题型

**理由**:
- 与现有管理页保持一致
- Form.Item 条件渲染 + `Form.useWatch('questionType')` 监听题型变化
- 选项列表使用 `Form.List` 动态增删

**内容结构映射（后端 → 前端表单）**:

| 题型 | 后端 Content 结构 | 表单字段 |
|------|-------------------|----------|
| FILE_UPLOAD | `{type, content}` | TextArea |
| SINGLE_CHOICE | `{type, content, options[], correctAnswer}` | TextArea + Form.List(选项) + Radio |
| MULTIPLE_CHOICE | `{type, content, options[], correctAnswers[]}` | TextArea + Form.List(选项) + Checkbox |
| ALGORITHM | `{type, content, testCases[], timeLimit, memoryLimit}` | TextArea + Form.List(用例) + InputNumber×2 |

### D3: DTO 类型修正策略

**选择**: 直接修正 `assessment.dto.ts` 中的 Content 类型，与后端 `QuestionContent` 多态 JSON 结构对齐

后端使用 Jackson `@JsonTypeInfo(property="type")` 多态序列化，每个 Content 对象包含 `type` 字段：

```
{ "type": "single_choice", "content": "题干", "options": ["A","B"], "correctAnswer": "A" }
```

前端 DTO 需要修正：
- 所有 Content 类型使用 `content` 字段（非 `description`）
- `correctAnswer` 类型为 `string`（非 `number`）
- `correctAnswers` 类型为 `string[]`（非 `number[]`）
- 移除后端不存在的字段（`allowedExtensions`、`maxFileSize`、`supportedLanguages`、`template`）

### D4: 文件附件上传

**选择**: 使用统一文件上传流程（先上传获取 fileId，再关联到考题）

调用 `POST /api/v1/file/upload?type=ASSESSMENT_ATTACHMENT` 上传，获取 fileId 后在创建/更新考题时传入 `attachmentId`。

## Risks / Trade-offs

- **[Steps 增加操作步数]** → 对于熟悉的管理员，Step 1/2 可缓存上次选择，减少重复操作
- **[题型内容校验复杂]** → 前端表单校验确保 content 结构与题型匹配，后端也有校验兜底
- **[DTO 修正影响用户端页面]** → 用户端页面（`/assessment/[timeId]/questions`）使用 `AssessmentQuestionDTO` 的通用字段，不受 Content 类型影响；但如果用户端后续展示选择/算法题内容，需同步使用修正后的类型
