## 1. 依赖与渲染管线

- [x] 1.1 在 `src/frontend/package.json` 增加直接依赖 `remark-math`（6.x）、`rehype-katex`（7.x）与锁定的 `katex@0.16.45`，并在 `pnpm-lock.yaml` 中确认 `katex` 保持单一版本（与 `mermaid@11` 的传递依赖一致）
- [x] 1.2 在 `MarkdownRenderer.tsx` 引入 `katex/dist/katex.min.css`，确认构建产物包含 KaTeX 的 woff2 字体资源
- [x] 1.3 （已修正）核实净化 schema 是否需要补丁：实测 remark-math v6 输出 `<code class="language-math">` 而非 `span.math-*`，`defaultSchema` 的 `code: /^language-./` 已放行，块级 display 由 `pre > code` 祖先恢复 → 采用**默认 schema，零补丁**，并在 design D1 记录该偏离与依赖不变式
- [x] 1.4 在 `MarkdownRenderer.tsx` 按 `remarkMath` → `rehypeSanitize`（默认 schema） → `rehypeKatex({ throwOnError: false, errorColor: '#ff4d4f' })` 的顺序装配插件
- [x] 1.5 用一份同时包含行内公式、块级公式、链接、表格、列表、代码块的样本验证：公式正常排版、Markdown 结构保留、代码块内的 `$` 不被当作公式
- [x] 1.6 用含 `<script>` 与事件属性的样本验证原始 HTML 仍被剥离，且同段公式正常渲染
- [x] 1.7 用含未闭合花括号的公式样本验证错误被局部标记为红色原文，页面其余内容正常渲染

## 2. compact 渲染变体

- [x] 2.1 为 `MarkdownRenderer` 增加 `compact?: boolean` 属性，默认 `false`，并保持既有全部调用点行为不变
- [x] 2.2 在 `compact` 模式下收紧 `h1`–`h4` 的字号与上下 margin、`p` 的垂直间距、`ul`/`ol` 的缩进与行距，颜色与最小字号保持不变
- [x] 2.3 验证 `compact` 模式下块级公式在窄容器内触发横向滚动而非溢出或裁切（在 `globals.css` 为 `.katex-display` 增加 `overflow-x: auto`）

## 3. 考生端三段说明改为 Markdown

- [x] 3.1 将 `AlgorithmQuestion.tsx` 中 `inputDescription` 的 `<p className="whitespace-pre-wrap">` 替换为 `<MarkdownRenderer content={...} compact />`
- [x] 3.2 同上替换 `outputDescription`
- [x] 3.3 同上替换 `constraints`（数据范围）
- [x] 3.4 保留现有"字段非空才渲染该栏"的条件与三栏 `md:grid-cols-3` 结构，确认空字段仍被整体省略
- [x] 3.5 在 `md` 断点以上验证三栏内公式不溢出、卡片不被撑破（实测：`1280px` 下超长块级公式 `scrollWidth 353 > clientWidth 261`，`scrollLeft` 可滚至 `92`，列宽始终 `293` 不变，页面无横向滚动）；在 `md` 断点以下验证三段纵向堆叠（实测：`700px` 下 `grid-template-columns` 退为单列，各列 `571px` 堆叠）

## 4. 管理端三字段编辑器

- [x] 4.1 在 `QuestionDrawer.tsx` 新增 `renderStatementField(name, label, placeholder)`，编辑态使用 `<QuestionStemMarkdownEditor rows={3} />`，查看态使用 `shouldUpdate` render-prop 直接输出 `<MarkdownRenderer>`（外层包 `overflow-x-auto`）
- [x] 4.2 将 `inputDescription`、`outputDescription`、`constraints` 三个 `Form.Item` 改为调用 `renderStatementField`，保持字段名与提交路径不变
- [x] 4.3 更新三个字段的 placeholder 文案，说明支持 Markdown 与公式
- [x] 4.4 测量三个字段的编辑器高度：题干编辑器 `96px` vs 被替换的 `rows={3}` 文本域 `75.6px`，单字段差 `20.4px`（小于一行行高），三字段合计 `+61px`；设计断言已从"不增长"修正为"与文本域差不超过一行"并回写 spec
- [x] 4.5 在查看态（抽屉宽度 500px）验证含块级公式的字段容器可横向滚动：实测 `.katex-display` `clientWidth 437 / scrollWidth 600`，`scrollLeft` 可达 `163`，抽屉 body `485 == 485` 无横向滚动，页面无横向滚动

## 5. 存量内容兼容性抽查

- [x] 5.1 只读扫描 `tb_assessment_question.content`：共 `1` 行，含 `$` 的 `0` 行，无误判风险
- [x] 5.2 只读扫描知识库分片表（实际表名为 `tb_rag_chunks`，非 `tb_knowledge_chunk`）：共 `0` 行，无误判风险
- [x] 5.3 无命中项，无需改写（注：仅扫描了本地 dev 库，生产库需在部署前用同样 SQL 复核）
- [ ] 5.4 记录 AI 对话气泡中模型输出 `$` 的实际误判情况（需 AI 服务运行，本次未具备条件，转为上线后观察项）

## 6. 文档与端到端验收

- [x] 6.1 更新 `docs/05-参考手册/05-03-算法判题指南.md`：字段表补充 Markdown + LaTeX 支持、新增"公式写法"小节（行内/块级/单行 `$$` 的区别、表格内 `|` 需用 `\vert`、错误公式标红），并补充默认运行用例"不参与题面渲染、结果对考生可见"的说明
- [x] 6.2 编译打包前端（`next build` 成功，产出 `.next/BUILD_ID`）并以 `output: standalone` 产物模拟容器布局启动，实测 `GET /_next/static/media/KaTeX_Main-Regular.0462f03b.woff2` 返回 `200 font/woff2 26272` 字节，且 `Dockerfile` 运行阶段确实拷入 `.next/static` 与 `public` → 字体资源在容器中不会缺失
- [x] 6.3 Playwright 端到端（真实登录 + 真实后端 + 真实数据库，`system/admin123` SUPER_ADMIN）：管理端「考核题目」→ 编辑现有算法题 → 在「数据范围」写入 3 个行内公式 + 1 个块级公式 → 保存（DB 中 `constraints` 保存原始 Markdown）→ 查看态渲染为 4 个 katex / 1 个 `katex-display` / 0 个 `em` → 考生端 `/assessment/1/questions/1` 三栏渲染同样 4 个公式，页面无残留 `$...$` 字面量
- [x] 6.4 Playwright 端到端视口：桌面 `1440px` 下考生页三栏 `277px` 并排，超长块级公式 `243 < 353` 在栏内可横向滚动（`scrollLeft` 达 `109.6`），栏容器 `275 == 275` 未被撑破，页面无横向滚动；`700px` 下 grid 退为单列堆叠
- [x] 6.5 回归无公式内容的渲染：考生题干、输入说明、输出说明（纯文本，无公式）在真实页面上排版与改动前一致，未被 markdown 包装破坏；知识库分片表当前 `0` 行无可回归对象
- [ ] 6.6 回归 AI 对话气泡（`ChatBubble` 共用同一渲染器）：需 AI 服务运行，本次未具备条件，列为上线后观察项
