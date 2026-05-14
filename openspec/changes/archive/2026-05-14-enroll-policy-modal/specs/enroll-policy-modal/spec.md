## ADDED Requirements

### Requirement: 政策文档弹窗交互

报名页面 SHALL 在点击"报名须知"或"隐私政策"链接时弹出 Modal 展示对应文档内容。

#### Scenario: 点击报名须知链接
- **WHEN** 用户点击报名表单底部的"报名须知"链接
- **THEN** 弹出 Modal 展示报名须知内容
- **AND** Modal 标题显示为"报名须知"

#### Scenario: 点击隐私政策链接
- **WHEN** 用户点击报名表单底部的"隐私政策"链接
- **THEN** 弹出 Modal 展示隐私政策内容
- **AND** Modal 标题显示为"隐私政策"

#### Scenario: 关闭弹窗
- **WHEN** 用户点击 Modal 的关闭按钮或遮罩层
- **THEN** Modal 关闭
- **AND** 页面停留在报名表单

#### Scenario: 移动端适配
- **WHEN** 用户在移动端打开 Modal
- **THEN** Modal 宽度适配屏幕，内容可滚动
- **AND** 不超出视口边界

### Requirement: Markdown 内容渲染

Modal 内部 SHALL 将文档内容以 Markdown 格式渲染为 HTML，支持标准 Markdown 语法。

#### Scenario: 渲染标题和段落
- **WHEN** Modal 展示包含标题和段落的 Markdown 内容
- **THEN** 正确渲染为对应等级的 HTML 标题和段落

#### Scenario: 渲染表格
- **WHEN** Markdown 内容包含表格
- **THEN** 渲染为带有表头样式的 HTML 表格

#### Scenario: 渲染列表
- **WHEN** Markdown 内容包含有序列表或无序列表
- **THEN** 正确渲染为对应样式的列表

#### Scenario: 渲染加粗和强调文本
- **WHEN** Markdown 内容包含加粗或斜体文本
- **THEN** 正确应用加粗或斜体样式

#### Scenario: 内容安全性
- **WHEN** Markdown 内容包含潜在恶意 HTML
- **THEN** 危险标签和属性被过滤，仅渲染安全内容

### Requirement: Mermaid 流程图渲染

报名须知中的报名流程 SHALL 使用 Mermaid 流程图语法嵌入 Markdown，并在客户端渲染为 SVG 图形。

#### Scenario: 渲染 Mermaid 流程图
- **WHEN** Modal 展示包含 Mermaid 代码块的 Markdown 内容
- **THEN** Mermaid 代码块被解析并渲染为 SVG 流程图
- **AND** 非 Mermaid 代码块仍按普通代码块渲染

#### Scenario: Mermaid 渲染错误处理
- **WHEN** Mermaid 语法存在错误导致渲染失败
- **THEN** 显示原始代码块内容
- **AND** 不阻塞整个 Modal 的展示

#### Scenario: 流程图样式适配
- **WHEN** Mermaid 流程图渲染完成
- **THEN** SVG 图形在暗色主题下清晰可读
- **AND** 图形宽度适配 Modal 内容区域，可横向滚动（如需要）
