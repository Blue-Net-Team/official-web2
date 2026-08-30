# CI/CD 按服务独立部署 — 技术设计

## Context

当前 `.github/workflows/test_and_build.yml`（1000+ 行、15 个 job）在每次 push 到 master/develop 时全量执行测试→构建→部署。约束与现状：

- **部署走 tar 传输**：`docker save → SCP → docker load → compose up`，避免国内服务器直接拉 ghcr 超时。此机制必须保留，手动部署也不能在服务器上 `docker compose pull`。
- **前端构建时机特殊**：`build-frontend` 依赖 `health-check`（等 api+judge+ai 全部健康），即前端镜像在后端部署健康后才构建，保证 SSR 构建期预渲染访问新后端。
- **实际分支**：远端仅 `develop` + 功能分支，尚无 master/main；GitHub Variables 已按生产域名 `www.gdou-bluenet.cn` 配置。
- 各服务 SSH secrets 独立（`DEPLOY_*`/`JUDGE_DEPLOY_*`/`AI_DEPLOY_*`/`DATABASE_DEPLOY_*`）。

需求见 proposal.md，可测试行为见 `specs/cicd-per-service-deploy/spec.md`。

## Goals / Non-Goals

**Goals:**

- 每次 push 自动 CI，但只对实际变更的服务测试+构建；纯文档不触发 CI。
- 每个服务一个 `trigger/<svc>` 文件存版本号：bump 版本 = 发布并自动 CD；删除文件 = 关闭；仅改代码 = 构建但不自动部署。
- 每服务独立 CD 工作流文件，同时保留跨服务依赖（前端等后端健康）。
- 镜像 tag 版本化：不可变 tag = trigger 版本号 + 浮动 `latest`/`develop`；手动 CD 部署最新镜像。
- CD 必须有对应 CI：自动路径 `workflow_run` 触发；手动路径部署 CI 已构建镜像。

**Non-Goals:**

- 不做 per-env 构建变量拆分（PR #55 被关闭，保持单组 GitHub Variables）。
- 不改动应用代码与 `docker/docker-compose.yml` 的服务定义。
- 不做 Kubernetes/Swarm 部署、不做监控告警体系。
- 不引入 Artifact Registry 之外的镜像仓库。

## Decisions

### 决策 1：可复用 workflow + 编排器（而非"完全独立 + 健康轮询"）

每服务 CD 逻辑写成**可复用 workflow**（`on: workflow_call` + `workflow_dispatch`），由编排器 `cd-deploy.yml` 用 `needs` 按序调用。

- **理由**：GitHub Actions 无跨 workflow 原生 `needs`。可复用 workflow + 编排器是唯一能获得"独立文件 + 真依赖顺序"的原生机制。前端 CD 真等 api CD 完成，避免轮询到旧后端就部署的竞态。
- **备选**：完全独立 CD + 内部健康轮询。更简单，但 CI 完成后各 CD 并行，前端可能在 api 新版本部署完成前部署，存在 SSR 版本错配窗口。被否。

### 决策 2：路径级 CI 过滤 = `paths-ignore`（触发层）+ `dorny/paths-filter`（job 层）

- `on.push.paths-ignore` 列出 `docs/**`、`**/*.md`、`.claude/**`、`scripts/hooks/**` → 纯文档推送工作流**不启动**（满足"无关文件不触发 CI"）。
- CI 首个 job 用 `dorny/paths-filter@v3` 计算本次变更的服务，输出 `api/frontend/judge/ai/infra` 布尔标志；各 test/build job 用 `if: needs.changes.outputs.<svc> == 'true'` 门控。
- 路径映射见 spec「路径级 CI 过滤」。每个服务的路径集**包含其 `trigger/<svc>` 文件**，使"bump 版本号"这一动作本身能触发该服务 CI。关键：`docker/docker-compose.yml` 映射到**全部服务**（compose 影响所有服务部署），`.github/workflows/**` 也触发全量（改流水线需全量验证）。

### 决策 3：CI→CD 的"本次变更服务清单"用 artifact 传递

编排器必须知道本次 CI 实际构建了哪些服务、以及其中哪些是"发布推送"（bump 了版本号），否则只改后端时会把 5 个服务全部署一遍，且无法区分"构建不部署"与"版本发布"。

