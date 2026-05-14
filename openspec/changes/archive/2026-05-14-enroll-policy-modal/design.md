## Context

报名页面 (`/enroll`) 底部的"报名须知"和"隐私政策"链接当前为占位符 (`href="#"`)，点击无响应。团队需要向报名者清晰传达隐私保护规则、报名流程和考核说明，其中报名流程使用 Mermaid 流程图可视化展示。

项目已有 `react-markdown@10.1.0`、`remark-gfm@4.0.1`、`rehype-sanitize@6.0.0`，Ant Design 6 提供 Modal 组件，技术栈已具备基础条件，仅需补充 Mermaid 渲染能力。

## Goals / Non-Goals

**Goals:**
- 点击"报名须知"和"隐私政策"链接后弹出 Modal 展示对应文档
- 文档使用 Markdown 格式渲染，支持表格、加粗、列表等标准语法
- 报名流程使用 Mermaid 流程图语法嵌入 Markdown，渲染为 SVG 图形
- 弹窗在桌面端和移动端均正常显示

**Non-Goals:**
- 不涉及后端 API、数据库、权限变更
- 不实现隐私政策内容的法律合规审核（内容由团队自行确认）
- 不实现 Markdown 编辑器或动态修改文档内容的功能
- 不做服务端渲染（Mermaid 仅在客户端渲染）

## Decisions

### 1. 使用 `mermaid` npm 库客户端渲染
- **选择**: 安装 `mermaid@^11.x` 依赖，在 `useEffect` 中调用 `mermaid.render()` 将 Mermaid 语法转换为 SVG
- **替代方案**: `react-markdown` + `remark-mermaidjs` 插件，但该插件依赖 Playwright，体积和构建复杂度远超直接调用 mermaid API
- **理由**: 直接调用 mermaid API 更轻量，与 React 生命周期配合更灵活

### 2. 自定义 `react-markdown` 的 `code` 组件
- **选择**: 在 `react-markdown` 的 `components` 中自定义 `code` 渲染器，当 `className` 包含 `language-mermaid` 时，渲染为 `MermaidBlock` 组件而非 `<pre><code>`
- **理由**: `react-markdown` 默认将代码块渲染为 `<pre><code>`，无法处理 Mermaid 语法；自定义组件是标准扩展方式

### 3. 文档内容抽离为独立的 TypeScript 文件
- **选择**: 将隐私政策和报名须知的 Markdown 字符串存放在 `src/components/Enroll/policies.ts` 中导出
- **替代方案**: 放在组件内联字符串、或放在 public 目录作为 `.md` 文件异步加载
- **理由**: 内联字符串维护困难；异步加载增加网络请求且 SSR 场景复杂；TypeScript 导出文件编译时打包，无额外网络开销，类型安全

### 4. 使用 Ant Design Modal
- **选择**: 复用项目已有的 `antd` Modal 组件，配合暗色主题样式覆盖
- **理由**: 与现有 UI 体系一致，无需引入额外组件库

## Risks / Trade-offs

- **[Risk] Mermaid 库体积较大（~500KB gzip 后约 150KB）**
  - **缓解**: 仅在报名页面加载，影响范围有限；Mermaid 11 采用模块化设计，实际按需加载核心渲染器
- **[Risk] Mermaid 客户端渲染可能导致 Modal 打开时短暂闪烁**
  - **缓解**: `MermaidBlock` 在 `useEffect` 中同步渲染，配合容器占位高度，视觉上无闪烁
- **[Trade-off] Markdown 内容硬编码在前端，更新需重新发版**
  - **缓解**: 隐私政策和报名须知为低频变更内容；若后续需要动态更新，可迁移至 CMS 或接口获取，当前方案为合理折中

## Migration Plan

无需迁移。纯前端新增功能，无数据迁移或回滚需求。部署后原有报名流程不受影响，仅新增链接点击交互。
