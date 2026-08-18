# backend-direction-learning-path Delta

## MODIFIED Requirements

### Requirement: Learning path data storage

The system SHALL store learning path steps in `tb_direction_learning_step` table with direction, step number, title, and related link URL fields. The link field SHALL be named `related_url` and carry the semantics of "相关链接" (any related resource link, not limited to videos).

#### Scenario: Database table structure
- **WHEN** the system initializes
- **THEN** `tb_direction_learning_step` table exists with columns: id, direction, step_number, title, related_url

#### Scenario: Unique constraint on direction and step
- **WHEN** inserting a learning step
- **THEN** the combination of direction and step_number MUST be unique

#### Scenario: Column rename migration
- **WHEN** migration V26 executes
- **THEN** column `video_url` is renamed to `related_url` and existing data is preserved

---

### Requirement: Public API for learning path retrieval

The system SHALL provide a public API endpoint `GET /api/v1/directions/{slug}/learning-path` to retrieve learning path data for a specific direction. Step objects in the response SHALL expose the link field as `relatedLink`.

#### Scenario: Successful retrieval with valid slug
- **WHEN** client requests `GET /api/v1/directions/cv/learning-path`
- **THEN** system returns HTTP 200 with learning path data for computer vision direction

#### Scenario: Successful retrieval with related links
- **WHEN** client requests learning path and steps have related URLs
- **THEN** system returns response with relatedLink field populated

#### Scenario: Successful retrieval without related links
- **WHEN** client requests learning path and steps have no related URLs
- **THEN** system returns response with relatedLink field as null

#### Scenario: Invalid direction slug
- **WHEN** client requests `GET /api/v1/directions/invalid/learning-path`
- **THEN** system returns HTTP 404 with error message

---

### Requirement: Admin API for learning path management

The system SHALL provide admin API endpoints for CRUD operations on learning path steps. Request and response DTOs SHALL use `relatedLink` as the link field name.

#### Scenario: Create learning step
- **WHEN** admin requests `POST /api/v1/admin/directions/{slug}/learning-steps` with valid data
- **THEN** system creates new learning step and returns HTTP 201

#### Scenario: Update learning step
- **WHEN** admin requests `PUT /api/v1/admin/directions/learning-steps/{id}` with valid data
- **THEN** system updates learning step and returns HTTP 200

#### Scenario: Delete learning step
- **WHEN** admin requests `DELETE /api/v1/admin/directions/learning-steps/{id}`
- **THEN** system deletes learning step and returns HTTP 204

#### Scenario: Unauthorized access
- **WHEN** unauthenticated user requests admin endpoints
- **THEN** system returns HTTP 401

---

## ADDED Requirements

### Requirement: Seed titles synchronized with current display copy

迁移 V26 SHALL 将三个方向的学习步骤标题更新为现行公开展示文案（与原前端 `data.ts` 硬编码一致），保证公开页切换为后端数据驱动后展示内容不回退。

#### Scenario: Titles updated by migration
- **WHEN** migration V26 executes
- **THEN** 每个方向既有步骤的标题被 UPDATE 为现行展示文案（如 COMPUTER_VISION 第 2 步为"OpenCV图像处理基础"）
