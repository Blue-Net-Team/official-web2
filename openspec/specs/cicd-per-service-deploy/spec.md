# cicd-per-service-deploy Specification

## Purpose
TBD - created by archiving change cicd-per-service-deploy. Update Purpose after archive.
## Requirements
### Requirement: 路径级 CI 过滤

CI 流水线 SHALL 只对本次推送实际变更的服务执行测试与镜像构建。仅变更文档或其他无关路径（`docs/**`、`**/*.md`、`.claude/**`、`scripts/hooks/**`）的推送 SHALL NOT 触发 CI 工作流运行。

服务与路径映射 MUST 至少覆盖：api（`src/backend/**`、`docker/api-service.Dockerfile`、`trigger/api`）、judge（`src/judge-service/**`、`docker/judge-service.Dockerfile`、`trigger/judge`）、ai（`src/ai-service/**`、`docker/ai-service.Dockerfile`、`trigger/ai`）、frontend（`src/frontend/**`、`docker/frontend.Dockerfile`、`trigger/frontend`）、infra（`docker/docker-compose.yml`、`docker/.env*`、`trigger/infra`）。`.github/workflows/**` 与 `docker/docker-compose.yml` 的变更 MUST 触发全量服务 CI。

#### Scenario: 仅后端代码变更
- **WHEN** 一次推送只修改了 `src/backend/**` 下文件
- **THEN** CI 只运行 api 服务的测试与镜像构建，其他服务的测试与构建 job 被跳过

#### Scenario: 纯文档变更
- **WHEN** 一次推送只修改了 `docs/**` 或 `*.md` 文件
- **THEN** CI 工作流完全不启动

#### Scenario: 仅版本号变更
- **WHEN** 一次推送只修改了 `trigger/api` 文件内容（版本号）
- **THEN** CI 运行 api 服务的测试与镜像构建

#### Scenario: 后端与前端同时变更
- **WHEN** 一次推送同时修改了 `src/backend/**` 与 `src/frontend/**`
- **THEN** CI 运行 api 与 frontend 两个服务的测试，并构建 api 镜像（frontend 镜像除外，见前端构建需求）

### Requirement: trigger 文件存储版本号并控制自动 CD

每个服务 SHALL 在仓库根 `trigger/` 目录拥有一个触发文件（`api`、`frontend`、`judge`、`ai`、`infra`），文件内容为语义化版本号（格式 `x.y.z`，如 `1.4.0`）。该文件存在 SHALL 表示该服务启用自动 CD；删除该文件 SHALL 表示关闭自动 CD。

自动 CD SHALL 仅在满足以下全部条件时执行该服务部署：
1. 该服务在本次推送的变更路径内；**且**
2. `trigger/<svc>` 文件存在；**且**
3. `trigger/<svc>` 文件内容在本次推送中发生变化（即版本号 bump，视为发布信号）。

仅变更服务代码而未变更 `trigger/<svc>` 版本号的推送，CI SHALL 照常构建镜像但 SHALL NOT 自动部署该服务。

`trigger/<svc>` 内容不符合 `x.y.z` 格式时，该服务 CI 构建 SHALL 失败。

#### Scenario: 版本号 bump 触发自动部署
- **WHEN** 本次推送修改了 `trigger/api` 内容（从 `1.4.0` 改为 `1.5.0`），且 `trigger/api` 文件存在
- **THEN** CI 构建 api 镜像并打 `1.5.0` tag，编排器自动触发 api 的 CD 部署该版本

#### Scenario: 仅改代码不部署
- **WHEN** 本次推送只修改了 `src/backend/**`，`trigger/api` 内容未变
- **THEN** CI 照常测试并构建 api 镜像，但编排器不自动部署 api

#### Scenario: 删除触发文件关闭 CD
- **WHEN** 运维删除 `trigger/frontend` 文件
- **THEN** 前端服务不再参与自动 CD

#### Scenario: 非法版本号构建失败
- **WHEN** `trigger/api` 内容为 `abc`（非 `x.y.z` 格式）
- **THEN** api 的 CI 构建 job 失败并提示版本号格式错误

### Requirement: CD 必须有对应 CI

自动 CD SHALL 仅在对应 CI 工作流成功完成后触发（`workflow_run` 且结论为 success）。手动 CD SHALL 仅部署 CI 已构建并推送至镜像仓库的镜像，不得在 CD 阶段从源码重新构建（前端除外，见前端构建需求）。

#### Scenario: CI 失败不部署
- **WHEN** CI 工作流某服务测试或构建失败
- **THEN** 对应服务的自动 CD 不被触发

