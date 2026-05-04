## Tasks

### Task 1: 更新 VO 类添加 logoFileId 字段

**File:** `src/main/java/com/bluenet/web/domain/model/vo/CompetitionBriefVO.java`

- [x] 添加 `private Long logoFileId;` 字段
- [x] 更新 `@Builder` 注解的构造函数

**File:** `src/main/java/com/bluenet/web/domain/model/vo/CompetitionVO.java`

- [x] 添加 `private Long logoFileId;` 字段
- [x] 更新 `@Builder` 注解的构造函数

### Task 2: 更新 DTO 类添加 logoFileId 字段并标记 logoUrl 为废弃

**File:** `src/main/java/com/bluenet/web/api/dto/competition/CompetitionBriefDTO.java`

- [x] 添加 `private Long logoFileId;` 字段
- [x] 在 `logoUrl` 字段上添加 `@Deprecated` 注解
- [x] 更新 `@Schema` 描述说明废弃原因
- [x] 更新 `@Builder` 注解的构造函数

**File:** `src/main/java/com/bluenet/web/api/dto/competition/CompetitionDetailDTO.java`

- [x] 添加 `private Long logoFileId;` 字段
- [x] 在 `logoUrl` 字段上添加 `@Deprecated` 注解
- [x] 更新 `@Schema` 描述说明废弃原因
- [x] 更新 `@Builder` 注解的构造函数

### Task 3: 更新 Mapper XML 文件

**File:** `src/main/resources/infrastructure/repository/mapper/CompetitionMapper.xml`

- [x] 在 `CompetitionBriefVOResultMap` 中添加 `logo_file_id` 字段映射
- [x] 在 `CompetitionVOResultMap` 中添加 `logo_file_id` 字段映射
- [x] 在 `selectEnabledCompetitionsWithLimit` 查询中添加 `c.logo_file_id` 字段
- [x] 在 `selectCompetitionById` 查询中添加 `c.logo_file_id` 字段

### Task 4: 更新 Converter 类

**File:** `src/main/java/com/bluenet/web/application/converter/CompetitionConverter.java`

- [x] 在 `convertToBriefDTO` 方法中添加 `logoFileId` 字段复制
- [x] 在 `convertToDetailDTO` 方法中添加 `logoFileId` 字段复制

### Task 5: 更新单元测试

**File:** `src/test/java/com/bluenet/web/application/converter/CompetitionConverterTest.java`

- [x] 更新测试用例，验证 `logoFileId` 字段正确转换

**File:** `src/test/java/com/bluenet/web/application/service/impl/CompetitionServiceImplTest.java`

- [x] 更新测试用例，验证返回的 DTO 包含 `logoFileId`

**File:** `src/test/java/com/bluenet/web/api/controller/v1/competition/CompetitionControllerIntegrationTest.java`

- [x] 更新集成测试，验证 API 响应包含 `logoFileId` 字段

### Task 6: 验证和测试

- [x] 运行单元测试确保通过
- [x] 运行集成测试确保通过
- [x] 使用 Swagger UI 验证 API 文档正确显示废弃标记
- [x] 手动测试 API 响应格式正确
