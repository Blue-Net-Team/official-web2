## Why

后端 API 已更新，在 `CompetitionBriefDTO` 中新增了 `logoFileId` 字段，并将 `logoUrl` 标记为废弃。前端需要适配这个变更，使用 `logoFileId` 通过文件下载接口获取竞赛Logo图片。

当前前端直接使用 `logoUrl` 显示图片，但 `logoUrl` 是 MinIO 内部地址，前端无法直接访问。

## What Changes

- 更新 `CompetitionBriefDTO` 接口定义，添加 `logoFileId` 字段
- 修改 `CompetitionCard` 组件，使用 `/api/v1/file/download/{logoFileId}` 获取Logo图片
- 更新相关类型定义和API调用

## Capabilities

### Modified Capabilities

- `frontend-competition-display`: 竞赛展示功能适配新的API字段

## Impact

- 类型定义变更：`CompetitionBriefDTO` 接口添加 `logoFileId` 字段
- 组件变更：`CompetitionCard` 组件修改图片获取方式
- 用户体验：无变化，Logo正常显示
