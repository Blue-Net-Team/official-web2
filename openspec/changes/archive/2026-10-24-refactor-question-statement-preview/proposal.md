## Why

管理端的题面预览目前只有"题干"一个字段有，而它是内嵌在每个 `Form.Item` 里的左右分栏编辑器。算法题的输入说明、输出说明、数据范围三个字段连预览都没有（`add-markdown-math-rendering` 用 `QuestionStemMarkdownEditor rows={3}` 做了过渡，形成四个字段各带一套分栏的碎片化布局）。管理员无法在一处看到"考生实际会看到的题面"。

根因是考生端的题面渲染没有独立组件：题干卡内联在 `QuestionDetail/index.tsx`，三栏与样例内联在 `AlgorithmQuestion.tsx`（且与代码工作区同处一个 `SmokedGlassCard`），选项列表内联在 `ChoiceQuestion.tsx`（且与提交态锁定逻辑交织）。因此管理端不可能以"再写一遍渲染"的方式做到像素级一致 —— 两边必然漂移。

## What Changes

- **抽取无状态的题面展示组件**：新增 `QuestionStatement/` 目录，包含 `StemCard`（题干卡）、`AlgorithmStatement`（三栏 + 样例列表）、`ChoiceStatement`（选项列表），均为纯展示、无提交态与判题逻辑
- **考生端改为引用抽出的组件**：`QuestionDetail/index.tsx`、`AlgorithmQuestion.tsx`、`ChoiceQuestion.tsx` 改为组合这些组件，视觉输出保持不变
- **管理端「题面信息」重构为左编辑 / 右预览**：左列为全部编辑控件（题干、三段说明、题面样例、默认运行用例、语言模板）独立滚动，右列为一块连贯的长预览区，像素级还原考生端题面版式
- **所有题型统一该结构**：文件上传、单选、多选、算法四种题型的题面区都使用同一套左编辑 / 右预览布局
- **新增表单到预览的数据适配层**：把扁平的表单值转换为考生端 DTO 形状，供预览组件消费
- **预览不泄漏考生不可见内容**：预览只渲染题干、三段说明、题面样例与选项，不含正确答案、默认运行用例、语言模板、判题配置
- **预览渲染去抖**：右侧预览使用延迟渲染，避免每次击键重新排版全部公式导致输入延迟
- **查看态与移动端降级**：查看态抽屉宽度不足时退化为单栏纯渲染；低于桌面断点时退化为编辑 / 预览两个页签

不改动：后端任何代码、接口契约、数据库结构、判题链路。

## Capabilities

### New Capabilities

- `question-statement-preview`: 管理端题面预览能力 —— 左侧编辑、右侧按考生端版式渲染的布局契约，预览内容边界（只含考生可见内容）、实时性要求、滚动与溢出策略，以及查看态与窄视口的降级行为。

### Modified Capabilities

- `assessment-question-admin-ui`: 现有「Markdown question stem editing / preview / read-only rendering」三条要求描述的是逐字段的编辑与预览；需改为题面区整体采用左编辑 / 右预览布局，预览为考生端版式的合并渲染，并保留查看态只读渲染要求。
- `frontend-assessment-question-page`: 考生答题页的题面、三栏说明、样例、选项当前由各作答组件内联渲染；需改为由共享的无状态题面组件渲染，且视觉输出与现状一致。

## Impact

**前端**

- 新增 `src/components/Assessment/QuestionStatement/{StemCard,AlgorithmStatement,ChoiceStatement,index}.ts`
- `src/components/Assessment/QuestionDetail/index.tsx` — 题干卡改为引用 `StemCard`
- `src/components/Assessment/QuestionDetail/AlgorithmQuestion.tsx` — 移除三栏与样例，仅保留代码工作区
- `src/components/Assessment/QuestionDetail/ChoiceQuestion.tsx` — 移除选项列表，仅保留提交态与选中逻辑
- 新增预览组件与管理端布局，落在 `src/app/admin/assessment/question/QuestionDrawer.tsx` 及新组件
- `src/components/Assessment/QuestionStemMarkdownEditor.tsx` — 过渡态编辑器在管理端被整体布局取代后可移除或降级保留

**依赖**：无新增。依赖 `add-markdown-math-rendering` 提供的 `MarkdownRenderer` `compact` 契约。

**后端 / 数据库 / 判题服务 / 接口契约**：无改动

**风险**

- 抽取涉及考生答题页三个核心组件，拆分错误会直接改变考生看到的页面；必须逐项比对抽取前后的 DOM 与样式
- 抽屉内左右双列各自滚动，与抽屉自身滚动叠加，容易出现滚动条打架或右侧预览被截断
