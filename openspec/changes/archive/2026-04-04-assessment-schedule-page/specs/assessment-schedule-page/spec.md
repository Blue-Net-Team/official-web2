## ADDED Requirements

### Requirement: 考核时间列表页面展示

系统 SHALL 提供考核时间列表页面（`/assessment`），展示当前用户可参加的所有考核时间安排。页面 SHALL 为客户端组件，需要用户已登录。

#### Scenario: 已登录考生访问考核中心
- **WHEN** 已登录考生通过导航栏点击"考核中心"
- **THEN** 系统展示 `/assessment` 页面，显示该考生方向和年级对应的所有考核时间卡片

#### Scenario: 未登录用户访问考核中心
- **WHEN** 未登录用户直接访问 `/assessment` 路径
- **THEN** 系统重定向到 `/login` 登录页面

#### Scenario: 考核时间列表为空
- **WHEN** 考生没有可见的考核时间
- **THEN** 页面展示空状态提示"暂无考核安排"

### Requirement: 考核时间卡片信息展示

每张考核时间卡片 SHALL 展示以下信息：
- 考核轮次（如"第一轮考核"）
- 考核方向名称（如"计算机视觉方向"）
- 考核状态徽章（未开始/进行中/已结束）
- 考核时间范围（startTime — endTime）
- 限时信息（限时 N 分钟 或 不限时）
- 答题进度（已完成数/总题数）及进度条

#### Scenario: 进行中的考核卡片展示
- **WHEN** 当前时间在考核的 startTime 和 endTime 之间
- **THEN** 卡片显示蓝色边框和蓝色状态徽章"进行中"，进度条使用蓝色渐变填充

#### Scenario: 未开始的考核卡片展示
- **WHEN** 当前时间早于考核的 startTime
- **THEN** 卡片显示灰色边框和灰色状态徽章"未开始"，进度条为空

#### Scenario: 已结束的考核卡片展示
- **WHEN** 当前时间晚于考核的 endTime
- **THEN** 卡片显示绿色边框和绿色状态徽章"已结束"，进度条满格绿色填充

#### Scenario: 限时考核展示
- **WHEN** 考核设置了 timeLimit = true 且 timeLimitMinutes = 120
- **THEN** 卡片元信息区域显示"限时 120 分钟"，使用橙色高亮

#### Scenario: 不限时考核展示
- **WHEN** 考核设置了 timeLimit = false
- **THEN** 卡片元信息区域显示"不限时"，使用灰色文字

### Requirement: 考核时间卡片操作按钮

每张考核时间卡片 SHALL 根据状态展示对应操作按钮。按钮样式 SHALL 保持一致（填充渐变色 + 白色文字）。

#### Scenario: 进行中考核的操作按钮
- **WHEN** 考核状态为"进行中"
- **THEN** 显示蓝色渐变按钮"继续答题"（当前版本按钮不可用，等考核详情页实现后跳转）

#### Scenario: 未开始考核的操作按钮
- **WHEN** 考核状态为"未开始"
- **THEN** 显示灰色禁用按钮"暂不可进入"

#### Scenario: 已结束考核的操作按钮
- **WHEN** 考核状态为"已结束"
- **THEN** 显示绿色渐变按钮"查看详情"（当前版本按钮不可用，等考核详情页实现后跳转）

### Requirement: 页面头部信息展示

页面 SHALL 展示标题"考核时间安排"、副标题"查看当前可参加的考核时间安排"，以及当前用户的考核方向和年级信息（如"计算机视觉 · 大一"）。

#### Scenario: 页面头部展示
- **WHEN** 考生进入考核时间页面
- **THEN** 页面顶部显示标题、描述和方向年级标签

### Requirement: 考核进度查询接口

系统 SHALL 提供 `GET /api/v1/assessment-times/{id}/progress` 接口，返回指定考核时间的进度信息。该接口需要用户已登录（AUTHENTICATED）。

#### Scenario: 查询存在的考核时间进度
- **WHEN** 已登录用户请求 `/api/v1/assessment-times/1/progress`
- **THEN** 返回 `{ assessmentTimeId: 1, totalQuestions: 8, completedQuestions: 5 }`

#### Scenario: 查询不存在的考核时间进度
- **WHEN** 已登录用户请求 `/api/v1/assessment-times/999/progress`
- **THEN** 返回 404 错误

### Requirement: 页面暗色主题样式

页面 SHALL 使用暗色主题，与项目现有设计系统一致：背景 #000000、卡片 rgba(255,255,255,0.03)、边框 rgba(255,255,255,0.06)、玻璃拟态效果（backdrop-filter: blur(20px)）。

#### Scenario: 暗色主题一致性
- **WHEN** 页面渲染
- **THEN** 卡片使用玻璃拟态效果，16px 圆角，半透明边框，hover 时有上浮和发光效果
