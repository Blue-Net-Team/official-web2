## Context

学院(College)是用户和报名信息的关联实体，用于标识用户所属学院。当前系统已有 `tb_college` 表和基础的 `College` 实体、`CollegeMapper`，但缺少完整的DDD四层架构实现和REST API接口。

现有代码结构：
- 实体层：`College.java` - 包含 `id` 和 `name` 字段
- 仓库层：`CollegeMapper.java` - 仅继承 `BaseMapper<College>`
- 数据库表：`tb_college` - 无软删除字段（参考数据不需要软删除）

## Goals / Non-Goals

**Goals:**
- 实现完整的DDD四层架构（控制层、应用层、领域层、仓库层）
- 提供学院CRUD的REST API接口
- 遵循项目现有的代码规范和架构模式
- 支持公开接口获取学院列表（用于报名和用户信息选择）
- 支持管理员权限的学院管理操作

**Non-Goals:**
- 不实现学院的软删除（参考数据不需要软删除）
- 不实现学院的复杂业务逻辑（仅简单CRUD）
- 不修改现有的数据库表结构

## Decisions

### 1. API路径设计

**决策**: 采用与竞赛模块类似的路径设计
- 公开接口：`/api/v1/colleges` - 获取学院列表
- 管理接口：`/api/v1/admin/colleges` - 创建、更新、删除学院

**理由**: 遵循项目现有的API设计规范，公开接口无前缀，管理接口使用 `/admin` 前缀

### 2. 权限设计

**决策**:
- `college:list` - 获取学院列表（公开）
- `college:create` - 创建学院（管理员）
- `college:update` - 更新学院（管理员）
- `college:delete` - 删除学院（管理员）

**理由**: 遵循项目现有的权限命名规范

### 3. DTO设计

**决策**:
- `CollegeDTO` - 学院信息（id, name）
- `CreateCollegeRequestDTO` - 创建学院请求（name）
- `UpdateCollegeRequestDTO` - 更新学院请求（name）

**理由**: 学院实体简单，仅需名称字段

### 4. VO设计

**决策**: `CollegeVO` - 包含 `id` 和 `name` 字段

**理由**: 与实体结构一致，用于领域层与应用层之间的数据传递

## Risks / Trade-offs

### 风险：学院被删除后影响关联用户

**缓解措施**:
- 删除学院前检查是否有关联的用户或报名记录
- 如有关联记录，拒绝删除并返回错误信息