- **方案**：CI 末尾用 `dorny/paths-filter` 结果生成 `changed-services.json`，每个服务记录 `codeChanged`（服务代码路径是否变更）与 `versionBumped`（`trigger/<svc>` 文件是否在本次变更内），作为 artifact 上传；编排器 `resolve-context` job 用 `actions/download-artifact@v4` 的 `run-id` 输入（= `github.event.workflow_run.id`）下载。
- **备选**：编排器内重跑 paths-filter。但 `workflow_run` 上下文下 push 事件的 base sha 不可得，需复杂推断，且 merge 场景会误报。artifact 传递确定性强。
- **自动 CD 条件** = `versionBumped === true 且 trigger/<svc> 文件存在`（发布信号）；仅 `codeChanged` 的推送走"构建不部署"。手动 CD 忽略 changed-services，按 `target_service` 输入。

### 决策 4：`backend-gate` 哨兵 job 解决"前端等后端，但后端不在本次 CD"的卡死

若 `cd-frontend` 直接 `needs: [cd-api]`，当本次只改前端（api 未进 CD 图）时，skipped 的 `cd-api` 会连带下游 skipped，前端 CD 永不执行。编排器因此加：

```yaml
backend-gate:
  needs: [cd-api]
  if: frontend_enabled && ${{ !cancelled() }}   # api 在/不在本次 CD 都执行
  steps: 轮询 https?://BACKEND_HOST:PORT/api/v1/health 直到 '"code":200'（超时则失败）

cd-frontend:
  needs: [backend-gate]
```

- api 在本次 CD → `needs` 保证等它完成，再验证健康。
- api 不在本次 CD → `if: !cancelled()` 使 job 照常运行，直接验证既有后端健康。

### 决策 5：trigger 文件 = 版本号，不可变 tag = 版本号

- `trigger/<svc>` 文件内容为语义化版本 `x.y.z`，**存在=启用 CD、删除=关闭、bump 版本号=发布信号**。
- CI 构建时读取 `trigger/<svc>` 版本号，用 `docker/metadata-action` 打：`<version>`（不可变，回滚用）+ `<branch>` + 浮动 `latest`（master/main）/ `develop`（develop）。版本号不合法（非 `x.y.z`）时该服务构建 job 直接失败。
- **自动 CD 部署版本号 tag**（`trigger/<svc>` 中的值）——"部署的就是本次发布的版本"，语义清晰。
- 手动 CD 部署浮动 `latest`/`develop`。
- **构建不部署规则**：仅代码变更（`trigger/<svc>` 未动）→ CI 照常构建并推镜像（打当前版本 tag），编排器不自动 CD。同版本 tag 可能被多次构建覆盖，见 Risks。

### 决策 6：CD 统一从 ghcr 拉镜像（runner 上），取消构建产物 artifact

自动 CD 与手动 CD 都走同一路径：runner `docker pull ghcr.io/<owner>/bluenet-<svc>:<tag>` → `docker save|gzip` → SCP tar + compose → 服务器 `docker load` → `docker compose --profile app up -d <svc>` → 轮询健康。GitHub runner 在云端可达 ghcr，服务器仍不直接拉 ghcr。

- 取消 CI 的 `docker save` tar artifact（原用于部署，现部署直接由 CD 拉取），简化 CI。

### 决策 7：前端镜像在 CD 阶段构建（保留现状语义）

ci.yml **不构建** frontend。`cd-frontend.yml` 在 `backend-gate` 确认后端健康后，在 runner 上以现有 build-args（`vars.BACKEND_HOST` 等）构建前端镜像 → 推 ghcr（打 `sha` + 浮动 tag，供手动 latest 复用）→ save→SCP→部署。手动前端 CD 也走重建（保证用最新代码+最新后端），而非复用旧镜像。

### 决策 8：infra CD 覆盖多个目标服务器

`cd-infra.yml` 一个可复用 workflow 内完成：database+redis 部署到 `DATABASE_DEPLOY_HOST`，rabbitmq 部署到 `RABBITMQ_DEPLOY_HOST`（与现状两个 job 对应）。仅当 infra 路径变更（或手动）时触发；api/judge/ai 不再把 infra 作为硬 `needs`（infra 假定已运行，避免 skip 依赖连锁），但编排器仍保证 infra 若在本次 CD 中则先于应用服务执行。

