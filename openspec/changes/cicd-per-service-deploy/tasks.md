# 实施任务清单

## 1. 触发文件与前置准备

- [x] 1.1 新建 `trigger/` 目录，创建 `api`、`frontend`、`judge`、`ai`、`infra` 五个文件，初始内容均为 `0.1.0`（语义化版本号）
- [x] 1.2 确认 `dorny/paths-filter@v3`、`actions/download-artifact@v4`、`docker/metadata-action@v5` 等动作已在仓库可用（均为公开 Marketplace 动作；`download-artifact@v4` 与 `metadata-action@v5` 已在现有工作流使用）
- [x] 1.3 用 `actionlint` 对现有 `test_and_build.yml` 建立基线（本机 actionlint 1.7.11，基线 exit 0）

## 2. CI 工作流（ci.yml）

- [x] 2.1 新建 `.github/workflows/ci.yml`，`on.push` 配置 `paths-ignore`（`docs/**`、`**/*.md`、`.claude/**`、`scripts/hooks/**`），`on.pull_request` 同样生效
- [x] 2.2 添加 `changes` job：用 `dorny/paths-filter` 输出每个服务的 `codeChanged`（服务代码路径）与 `versionBumped`（`trigger/<svc>` 路径），路径映射按 spec「路径级 CI 过滤」，每个服务路径集**包含其 `trigger/<svc>` 文件**
- [x] 2.3 `api-service-test`、`frontend-test`、`judge-service-test`、`ai-service-test` 四个测试 job，各加 `if: needs.changes.outputs.<svc>_code == 'true'`
- [x] 2.4 `build-api-service`、`build-judge-service`、`build-ai-service` 三个构建 job，门控同上，`if: github.event_name != 'pull_request'`
- [x] 2.5 构建 job 读取 `trigger/<svc>` 版本号并校验 `x.y.z` 格式（不合法则构建失败），用 `docker/metadata-action` 打 tag：`<version>`（不可变）+ `<branch>` + 浮动 `latest`（master/main）/ `develop`（develop），推 ghcr
- [x] 2.6 构建 job 成功后上传 `changed-services.json` artifact（每服务 `codeChanged` + `versionBumped`），retention 1 天
- [x] 2.7 确认 ci.yml **不含** frontend 镜像构建（前端镜像在 CD 阶段构建）

## 3. CD 可复用工作流（cd-*.yml）

- [x] 3.1 新建 `cd-api.yml`（`on: workflow_call` + `workflow_dispatch`）：输入 `environment`、`image_tag`；步骤 = runner 拉 `ghcr.io/<owner>/bluenet-api-service:<tag>` → `docker save|gzip` → SCP tar + compose → 服务器 `docker load` → `docker compose --profile app up -d api-service` → 轮询 `/api/v1/health` 返回 `"code":200`
- [x] 3.2 新建 `cd-judge.yml`：同上模式，部署到 `JUDGE_DEPLOY_HOST`，轮询 judge 健康
- [x] 3.3 新建 `cd-ai.yml`：同上模式，部署到 `AI_DEPLOY_HOST`，轮询 AI 健康
- [x] 3.4 新建 `cd-infra.yml`：先部署 database+redis 到 `DATABASE_DEPLOY_HOST`，再部署 rabbitmq 到 `RABBITMQ_DEPLOY_HOST`（版本号不用于镜像 tag，仅存在性开关）
- [x] 3.5 新建 `cd-frontend.yml`：步骤 = 校验后端健康（轮询 `BACKEND_HOST:PORT/api/v1/health`）→ runner 上以 `vars.BACKEND_HOST` 等 build-args 构建前端镜像 → 打 tag 推 ghcr（版本号 + 浮动 latest/develop）→ `docker save|gzip` → SCP → load → `docker compose --profile app up -d --no-deps frontend` → `docker image prune -f`
- [x] 3.6 每个可复用 CD 按 `environment` 输入解析对应 secrets（`DEPLOY_*` / `JUDGE_DEPLOY_*` / `AI_DEPLOY_*` / `DATABASE_DEPLOY_*`），保留现有 fallback 逻辑
- [x] 3.7 `workflow_dispatch` 单独手动触发时，`environment` 默认 `dev`，`image_tag` 默认 `latest`(prod)/`develop`(dev)

