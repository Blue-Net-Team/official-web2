# Delta: frontend-members-list

## MODIFIED Requirements

### Requirement: 方向筛选功能
页面 SHALL 提供方向筛选标签，支持按方向过滤成员列表，筛选时重置到第一页。筛选标签 SHALL 使用统一组件 `SmokedGlassTabs` 渲染。

#### Scenario: 显示筛选标签
- **WHEN** 页面加载完成
- **THEN** 显示四个筛选标签：全部、计算机视觉、结构设计、嵌入式开发
- **AND** 每个标签显示对应方向的成员数量

#### Scenario: 切换筛选标签
- **WHEN** 用户点击某个方向标签
- **THEN** 该标签变为激活状态，滑动胶囊指示器平移至该标签
- **AND** 成员列表只显示该方向的成员
- **AND** 分页重置到第一页
