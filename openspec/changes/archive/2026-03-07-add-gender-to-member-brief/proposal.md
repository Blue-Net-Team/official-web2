## Why

前端成员列表展示需要显示成员性别信息，当前后端 `MemberBriefDTO` 缺少 `gender` 字段，导致前端无法正确展示成员性别。同时需要保持前后端数据结构一致性。

## What Changes

- 在 `MemberBriefDTO` 中新增 `gender` 字段
- 在 `MemberDetailDTO` 中新增 `gender` 字段（如尚未包含）
- 在数据库实体 `Member` 中已有 `gender` 字段，需确保正确映射到 DTO
- 更新相关的 VO 转换逻辑

## Capabilities

### New Capabilities

- `member-gender-field`: 为成员简要信息和详细信息接口添加性别字段支持

### Modified Capabilities

无

## Impact

- **API**: `GET /api/v1/members` 返回的 `MemberBriefDTO` 新增 `gender` 字段
- **API**: `GET /api/v1/members/{id}` 返回的 `MemberDetailDTO` 新增 `gender` 字段（如尚未包含）
- **DTO**: `MemberBriefDTO`、`MemberDetailDTO` 新增字段
- **Converter**: `MemberConverter` 需要映射 `gender` 字段
- **前端**: 前端 `MemberBrief` 类型需要同步更新
