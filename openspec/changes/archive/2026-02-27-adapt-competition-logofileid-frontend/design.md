## 设计概述

前端适配后端 API 变更，使用 `logoFileId` 替代 `logoUrl` 获取竞赛Logo图片。

## 数据流

```
┌─────────────────────────────────────────────────────────────┐
│                      后端 API                                │
│  GET /api/v1/competitions                                   │
│  Response:                                                  │
│  {                                                          │
│    "id": 1,                                                 │
│    "name": "蓝桥杯",                                         │
│    "logoUrl": "http://minio/...",  // 废弃                   │
│    "logoFileId": 123,              // 新增                   │
│    ...                                                      │
│  }                                                          │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│              CompetitionBriefDTO (type.ts)                   │
│  - id: number                                               │
│  - name: string                                             │
│  - logoUrl: string | null  // 保留但不再使用                  │
│  - logoFileId: number | null  // 新增                        │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│              CompetitionCard 组件                            │
│  图片URL: /api/v1/file/download/{logoFileId}                │
│  <img src={`/api/v1/file/download/${competition.logoFileId}`} │
└─────────────────────────────────────────────────────────────┘
```

## 文件变更清单

| 文件路径 | 变更类型 | 变更内容 |
|---------|---------|---------|
| `src/apis/schema/type.ts` | 修改 | `CompetitionBriefDTO` 接口添加 `logoFileId` 字段 |
| `src/components/Home/CompetitionCard/index.tsx` | 修改 | 使用 `logoFileId` 构建下载URL |

## API 调用方式

### 变更前

```tsx
<Image
  src={competition.logoUrl}
  alt={competition.name}
  width={64}
  height={44}
/>
```

### 变更后

```tsx
<Image
  src={`/api/v1/file/download/${competition.logoFileId}`}
  alt={competition.name}
  width={64}
  height={44}
/>
```

## 注意事项

1. `logoFileId` 可能为 `null`，需要处理无Logo的情况
2. 文件下载接口需要认证，确保用户已登录
3. 图片加载失败时显示默认图标（保持现有行为）
