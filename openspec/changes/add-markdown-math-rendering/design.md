## Context

前端只有一条标准 Markdown 渲染链，位于 `src/components/Assessment/MarkdownRenderer.tsx`：

```
react-markdown v10
  remarkPlugins: [remarkGfm]
  rehypePlugins: [rehypeSanitize]          ← 默认 GitHub schema
```

它被 5 处复用：考生题干、管理端题干编辑器预览、管理端查看态、AI 对话气泡、知识库分片预览。另有一条独立链路 `components/Enroll/PolicyModal.tsx`（手写 `react-markdown` + mermaid 特判），本变更不触碰。

当前约束：

- 仓库内零 `katex` / `mathjax` / `remark-math` 直接依赖，但 `pnpm-lock.yaml` 中已存在 `katex@0.16.45`（`mermaid@11` 的传递依赖），且 `mermaid` 已被 `PolicyModal` 静态引入，因此 KaTeX 的 JS 体积实际已在产物中。
- 算法题的输入说明 / 输出说明 / 数据范围存放在 `content` JSON 的 `inputDescription` / `outputDescription` / `constraints` 三个 `String` 字段（`AlgorithmContent`），后端 `normalizeQuestionContent()` 与 `validateQuestionContent()` 均不处理它们，仍是自由文本。
- 考生端这三段由 `AlgorithmQuestion.tsx` 以 `<p className="whitespace-pre-wrap">` 输出，处于 `md:grid-cols-3` 窄栏中。
- `MarkdownRenderer` 的组件样式全部硬编码（`h1` = `text-2xl`、`p` = `my-3 text-sm`、`table` 带边框），无样式变体入口。

## Goals / Non-Goals

**Goals:**

- 让所有走 `MarkdownRenderer` 的位置支持行内 `$...$` 与块级 `$$...$$` 公式。
- 保持既有 XSS 防护强度不变（原始 HTML 仍被剥离），同时不破坏 KaTeX 生成的排版结构。
- 公式语法错误只影响局部片段，不导致题面白屏。
- 考生端三栏说明获得与题干一致的 Markdown + 公式能力，且窄栏排版不溢出。
- 管理端三字段具备编辑与预览能力，抽屉高度与现状持平。
- 不改后端、数据库、接口契约。

**Non-Goals:**

- 不重构 `PolicyModal` 的第二条渲染链（留待 `refactor-question-statement-preview` 或更后续）。
- 不做管理端「左编辑 / 右长预览」整体重排版（属于 `refactor-question-statement-preview`）。
- 不处理 Markdown 渲染性能的防抖/延迟优化（同上，本次仅记录基线）。
- 不引入 mermaid-in-markdown 的通用能力（现有 mermaid 支持仍局限在 `PolicyModal`）。
- 不新增富文本粘贴、公式可视化编辑器、公式实时补全等编辑辅助。

## Decisions

### D1. 插件顺序：`remarkMath → rehypeSanitize → rehypeKatex`（不需要改净化 schema）

这是本变更最关键的技术决策，实施阶段已实测验证。

已核对 `hast-util-sanitize@5` 的 `defaultSchema` 源码，它：

- `tagNames` 不含任何 MathML 标签（`math` / `mrow` / `mi` / `mo` / `annotation` / `semantics` …），
- `attributes.span` 未定义，`span` 只能继承 `'*'` 白名单，而该白名单**不含 `className` 与 `style`**，
- 全局无 `style` 放行。

后果：若让 KaTeX 输出再经过 sanitize，`<span class="katex">`、`<span class="mord" style="...">` 会失去所有 class 与 style，MathML 会被拆标签只留裸文本 —— 公式变成一堆挤在一起的乱码，**比现状更差**。

两个候选方案：

| 方案 | 做法 | 结论 |
|---|---|---|
| A. katex 先、sanitize 后 | 把 KaTeX 输出也交给 sanitize 清洗，为此在 schema 中放行约 30 个 MathML 标签 + `span` 的 `className`/`style` | **否决（已实测）**。实测产物中所有 katex class 与内联 style 均被剥离，`math` 标签消失，公式完全不排版；放行 `style` 又等于放开 CSS 注入面，而本渲染器同时服务 AI 大模型输出（`ChatBubble`），风险不可接受 |
| B. sanitize 先、katex 后（采用） | 先净化 markdown 派生的不可信 HTML，再让 KaTeX 从纯数学字符串生成其后代结构 | **采用**。KaTeX 输出由 KaTeX 自身从已净化的文本生成，不含用户原始 HTML；sanitize 仍然完整守护不可信输入 |

