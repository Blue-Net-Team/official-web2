## Why

GitHub issue #86 指出，请求失败时界面经常只显示 `400`、`500` 或 Axios 的英文状态码错误，而没有展示后端返回的业务消息。当前 API 错误提示分散在拦截器、Store、Hooks 和页面中，不仅行为不一致，还存在同一错误被重复提示的问题，需要统一前端错误处理职责。

## What Changes

- 在 Axios 响应拦截器中统一处理 HTTP 错误和业务错误，并调用 Ant Design `message.error()` 展示可读提示。
- 优先展示后端 `ResponseMessage.msg`；消息缺失时按 HTTP 状态码提供中文默认文案，网络异常、超时和未知错误也有明确兜底。
- 保留错误 Promise 拒绝语义以及现有 401 登录跳转行为，确保页面不会将失败误判为成功。
- 让认证客户端和公开客户端使用一致的错误提示规则，并避免服务端渲染期间调用浏览器消息 API。
- 清理页面和 Store 中针对 API 失败的重复消息解析与提示；页面继续负责本地表单校验、成功流程和加载状态。
- 增加针对消息优先级、常见状态码、网络错误和重复提示的自动化验证，并完成关键用户链路端到端验证。

## Capabilities

### New Capabilities

- `frontend-http-error-handling`: 定义 Axios 请求失败的统一消息解析、状态码默认文案、集中提示、错误传播及页面去重行为。

### Modified Capabilities

## Impact

- 主要影响 `src/frontend/src/apis/client.ts`，以及直接解析或提示 API 错误的前端页面、Hooks 和 `authStore`。
- 涉及 `apiClient`、`publicClient`、登录、密码相关流程及使用通用请求 Hooks 的页面。
- 不修改后端 API、`ResponseMessage` 结构或权限逻辑，不引入新的运行时依赖。
- 原生 `fetch` 和文件直传 XHR 不强制迁移到 Axios；仅明确其与本次统一处理范围的边界。
- 关联 GitHub issue #86。