#### Scenario: 手动部署依赖已构建镜像
- **WHEN** 运维手动触发某服务 CD，但该服务镜像从未被 CI 构建
- **THEN** CD 在部署前校验镜像存在，镜像不存在则 CD 失败

### Requirement: 手动 CD 部署最新镜像

`workflow_dispatch` 手动触发的 CD SHALL 部署浮动 tag 指向的最新镜像：生产环境为 `latest`，开发环境为 `develop`。手动 CD SHALL 不重新构建镜像，直接从镜像仓库拉取后部署（前端除外）。

手动 CD SHALL 支持指定单个服务（`target_service` 输入）或按 trigger 文件启用的所有服务。

#### Scenario: 手动部署 api 最新镜像
- **WHEN** 运维手动触发 CD 并选择 `api`
- **THEN** CD 拉取 `ghcr.io/<owner>/bluenet-api-service:latest` 并部署到对应环境

#### Scenario: 手动部署全部启用服务
- **WHEN** 运维手动触发 CD 且 `target_service=all`
- **THEN** CD 按依赖顺序部署所有存在 `trigger/<svc>` 文件的服务

### Requirement: 镜像 tag 版本管理

CI 构建产出的不可变镜像 tag SHALL 等于该服务 `trigger/<svc>` 文件中存储的版本号（如 `1.4.0`）。除此之外，每次构建 SHALL 同时产出浮动环境 tag（生产 `latest`、开发 `develop`）。

自动 CD SHALL 部署本次构建产出的版本号 tag（`trigger/<svc>` 中的版本），而非浮动 tag。

#### Scenario: 构建产出版本 tag 与浮动 tag
- **WHEN** `trigger/api` 内容为 `1.4.0`，CI 在 develop 分支构建 api 镜像
- **THEN** 镜像被标记为 `1.4.0`（不可变）与 `develop`（浮动），并推送至 ghcr

#### Scenario: 自动 CD 使用版本号 tag
- **WHEN** `trigger/api` 内容为 `1.5.0`，且本次为发布推送
- **THEN** 自动 CD 部署 `ghcr.io/<owner>/bluenet-api-service:1.5.0`

#### Scenario: 回滚到历史版本
- **WHEN** 运维需要回滚 api 到历史发布版本 `1.4.0`
- **THEN** 可通过指定该服务的 `1.4.0` 版本 tag 部署

### Requirement: 每服务独立 CD 流水线与依赖

每个服务 SHALL 拥有独立的 CD 工作流文件（`cd-api.yml`、`cd-frontend.yml`、`cd-judge.yml`、`cd-ai.yml`、`cd-infra.yml`），由编排器 `cd-deploy.yml` 统一调度。依赖关系 MUST 满足：api 依赖基础设施；judge/ai 依赖基础设施；前端 CD MUST 在 api 部署完成且后端健康后才进行镜像构建与部署。

api-service 与 frontend SHALL 支持相互独立的部署主机与部署路径，不再强制共享 `DEPLOY_*` Secrets。

#### Scenario: 只改后端只部署后端
- **WHEN** 本次推送仅变更后端且 `trigger/api` 版本 bump，其他服务未变更或 trigger 文件被删除
- **THEN** 编排器只执行 api 的 CD，其他服务 CD 不运行

#### Scenario: 前端部署等待后端健康
- **WHEN** 本次推送同时变更前后端且两者 trigger 均有效
- **THEN** 前端 CD 等待 api CD 完成后、轮询 `/api/v1/health` 返回健康，才构建并部署前端

#### Scenario: api 与 frontend 部署到不同主机
- **WHEN** GitHub Secrets 中 `API_DEPLOY_HOST_DEV` 与 `FRONTEND_DEPLOY_HOST_DEV` 配置为不同主机
- **THEN** api-service 与 frontend 分别部署到各自指定的主机

### Requirement: api-service 独立部署目标

api-service CD 工作流 SHALL 优先使用 `API_DEPLOY_HOST_PROD/DEV`、`API_DEPLOY_PATH_PROD/DEV`、`API_DEPLOY_USER`、`API_DEPLOY_KEY`、`API_DEPLOY_PORT` Secrets 作为部署目标。当上述任一 Secret 未配置时，SHALL 回退到对应的全局 `DEPLOY_HOST_*`、`DEPLOY_PATH_*`、`DEPLOY_USER`、`DEPLOY_KEY`、`DEPLOY_PORT` Secrets。

api-service 部署命令 SHALL 使用 `--profile api` 启动，确保仅启动 api-service 自身，不启动 frontend 或 ai-service。

#### Scenario: 未配置 API_DEPLOY_* 时回退到全局 DEPLOY_*
- **WHEN** GitHub Secrets 中未配置 `API_DEPLOY_HOST_DEV`
- **THEN** cd-api.yml 使用 `DEPLOY_HOST_DEV` 作为部署目标

