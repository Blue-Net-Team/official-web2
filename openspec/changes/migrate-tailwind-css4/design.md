## Context

前端项目（Next.js 15 + React 19）当前使用纯 CSS Modules 方案管理样式，共 43 个 `.module.css` 文件分布在组件和页面中。全局样式通过 `globals.css`（`:root` 变量、基础重置）和 `responsive.css`（响应式变量、工具类）管理。UI 组件库为 Ant Design 6（已将 `tailwindcss@4.2.2` 作为间接依赖安装）。

项目无 PostCSS 配置文件、无 Tailwind 配置文件、未使用任何 CSS 预处理器或 CSS-in-JS 方案。

## Goals / Non-Goals

**Goals:**

- 将 Tailwind CSS 4 集成为项目的直接依赖，配置 PostCSS 集成
- 将全局 CSS 变量（`responsive.css` 中的间距、字体、断点变量）迁移为 Tailwind `@theme` 自定义 token
- 将 `responsive.css` 中的响应式工具类替换为 Tailwind 内置响应式系统
- 逐组件将 CSS Module 样式迁移为 Tailwind utility class，每次迁移一个组件
- 确保迁移过程中项目始终可构建、可运行

**Non-Goals:**

- 不改变组件的功能逻辑或 JSX 结构（仅替换样式实现方式）
- 不替换 Ant Design 组件（仅对自定义样式部分使用 Tailwind）
- 不引入 Tailwind UI 或其他付费组件库
- 不在此次迁移中重构组件架构
- 不修改后端代码

## Decisions

### 1. 使用 Tailwind CSS 4（CSS-first 配置）

**选择**：使用 Tailwind CSS 4 的 CSS-first 配置方式（`@theme` 指令），而非 v3 的 `tailwind.config.js`。

**理由**：
- Tailwind CSS 4 移除了 JavaScript 配置文件，改为 CSS 原生配置
- 与 Ant Design 6 内置的 Tailwind 4 版本一致
- 零配置自动内容检测（无需配置 `content` 路径）
- 基于 Rust 引擎，构建性能更优

**替代方案**：
- Tailwind CSS 3：需要 `tailwind.config.js`，与 antd 6 内置的 v4 不一致，有版本冲突风险
- 内联 CSS-in-JS：与 React 19 Server Components 不兼容

### 2. PostCSS 集成方式

**选择**：使用 `@tailwindcss/postcss` 插件，创建 `postcss.config.mjs`。

**理由**：
- Tailwind CSS 4 官方推荐的 Next.js 集成方式
- Next.js 15 原生支持 PostCSS 配置文件
- 无需修改 `next.config.ts`

### 3. 渐进式迁移策略

**选择**：逐组件迁移，每个组件作为独立任务执行，迁移完成后立即验证。

**理由**：
- 43 个组件一次性迁移风险极高，难以定位问题
- 渐进式迁移允许在迁移过程中随时暂停、回退
- 每个组件迁移后可立即视觉验证，确保样式无偏差
- CSS Modules 和 Tailwind class 可在同一项目中共存，不会冲突

**替代方案**：
- 一次性全量迁移：风险高，难以调试，不符合用户要求
- 按页面迁移：同一页面内的组件间有样式依赖，不够细粒度

### 4. CSS 变量迁移策略

**选择**：将 `responsive.css` 中的 CSS 变量迁移到 `globals.css` 的 `@theme` 块中，作为 Tailwind 自定义 token。

**理由**：
- Tailwind 4 的 `@theme` 指令可定义自定义设计 token
- 迁移后可直接使用 `p-section-x`、`text-hero-title` 等语义化 class
- 断点系统可直接映射为 Tailwind 的 `sm:`/`md:`/`lg:` 前缀

### 5. 组件迁移顺序

**选择**：按依赖关系从叶子组件到根组件迁移。

**迁移批次（从底层到顶层）：**

1. **基础设施层**：安装依赖、配置 PostCSS、改造 globals.css、迁移 CSS 变量
2. **基础 UI 组件**：Footer、PublicNavbar、AdminSideBar、AdminHeadBar
3. **Home 页面子组件**：TopContent → DirectionCard → DirectionIntroduce → ProcessCard → RecruitmentProcess → CompetitionCard → Competitions → FeaturedEquipment → AchievementAndResources → TeamVibe
4. **独立页面组件**：ConsultationQrcode、TechStack、RecruitmentInfo、LearningPath、HeroSection、CareerSection
5. **Profile 系列组件**：AvatarCropModal → ProfileInfo → ProfileSidebar → ProfileTabs → AssessmentList → ExperienceSection
6. **成员系列组件**：MemberCard → Members → MemberProfile
7. **Achievements 系列组件**：AchievementCard → AchievementFilter → AchievementStats
8. **独立卡片组件**：CompetitionCard（独立）
9. **页面级样式**：各路由目录下的 CSS Module
10. **清理阶段**：删除 `responsive.css`、清理未使用的 `.module.css`

## Risks / Trade-offs

- **[样式差异风险]** CSS Module 的局部作用域与 Tailwind 全局 class 的行为不同 → 迁移时仔细检查类名冲突，使用 Tailwind 的 `@layer` 管理优先级
- **[Ant Design 样式冲突]** Tailwind 的 preflight 可能重置 Ant Design 组件样式 → 使用 Tailwind 4 的 `@layer` 机制确保 Ant Design 样式优先；必要时禁用 preflight 中的冲突规则
- **[CSS 变量兼容性]** 迁移 `responsive.css` 中的 CSS 变量到 `@theme` 可能影响未迁移组件 → 在迁移初期保留原 CSS 变量定义，直到所有组件完成迁移后再清理
- **[构建体积]** Tailwind CSS 会增加 CSS 产物体积 → Tailwind 4 的 JIT 引擎只生成使用到的 class，实际增量可控
- **[学习曲线]** 团队成员需熟悉 Tailwind class 命名 → 提供常用样式映射参考表