**实施修正（推翻原设计假设）**：本变更最初假设 remark-math 输出 `<span class="math math-inline">`、因此需要给 schema 打 `span.className` 补丁。**实测证明这个前提是错的**：

- remark-math v6 实际输出 `<code class="language-math math-inline">`（行内）与 `<pre><code class="language-math math-display">`（块级）。
- `defaultSchema` 已通过 `code: [['className', /^language-./]]` 放行 `language-math`。
- 块级公式的 display 模式由 rehype-katex 依据 `pre > code.language-math` 的祖先关系恢复，不依赖 `math-display` class。

因此**采用零 schema 修改（直接使用默认 schema）**：净化强度最大化，代码更简单。已实测对比「无补丁」与「候选补丁（`code` 放行 `math-inline`/`math-display`）」，两者产物完全一致，补丁属于无效配置。

**必须守住的不变式**：该结论依赖「remark-math 输出 `code.language-math`」+「块级公式有 `pre` 包装」两个事实。升级 `remark-math` / `rehype-katex` 时必须重跑以下验收：行内公式排版、块级公式为 display 模式、代码块内 `$` 不被渲染、原始 HTML 仍被剥离、坏公式局部标错。任一项失败的表现都是**静默降级**（不报错、不白屏）。

### D2. 单 `$` 与双 `$$` 同时启用（块级 `$$` 必须独占行）

`remark-math` 默认 `singleDollarTextMath: true`，即 `$x$` 为行内公式。保持默认。

**实测得到的语法边界（已写入文档与示例）**：

| 写法 | 结果 |
|---|---|
| `$1 \le n \le 10^5$` | 行内公式 |
| 三行形式：`$$` 独占一行 / 公式独占一行 / `$$` 独占一行 | **块级（display）公式** |
| 单行 `$$O(n)$$` | **行内公式**（不是块级） |

即：只有 `$$` 后面紧跟换行的围栏形式才触发块级渲染；`$$...$$` 挤在一行时 micromark 按行内公式处理。这是 micromark-extension-math 的 flow 规则，不可配置，只能靠文档引导。


理由：参数约束 `$1 \le n \le 10^5$` 是最自然的写法，管理员不需要为"行内"多打一个 `$`；关掉单 `$` 会让数据范围这类短表达式全部被迫写成块级。

代价与缓解：见 Risks 的存量 `$` 冲突项。

### D3. KaTeX 错误降级：`throwOnError: false` + `errorColor`

KaTeX 默认 `throwOnError: true`，一个写错的 `\frac{1}` 会抛出异常，在 `react-markdown` 渲染期直接让整块题面白屏。对题目类内容这是不可接受的失败模式。

采用 `{ throwOnError: false, errorColor: '#ff4d4f' }`：错误公式以红色原文内联呈现，管理员与考生都能看到"这段写错了"，其余内容正常。

### D4. `compact` 作为渲染器的显式变体，而不是给调用方开放 `components` 覆盖

考生端三栏是 1/3 宽的窄卡（抽屉/页面中约 300–360px），`MarkdownRenderer` 默认的 `h1: text-2xl`、`p: my-3`、`ul: pl-6` 会明显撑破卡片。

候选方案：

| 方案 | 结论 |
|---|---|
| 让调用方传 `components` 覆盖 | 否决。每个调用点都要重写一份排版，随渲染器演进而漂移 |
| 用父级 CSS 的 `[&_h1]` 等 Tailwind 任意变体覆盖 | 否决。依赖选择器优先级，可读性差，且会把排版规则拆散到两个文件 |
| 渲染器内置 `compact?: boolean` 变体（采用） | 采用。排版规则与渲染器同源，调用方只声明意图 |

`compact` 的语义限定为**间距与标题层级收紧**（减小 `h*` 字号与上下 margin、收紧 `ul/ol` 缩进与行距），**不改变字号下限与颜色**，保证可读性。`compact` 也是后续 `refactor-question-statement-preview` 中管理端预览复用考生端组件时必须存在的契约，因此本次先建立。

