## Requirements

### Requirement: 考题目录页路由
系统 SHALL 在 `/assessment/[timeId]/questions` 路径提供考题目录展示页，其中 `[timeId]` 为考核时间 ID 的动态路由参数。

#### Scenario: 访问考题目录页
- **WHEN** 用户通过浏览器访问 `/assessment/1/questions`
- **THEN** 系统渲染考题目录页，加载 timeId=1 对应考核时间的考题列表

#### Scenario: 未登录访问考题目录页
- **WHEN** 未认证用户访问考题目录页
- **THEN** 系统重定向到登录页

### Requirement: 考题目录页头部信息
系统 SHALL 在考题目录页头部展示考核基本信息，包含返回导航、考核标题（轮次 + "考题目录"）、方向、年级、考核时间范围、状态徽章。

#### Scenario: 展示头部信息
- **WHEN** 考题目录页加载完成
- **THEN** 页面头部显示考核标题、方向标签、年级、时间范围和状态信息

### Requirement: 考题目录统计卡片
系统 SHALL 在考题列表上方展示统计信息卡片，包含题目总数、已作答数、未作答数、总分。

#### Scenario: 展示统计信息
- **WHEN** 考题目录页加载完成且有统计数据
- **THEN** 页面展示 4 个统计卡片，分别显示题目总数、已作答、未作答、总分

### Requirement: 考题列表分页展示
系统 SHALL 以列表形式展示考题，每行显示序号、题目名称、题型（颜色徽章）、分值、答题状态。列表 SHALL 支持分页浏览。

#### Scenario: 展示考题列表
- **WHEN** 考核时间下有考题数据
- **THEN** 列表按 questionNo 升序显示考题行，题型使用不同颜色区分（单选题蓝色、多选题紫色、文件上传橙色、算法题青色）

#### Scenario: 答题状态显示
- **WHEN** 考题有已答/未答状态数据
- **THEN** 已答题目显示绿色勾选状态，未答题目显示灰色待答状态

#### Scenario: 分页导航
- **WHEN** 考题总数超过每页大小（默认10条）
- **THEN** 页面底部展示分页控件，用户可点击切换页码

#### Scenario: 无考题数据
- **WHEN** 考核时间下没有考题
- **THEN** 页面显示空状态提示 "暂无考题"

### Requirement: 考题目录页设计风格
考题目录页 SHALL 延续现有考核列表页的暗色毛玻璃设计风格，使用相同的配色方案和视觉效果。

#### Scenario: 设计风格一致
- **WHEN** 用户从考核列表页点击进入考题目录页
- **THEN** 两个页面的视觉风格（背景、卡片样式、配色）保持一致

### Requirement: 题型 Content 类型定义
前端 DTO 中题型 Content 类型 SHALL 与后端 `QuestionContent` 多态 JSON 结构完全一致。

所有 Content 类型继承基类字段 `type`（用于多态标识）和 `content`（题干）。

具体字段对齐：
- `SingleChoiceContent`: `content`（题干）, `options`（string[]）, `correctAnswer`（string）
- `MultipleChoiceContent`: `content`（题干）, `options`（string[]）, `correctAnswers`（string[]）
- `FileUploadContent`: `content`（题干），无额外字段
- `AlgorithmContent`: `content`（题干）, `testCases`（TestCase[]，含 `input` 和 `expectedOutput`）, `timeLimit`（number）, `memoryLimit`（number）

#### Scenario: DTO 字段名称正确
- **WHEN** 前端发送创建/更新考题请求
- **THEN** Content JSON 中题干字段名为 `content`，选项字段名为 `options`，正确答案字段名为 `correctAnswer`（单选）或 `correctAnswers`（多选）

#### Scenario: DTO 字段类型正确
- **WHEN** 前端处理单选题的 `correctAnswer`
- **THEN** 类型为 `string`（选项文本），而非 `number`

#### Scenario: DTO 字段类型正确-多选
- **WHEN** 前端处理多选题的 `correctAnswers`
- **THEN** 类型为 `string[]`（选项文本数组），而非 `number[]`
