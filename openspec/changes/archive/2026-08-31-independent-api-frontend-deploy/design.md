# independent-api-frontend-deploy 设计文档

## Context

当前 CI/CD 架构：

- 单一 `docker/docker-compose.yml` 定义所有服务，通过 profiles（`app` / `judge` / `infra` / `full`）控制启动范围。
- `api-service`、`frontend`、`ai-service` 共用 `app` profile，且共用同一组 `DEPLOY_HOST_*` / `DEPLOY_PATH_*` Secrets。
- `judge-service` 已使用 `JUDGE_DEPLOY_*` 独立部署到专用判题服务器。
- `ai-service` 已使用 `AI_DEPLOY_*` Secrets，但部署命令仍使用 `--profile app`。
- 前端镜像在 CD 阶段构建，构建前需轮询后端健康；运行时通过 `depends_on: api-service` 保证启动顺序。

## Goals / Non-Goals

**Goals:**

1. api-service 与 frontend 支持独立主机、独立路径部署。
2. 保留单一 `docker-compose.yml`，不拆分为多个 compose 文件。
3. 未配置新 Secrets 时，部署行为与现状完全一致（向后兼容）。
4. 统一所有服务 CD 工作流的 Secrets 回退规则：`<SVC>_DEPLOY_* || DEPLOY_*`。
5. 本地开发体验不变，`app` / `full` profile 继续可用。

**Non-Goals:**

- 不实现多份 `docker-compose.yml` 拆分。
- 不改变 `cd-deploy.yml` 的编排依赖顺序（仍是 `infra → api/judge/ai → backend-gate → frontend`）。
- 不自动下发/生成各主机 `.env` 文件（仍由运维在目标机维护）。
- 不调整前端 Next.js SSR 构建逻辑，仍要求构建时可访问后端。

## Decisions

### Decision 1: 保留单一 compose，用细粒度 profile 拆分

将 `api-service`、`frontend`、`ai-service` 分别加入独立的 `api`、`frontend`、`ai` profile，同时保留在 `app` profile 中。

```yaml
api-service:
  profiles: [full, app, api]
frontend:
  profiles: [full, app, frontend]
ai-service:
  profiles: [full, app, ai]
```

- `app` 保留用于本地开发一键启动。
- 生产 CD 使用独立 profile，实现“同一份 compose 文件，多台主机只启动自己需要的服务”。

**替代方案**：拆分成 `docker-compose.api.yml`、`docker-compose.frontend.yml` 等。被否决，因为会增加配置重复，且违背“单一 compose”约束。

### Decision 2: Secrets 回退规则

所有服务统一：

```yaml
deploy_host: ${{ secrets.API_DEPLOY_HOST_PROD || secrets.DEPLOY_HOST_PROD }}
deploy_path: ${{ secrets.API_DEPLOY_PATH_PROD || secrets.DEPLOY_PATH_PROD }}
deploy_user: ${{ secrets.API_DEPLOY_USER || secrets.DEPLOY_USER }}
deploy_key:  ${{ secrets.API_DEPLOY_KEY  || secrets.DEPLOY_KEY }}
deploy_port: ${{ secrets.API_DEPLOY_PORT || secrets.DEPLOY_PORT || '22' }}
```

未配置新 Secrets 时自动回退，保证零 breaking change。

### Decision 3: 前端部署使用 `--no-deps`

`frontend` 服务在 compose 中定义了 `depends_on: api-service`。跨主机部署时 Docker 无法解析该依赖，会导致启动失败。

CD 命令改为：

```bash
docker compose --profile frontend up -d --no-deps frontend --remove-orphans
```

- `--no-deps` 告诉 Compose 不启动/检查依赖服务。
- 同机部署时，api-service 已由 `cd-api` 提前启动，不影响。
- 跨机部署时，frontend 主机上没有 api-service 容器，避免报错。
- 启动顺序由 `cd-deploy.yml` 的 `backend-gate` 在编排层保证。

### Decision 4: ai-service 同步改为独立 profile

`cd-ai.yml` 当前使用 `--profile app up -d ai-service`，改为：

```bash
docker compose --profile ai up -d --no-deps ai-service --remove-orphans
```

使 ai 与 judge 的部署模型一致，避免“独立 Secrets 但共享 profile”的混乱状态。

### Decision 5: 统一所有 cd-*.yml 的 Secrets 回退

`cd-judge.yml`、`cd-ai.yml` 当前已使用专属 Secrets，但未实现 `|| secrets.DEPLOY_*` 回退。本次统一加上，形成一致的 Secrets 模型。

## Risks / Trade-offs

| 风险 | 缓解措施 |
|---|---|
| Secrets 回退表达式 `||` 在 GitHub Actions 中行为不符合预期 | 已在 `cd-infra.yml` 中使用相同模式验证可行（`DATABASE_HOST_DEV || DEPLOY_HOST_DEV`）。 |
| `--no-deps` 导致本地开发忘记启动 api-service | 本地开发仍推荐使用 `--profile app` 或 `--profile full`，`depends_on` 正常生效。 |
| 前端构建时仍需访问后端，跨机时网络不通 | 构建机在 GitHub Actions Runner 上，通过公网地址访问后端；`cd-frontend.yml` 已有 `backend-gate` 健康检查。 |
| 回退逻辑引入配置歧义 | 在文档中明确优先级：专属 Secrets > 全局 `DEPLOY_*`。 |
| 现有服务器没有配置新 Secrets 导致部署失败 | 未配置时回退到 `DEPLOY_*`，行为与现状完全一致。 |

## Migration Plan

1. **代码变更**：合并 compose profile 与 CD 工作流调整。
2. **验证同机部署**：不新增任何 Secrets，执行一次手动 CD，确认 api/frontend/ai 仍正常部署。
3. **可选：启用分离部署**：
   - 在 GitHub Secrets 中配置 `API_DEPLOY_*` 与 `FRONTEND_DEPLOY_*`。
   - 如果仍同机，将新 Secrets 填为与 `DEPLOY_*` 相同的值。
   - 如果跨机，填不同主机地址，并确保目标机 `.env` 已配置。
4. **回滚**：删除新 Secrets 或恢复旧版本 workflow 文件即可。

## Open Questions

- 是否需要把 `database`、`rabbitmq` 的 `DATABASE_DEPLOY_*` / `RABBITMQ_DEPLOY_*` 回退逻辑也一并统一到本次变更中？建议一并处理，保持模型一致。
