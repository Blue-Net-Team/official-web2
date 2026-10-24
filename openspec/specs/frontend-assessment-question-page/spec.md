## Purpose

考生答题页：渲染题面与各类题型作答工作区（代码作答、选项作答、文件上传），展示判题结果，并规定页面容器与内层元素的视觉分层规则。
## Requirements
### Requirement: Algorithm question editor experience for candidates
The assessment question detail page SHALL render an algorithm question workspace with statement, examples, language selector, starter code, code editor, run controls, submit controls, and result panels. The statement, input description, output description, constraints, and examples SHALL render through shared stateless statement components, and the three sections SHALL render as Markdown with LaTeX math support.

#### Scenario: Candidate opens algorithm question with one template
- **WHEN** the algorithm question provides only a Python starter template
- **THEN** the page SHALL select Python and prevent choosing unconfigured languages

#### Scenario: Candidate opens algorithm question with multiple templates
- **WHEN** the algorithm question provides multiple starter templates
- **THEN** the page SHALL allow the candidate to choose one of the configured languages

#### Scenario: Statement sections render Markdown with math
- **WHEN** a candidate opens an algorithm question whose input description, output description, or constraints contain Markdown and LaTeX math
- **THEN** the page SHALL render those sections as formatted content with typeset formulas instead of literal Markdown and formula source text

#### Scenario: Statement renders through shared components
- **WHEN** a candidate opens an algorithm question detail page
- **THEN** the stem card, the three statement sections, and the example blocks SHALL be rendered by the same shared statement components the admin preview uses, and the code workspace SHALL be rendered separately from the statement

#### Scenario: Statement visual output is unchanged by the extraction
- **WHEN** the statement rendering is moved into shared components
- **THEN** the candidate-facing DOM structure, spacing, borders, and icons of the stem card, statement sections, and example blocks SHALL remain identical to the previous inline rendering

#### Scenario: Statement sections stay inside their narrow columns
- **WHEN** a statement section containing a display formula is rendered in the three-column statement grid on a desktop viewport
- **THEN** the section SHALL use the compact Markdown layout and SHALL keep the formula inside the column bounds via horizontal scrolling rather than overflowing or breaking the grid

#### Scenario: Statement sections stack on narrow viewports
- **WHEN** a candidate opens an algorithm question on a viewport below the medium breakpoint
- **THEN** the statement sections SHALL stack vertically at full width and remain legible with the compact Markdown layout

#### Scenario: Empty statement sections are omitted
- **WHEN** an algorithm question leaves any of the input description, output description, or constraints empty
- **THEN** the page SHALL omit that section and SHALL still render the sections that have content

#### Scenario: Malformed formula in a statement section
- **WHEN** a statement section contains an invalid LaTeX expression
- **THEN** the page SHALL show the invalid expression highlighted as an error and continue rendering the remaining statement, examples, and answer controls

#### Scenario: Default run testcases are not rendered
- **WHEN** an algorithm question is configured with default run testcases
- **THEN** the candidate-facing statement SHALL NOT display those testcases and they SHALL only be executed when the candidate triggers a default run

### Requirement: Run algorithm code from question page
The assessment question detail page SHALL allow candidates to run algorithm code against system default testcases or custom input without submitting a scored answer.

#### Scenario: Run default testcase
- **WHEN** the candidate clicks run with system default testcase mode
- **THEN** the page SHALL create a run job, poll the job status, and display input, expected output, actual output, and status for each default testcase

#### Scenario: Run custom input
- **WHEN** the candidate clicks run with custom input
- **THEN** the page SHALL create a run job, poll the job status, and display stdout, stderr, status, time, and memory without showing correctness

### Requirement: Submit algorithm answer from question page
The assessment question detail page SHALL allow candidates to submit algorithm code as a formal answer and view the automatic judgement result.

#### Scenario: Submit algorithm answer
- **WHEN** the candidate clicks submit for an algorithm question
- **THEN** the page SHALL send the selected language and code, receive a judge job id, poll until completion, and show the judgement summary

#### Scenario: Hidden testcase fails
- **WHEN** the completed algorithm judgement includes failed hidden testcase details
- **THEN** the page SHALL display the failed input, expected output, actual output, and failure status

### Requirement: Automatic objective result display
The assessment question detail page SHALL display automatic judgement results for single choice, multiple choice, and algorithm answers after submission.

#### Scenario: Choice answer is submitted
- **WHEN** the candidate submits a single choice or multiple choice answer
- **THEN** the page SHALL show whether the answer was judged correct and the awarded score after the backend returns the judgement

### Requirement: 考生考题页面容器级玻璃材质统一为烟色玻璃

考生考题页面（assessment questions 列表页与 QuestionDetail 作答页）的所有容器级面板 SHALL 使用 `SmokedGlassCard` 组件渲染，MUST NOT 使用无底色纯透明玻璃配方（`bg-white/[0.0x]` + 细边框、无深色保底底色的容器壳）。考题页作为 DarkVeil 全屏场景，所有容器面板统一使用 deep（浓烟）档：不透明深色实底可阻挡背景光斑染色，保证任何帧下视觉稳定。

#### Scenario: 作答页容器面板

- **WHEN** 考生打开题目作答页
- **THEN** 题面主卡及页面所有容器面板（队伍、倒计时、提交、答题信息、算法题/选择题 section、编辑器容器、判题结果）均以 `<SmokedGlassCard>`（deep，不挂 `hoverable`）渲染

#### Scenario: backdrop-filter 失效环境

- **WHEN** 页面运行在 backdrop-filter 不生效的环境（如微信 Android XWEB 内核）
- **THEN** 所有面板仍因 SmokedGlassCard 的深色保底底色保持有形可辨识，不出现"融化"成背景的无形区域

#### Scenario: 面板内边距归属

- **WHEN** 容器面板迁移为 SmokedGlassCard
- **THEN** 删除面板内容根部的 Tailwind padding 工具类（`p-5`/`p-7` 等），内边距由组件默认 24px（`--sgc-padding`）统一提供

### Requirement: 面板内层元素分层规则

面板内部的元素 SHALL 按"容器 / 内层格子 / 内联点缀"三级处理：仅容器级元素使用 SmokedGlassCard；内层格子去底留框；内联点缀与 antd 原子组件保持原样。

#### Scenario: 内层格子去底留框

- **WHEN** 面板内存在嵌套的"面板状"内层容器（题目导航格、提交记录格、用例格等）
- **THEN** 移除其 `bg-white/[0.03]`~`bg-white/[0.06]` 透明底色，保留边框并加强至 `border-white/[0.15]`，保证在 DarkVeil 光斑透色的半透明实底上仍可分层

#### Scenario: 内联点缀零改动

- **WHEN** 面板内存在 blockquote 引用条、行内代码、代码块（`bg-black/30`）、彩色状态标签（AC/WA/选中态等）
- **THEN** 这些元素保持原有样式不改，仅随外层容器换底后自然呈现

#### Scenario: antd 原子组件零改动

- **WHEN** 面板内存在 antd 原子组件（Select、Tag、Button、Upload、Collapse、Tooltip、Modal 等）
- **THEN** 组件样式由全局 ConfigProvider（darkAlgorithm）token 驱动，不做任何组件级样式覆盖

