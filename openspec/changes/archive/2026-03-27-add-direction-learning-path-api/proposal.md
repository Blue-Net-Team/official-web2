## Why

方向介绍页面的学习路径数据目前硬编码在前端 `data.ts` 中，`videoLink` 字段为空字符串，无法动态管理视频链接。管理员需要能够通过后台维护各方向的学习路径视频链接，而无需修改代码重新部署。

## What Changes

- 新增 `tb_direction_learning_step` 数据库表，存储各方向的学习路径步骤信息
- 新增公开 API `GET /api/v1/directions/{slug}/learning-path`，供前端服务端组件获取学习路径数据
- 新增管理 API，支持管理员对学习路径步骤进行 CRUD 操作
- 新增权限定义，控制学习路径管理功能的访问权限
- 初始化默认学习路径数据（三个方向各 4 个步骤）

## Capabilities

### New Capabilities

- `direction-learning-path`: 方向学习路径管理功能，包括数据存储、公开 API 和管理 API

### Modified Capabilities

无

## Impact

**数据库**：
- 新增 `tb_direction_learning_step` 表
- 新增学习路径管理相关权限记录

**API 接口**：
- 新增公开接口：`GET /api/v1/directions/{slug}/learning-path`
- 新增管理接口：`POST/PUT/DELETE /api/v1/admin/directions/learning-steps`

**代码结构**：
- 新增 Entity: `DirectionLearningStep`
- 新增 VO: `LearningStepVO`
- 新增 DTO: `LearningStepDTO`, `CreateLearningStepRequestDTO`, `UpdateLearningStepRequestDTO`
- 新增 Repository: `LearningPathRepository`
- 新增 DomainService: `LearningPathDomainService`
- 新增 ApplicationService: `LearningPathService`
- 新增 Controller: `LearningPathController`, `AdminLearningPathController`

**依赖关系**：
- 复用现有 `Direction` 枚举
- 遵循 DDD 四层架构规范
