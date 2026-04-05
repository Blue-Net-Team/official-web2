## 1. 基础设施搭建

- [ ] 1.1 安装 Tailwind CSS 4 依赖：`pnpm add tailwindcss @tailwindcss/postcss`
- [ ] 1.2 创建 `src/frontend/postcss.config.mjs`，配置 `@tailwindcss/postcss` 插件
- [ ] 1.3 在 `globals.css` 顶部添加 `@import "tailwindcss"` 指令
- [ ] 1.4 将 `responsive.css` 中的 CSS 变量迁移到 `globals.css` 的 `@theme` 块（间距 token：section-padding-x/y、container-padding、card-gap、content-gap）
- [ ] 1.5 将 `responsive.css` 中的字体变量迁移到 `@theme`（字号 token：hero-title、hero-subtitle、section-title、section-title-large、card-title、body、small、caption）
- [ ] 1.6 将 `responsive.css` 中的行高和组件尺寸变量迁移到 `@theme`（line-height、button-height、button-padding-x、card-border-radius）
- [ ] 1.7 配置响应式断点 token（sm: 768px、md: 1024px）与现有 responsive.css 断点对齐
- [ ] 1.8 验证：执行 `pnpm build` 确认基础设施搭建成功，Ant Design 组件样式不受影响

## 2. 基础 UI 组件迁移

- [ ] 2.1 迁移 Footer 组件：`components/Footer/style.module.css` → Tailwind class
- [ ] 2.2 迁移 PublicNavbar 组件：`components/PublicNavbar/styles.module.css` → Tailwind class
- [ ] 2.3 迁移 AdminSideBar 组件：`components/Admin/AdminSideBar/styles.module.css` → Tailwind class
- [ ] 2.4 迁移 AdminHeadBar 组件：`components/Admin/AdminHeadBar/styles.module.css` → Tailwind class

## 3. Home 页面子组件迁移

- [ ] 3.1 迁移 TopContent 组件：`components/Home/TopContent/styles.module.css` → Tailwind class
- [ ] 3.2 迁移 DirectionCard 组件：`components/Home/DirectionIntroduce/DirectionCard/styles.module.css` → Tailwind class
- [ ] 3.3 迁移 DirectionIntroduce 组件：`components/Home/DirectionIntroduce/styles.module.css` → Tailwind class
- [ ] 3.4 迁移 ProcessCard 组件：`components/Home/RecruitmentProcess/ProcessCard/styles.module.css` → Tailwind class
- [ ] 3.5 迁移 RecruitmentProcess 组件：`components/Home/RecruitmentProcess/styles.module.css` → Tailwind class
- [ ] 3.6 迁移 CompetitionCard 组件：`components/Home/Competitions/CompetitionCard/styles.module.css` → Tailwind class
- [ ] 3.7 迁移 Competitions 组件：`components/Home/Competitions/styles.module.css` → Tailwind class
- [ ] 3.8 迁移 FeaturedEquipment 组件：`components/Home/FeaturedEquipment/styles.module.css` → Tailwind class
- [ ] 3.9 迁移 AchievementAndResources 组件：`components/Home/AchievementAndResources/styles.module.css` → Tailwind class
- [ ] 3.10 迁移 TeamVibe 组件：`components/Home/TeamVibe/styles.module.css` → Tailwind class

## 4. 独立页面组件迁移

- [ ] 4.1 迁移 ConsultationQrcode 组件：`components/Enroll/ConsultationQrcode/styles.module.css` → Tailwind class
- [ ] 4.2 迁移 TechStack 组件：`components/Direction/TechStack/styles.module.css` → Tailwind class
- [ ] 4.3 迁移 RecruitmentInfo 组件：`components/Direction/RecruitmentInfo/styles.module.css` → Tailwind class
- [ ] 4.4 迁移 LearningPath 组件：`components/Direction/LearningPath/styles.module.css` → Tailwind class
- [ ] 4.5 迁移 HeroSection 组件：`components/Direction/HeroSection/styles.module.css` → Tailwind class
- [ ] 4.6 迁移 CareerSection 组件：`components/Direction/CareerSection/styles.module.css` → Tailwind class

