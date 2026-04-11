## ADDED Requirements

### Requirement: 移动端响应式导航布局
系统 SHALL 根据屏幕宽度自动切换导航组件的显示模式。当屏幕宽度 ≤ 767px 时，使用移动端模式；当屏幕宽度 > 767px 时，使用桌面端模式。

#### Scenario: 桌面端显示 Sider
- **WHEN** 屏幕宽度 > 767px
- **THEN** 系统显示左侧 Sider 组件，支持折叠/展开

#### Scenario: 移动端显示 Drawer
- **WHEN** 屏幕宽度 ≤ 767px
- **THEN** 系统隐藏 Sider，显示悬浮菜单按钮，点击后从右侧弹出 Drawer

#### Scenario: 响应窗口大小变化
- **WHEN** 用户调整浏览器窗口大小跨越 767px 断点
- **THEN** 系统自动切换导航显示模式

### Requirement: 移动端菜单按钮位置
移动端菜单按钮 SHALL 位于 Header 右侧，使用透明背景，仅显示汉堡图标。

#### Scenario: 菜单按钮显示在 Header 右侧
- **WHEN** 屏幕宽度 ≤ 767px
- **THEN** Header 右侧显示透明背景的汉堡菜单图标按钮

#### Scenario: 点击菜单按钮打开 Drawer
- **WHEN** 用户点击移动端菜单按钮
- **THEN** 系统从右侧弹出 Drawer 侧边栏

### Requirement: 移动端 Drawer 行为
移动端侧边栏 SHALL 使用 Ant Design Drawer 组件，从右侧弹出，宽度 200px，不挤压右侧内容区域。

#### Scenario: Drawer 从右侧弹出
- **WHEN** 用户打开移动端侧边栏
- **THEN** Drawer 从屏幕右侧滑入，覆盖在内容区域之上

#### Scenario: Drawer 不挤压内容
- **WHEN** Drawer 打开时
- **THEN** 右侧内容区域保持原有宽度，不被挤压

#### Scenario: 点击遮罩关闭 Drawer
- **WHEN** 用户点击 Drawer 外部的遮罩区域
- **THEN** Drawer 关闭

#### Scenario: 点击菜单项关闭 Drawer
- **WHEN** 用户在 Drawer 中点击菜单项导航
- **THEN** Drawer 自动关闭

### Requirement: 组件目录结构
导航相关组件 SHALL 统一放置在 `AdminNav/` 目录下，包含容器组件、Header 组件和侧边栏组件。

#### Scenario: AdminNav 目录结构
- **WHEN** 开发者查看组件目录
- **THEN** 存在 `AdminNav/index.tsx`（容器组件）、`AdminNav/AdminHeadBar.tsx`（Header）、`AdminNav/AdminSideBar.tsx`（侧边栏）

#### Scenario: 共享状态管理
- **WHEN** 容器组件管理 `isMobile` 和 `drawerVisible` 状态
- **THEN** 子组件通过 props 接收状态和回调函数
