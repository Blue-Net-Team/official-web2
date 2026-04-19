## Why

考题题干目前只能按普通文本输入和展示，无法稳定表达列表、代码块、链接、表格等结构化内容。管理员录入考题时也缺少最终效果预览，容易在考生端出现格式偏差。

## What Changes

- 将考题 `content` 字符串约定为 Markdown 题干内容，保持现有后端 JSON 字段和 API 结构不变。
- 管理端新增/编辑考题时，题干输入提供 Markdown 实时预览。
- 管理端查看考题详情时，题干按 Markdown 只读渲染。
- 考生端题目详情页按 Markdown 渲染题干，不再把文件上传题题干按换行拆成首段和需求列表。
- 移动端管理编辑使用全屏抽屉和“编辑/预览”切换，桌面端使用宽抽屉和编辑预览双栏。
- Markdown 渲染必须禁用不受信任 HTML，并对输出做安全处理。

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `assessment-question-admin-ui`: 管理端考题抽屉的题干录入、预览、查看行为从普通文本升级为 Markdown。
- `frontend-assessment-question-page`: 考生端题目详情页的题干展示行为从纯文本/换行拆分升级为 Markdown 渲染。

## Impact

- Affected frontend files:
  - `src/frontend/src/app/admin/assessment/question/QuestionDrawer.tsx`
  - `src/frontend/src/app/(public)/(other)/assessment/[timeId]/questions/[questionId]/page.tsx`
  - Potential shared Markdown editor/renderer components under `src/frontend/src/components` or a local assessment component folder.
- Frontend dependencies likely need Markdown rendering packages, such as `react-markdown`, `remark-gfm`, and `rehype-sanitize`.
- No database migration is expected.
- No backend API shape change is expected.
