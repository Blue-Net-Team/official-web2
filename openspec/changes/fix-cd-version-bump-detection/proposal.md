## Why

自动 CD 把「版本号提升」当作发布信号，但提升的检测依赖 `dorny/paths-filter` 对**单次推送 diff** 的比对：`trigger/<svc>` 必须在**当前这个提交**里变化才算 bump。这导致一个真实缺陷：**提升版本的提交如果撞上 CI 失败，它的部署信号就永远丢失**——后续任何提交只要没再动 trigger 文件，`versionBumped` 就是 false，CD 静默跳过，且不报错、不留痕。

2026-09-17 实际发生：版本提升在提交 A（其 CI 恰好失败），修复 minio 镜像源的提交 B（CI 通过但未动 trigger）→ 版本提升的部署完全丢失，最后靠人工手动 dispatch CD 才补上。

## What Changes

把「版本号 bump 检测」的基准从**「本次推送的 diff」**改成**「当前 trigger 版本 vs 最近一次成功部署的版本」**：

- CD 在每次成功部署后，为该服务打一个 git 标记 `deploy/<svc>/v<version>`，作为「最近一次成功部署版本」的可信来源
- 检测逻辑改为：当「当前 `trigger/<svc>` 版本号」≠「该服务最近一次成功部署的版本号」时，视为需要部署
- 保留既有规则：仅改代码未 bump 版本不部署；trigger 文件删除则关闭该服务自动 CD；版本号格式非法则 CI 失败

**非目标**：不改变镜像 tag 版本管理规则（不可变版本 tag + 浮动 tag 并存）；不改变手动 CD 的行为。

## Capabilities

### New Capabilities

无。

### Modified Capabilities

- `cicd-per-service-deploy`: 「trigger 文件存储版本号并控制自动 CD」这条需求的 bump 检测口径 —— 从「本次推送 diff」改为「与最近一次成功部署的版本比较」

## Impact

- `.github/workflows/cd-deploy.yml`：`resolve-context` 的 bump 检测逻辑；新增「部署成功后打 `deploy/<svc>/v<version>` 标记」的步骤
- 新增 git tag 约定：`deploy/<svc>/v<version>`（部署成功的可信记录）
- 无代码、无数据库、无接口变更

## 风险

- **首个 bump 的边界**：如果某服务从未部署过（没有对应 `deploy/` 标记），首次 bump 检测必须能识别为「需要部署」，不能因为没有基准而跳过
- **失败部署的标记**：只有**成功**的部署才能打标记；部分服务部署失败时不得给失败服务打标记，否则下次会误判为已部署
- **并发部署**：同一服务并发触发 CD 时，标记的写时机需在部署成功确认之后，避免标记先于实际部署生效
