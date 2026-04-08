## ADDED Requirements

### Requirement: Tailwind CSS 4 依赖安装

系统 SHALL 将 `tailwindcss` 和 `@tailwindcss/postcss` 安装为前端的直接依赖。

#### Scenario: 安装 Tailwind CSS 4 依赖

- **WHEN** 执行 `pnpm add tailwindcss @tailwindcss/postcss`
- **THEN** `package.json` 中 SHALL 包含 `tailwindcss` 和 `@tailwindcss/postcss` 作为依赖
- **THEN** 安装完成后的版本 SHALL 与 Ant Design 6 内置的 Tailwind 版本兼容（4.x）

### Requirement: PostCSS 配置文件

系统 SHALL 在 `src/frontend/` 目录下创建 `postcss.config.mjs` 文件，配置 `@tailwindcss/postcss` 插件。

#### Scenario: PostCSS 配置生效

- **WHEN** Next.js 构建启动
- **THEN** PostCSS SHALL 自动加载 `postcss.config.mjs` 并应用 Tailwind CSS 插件
- **THEN** 构建过程 SHALL 无错误完成

### Requirement: 全局 CSS 入口改造

`globals.css` SHALL 在文件顶部添加 `@import "tailwindcss"` 指令，引入 Tailwind 基础层。

#### Scenario: Tailwind 基础层加载

- **WHEN** 应用启动并加载 `globals.css`
- **THEN** Tailwind 的 base、components、utilities 层 SHALL 被加载
- **THEN** 所有页面 SHALL 可使用 Tailwind utility class

### Requirement: CSS 变量迁移到 Tailwind @theme

系统 SHALL 将 `responsive.css` 中的 CSS 变量（间距、字体大小、行高、组件尺寸）迁移到 `globals.css` 的 `@theme` 块中，作为 Tailwind 自定义设计 token。

#### Scenario: 自定义间距 token 可用

- **WHEN** 开发者在组件中使用 `p-section-x`、`p-section-y`、`p-container` 等自定义间距 class
- **THEN** 系统 SHALL 正确应用对应的 CSS 变量值
- **THEN** 不同断点下的响应式值 SHALL 正确生效

#### Scenario: 自定义字体 token 可用

- **WHEN** 开发者使用 `text-hero-title`、`text-section-title`、`text-body` 等自定义字体大小 class
- **THEN** 系统 SHALL 正确应用对应的字号和行高
- **THEN** 不同断点下的响应式字号 SHALL 正确生效

### Requirement: 响应式断点配置

系统 SHALL 在 `@theme` 中配置与现有 `responsive.css` 一致的断点值：移动端（<768px）、平板（768-1024px）、桌面（>1024px）。

#### Scenario: Tailwind 响应式前缀与现有断点一致

- **WHEN** 开发者使用 `sm:`、`md:`、`lg:` 响应式前缀
- **THEN** `sm:` SHALL 对应移动端（<768px）
- **THEN** `md:` SHALL 对应平板（768-1024px）
- **THEN** `lg:` SHALL 对应桌面（>1024px）

### Requirement: Ant Design 样式兼容性

系统 SHALL 确保 Tailwind CSS 的引入不会破坏 Ant Design 组件的默认样式。

#### Scenario: Ant Design 组件样式不受影响

- **WHEN** 页面中渲染 Ant Design 组件（Button、Form、Table、Modal 等）
- **THEN** Ant Design 组件的默认样式 SHALL 保持不变
- **THEN** Ant Design 组件的功能行为 SHALL 不受影响

### Requirement: CSS Modules 共存兼容

在迁移过程中，系统 SHALL 允许 CSS Modules 和 Tailwind utility class 在同一项目中共存。

#### Scenario: CSS Module 和 Tailwind class 同时使用

- **WHEN** 某个组件仍使用 CSS Module 而另一组件已迁移到 Tailwind
- **THEN** 两种样式方式 SHALL 互不干扰
- **THEN** 应用 SHALL 正常构建和运行
