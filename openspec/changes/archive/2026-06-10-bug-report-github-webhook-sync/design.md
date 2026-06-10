## Context

当前系统已实现 Bug 报告的单向同步：用户在前端提交 Bug → 平台保存到数据库 → `@Async` 异步调用 GitHub API 创建 Issue。该流程使用 GitHub App（JWT + Installation Access Token）认证，同步失败不影响用户提交体验。

但此架构存在两个缺口：
1. **状态不同步**：GitHub Issue 被分配、关闭、重新打开时，平台侧 Bug 状态仍是手动维护，容易与 GitHub 实际状态脱节
2. **信息孤岛**：开发者在 GitHub 上直接创建的 Issue（如通过 GitHub CLI、手机端、或其他仓库操作）不会出现在平台管理页中

项目已有 GitHub REST API 调用先例（`GitHubIssueClient`、`GitHubAppTokenService`），使用 `RestTemplate` 风格。本次新增 Webhook 接收能力，保持技术栈一致性。

## Goals / Non-Goals

**Goals:**
- 接收并验证 GitHub `issues` 事件的 Webhook
- 根据 GitHub Issue 事件自动更新平台 Bug 报告状态
- GitHub 直接创建的 Issue 自动反向同步到平台
- 平台创建的 Issue 能被 Webhook 识别并跳过（避免重复处理）
- Webhook 端点具备签名验证，防止伪造请求

**Non-Goals:**
- 支持 GitHub 其他事件类型（`pull_request`、`comment` 等）
- 将平台状态变更同步到 GitHub（如平台标记 RESOLVED 时关闭 GitHub Issue）
- 自定义 Webhook 处理规则或用户可配置映射
- 批量历史 Issue 同步（仅处理 Webhook 收到的新事件）
- 复杂冲突解决策略（如同时收到多个事件时的竞争条件处理——依赖数据库乐观锁/事务隔离）

## Decisions

### Webhook 路径设计为公开端点，无需登录认证
- **选择**: `POST /api/v1/github/webhook` 不使用 `@RequiresPermission`，由独立的 HMAC-SHA256 签名验证保护
- **理由**: Webhook 由 GitHub 服务器调用，无用户会话；签名验证是行业标准做法（GitHub 官方推荐）
- **替代方案**: 使用 API Token 认证 — 不符合 GitHub Webhook 的调用模式

### 使用 HMAC-SHA256 签名验证 Webhook
- **选择**: 读取请求 Body，用 `X-Hub-Signature-256` header 中的签名验证
- **理由**: GitHub 原生支持，配置简单，安全性足够；与 GitHub App 已有的私钥机制形成双层安全
- **替代方案**: IP 白名单 — GitHub IP 段可能变化，维护成本高；Token 参数 — 签名更安全

### 通过 Issue Body 中的隐藏 HTML 注释区分 Issue 来源
- **选择**: 平台创建 Issue 时在 body 末尾追加 `\n\n<!-- bluenet-bug-report -->`
- **理由**: 零侵入（Markdown 渲染时完全不可见），无需额外数据库字段或 GitHub API 查询；Webhook payload 直接包含 body，解析无额外网络开销
- **替代方案**: GitHub Issue label 标记 — 用户可手动移除；数据库维护映射表 — 需额外查询，且平台创建时 issue number 还未知（但 API 响应后立即已知，可行但复杂）

### 反向同步的 Bug 报告使用现有字段，不新增来源标记字段
- **选择**: 反向同步的 BugReport 复用 `tb_bug_report` 现有结构，`pageUrl`/`environmentJson`/`reporterEmail` 为空
- **理由**: 最小化数据库变更；管理端可通过 `reporterEmail == null && pageUrl == null` 间接识别来源（可选），无需新增字段
- **替代方案**: 新增 `source` 枚举字段 — 更明确，但需 Flyway 迁移

### 状态映射采用无条件覆盖策略
- **选择**: Webhook 事件触发的状态更新无条件覆盖当前状态
- **理由**: GitHub 是 Issue 的权威数据源，其状态变化应被平台无条件接受；手动更新接口保留作为兜底，但 webhook 更新优先级更高
- **替代方案**: 只允许"向前"流转 — 更保守，但 reopened 等场景需要特殊处理，增加复杂度

### Webhook 处理失败不抛异常给 GitHub
- **选择**: 无论业务处理结果如何，均返回 HTTP 200（签名验证失败返回 401）
- **理由**: GitHub 在收到非 2xx 时会重试 Webhook，如果业务异常（如 BugReport 不存在）返回错误，会导致 GitHub 无意义重试；错误应记录日志而非影响 GitHub
- **替代方案**: 业务错误返回 422 — 导致 GitHub 重试，增加噪声

## Risks / Trade-offs

| 风险 | 缓解措施 |
|------|---------|
| Webhook Secret 泄露 | 通过 `.env` 文件注入，不提交到仓库；生产环境使用 Secret 管理；泄露后可立即更换 Secret |
| 平台创建 Issue 后 GitHub 发送 `opened` webhook，与反向同步逻辑冲突 | 通过 body 中的隐藏注释标记区分，平台创建的 Issue 会被识别并跳过 |
| 多次 reopen/close 导致状态频繁跳动 | 无条件接受 GitHub 状态，符合"GitHub 为权威源"的设计；管理端可查看状态历史（如需要后续可添加状态变更日志） |
| Webhook 接收与平台手动更新状态并发冲突 | 数据库行级锁（事务隔离）保证一致性；冲突概率极低（Bug 报告操作频率很低） |
| 反向同步的 Bug 报告缺少环境信息和截图 | 在管理端显示时自然展示为空；这是预期行为，因为信息本就不存在于 GitHub Issue |
| GitHub Webhook 投递延迟 | GitHub Webhook 通常秒级投递，可接受；如需实时性可考虑轮询，但当前场景无需 |

## Migration Plan

1. **配置准备**：
   - 在 `docker/.env` 和服务器配置中新增 `GITHUB_APP_WEBHOOK_SECRET`
   - 在 GitHub App 设置中配置 Webhook URL 为 `https://<domain>/api/v1/github/webhook`
   - 在 GitHub App 设置中启用 `Issues` 事件的订阅

2. **部署代码**：
   - 部署后端代码（包含新增 Controller/Service/Verifier）
   - 重启服务

3. **验证**：
   - 在 GitHub 上创建测试 Issue，验证是否反向同步到平台
   - 关闭/重新打开/分配 Issue，验证平台状态是否自动更新
   - 在平台提交新 Bug，验证是否仍能正常同步到 GitHub，且 webhook 不会重复创建

4. **回滚**：
   - 移除 GitHub App 中的 Webhook URL 配置即可立即停止事件推送
   - 代码回滚不影响已有数据

## Open Questions

- 是否需要记录状态变更历史（谁在什么时间把状态从 X 改为 Y）？→ 当前不实现，后续如需要可扩展
- 管理端是否需要显示"来自 GitHub"的标记？→ 当前不实现，反向同步的 Bug 报告自然显示（缺少 reporterEmail 等字段）
