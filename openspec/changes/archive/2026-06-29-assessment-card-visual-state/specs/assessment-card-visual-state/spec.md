## ADDED Requirements

### Requirement: 卡片视觉状态由单一优先级派生

`AssessmentCard` SHALL 从考核数据派生唯一的视觉状态 `VisualState`，取值为 `eliminated`、`inProgress`、`ended`、`notStarted` 之一。派生 SHALL 遵循固定优先级：`eliminated` 最高，其次按时间状态映射 `IN_PROGRESS → inProgress`、`ENDED → ended`，否则为 `notStarted`。

时间状态 SHALL 由 `startTime`/`endTime` 与当前时间比较得出（`now < start → NOT_STARTED`、`now > end → ENDED`、其余 `IN_PROGRESS`）。`eliminated` SHALL 取 `assessment.eliminated` 的布尔真值。

#### Scenario: 已淘汰优先于时间状态
- **WHEN** 考核 `eliminated = true` 且当前时间处于 `startTime` 与 `endTime` 之间
- **THEN** 视觉状态 SHALL 为 `eliminated`（不得为 `inProgress`）

#### Scenario: 进行中
- **WHEN** 考核 `eliminated` 为假且当前时间处于 `startTime` 与 `endTime` 之间
- **THEN** 视觉状态 SHALL 为 `inProgress`

#### Scenario: 已结束
- **WHEN** 考核 `eliminated` 为假且当前时间晚于 `endTime`
- **THEN** 视觉状态 SHALL 为 `ended`

#### Scenario: 未开始
- **WHEN** 考核 `eliminated` 为假且当前时间早于 `startTime`
- **THEN** 视觉状态 SHALL 为 `notStarted`

### Requirement: 四态视觉表现集中且一致

组件的边框、图标底色、图标元素、状态徽章底色、状态徽章文案、操作按钮样式、操作按钮文案、答题进度条颜色、顶部高光线 SHALL 全部由当前 `VisualState` 唯一决定，且各视觉位之间 SHALL 保持同一状态语义一致（不得出现某一视觉位使用与其它位不同的状态判断口径）。

各状态 SHALL 呈现下述既定表现（取值与重构前逐一等价）：

- `eliminated`：边框灰且半透明、图标底灰、图标为 `InboxOutlined`、徽章红底（`#ff4d4f`）文案「已被淘汰」、按钮灰且禁用样式文案「已被淘汰」、进度条灰、无顶部高光线
- `inProgress`：边框与图标底蓝紫（`#6677ff`）、图标为 `FieldTimeOutlined`、徽章蓝紫文案「进行中」、按钮蓝紫渐变文案「继续答题」、进度条蓝紫渐变、顶部蓝紫高光线
- `ended`：边框与图标底绿（`#07c160`）、图标为 `DesktopOutlined`、徽章绿文案「已结束」、按钮绿渐变文案「查看详情」、进度条绿渐变、顶部绿高光线
- `notStarted`：边框与图标底灰、图标为 `InboxOutlined`、徽章灰文案「未开始」、按钮灰且禁用样式文案「暂不可进入」、进度条灰、无顶部高光线

#### Scenario: 淘汰态图标与进度条显式归位
- **WHEN** 视觉状态为 `eliminated`
- **THEN** 图标元素 SHALL 为 `InboxOutlined`，进度条（若渲染）SHALL 为灰色——该取值 SHALL 被显式声明，而非依赖条件 fallthrough

#### Scenario: 进行中态整组视觉一致
- **WHEN** 视觉状态为 `inProgress`
- **THEN** 边框、图标底、徽章、按钮、进度条 SHALL 全部呈现蓝紫主题，徽章文案为「进行中」，按钮文案为「继续答题」，并显示顶部蓝紫高光线

#### Scenario: 已结束态整组视觉一致
- **WHEN** 视觉状态为 `ended`
- **THEN** 边框、图标底、徽章、按钮、进度条 SHALL 全部呈现绿色主题，图标为 `DesktopOutlined`，徽章文案为「已结束」，按钮文案为「查看详情」

#### Scenario: 进度条仅在有题目时渲染
- **WHEN** 考核 `totalQuestions` 为 0 或空
- **THEN** 组件 SHALL NOT 渲染答题进度条

### Requirement: 可点击行为由视觉状态统一决定

操作按钮的可点击性、点击跳转拦截、右箭头图标显隐 SHALL 全部由当前 `VisualState` 的可点击性统一决定。`inProgress` 与 `ended` SHALL 可点击；`eliminated` 与 `notStarted` SHALL NOT 可点击。

#### Scenario: 可点击状态跳转答题页
- **WHEN** 视觉状态为 `inProgress` 或 `ended`，用户点击操作按钮
- **THEN** 系统 SHALL 跳转至 `/assessment/{id}/questions`，且按钮 SHALL 显示右箭头图标

#### Scenario: 不可点击状态拦截跳转
- **WHEN** 视觉状态为 `eliminated` 或 `notStarted`，用户点击操作按钮
- **THEN** 系统 SHALL NOT 发生跳转，按钮 SHALL 呈现 `not-allowed` 光标且 SHALL NOT 显示右箭头图标

### Requirement: 重构不改变组件对外契约

本能力以纯前端组件内部重构方式实现，组件 props SHALL 保持为 `assessment: AssessmentTimeDTO` 不变，对外渲染结果 SHALL 与重构前逐像素等价，调用方 SHALL NOT 需要任何修改。

#### Scenario: 调用方无需改动
- **WHEN** 重构完成后
- **THEN** `assessment` 页面与 Profile 的 `AssessmentList` 两处调用 SHALL 无需修改即可正常渲染
