## Why

当前系统缺少学院管理接口，用户报名和用户信息中需要关联学院信息，但无法通过API进行学院的增删查改操作。需要实现学院管理接口以支持完整的用户和报名流程。

## What Changes

- 新增学院管理API，支持学院的增删查改操作
- 新增公开接口：获取学院列表
- 新增管理接口：创建学院、更新学院、删除学院
- 遵循DDD四层架构设计，实现控制层、应用层、领域层、仓库层

## Capabilities

### New Capabilities

- `college-api`: 学院管理接口，提供学院的增删查改功能，包括公开的学院列表查询和管理员权限的CRUD操作

### Modified Capabilities

- `college-management`: 更新学院管理规范，增加API层面的需求定义

## Impact

- 新增控制层：`CollegeController` 和 `AdminCollegeController`
- 新增应用层：`CollegeService` 和 `CollegeServiceImpl`
- 新增领域层：`CollegeDomainService` 和 `CollegeDomainServiceImpl`
- 新增仓库层：`CollegeRepository` 接口和 `CollegeRepositoryImpl` 实现
- 新增DTO：学院相关的请求和响应DTO
- 新增VO：`CollegeVO`
- 新增权限：学院管理相关权限
