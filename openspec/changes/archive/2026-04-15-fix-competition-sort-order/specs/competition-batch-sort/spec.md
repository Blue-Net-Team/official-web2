## ADDED Requirements

### Requirement: 批量更新竞赛排序号
系统 SHALL 提供批量更新竞赛排序号的接口，接收竞赛 ID 与新排序号的列表，一次性更新所有项的 sortOrder。用于页内拖拽排序后的批量提交。

#### Scenario: 正常批量更新
- **WHEN** 管理员提交 `[{id: 1, sortOrder: 1}, {id: 2, sortOrder: 2}, {id: 3, sortOrder: 3}]`
- **THEN** 系统更新所有指定竞赛的 sortOrder，返回 200

#### Scenario: 竞赛不存在
- **WHEN** 提交的列表中包含不存在的竞赛 ID
- **THEN** 系统返回 404 并提示竞赛不存在

#### Scenario: 列表为空
- **WHEN** 提交空列表
- **THEN** 系统返回参数校验错误

#### Scenario: sortOrder 为 null
- **WHEN** 列表中某项的 sortOrder 为 null
- **THEN** 系统返回参数校验错误

### Requirement: 上移/下移竞赛排序
系统 SHALL 提供上移/下移接口，将指定竞赛与全局相邻位置的竞赛交换 sortOrder。支持跨页调整。

#### Scenario: 上移成功
- **WHEN** 管理员对 sortOrder=5 的竞赛调用 move(direction=up)
- **THEN** 系统找到 sortOrder=4 的竞赛，交换两者 sortOrder，返回 200

#### Scenario: 下移成功
- **WHEN** 管理员对 sortOrder=3 的竞赛调用 move(direction=down)
- **THEN** 系统找到 sortOrder=4 的竞赛，交换两者 sortOrder，返回 200

#### Scenario: 已在最前无法上移
- **WHEN** 管理员对 sortOrder 最小的竞赛调用 move(direction=up)
- **THEN** 系统返回错误提示"已是第一个"

#### Scenario: 已在最后无法下移
- **WHEN** 管理员对 sortOrder 最大的竞赛调用 move(direction=down)
- **THEN** 系统返回错误提示"已是最后一个"

#### Scenario: 竞赛不存在
- **WHEN** 对不存在的竞赛 ID 调用 move
- **THEN** 系统返回 404

#### Scenario: 跨页上移
- **WHEN** 第 2 页第一个竞赛（sortOrder=21）调用 move(direction=up)
- **THEN** 与第 1 页最后一个竞赛（sortOrder=20）交换 sortOrder，刷新后第 2 页第一个变成原第 1 页末尾的竞赛

### Requirement: 新建竞赛自动填充排序号
系统 SHALL 在创建竞赛时自动将 sortOrder 设置为当前最大 sortOrder + 1。若数据库中无竞赛记录，则 sortOrder 设为 1。

#### Scenario: 数据库已有竞赛
- **WHEN** 当前最大 sortOrder 为 5，管理员新建竞赛
- **THEN** 新竞赛的 sortOrder 自动设为 6

#### Scenario: 数据库无竞赛
- **WHEN** 数据库中无竞赛记录，管理员新建竞赛
- **THEN** 新竞赛的 sortOrder 设为 1

### Requirement: 竞赛列表按 sortOrder 升序排列
系统 SHALL 在查询竞赛列表时按 `sort_order ASC, id ASC` 排序，使 sortOrder 数值越小的竞赛越靠前。

#### Scenario: 公开页面查询
- **WHEN** 用户访问竞赛列表页面
- **THEN** 竞赛按 sortOrder 升序排列，sortOrder 相同时按 ID 升序

#### Scenario: 管理页面分页查询
- **WHEN** 管理员查询第 2 页（每页 20 条）
- **THEN** 返回 sortOrder 第 21~40 的竞赛，按升序排列

### Requirement: 页内拖拽排序
前端管理页面 SHALL 支持在当前分页内拖拽调整竞赛顺序，拖拽结束后按新顺序重新计算 sortOrder 并批量提交。

#### Scenario: 拖拽调整顺序
- **WHEN** 管理员在第 1 页将第 3 个竞赛拖到第 1 个位置
- **THEN** 前端重新计算当前页所有项的 sortOrder（基准值 = 0，依次为 1, 2, 3...），批量提交到后端

#### Scenario: 拖拽到原位置
- **WHEN** 管理员拖拽后放回原位
- **THEN** 不触发排序更新请求

### Requirement: 上移/下移按钮
前端管理页面 SHALL 在每行竞赛添加上移/下移按钮，首行禁用上移、末行禁用下移。

#### Scenario: 点击上移按钮
- **WHEN** 管理员点击某行的上移按钮
- **THEN** 调用 move(direction=up) 接口，成功后刷新当前页列表

#### Scenario: 点击下移按钮
- **WHEN** 管理员点击某行的下移按钮
- **THEN** 调用 move(direction=down) 接口，成功后刷新当前页列表
