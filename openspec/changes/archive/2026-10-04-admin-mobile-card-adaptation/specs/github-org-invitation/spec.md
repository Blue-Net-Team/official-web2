# Delta: github-org-invitation

## ADDED Requirements

### Requirement: 邀请页移动端卡片视图
GitHub 组织邀请管理页 SHALL 在移动端（`isMobile = !screens.md`）将用户 Table 替换为卡片列表，解决表格超出手机视口的问题。每张卡片 SHALL 包含：姓名 + 角色标签、方向与邮箱、GitHub 绑定状态标签（未绑定 / @用户名）、右侧单条"邀请"按钮。

#### Scenario: 移动端展示邀请卡片
- **WHEN** 管理员在移动端视口打开邀请页
- **THEN** 每个用户渲染一张卡片，展示姓名、角色、方向、邮箱、GitHub 绑定状态与邀请按钮
- **AND** 页面无横向滚动

#### Scenario: 移动端单条邀请
- **WHEN** 用户点击卡片上的邀请按钮
- **THEN** SHALL 调用单条邀请 API 并展示与桌面端一致的结果反馈（成功或失败原因）

### Requirement: 移动端批量邀请
移动端卡片 SHALL 提供多选 checkbox 以保留批量邀请能力；选中后顶部"批量邀请"按钮 SHALL 显示已选数量并调用批量邀请 API，结果弹窗与桌面端一致。

#### Scenario: 移动端批量选择与邀请
- **WHEN** 用户勾选多个卡片并点击批量邀请
- **THEN** SHALL 调用批量邀请 API 并展示成功/失败汇总与明细

#### Scenario: 未选择时批量按钮禁用
- **WHEN** 未选中任何卡片
- **THEN** 批量邀请按钮 SHALL 禁用并提示先选择用户
