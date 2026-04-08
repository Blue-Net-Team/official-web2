## ADDED Requirements

### Requirement: 用户可以查询考核时间列表
系统应当允许已登录用户查询其可见的考核时间列表，包括考核的基本信息（方向、届次、年级、时间范围等）和答题进度。

#### Scenario: 成功获取考核时间列表
- **WHEN** 用户访问个人主页的"我的考核"标签页
- **THEN** 系统调用 `GET /api/v1/assessment-times` 接口，返回用户可见的考核时间列表

#### Scenario: 考核时间包含进度信息
- **WHEN** 考核时间列表返回时
- **THEN** 每个考核时间对象包含 `totalQuestions` 和 `completedQuestions` 字段，用于展示答题进度

### Requirement: 用户可以查询限时考核的会话信息
系统应当允许已登录用户获取限时考核的会话信息，包括截止时间。

#### Scenario: 获取限时考核截止时间
- **WHEN** 考核时间为限时考核（`timeLimit` 为 true）且用户首次访问时
- **THEN** 系统调用 `GET /api/v1/assessment-sessions/{assessmentTimeId}` 接口，返回会话的 `deadline` 字段

#### Scenario: 非限时考核不需要会话信息
- **WHEN** 考核时间为非限时考核（`timeLimit` 为 false）时
- **THEN** 不调用会话接口，`deadline` 为 null

### Requirement: 考核数据从后端 API 获取
系统应当从后端 API 获取考核数据，不再使用 mock 数据。

#### Scenario: 移除 mock 数据依赖
- **WHEN** 加载考核数据时
- **THEN** 不调用 `MockProfileService.getAssessments()`，而是调用后端 API

#### Scenario: 数据格式转换
- **WHEN** 后端返回 `AssessmentTimeDTO` 数据时
- **THEN** 前端将数据转换为 `Assessment` 类型，包含 `id`、`title`、`round`、`status`、`startDate`、`endDate`、`totalQuestions`、`completedQuestions` 等字段
