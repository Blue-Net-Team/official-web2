## ADDED Requirements

### Requirement: 全局方向筛选
考题管理页面的方向筛选器 SHALL 支持「全局」选项，用于筛选 `direction = null` 的全局考核时间。

#### Scenario: SUPER_ADMIN 看到全局选项
- **WHEN** SUPER_ADMIN 打开方向筛选下拉框
- **THEN** 下拉框包含三个具体方向选项以及「全局」选项

#### Scenario: 选择全局后列出全局考核时间
- **WHEN** SUPER_ADMIN 选择「全局」方向
- **THEN** 考核时间下拉框仅列出 `direction = null` 的考核时间

#### Scenario: 全局考核时间选项显示方向标签
- **WHEN** 考核时间下拉框展示全局考核时间
- **THEN** 选项标签显示为「全局 · 第 N 轮 · 不限年级」或等效文案

#### Scenario: DIRECTION_ADMIN 看不到全局选项
- **WHEN** DIRECTION_ADMIN 打开方向筛选下拉框
- **THEN** 下拉框仅包含其所在方向，不包含「全局」选项

## MODIFIED Requirements

### Requirement: 权限控制
管理页面 SHALL 根据用户角色限制操作权限，全局考核仅 SUPER_ADMIN 可操作。

#### Scenario: SUPER_ADMIN 全部权限
- **WHEN** SUPER_ADMIN 访问考题管理页面
- **THEN** 可以选择所有方向（含「全局」）、管理所有考核时间的考题

#### Scenario: DIRECTION_ADMIN 方向限制
- **WHEN** DIRECTION_ADMIN 访问考题管理页面
- **THEN** 方向筛选自动选中其方向，只能管理该方向考核时间下的考题，不可选择「全局」

#### Scenario: DIRECTION_ADMIN 无法操作全局考题
- **WHEN** DIRECTION_ADMIN 通过任何方式进入全局考核的题目列表
- **THEN** 不显示新增/编辑/删除按钮，仅允许查看
