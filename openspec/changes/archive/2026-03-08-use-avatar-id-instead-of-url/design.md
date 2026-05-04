## Context

当前系统中，头像相关字段存在不一致：

1. **成员相关 DTO**：只返回 `avatarUrl`（如 `/api/v1/files/456`），但 URL 格式与实际下载接口 `/api/v1/file/download/{fileId}` 不一致
2. **报名相关 DTO**：同时返回 `avatarId` 和 `avatarUrl`

后端在 Repository 层通过额外查询 File 表获取 URL，增加了不必要的数据库查询开销。

## Goals / Non-Goals

**Goals:**
- 统一所有 DTO 使用 `avatarFileId` 字段返回头像文件 ID
- 移除 Repository 层中不必要的 File 表查询
- 保持 API 命名一致性（使用 `avatarFileId` 而非 `avatarId`，明确表示这是文件 ID）

**Non-Goals:**
- 不修改数据库表结构（`avatar_id` 列保持不变）
- 不修改文件下载接口
- 不修改前端代码（前端需要自行适配）

## Decisions

### 1. 字段命名选择：`avatarFileId`

**选择**：使用 `avatarFileId` 而非 `avatarId`

**理由**：
- 明确表示这是文件 ID，前端可以直接用于调用 `/api/v1/file/download/{fileId}`
- 与其他文件相关字段命名保持一致（如 `logoFileId`、`qrcodeFileId`）
- 避免与数据库列名 `avatar_id` 混淆

**备选方案**：
- `avatarId`：更简洁，但语义不够明确
- `avatarFileUrl`：与本次变更目标相悖

### 2. VO 层字段修改

**选择**：同时修改 VO 层，移除 `avatarUrl`，添加 `avatarFileId`

**理由**：
- 保持 DTO 和 VO 的一致性
- 遵循 DDD 分层原则，VO 应该承载领域模型数据

### 3. 是否保留 `avatarUrl` 作为废弃字段

**选择**：直接移除，不保留废弃字段

**理由**：
- 系统尚未上线，无需向后兼容
- 保留废弃字段会增加维护成本
- API 文档会明确标注变更

## Risks / Trade-offs

### Risk 1: 前端需要同步修改
**风险**：前端依赖 `avatarUrl` 字段，需要同步修改

**缓解措施**：
- 前后端协调发布
- API 文档明确标注变更
- 提供迁移指南

### Risk 2: 测试用例需要更新
**风险**：大量测试用例依赖 `avatarUrl` 字段

**缓解措施**：
- 统计受影响的测试文件
- 批量更新测试断言

## Migration Plan

1. **后端修改**：修改 DTO、VO、Converter、Repository
2. **测试更新**：更新所有相关测试
3. **API 文档更新**：确保 OpenAPI 注解正确
4. **前端适配**：前端修改头像 URL 构建逻辑
5. **联调验证**：前后端联调确认功能正常

**回滚策略**：如需回滚，恢复代码变更即可（无数据库变更）
