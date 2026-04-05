## ADDED Requirements

### Requirement: 逐组件迁移规范

每个组件的 CSS Module 迁移 SHALL 作为独立任务执行，遵循固定的迁移步骤，确保每次只迁移一个组件。

#### Scenario: 单组件迁移流程

- **WHEN** 开始迁移一个组件的 CSS Module
- **THEN** SHALL 执行以下步骤：1) 阅读当前 `.module.css` 文件内容；2) 将每个 CSS 规则映射为对应的 Tailwind utility class；3) 替换组件 TSX 中的 `styles.xxx` 引用为 Tailwind class；4) 删除 `.module.css` 文件；5) 验证组件视觉效果无变化
- **THEN** 每个组件迁移完成后 SHALL 确保应用可正常构建（`pnpm build` 无错误）

### Requirement: CSS 规则到 Tailwind class 映射

系统 SHALL 定义常见的 CSS Module 规则到 Tailwind utility class 的映射规范。

#### Scenario: 布局样式映射

- **WHEN** CSS Module 中包含 `display: flex`、`flex-direction: column`、`gap: 20px`、`align-items: center` 等布局规则
- **THEN** SHALL 映射为 `flex flex-col gap-5 items-center` 等 Tailwind class

#### Scenario: 间距样式映射

- **WHEN** CSS Module 中包含 `padding`、`margin` 等使用 CSS 变量的间距规则
- **THEN** SHALL 映射为使用自定义 token 的 Tailwind class（如 `p-section-x`、`mt-content-gap`）

#### Scenario: 响应式样式映射

- **WHEN** CSS Module 中包含 `@media` 媒体查询规则
- **THEN** SHALL 映射为 Tailwind 的响应式前缀（如 `md:`、`lg:`）

#### Scenario: 自定义复杂样式映射

- **WHEN** CSS Module 中包含无法直接用 Tailwind utility 表达的复杂样式（如动画、特殊选择器）
- **THEN** SHALL 使用 Tailwind 的任意值语法（如 `w-[200px]`）或在组件中保留局部 `<style>` 标签

### Requirement: 迁移顺序约束

组件迁移 SHALL 按依赖关系从叶子组件到根组件的顺序执行。

#### Scenario: 先迁移子组件再迁移父组件

- **WHEN** 组件 A 被组件 B 引用
- **THEN** 组件 A SHALL 在组件 B 之前完成迁移

### Requirement: 迁移批次划分

组件迁移 SHALL 按以下批次执行，每个批次包含一组可并行迁移的组件。

#### Scenario: 批次 1 - 基础设施搭建

- **WHEN** 开始迁移
- **THEN** SHALL 先完成：安装依赖、创建 `postcss.config.mjs`、改造 `globals.css`、迁移 CSS 变量到 `@theme`

#### Scenario: 批次 2 - 基础 UI 组件

- **WHEN** 基础设施搭建完成
- **THEN** SHALL 迁移：Footer、PublicNavbar、AdminSideBar、AdminHeadBar

#### Scenario: 批次 3 - Home 页面子组件

- **WHEN** 基础 UI 组件迁移完成
- **THEN** SHALL 按顺序迁移：TopContent → DirectionCard → DirectionIntroduce → ProcessCard → RecruitmentProcess → CompetitionCard → Competitions → FeaturedEquipment → AchievementAndResources → TeamVibe

#### Scenario: 批次 4 - 独立页面组件

- **WHEN** Home 子组件迁移完成
- **THEN** SHALL 迁移：ConsultationQrcode、TechStack、RecruitmentInfo、LearningPath、HeroSection、CareerSection

#### Scenario: 批次 5 - Profile 系列组件

- **WHEN** 独立页面组件迁移完成
- **THEN** SHALL 迁移：AvatarCropModal → ProfileInfo → ProfileSidebar → ProfileTabs → AssessmentList → ExperienceSection

#### Scenario: 批次 6 - 成员系列组件

- **WHEN** Profile 组件迁移完成
- **THEN** SHALL 迁移：MemberCard → Members → MemberProfile

#### Scenario: 批次 7 - Achievements 系列组件

- **WHEN** 成员组件迁移完成
- **THEN** SHALL 迁移：AchievementCard → AchievementFilter → AchievementStats

#### Scenario: 批次 8 - 独立卡片组件

- **WHEN** Achievements 组件迁移完成
- **THEN** SHALL 迁移：CompetitionCard（components/CompetitionCard/）

#### Scenario: 批次 9 - 页面级样式

- **WHEN** 所有组件级 CSS Module 迁移完成
- **THEN** SHALL 迁移页面路由目录下的 CSS Module 文件（共 10 个页面级样式文件）

#### Scenario: 批次 10 - 清理阶段

- **WHEN** 所有组件和页面级样式迁移完成
- **THEN** SHALL 删除 `responsive.css` 文件（其中的工具类已被 Tailwind 替代）
- **THEN** SHALL 删除 `public/index.css` 文件（如果不再需要）
- **THEN** SHALL 清理所有已删除 `.module.css` 文件对应的 import 语句
- **THEN** SHALL 执行 `pnpm build` 确认构建成功

### Requirement: 视觉一致性验证

每个组件迁移完成后，SHALL 确保其视觉效果与迁移前保持一致。

#### Scenario: 单组件迁移验证

- **WHEN** 一个组件的 CSS Module 迁移完成
- **THEN** 组件的布局、间距、字体、颜色 SHALL 与迁移前视觉一致
- **THEN** 响应式行为（移动端/平板/桌面）SHALL 与迁移前一致
