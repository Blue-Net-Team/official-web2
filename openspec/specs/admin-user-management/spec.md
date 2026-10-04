## Requirements

### Requirement: 用户管理页移动端卡片列表
`/admin/users` 页面 SHALL 在移动端（`isMobile = !screens.md`）将用户 Table 替换为卡片列表，解决操作列 `fixed: 'right'` 在窄视口遮挡信息列的问题。桌面端 SHALL 保持原有 Table 不变。

卡片 SHALL 按三层组织：
- 主行：用户名 + 角色标签 + 账号状态标签（正常/禁用）
- 详情行：学号、方向、学院、考核年级（双列等宽数字）
- 操作区：查看、编辑、重置密码、禁用/启用按钮

#### Scenario: 移动端展示用户卡片
- **WHEN** 管理员在移动端视口打开用户管理页
- **THEN** 每个用户渲染一张卡片，包含姓名、角色、状态、学号、方向、学院、年级
- **AND** 不再出现横向滚动与固定操作列

#### Scenario: 卡片操作触发原有弹窗
- **WHEN** 用户在卡片上点击查看 / 编辑 / 重置密码 / 禁用
- **THEN** SHALL 打开与桌面端相同的详情 Drawer 或编辑 / 重置密码 / 删除确认 Modal
- **AND** 操作成功后列表数据刷新行为与桌面端一致

#### Scenario: 桌面端保持表格
- **WHEN** 视口大于等于 md 断点
- **THEN** 用户列表 SHALL 以原 Table 渲染，含固定右侧操作列与 `scroll={{ x: 'max-content' }}`

### Requirement: 移动端批量操作入口
移动端卡片列表 SHALL 保留批量操作能力：卡片主行提供多选 checkbox，选中后显示与桌面端一致的批量角色变更操作入口。

#### Scenario: 移动端批量选择
- **WHEN** 用户在移动端勾选多个用户卡片
- **THEN** 批量操作按钮 SHALL 显示已选数量
- **AND** 点击后打开与桌面端相同的批量角色变更 Modal

#### Scenario: 新建用户入口
- **WHEN** 用户在移动端点击"新建用户"
- **THEN** SHALL 打开与桌面端相同的创建用户 Modal
