## 1. 数据库迁移

- [x] 1.1 创建 Flyway 迁移脚本 `V2x__change_grade_to_enrollment_year.sql`，将 `tb_assessment_time.grade` 的值从年级序号（1/2/3）转换为入学年份，并更新列注释
- [x] 1.2 验证唯一约束 `uk_assessment_time_direction_epoch_grade` 在数据迁移后仍然有效

## 2. GradeCalculator 重构

- [x] 2.1 修改 `GradeCalculator`：将 `calculateGrade()` 方法语义从"计算年级序号"改为"提取入学年份"，或直接在调用处改用 `extractEnrollmentYear()`
- [x] 2.2 更新 `listAssessmentTimes()` 中 CANDIDATE 分支：将 `GradeCalculator.calculateGrade()` 调用改为 `GradeCalculator.extractEnrollmentYear()`

## 3. Repository 层新增查询方法

- [x] 3.1 在 `AssessmentTimeRepository` 接口新增 `findByUserParticipation(Long userId, Direction direction, Integer enrollmentYear, Pageable pageable)` 方法
- [x] 3.2 在 `AssessmentTimeRepositoryImpl` 实现该方法，使用 MyBatis-Plus 构建查询：`(direction = ? AND grade = ?) OR EXISTS (answer 子查询)`，按 id DESC 排序

## 4. Service 层重写 listAssessmentTimesForUser()

- [x] 4.1 重写 `AssessmentTimeServiceImpl.listAssessmentTimesForUser()`：不再调用 `listAssessmentTimes()`，改为调用新的 `findByUserParticipation()` 方法
- [x] 4.2 保留现有的 `completedQuestions` / `totalQuestions` 进度填充逻辑
- [x] 4.3 处理学号无法提取入学年份的边界情况（仅返回有 answer 的考核）

## 5. Admin 端适配

- [x] 5.1 修改 Admin 创建考核时间的 DTO：`grade` 字段校验从 1/2/3 改为有效入学年份范围
- [x] 5.2 修改前端 Admin 考核时间管理页面的年级输入：从下拉选择 1/2/3 改为年份输入/选择器

## 6. 测试

- [x] 6.1 为 `findByUserParticipation()` 编写 Repository 层单元测试
- [x] 6.2 为 `listAssessmentTimesForUser()` 编写 Service 层测试，覆盖所有角色场景（CANDIDATE、MEMBER、ADMIN、学号异常）
- [x] 6.3 验证管理端 `listAssessmentTimes()` 不受影响
