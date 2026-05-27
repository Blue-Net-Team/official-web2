## Why

当前考核平台的代码编辑区域全部使用原生 `<textarea>` 或 Ant Design `Input.TextArea`，没有任何语法高亮、代码补全、括号匹配等编辑器功能。考生在编写算法题代码时体验较差，管理员在编辑标准解和 Generator 源码时也容易出错。引入专业代码编辑器组件可以显著提升代码编写体验和准确性。

## What Changes

- 在前端引入 Monaco Editor 作为代码编辑器组件
- 替换考生端算法题代码编写区的原生 textarea 为 Monaco Editor，支持语法高亮、括号匹配、行号显示
- 替换管理端考题编辑中的 Generator 源码、标准解源码、语言模板代码的输入框为 Monaco Editor
- 配置 Monaco Editor 的语言支持：Python、C、C++、Java、JavaScript
- 针对 Next.js 15 + React 19 环境配置 Monaco Editor 的动态加载和 SSR 兼容
- Markdown 代码块渲染增加语法高亮（可选增强）

## Capabilities

### New Capabilities
- `frontend-monaco-code-editor`: 前端 Monaco Editor 代码编辑器组件封装及集成

### Modified Capabilities
- 无现有 spec 级别的需求变更，本变更仅涉及前端组件替换和 UI 增强

## Impact

- **新增依赖**: `@monaco-editor/react`（Monaco Editor 的 React 封装）
- **修改文件**: `AlgorithmQuestion.tsx`、`QuestionDrawer.tsx`、`MarkdownRenderer.tsx`
- **新增文件**: 封装后的 Monaco Editor 通用组件
- **不影响**: 后端 API、数据库结构、业务逻辑
