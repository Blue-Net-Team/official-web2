# Tasks: independent-api-frontend-deploy

## 1. 更新 docker-compose.yml 的 profiles

- [x] 1.1 将 `api-service` 的 profiles 从 `[full, app]` 改为 `[full, app, api]`
- [x] 1.2 将 `frontend` 的 profiles 从 `[full, app]` 改为 `[full, app, frontend]`
- [x] 1.3 将 `ai-service` 的 profiles 从 `[full, app]` 改为 `[full, app, ai]`

## 2. 更新 cd-api.yml

- [x] 2.1 在 `Resolve deployment variables` 步骤中，将 `deploy_host` 改为 `secrets.API_DEPLOY_HOST_PROD || secrets.DEPLOY_HOST_PROD`（dev 同理）
- [x] 2.2 将 `deploy_path` 改为 `secrets.API_DEPLOY_PATH_PROD || secrets.DEPLOY_PATH_PROD`（dev 同理）
- [x] 2.3 将 `deploy_user` 改为 `secrets.API_DEPLOY_USER || secrets.DEPLOY_USER`
- [x] 2.4 将 `deploy_port` 改为 `secrets.API_DEPLOY_PORT || secrets.DEPLOY_PORT || '22'`
- [x] 2.5 将 SSH `Deploy api-service via SSH` 步骤中的 `docker compose --profile app up -d api-service --remove-orphans` 改为 `docker compose --profile api up -d api-service --remove-orphans`

## 3. 更新 cd-frontend.yml

- [x] 3.1 在 `Resolve deployment variables` 步骤中，将 `deploy_host` 改为 `secrets.FRONTEND_DEPLOY_HOST_PROD || secrets.DEPLOY_HOST_PROD`（dev 同理）
- [x] 3.2 将 `deploy_path` 改为 `secrets.FRONTEND_DEPLOY_PATH_PROD || secrets.DEPLOY_PATH_PROD`（dev 同理）
- [x] 3.3 将 `deploy_user` 改为 `secrets.FRONTEND_DEPLOY_USER || secrets.DEPLOY_USER`
- [x] 3.4 将 `deploy_port` 改为 `secrets.FRONTEND_DEPLOY_PORT || secrets.DEPLOY_PORT || '22'`
- [x] 3.5 将 SSH `Deploy frontend via SSH` 步骤中的 `docker compose --profile app up -d --no-deps frontend --remove-orphans` 改为 `docker compose --profile frontend up -d --no-deps frontend --remove-orphans`

## 4. 更新 cd-ai.yml

- [x] 4.1 在 `Resolve deployment variables` 步骤中，为 `deploy_host` 增加回退：`secrets.AI_DEPLOY_HOST_PROD || secrets.DEPLOY_HOST_PROD`（dev 同理）
- [x] 4.2 为 `deploy_path` 增加回退：`secrets.AI_DEPLOY_PATH_PROD || secrets.DEPLOY_PATH_PROD`（dev 同理）
- [x] 4.3 为 `deploy_user` 增加回退：`secrets.AI_DEPLOY_USER || secrets.DEPLOY_USER`
- [x] 4.4 为 `deploy_port` 增加回退：`secrets.AI_DEPLOY_PORT || secrets.DEPLOY_PORT || '22'`
- [x] 4.5 将 SSH `Deploy ai-service via SSH` 步骤中的 `docker compose --profile app up -d ai-service --remove-orphans` 改为 `docker compose --profile ai up -d --no-deps ai-service --remove-orphans`

## 5. 更新 cd-judge.yml

- [x] 5.1 在 `Resolve deployment variables` 步骤中，为 `deploy_host` 增加回退：`secrets.JUDGE_DEPLOY_HOST_PROD || secrets.DEPLOY_HOST_PROD`（dev 同理）
- [x] 5.2 为 `deploy_path` 增加回退：`secrets.JUDGE_DEPLOY_PATH_PROD || secrets.DEPLOY_PATH_PROD`（dev 同理）
- [x] 5.3 为 `deploy_user` 增加回退：`secrets.JUDGE_DEPLOY_USER || secrets.DEPLOY_USER`
- [x] 5.4 为 `deploy_port` 增加回退：`secrets.JUDGE_DEPLOY_PORT || secrets.DEPLOY_PORT || '22'`

## 6. 检查并统一 cd-infra.yml 的 Secrets 回退

- [x] 6.1 确认 `cd-infra.yml` 中 database/redis/rabbitmq 的 Secrets 回退规则与本次统一模型一致（`<SVC>_DEPLOY_* || DEPLOY_*`）
- [x] 6.2 如不一致，调整 `cd-infra.yml` 以符合统一回退规则

## 7. 更新部署文档

- [x] 7.1 更新 `docs/04-运维部署/04-03-CI-CD自动部署.md`：补充 `API_DEPLOY_*`、`FRONTEND_DEPLOY_*` Secrets 说明与统一回退规则
- [x] 7.2 更新 `docs/04-运维部署/04-04-多服务器部署.md`：补充 api/frontend 独立部署拓扑与 `.env` 配置差异说明

## 8. 验证

- [x] 8.1 执行 `openspec validate independent-api-frontend-deploy --strict` 通过
- [x] 8.2 本地执行 `docker compose --profile app config` 确认本地开发配置仍有效
- [x] 8.3 本地执行 `docker compose --profile api config`、`--profile frontend config`、`--profile ai config` 确认独立 profile 配置有效
