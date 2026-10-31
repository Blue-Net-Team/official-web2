# Validation — GitHub issue #86

## 自动化检查

- 开发期请求层与上传边界测试共 32 个用例全部通过；根据后续要求，最终交付已移除前端测试文件、Vitest 配置和依赖。
- `pnpm exec tsc --noEmit`：通过（包含移除测试配置后的最终源码）。
- Prettier（本次修改文件）：通过。
- `pnpm exec eslint src`：0 errors；仓库仍有 45 条既有 warnings，本次未新增警告。

请求层用例覆盖后端 `msg` 优先、空白消息、HTTP 200 业务错误、15 个状态码、空/非 JSON 响应、超时、断网、未知状态码、SSR、原始错误拒绝、401 跳转和局部静默配置。

## Playwright 验收

验证环境为当前改动的 Next.js 开发服务（3001；3000 在检查后被另一进程占用并提供旧页面）。

- 登录接口返回 HTTP 400 + `ResponseMessage.msg="学号或密码错误"`：仅显示一次“学号或密码错误”。
- 无有效 `msg` 的 HTTP 400/403/404/500：分别显示参数、权限、资源不存在和服务器错误中文文案；每个请求只新增一个 Toast。
- 请求 `net::ERR_FAILED`：显示“网络连接异常，请检查网络后重试”。
- 空学号/密码提交：仍由页面本地校验处理，不发起登录请求。
- 401 的 Promise 拒绝、原始错误保留及登录页跳转已在开发期请求层测试中验证。

## 范围确认

- Axios `apiClient` / `publicClient` 统一处理；登录状态探测、可选数据、权限预览和轮询使用显式 `skipErrorMessage` 保持静默。
- 原生 `fetch`（AI 草稿）、XHR/预签名文件上传、SSE 未迁移；其本地错误提示保留。准备/确认上传的 Axios 和业务错误不重复提示，确认上传仅最终重试失败提示。
- `docker/dev-up.ps1 -Action build -Service frontend` 与 `-Action up -Service frontend` 已成功执行，容器首页返回 HTTP 200。
- 页面本地表单校验、文件格式/大小校验、业务结果数据提示和成功提示保留。
- 后端 API、`ResponseMessage` 结构和权限逻辑未修改。
