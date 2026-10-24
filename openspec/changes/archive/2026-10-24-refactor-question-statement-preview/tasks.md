## 1. 抽取共享题面组件（考生端零变化）

- [x] 1.1 新建 `src/components/Assessment/QuestionStatement/StemCard.tsx`，从 `QuestionDetail/index.tsx:917-929` 原样搬运题干卡（`SmokedGlassCard` 的 `radius`、`--sgc-padding`、`FileTextOutlined` 颜色、标题、`hr`、`MarkdownRenderer` 全部保持不变），props 为 `{ title, content, emptyText? }`；实施前先重算行号
- [x] 1.2 新建 `QuestionStatement/AlgorithmStatement.tsx`，从 `AlgorithmQuestion.tsx:68-114` 原样搬出三栏说明与样例列表，props 为 `{ inputDescription?, outputDescription?, constraints?, examples? }`，条件渲染逻辑与类名不变；实施前先重算行号
- [x] 1.2a 搬运三栏时必须携带变更 A 的产物：三个字段均使用 `<MarkdownRenderer content={...} compact />`，且每个栏容器带 `overflow-x-auto`；MUST NOT 搬回 `<p className="whitespace-pre-wrap">`
- [x] 1.2b 确认 `MarkdownRenderer` 已由变更 A 提供 `compact` 属性；若 A 尚未落地则本任务阻塞，不得先搬运后补
- [x] 1.3 新建 `QuestionStatement/ChoiceStatement.tsx`，从 `ChoiceQuestion.tsx:55-92` 搬出选项列表的展示部分（选项序号圆点、选中态边框/底色/字色/字重全部逐项内联保留），props 为 `{ options, selectedValues?, disabled?, onSelect? }`；单/多选差异由调用方在 `onSelect` 内决定，组件本身不感知题型
- [x] 1.3a 校验 `ChoiceStatement` 能完整复现选中态：传入非空 `selectedValues` 时，每个选项的容器样式、序号圆点样式、文字样式与现状逐类名一致；不传 `onSelect` 时不出现 `cursor-pointer` 与 hover 效果
- [x] 1.4 新建 `QuestionStatement/index.ts` 导出三个组件
- [x] 1.5 `QuestionDetail/index.tsx` 改为引用 `StemCard`，删除内联题干卡代码
- [x] 1.6 `AlgorithmQuestion.tsx` 改为引用 `AlgorithmStatement`，删除内联三栏与样例；文件内仅保留代码工作区、语言选择、运行/提交控件与结果徽章
- [x] 1.7 `ChoiceQuestion.tsx` 改为引用 `ChoiceStatement`，把 `selectedValues`、`disabled`（已提交锁定或已过期）与 `onSelect`（单选走 `onSelectOption`、多选走 `onToggleOption`）传入；已提交/重新提交徽章与重提交按钮继续留在 `ChoiceQuestion` 内
- [x] 1.8 对四种题型分别在抽取前后以相同视口截图（题干卡、三栏、样例、选项四个区域），逐像素比对确认零视觉差异

- [x] 1.8a 实测（比截图更严）：抽取前后对 9 个区域做归一化 outerHTML 的 sha256 比对，全部 `MATCH` —— `q1.stemCard` `q1.grid` `q1.algHeader` `q1.examples[0]` `q2.stemCard` `q2.options` `q3.stemCard` `q3.options` `q4.stemCard`
- [x] 1.8b 补充验证选项交互：点击第 2 项后，容器底色、边框色、序号圆点、文字字重四层同时切换到选中态，其余项保持未选中

## 2. 预览适配层

- [x] 2.1 新增 `buildPreviewQuestion(formValues)` 纯函数，按白名单方式构造考生端 DTO 形状的题面对象，明确不写入 `correctAnswer`、`correctAnswers`、`runTestCases`、`starterCode`、`testCases`、`timeLimit`、`memoryLimit`
- [x] 2.2 为四种题型各写一个最小表单样本，确认适配输出只含题干、三段说明、样例与选项
- [x] 2.3 新增 `QuestionStatementPreview` 组件，按 `questionType` 组合 `StemCard` + `AlgorithmStatement` / `ChoiceStatement` / 无额外块；`ChoiceStatement` 固定传空 `selectedValues` 且不传 `onSelect`
- [x] 2.4 在管理端抽屉内以临时位置挂载预览，逐一确认四种题型的预览顺序与考生端一致

