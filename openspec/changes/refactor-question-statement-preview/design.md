## Context

考生端题面渲染现状（`src/components/Assessment/QuestionDetail/`）：

| 内容 | 当前实现 | 阻碍 |
|---|---|---|
| 题干卡 | `index.tsx:917-929`（`{/* 题目描述 */}` 起的 `SmokedGlassCard`）内联 | 夹在附件下载、判题结果等兄弟节点之间，无法单独复用 |
| 三栏说明 + 样例 | `AlgorithmQuestion.tsx:68-114` 内联（三栏 grid 起于 71，样例 map 起于 98） | 与语言选择、Monaco 编辑器、运行/提交控件、结果徽章同处一个 `SmokedGlassCard` |
| 选项列表 | `ChoiceQuestion.tsx:55-92` 内联（`options.map`） | 与"已提交/重新提交/已锁定/选中态"逻辑交织；选中态样式逐选项内联在 73-79 / 81-87 / 88-90 |

管理端现状（`src/app/admin/assessment/question/QuestionDrawer.tsx`）：

- 抽屉宽度：`<md` 为 `100vw`，查看态 `500px`，编辑/创建态 `min(1120px, calc(100vw - 48px))`。
- 算法题「题面信息」tab 的字段顺序：题干 → 输入说明 → 输出说明 → 数据范围 → 题面样例 → 默认运行用例 → 语言模板。
- 表单是单一 antd `Form` 实例，由 `Form.useWatch('questionType')` 驱动 `renderContentFields()` 切换题型字段，切换时 `setFieldsValue` 重置。
- 表单形状是扁平的（`algorithmContent` / `inputDescription` / `options` / `correctAnswer: number` / `starterCodeTemplates: {language, code}[]`），而考生端 DTO 是嵌套的（`content.type = 'algorithm'`、`content.constraints`、`correctAnswer: string`）。

依赖前置：`add-markdown-math-rendering` 提供的 `MarkdownRenderer` `compact` 变体是本变更右列预览的排版基础。

## Goals / Non-Goals

**Goals:**

- 考生端与管理端共用同一份题面渲染实现，消除两边漂移的可能。
- 管理端「题面信息」变成左侧纯编辑、右侧连贯长预览，预览像素级还原考生端版式。
- 四种题型统一该布局，管理端只有一个题面编辑范式。
- 预览在结构上无法泄漏正确答案、默认运行用例、语言模板、判题配置。
- 抽取对考生端是零视觉变化。

**Non-Goals:**

- 不改动判题配置、资源限制两个 tab 的字段与布局。
- 不做题面内容的富文本/WYSIWYG 编辑，仍以 Markdown 源码为唯一编辑对象。
- 不统一 `PolicyModal` 的第二条 Markdown 渲染链。
- 不引入服务端预览渲染或题面版本对比等功能。

## Decisions

### D1. 抽取边界：只抽"考生可见的题面陈述"，不抽作答工作区

组件归属按"是否只依赖题面数据"划分：

```
QuestionStatement/
  StemCard.tsx             props: { title, content, emptyText? }
  AlgorithmStatement.tsx   props: { inputDescription?, outputDescription?, constraints?, examples? }
  ChoiceStatement.tsx      props: {
                             options: string[]
                             selectedValues?: string[]      // 选中项，默认 []
                             disabled?: boolean             // 已提交锁定 / 已过期，默认 false
                             onSelect?: (option: string) => void   // 缺省即无交互
                           }
  index.ts
```

`ChoiceStatement` 的 props 必须**包含选中态**，这是对最初设计的修正：

选中态的视觉不是加在列表容器上的，而是逐选项内联的 —— 每个选项 `div` 的边框与底色（`ChoiceQuestion.tsx:73-79`）、序号圆点的底色与字色（`81-87`）、选项文字的字重与颜色（`88-90`）都随 `isSelected` 变化。因此"外层包裹 + 只传 `options`"无法复现选中效果，会直接违反本变更自己的「视觉输出不变」验收。

