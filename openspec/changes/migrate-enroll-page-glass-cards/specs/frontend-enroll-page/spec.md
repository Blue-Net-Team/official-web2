## ADDED Requirements

### Requirement: 玻璃卡片材质统一
报名页面的所有卡片区块 SHALL 使用全站统一的 `SmokedGlassCard` 组件渲染，禁止手写旧磨砂玻璃配方（半透明底色 + backdrop-filter、无深色渐变保底）。

#### Scenario: 主表单卡片使用烟色玻璃
- **WHEN** 用户访问报名页面
- **THEN** 主报名表单卡片以 `SmokedGlassCard`（deep 档）渲染
- **AND** 卡片具有深色渐变底色保底，在 backdrop-filter 不生效的环境中仍有清晰形体
- **AND** 卡片不包含顶部三色渐变条与四角括号线装饰

#### Scenario: 侧栏数据容器使用淡烟玻璃
- **WHEN** 页面展示报名表下载卡片或咨询群二维码卡片
- **THEN** 两者以 `SmokedGlassCard tone="soft"` 渲染
- **AND** 容器 hover 时无浮起反馈

#### Scenario: 方向卡片使用主题色玻璃
- **WHEN** 页面展示方向选择卡片
- **THEN** 卡片以 `SmokedGlassCard hoverable accent` 渲染，accent 为对应方向主题色
- **AND** 不再使用横向平移（translate-x）hover 动画

### Requirement: 方向卡片主题色交互状态
方向选择卡片的 hover 与选中状态 SHALL 通过方向主题色（accent）表达，形成「默认 → hover → 选中」逐级加强的视觉层级。

#### Scenario: hover 状态主题色高亮
- **WHEN** 用户悬停未选中的方向卡片
- **THEN** 卡片边框变为方向主题色
- **AND** 卡片产生方向主题色光晕
- **AND** 卡片标题文字变为方向主题色
- **AND** 卡片向上浮起（translateY 动画）

#### Scenario: 选中状态保持主题色区分
- **WHEN** 某一方向被选中
- **THEN** 卡片边框与文字显示方向主题色
- **AND** 计算机视觉为蓝紫色、结构设计为橙红色、嵌入式开发为绿色
