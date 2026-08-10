## MODIFIED Requirements

### Requirement: Tab导航展示

系统SHALL在右侧内容区顶部展示Tab导航，包含个人信息、我的考核、项目经历、个人成就、实习经历五个Tab。

#### Scenario: 展示Tab计数
- **WHEN** 页面加载完成
- **THEN** 各Tab显示对应数据的数量Badge（考核数、项目数、个人成就数、实习数）
- **AND** 不再显示竞赛经历计数

#### Scenario: Tab切换
- **WHEN** 用户点击某个Tab
- **THEN** URL更新为 `?tab=<tab_name>`
- **AND** 对应Tab内容区域显示

### Requirement: 个人成就Tab展示

系统SHALL在个人中心新增“个人成就”只读Tab，展示由管理员维护的该用户关联成就。

#### Scenario: 查看个人成就
- **WHEN** 已登录用户访问 `/profile` 并点击“个人成就”Tab
- **THEN** 系统调用 `GET /api/v1/members/{currentUserId}/achievements`
- **AND** 展示只读成就卡片列表
- **AND** 不显示新增、编辑、删除按钮

#### Scenario: 个人成就空状态
- **WHEN** 当前用户未关联任何成就
- **THEN** “个人成就”Tab 展示空状态提示，如“暂无个人成就”