交互能力用 `onSelect` 是否传入来开关，而不是另加 `interactive` 布尔：

- 考生端传入 `onSelect`，展开后即产生 `cursor-pointer` 与 `hover:bg-white/[0.06]`（与原实现一致）；
- 管理端预览不传 `onSelect`，渲染出的就是不带指针与 hover 的静态选项列表。

单/多选差异由调用方在 `onSelect` 里决定（单选调用 `onSelectOption`、多选调用 `onToggleOption`），`ChoiceStatement` 本身不感知题型。

**行号说明**：上述行号取自本变更创建时的源码，实施前必须以当时的源码重算 —— 考生端题面相关代码会随其他变更持续位移。

明确排除在抽取范围之外的内容：语言选择（依赖 `starterCode` 与已确认的语言限制）、Monaco 编辑器、运行/提交控件、判题结果、已提交/可重提交状态、倒计时与队伍信息。

备选方案「把整个 `AlgorithmQuestion` 参数化成 readOnly 模式」被否决：那会把工作区状态与预览耦合，预览组件需要伪造 `onRun`/`onSubmit` 等回调，且判题结果面板会在预览里出现不该有的东西。

### D2. 预览的合成模型：题干卡 + 按题型的陈述组件

预览不是"四个字段各渲染一次再拼起来"，而是复刻考生端的合成顺序：

```
QuestionStatementPreview
  ├─ <StemCard title={form.title} content={form.<stemField>} />
  ├─ ALGORITHM     → <AlgorithmStatement ... /> + 样例列表
  ├─ SINGLE/MULTIPLE → <ChoiceStatement options={form.options} />
  └─ FILE_UPLOAD   → 无额外陈述块
```

这与考生端的节点顺序一一对应，因此像素级还原是结构性的，而不是靠人眼对齐。

### D3. 表单到预览的适配层：`buildPreviewQuestion(formValues)`

预览组件消费考生端 DTO 形状，因此需要一个纯函数适配器把扁平表单值映射过去：

- `FILE_UPLOAD` → `content = { type: 'file_upload', content: fileContent }`
- `SINGLE_CHOICE` / `MULTIPLE_CHOICE` → `content = { type, content: choiceContent, options }`，**不带 `correctAnswer` / `correctAnswers`**
- `ALGORITHM` → `content = { type: 'algorithm', content: algorithmContent, inputDescription, outputDescription, constraints, examples }`，**不带 `runTestCases` / `starterCode` / `testCases` / `timeLimit` / `memoryLimit`**

适配器复用 `QuestionDrawer` 中已有的 `buildContentFromForm` 的映射知识，但不复用它本身 —— 后者会写入答案字段，而预览必须没有。适配器刻意做成"只挑白名单字段"的白名单式构造，而非"从表单值里删除黑名单字段"，这样新增表单字段时默认不会泄漏进预览。

备选方案「预览组件直接接收表单值」被否决：那会让预览组件知道表单的内部形状，抽取出来的组件就不再是考生端可复用的同一份实现。

### D4. 只在右侧做一次合并渲染，并延迟求值

一次合并渲染有两个直接收益：KaTeX 每帧只排版一遍（而不是四个字段各一遍）；只有一处需要防抖。

采用 `useDeferredValue` 包裹预览所依赖的表单值，而不是手写 `setTimeout` 防抖：

- 左侧输入（纯 `TextArea`）保持同步响应，光标与输入永不延迟；
- 右侧预览在 React 空闲时追上最新值，长题面下不会阻塞击键。

放弃手写防抖的理由：`useDeferredValue` 不引入定时器与清理逻辑，且与 React 19 的并发渲染天然配合。

### D5. 滚动策略：抽屉 body 不滚，左右两列各自滚

抽屉内出现三层滚动（抽屉 body / 左列 / 右列）会互相打架。采用：

- 「题面信息」tab 内容区锁高度：`flex-1 min-h-0` 配合基于视口高度的 `calc()`，扣除抽屉 header、footer 与 tab 栏；
- 左列与右列各自 `overflow-y-auto`；
- 判题配置、资源限制两个 tab 保持自然高度（不使用锁高布局），由抽屉 body 滚动。

