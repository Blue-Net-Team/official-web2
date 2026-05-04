## 1. 数据库迁移

- [x] 1.1 新增 Flyway 迁移脚本（如 V6__rename_evaluation_to_assessment.sql）：重命名表 tb_evaluation_time → tb_assessment_time，tb_evaluation_question → tb_assessment_question，tb_evaluation_answer → tb_assessment_answer
- [x] 1.2 同一迁移中重命名相关列（如 evaluation_time_id → assessment_time_id）、索引名及注释
- [x] 1.3 同一迁移中更新权限表：将 permission value 从 evaluation-time:*、evaluation-answer:*、evaluation-question:*、evaluation-result:* 改为对应的 assessment-* 值
- [x] 1.4 更新 tb_file 类型注释及 tb_comment.answer_id 注释中的表名引用（若存在）
- [ ] 1.5 在本地或测试库执行迁移并验证表结构与权限数据

## 2. 实体与 Mapper

- [x] 2.1 将实体类 EvaluationTime、EvaluationQuestion、EvaluationAnswer 重命名为 AssessmentTime、AssessmentQuestion、AssessmentAnswer，表名注解改为 tb_assessment_*
- [x] 2.2 重命名 Mapper 接口与 XML 文件，XML 内表名、列名、resultMap、association 等改为 assessment 命名
- [x] 2.3 更新所有引用上述实体与 Mapper 的 Java 类（Repository/Service/Controller 等）的类名与导入

## 3. 权限与常量

- [x] 3.1 将代码中权限常量/字符串由 evaluation-time:*、evaluation-answer:*、evaluation-question:*、evaluation-result:* 改为 assessment-* 对应值（含注解与配置）
- [x] 3.2 将文件类型枚举中的 evaluation_attachment 改为 assessment_attachment，并更新相关注释与使用处

## 4. 收尾与验证

- [x] 4.1 全局搜索残留的 evaluation（表名、实体名、权限值、枚举），确保无遗漏
- [ ] 4.2 运行现有测试或关键接口验证考核流程（时间配置、题目、答题、评分、权限）
