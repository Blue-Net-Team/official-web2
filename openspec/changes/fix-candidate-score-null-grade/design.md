## Context

`AssessmentJudgementMapper.xml` 的 `selectCandidateScoreRows` 查询用于「人员视图」的考生评分矩阵。该查询先从 `tb_user` 中筛选候选考生（`candidate_users` CTE），再关联题目和评分记录。

筛选条件：
```sql
AND u.direction = t.direction
AND COALESCE(u.assessment_grade_year, ...) = t.grade
```

当考核时间的 `grade` 为 `null`（不限年级）或 `direction` 为 `null`（全局考核）时，PostgreSQL 的 `NULL = NULL` 返回 `UNKNOWN`，在 WHERE 中视为 false，导致 `candidate_users` 为空。

## Goals / Non-Goals

**Goals:**
- 修复 `grade=null` 时人员视图无法显示考生的 bug
- 同时确保 `direction=null`（全局考核）时也能正常工作
- 补充集成测试覆盖 null 场景

**Non-Goals:**
- 修改题目视图的 SQL（其 SQL 不预先过滤考生范围，不受此影响）
- 修改前端代码
- 修改 API 接口契约

## Decisions

**Decision: SQL 中使用 `IS NULL OR =` 模式处理 null**

参考 `AssessmentTimeMapper.xml:82-83` 中已有的处理方式：
```sql
(t.direction = #{direction} AND (t.grade = #{enrollmentYear} OR t.grade IS NULL))
OR (t.direction IS NULL AND (t.grade = #{enrollmentYear} OR t.grade IS NULL))
```

在 `candidate_users` CTE 中采用等价的条件：
```sql
AND (t.direction IS NULL OR u.direction = t.direction)
AND (t.grade IS NULL OR COALESCE(...) = t.grade)
```

**理由**：
- 语义清晰：`null` 表示"不限"，匹配所有值
- 与项目中 `AssessmentTimeMapper.xml` 的处理方式保持一致
- 无需引入动态 SQL（`<if>`），CTE 中直接处理即可

## Risks / Trade-offs

- **[Risk] 方向管理员查看全局考核时能看到所有方向的考生** → 这是预期行为（全局考核面向所有方向），且访问控制由 `AssessmentJudgementAccessGuard` 在应用层处理
- **[Risk] `COALESCE` 在学生 ID 不符合 `^\d{4}` 模式时返回 null，与 `t.grade IS NULL` 条件叠加可能扩大匹配范围** → 该行为与修复前一致，修复仅改变 null 比较逻辑，不改变 `COALESCE` 本身的计算方式
