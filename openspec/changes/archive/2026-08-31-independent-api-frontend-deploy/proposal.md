# independent-api-frontend-deploy

## Why

当前 CI/CD 流水线中，judge 和 ai 服务已支持独立服务器与独立路径部署，但 api-service 和 frontend 仍共用同一组 `DEPLOY_HOST_*` / `DEPLOY_PATH_*` Secrets，且都绑定到同一个 `app` profile。这导致前后端无法物理分离部署，限制了未来独立扩容、迁移或安全隔离的灵活性。

## What Changes

- 新增 `API_DEPLOY_HOST_PROD/DEV`、`API_DEPLOY_PATH_PROD/DEV` Secrets，供 api-service 独立部署；未配置时回退到现有 `DEPLOY_*`，保证兼容。
- 新增 `FRONTEND_DEPLOY_HOST_PROD/DEV`、`FRONTEND_DEPLOY_PATH_PROD/DEV` Secrets，供 frontend 独立部署；未配置时回退到现有 `DEPLOY_*`。
- 将 `docker-compose.yml` 中的 `app` profile 拆分为更细粒度的 `api`、`frontend`、`ai` profile，同时保留 `app` 用于本地开发一键启动。
- `cd-api.yml` 改用 `API_DEPLOY_*` secrets（回退 `DEPLOY_*`），部署 profile 改为 `api`。
- `cd-frontend.yml` 改用 `FRONTEND_DEPLOY_*` secrets（回退 `DEPLOY_*`），部署 profile 改为 `frontend`，并增加 `--no-deps` 跳过本地容器依赖检查。
- `cd-ai.yml` 部署 profile 从 `app` 改为 `ai`，并增加 `--no-deps`。
- 统一所有服务 `cd-*.yml` 的 Secrets 回退规则：优先使用 `<SVC>_DEPLOY_*`，未配置则回退到 `DEPLOY_*`。
- 更新 `docs/04-运维部署/04-03-CI-CD自动部署.md` 与 `04-04-多服务器部署.md`，记录新的 Secrets 模型与部署拓扑。

## Capabilities

### New Capabilities
- 无

### Modified Capabilities
- `cicd-per-service-deploy`: 扩展独立部署能力到 api-service 与 frontend，统一 Secrets 回退规则，调整 compose profile 划分与 CD 部署命令。

## Impact

- **代码**：`docker/docker-compose.yml`、`.github/workflows/cd-api.yml`、`.github/workflows/cd-frontend.yml`、`.github/workflows/cd-ai.yml`、`cd-judge.yml`、`cd-infra.yml`（如需统一回退）。
- **配置**：GitHub Secrets 可选新增 `API_DEPLOY_*` 与 `FRONTEND_DEPLOY_*`；不配置时行为与现状一致。
- **文档**：`docs/04-运维部署/04-03-CI-CD自动部署.md`、`04-04-多服务器部署.md`。
- **本地开发**：`app` / `full` profile 保留，无影响。
- **生产部署**：默认行为不变（回退到 `DEPLOY_*`）；配置新 Secrets 后可实现同机分离目录或跨主机部署。
