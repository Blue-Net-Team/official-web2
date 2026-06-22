## 1. 环境准备

- [x] 1.1 在 `src/backend/pom.xml` 中添加 `com.tngtech.archunit:archunit-junit5` 依赖
- [x] 1.2 确认项目能正确解析新依赖并编译通过

## 2. 架构测试编写

- [x] 2.1 创建 `src/test/java/com/bluenet/web/api/converter/ConverterLayerArchTest.java`
- [x] 2.2 编写 ArchUnit 规则：禁止 `api.converter` 包下 public `toDTO*` 方法接收 `domain.model.entity` 包参数
- [x] 2.3 运行架构测试，确认当前代码在删除死方法前能通过（规则应允许现有 Result 参数方法）

## 3. 删除死方法

- [x] 3.1 删除 `AssessmentTimeResponseConverter.toDTO(AssessmentTime entity)` 方法
- [x] 3.2 删除 `AssessmentTimeResponseConverter.toDTOList(List<AssessmentTime> entityList)` 方法
- [x] 3.3 删除 `AssessmentQuestionResponseConverter.toDTO(AssessmentQuestion entity)` 方法
- [x] 3.4 删除 `AssessmentQuestionResponseConverter.toDTOForUser(AssessmentQuestion entity)` 方法
- [x] 3.5 删除 `AssessmentQuestionResponseConverter.toDTOList(List<AssessmentQuestion> entityList)` 方法
- [x] 3.6 删除 `AssessmentQuestionResponseConverter.toDTOListForUser(List<AssessmentQuestion> entityList)` 方法

## 4. 测试验证

- [x] 4.1 运行 ArchUnit 架构测试，确认删除后规则仍然通过
- [x] 4.2 运行 `AssessmentTimeResponseConverter` 相关单元测试/集成测试
- [x] 4.3 运行 `AssessmentQuestionResponseConverter` 相关单元测试/集成测试
- [x] 4.4 运行后端全量测试，确认无回归

## 5. 文档与 issue 更新

- [x] 5.1 修正 `docs/后端开发手册.md` 中 converter 位置描述（`application/converter` → `api/converter`）
- [x] 5.2 在 `docs/后端开发手册.md` 中明确禁止 ResponseConverter 直接接收领域实体参数
- [x] 5.3 更新 GitHub issue #35 标题为 "[API] 清理实体直转DTO的死方法，修复 eliminated/answered 等派生字段缺失"
- [x] 5.4 更新 GitHub issue #35 正文，补充 AssessmentQuestionResponseConverter 排查结果和清理范围

## 6. 代码提交

- [x] 6.1 按项目提交规范编写提交信息，引用 #35（使用 `ref #35`，不使用 `fixes` 或 `close`）
- [x] 6.2 提交并推送变更
