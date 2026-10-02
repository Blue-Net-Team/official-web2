# Tasks: unify-smoked-glass-tabs

## 1. 迁移准备

- [x] 1.1 全局搜索 `ProfileTabs` 引用点（发现 profile 与 members/[id] 两处），确认其余 `SmokedGlassTabs` 引用无遗漏页面
- [x] 1.2 确认 3000 端口状态：用户 dev server 运行中，复用现有服务验证

## 2. 团队成员页迁移

- [x] 2.1 修改 `src/frontend/src/components/Members/Members.tsx`：自研筛选条替换为 `SmokedGlassTabs` 受控写法，`filterTabs` map 为 antd `items`（label 含方向名 + Badge 计数）
- [x] 2.2 Playwright 验证：胶囊渲染、计数正确、切换过滤 + 分页重置、指示器平移正常（final-members.png）

## 3. Profile 页迁移

- [x] 3.1 `profile/page.tsx` 替换为 `SmokedGlassTabs`（tone="soft"），新增共享工具 `components/Profile/buildTabItems.tsx` 映射 items（图标 + Badge）
- [x] 3.2 删除 `src/frontend/src/components/Profile/ProfileTabs/` 目录，`members/[id]/page.tsx` 同样迁移（共享 buildTabItems）
- [x] 3.3 Playwright 验证（使用测试账号登录）：五 Tab 切换、激活白色高亮、胶囊平移、内容面板切换正常（profile-switched.png / member-detail 截图）

## 4. 资源库页迁移

- [x] 4.1 新增 client 子组件 `resources/DirectionTabs.tsx`：`activeKey` 由 searchParams 派生，`onChange` 调 `router.push`
- [x] 4.2 用 `DirectionTabs` 替换原 `<nav>` 自研 Tab，`TABS`/`buildHref` 复用，遗留样式删除
- [x] 4.3 Playwright 验证：`?tab=embedded` 直达正确、点击切换 URL 更新 + SSR 重新渲染、资源过滤正确（resources-switched.png）

## 5. 清理与回归

- [x] 5.1 删除演示页 `tabs-demo/`，全仓无残留引用
- [x] 5.2 编译验证：`npx tsc --noEmit` 通过（3000 被用户 dev server 占用，按项目规范禁止 `pnpm build`，留待用户停服后补验）
- [x] 5.3 Playwright 全链路回归 members / profile / members/[id] / resources 四个页面，无自研 Tab 样式残留

## 实施中追加（用户反馈驱动）

- [x] 6.1 `SmokedGlassTabs` 新增 `tone?: 'deep' | 'soft'`（对齐 SmokedGlassCard 深浅档位），Profile 两处使用 `tone="soft"`
- [x] 6.2 指示器材质对齐 SmokedGlassCard accent 变体（22% accent 边框 / 10% 光晕 / 14% 内高光，color-mix 派生）
