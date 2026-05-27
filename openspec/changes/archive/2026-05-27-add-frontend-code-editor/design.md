## Context

当前考核平台前端的所有代码编辑区域均使用原生 `<textarea>` 或 Ant Design `Input.TextArea`，仅通过 CSS `font-mono` 提供等宽字体，无语法高亮、无代码补全、无行号显示。主要受影响区域包括：

- 考生端算法题代码编写（`AlgorithmQuestion.tsx`）
- 管理端 Generator 源码编辑（`QuestionDrawer.tsx`）
- 管理端标准解源码编辑（`QuestionDrawer.tsx`）
- 管理端语言模板代码编辑（`QuestionDrawer.tsx`）

项目使用 Next.js 15 + React 19 + TypeScript + Tailwind CSS，需要确保引入的编辑器库与此技术栈兼容。

## Goals / Non-Goals

**Goals:**
- 为所有代码编辑区域提供语法高亮、行号显示、括号匹配功能
- 支持 Python、C、C++、Java、JavaScript 五种语言的语法识别
- 编辑器组件封装为可复用的 React 组件，支持主题切换和语言动态切换
- 解决 Monaco Editor 在 Next.js SSR 环境下的兼容问题
- 保持现有 UI 风格和用户体验的一致性

**Non-Goals:**
- 不实现后端代码执行或在线判题功能（已有独立服务）
- 不实现代码自动补全的智能提示（仅基础关键字提示）
- 不修改现有表单提交和数据流逻辑
- 不替换 Markdown 题干的编辑器（非代码编辑场景）

## Decisions

### 1. 选用 Monaco Editor 而非 CodeMirror
**选择**: `@monaco-editor/react`（Monaco Editor 的 React 封装）
**理由**:
- Monaco 是 VS Code 同款编辑器，考生熟悉度高，体验最好
- 内置对 Python、C、C++、Java、JavaScript 的完整语法高亮支持
- 括号匹配、代码折叠、minimap、错误波浪线等功能开箱即用
- 考生端是核心使用场景，编辑器体验直接影响考核质量

**替代方案**: CodeMirror 6 更轻量，但功能较弱，需要额外配置语言包和插件才能达到 Monaco 的基础体验。考虑到考核场景对编辑器体验的要求，Monaco 的体积成本可接受。

### 2. 动态加载策略
**选择**: 使用 Next.js 动态 `import()` + `@monaco-editor/react` 的 `loader` 配置
**理由**:
- Monaco Editor 体积较大（~3MB），必须避免打包进主 bundle
- 仅在算法题相关页面按需加载，减少首屏加载时间
- `@monaco-editor/react` 支持配置 `loader` 从 CDN 或本地加载 Monaco 资源

### 3. 组件封装设计
**选择**: 封装 `CodeEditor` 通用组件，统一配置主题、字体、语言映射
**理由**:
- 5 个代码编辑区域配置高度一致（暗色主题、等宽字体、相似尺寸）
- 统一封装避免重复配置，便于后续维护（如主题切换、快捷键调整）
- 组件 Props 设计：`value`、`onChange`、`language`、`readOnly`、`height`

### 4. 语言映射
**选择**: 将项目内部语言标识（`python`、`c`、`cpp`、`java`、`javascript`）映射到 Monaco Editor 的语言标识
**映射关系**:
- `python` → `python`
- `c` → `c`
- `cpp` → `cpp`
- `java` → `java`
- `javascript` → `javascript`

### 5. 主题适配
**选择**: 使用 Monaco 内置暗色主题 `vs-dark`，与现有暗色 UI 风格一致
**理由**: 现有考核页面和后台管理页面均为暗色主题，无需引入额外的主题文件。

## Risks / Trade-offs

- **[Risk] Monaco Editor 体积大，可能导致首屏加载变慢** → Mitigation: 使用动态导入，仅在需要时加载；配置 loader 从 CDN 加载编辑器核心文件，减少打包体积
- **[Risk] Next.js SSR 与 Monaco Editor 不兼容** → Mitigation: `@monaco-editor/react` 内部已处理 SSR 检测，组件在客户端渲染；必要时使用 `next/dynamic` 的 `ssr: false` 配置
- **[Risk] 部分低配置设备运行 Monaco 可能出现性能问题** → Mitigation: 配置 `minimap: { enabled: false }` 关闭小地图，降低资源占用；保持 `scrollBeyondLastLine: false` 等轻量配置
- **[Risk] 与现有 Ant Design Form 的集成** → Mitigation: `CodeEditor` 组件包装为受控组件，兼容 `Form.Item` 的 `value`/`onChange` 模式
