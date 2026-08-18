# frontend-direction-learning-path-service Delta

## MODIFIED Requirements

### Requirement: DTO type definitions

The system SHALL define TypeScript types matching backend DTOs for direction learning path. The link field SHALL be named `relatedLink` with the semantics of "相关链接".

#### Scenario: LearningStepDTO type
- **WHEN** defining step type
- **THEN** type includes id (number), stepNumber (number), title (string), relatedLink (string | null)

#### Scenario: DirectionLearningPathDTO type
- **WHEN** defining response type
- **THEN** type includes direction (string), directionName (string), steps (LearningStepDTO[])

---

## ADDED Requirements

### Requirement: Admin service methods for learning step management

系统 SHALL 在方向 service 中提供学习步骤管理方法，使用认证 client 调用后端管理接口。

#### Scenario: Create step method
- **WHEN** 调用 `createStep(slug, { stepNumber, title, relatedLink })`
- **THEN** 系统通过认证 client 请求 `POST /admin/directions/{slug}/learning-steps`

#### Scenario: Update step method
- **WHEN** 调用 `updateStep(id, { stepNumber, title, relatedLink })`
- **THEN** 系统通过认证 client 请求 `PUT /admin/directions/learning-steps/{id}`

#### Scenario: Delete step method
- **WHEN** 调用 `deleteStep(id)`
- **THEN** 系统通过认证 client 请求 `DELETE /admin/directions/learning-steps/{id}`
