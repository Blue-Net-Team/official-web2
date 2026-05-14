## 1. Setup

- [x] 1.1 安装 `mermaid` npm 依赖（`pnpm add mermaid`）

## 2. Markdown 内容文件

- [x] 2.1 创建 `src/components/Enroll/policies.ts`，导出隐私政策 Markdown 字符串
- [x] 2.2 创建 `src/components/Enroll/policies.ts`，导出报名须知 Markdown 字符串（含 Mermaid 流程图）

## 3. PolicyModal 组件

- [x] 3.1 创建 `src/components/Enroll/PolicyModal.tsx` 组件，使用 antd Modal + react-markdown 渲染 Markdown
- [x] 3.2 在 PolicyModal 中自定义 `code` 组件，检测 `language-mermaid` 并渲染为 MermaidBlock
- [x] 3.3 创建 `MermaidBlock` 子组件，在 `useEffect` 中调用 `mermaid.render()` 异步渲染 SVG
- [x] 3.4 处理 Mermaid 渲染错误（降级显示原始代码块）
- [x] 3.5 应用暗色主题样式，确保 Markdown 内容在暗色背景下的可读性

## 4. EnrollForm 集成

- [x] 4.1 修改 `EnrollForm.tsx`，引入 PolicyModal 和 policies 内容
- [x] 4.2 将底部"报名须知"链接从 `href="#"` 改为点击打开 PolicyModal（传入报名须知内容）
- [x] 4.3 将底部"隐私政策"链接从 `href="#"` 改为点击打开 PolicyModal（传入隐私政策内容）

## 5. 样式与适配

- [x] 5.1 确保 Modal 内容区域在桌面端和移动端均可正常滚动
- [x] 5.2 确保 Mermaid SVG 在暗色主题下节点文字清晰可读
- [x] 5.3 确保 Mermaid SVG 宽度不溢出 Modal 内容区，超长时支持横向滚动

## 6. 验证

- [x] 6.1 本地运行前端，点击"报名须知"验证 Modal 弹出、Markdown 渲染正确、Mermaid 流程图正常显示
- [x] 6.2 点击"隐私政策"验证表格、加粗文本等 Markdown 语法渲染正确
- [x] 6.3 移动端浏览器验证 Modal 适配