#### Scenario: 配置 API_DEPLOY_* 后独立部署
- **WHEN** GitHub Secrets 中配置了 `API_DEPLOY_HOST_DEV` 且与 `DEPLOY_HOST_DEV` 不同
- **THEN** api-service 被部署到 `API_DEPLOY_HOST_DEV` 指定的主机

#### Scenario: api-service 使用独立 profile 启动
- **WHEN** cd-api.yml 执行部署
- **THEN** 在目标主机执行 `docker compose --profile api up -d api-service --remove-orphans`

### Requirement: frontend 独立部署目标

frontend CD 工作流 SHALL 优先使用 `FRONTEND_DEPLOY_HOST_PROD/DEV`、`FRONTEND_DEPLOY_PATH_PROD/DEV`、`FRONTEND_DEPLOY_USER`、`FRONTEND_DEPLOY_KEY`、`FRONTEND_DEPLOY_PORT` Secrets 作为部署目标。当上述任一 Secret 未配置时，SHALL 回退到对应的全局 `DEPLOY_*` Secrets。

frontend 部署命令 SHALL 使用 `--profile frontend` 并携带 `--no-deps`，确保跨主机部署时不会因本地缺少 api-service 容器而失败。

#### Scenario: 未配置 FRONTEND_DEPLOY_* 时回退到全局 DEPLOY_*
- **WHEN** GitHub Secrets 中未配置 `FRONTEND_DEPLOY_HOST_DEV`
- **THEN** cd-frontend.yml 使用 `DEPLOY_HOST_DEV` 作为部署目标

#### Scenario: 配置 FRONTEND_DEPLOY_* 后独立部署
- **WHEN** GitHub Secrets 中配置了 `FRONTEND_DEPLOY_HOST_DEV` 且与 `DEPLOY_HOST_DEV` 不同
- **THEN** frontend 被部署到 `FRONTEND_DEPLOY_HOST_DEV` 指定的主机

#### Scenario: frontend 部署跳过本地依赖检查
- **WHEN** cd-frontend.yml 执行部署
- **THEN** 在目标主机执行 `docker compose --profile frontend up -d --no-deps frontend --remove-orphans`

### Requirement: 统一服务级 Secrets 回退规则

所有服务的 CD 工作流（`cd-api.yml`、`cd-frontend.yml`、`cd-judge.yml`、`cd-ai.yml`、`cd-infra.yml`）SHALL 采用统一的 Secrets 回退规则：优先使用 `<SERVICE>_DEPLOY_*`，未配置时回退到全局 `DEPLOY_*`。

回退规则 SHALL 适用于以下字段：`HOST`、`PATH`、`USER`、`KEY`、`PORT`。

#### Scenario: judge-service 未配置 JUDGE_DEPLOY_* 时回退
- **WHEN** GitHub Secrets 中未配置 `JUDGE_DEPLOY_HOST_DEV`
- **THEN** cd-judge.yml 使用 `DEPLOY_HOST_DEV` 作为部署目标

#### Scenario: ai-service 未配置 AI_DEPLOY_* 时回退
- **WHEN** GitHub Secrets 中未配置 `AI_DEPLOY_HOST_DEV`
- **THEN** cd-ai.yml 使用 `DEPLOY_HOST_DEV` 作为部署目标

### Requirement: ai-service 使用独立 profile

ai-service CD 工作流 SHALL 使用 `--profile ai` 并携带 `--no-deps` 启动，与 judge-service 的独立部署模式保持一致。

#### Scenario: ai-service 使用独立 profile 启动
- **WHEN** cd-ai.yml 执行部署
- **THEN** 在目标主机执行 `docker compose --profile ai up -d --no-deps ai-service --remove-orphans`

### Requirement: 前端镜像在 CD 阶段构建

前端镜像 SHALL NOT 在每次 CI 中构建。前端镜像 MUST 在 CD 阶段、后端健康检查通过之后才构建，以保证 SSR 构建期预渲染访问的是新版本后端。

#### Scenario: 前端发布推送
- **WHEN** 本次推送修改 `trigger/frontend` 版本号（发布信号）
- **THEN** CI 仅运行前端测试；CD 阶段在确认后端健康后构建前端镜像并部署

#### Scenario: 仅改前端代码不部署
- **WHEN** 本次推送只修改 `src/frontend/**`，`trigger/frontend` 版本未变
- **THEN** CI 仅运行前端测试，不构建前端镜像，也不自动部署

#### Scenario: 手动触发前端 CD
- **WHEN** 运维手动触发前端 CD
- **THEN** CD 先验证后端健康，再构建前端镜像（推 ghcr 供后续复用）并部署

