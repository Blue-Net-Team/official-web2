## 1. Axios 客户端配置

- [x] 1.1 修改 `src/apis/client.ts`，添加 `withCredentials: true` 到 apiClient
- [x] 1.2 修改 `src/apis/client.ts`，添加 `withCredentials: true` 到 publicClient
- [x] 1.3 添加请求拦截器，对 POST/PUT/DELETE/PATCH 自动添加 `X-CSRF-Token` header

## 2. API 类型定义更新

- [x] 2.1 修改 `src/apis/schema/type.ts`，更新 `UserAuthResponseDTO`：移除 `token`，添加 `csrfToken`
- [x] 2.2 新增 `AuthMeResponseDTO` 类型：`{ userInfo: UserInfo; csrfToken: string }`

## 3. Auth Service 改造

- [x] 3.1 新增 `authService.getAuthMe()` 方法，调用 `GET /auth/me`
- [x] 3.2 修改 `authService.login()`，处理新的响应格式（无 token，有 csrfToken）
- [x] 3.3 修改 `authService.logout()`，确保请求携带 Cookie

## 4. Auth Store 改造

- [x] 4.1 移除 `token` 状态字段
- [x] 4.2 新增 `csrfToken` 状态字段（内存存储，不持久化）
- [x] 4.3 修改 `login` action，存储 `userInfo` 和 `csrfToken`
- [x] 4.4 修改 `logout` action，清除所有状态
- [x] 4.5 新增 `checkAuthStatus` action，调用 `getAuthMe()` 恢复登录状态
- [x] 4.6 更新 persist 配置：仅持久化 `userInfo`，不持久化 `csrfToken`

## 5. 应用初始化

- [x] 5.1 在 `src/app/layout.tsx` 或创建 `AuthProvider` 组件
- [x] 5.2 应用启动时调用 `authStore.checkAuthStatus()`
- [x] 5.3 处理初始化过程中的 loading 状态
- [x] 5.4 处理 401 响应自动跳转登录页

## 6. 清理遗留代码

- [x] 6.1 移除 `localStorage.getItem('token')` 和 `localStorage.setItem('token', ...)` 调用
- [x] 6.2 移除 `localStorage.removeItem('token')` 调用
- [x] 6.3 移除 axios 拦截器中的 `Authorization: Bearer` header 逻辑

## 7. 环境配置

- [x] 7.1 更新 `.env.local` 示例文件，添加 `NEXT_PUBLIC_API_BASE_URL` 说明
- [x] 7.2 确保 `BACKEND_HOST` 和 `BACKEND_PORT` 环境变量正确配置

## 8. 测试验证

- [x] 8.1 手动测试登录流程：验证 Cookie 设置成功
- [x] 8.2 手动测试页面刷新：验证登录状态保持
- [x] 8.3 手动测试登出流程：验证 Cookie 清除
- [x] 8.4 手动测试 CSRF Token：验证 POST 请求携带 header
- [x] 8.5 手动测试 401 响应：验证自动跳转登录页

## 9. 文档更新

- [x] 9.1 更新前端 `CLAUDE.md`（如有），记录新的认证机制
- [x] 9.2 更新 README（如有），说明环境变量配置