## 4. CD 编排器（cd-deploy.yml）

- [x] 4.1 新建 `.github/workflows/cd-deploy.yml`：`on.workflow_run`（workflows: ci.yml, types: completed, branches: master/main/develop）+ `on.workflow_dispatch`（输入 `target_service`：all/api/frontend/judge/ai/infra）
- [x] 4.2 添加 `concurrency: group: cd-${{ github.event.workflow_run.head_branch || github.ref }}, cancel-in-progress: true`
- [x] 4.3 `resolve-context` job：自动路径用 `actions/download-artifact@v4` 的 `run-id`（=`github.event.workflow_run.id`）下载 `changed-services.json`；手动路径忽略该清单
- [x] 4.4 `resolve-context` job：checkout 触发 commit → 读取五个 `trigger/*` 文件内容（版本号）→ 输出 `environment`（prod/dev）、每服务 `image_tag`（auto=该服务 trigger 版本号 / manual=latest|develop）、各服务 `deploy_enabled` 标志
- [x] 4.5 自动 CD 启用条件 = `changed-services.json 中该服务 versionBumped === true 且 trigger/<svc> 文件存在`；仅 `codeChanged` 的推送不部署。手动 CD 启用条件 = `target_service 选择 且 trigger/<svc> 文件存在`
- [x] 4.6 添加 `backend-gate` job：`needs: [cd-api]`，`if: frontend_deploy_enabled && !cancelled()`，轮询 api 健康直到成功（超时失败）
- [x] 4.7 按依赖顺序调用可复用工作流：`cd-infra` → `cd-api`（needs cd-infra）→ `cd-judge`/`cd-ai`（needs cd-infra）→ `cd-frontend`（needs backend-gate），均带 `if: <svc>_deploy_enabled` 门控，`image_tag` 传各服务版本号

## 5. 文档更新

- [x] 5.1 重写 `docs/04-运维部署/04-03-CI-CD自动部署.md`：新工作流文件清单、trigger 版本号用法（bump 发布/删除关闭/仅改代码构建不部署）、tag 方案、手动部署方式、服务依赖图
- [x] 5.2 更新 `docs/00-文档导航.md` 中 CI/CD 相关引用（如需）

## 6. 验证与灰度上线

- [x] 6.1 对所有新工作流文件运行 `actionlint`，修复告警
- [ ] 6.2 推送仅修改 `src/backend/**`（不 bump `trigger/api`）到 develop：确认 CI 运行 api 测试+构建，且**不触发** api CD
- [ ] 6.3 推送仅修改 `docs/**` 的 commit 到 develop：确认 CI 完全不触发
- [ ] 6.4 bump `trigger/api`（如 `0.1.0`→`0.2.0`）并 push：确认 CI 构建 `0.2.0` tag 镜像，CD 自动部署 `0.2.0`
- [ ] 6.5 同时 bump `trigger/frontend` 与 `trigger/api` 并 push：确认前端 CD 等后端健康后才构建部署
- [ ] 6.6 删除 `trigger/frontend` 文件并 bump 其他服务版本 push：确认前端 CD 被跳过
- [ ] 6.7 在 Actions 页面手动 `workflow_dispatch` 触发 CD：确认部署 `latest`/`develop` 浮动 tag 镜像
- [ ] 6.8 将 `trigger/api` 内容改为非法版本（如 `abc`）并 push：确认 api 构建失败并报版本号格式错误
- [ ] 6.9 灰度验证全部通过后，删除 `.github/workflows/test_and_build.yml`
- [x] 6.10 更新记忆（project_environment 等）与线上部署目录中的 compose 同步说明（如适用）
