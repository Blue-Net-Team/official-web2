## MODIFIED Requirements

### Requirement: DTO type definitions

The system SHALL define TypeScript types matching backend DTOs for direction learning path. The link field SHALL be named `relatedLink` with the semantics of "相关链接". Step types SHALL NOT contain any step number or sort order field, because display numbering is derived by the frontend from list position.

#### Scenario: LearningStepDTO type
- **WHEN** defining step type
- **THEN** type includes id (number), title (string), relatedLink (string | null) and contains no stepNumber or sortOrder field

#### Scenario: DirectionLearningPathDTO type
- **WHEN** defining response type
- **THEN** type includes direction (string), directionName (string), steps (LearningStepDTO[])

#### Scenario: Step request type
- **WHEN** defining the create/update request type
- **THEN** type includes title (string) and relatedLink (string | null | undefined) and contains no stepNumber field

---

### Requirement: Admin service methods for learning step management

系统 SHALL 在方向 service 中提供学习步骤管理方法，使用认证 client 调用后端管理接口。

#### Scenario: Create step method
- **WHEN** 调用 `createStep(slug, { title, relatedLink })`
- **THEN** 系统通过认证 client 请求 `POST /admin/directions/{slug}/learning-steps`，请求体不含步骤序号

#### Scenario: Update step method
- **WHEN** 调用 `updateStep(id, { title, relatedLink })`
- **THEN** 系统通过认证 client 请求 `PUT /admin/directions/learning-steps/{id}`，请求体不含步骤序号

#### Scenario: Delete step method
- **WHEN** 调用 `deleteStep(id)`
- **THEN** 系统通过认证 client 请求 `DELETE /admin/directions/learning-steps/{id}`

#### Scenario: Delete does not require order compensation
- **WHEN** 删除某步骤后重新拉取列表
- **THEN** service 直接返回后端数组顺序，不向前端暴露需要补位的序号字段

## ADDED Requirements

### Requirement: Batch sort order service method

系统 SHALL 在方向 service 中提供批量提交学习步骤顺序的方法，使用认证 client 调用后端排序接口。

#### Scenario: Batch update sort order method
- **WHEN** 调用 `batchUpdateSortOrder(slug, { items: [{ id, sortOrder }] })`
- **THEN** 系统通过认证 client 请求 `PUT /admin/directions/{slug}/learning-steps/sort`，请求体为 items 数组

#### Scenario: Sort order payload type
- **WHEN** 定义排序请求类型
- **THEN** type 为 `{ items: Array<{ id: number; sortOrder: number }> }`，与后端 `BatchUpdateSortOrderRequestDTO` 对齐

#### Scenario: Sort method surfaces failure to caller
- **WHEN** 后端返回非成功状态
- **THEN** service 以 rejected promise 暴露失败，供页面执行乐观更新回滚
