## Why

成就模块目前只有公开的查询接口，缺乏管理员管理功能。超级管理员需要创建、更新、删除成就的能力，以维护团队获奖记录。当前系统已经实现了成就的展示和统计功能，但缺乏完整的管理后台支持，无法通过管理界面维护成就数据。

## What Changes

- 添加管理员专用的成就管理API接口，支持创建、更新、删除成就操作
- 扩展现有的成就应用服务和服务接口，增加管理功能
- 创建成就管理前端页面，集成到现有管理后台
- 使用现有管理员菜单项（已配置`/admin/achievement`路由，权限等级3），确保只有超级管理员可访问
- 遵循项目现有的DDD四层架构模式和权限控制规范
- 页面风格与现有管理界面保持一致（Ant Design + 抽屉表单）

## Capabilities

### New Capabilities

- `achievement-management`: 成就管理能力，为超级管理员提供成就的创建、更新、删除功能，包括后端API和前端管理界面

### Modified Capabilities

- 无（现有`team-achievements`能力保持不变，仅涉及公开查询功能）

## Impact

- **后端API**: 新增`AdminAchievementController`，扩展`AchievementService`接口和实现，新增`AchievementDomainService`
- **前端**: 新增`/admin/achievement`页面组件和API服务
- **权限系统**: 新增`achievement:create`、`achievement:update`、`achievement:delete`权限标识，与现有的`achievement:list`、`achievement:stats`保持一致
- **数据库**: 无需修改现有表结构（`tb_achievement`表已包含所有必要字段）
- **集成点**: 与现有的文件上传系统集成（成就图片文件验证）