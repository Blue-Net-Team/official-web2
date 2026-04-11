## Context

当前管理后台导航系统由两个独立组件构成：
- `AdminHeadBar`：顶部导航栏，显示 Logo 和标题
- `AdminSideBar`：左侧边栏，使用 Ant Design Sider 组件

**现有问题**：
1. 移动端 Sider 展开时占据固定宽度（200px），挤压右侧内容
2. 移动端菜单按钮位于侧边栏折叠区域，不符合常规交互习惯
3. 两个组件分散在不同目录，状态管理不便

**技术约束**：
- 使用 Ant Design 6.x 组件库
- Next.js 15 + React 19
- 保持桌面端现有行为不变

## Goals / Non-Goals

**Goals:**
- 统一导航组件到 `AdminNav` 目录，便于状态共享和维护
- 移动端使用 Drawer 覆盖层模式，不挤压右侧内容
- 菜单按钮移至 Header 右侧，透明背景，符合移动端设计规范
- Drawer 从右侧弹出，与按钮位置一致

**Non-Goals:**
- 不修改菜单项配置和权限过滤逻辑
- 不改变桌面端 Sider 的折叠/展开行为
- 不引入新的状态管理库（继续使用 props 传递）

## Decisions

### 1. 组件目录结构

**决定**：创建 `AdminNav/` 目录，包含三个文件

```
AdminNav/
├── index.tsx          # 容器组件，管理共享状态
├── AdminHeadBar.tsx   # 顶部导航栏（含移动端菜单按钮）
└── AdminSideBar.tsx   # 侧边栏（桌面端 Sider + 移动端 Drawer）
```

**理由**：
- 保持组件职责单一，便于测试和维护
- 容器组件统一管理 `isMobile`、`drawerVisible` 状态
- 避免引入 Context 的复杂性，props 传递足够简单

**替代方案**：
- 方案 A：保持两个独立组件，通过 props 回调共享状态 → 增加父组件复杂度
- 方案 B：使用 Context 共享状态 → 对于简单状态过度设计

### 2. 移动端检测策略

**决定**：使用 `window.matchMedia('(max-width: 767px)')` + resize 监听

**理由**：
- 与 Ant Design 响应式断点一致（sm: 576px, md: 768px）
- 实时响应窗口大小变化

### 3. Drawer 配置

**决定**：
- `placement="right"`：从右侧弹出
- `width={200}`：与桌面端 Sider 宽度一致
- 隐藏 header，保持与 Sider 视觉一致性
- body 背景色 `#001529`（Ant Design 暗色主题）

### 4. 菜单按钮样式

**决定**：
- 透明背景，仅显示图标
- 位于 Header 右侧
- 点击区域 48x48px，便于触摸操作

## Risks / Trade-offs

- **风险**：组件重构可能引入回归问题 → 充分测试桌面端和移动端行为
- **权衡**：Drawer 无法像 Sider 一样折叠为图标 → 移动端用户需要完全打开才能看到菜单，但这是移动端的标准交互模式
