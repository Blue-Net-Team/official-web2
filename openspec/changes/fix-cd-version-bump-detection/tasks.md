## 1. 改造 bump 检测（cd-deploy.yml resolve-context）

- [x] 1.1 checkout 增加 `fetch-depth: 0`，确保能读到历史 `deploy/` 标记
- [x] 1.2 读取 `trigger/<svc>` 当前版本号；读取 `git tag -l 'deploy/<svc>/v*'` 中版本号最大的一个作为最近部署版本
- [x] 1.3 自动路径判定改为：trigger 存在 **且** 当前版本 ≠ 最近部署版本 → 需要部署（去掉「本次推送变更路径内」条件，不再读 changed-services.json 的 versionBumped）
- [x] 1.4 冷启动：某服务无 `deploy/` 标记时视为需要部署
- [x] 1.5 保留既有行为：删除 trigger 关闭该服务 CD；仅改代码未 bump 版本（且已部署到当前版本）不部署；手动路径（target_service 选择 + trigger 存在）不变
- [x] 1.6 补 CI conclusion 守卫：`workflow_run.conclusion != 'success'` 时跳过自动 CD（既有 spec「CD 必须有对应 CI」要求，此前实现缺失——本次事故中提升版本的失败 CI 就触发了 doomed deploy）

## 2. 镜像存在性守卫（api / judge / ai）

- [x] 2.1 resolve-context job 增加 `packages: read` 权限，并登录 ghcr.io
- [x] 2.2 自动路径下，对 api/judge/ai 校验 `ghcr.io/<owner>/bluenet-<svc>-service:<当前版本>` 是否存在；frontend/infra 不校验
- [x] 2.3 镜像缺失时该服务 ENABLED=false 并输出 `::warning::`（版本漂移但镜像未构建），不使整个 CD 失败

## 3. 部署成功后打标记（编排器集中实现，不改 5 个子工作流）

- [x] 3.1 在 cd-deploy.yml 新增 tag-api/tag-judge/tag-ai/tag-frontend/tag-infra 五个 job，`needs` 各自部署 job，成功才执行
- [x] 3.2 仅当部署 tag 为语义化版本（x.y.z）时才写标记；浮动 tag（latest/develop）手动部署不写
- [x] 3.3 部署失败的服务 MUST NOT 被打上标记（needs 失败 → tag job 被跳过）
- [x] 3.4 cd-deploy.yml 顶层 `permissions` 调整为 `contents: write`；tag job 声明 `contents: write`；resolve-context 补 `packages: read`；子工作流 `cd-<svc>.yml` 无需改动

## 4. 验证

- [x] 4.1 语法校验：actionlint 校验 5 个 workflow YAML 合法（退出码 0）
- [ ] 4.2 建立基线：首次运行前为当前已部署版本预建 `deploy/<svc>/v<version>` 标记（避免冷启动误判为全量漂移而重部署）
- [ ] 4.3 提一个版本号提升（如 trigger/ai 0.1.3 → 0.1.4），CI 绿后确认自动部署 + `deploy/ai/v0.1.4` 标记写入
- [ ] 4.4 回归「提升版本但 CI 失败 → 无关修复提交 CI 通过」：确认修复提交的 CD 自动部署所有受影响服务
- [ ] 4.5 回归幂等：已部署到当前版本的服务不重复部署
