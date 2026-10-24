## ADDED Requirements

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

## MODIFIED Requirements

### Requirement: Algorithm question editor experience for candidates
The assessment question detail page SHALL render an algorithm question workspace with examples, language selector, starter code, code editor, run controls, submit controls, and result panels. The question statement SHALL be rendered by the shared question description panel instead of the algorithm workspace.

#### Scenario: Candidate opens algorithm question with one template
- **WHEN** the algorithm question provides only a Python starter template
- **THEN** the page SHALL select Python and prevent choosing unconfigured languages

#### Scenario: Candidate opens algorithm question with multiple templates
- **WHEN** the algorithm question provides multiple starter templates
- **THEN** the page SHALL allow the candidate to choose one of the configured languages

#### Scenario: Algorithm workspace heading
- **WHEN** the algorithm answer workspace is rendered
- **THEN** its heading SHALL be an action phrase consistent with the other answer panels and MUST NOT repeat the question type label already shown in the page header
