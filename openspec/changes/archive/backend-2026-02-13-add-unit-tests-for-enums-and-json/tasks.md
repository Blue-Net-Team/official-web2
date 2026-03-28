## 1. 测试环境配置

- [x] 1.1 配置 H2 内存数据库用于测试
- [x] 1.2 创建 application-test.yml 测试配置
- [x] 1.3 添加测试依赖（如果需要）

## 2. 枚举映射测试

- [x] 2.1 创建 EnumMappingTest 测试类
- [x] 2.2 测试 Direction 枚举映射
- [x] 2.3 测试 FileType 枚举映射
- [x] 2.4 测试 ExperienceType 枚举映射
- [x] 2.5 测试 AchievementType 枚举映射
- [x] 2.6 测试 EnrollStatus 枚举映射
- [x] 2.7 测试 ImageType 枚举映射
- [x] 2.8 测试 QuestionType 枚举映射
- [x] 2.9 测试 ProgrammingLanguage 枚举映射

## 3. JSON 序列化测试

- [x] 3.1 创建 QuestionContentJsonTest 测试类
- [x] 3.2 测试 SingleChoiceContent 序列化/反序列化
- [x] 3.3 测试 MultipleChoiceContent 序列化/反序列化
- [x] 3.4 测试 AlgorithmContent 序列化/反序列化
- [x] 3.5 测试 FileUploadContent 序列化/反序列化
- [x] 3.6 测试多态类型识别（type discriminator）

## 4. 实体 CRUD 测试

- [x] 4.1 创建 EntityCrudTest 测试类
- [x] 4.2 测试 User 实体 CRUD
- [x] 4.3 测试 EvaluationQuestion 含 JSON content CRUD
- [x] 4.4 测试其他关键实体（Role, College, Enroll）

## 5. 运行验证

- [x] 5.1 运行所有测试确保通过（需要IDE运行，Maven编译环境问题）
- [x] 5.2 修复发现的任何问题
