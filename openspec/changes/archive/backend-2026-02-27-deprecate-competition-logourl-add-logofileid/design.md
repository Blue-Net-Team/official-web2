## 设计概述

本次变更旨在解决竞赛API返回的 `logoUrl` 无法直接访问的问题，通过新增 `logoFileId` 字段，让前端能够使用现有的文件下载接口获取图片内容。

## 数据流变更

### 当前数据流（存在问题）

```
┌─────────────────┐     SQL JOIN      ┌─────────────────┐
│ tb_competition  │◄─────────────────►│ tb_file         │
│ - logo_file_id  │                   │ - id            │
└────────┬────────┘                   │ - url           │
         │                            └─────────────────┘
         │
         ▼
┌─────────────────────────┐
│ CompetitionBriefVO      │
│ - logoUrl (来自file.url)│◄── 前端无法直接访问
└───────────┬─────────────┘
            │
            ▼
┌─────────────────────────┐
│ CompetitionBriefDTO     │
│ - logoUrl               │◄── 返回给前端
└─────────────────────────┘
```

### 新数据流

```
┌─────────────────┐     SQL JOIN      ┌─────────────────┐
│ tb_competition  │◄─────────────────►│ tb_file         │
│ - logo_file_id  │                   │ - id            │
└────────┬────────┘                   │ - url           │
         │                            └─────────────────┘
         │
         ▼
┌─────────────────────────┐
│ CompetitionBriefVO      │
│ - logoUrl               │
│ - logoFileId (新增)     │◄── 用于调用下载接口
└───────────┬─────────────┘
            │
            ▼
┌─────────────────────────┐
│ CompetitionBriefDTO     │
│ - logoUrl (@Deprecated) │
│ - logoFileId (新增)     │◄── 返回给前端
└─────────────────────────┘
```

## 接口变更

### GET /api/v1/competitions

**响应变更：**

```json
// 变更前
{
  "id": 1,
  "name": "蓝桥杯",
  "shortName": "蓝桥杯",
  "logoUrl": "http://minio:9000/normal_img/xxx.jpg",
  "summary": "..."
}

// 变更后
{
  "id": 1,
  "name": "蓝桥杯",
  "shortName": "蓝桥杯",
  "logoUrl": "http://minio:9000/normal_img/xxx.jpg",
  "logoFileId": 123,
  "summary": "..."
}
```

### GET /api/v1/competitions/{id}

**响应变更：**

```json
// 变更前
{
  "id": 1,
  "name": "蓝桥杯",
  "shortName": "蓝桥杯",
  "logoUrl": "http://minio:9000/normal_img/xxx.jpg",
  "summary": "...",
  "detail": "...",
  "images": [...]
}

// 变更后
{
  "id": 1,
  "name": "蓝桥杯",
  "shortName": "蓝桥杯",
  "logoUrl": "http://minio:9000/normal_img/xxx.jpg",
  "logoFileId": 123,
  "summary": "...",
  "detail": "...",
  "images": [...]
}
```

## 前端使用方式

### 展示Logo图片

```html
<!-- 变更前（无法工作） -->
<img src="{{logoUrl}}" />

<!-- 变更后 -->
<img src="/api/v1/file/download/{{logoFileId}}" />
```

## 文件变更清单

| 文件路径 | 变更类型 | 变更内容 |
|---------|---------|---------|
| `CompetitionBriefVO.java` | 修改 | 新增 `logoFileId` 字段 |
| `CompetitionVO.java` | 修改 | 新增 `logoFileId` 字段 |
| `CompetitionBriefDTO.java` | 修改 | 新增 `logoFileId` 字段，标记 `logoUrl` 为 `@Deprecated` |
| `CompetitionDetailDTO.java` | 修改 | 新增 `logoFileId` 字段，标记 `logoUrl` 为 `@Deprecated` |
| `CompetitionMapper.xml` | 修改 | SQL查询增加 `logo_file_id` 字段映射 |
| `CompetitionConverter.java` | 修改 | 转换逻辑增加 `logoFileId` 字段复制 |

## 废弃策略

1. `logoUrl` 字段保留但标记为 `@Deprecated`，确保向后兼容
2. 在API文档中说明 `logoUrl` 已废弃，建议使用 `logoFileId`
3. 未来版本（如v2 API）可以完全移除 `logoUrl` 字段
