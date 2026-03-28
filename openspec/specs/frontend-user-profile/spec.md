# User Profile Specification

定义用户个人主页的核心功能，包括个人信息展示、编辑以及考核信息展示。

## ADDED Requirements

### Requirement: 用户信息卡片展示
系统SHALL在个人主页左侧展示用户信息卡片，包含头像、姓名、角色、简介、基本信息和统计数据。

#### Scenario: 展示考生信息卡片
- **WHEN** 角色为"考生"的用户访问个人主页
- **THEN** 系统展示包含以下信息的卡片：
  - 头像（如有）或默认头像
  - 姓名和昵称
  - 角色标签显示"考生"（橙色渐变）
  - 个人简介文本
  - 学院、专业、年级信息
  - 报名方向信息
  - 统计数据（考核轮次数、已完成数、平均分）

#### Scenario: 展示正式成员信息卡片
- **WHEN** 角色为"正式成员"的用户访问个人主页
- **THEN** 系统展示角色标签为"成员"（蓝紫渐变）

### Requirement: 个人信息编辑
系统SHALL允许用户编辑个人信息，包括姓名、昵称、年级、学院、专业、报名方向、GitHub链接和个人简介。

#### Scenario: 编辑个人信息
- **WHEN** 用户在"个人信息"Tab修改表单并点击保存
- **THEN** 系统更新用户信息并显示保存成功提示

#### Scenario: 学号不可修改
- **WHEN** 用户查看个人信息表单
- **THEN** 学号字段显示为禁用状态，不可编辑

#### Scenario: 必填字段验证
- **WHEN** 用户提交表单时缺少必填字段（姓名、年级、学院、专业、报名方向）
- **THEN** 系统显示字段验证错误提示

### Requirement: Tab导航展示
系统SHALL在右侧内容区顶部展示Tab导航，包含个人信息、我的考核、项目经历、竞赛经历、实习经历五个Tab。

#### Scenario: 展示Tab计数
- **WHEN** 页面加载完成
- **THEN** 各Tab显示对应数据的数量Badge（考核数、项目数、竞赛数、实习数）

#### Scenario: Tab切换
- **WHEN** 用户点击某个Tab
- **THEN** URL更新为 `?tab=<tab_name>`
- **AND** 对应Tab内容区域显示

### Requirement: 考核列表展示
系统SHALL在"我的考核"Tab展示用户参与的考核列表，每个考核显示状态、时间、题目数量和进度。

#### Scenario: 展示进行中的考核
- **WHEN** 考核状态为"进行中"
- **THEN** 卡片显示：
  - 考核名称和轮次
  - "进行中"状态标签（蓝紫渐变）
  - 考核时间范围
  - 剩余时间
  - 题目总数
  - 完成进度条
  - "继续答题"操作入口

#### Scenario: 展示已结束的考核
- **WHEN** 考核状态为"已结束"
- **THEN** 卡片显示：
  - "已结束"状态标签（绿色）
  - 最终得分
  - "查看详情"操作入口

#### Scenario: 展示未开始的考核
- **WHEN** 考核状态为"未开始"
- **THEN** 卡片显示：
  - "未开始"状态标签（灰色）
  - 距开始时间
  - "暂不可进入"提示

### Requirement: 邮箱显示（只读）
系统SHALL在个人信息Tab显示当前绑定的邮箱，但暂不支持修改功能。

#### Scenario: 展示已验证邮箱
- **WHEN** 用户查看邮箱设置区域
- **THEN** 显示当前邮箱和"已验证"状态
- **AND** "修改邮箱"按钮显示但处于禁用状态

### Requirement: 前端可获取当前用户信息
前端 SHALL 通过 `GET /api/v1/user/info` 获取当前登录用户的基本信息。

#### Scenario: 成功获取用户信息
- **WHEN** 已登录用户访问个人主页
- **THEN** 系统调用 `getUserInfo()` API 获取用户信息
- **AND** 页面展示真实的用户数据

#### Scenario: 未登录用户访问
- **WHEN** 未登录用户访问个人主页
- **THEN** 系统重定向到登录页

### Requirement: 前端可更新用户信息
前端 SHALL 通过 `PUT /api/v1/user/info` 更新用户信息，根据用户角色控制可修改字段。

#### Scenario: CANDIDATE 更新基本信息
- **WHEN** CANDIDATE 角色用户修改昵称或个人简介
- **THEN** 系统调用 `updateProfile()` API
- **AND** 更新成功后显示成功提示

#### Scenario: MEMBER 更新扩展信息
- **WHEN** MEMBER 及以上角色用户修改用户名、性别、学院、专业或方向
- **THEN** 系统调用 `updateProfile()` API
- **AND** 更新成功后显示成功提示

#### Scenario: CANDIDATE 尝试修改受限字段
- **WHEN** CANDIDATE 角色用户尝试修改用户名、性别、学院、专业或方向
- **THEN** 前端禁止编辑这些字段（输入框禁用）

### Requirement: 前端可获取 Tab 计数
前端 SHALL 通过 `GET /api/v1/user/tab-counts` 获取 Tab 计数。

#### Scenario: 显示 Tab 计数
- **WHEN** 用户访问个人主页
- **THEN** 系统调用 `getTabCounts()` API
- **AND** Tab 标签显示对应的数据计数
