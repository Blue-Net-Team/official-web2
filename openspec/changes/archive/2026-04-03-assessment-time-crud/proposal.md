## Why

系统当前缺少考核时间的管理接口。`tb_assessment_time` 表和基础领域模型（Entity、VO、Mapper）已存在，但没有完整的 CRUD API。管理员无法通过后端接口创建、查询、更新、删除考核时间配置，考生和成员也无法根据权限查看相应的考核时间信息。

## What Changes

- 在 `tb_assessment_time` 表新增 `grade` 字段（INTEGER，表示大几：1=大一，2=大二，3=大三），实现按届次独立配置考核时间
- 新建唯一约束：`direction + epoch + grade` 组合唯一
- 实现考核时间的完整 CRUD 接口（后端），遵循 DDD 四层架构
- 实现基于角色的查询过滤（考生看自己方向+年级、成员看自己方向全部年级、方向管理员以上看全部）
- 删除时检查是否有关联题目，有则返回 409 Conflict
- 更新时校验：已开始的考核不可修改 startTime，但可修改 endTime
- 创建时校验：startTime < endTime，timeLimit=true 时 timeLimitMinutes 必填
- 分页查询，默认每页 5 条

## Capabilities

### New Capabilities

- `assessment-time-management`: 考核时间管理 CRUD，包含创建、分页查询（角色过滤）、更新（部分字段锁定校验）、删除（关联检查）

### Modified Capabilities

## Impact

- **数据库**: 新增 Flyway 迁移脚本，为 `tb_assessment_time` 添加 `grade` 列和唯一约束
- **后端 API**: 新增 `/api/v1/admin/assessment-times` 管理 CRUD 接口，新增 `/api/v1/assessment-times` 查询接口
- **后端代码**: 新增 AssessmentTime 的完整 DDD 四层实现（Repository、DomainService、ApplicationService、Converter、Controller、DTO）
- **依赖关系**: `AssessmentQuestionMapper` 需要用于关联检查（删除时判断是否有题目引用该考核时间）
