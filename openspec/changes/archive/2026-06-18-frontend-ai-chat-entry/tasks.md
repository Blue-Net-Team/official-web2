## 1. 环境与配置

- [x] 1.1 在 `src/frontend/.env` 和 `.env.example` 中新增 `NEXT_PUBLIC_AI_SERVICE_*` 变量
- [x] 1.2 在 `docker/docker-compose.yml` 的 `frontend` 服务中注入 AI 服务环境变量
- [x] 1.3 在 `src/frontend/src/apis/config.ts` 中新增 `AI_CHAT_BASE_URL` 构建函数

## 2. API 与类型

- [x] 2.1 创建 `src/frontend/src/apis/schema/ai-chat.dto.ts` 定义 `AiStreamChunk` / `ChatMessage` 类型
- [x] 2.2 创建 `src/frontend/src/apis/services/ai-chat.service.ts` 封装 SSE 请求与行缓冲解析
- [x] 2.3 为 `ai-chat.service.ts` 添加单元测试或类型级测试

## 3. 状态管理 Hook

- [x] 3.1 创建 `src/frontend/src/hooks/useAiChat.ts`：消息列表、conversation_id、发送、重置、取消
- [x] 3.2 验证 SSE 事件聚合为单条 assistant 消息

## 4. UI 组件

- [x] 4.1 创建 `src/frontend/src/components/AiChat/ChatBubble.tsx`（用户/AI 气泡，含 reasoning、tool_call、content）
- [x] 4.2 创建 `src/frontend/src/components/AiChat/ReasoningBlock.tsx`（思考中展开、结束后折叠）
- [x] 4.3 创建 `src/frontend/src/components/AiChat/ToolCallCard.tsx`（默认折叠，展示工具名与结果）
- [x] 4.4 创建 `src/frontend/src/components/AiChat/MessageList.tsx`（滚动消息列表）
- [x] 4.5 创建 `src/frontend/src/components/AiChat/ChatInput.tsx`（输入框与发送按钮）
- [x] 4.6 创建 `src/frontend/src/components/AiChat/AiChatPanel.tsx`（Drawer 内部面板）
- [x] 4.7 创建 `src/frontend/src/components/AiChat/AiChatDrawer.tsx`（右侧抽屉壳）
- [x] 4.8 创建 `src/frontend/src/components/AiChat/AiChatFloatButton.tsx`（入口按钮）

## 5. 全局入口集成

- [x] 5.1 创建 `src/frontend/src/components/GlobalFloatActions.tsx` 将 Bug 反馈与 AI 客服合并为 `FloatButton.Group`
- [x] 5.2 在 `src/frontend/src/app/layout.tsx` 中替换 `<BugReportFloatButton />` 为 `<GlobalFloatActions />`
- [x] 5.3 移除原 `BugReportFloatButton` 的单独引用（保留组件用于 Group）

## 6. 响应式与体验

- [x] 6.1 为 Drawer 设置响应式宽度（桌面 420px / 移动端 100%）
- [x] 6.2 添加空状态、加载状态与错误提示
- [x] 6.3 为气泡与 reasoning 块添加进入/展开动画

## 7. 验证

- [x] 7.1 本地启动 `ai-service` 与前端 dev 服务，检查 3000 端口占用
- [x] 7.2 在首页点击 AI 按钮打开抽屉并发送消息，验证 SSE 流式渲染
- [x] 7.3 验证 reasoning 思考时展开、结束后折叠
- [x] 7.4 验证 tool_call/tool_result 默认折叠
- [x] 7.5 验证 Markdown 实时渲染
- [x] 7.6 验证刷新页面后 conversation_id 重置
- [x] 7.7 运行 `pnpm lint` 与 `pnpm format:check`

## 8. Docker 部署

- [x] 8.1 清理占用 3000 端口的遗留进程
- [x] 8.2 按 CI/CD 参数构建前端镜像 `bluenet-frontend:latest`
- [x] 8.3 使用 `docker compose --profile app up -d --no-deps frontend` 启动容器
- [x] 8.4 验证前端容器端口映射与运行状态
- [x] 8.5 在 Docker 容器中再次端到端验证 AI 客服流式对话
