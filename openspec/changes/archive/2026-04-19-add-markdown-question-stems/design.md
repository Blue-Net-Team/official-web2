## Context

考题内容的后端模型已经把题干存放在 `QuestionContent.content` 字符串中，前端 DTO 也暴露为 `content: string`。因此 Markdown 支持不需要改变数据库、接口路径或 JSON 字段结构，可以作为前端编辑和展示约定落地。

当前管理端 `QuestionDrawer` 在新增、编辑、查看考题时使用普通文本输入或展示题干。考生端题目详情页目前只对文件上传题展示题干，并把题干按换行拆成主描述和需求列表，这会破坏 Markdown 的标题、列表、代码块和表格语义。

## Goals / Non-Goals

**Goals:**

- 让所有题型的题干内容都可以按 Markdown 编写。
- 管理端新增/编辑时提供实时预览，桌面端使用宽抽屉双栏，移动端使用全屏抽屉和编辑/预览切换。
- 管理端查看详情和考生端题目详情都按 Markdown 渲染题干。
- 保持现有考题 API、数据库结构和后端领域模型不变。
- Markdown 渲染默认安全，不执行管理员输入中的原始 HTML。

**Non-Goals:**

- 不引入富文本编辑器或所见即所得编辑器。
- 不新增题干版本历史、草稿保存或协同编辑。
- 不改变题目标题、选项、答案、附件、测试用例等字段的业务含义。
- 不在本次变更中实现选择题或算法题的完整考生端答题能力。

## Decisions

### Use Markdown as a content convention, not a data model migration

`QuestionContent.content` remains a plain string. Existing plain text题干 remains valid Markdown, so no migration is needed.

Alternative considered: add a `contentFormat` field such as `markdown` or `plain_text`. This would make format explicit, but requires backend DTO/domain/storage changes and a migration story. Since existing content can be treated as Markdown safely, the extra field is not needed for this change.

### Create shared frontend Markdown rendering/editing primitives

Introduce reusable frontend components for Markdown rendering and question stem editing instead of embedding renderer logic separately in admin and public pages.

Recommended split:

- `MarkdownRenderer`: renders Markdown consistently with shared styling and sanitization.
- `QuestionStemMarkdownEditor`: integrates AntD form value editing with preview behavior.

Alternative considered: implement rendering inline in each page. That is faster initially but risks inconsistent sanitization, spacing, and dark-theme styling.

### Desktop uses wide drawer with side-by-side edit and preview

For create/edit mode on desktop, the drawer should expand to a wide responsive width and place the Markdown textarea beside the preview. This keeps long题干, code blocks, and tables inspectable while preserving the existing list-to-drawer workflow.

Alternative considered: move create/edit to a dedicated page. That gives more space, but increases navigation complexity and is unnecessary for this scoped enhancement.

### Mobile uses full-screen drawer with tabs

On mobile, side-by-side preview is not usable. The drawer should use `width: 100vw`; the题干 editor area should expose tabs for "编辑" and "预览", defaulting to edit mode. Preview is still available because Markdown syntax mistakes are common on mobile too.

Alternative considered: hide preview on mobile. This reduces UI work but weakens the key feature exactly where syntax is harder to inspect.

### Render Markdown safely

The Markdown renderer must not execute arbitrary HTML from题干 content. Prefer `react-markdown` with GFM support and sanitization, or an equivalent renderer configured to disallow unsafe HTML.

Alternative considered: allow raw HTML for richer formatting. This introduces unnecessary XSS risk because考题内容 is eventually shown to考生.

## Risks / Trade-offs

- Markdown dependencies increase frontend bundle size -> Use focused packages and avoid a full WYSIWYG editor.
- Existing plain text line breaks may render differently from the old split-list display -> Preserve normal paragraphs and support explicit Markdown lists; existing content remains readable.
- Dark theme Markdown styles can be inconsistent for tables/code blocks -> Centralize renderer styling and verify on admin and public pages.
- Sanitization can strip content administrators expected to work, especially raw HTML -> Document Markdown-first behavior and intentionally do not support raw HTML.

## Migration Plan

1. Add Markdown dependencies and shared renderer/editor components.
2. Replace management题干 textareas with Markdown editor instances.
3. Replace management view mode题干 display with Markdown renderer.
4. Replace考生端题干 split-line display with Markdown renderer.
5. Verify existing plain text题干 still renders legibly.

Rollback is straightforward: remove the Markdown components and restore the previous textarea/plain text rendering. No persisted data migration is required.

## Open Questions

- Should option text also support Markdown later, or remain plain text for simpler answer selection?
- Should attachments linked inside Markdown be supported through uploaded file IDs, or should附件 continue to be managed only through the existing attachment field?
