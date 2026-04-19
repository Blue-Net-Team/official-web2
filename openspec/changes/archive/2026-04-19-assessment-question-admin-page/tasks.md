## 1. DTO 类型修正

- [x] 1.1 修正 `assessment.dto.ts` 中的 `SingleChoiceContent`：`description` → `content`，`correctAnswer: number` → `correctAnswer: string`
- [x] 1.2 修正 `MultipleChoiceContent`：`description` → `content`，`correctAnswers: number[]` → `correctAnswers: string[]`
- [x] 1.3 修正 `FileUploadContent`：移除 `allowedExtensions` 和 `maxFileSize` 字段，只保留 `content`
- [x] 1.4 修正 `AlgorithmContent`：移除 `supportedLanguages` 和 `template` 字段，添加 `testCases`（含 `input`/`expectedOutput`）、`timeLimit`、`memoryLimit`
- [x] 1.5 新增 `CreateQuestionRequestDTO` 和 `UpdateQuestionRequestDTO` 类型定义

## 2. 管理端 API 服务

- [x] 2.1 创建 `admin-assessment-question.service.ts`，实现 `getList(assessmentTimeId, page, size)` 方法
- [x] 2.2 实现 `create(data)` 方法
- [x] 2.3 实现 `update(id, data)` 方法
- [x] 2.4 实现 `delete(id)` 方法

## 3. 考题管理主页面

- [x] 3.1 创建 `/admin/assessment/question/page.tsx` 页面框架，引入 Steps、Table、Drawer 等组件
- [x] 3.2 实现 Step 1：方向选择（DIRECTION_ADMIN 自动跳过）
- [x] 3.3 实现 Step 2：考核时间列表选择（调用 `adminAssessmentTimeService.getList` 按方向过滤）
- [x] 3.4 实现 Step 3：考题表格（列：题号、题型 Tag、标题、分值、附件标识、操作按钮）
- [x] 3.5 实现权限控制：DIRECTION_ADMIN 不可操作非自己方向的考题
- [x] 3.6 实现删除确认 Modal

## 4. 考题编辑抽屉

- [x] 4.1 创建 `QuestionDrawer.tsx` 基础结构（Drawer + Form，三种模式：view/edit/create）
- [x] 4.2 实现通用字段：题号、题型选择、标题、分值、附件上传
- [x] 4.3 实现文件上传题型内容编辑（TextArea 题干）
- [x] 4.4 实现单选题内容编辑（TextArea 题干 + Form.List 选项 + Radio 正确答案）
- [x] 4.5 实现多选题内容编辑（TextArea 题干 + Form.List 选项 + Checkbox 正确答案）
- [x] 4.6 实现算法题内容编辑（TextArea 题干 + Form.List 测试用例 + InputNumber 时间/内存限制）
- [x] 4.7 实现题型切换时内容区域重置逻辑
- [x] 4.8 实现 view 模式只读展示 + 编辑/删除按钮
- [x] 4.9 实现表单提交逻辑（组装 content JSON，区分 create/update）

## 5. 集成验证

- [x] 5.1 验证 Steps 流程完整（方向 → 考核时间 → 考题列表 → 新增/编辑/删除/查看）
- [x] 5.2 验证 4 种题型表单数据正确组装为后端期望的 JSON 结构
- [x] 5.3 验证 DIRECTION_ADMIN 权限隔离
- [x] 5.4 验证移动端响应式布局
