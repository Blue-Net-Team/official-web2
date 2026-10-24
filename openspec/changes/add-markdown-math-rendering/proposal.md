## Why

算法题的题面本质上充满数学符号（`1 ≤ n ≤ 10^5`、`Σ aᵢ`、复杂度 `O(n log n)`），但当前前端 Markdown 渲染链只有 `remark-gfm` + `rehype-sanitize`，没有任何公式处理插件。管理员写下的 `$$...$$` / `$...$` 会原样以纯文本呈现，且公式中的 `*`、`\{`、`\\` 还会被 Markdown 行内规则静默吞掉，产生"能读但错了"的题面。

更突出的是渲染口径不一致：题面正文（`content.content`）走 Markdown 渲染，而紧挨着的输入说明、输出说明、数据范围三段是纯 `<p>` + `whitespace-pre-wrap`，完全没有格式化能力。

## What Changes

- **新增数学公式渲染能力**：`MarkdownRenderer` 接入 `remark-math` + `rehype-katex`，同时支持行内 `$...$` 与块级 `$$...$$`
- **修复 sanitize 与 KaTeX 的顺序冲突**：`rehype-sanitize` 默认 schema 会剥离 KaTeX 依赖的 `span.className` / `style` 与整套 MathML 标签，必须调整插件顺序并给 schema 打最小补丁，否则公式会渲染成乱码
- **公式渲染错误降级**：KaTeX 语法错误不得让整块题面白屏，错误片段以红色原文内联呈现
- **新增 `compact` 渲染变体**：为窄栏（考生端三栏卡片）提供收紧的标题/段落间距，避免 Markdown 默认排版撑破卡片
- **考生端三段说明升级为 Markdown**：`AlgorithmQuestion` 的输入说明、输出说明、数据范围由纯文本 `<p>` 改为 `MarkdownRenderer`（`compact`），从而与题面正文共享同一套公式与排版能力
- **管理端三字段获得编辑预览能力**：输入说明、输出说明、数据范围的 `Input.TextArea` 复用现有 `QuestionStemMarkdownEditor`（`rows={3}` 高度与现状一致，抽屉不变长），编辑态实时预览、查看态只读渲染
- **存量内容兼容**：开启单 `$` 后，历史正文中成对的 `$` 可能被误判为公式，需在实施阶段抽查存量题面与知识库分片

不改动：后端任何代码（`constraints` 等字段仍为 `String`，`normalize` / `validate` 逻辑不涉及它们）、数据库结构、判题链路、接口契约。

## Capabilities

### New Capabilities

- `markdown-math-rendering`: 前端 Markdown 渲染器对 LaTeX 数学公式的支持契约 —— 支持的定界符、插件与净化顺序、公式渲染失败时的降级行为、窄栏 `compact` 排版变体，以及对既有纯文本内容的向后兼容。

### Modified Capabilities

- `assessment-question-admin-ui`: 现有「Markdown question stem editing / preview / read-only rendering / Safe Markdown rendering」四条要求只覆盖题干；需扩展为覆盖算法题输入说明、输出说明、数据范围三个字段（编辑、实时预览、查看态只读渲染），并要求公式一并可用且仍受净化约束。
- `frontend-assessment-question-page`: 考生答题页算法题的输入说明、输出说明、数据范围当前以纯文本呈现；需改为 Markdown + 公式渲染，并明确窄栏下的排版约束与横向溢出处理。

## Impact

**前端**

- `src/components/Assessment/MarkdownRenderer.tsx` — 渲染管线与 `compact` 变体（唯一渲染核心改动点）
- `src/components/Assessment/QuestionDetail/AlgorithmQuestion.tsx` — 三段说明改渲染器
- `src/app/admin/assessment/question/QuestionDrawer.tsx` — 三字段编辑器替换
- 自动受益（无需改动）：考生题干、AI 对话气泡、知识库分片预览
- 依赖新增：`remark-math@6`、`rehype-katex@7`（`katex@0.16.45` 已作为 `mermaid` 的传递依赖存在于 lockfile，需提升为直接依赖并锁定同版本）
- 样式：引入 `katex/dist/katex.min.css`（含 woff2 字体资源）

**后端 / 数据库 / 判题服务 / 接口契约**：无改动

**文档**

- `docs/05-参考手册/05-03-算法判题指南.md` — 题面字段说明补充公式写法与示例

**风险**

- 存量正文中成对的 `$` 会被识别为行内公式（金额、代码片段、`$$` 符号本身）；需对 `tb_assessment_question.content`、`tb_knowledge_chunk` 做一次命中扫描与人工抽查
- 管理端题干编辑器的实时预览会在每次击键重新执行 KaTeX 排版，长题面可能出现输入延迟（本变更先记录基线，防抖优化在后续 `refactor-question-statement-preview` 中落地）
