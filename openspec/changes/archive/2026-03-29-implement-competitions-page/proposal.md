## Why

需要实现团队竞赛列表展示页面，用于展示蓝网团队参加过的各类竞赛信息。该页面将帮助访客了解团队的技术实力和竞赛成果，提升团队形象。设计稿已完成，需要将其转化为可交互的前端页面。

## What Changes

### 后端变更
- 扩展 `tb_competition` 表，添加新字段：
  - `level` (VARCHAR): 竞赛级别，枚举值：国家级/省级
  - `month` (VARCHAR): 举办时间（月份），如 "5月"
  - `organizer` (VARCHAR): 主办单位，可为空
  - `promo_image_file_id` (BIGINT): 宣传图片文件ID，用于卡片背景
- 更新 `CompetitionBriefDTO`，添加新字段
- 更新 `CompetitionDetailDTO`，添加新字段
- 更新创建/更新竞赛的请求 DTO

### 前端变更
- 新增竞赛列表页面 (`/competitions`)
- 实现响应式布局，支持桌面端 (1440px) 和移动端 (375px)
- 展示竞赛卡片列表，每张卡片包含：
  - 竞赛名称
  - 竞赛级别标签（国家级/省级）
  - 举办时间（月份）
  - 主办单位（可为空，为空时不显示）
  - 竞赛简介
  - 宣传图片（第一张卡片使用背景+渐变，其他卡片不使用）
- 不使用 Tailwind CSS，使用 CSS Modules 或 styled-components
- 国家级标签使用橙色 (#E86835)，省级标签使用蓝色 (#4A90E2)

## Capabilities

### New Capabilities
- `competitions-page`: 竞赛列表展示页面，包含页面布局、竞赛卡片组件、响应式适配
- `competition-backend-extension`: 后端竞赛模块扩展，支持级别、时间、主办单位、宣传图片

### Modified Capabilities
- 无

## Impact

### 后端影响
- 数据库：新增字段的 Flyway 迁移脚本
- DTO：CompetitionBriefDTO、CompetitionDetailDTO、CreateCompetitionRequestDTO、UpdateCompetitionRequestDTO
- Entity：Competition 实体类
- Converter：CompetitionConverter 转换逻辑
- Service：CompetitionService 业务逻辑

### 前端影响
- 路由：新增 `/competitions` 路由
- 组件：`src/app/competitions/page.tsx` 及子组件
- 样式：使用 CSS Modules（不使用 Tailwind CSS）
- API 调用：使用后端接口获取竞赛列表
