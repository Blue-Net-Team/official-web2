# Tasks: colocate-dockerfiles

## 1. 迁移 Dockerfile 并新增 .dockerignore

- [x] 1.1 `git mv docker/api-service.Dockerfile src/backend/Dockerfile`，COPY 改为 `COPY target/*.jar /app/backend.jar`，头部注释改为「构建上下文应为 src/backend 目录」
- [x] 1.2 `git mv docker/judge-service.Dockerfile src/judge-service/Dockerfile`，COPY 改为 `COPY target/*.jar /app/judge-service.jar`，头部注释同步更新
- [x] 1.3 `git mv docker/frontend.Dockerfile src/frontend/Dockerfile`，两处 COPY 改为 `COPY package.json pnpm-lock.yaml ./` 与 `COPY ./ ./`，头部注释同步更新
- [x] 1.4 `git mv docker/ai-service.Dockerfile src/ai-service/Dockerfile`，两处 COPY 改为 `COPY pyproject.toml uv.lock uv.toml ./` 与 `COPY ./ .`，头部注释同步更新
- [x] 1.5 新建 `src/frontend/.dockerignore`（排除 `node_modules`、`.next`、`*.log`）
- [x] 1.6 新建 `src/ai-service/.dockerignore`（排除 `.venv`、`__pycache__`、`.pytest_cache`）
- [x] 1.7 新建 `src/backend/.dockerignore` 与 `src/judge-service/.dockerignore`（排除 `hs_err_pid*.log`、`replay_pid*.log`、`*.log` 等日志文件）
- [x] 1.8 删除仓库根 `.dockerignore`；删除仓库内所有本地日志文件：`src/backend/hs_err_pid21004.log`、`src/backend/replay_pid21004.log`、`src/frontend/frontend-dev.log`

## 2. 更新 docker-compose.yml

- [x] 2.1 `api-service.build` 改为 `context: ../src/backend` + `dockerfile: Dockerfile`
- [x] 2.2 `frontend.build` 改为 `context: ../src/frontend` + `dockerfile: Dockerfile`，原有 `args` 段逐字保留
- [x] 2.3 `judge-service.build` 改为 `context: ../src/judge-service` + `dockerfile: Dockerfile`
- [x] 2.4 `ai-service.build` 改为 `context: ../src/ai-service` + `dockerfile: Dockerfile`

## 3. 更新 CI/CD 工作流

- [x] 3.1 `ci.yml` 路径过滤器：4 处 `docker/<svc>.Dockerfile` 替换为对应 `src/<service>/Dockerfile`
- [x] 3.2 `ci.yml` 3 处 build-push-action：`context: .` + `file: docker/<svc>.Dockerfile` 改为 `context: src/<service>` + `file: src/<service>/Dockerfile`
- [x] 3.3 `cd-frontend.yml` build-push-action：`context: .` + `file: docker/frontend.Dockerfile` 改为 `context: src/frontend` + `file: src/frontend/Dockerfile`，build-args 逐字保留

## 4. 更新文档

- [x] 4.1 更新 `docs/04-运维部署/04-02-Docker部署.md` 中构建命令示例（Dockerfile 路径、context 说明）
- [x] 4.2 更新 `docs/04-运维部署/04-03-CI-CD自动部署.md` 中涉及 `docker/*.Dockerfile` 的描述
- [x] 4.3 全仓 grep 验证无残留引用：`grep -rn "docker/.*Dockerfile\|api-service.Dockerfile\|frontend.Dockerfile\|judge-service.Dockerfile\|ai-service.Dockerfile" --include="*.yml" --include="*.md" --include="*.yaml" .`（排除 openspec 归档与 node_modules）结果为空

## 5. 验证

- [x] 5.1 启动基础设施 `docker compose -p bluenet --profile infra up -d`，随后 4 个服务逐一 `docker compose build <svc>` 成功
- [x] 5.2 frontend 构建时观察「transferring context」输出，确认上下文为 MB 级且不含 `node_modules`
- [x] 5.3 api / judge 镜像内 jar 存在且容器可启动（healthcheck 通过）
- [x] 5.4 frontend 容器启动后可访问 3000 端口，SSR 与浏览器侧后端地址行为与迁移前一致
- [x] 5.5 提交前确认 compose 配置合法：`docker compose config -q`
