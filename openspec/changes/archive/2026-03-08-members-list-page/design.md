## Context

基于UI设计稿 `docs/UI/members/index.html`，需要实现团队成员列表页面。页面采用深色主题设计，具有渐变光效背景，展示团队成员信息。

## Goals / Non-Goals

**Goals:**
- 实现响应式成员列表页面
- 支持按方向筛选成员
- 展示成员卡片，包含头像、姓名、方向、职责等信息
- 实现卡片悬停动画效果
- 页面不包含导航栏和Footer

**Non-Goals:**
- 成员详情弹窗/页面（后续迭代）
- 成员编辑功能
- 实时数据更新

## Decisions

### 1. 页面结构
- 使用 Next.js App Router 创建 `/members` 路由
- 页面组件位于 `app/members/page.tsx`
- 提取 MemberCard 组件到 `components/members/MemberCard.tsx`

### 2. 状态管理
- 使用 React useState 管理当前选中的筛选标签
- 使用 useMemo 过滤成员列表

### 3. 样式方案
- 使用 CSS Modules 或 Tailwind CSS（根据项目现有配置）
- 颜色变量：
  - 主背景: #0A0A0A
  - 卡片背景: rgba(255, 255, 255, 0.03)
  - 主色调: #6677FF (蓝紫色)
  - 强调色: #FF6B35 (橙色)
  - 嵌入式: #2ECC71 (绿色)

### 4. 响应式断点
- 桌面端: > 1024px，网格 4 列
- 平板端: 768px - 1024px，网格 3 列
- 移动端: < 768px，网格 1 列

## Risks / Trade-offs

- [Risk] 成员数据量大时性能问题 → 使用虚拟滚动或分页加载
- [Risk] 头像加载失败 → 使用默认头像占位符

## Migration Plan

1. 创建页面组件和样式
2. 集成后端API获取成员数据
3. 测试响应式布局
