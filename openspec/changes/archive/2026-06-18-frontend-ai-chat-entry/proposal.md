## Why

蓝网官网目前已具备基于 RAG 的 AI 客服后端（`ai-service`），但前端缺少统一入口，用户无法在浏览网站时直接发起咨询。为了让潜在招新对象和访客能随时咨询团队方向、招新流程、考核等问题，需要在官网提供可折叠的 AI 聊天组件，并接入已有的 SSE 流式对话接口。

## What Changes

- 在全局公开页面右下角添加一个 AI 客服入口，与现有的 Bug 反馈按钮合并为 `FloatButton.Group`。
- 点击入口后从右侧展开聊天抽屉（Drawer）。
- 抽屉内提供对话面板：消息列表、输入框、清空会话按钮。
- 接入 `ai-service` 的 `POST /ai/v1/chat/stream` SSE 接口，支持 reasoning、tool_call、tool_result、content 事件聚合到同一个 AI 气泡。
- 新增 AI 服务独立的环境变量配置（`NEXT_PUBLIC_AI_SERVICE_*`），并在 Docker Compose 前端服务中注入。
- 会话状态保存在前端内存，`conversation_id` 在抽屉开关之间保持；刷新页面后开启新会话。

## Capabilities

### New Capabilities

- `frontend-ai-chat-entry`：前端 AI 客服入口、抽屉、气泡、SSE 流式对接、会话状态管理。

### Modified Capabilities

- 无

## Impact

- `src/frontend/src/app/layout.tsx`：替换单独的 Bug 反馈入口为全局浮动操作组。
- `src/frontend/src/components/AiChat/`：新增 AI 聊天组件集。
- `src/frontend/src/apis/services/ai-chat.service.ts`：新增 SSE 请求封装。
- `src/frontend/src/hooks/useAiChat.ts`：新增聊天状态 Hook。
- `src/frontend/.env.example`、`docker/docker-compose.yml`：新增 AI 服务环境变量。
