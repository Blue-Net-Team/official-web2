## ADDED Requirements

### Requirement: GitHub OAuth2 授权流程

系统 SHALL 提供 GitHub OAuth2 授权码流程，允许用户通过 GitHub 账号登录。

#### Scenario: 发起 GitHub 授权
- **WHEN** 用户点击 GitHub 登录按钮
- **THEN** 前端 SHALL 调用 `GET /api/v1/auth/github` 获取授权 URL
- **AND** 前端 SHALL 将用户重定向到该 URL

#### Scenario: GitHub 授权回调（已绑定用户）
- **WHEN** GitHub 回调 `GET /api/v1/auth/github/callback?code=xxx&state=yyy`
- **AND** state 参数验证通过
- **AND** 通过 GitHub access_token 获取的 githubId 在 tb_user 中找到匹配记录
- **THEN** 系统 SHALL 为该用户设置 JWT Cookie 和 CSRF Cookie
- **AND** 系统 SHALL 302 重定向到前端 `/login?github=success`

#### Scenario: GitHub 授权回调（未绑定用户）
- **WHEN** GitHub 回调且 githubId 未匹配到任何用户
- **THEN** 系统 SHALL 302 重定向到前端 `/login?github=unbound`
- **AND** 前端 SHALL 提示用户"请先使用学号登录，然后在个人设置中绑定 GitHub 账号"

#### Scenario: state 参数校验失败
- **WHEN** 回调中的 state 参数无效或已过期
- **THEN** 系统 SHALL 返回 401 错误
- **AND** 系统 SHALL 重定向到前端 `/login?github=error`

#### Scenario: GitHub API 调用失败
- **WHEN** 使用 code 换取 access_token 失败，或获取用户信息失败
- **THEN** 系统 SHALL 重定向到前端 `/login?github=error`
- **AND** 系统 SHALL 记录错误日志

### Requirement: GitHub 账号绑定

已登录用户 SHALL 能够在个人设置中绑定 GitHub 账号。

#### Scenario: 发起绑定
- **WHEN** 已登录用户点击"绑定 GitHub"按钮
- **THEN** 前端 SHALL 调用 `GET /api/v1/auth/github/bind` 获取绑定授权 URL
- **AND** 前端 SHALL 将用户重定向到该 URL

#### Scenario: 绑定回调成功
- **WHEN** GitHub 回调 `GET /api/v1/auth/github/bind/callback?code=xxx&state=yyy`
- **AND** state 参数验证通过
- **AND** 当前用户未绑定 GitHub
- **AND** 获取到的 githubId 未被其他用户绑定
- **THEN** 系统 SHALL 将 githubId 和 githubUsername 写入当前用户记录
- **AND** 系统 SHALL 302 重定向到前端 `/profile?github=binding_success`

#### Scenario: 绑定回调失败 - GitHub 账号已被其他用户绑定
- **WHEN** 获取到的 githubId 已被其他用户绑定
- **THEN** 系统 SHALL 302 重定向到前端 `/profile?github=already_bound`
- **AND** 前端 SHALL 提示"该 GitHub 账号已被其他用户绑定"

#### Scenario: 绑定回调失败 - 用户已绑定其他 GitHub
- **WHEN** 当前用户已绑定 GitHub 账号
- **THEN** 系统 SHALL 302 重定向到前端 `/profile?github=already_bound`

#### Scenario: 查询绑定状态
- **WHEN** 前端调用 `GET /api/v1/auth/github/status`
- **THEN** 系统 SHALL 返回当前用户的 GitHub 绑定状态（已绑定返回 githubUsername，未绑定返回 null）

### Requirement: GitHub 账号解绑

已绑定 GitHub 的用户 SHALL 能够解绑。

#### Scenario: 解绑成功
- **WHEN** 已登录用户调用 `DELETE /api/v1/auth/github/bind`
- **AND** 当前用户已绑定 GitHub
- **THEN** 系统 SHALL 清除该用户的 githubId 和 githubUsername
- **AND** 系统 SHALL 返回成功响应

#### Scenario: 解绑失败 - 未绑定
- **WHEN** 用户调用解绑接口但未绑定 GitHub
- **THEN** 系统 SHALL 返回错误提示"未绑定 GitHub 账号"

### Requirement: GitHub 登录时更新用户名

系统 SHALL 在每次 GitHub 登录时更新 githubUsername 字段。

#### Scenario: GitHub 用户名变更
- **WHEN** 用户通过 GitHub 登录成功
- **AND** GitHub 返回的用户名与数据库中的 githubUsername 不同
- **THEN** 系统 SHALL 更新 githubUsername 为最新值

### Requirement: state 参数管理

系统 SHALL 使用 state 参数防止 OAuth CSRF 攻击。

#### Scenario: 生成 state
- **WHEN** 发起 GitHub 授权（登录或绑定）
- **THEN** 系统 SHALL 生成随机 UUID 作为 state
- **AND** 系统 SHALL 将 state 存入 Redis，TTL 为 10 分钟
- **AND** 系统 SHALL 将 state 作为参数拼接到 GitHub 授权 URL

#### Scenario: 验证 state
- **WHEN** 收到 GitHub 回调
- **THEN** 系统 SHALL 从 Redis 取出 state 并验证
- **AND** 验证后 SHALL 立即删除该 state（一次性使用）
- **AND** 如果 state 不存在或不匹配 SHALL 拒绝请求
