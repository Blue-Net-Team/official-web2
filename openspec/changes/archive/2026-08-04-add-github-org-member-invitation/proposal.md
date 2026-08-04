# Proposal: GitHub 组织成员邀请

## Why

考生通过最终考核后，需要自动加入 Blue-Net-Team GitHub 组织及对应方向的团队，以便及时获得项目协作权限。同时需要为管理员提供手动邀请和批量邀请的能力，处理自动邀请失败或特殊情况。

## What Changes

- **新增 GitHub Org App 配置**：创建独立的 GitHub App 用于组织成员邀请，配置 `Organization members: Read and write` 权限，安装到 `Blue-Net-Team` 组织
- **新增组织邀请服务**：封装 GitHub 组织邀请 API（`POST /orgs/{org}/invitations`），支持按 `githubId`（优先）或 `email` 邀请
- **自动邀请触发**：在 `AssessmentDecisionPublicationService` 中，当考生通过全局最终考核并升级为 MEMBER 时，异步发送 GitHub 组织邀请
- **方向团队映射**：根据用户 `direction` 字段，自动加入对应 GitHub Team（Computer Vision / Embedded control / Structure and Analysis）
- **Admin 手动邀请页面**：新建独立页面 `admin/github-invitations`，支持卡片/表格视图、单用户邀请、批量邀请
- **批量邀请接口**：`POST /api/v1/admin/github-org-invitations/batch`，返回统一格式（`userId`/`success`/`reason`）
- **报名邮箱提示**：在报名表单邮箱字段旁添加悬浮提示，说明该邮箱将用于 GitHub 组织邀请

## Capabilities

### New Capabilities

- `github-org-invitation`: GitHub 组织成员邀请的核心服务，包括配置管理、邀请发送、方向团队映射、自动/手动触发逻辑

### Modified Capabilities

- `assessment-decision-publish`: 在发布决策结果时，新增触发 GitHub 组织邀请的逻辑（当考生升级为 MEMBER 时）
- `backend-enrollment`: 在报名表单中，邮箱字段旁添加悬浮提示说明

## Impact

- **后端**：
  - 新增配置：`application.yml` 中 `github.org` 配置块
  - 新增服务：`GitHubOrgInvitationService`（配置、Token 获取、邀请发送）
  - 修改服务：`AssessmentDecisionPublicationService` 增加异步邀请触发
  - 新增 Controller：`AdminGitHubOrgInvitationController` 提供手动邀请接口
  - 权限标识：`github-org-invitation:invite`（手动邀请）、`github-org-invitation:invite-batch`（批量邀请）

- **前端**：
  - 新增页面：`admin/github-invitations`（用户列表 + 邀请操作）
  - 修改页面：`EnrollForm.tsx` 邮箱字段添加 Tooltip 提示

- **外部依赖**：
  - 需创建新的 GitHub App（Organization members: Read and write）
  - 环境变量：`GITHUB_ORG_APP_ID`、`GITHUB_ORG_PRIVATE_KEY_PATH`

- **数据库**：无 schema 变更

- **部署**：需在 GitHub 上创建 App 并安装到 `Blue-Net-Team` 组织，配置私钥文件路径
