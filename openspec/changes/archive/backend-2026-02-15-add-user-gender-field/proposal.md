## Why

当前用户表缺少性别字段，无法满足系统对用户基础信息的完整采集需求。许多业务场景（如用户画像分析、个性化推荐、统计报表等）都需要基于用户性别进行数据筛选和分析。添加性别字段是完善用户基础信息体系的必要步骤。

## What Changes

- **数据库层**: 在 `users` 表中添加 `gender` 字段，类型为枚举（男/女/未知），默认值为"未知"
- **后端DTO**: 修改用户相关的DTO（Data Transfer Object），添加性别字段及校验规则
- **后端VO**: 修改用户相关的VO（View Object），在响应中返回性别字段
- **枚举定义**: 新增或更新性别枚举（GenderEnum），支持数据库枚举值映射
- **API文档**: 更新相关API接口文档，说明性别字段的取值范围和格式

## Capabilities

### New Capabilities
<!-- 没有新增能力，只是修改现有能力 -->

### Modified Capabilities
- `user-management`: 在用户实体数据结构需求中添加性别字段要求，包括：
  - 性别字段必须支持三种值：male（男）、female（女）、unknown（未知）
  - 数据库中存储为 lowercase snake_case 格式
  - 性别字段默认值为 unknown
  - DTO和VO需要同步更新以支持性别字段的传输和展示

## Impact

- **数据库**: `users` 表结构变更，需要迁移脚本
- **后端代码**:
  - Entity: User实体类添加gender字段
  - DTO: UserCreateDTO, UserUpdateDTO, UserQueryDTO等
  - VO: UserVO, UserDetailVO等
  - Enum: 新增或更新Gender枚举类
- **API接口**: 涉及用户创建、更新、查询的所有接口
- **前端**: 需要同步更新表单和展示逻辑（不在本次后端变更范围内）
