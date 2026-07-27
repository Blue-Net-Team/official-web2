## ADDED Requirements

### Requirement: 竞赛 Logo 更新接口
系统 SHALL 提供接口 `PUT /api/v1/admin/competitions/{id}/logo`，允许管理员通过 fileId 更新竞赛 Logo。

#### Scenario: 成功更新 Logo
- **WHEN** 管理员 PUT `/api/v1/admin/competitions/1/logo` body=`{fileId: 100}`
- **THEN** 系统 SHALL 校验竞赛存在
- **AND** 系统 SHALL 校验 fileId 对应文件存在且类型为 NORMAL_IMG
- **AND** 系统 SHALL 更新 tb_competition.logo_file_id
- **AND** 返回 200

#### Scenario: 竞赛不存在
- **WHEN** PUT `/api/v1/admin/competitions/9999/logo` body=`{fileId: 100}`
- **THEN** 返回 404 错误

### Requirement: 竞赛封面更新接口
系统 SHALL 提供接口 `PUT /api/v1/admin/competitions/{id}/cover`，允许管理员通过 fileId 更新竞赛封面图片。每场竞赛仅一张封面。

#### Scenario: 成功更新封面
- **WHEN** 管理员 PUT `/api/v1/admin/competitions/1/cover` body=`{fileId: 200}`
- **THEN** 系统 SHALL 校验竞赛存在
- **AND** 系统 SHALL 校验 fileId 对应文件存在且类型为 NORMAL_IMG
- **AND** 系统 SHALL 更新 tb_competition.cover_file_id
- **AND** 返回 200

#### Scenario: 竞赛不存在
- **WHEN** PUT `/api/v1/admin/competitions/9999/cover` body=`{fileId: 200}`
- **THEN** 返回 404 错误

## MODIFIED Requirements

### Requirement: Competition image association via IntroduceImage
竞赛图片关联改为仅支持 logo + 封面各一张，通过 tb_competition 表的 logo_file_id 和 cover_file_id 字段直接关联，不再通过 tb_introduce_image 多图关联。

#### Scenario: Competition logo association
- **WHEN** 管理员设置竞赛 logo
- **THEN** logo_file_id MUST reference a valid File record with type NORMAL_IMG
- **THEN** 每场竞赛仅一张 logo

#### Scenario: Competition cover association
- **WHEN** 管理员设置竞赛封面
- **THEN** cover_file_id MUST reference a valid File record with type NORMAL_IMG
- **THEN** 每场竞赛仅一张封面

### Requirement: Public competition list endpoint
公开竞赛列表接口 SHALL 返回 logoFileId 和 coverFileId。

#### Scenario: Get competition list
- **WHEN** requesting GET /api/v1/competitions
- **THEN** each competition SHALL include id, name, shortName, logoFileId, coverFileId, summary, level, month, organizer
- **THEN** coverFileId 为 null 时前端应 fallback 到历史数据中的 introduce_image

### Requirement: 竞赛名称唯一性约束

系统应确保竞赛名称在全局范围内唯一，作为成就与竞赛之间的稳定关联键。

#### Scenario: 创建竞赛时名称唯一
- **WHEN** 管理员创建竞赛，填写的名称在竞赛库中不存在
- **THEN** 系统创建竞赛成功

#### Scenario: 创建竞赛时名称重复
- **WHEN** 管理员创建竞赛，填写的名称在竞赛库中已存在
- **THEN** 系统返回错误，提示"竞赛名称已存在"

#### Scenario: 更新竞赛时名称唯一
- **WHEN** 管理员更新竞赛，将名称修改为竞赛库中不存在的名称
- **THEN** 系统更新竞赛成功

#### Scenario: 更新竞赛时名称重复
- **WHEN** 管理员更新竞赛，将名称修改为竞赛库中已存在的名称（且不是当前竞赛自身）
- **THEN** 系统返回错误，提示"竞赛名称已存在"

#### Scenario: 数据库层唯一约束兜底
- **WHEN** 并发场景或绕过应用层直接写库导致插入重复竞赛名称
- **THEN** 数据库唯一约束阻止插入，返回约束冲突错误