### D5. 管理端三字段：复用 `QuestionStemMarkdownEditor rows={3}` 作为过渡态

`QuestionStemMarkdownEditor` 的高度计算是 `rows * 24 + 24`，因此 `rows={3}` = 96px，与现状三个 `Input.TextArea rows={3}` **完全等高**，抽屉不会变长；桌面端左右分栏（各约 528px，容得下公式），移动端自动降级为编辑/预览 Tab。

选择复用而非新建轻量编辑器：单调用点（`QuestionDrawer.tsx` 经 `renderStemField` 使用），复用即刻获得"编辑态实时预览 + 查看态只读渲染"两个分支的既有正确实现。

已知不理想之处（明确接受）：三个字段各自带一套左右分栏，视觉上略碎；查看态抽屉宽度仅 500px，公式可能横向溢出 —— 因此查看态渲染容器需要 `overflow-x-auto`。整体重排版由变更 B 承担。

### D6. 三段说明的渲染承接组件

考生端三处 `<p className="whitespace-pre-wrap">` 直接替换为 `<MarkdownRenderer content={...} compact />`，沿用 `AlgorithmQuestion` 现有的"字段非空才渲染该栏"的条件（`inputDescription && …`），不改动三栏 grid 结构与卡片边框。

未采用"把三段合并成一段联合 markdown 渲染"：那会改变字段归属与配置语义，且需要后端契约变更。

## Risks / Trade-offs

- **[存量正文中成对的 `$` 被误判为公式]** → 实施阶段执行只读扫描：对 `tb_assessment_question.content` 与 `tb_knowledge_chunk.content` 统计含 `$` 的行，人工抽查命中项；对确属误判的内容在管理端改写为 `\$` 转义。上线前无自动化判据，允许人工兜底。
- **[sanitize 顺序写反导致公式静默不显示]** → 实施时以「同时含公式与链接/表格的题面」作为验收样本，人工确认三件事同时成立：公式渲染、Markdown 结构保留、原始 HTML 被剥离。
- **[KaTeX 引入后管理端实时预览输入卡顿]** → 本次只记录基线（在真实题目上评估击键延迟）；若明显影响录入，后续变更 B 的 `useDeferredValue` 防抖即为解药。不在本次引入防抖，避免与 B 的布局重构叠加改动。
- **[katex 版本与 mermaid 传递依赖版本分裂]** → 显式声明 `katex` 为直接依赖并锁定 `0.16.45`，与 lockfile 现有版本一致，避免 pnpm 装入两份 KaTeX。
- **[KaTeX 字体资源在 Docker 产物中缺失导致公式排版错位]** → 验收必须在容器化产物上完成，而不是只在 `next dev` 下确认；字体缺失的表现是符号间距异常而非报错。
- **[compact 变体的排版回归]** → 考生端三栏只有 `md:` 断点以上为三列，移动端为单列堆叠；两个断点都要验收，避免 `compact` 在宽栏下显得过于局促。

## Migration Plan

1. 引入依赖与 `katex.min.css`，改造 `MarkdownRenderer`（D1/D2/D3/D4），此时其他调用点行为不变（无公式内容时输出与现状一致，`compact` 默认为 `false`）。
2. 抽查存量 `$` 数据（Risks 第一条），必要时修改内容。
3. 考生端三栏切换到 `MarkdownRenderer compact`。
4. 管理端三字段切换到 `QuestionStemMarkdownEditor`。
5. 更新 `docs/05-参考手册/05-03-算法判题指南.md` 的字段说明。
6. 端到端验收：容器化前端产物 + 真实题目，覆盖桌面/移动两个断点、公式正确/公式错误/含原始 HTML 三类样本。

**回滚策略**：本变更对后端零改动，内容字段仍为字符串。回滚只需回退前端提交；已录入的含 `$` 题面在回滚后表现为字面文本，不会造成数据损坏。

## Open Questions

- KaTeX 字体资源在现有 Next 构建（Docker + standalone 输出）中的产出路径是否完整，需要在实施时以容器产物实测确认，而不是依赖本地 `dev` 行为。
- AI 对话气泡与知识库分片预览同样会开始渲染公式；大模型输出里的 `$` 使用习惯不可控，若误判过多，是否需要为这两个调用点单独关闭单 `$`（`compact` 之外再加一个"公式档位"参数），留待观察后再定。
