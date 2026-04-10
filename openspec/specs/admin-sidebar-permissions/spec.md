## ADDED Requirements

### Requirement: 菜单项权限过滤
系统 SHALL 根据当前用户的 roleLevel 过滤 AdminSideBar 菜单项。每个菜单项 SHALL 定义最低可见 roleLevel（minLevel），只有用户 roleLevel >= minLevel 时才显示该菜单项。

#### Scenario: MEMBER 看到的菜单
- **WHEN** 用户 roleLevel 为 1（MEMBER）
- **THEN** 侧边栏显示：回到首页、报名管理、考核（仅含考核评判子项）、QA管理（灰显）

#### Scenario: DIRECTION_ADMIN 看到的菜单
- **WHEN** 用户 roleLevel 为 2（DIRECTION_ADMIN）
- **THEN** 侧边栏显示：回到首页、报名管理、考核（含考核时间、考核题目、考核评判）、QA管理（灰显）

#### Scenario: SUPER_ADMIN 看到的菜单
- **WHEN** 用户 roleLevel 为 3（SUPER_ADMIN）
- **THEN** 侧边栏显示：回到首页、报名管理、竞赛管理、成就管理、考核（含考核时间、考核题目、考核评判）、QA管理（灰显）

### Requirement: 菜单导航跳转
系统 SHALL 在用户点击菜单项时导航到对应路由。每个菜单项 SHALL 绑定目标路由路径。

#### Scenario: 点击一级菜单项
- **WHEN** 用户点击"报名管理"菜单项
- **THEN** 系统导航到 `/admin/enroll`

#### Scenario: 点击二级菜单项
- **WHEN** 用户点击"考核 > 考核时间"菜单项
- **THEN** 系统导航到 `/admin/assessment/time`

#### Scenario: 点击回到首页
- **WHEN** 用户点击"回到首页"菜单项
- **THEN** 系统导航到 `/`（公共首页）

### Requirement: QA管理灰显
QA管理菜单项 SHALL 始终显示为禁用状态（灰显），不可点击。

#### Scenario: QA管理在所有角色下的状态
- **WHEN** 任意角色用户查看侧边栏
- **THEN** QA管理菜单项显示为灰显禁用状态，点击无响应

### Requirement: 菜单数据驱动配置
菜单项 SHALL 通过配置数组定义，包含 key、label、path、minLevel、disabled 等字段。组件 SHALL 从 authStore 获取用户角色信息，使用 getRoleLevel() 计算权限等级，动态过滤菜单项。

#### Scenario: 从配置生成菜单
- **WHEN** 组件渲染时
- **THEN** 系统读取菜单配置数组，过滤掉 minLevel > 当前用户 roleLevel 的项，将结果传递给 Ant Design Menu 组件
