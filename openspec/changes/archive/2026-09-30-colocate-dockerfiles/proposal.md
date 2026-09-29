# Proposal: colocate-dockerfiles

## Why

当前 4 个服务的 Dockerfile 统一放在 `docker/` 目录，且构建上下文必须是仓库根目录。这带来两个问题：本地开发部署体验差（每次构建前端镜像都要把整个 monorepo 作为构建上下文发送给 Docker daemon，`node_modules`/`.next` 等巨目录仅靠根目录 `.dockerignore` 兜底）；Dockerfile 与它所构建的源码分离，源码目录内的 COPY 路径必须带 `src/<service>/` 前缀，违反"Dockerfile 与其上下文同目录"的直觉惯例。

## What Changes

- 将 4 个 Dockerfile 从 `docker/` 迁移至各自服务源码目录：`src/backend/Dockerfile`、`src/frontend/Dockerfile`、`src/judge-service/Dockerfile`、`src/ai-service/Dockerfile`。
- 构建上下文从仓库根目录下沉为各服务目录（`context: src/<service>`），Dockerfile 内所有 COPY 路径去掉 `src/<service>/` 前缀。
- 为每个服务目录新增 `.dockerignore`（frontend: `node_modules`/`.next`；ai-service: `.venv`/`__pycache__`；backend/judge: `target` 中除产物外的构建垃圾等）。
- 重写 `docker/docker-compose.yml` 中 4 处 `build` 段（context + dockerfile），顺带统一 `api-service` 当前缺失 `docker/` 前缀的 `dockerfile:` 写法。
- 更新 CI/CD 工作流：`.github/workflows/ci.yml`（4 处路径过滤器 + 3 处 `file:`/`context:`）与 `cd-frontend.yml`（1 处 `file:`/`context:`）。
- 更新部署文档 `docs/04-运维部署/04-02-Docker部署.md`、`04-03-CI-CD自动部署.md` 中的构建命令示例。
- 删除根目录 `.dockerignore`（无消费方），并删除仓库内所有本地日志文件（`src/backend` 的 JVM 崩溃日志 ×2、`src/frontend/frontend-dev.log`）。
- **BREAKING（仅工作流契约）**：CI 路径过滤映射变化——`docker/<svc>.Dockerfile` 过滤器路径变更为 `src/<service>/Dockerfile`；任何外部脚本/文档若硬编码 `docker/*.Dockerfile` 路径需同步更新。

## Capabilities

### New Capabilities

- `dockerfile-colocation`: Docker 镜像构建的目录组织约定——Dockerfile 与其构建上下文同位于 `src/<service>/`，每服务自带 `.dockerignore`，compose 与 CI 统一以 `context: src/<service>` + `dockerfile: Dockerfile` 引用。

### Modified Capabilities

- `cicd-per-service-deploy`: 「路径级 CI 过滤」需求中的服务与路径映射更新——Dockerfile 过滤器路径从 `docker/<svc>.Dockerfile` 改为 `src/<service>/Dockerfile`；镜像构建的 context/file 指向同步变更。CI 触发语义（变更哪些文件触发哪个服务）本身不变。

## Impact

- **代码/配置**：`docker/*.Dockerfile`（移动+改写）、`docker/docker-compose.yml`、根 `.dockerignore`、4 个新增 `src/<service>/.dockerignore`。
- **CI/CD**：`.github/workflows/ci.yml`、`cd-frontend.yml`（`cd-api/judge/ai.yml` 仅做 ghcr→ACR 镜像搬运，不涉及构建，无需改动）。
- **文档**：`docs/04-运维部署/04-02`、`04-03`。
- **运行时行为**：无——镜像内容、构建参数（NEXT_PUBLIC_* 等）、compose 服务定义、部署目标均不变，仅构建目录结构变化。
- **兼容性**：CI 的 GHA 构建缓存（`cache-from: type=gha`）键不受影响；首次下沉后各服务 context 变小，构建上下文发送量显著下降（尤其前端）。
