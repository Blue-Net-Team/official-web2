## 1. 枚举类创建

- [x] 1.1 创建 Direction 枚举（computer_vision, structural_design, embedded）
- [x] 1.2 创建 FileType 枚举（avatar, normal_img, evaluation_attachment, work, qrcode）
- [x] 1.3 创建 ExperienceType 枚举（competition, project, internship）
- [x] 1.4 创建 AchievementType 枚举（paper, patent, competition）
- [x] 1.5 创建 EnrollStatus 枚举（pending, approved, rejected）
- [x] 1.6 创建 ImageType 枚举（laboratory, equipment, team_photo, direction, competition, patent, paper）
- [x] 1.7 创建 QuestionType 枚举（single_choice, multiple_choice, file_upload, algorithm）
- [x] 1.8 创建 ProgrammingLanguage 枚举（python, c, cpp, java, javascript）
- [x] 1.9 为所有枚举添加 @EnumValue 注解配置

## 2. QuestionContent 多态体系

- [x] 2.1 创建 QuestionContent 抽象基类
- [x] 2.2 创建 FileUploadContent 子类
- [x] 2.3 创建 SingleChoiceContent 子类（含 options, correctAnswer）
- [x] 2.4 创建 MultipleChoiceContent 子类（含 options, correctAnswers）
- [x] 2.5 创建 AlgorithmContent 子类（含 testCases, timeLimit, memoryLimit）
- [x] 2.6 添加 Jackson @JsonTypeInfo 和 @JsonSubTypes 配置

## 3. 核心实体类创建

- [x] 3.1 创建 Role 实体
- [x] 3.2 创建 Permission 实体
- [x] 3.3 创建 RolePermission 关联实体
- [x] 3.4 创建 College 实体
- [x] 3.5 创建 User 实体（含 disable 字段）
- [x] 3.6 创建 UserExperience 实体
- [x] 3.7 创建 Achievement 实体
- [x] 3.8 创建 UserAchievement 关联实体

## 4. 报名与文件实体

- [x] 4.1 创建 File 实体
- [x] 4.2 创建 IntroduceImage 实体
- [x] 4.3 创建 Enroll 实体（含 status 和 avatar_id）
- [x] 4.4 创建 VerifyCode 实体（验证码管理）

## 5. 考核系统实体

- [x] 5.1 创建 MessageTemplate 实体
- [x] 5.2 创建 EvaluationTime 实体（含 epoch, time_limit）
- [x] 5.3 创建 EvaluationQuestion 实体（含 JSON content）
- [x] 5.4 创建 EvaluationAnswer 实体（含 language 字段）
- [x] 5.5 创建 Comment 实体（含 score 字段）
- [x] 5.6 创建 Audit 实体（审计日志，无 deleted 字段）

## 6. MyBatis Mapper 接口

- [x] 6.1 创建 RoleMapper 接口
- [x] 6.2 创建 PermissionMapper 接口
- [x] 6.3 创建 RolePermissionMapper 接口
- [x] 6.4 创建 UserMapper 接口
- [x] 6.5 创建 UserExperienceMapper 接口
- [x] 6.6 创建 AchievementMapper 接口
- [x] 6.7 创建 UserAchievementMapper 接口
- [x] 6.8 创建 CollegeMapper 接口
- [x] 6.9 创建 FileMapper 接口
- [x] 6.10 创建 IntroduceImageMapper 接口
- [x] 6.11 创建 EnrollMapper 接口
- [x] 6.12 创建 VerifyCodeMapper 接口
- [x] 6.13 创建 MessageTemplateMapper 接口
- [x] 6.14 创建 EvaluationTimeMapper 接口
- [x] 6.15 创建 EvaluationQuestionMapper 接口
- [x] 6.16 创建 EvaluationAnswerMapper 接口
- [x] 6.17 创建 CommentMapper 接口
- [x] 6.18 创建 AuditMapper 接口

## 7. MyBatis XML 映射文件

- [x] 7.1 创建 RoleMapper.xml（含 resultMap）
- [x] 7.2 创建 PermissionMapper.xml（含 resultMap）
- [x] 7.3 创建 RolePermissionMapper.xml（含 resultMap）
- [x] 7.4 创建 UserMapper.xml（含 resultMap）
- [x] 7.5 创建 UserExperienceMapper.xml（含 resultMap）
- [x] 7.6 创建 AchievementMapper.xml（含 resultMap）
- [x] 7.7 创建 UserAchievementMapper.xml（含 resultMap）
- [x] 7.8 创建 CollegeMapper.xml（含 resultMap）
- [x] 7.9 创建 FileMapper.xml（含 resultMap）
- [x] 7.10 创建 IntroduceImageMapper.xml（含 resultMap）
- [x] 7.11 创建 EnrollMapper.xml（含 resultMap）
- [x] 7.12 创建 VerifyCodeMapper.xml（含 resultMap）
- [x] 7.13 创建 MessageTemplateMapper.xml（含 resultMap）
- [x] 7.14 创建 EvaluationTimeMapper.xml（含 resultMap）
- [x] 7.15 创建 EvaluationQuestionMapper.xml（含 resultMap 和 JSON 映射）
- [x] 7.16 创建 EvaluationAnswerMapper.xml（含 resultMap）
- [x] 7.17 创建 CommentMapper.xml（含 resultMap）
- [x] 7.18 创建 AuditMapper.xml（含 resultMap）

## 8. Flyway 迁移脚本

- [x] 8.1 创建 V1__init_schema.sql 表结构
- [x] 8.2 创建 tb_role 表（含自增ID）
- [x] 8.3 创建 tb_permission 表
- [x] 8.4 创建 tb_role_permission 关联表
- [x] 8.5 创建 tb_college 表
- [x] 8.6 创建 tb_user 表（含 disable 字段）
- [x] 8.7 创建 tb_user_experience 表
- [x] 8.8 创建 tb_achievement 表
- [x] 8.9 创建 tb_user_achievement 关联表
- [x] 8.10 创建 tb_file 表
- [x] 8.11 创建 tb_introduce_image 表
- [x] 8.12 创建 tb_enroll 表
- [x] 8.13 创建 tb_verify_code 表
- [x] 8.14 创建 tb_message_template 表
- [x] 8.15 创建 tb_evaluation_time 表
- [x] 8.16 创建 tb_evaluation_question 表（含 JSON 类型 content）
- [x] 8.17 创建 tb_evaluation_answer 表
- [x] 8.18 创建 tb_comment 表
- [x] 8.19 创建 tb_audit 表（无 deleted 字段）
- [x] 8.20 添加外键约束和索引

## 9. 配置与验证

- [x] 9.1 配置 application.yml MyBatis 路径
- [x] 9.2 配置 Flyway 迁移路径
- [x] 9.3 运行 Flyway 迁移验证表创建 - **已验证成功** (18张表)
- [x] 9.4 验证所有枚举映射正确 - **代码已完成** (EnumMappingTest.java)
- [x] 9.5 验证 QuestionContent JSON 序列化/反序列化 - **代码已完成** (QuestionContentJsonTest.java)
- [x] 9.6 验证实体 CRUD 操作 - **代码已完成** (EntityCrudTest.java)
