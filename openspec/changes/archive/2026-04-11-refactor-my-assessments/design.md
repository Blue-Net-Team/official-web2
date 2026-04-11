## Context

个人主页"我的考核"tab 调用 `GET /api/v1/assessment-times`，底层复用管理端的 `listAssessmentTimes()` 方法进行角色权限过滤（ADMIN 看全部、MEMBER 按方向、CANDIDATE 按方向+年级），然后仅额外填充 `completedQuestions` 进度信息。

`tb_assessment_time.grade` 存储的是年级序号（1/2/3），`GradeCalculator` 从学号前4位提取入学年份后计算当前年级。这导致新一轮考核需要物理删除旧的考核时间+考题数据，晋级后的成员无法追溯历史考核记录。

## Goals / Non-Goals

**Goals:**
- "我的考核"按个人参与视角过滤：分配给我的（direction + grade 匹配）或我参与过的（有 answer 记录）
- 不同年份的考核数据可长期共存，不需要在新一轮开始时删除旧数据
- 所有角色在个人主页看到一致的"我的考核"逻辑

**Non-Goals:**
- 管理端的过滤逻辑保持不变（按角色权限过滤）
- 考核成绩/分数展示（后续扩展）
- 管理端考核列表的归档/筛选功能（后续按需添加）

## Decisions

### Decision 1: grade 语义从年级序号改为入学年份

**选择**: `tb_assessment_time.grade` 存储入学年份（如 2024），不再存储年级序号（1/2/3）。

**理由**: 入学年份是固定值，不随时间变化。不同年份的考核天然隔离，无需删除旧数据。2024 级的考核和 2025 级的考核可以共存于同一张表。

**备选方案**:
- 加"归档"标记：需要额外的字段和过滤逻辑，且 grade 的语义矛盾仍然存在
- 不改 grade，只改过滤逻辑：仍然需要在新一轮删除旧数据，无法保留历史

**影响**:
- `GradeCalculator.calculateGrade()` 不再被 `listAssessmentTimes()` 使用。用户端查询直接使用 `extractEnrollmentYear()` 获取入学年份做匹配
- Admin 创建考核时间时输入入学年份而非选择 1/2/3
- Flyway 迁移需要转换现有数据（根据创建时间和当前日期推算对应的入学年份，或直接清空现有数据）

### Decision 2: listAssessmentTimesForUser() 独立实现，不复用管理端方法

**选择**: `listAssessmentTimesForUser()` 不再调用 `listAssessmentTimes()`，独立实现过滤逻辑。

**理由**: 两个方法的过滤语义完全不同。管理端是"权限视角"（你能管理哪些），用户端是"参与视角"（哪些跟你有关）。共用方法导致逻辑耦合，改动相互影响。

**过滤条件**:
```sql
WHERE (direction = :myDirection AND grade = :myEnrollmentYear)
   OR EXISTS (
     SELECT 1 FROM tb_assessment_question q
     JOIN tb_assessment_answer a ON a.question_id = q.id
     WHERE q.assessment_time_id = at.id AND a.user_id = :myUserId
   )
```

`myEnrollmentYear` 从 `UserVO.studentId` 前4位提取，对所有角色统一。

**备选方案**:
- 在 `findByFilters()` 加参数控制：增加方法复杂度，违反单一职责
- 在应用层做二次过滤（先查全部再内存过滤）：数据量大时性能差

### Decision 3: Repository 层新增 findByUserId 方法

**选择**: 在 `AssessmentTimeRepository` 接口新增 `findByUserParticipation(Long userId, Direction direction, Integer enrollmentYear, Pageable pageable)` 方法。

**理由**: 组合条件 `(direction + grade) OR EXISTS answer` 无法通过简单的 `findByFilters` 参数叠加实现，需要自定义查询。使用 MyBatis-Plus 的 `LambdaQueryWrapper` + `inSql` 子查询实现 EXISTS 逻辑。

## Risks / Trade-offs

- **[Flyway 数据迁移]** 现有 `grade=1/2/3` 的数据需要转换 → 如果是开发阶段且数据不重要，可以直接清空重建；如果有生产数据，需要根据考核时间表的创建时间和方向信息推算入学年份
- **[GradeCalculator.calculateGrade() 废弃]** 该方法目前被 `listAssessmentTimes()` 中的 CANDIDATE 分支使用 → 改为入学年份后，`calculateGrade()` 在管理端也不需要了，管理端的 CANDIDATE 过滤改为 `grade = enrollmentYear`
- **[Admin 前端年级输入]** 从下拉选择 1/2/3 变为输入入学年份 → 建议使用年份选择器或当前年份附近的快速选项
