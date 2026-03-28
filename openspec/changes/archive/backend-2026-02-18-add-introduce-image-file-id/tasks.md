## 1. Database Migration

- [x] 1.1 Create Flyway migration script to add `file_id` column to `tb_introduce_image` table
- [x] 1.2 Add foreign key constraint from `tb_introduce_image.file_id` to `tb_file.id`
- [x] 1.3 Add comment for `file_id` column
- [x] 1.4 Test migration script locally

## 2. Domain Layer

- [x] 2.1 Update `IntroduceImage` entity to add `fileId` field
- [x] 2.2 Create `IntroduceImageVO` class with id, type, description, fileId, fileUrl fields
- [x] 2.3 Create `IntroduceImageRepository` interface with method: findByTypeAndDirection
- [x] 2.4 Create `IntroduceImageRepositoryImpl` class implementing repository interface
- [x] 2.5 Implement VO to Entity conversion in repository implementation
- [x] 2.6 Implement file URL loading in repository (LEFT JOIN with File table)
- [x] 2.7 Create custom SQL in IntroduceImageMapper.xml for type and direction filtering
- [x] 2.8 Create `IntroduceImageDomainService` class with query business logic

## 3. Application Layer

- [x] 3.1 Create `IntroduceImageService` interface with method: getIntroduceImages
- [x] 3.2 Create `IntroduceImageServiceImpl` class implementing service interface
- [x] 3.3 Implement getIntroduceImages method with type and direction parameters
- [x] 3.4 Add parameter validation: direction only valid when type=direction
- [x] 3.5 Ensure file URLs are included in returned data
- [x] 3.6 Create `IntroduceImageConverter` for VO to DTO conversion using MapStruct

## 4. API Layer

- [x] 4.1 Create `IntroduceImageDTO` class with id, type, description, fileId, fileUrl fields
- [x] 4.2 Create `IntroduceImageController` class with REST endpoint
- [x] 4.3 Implement GET /api/v1/introduce-images endpoint with type and direction parameters
- [x] 4.4 Add @RequiredPermission annotation (public access)
- [x] 4.5 Add OpenAPI annotations for API documentation
- [x] 4.6 Add @Parameter annotations for type and direction parameters
- [x] 4.7 Add exception handling for validation errors and 404

## 5. UserVO Verification

- [x] 5.1 Verify UserVO contains avatarUrl field with complete URL
- [x] 5.2 Verify UserVO contains wechatQrCodeUrl field with complete URL
- [x] 5.3 Test that frontend can use these URLs directly

## 6. Testing

- [x] 6.1 Write unit tests for IntroduceImageRepository
- [x] 6.2 Write unit tests for IntroduceImageDomainService
- [x] 6.3 Write unit tests for IntroduceImageService
- [x] 6.4 Write unit tests for parameter validation
- [x] 6.5 Write integration tests for IntroduceImageController
- [x] 6.6 Test query by type (laboratory, competition, etc.)
- [x] 6.7 Test query by type and direction
- [x] 6.8 Test invalid parameter combinations
- [x] 6.9 Test file URL loading and retrieval

## 7. Documentation

- [x] 7.1 Update API documentation with new endpoint
- [x] 7.2 Update database schema documentation
- [x] 7.3 Add examples for introduce image query by type
- [x] 7.4 Add examples for introduce image query by type and direction
- [x] 7.5 Document UserVO url fields for frontend usage
