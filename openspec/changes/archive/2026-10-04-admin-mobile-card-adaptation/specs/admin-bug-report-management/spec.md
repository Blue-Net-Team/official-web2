# Delta: admin-bug-report-management

## ADDED Requirements

### Requirement: Bug 报告列表移动端卡片
Bug 报告管理页 SHALL 在移动端（`isMobile = !screens.md`）将 Table 替换为卡片列表，解决表格超出手机视口的问题。

卡片 SHALL 包含：状态标签 + GitHub Issue 链接（无则显示"未关联"）、标题（最多两行截断）、描述（最多两行截断）、底行报告 ID + 页面 URL + "查看详情"按钮。图标使用 `SearchOutlined`、`GithubOutlined`、`EyeOutlined`。

#### Scenario: 移动端展示报告卡片
- **WHEN** 管理员在移动端视口打开 Bug 报告管理页
- **THEN** 每条报告渲染一张卡片，状态、标题、描述、Issue 链接与查看入口完整可见
- **AND** 页面无横向滚动

#### Scenario: 移动端查看详情
- **WHEN** 用户点击卡片上的查看详情
- **THEN** 详情 Drawer 在移动端以全屏宽度打开，展示状态、标题、描述、页面 URL、环境信息与截图预览

#### Scenario: 状态筛选在移动端可用
- **WHEN** 用户在移动端使用状态筛选
- **THEN** 卡片列表 SHALL 按筛选条件刷新，行为与桌面端一致

#### Scenario: 桌面端保持表格
- **WHEN** 视口大于等于 md 断点
- **THEN** 页面 SHALL 渲染原 Table（`scroll={{ x: 'max-content' }}`），视觉与改动前一致
