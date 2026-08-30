# CI/CD 按服务独立部署（cicd-per-service-deploy）

## Why

当前 `.github/workflows/test_and_build.yml` 是一个 1000+ 行的单文件工作流：每次 push 到 master/develop 都全量执行测试、构建、部署所有服务，无法按服务选择性地部署，也无法避免纯文档改动触发 CI。镜像 tag 只有 `latest`/`develop` 两类，回滚和手动部署缺乏版本化控制。团队需要一个"CI 自动但受控、CD 按服务可开关、镜像可版本管理"的流水线。

## What Changes

- **CI 与 CD 拆分**：`ci.yml` 负责测试与构建（每个服务独立 job，路径级过滤）；CD 拆成编排器 + 每服务独立可复用工作流文件。
- **路径级 CI 过滤**：一次 push 只测试/构建实际变更的服务；纯文档（`docs/**`、`*.md`）改动不触发 CI。
- **`trigger/*` 文件存版本号并控制 CD**：每个服务一个 `trigger/<svc>` 文件，内容为语义化版本号（`x.y.z`）；文件存在=启用、删除=关闭；**bump 版本号即发布信号**。仅改代码不 bump 版本 → CI 照常构建镜像但不自动部署。
- **CD 必须有对应 CI**：自动路径由 CI 成功后的 `workflow_run` 触发；手动路径部署的是 CI 已构建过的镜像。
- **手动 CD 部署最新镜像**：`workflow_dispatch` 部署浮动 `latest`（生产）/ `develop`（开发）tag，不重新构建（前端除外，见下）。
- **镜像 tag 版本管理**：不可变 tag = `trigger/<svc>` 中的版本号（如 `1.4.0`），每次构建同时打浮动 `latest`/`develop`；自动 CD 部署版本号 tag，回滚按版本 tag。
- **保留服务依赖关系**：前端 CD 在 `cd-api` 完成后、并轮询后端健康后才构建并部署（维持现状"后端健康后构建前端"语义）。
- **删除旧工作流**：`test_and_build.yml` 由新文件替换。**BREAKING**：旧的"每次 push 全量部署"行为取消。

## Capabilities

### New Capabilities
- `cicd-per-service-deploy`: 描述 CI/CD 流水线的可观测行为——路径级 CI 过滤、trigger 文件控制 CD、镜像 tag 版本管理、每服务独立 CD 流水线与跨服务依赖。

### Modified Capabilities
<!-- 无既有 CI/CD spec，无需修改 -->

## Impact

- **`.github/workflows/`**：`test_and_build.yml` 删除，新增 `ci.yml`、`cd-deploy.yml`（编排器）、`cd-api.yml`、`cd-frontend.yml`、`cd-judge.yml`、`cd-ai.yml`、`cd-infra.yml`。
- **仓库根**：新增 `trigger/` 目录（`api`、`frontend`、`judge`、`ai`、`infra` 五个文件，初始内容 `0.1.0`；infra 文件的版本号不用于镜像 tag，仅作存在性开关）。
- **`docker/docker-compose.yml`**：保持兼容（镜像 tag 通过环境变量传入），无需改动。
- **`docs/04-运维部署/04-03-CI-CD自动部署.md`**：需重写以反映新流水线。
- **GitHub Secrets/Variables**：复用现有 `DEPLOY_*`/`JUDGE_DEPLOY_*`/`AI_DEPLOY_*`/`DATABASE_DEPLOY_*` 与 `BACKEND_HOST` 等变量，无新增必需项。
- **应用代码**：无影响（纯运维/CI 层改造）。
