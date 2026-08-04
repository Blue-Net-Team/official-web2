## 1. 配置结构调整与共享基础设施

- [x] 1.1 创建 `GitHubAppConfig` 记录类，包含 appId、privateKeyPath、type、enabled、owner、repo、org、teamMapping 等字段
- [x] 1.2 创建 `GitHubAppsProperties` 统一配置类（prefix: `github`），包含 `Map<String, GitHubAppConfig> apps`
- [x] 1.3 创建 `GitHubJwtGenerator` 共享组件，封装私钥加载和 JWT 生成逻辑
- [x] 1.4 重构 `GitHubAppTokenService`，支持按 appName 获取 Token，根据 type 选择 installation URL
- [x] 1.5 保留 `GitHubAppProperties` 作为过渡层，内部委托给 `GitHubAppsProperties`，确保现有 Issue 同步功能不受影响
- [x] 1.6 修改 `GitHubIssueClient`，通过 `GitHubAppTokenService.getAccessToken("issue-sync")` 获取 Token
- [x] 1.7 更新 `application.yml`，将 `github.app.*` 迁移为 `github.apps.issue-sync.*`，添加 `github.apps.org-invitation.*` 配置块
- [x] 1.8 更新 `.env.example`，添加 `GITHUB_ISSUE_APP_ID`、`GITHUB_ISSUE_PRIVATE_KEY_PATH`、`GITHUB_ORG_APP_ID`、`GITHUB_ORG_PRIVATE_KEY_PATH` 环境变量说明
- [x] 1.9 验证 Issue 同步功能回归测试通过

## 2. GitHub 组织邀请服务

- [x] 2.1 创建 `GitHubOrgInvitationClient`，封装 `POST /orgs/{org}/invitations` API，支持 `invitee_id` 或 `email` 参数，支持 `team_ids`
- [x] 2.2 创建 `GitHubOrgTeamResolver`，启动时通过 `GET /orgs/{org}/teams` 解析配置的 team name 到 team ID，缓存结果
- [x] 2.3 创建 `GitHubOrgInvitationService`，编排邀请逻辑：解析用户邀请身份（githubId/email）、查询 team ID、调用邀请 API、处理异常和日志
- [x] 2.4 创建 `GitHubOrgAsyncConfig`，配置独立线程池用于异步邀请
- [x] 2.5 添加单元测试：验证邀请身份解析、team 映射、异常处理

## 3. 自动邀请集成

- [x] 3.1 修改 `AssessmentDecisionPublicationService.publish()`，在角色升级后异步调用 `GitHubOrgInvitationService.inviteAsync(user)`
- [x] 3.2 确保异步执行不阻塞主事务，邀请失败只记录日志
- [x] 3.3 添加单元测试：验证自动邀请触发条件、失败不阻塞主流程

## 4. Admin 手动邀请接口

- [x] 4.1 创建 `AdminGitHubOrgInvitationController`，提供单用户邀请接口 `POST /api/v1/admin/github-org-invitations/users/{userId}`
- [x] 4.2 提供批量邀请接口 `POST /api/v1/admin/github-org-invitations/batch`，接收 `userIds` 列表，返回统一格式（total/succeeded/failed/details）
- [x] 4.3 添加权限注解 `@RequiresPermission`，权限标识 `github-org-invitation:invite` 和 `github-org-invitation:invite-batch`
- [x] 4.4 创建对应的 DTO/Result 类，确保批量返回格式统一（每个结果包含 userId/success/reason）
- [x] 4.5 添加集成测试：验证单邀、批量邀请、权限控制、返回格式

## 5. Admin 前端页面

- [x] 5.1 创建 `admin/github-invitations` 页面路由和基础布局
- [x] 5.2 实现用户列表展示（支持卡片/表格视图切换），显示姓名、邮箱、GitHub 绑定状态、方向、角色
- [x] 5.3 实现单用户邀请按钮和结果反馈（成功/失败提示）
- [x] 5.4 实现批量选择、批量邀请按钮和结果汇总展示
- [x] 5.5 添加页面权限控制，仅管理员可访问
- [x] 5.6 创建对应的 API service 调用后端接口

## 6. 报名表单提示

- [x] 6.1 修改 `EnrollForm.tsx`，在邮箱字段 label 旁添加 Tooltip 组件
- [x] 6.2 编写提示文案：说明邮箱将用于考核通知和 GitHub 组织邀请，建议有 GitHub 账号的用户使用绑定邮箱
- [ ] 6.3 验证表单提交和提示显示正常

## 7. 测试与验证

- [x] 7.1 运行后端编译和全部测试
- [x] 7.2 运行前端构建
- [x] 7.3 验证 Issue 同步功能回归（确保配置结构调整未破坏现有功能）
- [x] 7.4 手动验证自动邀请流程（模拟考核通过）
- [x] 7.5 手动验证 Admin 页面邀请功能
- [x] 7.6 验证报名表单提示显示
