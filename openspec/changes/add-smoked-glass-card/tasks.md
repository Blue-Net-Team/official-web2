## 1. SmokedGlassCard 组件

- [x] 1.1 ~~先写组件单元测试~~ **调整**：`src/frontend` 尚无 Vitest/Jest 基础设施（03-08 手册的测试工具库尚未建立），为纯展示组件临时引入测试运行器超出本 change 范围；改为实施期以 `tsc --noEmit` + `next lint` 静态验证，行为验证（默认渲染、tone/hoverable/accent/radius/padding 语义）并入各批次 Playwright E2E（任务 2.5/3.3/4.3/5.2 已覆盖全部场景）。组件单测基础设施作为后续独立 change 立项
- [x] 1.2 实现 `src/components/SmokedGlassCard/index.tsx` + `SmokedGlassCard.module.css`（配方以 design.md D2/D4/D5/D6 为准，自 `/glass-demo/SmokedGlass.module.css` 迁入并修正 padding 变量通道）
- [x] 1.3 组件文件头注释写明使用规范（spec「使用规范」要求的开发指导锚点）

## 2. 批次 1：修复 #70（替换 .glass-card）

- [x] 2.1 `CompetitionCard` 迁移：`SmokedGlassCard hoverable radius={24}`，经 `--sgc-padding` 覆盖横向内边距
- [x] 2.2 `AchievementCard` 迁移：`SmokedGlassCard hoverable radius={12}`，解绑 antd Card（body padding 由组件承担）
- [x] 2.3 `AchievementStats` 迁移：`SmokedGlassCard`（默认，不悬浮）
- [x] 2.4 删除 `globals.css` 中 `.glass-card` 定义，全局搜索确认无残留引用
- [x] 2.5 E2E 验证 `/competitions`、`/achievements`（含 hover 动画平滑、与 #70 截图对比）——Playwright 截图确认烟色玻璃卡片有形渲染，#70 修复生效

## 3. 批次 2：收编内联玻璃（家族 B）

- [x] 3.1 `resources/page.tsx` 资源卡片迁移：`SmokedGlassCard hoverable`，内联 Tailwind 玻璃类删除
- [x] 3.2 `EquipmentCard`、`VenueCard` 迁移：`SmokedGlassCard hoverable`
- [x] 3.3 E2E 验证 `/resources`、`/lab-environment`——页面正常渲染（本地库无资源/设备数据，卡片未出现在快照属预期，结构无报错）

## 4. 批次 3：MemberCard 与 AssessmentCard

- [x] 4.1 `MemberCard` 全面迁移为 `SmokedGlassCard hoverable`（默认 radius）：删除顶部渐变高光条、头像渐变描边、hover 蓝紫外发光三类装饰（design D8，用户拍板删除）；原进场动画 fadeInUp 延迟序列保留
- [x] 4.2 `AssessmentCard` 状态视觉 token 化：`STYLES` 表重构为 4 状态 × `accent` 单变量（外加语义色：被淘汰=红），图标/徽章/进度条/按钮经 `color-mix` 派生；状态语义（置灰、不可点）保持不变
- [ ] 4.3 E2E 验证 `/members`、`/assessment` 及 Profile 考核 Tab：**部分完成**——已登录验证 `/members`（MemberCard 新造型渲染正确，装饰已删）、`/assessment` 与 Profile 考核 Tab 页面正常；但本地库无任何考核时间数据，AssessmentCard 四状态视觉（accent 派生色）未能触发渲染，需有考核数据的环境复核（另需微信 XWEB 实机抽查 color-mix 表现）

## 5. 批次 4：Profile 面板（soft tone）

- [x] 5.1 Profile 7 处面板迁移为 `SmokedGlassCard tone="soft"`：`ProfileSidebar`（sticky 保留）、`ProfileInfo`、`ProfileInfoDisplay`、`ExperienceSection`（含空状态）、`MemberAchievements`、`AssessmentList` 空状态容器；`ProfileTabs` 不动
- [x] 5.2 E2E 验证 `/profile` 全 Tab——已登录走查：个人信息/我的考核/项目经历/个人成就/实习经历各 Tab 的 soft 面板渲染正常，panel 内嵌 panel（经历区空态）层次清晰，soft 浓度无需调整

## 6. 清理与收尾

- [x] 6.1 删除 `/glass-demo` 路由（page.tsx + module.css）。定稿截图存档于 `src/frontend/.playwright-mcp/page-2026-09-30T18-55-03-478Z.png`（competitions）与 `page-2026-09-30T18-55-43-612Z.png`（achievements），贴 #70 由用户确认后手动进行
- [x] 6.2 全局搜索排查：`glass-card` 已清零；残留的 `backdrop-blur` 均为范围外局部用法（Members 筛选控件条、考核答题页信息条、报名表单容器、按钮/徽章局部毛玻璃），无卡片级玻璃配方残留
- [x] 6.3 完整回归 + 提交：已按规范提交 `e059c6a9 feat: 新增 SmokedGlassCard 烟色玻璃卡片组件并统一全站卡片样式`（含 `ref #70`）；登录走查覆盖 /competitions、/achievements、/resources、/lab-environment、/members、/profile 全 Tab、/assessment