备选方案「右侧 `sticky`、跟随抽屉 body 滚动」被否决：在移动端与 `backdrop-filter` 环境下嵌套 sticky 表现不稳定，且右侧长预览会被左侧短内容提前"截断"。

### D6. 窄视口与查看态的降级

| 场景 | 行为 |
|---|---|
| 查看态（抽屉 500px） | 不显示编辑器，只渲染 `QuestionStatementPreview`，单栏纵向 |
| 编辑态且视口 `< md`（抽屉 `100vw`） | 单栏 + 顶部「编辑 / 预览」切换，避免左右各半宽导致两边都不可用 |
| 编辑态且视口 `>= md` | 左右双列 |

### D7. 考生端零视觉变化的保障方式

抽取不做"顺手优化"。验收方式是对同一道题在抽取前后截取相同视口的截图逐像素比对（题干卡、三栏、样例、选项四个区域分别比），任何差异都视为抽取错误而非改进。`SmokedGlassCard` 的 `--sgc-padding`、`radius`、图标与分隔线顺序必须原样搬运。

## Risks / Trade-offs

- **[抽取导致考生答题页视觉回归]** → D7 的截图比对作为硬性验收；抽取与布局重构分成两个提交，先合并"纯抽取 + 考生端引用"并通过回归，再合并"管理端布局"，使问题可二分定位。
- **[预览与考生端再次漂移]** → 预览不使用任何独立样式，只组合共享组件；代码审查时若发现预览里出现新的排版类名，即视为偏离。
- **[锁高布局在不同内容高度下出错]** → `calc()` 依赖抽屉 header/footer/tab 的实际高度，主题或文案变化会导致高度漂移；验收需覆盖含/不含附件、不同题型、不同 tab 的组合。
- **[延迟渲染使管理员误以为预览"没跟上"]** → 延迟幅度以用户感知阈值为准；若实测有滞后感，退化为在预览角落显示一个轻量的"更新中"指示，而不是取消延迟。
- **[过渡态编辑器的去留]** → `QuestionStemMarkdownEditor` 在管理端被整体布局取代；若其他位置仍有使用则保留，无使用则移除。移除属于清理动作，不得与布局重构混在同一提交。
- **[表单值到 DTO 的映射漂移]** → `buildPreviewQuestion` 与 `buildContentFromForm` 存在映射知识的重复；通过"白名单构造"约束泄漏风险，并在字段新增时以代码审查兜底，而非引入共享抽象（共享抽象会把答案字段带进预览路径）。

## Migration Plan

1. 提交一：新增 `QuestionStatement/` 三个组件，内容从三处内联代码原样搬运（含类名与结构）。
2. 提交一：考生端三处改为引用抽出的组件，不加新样式；执行截图比对回归。
3. 提交二：新增 `buildPreviewQuestion` 与 `QuestionStatementPreview`，先在管理端以"临时预览块"验证四类题型的渲染正确性。
4. 提交二：「题面信息」tab 重构为左编辑 / 右预览（含锁高滚动、`useDeferredValue`、窄视口降级）。
5. 提交三：清理过渡态编辑器（若已无使用方）。
6. 端到端验收：四种题型 × 编辑/查看 × 桌面/移动，共 16 个组合，逐项确认预览与考生端一致。

**回滚策略**：纯前端改动，后端零依赖。截图比对发现回归时回退对应提交；管理端布局与考生端抽取分属不同提交，可独立回退。

## Open Questions

- 「题面信息」tab 锁高后，判题配置 / 资源限制两个 tab 是否也需要与之视觉对齐（例如统一顶部高度），还是保持自然高度由抽屉 body 滚动 —— 需在实际页面里看一眼再定。
- 移动端单栏下的「编辑 / 预览」切换应当复用 antd `Tabs` 还是用分段控件（Segmented），需要与抽屉内已有的 tab 层级区分开，避免两级 tab 视觉混淆。
