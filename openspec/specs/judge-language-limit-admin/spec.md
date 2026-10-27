# judge-language-limit-admin Specification

## Purpose

管理端算法题语言资源限制的读取、展示、回填与确认行为规范。

## Requirements

### Requirement: Admin judge config response includes persisted language limits

管理端读取算法题判题配置时，系统 SHALL 在同一个响应中返回该题已持久化的语言资源限制列表，每项包含语言、时间限制（毫秒）、内存限制（KB）、输出限制（KB）、确认状态与确认时间；限制列表 MUST 按语言名升序排列。

#### Scenario: Config response carries confirmed limits

- **WHEN** 管理员请求 `GET /api/v1/admin/judge/questions/{questionId}/config`，且该题已为 `python` 确认过 `time_limit_ms=1200`、`memory_limit_kb=262144`、`output_limit_kb=1024`
- **THEN** 响应 SHALL 在 `languageLimits` 中包含一项语言为 `python` 的限制对象，其时间、内存、输出限制与数据库一致，确认状态为已确认，并带上确认时间

#### Scenario: Question has no persisted limits

- **WHEN** 管理员请求某算法题的判题配置，且该题从未确认过任何语言资源限制
- **THEN** 响应 SHALL 返回空的 `languageLimits` 列表，且其余判题配置字段保持原有内容不变

#### Scenario: Multiple languages are ordered deterministically

- **WHEN** 某题已确认 `python` 与 `cpp` 两种语言的资源限制
- **THEN** 响应中的 `languageLimits` SHALL 按语言名升序排列（`cpp` 在前，`python` 在后）

#### Scenario: Limits are read by question scope

- **WHEN** 某题的判题配置被原地更新（generator 或标准解变更）而语言资源限制未被重新确认
- **THEN** 判题配置响应 SHALL 继续返回该题此前确认的语言资源限制，不因配置行更新而丢失

### Requirement: Admin resource limit tab reflects persisted limits

管理端考题编辑面板的「资源限制」页签 SHALL 展示每个已持久化的语言资源限制的数值与确认状态，并 SHALL 在确认表单中回填该语言的既有限制值；对尚未确认的语言，表单 SHALL 预填 benchmark 建议限时与峰值内存，缺失时回退到宽松默认值。

#### Scenario: Reopen drawer shows confirmed values

- **WHEN** 管理员确认过 `python` 的资源限制后关闭抽屉，随后重新打开该题的编辑面板并切换到「资源限制」页签
- **THEN** 页签 SHALL 显示 `python` 的时间、内存、输出限制与「已确认」状态，而不是一张空表单

#### Scenario: Selecting a confirmed language prefills stored values

- **WHEN** 管理员在确认表单中把语言切换为一个已有确认记录的语言
- **THEN** 时间、内存、输出三个输入框 SHALL 被填入该语言已持久化的限制值

#### Scenario: Selecting an unconfirmed language prefills suggestion

- **WHEN** 管理员在确认表单中把语言切换为一个尚无确认记录、但 benchmark 已完成的语言
- **THEN** 时间输入框 SHALL 填入该语言的建议限时，内存输入框 SHALL 填入其峰值内存

#### Scenario: Suggestion missing falls back to permissive defaults

- **WHEN** 管理员把语言切换为一个既无确认记录也无 benchmark 结果的语言
- **THEN** 表单 SHALL 使用宽松默认值（时间 5000 毫秒、内存 262144 KB、输出 1024 KB）

#### Scenario: Confirming refreshes the tab without reopening

- **WHEN** 管理员提交确认某语言的资源限制并收到成功响应
- **THEN** 页签 SHALL 立即以新值展示该语言的已确认限制与确认状态，无需关闭并重新打开抽屉

### Requirement: Confirm form language options follow configured standard solutions

确认表单的语言下拉 SHALL 与当前判题配置中标准解语言集合保持一致，且默认选中该集合中的第一个语言，不再使用硬编码语言值。

#### Scenario: Default language follows configured solutions

- **WHEN** 某题标准解语言为 `cpp` 与 `go`，管理员首次打开「资源限制」页签
- **THEN** 确认表单的语言下拉 SHALL 默认选中 `cpp`，而不是固定选中 `python`

#### Scenario: Removed language is not selectable

- **WHEN** 某题历史上确认过 `java` 的资源限制，但当前标准解已不再包含 `java`
- **THEN** 确认表单的语言下拉 SHALL 不提供 `java` 选项，历史限制行 SHALL 仅作为历史记录展示且不参与选择
