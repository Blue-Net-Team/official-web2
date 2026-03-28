## Why

团队需要一个集中展示竞赛获奖成果的页面，用于对外展示团队实力和成就，同时方便成员了解团队历史荣誉。当前系统已有 `tb_achievement` 表，但缺少竞赛获奖所需的字段（奖项级别、获奖名称、获奖人数等），无法满足设计稿展示需求。

## What Changes

- 扩展现有 `tb_achievement` 表，添加竞赛获奖所需字段（competition_id, award_level, award_name, winner_count）
- 新增团队成就展示页面，支持桌面端和移动端响应式布局
- 新增成就卡片组件，展示竞赛名称、Logo、获奖等级、年份、获奖人数等信息
- 新增筛选功能，支持按年份、奖项级别（国家级/省级/校级）筛选
- 新增统计数据展示区域，显示总获奖数、各级别奖项数量
- 新增后端 API 接口，支持成就数据的查询和筛选

## Capabilities

### New Capabilities

- `team-achievements`: 团队成就展示功能，包含成就列表查询、筛选、统计展示等能力

### Modified Capabilities

- 无

## Impact

- **数据库**: 扩展 `tb_achievement` 表，添加 4 个新字段
- **后端**: 扩展 Achievement 实体，新增 AwardLevel 枚举，新增成就查询 API
- **前端**: 新增 `/achievements` 路由页面，新增成就卡片组件、筛选组件
