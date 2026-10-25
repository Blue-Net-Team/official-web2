## Purpose

定义考生考题详情页（`/assessment/[timeId]/questions/[questionId]`）在四类题型（文件上传 / 单选 / 多选 / 算法）下的展示与交互要求：题干唯一渲染来源、共享题面组件与 Markdown/LaTeX 数学渲染、各题型作答工作区、算法题运行与提交链路、判题结果展示，以及页面容器与内层元素的视觉分层规则。

## Requirements

### Requirement: Algorithm question editor experience for candidates
The assessment question detail page SHALL render an algorithm question workspace with examples, language selector, starter code, code editor, run controls, submit controls, and result panels. The question stem SHALL be rendered by the shared stem card instead of the algorithm workspace. The input description, output description, constraints, and examples SHALL render through shared stateless statement components; the three sections and each example's explanation SHALL render as Markdown with LaTeX math support, while example input and expected output SHALL remain verbatim text.

#### Scenario: Candidate opens algorithm question with one template
- **WHEN** the algorithm question provides only a Python starter template
- **THEN** the page SHALL select Python and prevent choosing unconfigured languages

#### Scenario: Candidate opens algorithm question with multiple templates
- **WHEN** the algorithm question provides multiple starter templates
- **THEN** the page SHALL allow the candidate to choose one of the configured languages

#### Scenario: Algorithm workspace heading
- **WHEN** the algorithm answer workspace is rendered
- **THEN** its heading SHALL be an action phrase consistent with the other answer panels and MUST NOT repeat the question type label already shown in the page header

#### Scenario: Statement sections render Markdown with math
- **WHEN** a candidate opens an algorithm question whose input description, output description, or constraints contain Markdown and LaTeX math
- **THEN** the page SHALL render those sections as formatted content with typeset formulas instead of literal Markdown and formula source text

#### Scenario: Example explanation renders Markdown with math
- **WHEN** an algorithm question's statement example carries an explanation containing Markdown and LaTeX math
- **THEN** the page SHALL render that explanation with typeset formulas instead of literal formula source text, and the example input and expected output SHALL stay verbatim

#### Scenario: Example without explanation
- **WHEN** a statement example has no explanation or only whitespace
- **THEN** the example block renders input and expected output only, with no empty explanation area

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

面板内部的元素 MUST 按"容器 / 内层格子 / 内联点缀"三级处理：仅容器级元素使用 SmokedGlassCard；内层格子去底留框；内联点缀与 antd 原子组件保持原样。

#### Scenario: 内层格子去底留框

- **WHEN** 面板内存在嵌套的"面板状"内层容器（题目导航格、提交记录格、用例格等）
- **THEN** 移除其 `bg-white/[0.03]`~`bg-white/[0.06]` 透明底色，保留边框并加强至 `border-white/[0.15]`，保证在 DarkVeil 光斑透色的半透明实底上仍可分层

#### Scenario: 内联点缀零改动

- **WHEN** 面板内存在 blockquote 引用条、行内代码、代码块（`bg-black/30`）、彩色状态标签（AC/WA/选中态等）
- **THEN** 这些元素保持原有样式不改，仅随外层容器换底后自然呈现

#### Scenario: antd 原子组件零改动

- **WHEN** 面板内存在 antd 原子组件（Select、Tag、Button、Upload、Collapse、Tooltip、Modal 等）
- **THEN** 组件样式由全局 ConfigProvider（darkAlgorithm）token 驱动，不做任何组件级样式覆盖

### Requirement: 考生端题干单一来源

考生考题作答页的题干内容（`question.content.content` 的 Markdown）SHALL 只由通用「题目描述」面板渲染一次；各题型的作答面板 MUST NOT 再次渲染题干 Markdown。

#### Scenario: 算法题仅出现一份题面

- **WHEN** 考生打开一道算法题的作答页
- **THEN** 页面上题干文本出现且仅出现一次，位于「题目描述」面板内

#### Scenario: 算法题作答面板不含题干

- **WHEN** 考生查看算法题作答面板
- **THEN** 面板自上而下依次为输入说明/输出说明/数据范围（存在时）、样例列表（存在时）、语言选择、代码编辑器、运行与提交控件，且不含题干 Markdown

#### Scenario: 文件上传题与选择题题面唯一

- **WHEN** 考生打开文件上传题、单选题或多选题的作答页
- **THEN** 题干文本同样只出现在「题目描述」面板内，作答面板只呈现该题型的交互元素

#### Scenario: 题干为空

- **WHEN** 题目的题干 Markdown 为空或仅含空白字符
- **THEN** 「题目描述」面板 SHALL 显示占位文案，且该面板仍然渲染以承载题目标题

### Requirement: 上传约束仅对文件上传题可见

文件上传相关约束（允许的文件类型、最大文件大小、压缩包格式提示）SHALL 只在文件上传题（`FILE_UPLOAD`）的作答场景中出现；算法题与选择题页面 MUST NOT 出现任何文件类型或文件大小提示。

#### Scenario: 算法题不显示文件类型

- **WHEN** 考生打开算法题作答页
- **THEN** 页面上不存在「允许的文件类型」「最大文件大小」等信息，也不出现压缩包扩展名列表

#### Scenario: 选择题不显示文件类型

- **WHEN** 考生打开单选题或多选题作答页
- **THEN** 页面上不存在任何文件类型或文件大小提示

#### Scenario: 文件上传题保留上传约束

- **WHEN** 考生打开文件上传题作答页
- **THEN** 允许的扩展名列表与最大文件大小提示在上传作答区内可见，且不在「题目描述」面板内重复出现

#### Scenario: 后端未返回扩展名

- **WHEN** 题目内容未提供 `allowedExtensions`
- **THEN** 上传作答区使用系统压缩包白名单展示提示，且该校验规则与上传组件的 `accept`/`beforeUpload` 保持一致
