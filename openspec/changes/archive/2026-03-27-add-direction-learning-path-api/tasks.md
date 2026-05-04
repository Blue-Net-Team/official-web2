## 1. Database Migration

- [x] 1.1 Create migration file `V15__add_direction_learning_step.sql`
- [x] 1.2 Define `tb_direction_learning_step` table structure
- [x] 1.3 Add unique constraint on (direction, step_number)
- [x] 1.4 Insert initial learning path data for all three directions
- [x] 1.5 Add permission records for learning path management

## 2. Domain Layer

- [x] 2.1 Create Entity `DirectionLearningStep.java`
- [x] 2.2 Create VO `LearningStepVO.java`
- [x] 2.3 Create Repository interface `LearningPathRepository.java`
- [x] 2.4 Create Domain Service interface `LearningPathDomainService.java`
- [x] 2.5 Implement Domain Service `LearningPathDomainServiceImpl.java`

## 3. Infrastructure Layer

- [x] 3.1 Create Mapper `LearningPathMapper.java`
- [x] 3.2 Create Mapper XML `LearningPathMapper.xml`
- [x] 3.3 Implement Repository `LearningPathRepositoryImpl.java`
- [x] 3.4 Create `DirectionSlugConverter` utility class

## 4. Application Layer

- [x] 4.1 Create DTO `LearningStepDTO.java`
- [x] 4.2 Create DTO `CreateLearningStepRequestDTO.java`
- [x] 4.3 Create DTO `UpdateLearningStepRequestDTO.java`
- [x] 4.4 Create DTO `DirectionLearningPathDTO.java`
- [x] 4.5 Create Service interface `LearningPathService.java`
- [x] 4.6 Implement Service `LearningPathServiceImpl.java`
- [x] 4.7 Create Converter `LearningPathConverter.java`

## 5. API Layer - Public Endpoints

- [x] 5.1 Create `LearningPathController.java`
- [x] 5.2 Implement `GET /api/v1/directions/{slug}/learning-path` endpoint
- [x] 5.3 Add `@RequiresPermission` annotation with PUBLIC access

## 6. API Layer - Admin Endpoints

- [x] 6.1 Create `AdminLearningPathController.java`
- [x] 6.2 Implement `POST /api/v1/admin/directions/{slug}/learning-steps` endpoint
- [x] 6.3 Implement `PUT /api/v1/admin/directions/learning-steps/{id}` endpoint
- [x] 6.4 Implement `DELETE /api/v1/admin/directions/learning-steps/{id}` endpoint
- [x] 6.5 Add `@RequiresPermission` annotations with PROTECTED access

## 7. Testing

- [x] 7.1 Write unit tests for `LearningPathDomainServiceImpl`
- [x] 7.2 Write unit tests for `LearningPathServiceImpl`
- [x] 7.3 Write integration tests for `LearningPathController`
- [x] 7.4 Write integration tests for `AdminLearningPathController`
- [x] 7.5 Verify all tests pass

## 8. Documentation

- [x] 8.1 Add OpenAPI annotations to all DTOs
- [x] 8.2 Add OpenAPI annotations to all endpoints
- [x] 8.3 Verify API documentation is generated correctly
