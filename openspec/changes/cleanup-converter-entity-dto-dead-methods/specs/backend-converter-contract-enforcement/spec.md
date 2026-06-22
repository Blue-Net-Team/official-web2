## ADDED Requirements

### Requirement: Converter methods SHALL NOT accept domain entities as parameters

API ResponseConverter 中用于将数据转换为响应 DTO 的 public 方法，其参数类型 MUST NOT 为 `com.bluenet.web.domain.model.entity` 包下的领域实体。

#### Scenario: ArchUnit detects entity parameter in toDTO method
- **WHEN** 开发者在 `api.converter` 包下新增一个 public 方法 `toDTO(AssessmentTime entity)`
- **THEN** ArchUnit 架构测试 MUST 失败并阻止合并

#### Scenario: ArchUnit allows Result parameter in toDTO method
- **WHEN** 开发者在 `api.converter` 包下新增一个 public 方法 `toDTO(AssessmentTimeResult result)`
- **THEN** ArchUnit 架构测试 MUST 通过

### Requirement: Dead entity-to-DTO methods SHALL be removed

`AssessmentTimeResponseConverter` 和 `AssessmentQuestionResponseConverter` 中当前无调用方的实体直转 DTO 方法 MUST 被删除。

#### Scenario: AssessmentTimeResponseConverter no longer contains entity-based toDTO
- **WHEN** 代码审查 `AssessmentTimeResponseConverter.java`
- **THEN** MUST NOT 存在 `toDTO(AssessmentTime entity)` 或 `toDTOList(List<AssessmentTime>)` 方法

#### Scenario: AssessmentQuestionResponseConverter no longer contains entity-based toDTO variants
- **WHEN** 代码审查 `AssessmentQuestionResponseConverter.java`
- **THEN** MUST NOT 存在 `toDTO(AssessmentQuestion entity)`、`toDTOForUser(AssessmentQuestion entity)` 或对应的 `toDTOList` 方法

### Requirement: Existing API behavior MUST remain unchanged

删除死方法后，现有 Controller 接口返回的 DTO 字段值 MUST 与变更前一致。

#### Scenario: User-visible assessment time list still returns eliminated field
- **WHEN** 已登录用户调用 `GET /api/v1/assessment-times`
- **THEN** 返回的 `AssessmentTimeDTO` 中 `eliminated` 字段 MUST 不为 null，且值与当前用户淘汰状态一致

#### Scenario: Admin assessment time list still returns allowTeam and progress fields
- **WHEN** 管理员调用 `GET /api/v1/admin/assessment-times` 或创建/更新考核时间
- **THEN** 返回的 `AssessmentTimeDTO` 中 `allowTeam`、`totalQuestions`、`completedQuestions` 等字段 MUST 正确填充

#### Scenario: User question list still returns answered field
- **WHEN** 已登录用户查询考核题目列表
- **THEN** 返回的 `AssessmentQuestionDTO` 中 `answered` 字段 MUST 不为 null，且值与当前用户作答状态一致
