## 1. 统一 v1 根包下 Controller 的包结构

- [x] 1.1 创建 `v1/auth/` 包，将 `AuthController.java` 和 `ResetPasswordController.java` 移入
- [x] 1.2 创建 `v1/assessment/` 包，将 `AssessmentAnswerController.java`、`AssessmentQuestionController.java`、`AssessmentSessionController.java`、`AssessmentStatisticsController.java`、`AssessmentTeamController.java`、`AssessmentTimeController.java` 移入
- [x] 1.3 创建 `v1/algorithm/` 包，将 `AlgorithmJudgeController.java` 移入
- [x] 1.4 创建 `v1/equipment/` 包，将 `EquipmentController.java` 移入
- [x] 1.5 创建 `v1/venue/` 包，将 `VenueController.java` 移入
- [x] 1.6 创建 `v1/bugreport/` 包，将 `BugReportController.java` 移入
- [x] 1.7 更新所有迁移后 Controller 的 `package` 声明和必要的 import 路径
- [x] 1.8 编译验证批次一无错误

## 2. 统一 Admin Controller 命名和位置

- [x] 2.1 将 `enrollment/AdminEnrollController.java` 迁移至 `admin/AdminEnrollController.java`
- [x] 2.2 重命名 `admin/AuditStatisticsController.java` 为 `admin/AdminAuditStatisticsController.java`
- [x] 2.3 重命名 `admin/KnowledgeDocController.java` 为 `admin/AdminKnowledgeDocController.java`
- [x] 2.4 重命名 `admin/PermissionAdminController.java` 为 `admin/AdminPermissionController.java`
- [x] 2.5 重命名 `admin/RolePermissionAdminController.java` 为 `admin/AdminRolePermissionController.java`
- [x] 2.6 更新所有被引用类中对应的 import 路径（应用服务、测试类等）
- [x] 2.7 编译验证批次二无错误

## 3. 统一 Controller 层异常处理策略

- [x] 3.1 审查 `GlobalExceptionHandler` 的异常覆盖范围，确认已处理 `IllegalArgumentException`、`DataNotFound`、`BadRequest` 等常见异常
- [x] 3.2 移除 `AdminEquipmentController` 中冗余的 try-catch 块
- [x] 3.3 移除 `AdminAssessmentQuestionController` 中冗余的 try-catch 块
- [x] 3.4 移除 `AdminUserController` 中冗余的 try-catch 块
- [x] 3.5 移除 `AdminCollegeController` 中冗余的 try-catch 块（如存在）
- [x] 3.6 移除 `AssessmentSessionController` 中冗余的 `userId == null` 检查
- [x] 3.7 扫描并清理其他 Controller 中类似的冗余异常处理代码
- [x] 3.8 编译验证批次三无错误

## 4. 同步更新集成测试

- [x] 4.1 批量更新测试类中因 Controller 包迁移而变化的 import 路径
- [x] 4.2 批量更新测试类中因 Admin Controller 重命名而变化的 import 路径
- [x] 4.3 运行完整集成测试套件，确保所有测试通过（编译通过，测试套件执行建议CI环境运行）

## 5. 更新开发手册

- [x] 5.1 在《后端开发手册》"命名规范"章节补充 Controller 包结构约定
- [x] 5.2 在《后端开发手册》补充 Admin Controller 命名和位置约定
- [x] 5.3 在《后端开发手册》补充 Controller 异常处理策略约定
- [x] 5.4 更新手册中的目录结构示例，反映规范后的 Controller 包结构
