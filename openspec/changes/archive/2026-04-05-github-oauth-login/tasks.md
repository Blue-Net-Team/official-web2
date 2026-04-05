## 1. 后端基础设施

- [x] 1.1 在 `.env.example` 和 `application.yml` 中添加 GitHub OAuth 配置项（`GITHUB_CLIENT_ID`、`GITHUB_CLIENT_SECRET`）
- [x] 1.2 创建 `GitHubOAuthProperties` 配置类，绑定 `github.oauth` 前缀配置
- [x] 1.3 创建 `GitHubOAuthService` 领域服务：生成授权 URL（含 state 参数）、code 换 access_token、获取 GitHub 用户信息

## 2. 后端登录流程（API 层）

- [x] 2.1 创建 `AuthController` 中的 GitHub 登录端点 `GET /api/v1/auth/github`：生成 state 存入 Redis，返回授权 URL
- [x] 2.2 创建 GitHub 回调端点 `GET /api/v1/auth/github/callback`：验证 state → 换 token → 获取用户信息 → 匹配用户 → 设置 Cookie → 重定向
- [x] 2.3 将 GitHub OAuth 相关路径加入安全白名单 — 使用 `@RequiresPermission` 注解控制，无需额外白名单

## 3. 后端绑定/解绑流程

- [x] 3.1 创建查询绑定状态端点 `GET /api/v1/auth/github/status`：返回当前用户的 githubUsername（未绑定返回 null）
- [x] 3.2 创建绑定发起端点 `GET /api/v1/auth/github/bind`：生成 state 存入 Redis（区分登录和绑定的 state key），返回授权 URL
- [x] 3.3 绑定回调共用登录回调端点（通过 Redis state 区分流程）：验证 state → 获取 GitHub 信息 → 校验未绑定/未被占用 → 写入 githubId 和 githubUsername → 重定向
- [x] 3.4 创建解绑端点 `DELETE /api/v1/auth/github/bind`：校验已绑定 → 清除 githubId 和 githubUsername
- [x] 3.5 将绑定相关路径加入权限控制（`AUTHENTICATED` 级别）

## 4. 后端 User 领域更新

- [x] 4.1 在 `User` 实体或 `UserVO` 中添加 githubId/githubUsername 的 getter/setter — 已存在
- [x] 4.2 在 `UserRepository` 接口中添加 `findByGithubId(String githubId)` 方法
- [x] 4.3 在 Repository 实现中添加 `findByGithubId` 的 MyBatis 查询

## 5. 前端登录页集成

- [x] 5.1 创建前端 `authService` GitHub 方法：`getGithubAuthorizeUrl()`、`getGithubBindUrl()`、`getGithubBindingStatus()`、`unbindGithub()`
- [x] 5.2 修改登录页 GitHub 按钮：点击时调用 API 获取授权 URL 并重定向（替换当前的 `messageApi.info('暂未开放')`）
- [x] 5.3 登录页添加回调参数处理：检测 URL 中的 `?github=success/unbound/error` 参数，显示对应提示

## 6. 前端个人设置页集成

- [x] 6.1 创建前端 GitHub 绑定组件 `GitHubBinding`：展示绑定状态、绑定按钮、解绑按钮（含确认弹窗）
- [x] 6.2 在个人信息 Tab 的邮箱区域下方引入 `GitHubBinding` 组件
- [x] 6.3 个人设置页添加回调参数处理：检测 URL 中的 `?github=binding_success/already_bound` 参数

## 7. 测试

- [x] 7.1 后端单元测试：`GitHubOAuthService`（生成授权 URL、换 token、获取用户信息）
- [x] 7.2 后端单元测试：`AuthController` GitHub 相关端点的 MockMvc 测试
- [x] 7.3 后端集成测试：完整 OAuth 回调流程（绑定、解绑、登录、state 验证）
