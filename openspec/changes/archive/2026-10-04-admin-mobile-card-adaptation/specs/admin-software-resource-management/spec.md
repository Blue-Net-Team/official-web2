# Spec: admin-software-resource-management

## ADDED Requirements

### Requirement: 资源库管理页移动端卡片
软件资源管理页（`/admin/resources`）SHALL 在移动端（`isMobile = !screens.md`）将 Table 替换为卡片列表，解决表格超出手机视口的问题。桌面端（含管理员的 dnd-kit 拖拽排序表格）SHALL 保持原样。

卡片 SHALL 包含：资源名称 + 启用状态开关（管理员）或状态文字（非管理员）、方向与分类标签、描述（最多两行截断）、底行外部链接 + 操作区（编辑 / 删除，管理员可见）。

#### Scenario: 移动端展示资源卡片
- **WHEN** 管理员在移动端视口打开资源库管理页
- **THEN** 每个资源渲染一张卡片，名称、状态、方向、分类、描述与操作完整可见
- **AND** 页面无横向滚动

#### Scenario: 移动端切换启用状态
- **WHEN** 管理员在卡片上点击启用开关
- **THEN** SHALL 调用状态切换 API，行为与桌面端 Switch 一致

#### Scenario: 非管理员移动端视图
- **WHEN** 非管理员在移动端打开资源库页
- **THEN** 卡片 SHALL 隐藏编辑/删除/调序操作，状态以文字展示，与桌面端权限一致

### Requirement: 移动端调序替代拖拽
移动端 SHALL 禁用 dnd-kit 拖拽排序，改为每张卡片操作区提供"上移/下移"按钮（`UpOutlined` / `DownOutlined` 或 `ArrowUpOutlined` / `ArrowDownOutlined`），点击后调用与拖拽相同的排序更新 API；首位卡片禁用上移，末位卡片禁用下移。

#### Scenario: 移动端上移资源
- **WHEN** 管理员点击某卡片的"上移"
- **THEN** 该资源与上一项交换顺序，列表即时更新并调用排序 API
- **AND** 排序失败时回滚并提示错误，与桌面端拖拽失败行为一致

#### Scenario: 边界按钮禁用
- **WHEN** 卡片位于列表首位（或末位）
- **THEN** 上移（或下移）按钮 SHALL 禁用

#### Scenario: 桌面端保持拖拽排序
- **WHEN** 视口大于等于 md 断点且用户为管理员
- **THEN** 资源列表 SHALL 保持 dnd-kit 拖拽排序表格，行为与改动前一致
