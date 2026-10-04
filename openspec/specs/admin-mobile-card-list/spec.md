## Requirements

### Requirement: 移动端断点判定与双渲染策略
管理平台数据列表页 SHALL 使用 Ant Design `Grid.useBreakpoint()` 判定移动端，约定 `isMobile = !screens.md`。页面 SHALL 在移动端渲染卡片列表，在非移动端渲染原有 Table，两者共享同一数据源、分页与操作回调，桌面端视觉与行为 MUST 零变化。

#### Scenario: 移动端渲染卡片
- **WHEN** 视口宽度小于 antd `md` 断点（<768px）
- **THEN** 页面 SHALL 渲染卡片列表而非 Table
- **AND** 卡片与 Table 使用同一 dataSource 与分页状态

#### Scenario: 桌面端渲染表格
- **WHEN** 视口宽度大于等于 antd `md` 断点
- **THEN** 页面 SHALL 渲染原有 Table，列定义、固定列、滚动行为与改动前一致

#### Scenario: 断点切换时状态保持
- **WHEN** 用户旋转屏幕或缩放窗口跨越 md 断点
- **THEN** 当前页码、筛选条件与选中状态 SHALL 保持不变

### Requirement: 卡片视觉规范
移动端卡片 SHALL 沿用全站烟色玻璃视觉体系：底色 `rgba(255,255,255,0.045)`、描边 `rgba(255,255,255,0.08)`、圆角 12px、主文字 `rgba(255,255,255,0.92)`、次要文字 `rgba(255,255,255,0.45)`、强调色 `#fa8c16`。卡片内信息 SHALL 按"主行（名称+关键标签）→ 详情行 → 操作区（顶部细分隔线）"三层组织。

#### Scenario: 卡片视觉一致性
- **WHEN** 任意管理页移动端卡片渲染
- **THEN** 卡片底色、描边、圆角、文字色阶与全站烟色玻璃组件一致
- **AND** 操作区与其他内容区以 `rgba(255,255,255,0.08)` 细分隔线分隔

### Requirement: 图标使用约束
移动端卡片内所有图标 SHALL 使用 `@ant-design/icons` 组件渲染，禁止直接使用 emoji 字符作为界面图标。

#### Scenario: 卡片图标来源
- **WHEN** 卡片需要搜索、时间、队伍、警告等图标
- **THEN** SHALL 分别使用 `SearchOutlined`、`ClockCircleOutlined`、`TeamOutlined`、`WarningOutlined` 等 antd 图标组件
- **AND** 源码与渲染结果中不得出现作为图标的 emoji 字符
