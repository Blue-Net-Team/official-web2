## MODIFIED Requirements

### Requirement: trigger 文件存储版本号并控制自动 CD

每个服务 SHALL 在仓库根 `trigger/` 目录拥有一个触发文件（`api`、`frontend`、`judge`、`ai`、`infra`），文件内容为语义化版本号（格式 `x.y.z`，如 `1.4.0`）。该文件存在 SHALL 表示该服务启用自动 CD；删除该文件 SHALL 表示关闭自动 CD。

每次成功部署后，CD SHALL 为该服务创建或更新一个 git 标记 `deploy/<svc>/v<version>`，记录「该服务最近一次成功部署的版本号」。

自动 CD SHALL 仅在满足以下全部条件时执行该服务部署：
1. 该服务在本次推送的变更路径内；**且**
2. `trigger/<svc>` 文件存在；**且**
3. `trigger/<svc>` 的当前版本号 ≠ 该服务最近一次成功部署的版本号（以 `deploy/<svc>/v<version>` 标记为准）。

若某服务从未成功部署过（不存在对应 `deploy/` 标记），则条件 3 SHALL 视为满足（即首次 bump 必须触发部署）。

仅变更服务代码而未变更 `trigger/<svc>` 版本号的推送，CI SHALL 照常构建镜像但 SHALL NOT 自动部署该服务。

`trigger/<svc>` 内容不符合 `x.y.z` 格式时，该服务 CI 构建 SHALL 失败。

#### Scenario: 版本号 bump 触发自动部署
- **WHEN** 推送修改了 `trigger/api` 内容（从 `1.4.0` 改为 `1.5.0`），且 `trigger/api` 存在，且 api 最近一次成功部署的版本为 `1.4.0`
- **THEN** CI 构建 api 镜像并打 `1.5.0` tag，编排器自动触发 api 的 CD 部署该版本

#### Scenario: 失败的 CI 不丢失部署信号
- **WHEN** 提升版本的提交因无关原因 CI 失败，随后一个修复提交的 CI 通过，且该服务最近一次成功部署的版本仍为旧版本
- **THEN** 修复提交的 CD 检测到「当前 trigger 版本 ≠ 最近部署版本」，自动部署该服务
- **AND** 无需人工手动 dispatch

#### Scenario: 已部署到当前版本则不重复部署
- **WHEN** 当前 `trigger/api` 版本等于 api 最近一次成功部署的版本
- **THEN** CD SHALL NOT 部署 api

#### Scenario: 首次部署无基准时按 bump 处理
- **WHEN** 某服务存在 `trigger/<svc>` 但从未成功部署过（无 `deploy/<svc>/v<version>` 标记）
- **THEN** CD SHALL 将其视为需要部署

#### Scenario: 仅改代码不部署
- **WHEN** 本次推送只修改了 `src/backend/**`，`trigger/api` 内容未变，且当前版本已部署
- **THEN** CI 照常测试并构建 api 镜像，但编排器不自动部署 api

#### Scenario: 部署成功后才打标记
- **WHEN** 某服务部署成功
- **THEN** CD SHALL 创建或更新 `deploy/<svc>/v<version>` 标记指向当前提交
- **AND** 部署失败的服务 MUST NOT 被打上标记

#### Scenario: 删除触发文件关闭 CD
- **WHEN** 运维删除 `trigger/frontend` 文件
- **THEN** 前端服务不再参与自动 CD

#### Scenario: 非法版本号构建失败
- **WHEN** `trigger/api` 内容为 `abc`（非 `x.y.z` 格式）
- **THEN** api 的 CI 构建 job 失败并提示版本号格式错误