## 5. Profile 系列组件迁移

- [ ] 5.1 迁移 AvatarCropModal 组件：`components/Profile/AvatarCropModal/styles.module.css` → Tailwind class
- [ ] 5.2 迁移 ProfileInfo 组件：`components/Profile/ProfileInfo/styles.module.css` → Tailwind class
- [ ] 5.3 迁移 ProfileSidebar 组件：`components/Profile/ProfileSidebar/styles.module.css` → Tailwind class
- [ ] 5.4 迁移 ProfileTabs 组件：`components/Profile/ProfileTabs/styles.module.css` → Tailwind class
- [ ] 5.5 迁移 AssessmentList 组件：`components/Profile/AssessmentList/styles.module.css` → Tailwind class
- [ ] 5.6 迁移 ExperienceSection 组件：`components/Profile/ExperienceSection/styles.module.css` → Tailwind class

## 6. 成员系列组件迁移

- [ ] 6.1 迁移 MemberCard 组件：`components/Members/MemberCard/MemberCard.module.css` → Tailwind class
- [ ] 6.2 迁移 Members 组件：`components/Members/Members.module.css` → Tailwind class
- [ ] 6.3 迁移 MemberProfile 组件：`components/MemberProfile/MemberProfile.module.css` → Tailwind class

## 7. Achievements 系列组件迁移

- [ ] 7.1 迁移 AchievementCard 组件：`components/Achievements/AchievementCard/styles.module.css` → Tailwind class
- [ ] 7.2 迁移 AchievementFilter 组件：`components/Achievements/AchievementFilter/styles.module.css` → Tailwind class
- [ ] 7.3 迁移 AchievementStats 组件：`components/Achievements/AchievementStats/styles.module.css` → Tailwind class

## 8. 独立卡片组件迁移

- [ ] 8.1 迁移 CompetitionCard 组件（独立）：`components/CompetitionCard/CompetitionCard.module.css` → Tailwind class

## 9. 页面级样式迁移

- [ ] 9.1 迁移首页页面样式：`app/(public)/(home)/styles.module.css` → Tailwind class
- [ ] 9.2 迁移个人资料页样式：`app/(public)/(other)/profile/styles.module.css` → Tailwind class
- [ ] 9.3 迁移成员列表页样式：`app/(public)/(other)/members/styles.module.css` → Tailwind class
- [ ] 9.4 迁移登录页样式：`app/(public)/(other)/login/styles.module.css` → Tailwind class
- [ ] 9.5 迁移实验室环境页样式：`app/(public)/(other)/lab-environment/page.module.css` → Tailwind class
- [ ] 9.6 迁移报名页样式：`app/(public)/(other)/enroll/styles.module.css` → Tailwind class
- [ ] 9.7 迁移方向详情页样式：`app/(public)/(other)/direction/[slug]/styles.module.css` → Tailwind class
- [ ] 9.8 迁移竞赛页样式：`app/(public)/(other)/competitions/page.module.css` → Tailwind class
- [ ] 9.9 迁移考核页样式：`app/(public)/(other)/assessment/styles.module.css` → Tailwind class
- [ ] 9.10 迁移成果页样式：`app/(public)/(other)/achievements/styles.module.css` → Tailwind class

## 10. 清理阶段

- [ ] 10.1 删除 `src/frontend/src/styles/responsive.css`（工具类已被 Tailwind 替代）
- [ ] 10.2 清理 `globals.css` 中已被 Tailwind 替代的原始样式（darkText、lightText 等工具类）
- [ ] 10.3 检查并清理 `public/index.css` 是否仍被引用，如不需要则删除
- [ ] 10.4 全局搜索确认无残留的 `.module.css` import 语句
- [ ] 10.5 执行 `pnpm build` 确认构建成功，无任何错误或警告
- [ ] 10.6 执行 `pnpm lint` 确认代码质量检查通过
