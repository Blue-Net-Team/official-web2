# Tasks: unify-smoked-glass-tabs

## 1. 迁移准备

- [ ] 1.1 全局搜索 `ProfileTabs` 引用点，确认仅 profile 页面使用；搜索其余 `SmokedGlassTabs` 引用确认无遗漏页面
- [ ] 1.2 确认 3000 端口状态：已占用则复用用户 dev server，未占用则自行 `pnpm dev`

## 2. 团队成员页迁移

- [ ] 2.1 修改 `src/frontend/src/components/Members/Members.tsx`：将自研筛选条替换为 `SmokedGlassTabs` 受控写法（`activeKey={activeFilter}`，`onChange` 内执行 `setActiveFilter` + `setCurrentPage(0)`），`filterTabs` map 为 antd `items`（label 含方向名 + 数量）
- [ ] 2.2 Playwright 验证：筛选标签渲染为烟色玻璃胶囊、计数正确、切换方向后列表过滤且分页重置、滑动指示器平移正常

## 3. Profile 页迁移

- [ ] 3.1 修改 `src/frontend/src/app/(public)/(other)/profile/page.tsx`（或其客户端容器组件）：将 `ProfileTabs` 替换为 `SmokedGlassTabs`，tabs 配置 map 为 antd `items`（label 内联图标 + Badge 计数），保持 `?tab=` URL 同步逻辑不变
- [ ] 3.2 删除 `src/frontend/src/components/Profile/ProfileTabs/` 目录
- [ ] 3.3 Playwright 验证：五个 Tab 切换、URL 更新、计数徽标显示、激活文字白色高亮、指示器平移

## 4. 资源库页迁移

- [ ] 4.1 在 `src/frontend/src/app/(public)/(other)/resources/page.tsx` 中新增 client 子组件 `DirectionTabs`：包装 `SmokedGlassTabs`，`activeKey` 由 searchParams 的 tab 派生，`onChange` 调 `router.push(buildHref(key, 0))`
- [ ] 4.2 用 `DirectionTabs` 替换原 `<nav>` 自研 Tab 渲染，删除遗留样式代码，`TABS`/`buildHref` 定义保留并复用
- [ ] 4.3 Playwright 验证：`/resources?tab=embedded` 直达渲染正确、点击切换触发 SSR 重新渲染、资源列表过滤正确

## 5. 清理与回归

- [ ] 5.1 删除演示页 `src/frontend/src/app/(public)/(other)/tabs-demo/`
- [ ] 5.2 运行 `pnpm build`（若 3000 被占用导致无法 build，改用 `pnpm tsc --noEmit` 或等待用户停止 dev server 后补验）确认编译通过
- [ ] 5.3 Playwright 全链路回归三个页面，确认无原自研 Tab 样式残留
