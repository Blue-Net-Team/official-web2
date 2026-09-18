## Context

当前自动 CD 的「版本号 bump 检测」依赖 `dorny/paths-filter@v3`，它比对的是**单次推送的 diff**——`trigger/<svc>` 必须在当前这个提交里变化才算 bump。

这个口径有一个致命缺陷：**提升版本的提交若撞上 CI 失败，它的部署信号会永远丢失**。后续提交只要没再动 trigger 文件，检测就认为"没 bump"，CD 静默跳过且不报错。

2026-09-17 实测发生：

```
035fa83b  提升 trigger/ai、api、frontend 版本号   → CI 撞 minio 镜像故障 → 失败 → CD 未触发
374dec00  修 minio                               → CI 通过，但 diff 里没有 trigger → versionBumped=false → CD 全跳过
```

最后靠人工 `gh workflow run` 手动 dispatch 才补上部署。

## Goals / Non-Goals

**Goals:**
- 让「提升版本」这个发布意图**不因某次 CI 失败而丢失**
- 不引入新的外部依赖（不落数据库、不读对象存储）
- 部署状态可审计（git tag 即部署历史）

**Non-Goals:**
- 不改变镜像 tag 版本管理（不可变版本 tag + 浮动 tag 并存，已有规则）
- 不改变手动 CD 的行为
- 不处理「同一次部署中部分服务失败」的复杂回滚编排

## Decisions

### D1. 用 git tag 记录「最近一次成功部署的版本」

**决定**：每次成功部署后，CD 为该服务创建或更新一个 git tag：

```
deploy/api/v0.1.1
deploy/frontend/v0.1.3
deploy/ai/v0.1.3
```

**理由**：
- git tag 是**版本控制内**的可信记录，无需额外存储
- 命名含版本号，本身就携带「部署到哪个版本」这个信息
- 审计价值：`git tag -l 'deploy/*'` 就是完整的部署历史

**放弃的替代方案**：
- 在仓库里维护一个 `deployed-versions.json` 文件由 CD 提交回写 —— 需要 CD 有写权限往主分支提交，引入「自动化提交」的复杂性和并发风险
- 查询 ghcr.io 的已部署 tag —— 需要分清「构建过」和「部署过」，且依赖 registry API 的稳定性

### D2. bump 检测 = 当前 trigger 版本 ≠ 最近部署版本

**决定**：CD 的 `resolve-context` 在判断某服务是否需要部署时，改为：

```
读取 trigger/<svc> 当前版本 V_trigger
读取 git tag deploy/<svc>/v* 中版本号最大的一个 V_deployed
需要部署 ⟺ V_trigger ≠ V_deployed（或不存在 V_deployed）
```

**理由**：把「发布信号」从「diff 事件」变成「状态差」，天然幂等——同一个目标版本无论被推几次、中间夹多少无关提交，只要没部署到就始终「需要部署」。

**关键不变量**：
- 部署成功才打标记 → `V_deployed` 始终代表「实际在跑的版本」
- 部署失败不打标记 → 下次仍会检测到状态差并重新部署

**去掉「本次推送变更路径内」这个条件**（原 spec 条件 1）。状态差检测本身就蕴含了「该不该部署」，与本次提交动了哪个服务无关。这是修复生效的前提——本次事故里，修复 minio 的提交只动了 `src/backend`，若保留「变更路径内」条件，只能救回 api，ai/frontend 依旧漏掉，达不到「夹了无关提交也不漏」的目标。

### D3. 镜像存在性守卫（仅 CI 构建镜像的服务）

**决定**：对镜像由 CI 构建的服务（`api`、`judge`、`ai`），在启用部署前用 `docker manifest inspect ghcr.io/<owner>/bluenet-<svc>-service:<当前版本>` 校验镜像存在。不存在则**跳过该服务 + 输出 `::warning::`**，不让整个 CD 失败。

**理由**：CI 的构建 job `needs: [test]`——测试挂则镜像不构建。提升版本的提交若 CI 失败，镜像可能根本没产出；去掉「变更路径内」条件后，任何提交都可能触发一个镜像缺失的服务部署，导致 `docker pull` 失败并级联阻塞（api 失败会挡住 backend-gate → frontend）。守卫把「静默跳过」和「失败级联」都降级成「可见告警 + 自愈」。

**不适用**：`frontend`（镜像在 cd-frontend.yml 里现构建，无缺失问题）、`infra`（无独立镜像）。

**自愈性**：镜像缺失只是暂时跳过；一旦某个提交重建了该服务镜像，下一次 CD 检测到镜像存在就会部署。

### D4. 打标记的步骤放在部署成功确认之后

**决定**：每个服务的 CD job 在部署完成并通过健康检查后，`git tag -f deploy/<svc>/v<version> <sha> && git push -f origin <tag>`。

**理由**：标记代表「这个版本已经在跑」。如果部署中途失败，标记不更新，下一次 CD 仍能检测到状态差。

**风险点**：并发部署同一服务时两个 job 同时写同一个 tag。CD 已有 `concurrency: group: cd-<branch>` + `cancel-in-progress: true`，同一分支同一时刻只有一个 CD 在跑，所以这个窗口实际不存在。

### D5. 首个 bump 的边界：无基准即视为需要部署

**决定**：若某服务没有对应的 `deploy/` 标记，检测 SHALL 视为需要部署。

**理由**：这是新机制的冷启动路径。没有这个规则，从未部署过的服务永远不会被自动部署。

## Risks / Trade-offs

- **[首个 bump 误部署]** 如果某服务的 `deploy/` 标记丢了（比如 tag 被清理），下次任何提交都会触发该服务的部署 → 缓解：标记是 `deploy/<svc>/v<version>` 形式，不在常规清理范围；且部署本身幂等（部署已部署的版本是无害的）
- **[写 tag 需要权限]** CD job 需要 `contents: write` 权限来 push tag → 现有 `permissions: contents: read` 需要调整（见 Migration）
- **[浮动 tag 与版本 tag 的关系]** 检测只看 `deploy/` 标记，与镜像 tag 无耦合，不引入新的一致性问题

## Migration Plan

1. 修改 `cd-deploy.yml`：`resolve-context` 的 bump 检测逻辑改为「当前 trigger 版本 vs 最近部署 tag」，去掉「本次推送变更路径内」条件；对 api/judge/ai 增加镜像存在性守卫
2. 各服务 CD job 末尾增加「部署成功后打 `deploy/<svc>/v<version>` 标记」步骤（仅版本化部署）
3. 调整 `cd-deploy.yml` 及各 `cd-<svc>.yml` 的 `permissions`：`contents: write`（写 tag）；`resolve-context` 增加 `packages: read`（镜像存在性校验）
4. 用一次真实 bump 验证：提一个版本号提升，CI 绿后确认自动部署 + 标记写入
5. 用一个「提升版本但 CI 失败 → 无关修复提交 CI 通过」的场景回归验证状态差检测能救回所有受影响服务

回滚：恢复 `cd-deploy.yml` 的检测逻辑即可；已打的 `deploy/` 标记不删除，只是不再被读取。

## Open Questions

1. **`deploy/` 标记的保留期**：是永久保留（部署历史）还是定期清理？我的建议是永久保留——git tag 几乎不占空间，且是部署审计的依据。
2. **多服务同时 bump**：一次推送同时 bump 多个服务时，各自独立检测与部署，本机制天然支持，无需额外处理。