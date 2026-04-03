## 1. 数据库迁移

- [x] 1.1 创建 Flyway 迁移脚本：为 `tb_assessment_time` 添加 `grade` 列（INTEGER, NOT NULL）和 `(direction, epoch, grade)` 唯一约束

## 2. 领域层

- [x] 2.1 更新 `AssessmentTime` Entity：添加 `grade` 字段
- [x] 2.2 更新 `AssessmentTimeVO`：添加 `grade` 字段
- [x] 2.3 创建 `AssessmentTimeRepository` 接口（领域仓库）：定义 CRUD 方法 + 关联检查方法 + 分页查询方法
- [x] 2.4 创建 `AssessmentTimeDomainService` 接口和实现：包含创建校验（唯一性、时间合法性）、更新校验（已开始不可改 startTime）、删除校验（关联题目检查）
- [x] 2.5 创建 `GradeCalculator` 工具类：根据学号前4位和当前日期计算年级

## 3. 基础设施层

- [x] 3.1 创建 `AssessmentTimeRepositoryImpl`：MyBatis-Plus 实现，包含 Entity↔VO 转换、分页查询、方向/年级过滤、关联题目检查

## 4. 应用层

- [x] 4.1 创建 `AssessmentTimeConverter`：VO ↔ DTO 转换
- [x] 4.2 创建 `AssessmentTimeService` 接口和 `AssessmentTimeServiceImpl`：协调领域服务，实现角色过滤逻辑

## 5. 接口层

- [x] 5.1 创建 DTO 类：`AssessmentTimeDTO`、`CreateAssessmentTimeRequestDTO`、`UpdateAssessmentTimeRequestDTO`、`ResponseMessageAssessmentTimeList`
- [x] 5.2 创建 `AdminAssessmentTimeController`：管理端 CRUD（POST/PUT/DELETE/GET），权限 `assessment-time:create/update/delete/list`
- [x] 5.3 创建 `AssessmentTimeController`：用户端查询（GET），权限 AUTHENTICATED
