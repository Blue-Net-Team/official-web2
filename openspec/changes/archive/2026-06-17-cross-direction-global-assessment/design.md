## Context

当前考核时间表 `tb_assessment_time` 按 `(direction, epoch, grade)` 三维拆分，每个组合独立配置。数据库 `direction` 字段已允许为 null，但 `grade` 为 `NOT NULL`，且应用层代码假设所有字段非空（`@NotNull` 校验、`validateDirectionPermission` 未处理 null、`existsByDirectionEpochGrade` 对 null 匹配失败等）。导致跨方向跨年级的全局考核在系统层面无法创建和使用。

业务上，"最终考核"需要所有方向、所有年级的考生共用同一套题目、同一时间段，且可跨方向组队。

## Goals / Non-Goals

**Goals:**
- SUPER_ADMIN 可创建 `direction = null, grade = null` 的全局考核
- 全局考核对所有考生（不限方向、不限年级）可见
- 全局考核的题目、答题、评审、组队均跳过方向/年级校验
- 全局考核的最终轮次判定不受方向限制

**Non-Goals:**
- 不引入 `isGlobal` 等新字段，复用现有 nullable 字段语义
- 不改动 DIRECTION_ADMIN 对自有方向考核的管理权限
- 不涉及算法题判题服务的跨方向改造
- 不改动已有的报名、成员、成就等模块

## Decisions

### 1. 复用 nullable 字段而非新增 isGlobal 标记

**选择**：`direction = null` 表示跨方向，`grade = null` 表示跨年级。

**理由**：数据库已有 nullable 设计，复用可避免新增字段。组合语义清晰：`(null, null)` 为全局考核，`(null, 2025)` 为 2025 级所有方向，`(cv, null)` 为 CV 方向所有年级。

### 2. 唯一性校验：方向相关 vs 全局独立

**选择**：当 `direction != null` 时按现有 `(direction, epoch, grade)` 去重；当 `direction = null` 时按 `(epoch, grade)` 去重（新增 `countByEpochGrade` 方法）。

**理由**：`UNIQUE (direction, epoch, grade)` 约束在 PostgreSQL 中对 `(null, ...)` 行不生效（null != null），必须靠应用层保证唯一性。

### 3. 权限：仅 SUPER_ADMIN 可创建全局考核

**选择**：`validateDirectionPermission` 中，当 `targetDirection == null` 时只允许 SUPER_ADMIN。

**理由**：全局考核涉及所有方向，DIRECTION_ADMIN 只管理本方向，无权创建跨方向考核。但评审和决策时，DIRECTION_ADMIN 可以参与（因为题目需要各方向管理员共同评分）。

### 4. 评审权限：全局考核跳过方向校验

**选择**：`AssessmentJudgementAccessGuard.assertAssessmentTimeScope` 中，当 `assessmentTime.getDirection() == null` 时跳过方向校验。

**理由**：全局考核没有"本方向"的概念，所有方向管理员和成员都应可评审。

### 5. 最终轮次判定：动态条件 SQL

**选择**：修改 Mapper XML 中的 `selectMaxEpoch` 为动态 `WHERE` 条件，根据 direction/grade 是否 null 组装查询。

**理由**：避免新增多个 `findMaxEpochByXxx` 方法，单一方法覆盖全部组合。

### 6. 前端："全局"选项的值表示

**选择**：前端 Select 中 value = `"GLOBAL"` 作为特殊标记，提交时转换为 `null`。

**理由**：Ant Design Select 组件 Option value 不支持 null，TypeScript 枚举也需与 `undefined` 区分。前端类型扩展为 `Direction | 'GLOBAL'`。

## Risks / Trade-offs

- [兼容性] 现有 `grade INTEGER NOT NULL DEFAULT 1` 数据不受影响，迁移只去掉 NOT NULL 不改默认值
- [查询性能] `selectPageByUserParticipation` 增加 `OR t.grade IS NULL` 条件，但 grade 和 direction 都有索引，影响可忽略
- [数据一致性] 应用层唯一性校验依赖 Java 代码，分布式部署下需注意并发创建问题（锁或悲观插入）

## Migration Plan

1. 执行数据库迁移 V15：`ALTER TABLE tb_assessment_time ALTER COLUMN grade DROP NOT NULL;`
2. 发布后端代码改动
3. 发布前端代码改动
4. 无数据回迁需求
