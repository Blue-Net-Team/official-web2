## Why

报名页面底部的"报名须知"和"隐私政策"链接当前为占位符（`href="#"`），点击无响应，无法向报名者展示必要的规则和隐私说明。需要在点击后弹出 Modal 展示完整的 Markdown 格式文档，其中报名流程使用 Mermaid 流程图渲染，提升信息传达的清晰度。

## What Changes

- 新增隐私政策 Markdown 内容文档，涵盖信息收集范围、使用目的、存储保护、用户权利
- 新增报名须知 Markdown 内容文档，涵盖报名条件、报名流程（含 Mermaid 流程图）、填写规范、方向选择说明、考核说明
- 新增 `PolicyModal` 组件，支持 Markdown 渲染 + Mermaid 流程图解析为 SVG
- 新增 `MermaidBlock` 子组件，在客户端异步渲染 Mermaid 语法为 SVG
- 修改 `EnrollForm` 组件底部链接，从空链接改为点击打开对应 PolicyModal
- 新增 `mermaid` npm 依赖（项目已有 `react-markdown`、`remark-gfm`、`rehype-sanitize`）

## Capabilities

### New Capabilities

- `enroll-policy-modal`: 报名页面政策文档弹窗展示，包含隐私政策和报名须知的 Markdown 渲染及 Mermaid 流程图支持

### Modified Capabilities

- 无现有能力需求变更（纯前端交互增强，不涉及后端 API 或业务规则变更）

## Impact

- **前端代码**: `src/frontend/src/components/Enroll/` 目录新增组件和内容文件，`EnrollForm.tsx` 修改链接交互
- **依赖**: 新增 `mermaid@^11.15.0` 依赖
- **无 API 变更**: 不涉及后端接口、数据库、权限变更
