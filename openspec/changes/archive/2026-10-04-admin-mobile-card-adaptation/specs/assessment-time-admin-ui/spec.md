# Delta: assessment-time-admin-ui

## ADDED Requirements

### Requirement: 移动端考核时间卡片
考核时间管理页 SHALL 在移动端（`isMobile = !screens.md`）将表格替换为卡片列表（替代现有的"删列后表格"策略），解决开始/结束时间两列在窄视口换行不美观的问题。

卡片 SHALL 包含：方向标签（全局/具体方向）+ 轮次 + 年级、时间范围单行展示（`YYYY-MM-DD HH:mm ~ HH:mm`，配 `ClockCircleOutlined` 图标）、限时与组队说明、状态标签（未开始/进行中/已结束）、编辑与删除按钮。

#### Scenario: 移动端时间单行展示
- **WHEN** 管理员在移动端视口打开考核时间页
- **THEN** 每条考核时间渲染一张卡片，开始与结束时间在同一行内完整展示，不换行、不截断
- **AND** 卡片显示限时、组队、状态与操作按钮

#### Scenario: 卡片操作与桌面端一致
- **WHEN** 用户点击卡片或编辑 / 删除按钮
- **THEN** SHALL 打开与桌面端相同的详情 Drawer / 编辑 Drawer / 删除确认 Modal
- **AND** 删除确认文案与权限控制（仅 SUPER_ADMIN 或本方向管理员可操作）与桌面端一致

#### Scenario: 桌面端保持表格
- **WHEN** 视口大于等于 md 断点
- **THEN** 页面 SHALL 渲染原完整表格（含年级、限时、组队列），视觉与改动前一致
