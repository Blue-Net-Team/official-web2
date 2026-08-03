# Design: GitHub 组织成员邀请

## Context

BlueNet 团队通过考核系统选拔新成员。目前考生通过最终考核后，系统会自动将其角色从 CANDIDATE 升级为 MEMBER，但加入 GitHub 组织（Blue-Net-Team）仍需管理员手动操作。

系统已有一个 GitHub App 用于 Issue 同步（仓库级权限），以及 GitHub OAuth 用于用户绑定（用户级授权）。组织成员邀请需要组织级权限，且与现有 Issue 同步的关注点不同，因此需要独立的 GitHub App。

## Goals / Non-Goals

**Goals:**
- 考生通过全局最终考核后自动收到 GitHub 组织邀请
- 根据用户方向自动分配到对应 GitHub Team
- 管理员可手动邀请单个用户或批量邀请
- 邀请失败只记录日志，不影响主流程（角色升级、邮件通知）
- 支持按 `githubId`（优先）或 `email` 邀请

**Non-Goals:**
- 自动接受邀请（用户需自行点击邮件中的邀请链接）
- 邀请状态跟踪与重试机制（当前版本只记录日志）
- 数据库审计表（手动邀请走 HTTP 接口，已有 AuditAspect 自动审计）
- 从 GitHub 组织移除成员（超出当前需求范围）

## Decisions

### 1. 新建独立 GitHub App，不复用 Issue 同步 App

**决策**：创建新的 GitHub App 专门用于组织成员邀请。

**理由**：
- 权限隔离：Issue 同步只需 `Issues: Read/Write`，组织邀请需要 `Organization members: Read/Write`
- 安装范围：Issue 同步安装在仓库，组织邀请必须安装在组织
- 关注点分离：Issue 同步面向外部 Bug 报告，组织邀请面向内部成员管理
- 故障隔离：任一 App 出问题不影响另一个

**替代方案**：复用现有 App 并扩展权限 —— 被拒绝，违反最小权限原则，配置语义混乱。

### 2. 多 GitHub App 配置管理：统一配置中心

**决策**：采用 `github.apps.{name}.*` 统一配置结构，替代分散的 `github.app.*` 和 `github.org.*`。

**配置结构**：
```yaml
github:
  apps:
    issue-sync:
      app-id: ${GITHUB_ISSUE_APP_ID:}
      private-key-path: ${GITHUB_ISSUE_PRIVATE_KEY_PATH:}
      type: repository
      owner: Blue-Net-Team
      repo: bluenet-web
      enabled: true
    org-invitation:
      app-id: ${GITHUB_ORG_APP_ID:}
      private-key-path: ${GITHUB_ORG_PRIVATE_KEY_PATH:}
      type: organization
      org: Blue-Net-Team
      enabled: true
      team-mapping:
        COMPUTER_VISION: "Computer Vision"
        EMBEDDED: "Embedded control"
        STRUCTURAL_DESIGN: "Structure and Analysis"
```

**理由**：
- 配置集中：所有 GitHub App 配置在一处，运维一目了然
- 结构统一：每个 App 都有 `app-id`、`private-key-path`、`type`、`enabled` 等标准字段
- 易于扩展：未来新增 App 只需加配置段，无需改代码结构
- 环境变量语义化：`GITHUB_ISSUE_APP_ID` / `GITHUB_ORG_APP_ID` 比 `GITHUB_APP_ID` 更清晰

**迁移成本**：现有 `github.app.*` 需要迁移到 `github.apps.issue-sync.*`，这是一次性工作，长期收益大于成本。

**替代方案**：
- 方案 A（完全独立配置）：`github.app.*` + `github.org.*` —— 被拒绝，配置分散，扩展时需新增前缀
- 方案 B（抽象共享但配置分离）：共享代码但配置仍分散 —— 被拒绝，运维体验不佳

### 3. 共享基础设施：JWT 生成与 Token 获取

**决策**：抽取共享的 JWT 生成和 Installation Token 获取逻辑，各 App 通过配置名称获取 Token。

**核心组件**：
```java
// 统一配置
@ConfigurationProperties(prefix = "github")
public class GitHubAppsProperties {
    private Map<String, GitHubAppConfig> apps;
}

// 共享 Token 服务
@Service
public class GitHubAppTokenService {
    public String getAccessToken(String appName) {
        GitHubAppConfig config = properties.getApp(appName);
        String installationUrl = switch (config.getType()) {
            case REPOSITORY -> String.format("%s/repos/%s/%s/installation", ...);
            case ORGANIZATION -> String.format("%s/orgs/%s/installation", ...);
        };
        // 通用 JWT + Token 获取逻辑
    }
}
```

**理由**：
- JWT 生成、私钥加载逻辑完全相同，只写一次
- 通过 `type` 字段区分 installation URL 策略（repository vs organization）
- 新增 App 类型只需扩展 switch 分支

### 4. 邀请身份策略：githubId 优先，email 兜底

**决策**：优先使用 `User.githubId` 作为 `invitee_id`，无绑定时使用 `email`。

