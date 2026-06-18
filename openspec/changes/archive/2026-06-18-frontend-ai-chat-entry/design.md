## Context

蓝网官网前端基于 Next.js 15 + React 19 + Ant Design 6，当前已有一个全局的 Bug 反馈 FloatButton（`src/frontend/src/components/BugReport`）。AI 客服能力由独立的 `ai-service`（FastAPI）提供，通过 `POST /ai/v1/chat/stream` 返回 SSE 事件流，事件类型包括 `reasoning`、`tool_call`、`tool_result`、`content`、`done`。前端目前没有对接该能力的任何入口或组件。

## Goals / Non-Goals

**Goals:**
- 在全局公开页面提供统一的 AI 客服入口。
- 通过右侧抽屉承载对话界面。
- 将一次 AI 回复对应的多条 SSE 事件聚合到一个气泡中。
- 实时展示 reasoning、折叠展示工具调用、Markdown 渲染最终答案。
- 支持会话在抽屉开关间保持，刷新页面后开启新会话。
- 将 AI 服务地址配置与主 API 服务解耦。

**Non-Goals:**
- 不修改 `ai-service` 的会话存储机制（仍为内存）。
- 不实现多 tab 同步或刷新后恢复历史。
- 不实现用户登录态与 AI 会话的绑定。
- 不对 AI 服务进行网关聚合或反向代理改造。

## Decisions

1. **全局入口 vs 仅首页**
   - **Decision**: 在根布局中提供全局入口，与 Bug 反馈按钮合并为 `FloatButton.Group`。
   - **Rationale**: 用户已选择 A 方案（合并按钮）。合并后两个按钮始终在一起，视觉一致；AI 客服在方向、招新等页面也有用，全局入口体验更好。admin 布局独立，不受影响。

2. **抽屉位置**
   - **Decision**: 从右侧展开 Ant Design `Drawer`。
   - **Rationale**: 右侧抽屉与大多聊天助手（Intercom、Crisp）一致，不遮挡页面主内容，且符合用户习惯。

3. **气泡聚合策略**
   - **Decision**: 一次 `chat_stream` 调用产生的事件全部归入同一个 assistant 气泡，直到 `done`。
   - **Rationale**: `RagAgent.chat_stream` 在工具调用阶段输出 reasoning/tool_call/tool_result，最终阶段输出 content；用户明确要求“一个回复一个气泡”。

4. **实时 Markdown 渲染**
   - **Decision**: content 流实时用 `ReactMarkdown` 渲染。
   - **Rationale**: 用户明确要求实时 Markdown。未闭合的 Markdown 语法可能导致轻微布局抖动，若后续体验不佳再降级为“流式纯文本 + done 后 Markdown”。

5. **会话状态存储位置**
   - **Decision**: `conversation_id` 和消息列表保存在组件内部 state/ref，不使用 `localStorage`/`sessionStorage`。
   - **Rationale**: 刷新后开启新会话；抽屉关闭再打开仍在同一页面生命周期内，无需持久化到 storage。

6. **AI 服务地址配置**
   - **Decision**: 新增独立的 `NEXT_PUBLIC_AI_SERVICE_*` 环境变量，并在 Docker Compose 前端服务中注入。
   - **Rationale**: AI 服务与主 API 服务运行在不同端口/路径，必须独立配置；`NEXT_PUBLIC_` 前缀保证客户端代码可读取。

7. **SSE 客户端实现**
   - **Decision**: 使用原生 `fetch` + `ReadableStream` + `TextDecoder` 解析 SSE，而非 axios。
   - **Rationale**: axios 对 SSE 流式读取支持不直接；原生 API 配合 `AbortController` 能干净地取消请求。需处理中文多字节被 chunk 边界截断的问题，维护行缓冲区。

## Risks / Trade-offs

| Risk | Mitigation |
|------|------------|
| 两个 FloatButton 在右下角视觉冲突 | 使用 `FloatButton.Group` 垂直排列，保持统一风格 |
| SSE chunk 截断中文导致 JSON 解析失败 | 使用 `TextDecoder` + 行缓冲区，按 `\n\n` 边界解析 `data:` 行 |
| 实时 Markdown 渲染导致布局抖动 | 先按实时 Markdown 实现，后续根据体验决定是否降级为纯文本 |
| AI 服务重启后内存会话丢失 | 本次变更不解决，后续如需跨重启持久化再引入 Redis/DB |
| 移动端 Drawer 宽度过窄 | 使用响应式宽度：桌面 420px，移动端 100% |
| 工具调用阶段 content 为空导致气泡塌陷 | 气泡最小高度与骨架占位，确保视觉稳定 |

## Migration Plan

1. 合并代码后，在本地 `.env` 中补充 `NEXT_PUBLIC_AI_SERVICE_*` 变量。
2. 确保 `ai-service` 容器已启动并暴露 8000 端口。
3. 前端开发服务器启动前检查 3000 端口占用。
4. 部署时同步更新 `docker/docker-compose.yml` 前端环境变量。
5. 回滚：移除根布局中的 `GlobalFloatActions`，恢复原有 `BugReportFloatButton`。

## Open Questions

- 是否在 AI 服务前增加网关统一入口？本次不变更。
- 是否需要为 AI 客服添加访问统计或错误上报？本次不实现。
