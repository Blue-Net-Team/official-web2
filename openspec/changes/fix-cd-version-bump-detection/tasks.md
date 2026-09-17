## 1. 改造 bump 检测

- [ ] 1.1 读 `.github/workflows/cd-deploy.yml` 的 `resolve-context`，把「`jq` 读 changed-services.json 的 versionBumped」改为「读取 `trigger/<svc>` 当前版本号」
- [ ] 1.2 在检测逻辑里读取 `git tag -l 'deploy/<svc>/v*'` 中版本号最大的一个，与该服务当前 trigger 版本比较，不等则视为需要部署
- [ ] 1.3 处理冷启动：某服务没有对应 `deploy/` 标记时视为需要部署
- [ ] 1.4 保留「仅改代码未 bump 版本不部署」「删除 trigger 关闭 CD」「非法版本号 CI 失败」的既有行为

## 2. 部署成功后打标记

- [ ] 2.1 在各服务 CD job（`cd-api`、`cd-frontend`、`cd-judge`、`cd-ai`、`cd-infra`）部署成功并通过健康检查后，创建或更新 `deploy/<svc>/v<version>` 标记
- [ ] 2.2 用 `git tag -f deploy/<svc>/v<version> <sha>` + `git push -f origin <tag>` 推送标记
- [ ] 2.3 部署失败的服务 MUST NOT 被打上标记（验证：故意构造一次失败部署，确认标记未更新）
- [ ] 2.4 调整 CD job 的 `permissions` 增加 `contents: write`

## 3. 验证

- [ ] 3.1 提一个版本号提升（如 `trigger/ai` 从 0.1.3 → 0.1.4），CI 绿后确认自动部署 + `deploy/ai/v0.1.4` 标记写入
- [ ] 3.2 回归验证状态差检测：构造「提升版本但 CI 失败 → 修复提交 CI 通过」的场景，确认修复提交的 CD 自动部署
- [ ] 3.3 回归验证幂等：对已部署到当前版本的服务，确认不重复部署
