# Design: colocate-dockerfiles

## Context

当前 4 个服务（api / frontend / judge / ai）的 Dockerfile 全部位于 `docker/` 目录，且约定「构建上下文 = 仓库根目录」：

```
仓库根 (context)
├── .dockerignore                 ← node_modules / .next / .git ...
├── docker/<svc>.Dockerfile       ← COPY src/<svc>/... 到处带前缀
└── src/<svc>/                    ← 实际源码
```

构建镜像的入口共两处：`.github/workflows/ci.yml`（构建 api / judge / ai 镜像，`context: .` + `file: docker/<svc>.Dockerfile`）与 `cd-frontend.yml`（构建 frontend 镜像，含 NEXT_PUBLIC_* 构建参数注入）。`cd-api/judge/ai.yml` 只做 ghcr→ACR 镜像搬运，不构建。`docker/docker-compose.yml` 中 4 个服务均有 `build` 段（`context: ..` + `dockerfile:`）。

关键事实：
- api / judge 采用「宿主机预编译 jar → `COPY src/<svc>/target/*.jar`」，jar 本就生成在源码目录的 `target/` 下，下沉 context 后天然可用。
- frontend Dockerfile 两段式：`ARG`（CI build-args 注入）→ `ENV` → `pnpm build`，NEXT_PUBLIC_* 语义不受 context 影响。
- ai-service 用 uv builder 阶段，`COPY src/ai-service/pyproject.toml uv.lock uv.toml ./` + `COPY src/ai-service/ .`。
- compose 中 `api-service` 的 `dockerfile: api-service.Dockerfile`（无 `docker/` 前缀）疑似笔误，迁移时统一消除。

## Goals / Non-Goals

**Goals:**
- 4 个 Dockerfile 迁入 `src/<service>/`，构建上下文统一下沉为服务目录。
- 本地 compose 构建与 CI 构建的 context/file 引用全部一致、可工作。
- CI 路径过滤映射同步更新，触发语义不变。
- 前端镜像本地构建的上下文发送量显著下降。

**Non-Goals:**
- 不改变任何镜像内容、构建参数、环境变量、compose 运行时定义或部署目标。
- 不改变 CI/CD 触发机制、版本号管理（`trigger/<svc>`）、ghcr→ACR 搬运与 Helm 部署流程。
- 不重构 Dockerfile 内部结构（多阶段、依赖安装顺序等保持原样），仅改 COPY 路径。

## Decisions

### D1: context 下沉到 `src/<service>`，而非保持根 context 只挪文件

**选择**：`context: src/<service>` + `dockerfile: Dockerfile`（compose 中为 `context: ../src/<svc>`）。
**备选**：保持根 context，仅把 Dockerfile 挪进 src（文件里 COPY 路径变成 `src/frontend/...`，与文件自身位置矛盾）。
**理由**：方案 A 是用户明确选择；context 缩小直接解决前端构建发送巨目录的问题，且 Dockerfile 与上下文同目录是 Docker 官方惯例。唯一代价是需要为每个服务维护 `.dockerignore`。

### D2: 每服务独立 `.dockerignore`，替换根 `.dockerignore`

- `src/frontend/.dockerignore`：`node_modules`、`.next`、（可选 `*.tsbuildinfo`）。
- `src/ai-service/.dockerignore`：`.venv`、`__pycache__`、`.pytest_cache`。
- `src/backend/.dockerignore`、`src/judge-service/.dockerignore`：原则上 context 内全都要用（jar 在 `target/`），不需要排除；仅排除明确的垃圾文件（如 `hs_err_pid*.log`、`replay_pid*.log`——backend 根目录确实存在这两个文件）。

根 `.dockerignore` 在全部 4 个服务下沉后已无消费方，删除。

**风险点**：frontend Dockerfile 第二阶段 `COPY --from=builder` 不受影响；第一阶段 `COPY src/frontend/ ./` 在新 context 下为 `COPY ./ ./`，`.dockerignore` 生效，`node_modules` 不会进入 context（与现状根 `.dockerignore` 行为等价，且 Dockerfile 内本就有 `rm -rf /app/node_modules && pnpm install` 双保险）。

