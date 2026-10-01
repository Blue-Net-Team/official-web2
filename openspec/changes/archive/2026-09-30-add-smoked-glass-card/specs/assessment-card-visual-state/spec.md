## MODIFIED Requirements

### Requirement: 四态视觉表现集中且一致

组件的边框、图标底色、图标元素、状态徽章底色、状态徽章文案、操作按钮样式、操作按钮文案、答题进度条颜色、顶部高光线 SHALL 全部由当前 `VisualState` 唯一决定，且各视觉位之间 SHALL 保持同一状态语义一致（不得出现某一视觉位使用与其它位不同的状态判断口径）。

各状态 SHALL 呈现下述既定表现：

- `eliminated`：边框灰且半透明、图标底灰、图标为 `InboxOutlined`、徽章红底（`#ff4d4f`）文案「已被淘汰」、按钮灰且禁用样式文案「已被淘汰」、进度条灰、无顶部高光线
- `inProgress`：边框与图标底绿（`#07c160`）、图标为 `FieldTimeOutlined`、徽章绿文案「进行中」、按钮绿渐变文案「继续答题」、进度条绿渐变、顶部绿高光线
- `ended`：边框与图标底蓝紫（`#6677ff`）、图标为 `DesktopOutlined`、徽章蓝紫文案「已结束」、按钮蓝紫渐变文案「查看详情」、进度条蓝紫渐变、顶部蓝紫高光线
- `notStarted`：边框与图标底灰、图标为 `InboxOutlined`、徽章灰文案「未开始」、按钮灰且禁用样式文案「暂不可进入」、进度条灰、无顶部高光线

> 配色变更说明（本 change 引入）：`inProgress` 与 `ended` 的主题色自 2026-09 起对调——进行中=绿（可操作的「进行」语义）、已结束=蓝紫（中性的「归档」语义），取代此前进行中=蓝紫、已结束=绿的映射。

#### Scenario: 淘汰态图标与进度条显式归位

- **WHEN** 视觉状态为 `eliminated`
- **THEN** 图标元素 SHALL 为 `InboxOutlined`，进度条（若渲染）SHALL 为灰色——该取值 SHALL 被显式声明，而非依赖条件 fallthrough

#### Scenario: 进行中态整组视觉一致

- **WHEN** 视觉状态为 `inProgress`
- **THEN** 边框、图标底、徽章、按钮、进度条 SHALL 全部呈现绿色主题，徽章文案为「进行中」，按钮文案为「继续答题」，并显示顶部绿色高光线

#### Scenario: 已结束态整组视觉一致

- **WHEN** 视觉状态为 `ended`
- **THEN** 边框、图标底、徽章、按钮、进度条 SHALL 全部呈现蓝紫主题，图标为 `DesktopOutlined`，徽章文案为「已结束」，按钮文案为「查看详情」

#### Scenario: 进度条仅在有题目时渲染

- **WHEN** 考核 `totalQuestions` 为 0 或空
- **THEN** 组件 SHALL NOT 渲染答题进度条

### Requirement: 重构不改变组件对外契约

本能力以纯前端组件内部重构方式实现，组件 props SHALL 保持为 `assessment: AssessmentTimeDTO` 不变，调用方 SHALL NOT 需要任何修改。

既有「对外渲染结果与重构前逐像素等价」的约束 SHALL 由「四态视觉表现集中且一致」中的新配色映射取代（进行中=绿、已结束=蓝紫）：除该显式配色变更外，组件渲染结果 SHALL 无其它差异。

#### Scenario: 调用方无需改动

- **WHEN** 重构完成后
- **THEN** `assessment` 页面与 Profile 的 `AssessmentList` 两处调用 SHALL 无需修改即可正常渲染
