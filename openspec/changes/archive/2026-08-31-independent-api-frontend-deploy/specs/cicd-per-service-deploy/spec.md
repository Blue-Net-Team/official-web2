# cicd-per-service-deploy Specification Delta

## ADDED Requirements

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

## MODIFIED Requirements

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