### D3: Dockerfile 采用 `git mv` 保留历史，COPY 路径去前缀

| 服务 | 现状 | 改后 |
|------|------|------|
| api | `COPY src/backend/target/*.jar /app/backend.jar` | `COPY target/*.jar /app/backend.jar` |
| judge | `COPY src/judge-service/target/*.jar /app/judge-service.jar` | `COPY target/*.jar /app/judge-service.jar` |
| frontend | `COPY src/frontend/package.json src/frontend/pnpm-lock.yaml ./` | `COPY package.json pnpm-lock.yaml ./` |
| frontend | `COPY src/frontend/ ./` | `COPY ./ ./` |
| ai | `COPY src/ai-service/pyproject.toml src/ai-service/uv.lock src/ai-service/uv.toml ./` | `COPY pyproject.toml uv.lock uv.toml ./` |
| ai | `COPY src/ai-service/ .` | `COPY ./ .` |

注释「构建上下文应为项目根目录」同步改为「构建上下文应为 src/<service> 目录」。

### D4: CI 引用一次性切换，不留双路径兼容期

**选择**：ci.yml 与 cd-frontend.yml 直接改 `context`/`file`；路径过滤器把 `docker/<svc>.Dockerfile` 替换为 `src/<service>/Dockerfile`。
**备选**：过渡期同时监听新旧两条路径。
**理由**：这是一次性仓库内重构，过渡期过滤器反而让旧路径永久留痕；合并后旧 Dockerfile 物理删除，过滤器里保留旧路径永远触发不了构建。PR 合并即完成切换，无线上状态需要兼容。

### D5: compose `build` 段统一写法

```yaml
frontend:
  build:
    context: ../src/frontend
    dockerfile: Dockerfile
    args: ...        # frontend 保留原有 build-args
api-service:
  build:
    context: ../src/backend
    dockerfile: Dockerfile
```
`docker/` 目录下不再存在任何 Dockerfile，`api-service` 历史笔误自然消除。

## Risks / Trade-offs

- **CI 的 GHA 缓存首次失效** → `cache-from: type=gha` 的层键与 context 内容相关，切换后首次构建缓存全 miss，构建时间变长一次；后续恢复正常。可接受。
- **`.dockerignore` 遗漏导致 context 变大** → 在 tasks 中加入验证步骤：本地对每个服务执行 `docker build` 并观察 context 大小输出；frontend 应回到 MB 级而非百 MB 级。
- **遗漏引用点**（文档、脚本、IDEA 运行配置中硬编码 `docker/*.Dockerfile`）→ tasks 中包含全仓 grep 验证步骤；`docs/04-02`、`04-03` 已知需更新。
- **compose 中 infra profile 引用** → `docker/docker-compose.yml` 的 `infra` 过滤器路径（`docker/docker-compose.yml`、`docker/.env*`）不涉及 Dockerfile，不变。

## Migration Plan

1. 本变更以单个 PR 落地：迁移 Dockerfile + 新增 `.dockerignore` + 重写 compose/CI 引用 + 更新文档。
2. 合并前验证：本地 4 个服务 `docker compose build` 成功；CI 在 PR 上跑通（注意 ci.yml 的路径过滤器在 PR 分支上已用新路径，能正确触发）。
3. 回滚：git revert 该 PR 即可——无数据迁移、无状态变更，回滚无风险。
4. 注意点：合并 PR 的这次推送本身改动了 `src/<service>/Dockerfile` 与 `src/<service>/**`，会按新过滤器触发全部 4 个服务的 CI 构建（同时 `trigger/<svc>` 版本未变则只构建不部署，符合现有语义）。

## Open Questions

无——两项待决已由用户确认：

- 根 `.dockerignore`：**删除**（全部服务下沉后无消费方）。
- 日志文件：**删除仓库内所有本地日志**——`src/backend/hs_err_pid21004.log`、`src/backend/replay_pid21004.log`（JVM 崩溃日志，疑似误提交）、`src/frontend/frontend-dev.log`（前端 dev 输出）；同时各服务 `.dockerignore` 以 `*.log` / `hs_err_pid*.log` 兜底防再次进入构建上下文。
