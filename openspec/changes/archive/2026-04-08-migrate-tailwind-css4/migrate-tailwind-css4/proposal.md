## Why

前端项目当前使用纯 CSS Modules 方案管理样式（43 个 `.module.css` 文件），存在以下问题：

1. **响应式开发效率低**：`responsive.css` 中定义了大量响应式 CSS 变量和工具类，需要手动维护断点、间距、字体大小等变量，而 Tailwind CSS 4 内置完整的响应式系统和设计 token，可大幅减少手写媒体查询。
2. **样式代码冗余**：大量重复的布局模式（flex、grid、padding、margin）在每个 CSS Module 中反复书写，Tailwind 的原子化 class 可消除这些重复。
3. **Ant Design 6 已内置 Tailwind CSS 4.2.2**：`antd@6.3.0` 已经将 `tailwindcss@4.2.2` 作为间接依赖安装，迁移成本较低，且可以与 Ant Design 组件风格更好地集成。
4. **Tailwind CSS 4 新特性**：基于 Rust 引擎的零配置 Vite 集成、CSS-first 配置方式、自动内容检测，相比 v3 有显著的性能和 DX 提升。

迁移策略必须是**渐进式的、逐组件迁移**，确保每个阶段项目都可正常运行，避免大规模一次性迁移带来的风险。

## What Changes

- 安装 Tailwind CSS 4 作为直接依赖，配置 PostCSS 集成
- 将 `globals.css` 和 `responsive.css` 中的全局样式迁移为 Tailwind 基础层（`@layer base`）和自定义 CSS 变量
- 将 `responsive.css` 中的响应式工具类替换为 Tailwind 内置的响应式断点系统
- **逐组件迁移** 43 个 CSS Modules 文件，每次迁移一个组件，将 `.module.css` 中的样式转换为 Tailwind utility class
- 清理不再需要的 `.module.css` 文件和 `responsive.css` 工具类
- 配置 `@tailwindcss/postcss` 插件以兼容 Next.js 15

## Capabilities

### New Capabilities

- `tailwind-css4-infrastructure`: Tailwind CSS 4 基础设施搭建 — 安装依赖、PostCSS 配置、全局 CSS 入口改造、CSS 变量迁移、与 Next.js 15 集成
- `tailwind-css4-component-migration`: 逐组件迁移策略和执行规范 — 定义每个组件的迁移顺序、迁移步骤、验证标准

### Modified Capabilities

## Impact

- **前端依赖**：新增 `tailwindcss`、`@tailwindcss/postcss` 为直接依赖；新增 `postcss.config.mjs` 配置文件
- **全局样式文件**：`globals.css` 将引入 `@import "tailwindcss"`，`responsive.css` 中的 CSS 变量将迁移到 Tailwind 的 `@theme` 配置中
- **43 个组件文件**：每个使用 CSS Module 的组件都需要将 `styles.xxx` 引用替换为 Tailwind class，并删除对应的 `.module.css` 文件
- **26 个使用内联样式的文件**：部分内联样式可同步迁移为 Tailwind class
- **构建配置**：Next.js 配置无需修改（Tailwind CSS 4 通过 PostCSS 插件自动集成）
- **无后端影响**：此变更仅涉及前端样式层，不涉及后端 API 或数据结构变化
