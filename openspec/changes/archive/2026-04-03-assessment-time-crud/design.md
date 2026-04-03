## Context

当前系统已有 `tb_assessment_time` 表和基础领域模型（Entity、VO、Mapper），但缺少完整的 DDD 四层 CRUD 实现。管理员无法通过 API 管理考核时间配置。

现有表结构包含 `direction`、`epoch`、`start_time`、`end_time`、`time_limit`、`time_limit_minutes` 字段，但缺少 `grade`（年级）字段来区分不同届次的考核配置。

系统角色层级：SUPER_ADMIN > DIRECTION_ADMIN > MEMBER > CANDIDATE。当前用户信息通过 `UserCTX` 获取，用户的 `studentId` 前4位为入学年份，可动态计算年级。

## Goals / Non-Goals

**Goals:**
- 为 `tb_assessment_time` 添加 `grade` 字段，支持按届次独立配置考核时间
- 实现考核时间的完整 CRUD 后端接口，遵循 DDD 四层架构
- 实现基于角色的查询过滤：考生看自己方向+年级、成员看自己方向全部、方向管理员以上看全部
- 实现创建/更新时的业务校验
- 实现删除时的关联题目检查（返回 409）
- 分页查询，默认每页 5 条

**Non-Goals:**
- 不实现前端管理页面（后续单独开发）
- 不实现考核时间冲突检测
- 不修改现有考核题目、答案等关联功能的逻辑

## Decisions

### 1. grade 字段设计

**决定**：在 `tb_assessment_time` 新增 `grade` 列（INTEGER，NOT NULL），取值 1/2/3 表示大一/大二/大三。

**唯一约束**：`(direction, epoch, grade)` 组合唯一。

**理由**：每届考核独立配置，同方向同轮次不同年级有不同的考核时间。用数字而非入学年份表示年级，使查询更直观（考生根据自己年级直接匹配）。

### 2. 年级计算方式

**决定**：从当前用户 `studentId` 前4位提取入学年份，与当前日期对比计算年级。以每年9月1日为分界线。

**计算逻辑**：
```
currentYear = now().year
currentMonth = now().monthValue
if currentMonth >= 9:
    referenceYear = currentYear
else:
    referenceYear = currentYear - 1
grade = referenceYear - enrollmentYear + 1
```

**放置位置**：在领域层创建工具方法 `GradeCalculator.calculateGrade(studentId)`，供 DomainService 和 ApplicationService 使用。

**理由**：年级是动态计算的值，不应持久化。集中在一处避免重复逻辑。

### 3. API 路径设计

**管理接口**（需要 `assessment-time:*` 权限）：
- `POST /api/v1/admin/assessment-times` — 创建
- `PUT /api/v1/admin/assessment-times/{id}` — 更新
- `DELETE /api/v1/admin/assessment-times/{id}` — 删除
- `GET /api/v1/admin/assessment-times` — 管理员分页查询（角色过滤）

**查询接口**（AUTHENTICATED 级别）：
- `GET /api/v1/assessment-times` — 考生/成员查看自己的考核时间

**理由**：管理操作和查看操作分属不同权限级别，分开路径更清晰。

### 4. 角色查询过滤策略

**决定**：在 ApplicationService 层根据当前用户角色设置查询参数，传递给 Repository 层执行。

| 角色 | 过滤条件 | 说明 |
|------|---------|------|
| CANDIDATE | direction + grade | 只看自己方向和年级 |
| MEMBER | direction | 看自己方向全部年级 |
| DIRECTION_ADMIN+ | 无过滤 | 看全部 |

**理由**：过滤逻辑属于应用层编排，Repository 只负责执行查询条件。

### 5. 已开始考核的更新策略

**决定**：在 DomainService 校验。如果 `startTime <= now()`，则禁止修改 `startTime`，但允许修改 `endTime`。校验在更新前执行，通过比较数据库中当前值与新值判断。

**理由**：考核已开始后修改开始时间会导致时间线混乱，但结束时间可能需要延长。

### 6. 删除关联检查

**决定**：在 DomainService 中检查 `tb_assessment_question` 是否有记录引用该 `assessment_time_id`。如果存在关联，抛出异常，Controller 层捕获并返回 409 Conflict。

**理由**：有题目的考核时间不能删除，需要先删除题目。409 Conflict 语义最准确（资源冲突）。

## Risks / Trade-offs

- **[Grade 计算边界]** 9月1日分界线可能与实际开学日期不完全一致 → 可接受，全校统一以9月为准
- **[并发创建冲突]** 同方向+轮次+年级可能被并发创建 → 依赖数据库唯一约束保障，Service 层捕获唯一约束异常转为友好提示
- **[关联题目检查性能]** 删除时需查询题目表 → 通过 `assessment_time_id` 索引已存在，查询高效