## 工作流文件骨架

```text
.github/workflows/
├─ ci.yml            # push(除 paths-ignore) + PR：changes(job) → per-svc test/build
├─ cd-deploy.yml     # workflow_run(ci,success) + workflow_dispatch
│                    #   resolve-context(下载 changed-services.json + 读 trigger/* 版本 + 判发布信号 + 算tag/env)
│                    #   → cd-infra / cd-api / cd-judge / cd-ai / backend-gate / cd-frontend
├─ cd-api.yml        # reusable + dispatch：拉 <版本号>|latest → SCP → up → 轮询/api/v1/health
├─ cd-frontend.yml   # reusable + dispatch：等后端健康 → 构建 → SCP → up
├─ cd-judge.yml      # reusable + dispatch：拉镜像 → SCP → up → 轮询 judge/health
├─ cd-ai.yml         # reusable + dispatch：拉镜像 → SCP → up → 轮询 ai/health
└─ cd-infra.yml      # reusable + dispatch：database+redis → DATABASE host；rabbitmq → RABBITMQ host
```

编排器 `resolve-context` 输出：`environment`（prod/dev）、`image_tag`（auto=sha / manual=latest|develop）、各服务 `*_enabled`、`target_hosts`。可复用 CD 通过 `workflow_call` 输入接收 `environment` 与 `image_tag`，自行按 environment 解析 secrets 与健康检查地址。

## Risks / Trade-offs

- **[workflow_run 并行竞态]** 两次快速 push 的 CI 完成后，旧 CD 可能覆盖新部署 → 编排器加 `concurrency: group: cd-${{ github.ref }}, cancel-in-progress: true`，并部署当次版本号 tag 双重防护。
- **[同版本 tag 被多次构建覆盖]** 仅改代码不 bump 版本时，CI 会重打同一版本 tag（如 `1.4.0`），回滚到 `1.4.0` 会拿到"最后一次构建的内容"而非首发内容 → 规则约束：正式发布前必须 bump 版本；如需严格不可变，可改为"版本 tag 仅在发布推送时推送、代码推送只推浮动/sha tag"。待团队确认。
- **[paths-ignore 维护脆弱]** 新文档/无关路径未列入会意外触发 CI → paths-ignore 保持最小集合，paths-filter 作为兜底（未列路径默认归类到某服务或忽略）；新增目录时提醒更新。
- **[`if: !cancelled()` 语义陷阱]** backend-gate 在 cd-api 失败时也会跑，但健康轮询会失败从而阻断前端 CD → 以健康轮询为最终防线，不依赖 workflow 状态。
- **[compose 变更触发全量部署]** 重但正确（compose 影响所有服务）；若团队希望更省可改为仅 infra，但存在"改了 compose 忘重部署"风险 → 默认全量。
- **[手动前端 CD 会重建而非用现成镜像]** 与"手动部署最新镜像"的字面略有出入，但保证新鲜度；可接受。
- **[可复用 workflow 调试成本]** workflow_call + workflow_dispatch 双触发路径需各自验证；以 `workflow_dispatch` 为主做本地回归。

## Migration Plan

1. 新增 `trigger/{api,frontend,judge,ai,infra}` 五个文件，内容 `on`。
2. 编写 `ci.yml`（含 paths-filter），本地 `actionlint` 校验语法。
3. 编写 `cd-*.yml` 可复用工作流 + `cd-deploy.yml` 编排器。
4. 先推 develop 分支灰度：只改后端文件 → 观察 CI 仅跑 api、CD 仅部署 api；只改文档 → 确认 CI 不触发。
5. 验证通过后删除 `test_and_build.yml`。
6. 更新 `docs/04-运维部署/04-03-CI-CD自动部署.md`。
7. **回滚**：若新流水线异常，git revert 到删除前提交，旧工作流仍可用（文件删除即回退）。

## Open Questions

- **前端 CD 是否需等 judge/ai 也健康？** 现状 `health-check` 等 api+judge+ai 三者。本设计默认前端只等 api（用户"后端成功启动"即 api 健康）。若需保留三服务等待，`cd-frontend` 的 `needs` 增加 cd-judge、cd-ai。待用户确认。
- **生产分支名**：默认 `master`+`main` 双识别（现 workflow 用 master、文档用 main），远端尚未创建。
