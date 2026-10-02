# Delta: frontend-user-profile

## MODIFIED Requirements

### Requirement: Tab导航展示

系统SHALL在右侧内容区顶部展示Tab导航，包含个人信息、我的考核、项目经历、个人成就、实习经历五个Tab。Tab导航 SHALL 使用统一组件 `SmokedGlassTabs` 渲染。

#### Scenario: 展示Tab计数
- **WHEN** 页面加载完成
- **THEN** 各Tab显示对应数据的数量Badge（考核数、项目数、个人成就数、实习数）
- **AND** 不再显示竞赛经历计数

#### Scenario: Tab切换
- **WHEN** 用户点击某个Tab
- **THEN** URL更新为 `?tab=<tab_name>`
- **AND** 对应Tab内容区域显示

#### Scenario: Tab导航视觉统一
- **WHEN** 个人主页渲染Tab导航
- **THEN** Tab呈现为烟色玻璃胶囊样式，带滑动胶囊指示器与文字颜色过渡动画
- **AND** 原渐变按钮组实现（`ProfileTabs`）不再被引用
