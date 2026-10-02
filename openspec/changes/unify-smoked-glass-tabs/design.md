# Design: unify-smoked-glass-tabs

## Context

站点公开页面（profile、members、resources）的 Tab/筛选条均为各页面手写的 button + Tailwind，三种互不一致的样式（渐变激活、半透明描边、圆角胶囊）偏离全站烟色玻璃体系。`SmokedGlassTabs` 组件已在 `tabs-demo` 演示页验证通过（antd Tabs API、烟色玻璃胶囊、滑动指示器、局部 ConfigProvider token），本变更将其推广为唯一 Tab 组件。

约束：
- antd v6 的 CSS-in-JS 在运行时注入样式，颜色必须走局部 ConfigProvider token，禁止高特异性选择器军备竞赛
- 指示器用 `z-index: -1` 沉在 nav 背景之上、文字之下（antd `nav > nav-wrap > nav-list` 内部层叠上下文复杂，同级比 z-index 不可靠）
- 资源库页是 SSR + `?tab=` searchParam 驱动，组件必须支持受控 `activeKey` + `onChange` 后由页面 `router.push` 换状态
- 项目 ISR 规范、3000 端口可能已被用户 dev server 占用（验证时直接用现有服务）

## Goals / Non-Goals

**Goals:**
- 三个页面（profile、members、resources）的 Tab 全部迁移到 `SmokedGlassTabs`
- 删除被替换的自研实现（`ProfileTabs`），删除演示页 `tabs-demo`
- 交互行为不变：计数徽标、URL 同步、分页重置、SSR 重新渲染

**Non-Goals:**
- 不改变任何 API 请求、权限、路由、数据流
- 不改动 admin 后台的 Tabs 使用（`Tabs` 在 admin 属后台表格场景，视觉体系不同，不在本次范围）
- 不改 `SmokedGlassCard` 本体

## Decisions

### 1. ProfileTabs 删除而非改造
原 `ProfileTabs` 是"纯导航条"组件（button 列表 + `onTabChange` 通知父组件），而 `SmokedGlassTabs` 直接吃 antd `items`。决定：删除 `ProfileTabs/index.tsx`，由 `profile/page.tsx` 把 tabs 配置 map 成 antd `items`（label 内联图标 + Badge），状态仍由页面受控。
- 备选：保留 `ProfileTabs` 内部改用 `SmokedGlassTabs` 渲染 → 多一层无意义包装，且 props 模型（`TabConfig[]` + `countKey`）与 antd `items` 重复。

### 2. Members 筛选条改为受控客户端组件
`Members.tsx` 已是客户端组件，`usePagination` 依赖 `activeFilter`。改为 `SmokedGlassTabs` 受控写法（`activeKey={activeFilter} onChange={handleFilterChange}`），`handleFilterChange` 内做 `setActiveFilter + setCurrentPage(0)`，逻辑零变化，只换渲染。
- 备选：拆分独立筛选组件 → 当前筛选条与列表强耦合（分页重置），无拆分收益。

### 3. Resources 页保持 SSR，onChange 中 router.push
`resources/page.tsx` 是 Server Component，Tab 状态在 URL（`?tab=`）。`SmokedGlassTabs` 需要客户端交互，用一个小的 client 子组件（`DirectionTabs`）包裹：`activeKey` 来自 searchParams，`onChange` 调 `router.push(buildHref(key, 0))` 触发服务端重新渲染。不改数据获取逻辑。
- 备选：整体转客户端组件 + 客户端拉数据 → 违背 ISR 规范，弃。

### 4. 演示页在验收后删除
`tabs-demo` 是开发期选型工具，三个页面迁移完成并 E2E 验收后删除，避免公开路由残留。

## Risks / Trade-offs

- [滑动指示器几何在徽标计数异步到达时错位] → `ResizeObserver` 已覆盖容器尺寸变化；计数更新会改变按钮宽度，触发重算
- [resources 页 `router.push` 后 SSR 刷新导致指示器闪回] → 指示器由 `MutationObserver` 监听激活 class 驱动，SSR 重新渲染后激活项正确，胶囊会平移到新位置（可接受，视觉一致）
- [antd 升级后内部 class/层叠结构变化导致样式失效] → 集中单组件，失效点唯一；E2E 验证三个页面可见
- [删除 ProfileTabs 影响其他引用] → 迁移前全局搜索 `ProfileTabs` 引用点，确认仅 profile 页面使用

## Migration Plan

1. 迁移 Members（纯客户端，最简单）→ Playwright 验证筛选 + 分页重置
2. 迁移 Profile → 验证计数徽标、URL 同步、五个 Tab 内容切换
3. 迁移 Resources（SSR + client tabs 子组件）→ 验证 `?tab=` 直达、切换、分页
4. 删除 `ProfileTabs/` 与 `tabs-demo/`
5. `pnpm build` 编译验证 + Playwright 全链路回归

回滚：纯前端视觉改动，git revert 单个 commit 即可，无数据/接口影响。

## Open Questions

- 无（演示页已验证全部视觉与交互决策）