## 3. 管理端「题面信息」重构为左编辑 / 右预览

- [x] 3.1 将「题面信息」tab 内容改为两栏容器：左列全部编辑控件（题干、输入说明、输出说明、数据范围、题面样例、默认运行用例、语言模板），右列 `QuestionStatementPreview`
- [x] 3.2 实现锁高滚动：tab 内容区 `flex-1 min-h-0` + 基于视口高度的 `calc()` 扣除抽屉 header、footer 与 tab 栏，左右两列各自 `overflow-y-auto`，抽屉 body 不滚动
- [x] 3.3 用 `useDeferredValue` 包裹预览所依赖的表单值，使左侧输入保持同步响应、右侧预览延后追上
- [x] 3.4 低于 `md` 断点时改为单栏 + 编辑/预览切换，切换控件与抽屉内已有的一级 tab 在视觉上区分开
- [x] 3.5 查看态（抽屉 500px）只渲染 `QuestionStatementPreview`，不挂载任何编辑控件
- [x] 3.6 确认判题配置、资源限制两个 tab 不受锁高布局影响，仍保持自然高度由抽屉 body 滚动

## 4. 预览内容边界验证

- [x] 4.1 在左侧编辑默认运行用例，确认右侧预览不出现该用例的任何内容
- [x] 4.2 在左侧编辑正确答案（单选/多选），确认右侧预览不显示也不高亮答案
- [x] 4.3 在左侧编辑语言模板，确认右侧预览不出现模板代码与语言选择器
- [x] 4.4 在左侧修改判题配置与资源限制，确认右侧预览无变化

## 5. 过渡态清理

- [x] 5.1 确认 `QuestionStemMarkdownEditor` 在全仓已无使用方
- [x] 5.2 移除 `QuestionStemMarkdownEditor` 及其在 `components/Assessment/index.ts` 的导出
- [x] 5.3 确认移除后无遗留引用，构建通过

- [x] 4.5 实测记录：预览面板内 `textarea=0`、`.monaco-editor=0`、`.ant-select=0`，不含「默认运行用例」「语言模板」文案；改判题配置倍率 `1.2 → 1.5` 后预览文本哈希不变
- [x] 5.4 补充：左列改为纯 `Input.TextArea`（题干与三段说明），`QuestionStemMarkdownEditor` 全仓零引用后删除，`tsc --noEmit` 通过

- [x] 6.1a 文档补充：字段表标注「不出现在右侧预览」、新增「右侧预览的内容边界」与「预览与考生端的一致性」两段

## 6. 文档与端到端验收

- [x] 6.1 更新 `docs/05-参考手册/05-03-算法判题指南.md`：说明管理端题面区为左编辑 / 右预览，以及哪些字段会出现在预览、哪些不会
- [x] 6.2 Playwright 端到端：四种题型 × 编辑态/查看态 × 桌面/移动视口，逐项确认预览与考生端题面一致、预览不含不可见内容
- [x] 6.3 实测：写入含 10 项求和的块级公式时，左侧输入同步更新耗时 `27.1ms`；预览在主线程空闲后收敛（`0 → 1` 个 katex，约 `234ms`），输入路径无解析开销
- [x] 6.4 实测：预览栏内 `.katex-display` `106/600` 可滚至 `494`；查看态 500px 抽屉内 `90/600` 可滚至 `510`，抽屉 body 与页面均无横向滚动
- [x] 6.5 生产构建 `next build` 通过（BUILD_ID 产出），并以 `output: standalone` 产物按 Dockerfile 的拷贝方式（`static` + `public`）启动验证：`/admin/assessment/question` 与 `/assessment/1/questions/1` 返回 `307`（未带会话的正常重定向）、KaTeX 字体 `200 26272` 字节；产物中已无 `QuestionStemMarkdownEditor` 残留。未执行完整 `docker build`（无新增静态资源，打包路径与上一个变更一致）
