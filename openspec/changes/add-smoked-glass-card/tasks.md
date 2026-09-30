## 1. SmokedGlassCard 组件（TDD）

- [ ] 1.1 先写组件单元测试（渲染快照/属性断言）：默认渲染（deep 配方、radius 16、padding 24）、`tone="soft"`（soft 配方数值）、`hoverable`（仅 deep 生效，soft + hoverable 不渲染交互态）、`accent` 注入 `--accent`、className/style 透传
- [ ] 1.2 实现 `src/components/SmokedGlassCard/index.tsx` + `SmokedGlassCard.module.css`（配方以 design.md D2/D4/D5/D6 为准，自 `/glass-demo/SmokedGlass.module.css` 迁入并修正 padding 变量通道）
- [ ] 1.3 组件文件头注释写明使用规范（spec「使用规范」要求的开发指导锚点）

## 2. 批次 1：修复 #70（替换 .glass-card）

- [ ] 2.1 `CompetitionCard` 迁移：`SmokedGlassCard hoverable radius={24}`，经 `--sgc-padding` 覆盖横向内边距
- [ ] 2.2 `AchievementCard` 迁移：`SmokedGlassCard hoverable radius={12}`，解绑 antd Card（body padding 由组件承担）
- [ ] 2.3 `AchievementStats` 迁移：`SmokedGlassCard`（默认，不悬浮）
- [ ] 2.4 删除 `globals.css` 中 `.glass-card` 定义，全局搜索确认无残留引用
- [ ] 2.5 E2E 验证 `/competitions`、`/achievements`（含 hover 动画平滑、与 #70 截图对比）

## 3. 批次 2：收编内联玻璃（家族 B）

- [ ] 3.1 `resources/page.tsx` 资源卡片迁移：`SmokedGlassCard hoverable`，内联 Tailwind 玻璃类删除
- [ ] 3.2 `EquipmentCard`、`VenueCard` 迁移：`SmokedGlassCard hoverable`
- [ ] 3.3 E2E 验证 `/resources`、`/lab-environment`（底色更实、hover 浮起属可感知变化，截图对比确认）

## 4. 批次 3：MemberCard 与 AssessmentCard

- [ ] 4.1 `MemberCard` 全面迁移为 `SmokedGlassCard hoverable`（默认 radius）：删除顶部渐变高光条、头像渐变描边、hover 蓝紫外发光三类装饰（design D8，用户拍板删除）；原 hover 位移/提亮由组件接管，注意保留进场动画（fadeInUp 延迟序列）
- [ ] 4.2 `AssessmentCard` 状态视觉 token 化：`STYLES` 表 4 状态 × 8 字段的手工颜色替换为 4 状态 × `--accent` 单变量 + `color-mix` 派生；状态语义（置灰、不可点、hover 行为）保持不变
- [ ] 4.3 E2E 验证 `/members`、`/assessment` 及 Profile 考核 Tab：4 状态视觉逐一核对

## 5. 批次 4：Profile 面板（soft tone）

- [ ] 5.1 Profile 6 处面板迁移为 `SmokedGlassCard tone="soft"`：`ProfileSidebar`（sticky 保留）、`ProfileInfo`、`ProfileInfoDisplay`、`ExperienceSection`（含空状态）、`MemberAchievements`、`AssessmentList` 空状态容器；`ProfileTabs` 不动
- [ ] 5.2 E2E 验证 `/profile` 全 Tab，确认 soft 浓度在实页观感（数值可按 design「Open Questions」微调）

## 6. 清理与收尾

- [ ] 6.1 删除 `/glass-demo` 路由（page.tsx + module.css），删除前在 #70 贴定稿截图存档
- [ ] 6.2 全局搜索 `glass-card`、`backdrop-blur` 排查新增残留，确认仅组件 module.css 持有玻璃配方
- [ ] 6.3 完整回归：/competitions → /achievements → /resources → /lab-environment → /members → /assessment → /profile，Playwright 走查后提交（提交消息按规范 `ref #70`，不用 fixes）
