## Why

当前系统仅支持学号+密码登录，团队成员希望提供 GitHub 作为替代登录方式，提升登录便捷性。前端登录页已预留 GitHub 登录按钮（提示"暂未开放"），后端 User 实体已有 `githubId` 和 `githubUsername` 字段，基础设施已就绪。

## What Changes

- 新增 GitHub OAuth2 授权流程：用户点击 GitHub 登录 → 跳转 GitHub 授权 → 回调后端换取 access_token → 获取 GitHub 用户信息 → 匹配本地用户 → 设置 JWT Cookie
- 新增 GitHub 账号绑定/解绑功能：已登录用户可在个人设置中绑定或解绑 GitHub 账号
- 前端登录页激活 GitHub 登录按钮，前端个人设置页新增 GitHub 绑定区域
- GitHub OAuth 仅作为已有学号账号的替代登录方式，不支持通过 GitHub 直接注册新账号

## Capabilities

### New Capabilities

- `github-oauth`: GitHub OAuth2 登录与账号绑定功能，包含授权流程、回调处理、绑定/解绑管理

### Modified Capabilities

- `frontend-login-integration`: 激活 GitHub 登录按钮，接入后端 OAuth 授权流程
- `frontend-user-profile`: 个人设置页新增 GitHub 账号绑定/解绑区域

## Impact

- **后端**：新增 GitHub OAuth 相关接口（授权、回调、绑定、解绑），需添加 Spring Security OAuth2 Client 依赖或手动实现 OAuth 流程
- **前端**：登录页修改（激活按钮）、个人设置页新增绑定区域
- **配置**：`.env` 新增 `GITHUB_CLIENT_ID` 和 `GITHUB_CLIENT_SECRET`
- **数据库**：`tb_user` 表的 `github_id` 和 `github_username` 字段已有，无需迁移
- **依赖**：可能需要新增 `spring-boot-starter-oauth2-client` 或使用 `WebClient`/`RestClient` 手动调用 GitHub API
