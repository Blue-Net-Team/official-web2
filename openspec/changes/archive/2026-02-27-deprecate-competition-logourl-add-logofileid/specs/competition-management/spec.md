## Purpose

更新竞赛管理规范，废弃 `logoUrl` 字段，新增 `logoFileId` 字段，使前端能够通过文件下载接口获取竞赛Logo。

## Requirements

### Requirement: Public competition list endpoint

#### Scenario: Get competition list with limit
- **WHEN** requesting GET /api/v1/competitions with limit parameter
- **THEN** system SHALL return at most limit competitions
- **THEN** each competition SHALL include id, name, shortName, summary, logoUrl, logoFileId
- **THEN** logoUrl SHALL be marked as deprecated in API documentation
- **THEN** logoFileId SHALL be used to access logo via /api/v1/file/download/{fileId}
- **THEN** competitions SHALL be sorted by sort_order DESC, created_at DESC
- **THEN** only enabled competitions SHALL be returned
- **THEN** limit SHALL default to 10 if not provided
- **THEN** limit SHALL NOT exceed 50

#### Scenario: Competition without logo
- **WHEN** a competition has no logo_file_id
- **THEN** logoUrl SHALL be null
- **THEN** logoFileId SHALL be null

### Requirement: Public competition detail endpoint

#### Scenario: Get competition detail
- **WHEN** requesting GET /api/v1/competitions/{id}
- **THEN** system SHALL return competition with id, name, shortName, summary, detail, logoUrl, logoFileId
- **THEN** logoUrl SHALL be marked as deprecated in API documentation
- **THEN** logoFileId SHALL be used to access logo via /api/v1/file/download/{fileId}
- **THEN** system SHALL return associated images from tb_introduce_image where type=COMPETITION and competition_id={id}
- **THEN** images SHALL be sorted by sort_order ASC
- **THEN** each image SHALL include id, url, description

#### Scenario: Competition not found
- **WHEN** requesting a non-existent competition id
- **THEN** system SHALL return 404 error

#### Scenario: Competition disabled
- **WHEN** requesting a disabled competition
- **THEN** system SHALL return 404 error

## API Schema Changes

### CompetitionBriefDTO

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "竞赛简要信息")
public class CompetitionBriefDTO {
    @Schema(description = "竞赛ID")
    private Long id;

    @Schema(description = "竞赛名称")
    private String name;

    @Schema(description = "竞赛简称")
    private String shortName;

    @Deprecated
    @Schema(description = "Logo URL (已废弃，请使用logoFileId)")
    private String logoUrl;

    @Schema(description = "Logo文件ID，用于调用下载接口 /api/v1/file/download/{fileId}")
    private Long logoFileId;

    @Schema(description = "竞赛简介")
    private String summary;
}
```

### CompetitionDetailDTO

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "竞赛详细信息")
public class CompetitionDetailDTO {
    @Schema(description = "竞赛ID")
    private Long id;

    @Schema(description = "竞赛名称")
    private String name;

    @Schema(description = "竞赛简称")
    private String shortName;

    @Deprecated
    @Schema(description = "Logo URL (已废弃，请使用logoFileId)")
    private String logoUrl;

    @Schema(description = "Logo文件ID，用于调用下载接口 /api/v1/file/download/{fileId}")
    private Long logoFileId;

    @Schema(description = "竞赛简介")
    private String summary;

    @Schema(description = "竞赛详细介绍")
    private String detail;

    @Schema(description = "竞赛相关照片")
    private List<CompetitionImageDTO> images;
}
```

## Data Model Changes

### CompetitionBriefVO

```java
@Data
@AllArgsConstructor
@Builder
public class CompetitionBriefVO {
    private Long id;
    private String name;
    private String shortName;
    private String logoUrl;
    private Long logoFileId;  // 新增
    private String summary;
}
```

### CompetitionVO

```java
@Data
@AllArgsConstructor
@Builder
public class CompetitionVO {
    private Long id;
    private String name;
    private String shortName;
    private String logoUrl;
    private Long logoFileId;  // 新增
    private String summary;
    private String detail;
    private Integer sortOrder;
    private Boolean enabled;
}
```

## SQL Query Changes

### CompetitionMapper.xml

```xml
<resultMap id="CompetitionBriefVOResultMap" type="com.bluenet.web.domain.model.vo.CompetitionBriefVO">
    <id column="id" property="id"/>
    <result column="name" property="name"/>
    <result column="short_name" property="shortName"/>
    <result column="logo_url" property="logoUrl"/>
    <result column="logo_file_id" property="logoFileId"/>  <!-- 新增 -->
    <result column="summary" property="summary"/>
</resultMap>

<resultMap id="CompetitionVOResultMap" type="com.bluenet.web.domain.model.vo.CompetitionVO">
    <id column="id" property="id"/>
    <result column="name" property="name"/>
    <result column="short_name" property="shortName"/>
    <result column="logo_url" property="logoUrl"/>
    <result column="logo_file_id" property="logoFileId"/>  <!-- 新增 -->
    <result column="summary" property="summary"/>
    <result column="detail" property="detail"/>
    <result column="sort_order" property="sortOrder"/>
    <result column="enabled" property="enabled"/>
</resultMap>

<select id="selectEnabledCompetitionsWithLimit" resultMap="CompetitionBriefVOResultMap">
    SELECT
        c.id,
        c.name,
        c.short_name,
        f.url AS logo_url,
        c.logo_file_id,  <!-- 新增 -->
        c.summary
    FROM tb_competition c
    LEFT JOIN tb_file f ON c.logo_file_id = f.id
    WHERE c.enabled = TRUE
    ORDER BY c.sort_order DESC, c.created_at DESC
    LIMIT #{limit}
</select>

<select id="selectCompetitionById" resultMap="CompetitionVOResultMap">
    SELECT
        c.id,
        c.name,
        c.short_name,
        f.url AS logo_url,
        c.logo_file_id,  <!-- 新增 -->
        c.summary,
        c.detail,
        c.sort_order,
        c.enabled
    FROM tb_competition c
    LEFT JOIN tb_file f ON c.logo_file_id = f.id
    WHERE c.id = #{id} AND c.enabled = TRUE
</select>
```

## Migration Guide

### 前端迁移

**变更前：**
```html
<img src="{{competition.logoUrl}}" alt="{{competition.name}}" />
```

**变更后：**
```html
<img src="/api/v1/file/download/{{competition.logoFileId}}" alt="{{competition.name}}" />
```

### 注意事项

1. `logoUrl` 字段仍然保留以确保向后兼容，但已标记为 `@Deprecated`
2. 当 `logo_file_id` 为 null 时，`logoFileId` 返回 null
3. 前端应优先使用 `logoFileId` 获取Logo图片
