# cicd-per-service-deploy Delta Specification

## MODIFIED Requirements

### Requirement: 路径级 CI 过滤

CI 流水线 SHALL 只对本次推送实际变更的服务执行测试与镜像构建。仅变更文档或其他无关路径（`docs/**`、`**/*.md`、`.claude/**`、`scripts/hooks/**`）的推送 SHALL NOT 触发 CI 工作流运行。

服务与路径映射 MUST 至少覆盖：api（`src/backend/**`、`src/backend/Dockerfile`、`trigger/api`）、judge（`src/judge-service/**`、`src/judge-service/Dockerfile`、`trigger/judge`）、ai（`src/ai-service/**`、`src/ai-service/Dockerfile`、`trigger/ai`）、frontend（`src/frontend/**`、`src/frontend/Dockerfile`、`trigger/frontend`）、infra（`docker/docker-compose.yml`、`docker/.env*`、`trigger/infra`）。`.github/workflows/**` 与 `docker/docker-compose.yml` 的变更 MUST 触发全量服务 CI。

CI 镜像构建 job SHALL 使用服务目录作为构建上下文：api 使用 `context: src/backend` + `file: src/backend/Dockerfile`，judge 使用 `context: src/judge-service` + `file: src/judge-service/Dockerfile`，ai 使用 `context: src/ai-service` + `file: src/ai-service/Dockerfile`；frontend 镜像在 cd-frontend 工作流中构建，使用 `context: src/frontend` + `file: src/frontend/Dockerfile`。

#### Scenario: 仅后端代码变更
- **WHEN** 一次推送只修改了 `src/backend/**` 下文件
- **THEN** CI 只运行 api 服务的测试与镜像构建，其他服务的测试与构建 job 被跳过

#### Scenario: 仅修改 api 的 Dockerfile
- **WHEN** 一次推送只修改了 `src/backend/Dockerfile`
- **THEN** CI 触发 api 服务的测试与镜像构建（Dockerfile 变更视同该服务变更）

#### Scenario: 纯文档变更
- **WHEN** 一次推送只修改了 `docs/**` 或 `*.md` 文件
- **THEN** CI 工作流完全不启动

#### Scenario: 仅版本号变更
- **WHEN** 一次推送只修改了 `trigger/api` 文件内容（版本号）
- **THEN** CI 运行 api 服务的测试与镜像构建

#### Scenario: 后端与前端同时变更
- **WHEN** 一次推送同时修改了 `src/backend/**` 与 `src/frontend/**`
- **THEN** CI 运行 api 与 frontend 两个服务的测试，并构建 api 镜像（frontend 镜像除外，见前端构建需求）

#### Scenario: CI 镜像构建上下文
- **WHEN** CI 构建 api 服务镜像
- **THEN** 构建上下文为 `src/backend`（非仓库根目录），`COPY target/*.jar` 指令成功解析