**理由**：
- GitHub 邀请 API 不接受用户名（`login`），只接受 `invitee_id`（数字 ID）或 `email`
- OAuth 绑定时已存储 `githubId`，无需额外查询
- `githubId` 不受邮箱变更影响，更可靠
- 未绑定用户自动 fallback 到邮箱，覆盖所有场景

**替代方案**：只用邮箱邀请 —— 被拒绝，邮箱可能未绑定 GitHub 账号导致邀请失效。

### 5. 方向到 GitHub Team 的映射：配置化动态解析

**决策**：在 `application.yml` 中配置 `github.apps.org-invitation.team-mapping`，启动时通过 `GET /orgs/{org}/teams` 解析 team name 到 team ID。

**理由**：
- 后端方向枚举（`COMPUTER_VISION`/`EMBEDDED`/`STRUCTURAL_DESIGN`）与 GitHub Team 名称（`Computer Vision`/`Embedded control`/`Structure and Analysis`）不一致，不能硬编码
- Team 重建后 ID 会变，用 name 匹配更稳定
- 配置化便于调整映射关系

**替代方案**：直接配置 team ID —— 被拒绝，team 重建后需手动更新配置。

### 6. 自动邀请触发点：AssessmentDecisionPublicationService

**决策**：在 `publish()` 方法中，角色升级成功后异步触发 GitHub 邀请。

**理由**：
- 该位置是「全局最终考核通过 → CANDIDATE → MEMBER」的唯一触发点
- 异步执行，不阻塞主事务
- 邀请失败只记录日志，不影响角色升级和邮件通知

### 7. 手动邀请：独立 Admin 页面

**决策**：新建 `admin/github-invitations` 页面，支持卡片/表格视图、单邀、批量邀请。

**理由**：
- 职责单一：专门处理 GitHub 组织邀请，不混入通用用户管理
- 未来可扩展：可添加邀请状态、重试、历史记录等功能
- 与现有 `admin/users` 页面解耦

### 8. 批量邀请 API 设计：统一返回格式

**决策**：`POST /api/v1/admin/github-org-invitations/batch` 返回统一结构：

```json
{
  "total": 3,
  "succeeded": 2,
  "failed": 1,
  "details": [
    { "userId": 1, "success": true, "reason": "已通过 GitHub ID 邀请并分配至 Computer Vision 团队" },
    { "userId": 2, "success": false, "reason": "未绑定 GitHub 且无邮箱，无法邀请" }
  ]
}
```

**理由**：
- 避免多态类型（不混用 `invitee`/`reason` 字段）
- 前端处理简单统一

### 9. 审计策略：自动邀请不审计，手动邀请自动审计

**决策**：
- 自动邀请在 Service 层触发，不走 Controller，AuditAspect 不记录
- 手动邀请走 Admin Controller，带 `@RequiresPermission`，AuditAspect 自动审计

**理由**：符合用户要求，且利用现有审计基础设施，无需额外开发。

## Risks / Trade-offs

| 风险 | 缓解措施 |
|------|---------|
| 用户邮箱未绑定 GitHub 账号，邀请邮件无法送达 | 报名表单添加 Tooltip 提示；邀请失败记录日志；Admin 页面支持重试 |
| GitHub API 限流（批量邀请时） | 控制批量大小（建议 ≤50）；异步发送；失败记录日志 |
| Team 名称变更导致映射失效 | 启动时解析失败记录日志；邀请时 team 不存在则只发组织邀请不分配 team |
| 私钥文件泄露 | 私钥路径通过环境变量配置；文件权限控制；不提交到版本库 |
| 重复邀请（用户已在组织中） | GitHub API 会返回 422，捕获后记录日志，不视为错误 |

## Migration Plan

### 阶段 1：配置结构调整（向后兼容）

1. 创建 `GitHubAppsProperties` 统一配置类，支持 `github.apps.{name}.*` 结构
2. 保留现有 `GitHubAppProperties` 作为过渡，内部委托给新配置
3. 创建共享的 `GitHubJwtGenerator` 和重构 `GitHubAppTokenService`
4. 修改 `GitHubIssueClient` 使用新的 Token 服务（传入 `appName = "issue-sync"`）

### 阶段 2：组织邀请功能

5. 在 GitHub 上创建新 App，配置 `Organization members: Read and write` 权限
6. 安装 App 到 `Blue-Net-Team` 组织
7. 下载私钥文件，配置到服务器
8. 配置环境变量：`GITHUB_ORG_APP_ID`、`GITHUB_ORG_PRIVATE_KEY_PATH`
9. 配置 `application.yml` 中的 `github.apps.org-invitation` 块（含 team-mapping）
10. 实现组织邀请相关服务和接口

### 阶段 3：部署与验证

11. 部署后端服务
12. 验证 Issue 同步功能正常（回归测试）
13. 验证组织邀请功能正常
14. 部署前端服务
15. 验证 Admin 页面和报名表单

## Open Questions

1. 批量邀请的最大数量限制？（建议 50，与现有批量操作一致）
2. 是否需要记录邀请历史到数据库？（当前版本只记录日志）
3. 考生接受邀请后，是否需要同步更新系统状态？（当前版本不做）
