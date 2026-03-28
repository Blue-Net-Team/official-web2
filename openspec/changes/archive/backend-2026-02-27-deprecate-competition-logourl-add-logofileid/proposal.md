## Why

当前竞赛API返回的 `logoUrl` 是 MinIO 内部存储地址（如 `http://minio:9000/normal_img/xxx.jpg`），前端无法直接访问该地址获取图片内容。同时，文件下载接口 `/api/v1/file/download/{fileId}` 需要 `fileId` 参数，但竞赛API没有返回 `logoFileId`。

这导致前端无法正确显示竞赛Logo图片，需要通过后端代理访问文件。

## What Changes

- 修改 `CompetitionBriefDTO` 和 `CompetitionDetailDTO`，新增 `logoFileId` 字段
- 将 `logoUrl` 字段标记为 `@Deprecated`，表示即将废弃
- 修改 `CompetitionBriefVO` 和 `CompetitionVO`，新增 `logoFileId` 字段
- 修改 `CompetitionMapper.xml` 中的查询SQL，同时返回 `logo_file_id`
- 修改 `CompetitionConverter`，在转换时复制 `logoFileId`

## Capabilities

### Modified Capabilities

- `competition-management`: 竞赛DTO和VO增加 `logoFileId` 字段，标记 `logoUrl` 为废弃

## Impact

- API响应结构变更：新增 `logoFileId` 字段
- 向后兼容：`logoUrl` 字段仍然保留但标记为废弃
- 前端需要迁移：从使用 `logoUrl` 改为使用 `logoFileId` 调用下载接口
