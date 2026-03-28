## 1. Database Migration

- [x] 1.1 Create Flyway migration script V9__add_competition_support.sql
- [x] 1.2 Create tb_competition table with columns: id, name, short_name, logo_file_id, summary, detail, sort_order, enabled, created_at, updated_at
- [x] 1.3 Alter tb_introduce_image table: add competition_id and sort_order columns
- [x] 1.4 Create indexes: idx_competition_enabled_sort, idx_introduce_image_competition

## 2. Domain Layer - Entity

- [x] 2.1 Create Competition entity class with @TableName("tb_competition")
- [x] 2.2 Update IntroduceImage entity: add competitionId and sortOrder fields

## 3. Domain Layer - Value Objects

- [x] 3.1 Create CompetitionVO with fields: id, name, shortName, logoUrl, summary, detail, sortOrder, enabled
- [x] 3.2 Create CompetitionBriefVO with fields: id, name, shortName, logoUrl, summary
- [x] 3.3 Update IntroduceImageVO: add competitionId and sortOrder fields

## 4. Domain Layer - Repository

- [x] 4.1 Create CompetitionRepository interface
- [x] 4.2 Create CompetitionMapper interface extending BaseMapper<Competition>
- [x] 4.3 Create CompetitionMapper.xml with selectEnabledCompetitionsWithLimit query
- [x] 4.4 Create CompetitionMapper.xml with selectCompetitionById query
- [x] 4.5 Implement CompetitionRepositoryImpl
- [x] 4.6 Update IntroduceImageRepository: add findByTypeAndCompetitionId method
- [x] 4.7 Update IntroduceImageMapper.xml: add selectByTypeAndCompetitionId query
- [x] 4.8 Update IntroduceImageRepositoryImpl: implement findByTypeAndCompetitionId

## 5. Domain Layer - Service

- [x] 5.1 Create CompetitionDomainService interface
- [x] 5.2 Implement CompetitionDomainServiceImpl with getCompetitionList method
- [x] 5.3 Implement CompetitionDomainServiceImpl with getCompetitionById method
- [x] 5.4 Implement CompetitionDomainServiceImpl with createCompetition method
- [x] 5.5 Implement CompetitionDomainServiceImpl with updateCompetition method
- [x] 5.6 Implement CompetitionDomainServiceImpl with deleteCompetition method
- [x] 5.7 Implement CompetitionDomainServiceImpl with updateSortOrder method
- [x] 5.8 Update IntroduceImageDomainService: add getCompetitionImages method
- [x] 5.9 Update IntroduceImageDomainService: add addCompetitionImage method
- [x] 5.10 Update IntroduceImageDomainService: add removeCompetitionImage method
- [x] 5.11 Update IntroduceImageDomainService: add countByCompetitionId method

## 6. Application Layer - DTO

- [x] 6.1 Create CompetitionBriefDTO with Swagger annotations
- [x] 6.2 Create CompetitionDetailDTO with Swagger annotations
- [x] 6.3 Create CompetitionImageDTO with Swagger annotations (reuse IntroduceImageDTO pattern)
- [x] 6.4 Create CreateCompetitionRequestDTO with validation annotations
- [x] 6.5 Create UpdateCompetitionRequestDTO with validation annotations
- [x] 6.6 Create UpdateSortOrderRequestDTO with validation annotations
- [x] 6.7 Create AddCompetitionImageRequestDTO with validation annotations
- [x] 6.8 Create ResponseMessage wrapper classes for each DTO

## 7. Application Layer - Converter

- [x] 7.1 Create CompetitionConverter with convertToBriefDTO method
- [x] 7.2 Create CompetitionConverter with convertToDetailDTO method
- [x] 7.3 Update IntroduceImageConverter: handle competitionId and sortOrder fields

## 8. Application Layer - Service

- [x] 8.1 Create CompetitionService interface
- [x] 8.2 Implement CompetitionServiceImpl with getCompetitionList method
- [x] 8.3 Implement CompetitionServiceImpl with getCompetitionDetail method (coordinate with IntroduceImageService)
- [x] 8.4 Implement CompetitionServiceImpl with createCompetition method
- [x] 8.5 Implement CompetitionServiceImpl with updateCompetition method
- [x] 8.6 Implement CompetitionServiceImpl with deleteCompetition method
- [x] 8.7 Implement CompetitionServiceImpl with updateSortOrder method
- [x] 8.8 Implement CompetitionServiceImpl with addCompetitionImage method
- [x] 8.9 Implement CompetitionServiceImpl with removeCompetitionImage method

## 9. Controller Layer - Public API

- [x] 9.1 Create CompetitionController with @RequestMapping("/api/v1/competitions")
- [x] 9.2 Implement GET /api/v1/competitions endpoint with limit parameter
- [x] 9.3 Implement GET /api/v1/competitions/{id} endpoint
- [x] 9.4 Add @RequiresPermission annotation with AccessLevel.PUBLIC
- [x] 9.5 Add Swagger @Operation, @ApiResponses annotations

## 10. Controller Layer - Admin API

- [x] 10.1 Create AdminCompetitionController with @RequestMapping("/api/v1/admin/competitions")
- [x] 10.2 Implement POST /api/v1/admin/competitions endpoint
- [x] 10.3 Implement PUT /api/v1/admin/competitions/{id} endpoint
- [x] 10.4 Implement DELETE /api/v1/admin/competitions/{id} endpoint
- [x] 10.5 Implement PUT /api/v1/admin/competitions/{id}/sort endpoint
- [x] 10.6 Implement POST /api/v1/admin/competitions/{id}/images endpoint
- [x] 10.7 Implement DELETE /api/v1/admin/competitions/{id}/images/{imageId} endpoint
- [x] 10.8 Add @RequiresPermission annotation with admin role requirement
- [x] 10.9 Add Swagger annotations for all admin endpoints

## 11. Testing

### 11.1 Unit Tests

- [x] 11.1.1 Create CompetitionDomainServiceImplTest (15 test cases)
- [x] 11.1.2 Create CompetitionServiceImplTest (22 test cases)
- [x] 11.1.3 Create CompetitionConverterTest (14 test cases)

### 11.2 Integration Tests

- [x] 11.2.1 Create CompetitionControllerIntegrationTest (11 test cases)
- [x] 11.2.2 Create AdminCompetitionControllerIntegrationTest (16 test cases)

### 11.3 Test Verification

- [x] 11.3.1 All unit tests passed
- [x] 11.3.2 All integration tests passed
