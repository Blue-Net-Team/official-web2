## Context

成就模块目前已经实现了公开的成就展示功能，包括：
- 成就列表查询API (`GET /api/v1/achievements`)
- 成就统计API (`GET /api/v1/achievements/stats`)
- 前端公开成就展示页面 (`/achievements`)
- 数据库表 `tb_achievement` 包含必要字段（id, title, type, relate_to, achieve_at, award_level, award_name, file_id）

系统采用DDD四层架构（接口层、应用层、领域层、基础设施层），权限控制使用 `@RequiresPermission` 注解，访问级别包括 PUBLIC、AUTHENTICATED、PROTECTED。

竞赛管理模块已存在，提供了完整的管理CRUD接口（`AdminCompetitionController`），可作为成就管理的参考实现。

## Goals / Non-Goals

**Goals:**
1. 为超级管理员提供成就的完整CRUD操作接口
2. 集成到现有管理后台，使用已有的管理员菜单项
3. 遵循项目现有的DDD架构和权限控制规范
4. 确保数据一致性验证（枚举值验证、文件ID验证、业务规则验证）
5. 页面风格与现有管理界面保持一致

**Non-Goals:**
1. 用户成就关联管理（暂时不需要）
2. 批量导入功能
3. 成就审核流程
4. 数据库表结构修改（现有表结构已满足需求）
5. 公开成就查询接口的修改（保持原有功能不变）

## Decisions

1. **API路径与权限设计**
   - 管理接口路径：`/api/v1/admin/achievements`，与竞赛管理保持一致
   - 权限标识：`achievement:create`、`achievement:update`、`achievement:delete`
   - 访问级别：`AccessLevel.PROTECTED`（需要超级管理员角色）
   - 公开查询接口权限保持不变：`achievement:list`、`achievement:stats` 保持 `AccessLevel.PUBLIC`

2. **参考模型选择**
   - 完全参照竞赛管理模块 (`AdminCompetitionController`) 的实现模式
   - 理由：保持一致性，减少认知负担，已有经过验证的设计模式

3. **DTO设计**
   - 创建请求DTO：`CreateAchievementRequestDTO`
   - 更新请求DTO：`UpdateAchievementRequestDTO`
   - 响应DTO：复用现有的 `AchievementDTO`
   - 与竞赛管理使用相同的DTO命名模式和验证注解

4. **业务规则验证**
   - `type` 必须是有效的 `AchievementType` 枚举值
   - 如果 `type = COMPETITION`，`awardLevel` 和 `awardName` 必填
   - `achieveAt` 不能是未来日期
   - `fileId` 必须存在且类型为 `NORMAL_IMG`（参考竞赛管理的 `validateFileId` 方法）
   - `relateTo` 格式验证（竞赛名称/期刊名称）

5. **前端集成策略**
   - 使用现有管理菜单配置（已在 `menuConfig` 中配置，权限等级3）
   - 页面组件结构参照竞赛管理页面 (`/admin/competition`)
   - 使用抽屉表单进行创建/编辑操作
   - API服务模式参照 `admin-competition.service.ts`

6. **领域服务设计**
   - 新增 `AchievementDomainService` 处理领域逻辑
   - 扩展现有 `AchievementRepository` 接口，增加 `save`、`update`、`deleteById` 方法
   - 保持与现有成就查询逻辑的分离

## Risks / Trade-offs

1. **文件验证风险**
   - 风险：`fileId` 验证可能不完整，导致关联不存在的文件
   - 缓解：复用竞赛管理中的 `validateFileId` 方法，确保文件存在且类型正确

2. **枚举值转换风险**
   - 风险：DTO中的字符串枚举值转换为Java枚举时可能大小写不敏感问题
   - 缓解：使用统一的枚举转换工具方法，参考竞赛管理的 `parseLevel` 方法

3. **竞赛名称关联风险**
   - 风险：成就通过 `relate_to` 字段与竞赛名称关联，不是外键约束，可能存在数据不一致
   - 缓解：添加业务逻辑验证，确保关联的竞赛名称存在（通过查询 `tb_competition` 表）

4. **权限控制遗漏风险**
   - 风险：忘记为管理接口添加 `@RequiresPermission` 注解
   - 缓解：参考现有管理接口的模式，确保所有接口都有正确的权限控制

5. **前端表单验证风险**
   - 风险：前端验证与后端验证不一致
   - 缓解：使用相同的验证规则，前端使用Ant Design表单验证，后端使用Jakarta Validation注解