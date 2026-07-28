## Purpose

定义团队成就展示功能，支持访客浏览团队竞赛获奖、论文、专利等官方成就，并按年份与奖项级别筛选。
## Requirements
### Requirement: 用户可以查看团队成就列表

系统SHALL允许用户浏览团队在各竞赛中获得的奖项信息，包括竞赛名称、Logo、获奖等级、获奖年份、获奖人数、关联成员等。

#### Scenario: 访问成就页面
- **WHEN** 用户访问 `/achievements` 页面
- **THEN** 系统显示成就列表，按年份倒序排列

#### Scenario: 成就卡片展示
- **WHEN** 成就列表加载完成
- **THEN** 每个成就卡片显示竞赛Logo、竞赛名称、奖项等级标签、获奖年份、获奖人数
- **AND** 显示系统内成员头像缩略图和外部协作者数量标签

### Requirement: 用户可以筛选成就

系统SHALL支持用户按年份和奖项级别筛选成就列表。

#### Scenario: 按年份筛选
- **WHEN** 用户选择特定年份
- **THEN** 系统仅显示该年份的获奖记录

#### Scenario: 按奖项级别筛选
- **WHEN** 用户选择奖项级别（国家级/省级/校级）
- **THEN** 系统仅显示该级别的获奖记录

#### Scenario: 组合筛选
- **WHEN** 用户同时选择年份和奖项级别
- **THEN** 系统显示同时满足两个条件的获奖记录

### Requirement: 用户可以查看成就统计

系统SHALL在页面顶部展示成就统计数据，包括总获奖数、各级别奖项数量。

#### Scenario: 统计数据展示
- **WHEN** 用户访问成就页面
- **THEN** 系统显示总获奖数、国家级奖项数、省级奖项数、校级奖项数

### Requirement: 成就页面支持响应式布局

系统SHALL支持桌面端和移动端两种布局。

#### Scenario: 桌面端布局
- **WHEN** 用户使用桌面设备访问（宽度 >= 1024px）
- **THEN** 成就卡片以多列网格形式展示

#### Scenario: 移动端布局
- **WHEN** 用户使用移动设备访问（宽度 < 768px）
- **THEN** 成就卡片以单列形式展示，筛选功能折叠显示

### Requirement: 成就列表API

系统SHALL提供公开的成就列表查询接口。

#### Scenario: 获取成就列表
- **WHEN** 客户端请求 `GET /api/v1/achievements`
- **THEN** 系统返回成就列表，包含关联的竞赛信息
- **AND** 返回数据中包含 `members` 和 `externalMembers` 字段

#### Scenario: 带筛选条件查询
- **WHEN** 客户端请求 `GET /api/v1/achievements?year=2024&level=NATIONAL`
- **THEN** 系统返回符合条件的成就列表

### Requirement: 成就统计API

系统SHALL提供公开的成就统计接口。

#### Scenario: 获取统计数据
- **WHEN** 客户端请求 `GET /api/v1/achievements/stats`
- **THEN** 系统返回统计数据，包含总数和各级别数量

