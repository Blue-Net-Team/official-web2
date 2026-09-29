# dockerfile-colocation Specification

## ADDED Requirements

### Requirement: Dockerfile 与服务源码同目录

每个提供 Docker 镜像的服务 SHALL 在其源码目录根部拥有名为 `Dockerfile` 的文件：`src/backend/Dockerfile`（api）、`src/frontend/Dockerfile`（frontend）、`src/judge-service/Dockerfile`（judge）、`src/ai-service/Dockerfile`（ai）。`docker/` 目录 SHALL NOT 包含任何 Dockerfile。

#### Scenario: 定位 api 服务 Dockerfile
- **WHEN** 开发者需要修改 api 服务的镜像构建逻辑
- **THEN** 该 Dockerfile 位于 `src/backend/Dockerfile`，与其构建的 jar 产物（`target/`）同目录

#### Scenario: docker 目录清理
- **WHEN** 检查 `docker/` 目录内容
- **THEN** 目录中不存在 `*.Dockerfile` 文件

### Requirement: 构建上下文为服务目录

所有镜像构建入口（`docker-compose.yml` 的 `build` 段、CI 工作流的 docker/build-push-action）SHALL 以 `src/<service>` 作为构建上下文（`context`），并以 `Dockerfile` 作为 `dockerfile` 值。Dockerfile 内的 COPY 指令 MUST 使用相对服务目录的路径（不带 `src/<service>/` 前缀）。

#### Scenario: compose 构建 frontend
- **WHEN** 在 `docker/` 目录执行 `docker compose build frontend`
- **THEN** 构建上下文为 `../src/frontend`，Dockerfile 为 `src/frontend/Dockerfile`，构建成功

#### Scenario: CI 构建 api 镜像
- **WHEN** CI 工作流运行 api 镜像构建 job
- **THEN** build-push-action 使用 `context: src/backend` 与 `file: src/backend/Dockerfile`，`COPY target/*.jar` 成功解析

### Requirement: 每服务独立 .dockerignore

每个包含 Dockerfile 的服务目录 MUST 拥有 `.dockerignore` 文件，排除不应进入构建上下文的本地产物：`src/frontend/.dockerignore` SHALL 至少排除 `node_modules` 与 `.next`；`src/ai-service/.dockerignore` SHALL 至少排除 `.venv`、`__pycache__`、`.pytest_cache`；`src/backend` 与 `src/judge-service` SHALL 排除 JVM 崩溃日志等垃圾文件（`hs_err_pid*.log`、`replay_pid*.log`）。仓库根 `.dockerignore` SHALL 被删除。

#### Scenario: frontend 上下文体积
- **WHEN** 构建 frontend 镜像
- **THEN** 构建上下文不包含 `node_modules` 与 `.next`，发送给 Docker daemon 的上下文为 MB 级

#### Scenario: 根 dockerignore 移除
- **WHEN** 检查仓库根目录
- **THEN** 不存在 `.dockerignore` 文件

### Requirement: 构建参数语义保持不变

Dockerfile 迁移与上下文下沉 MUST NOT 改变任何构建参数（`ARG`/`ENV`）的名称、默认值与传递方式。CI 中注入的 `NEXT_PUBLIC_*`、`BUILD_BACKEND_*`、`CACHE_BUST` 等 build-args 与 compose 中 frontend 的 `args` 段保持逐字不变。

#### Scenario: 前端构建参数注入
- **WHEN** cd-frontend 工作流以 `vars.PUBLIC_HOST` 等变量构建 frontend 镜像
- **THEN** 构建产物中客户端 bundle 的后端地址与迁移前完全一致
