## ADDED Requirements

### Requirement: 分页器基本功能
分页组件 SHALL 提供页码切换功能，支持显示当前页码和总页数。

#### Scenario: 显示分页器
- **WHEN** 成员总数超过每页显示数量
- **THEN** 显示分页器组件
- **AND** 显示当前页码
- **AND** 显示总条数

#### Scenario: 切换页码
- **WHEN** 用户点击页码按钮
- **THEN** 切换到对应页码
- **AND** 更新成员列表显示

### Requirement: 分页器样式
分页组件 SHALL 使用 antd Pagination 组件，保持与项目 UI 风格一致。

#### Scenario: 分页器居中显示
- **WHEN** 分页器渲染
- **THEN** 分页器在成员列表下方居中显示
- **AND** 与成员列表保持适当间距

### Requirement: 页码切换交互
分页组件 SHALL 在页码切换时提供良好的用户体验。

#### Scenario: 切换页码时滚动到顶部
- **WHEN** 用户切换页码
- **THEN** 页面平滑滚动到成员列表顶部

#### Scenario: 切换页码时显示加载状态
- **WHEN** 用户切换页码且数据正在加载
- **THEN** 显示加载状态指示器
