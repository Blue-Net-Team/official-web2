## Context

当前 `FileUploadController` 包含 8 个上传接口，每个都混合了文件存储和业务逻辑。`FileServiceImpl` 依赖了 `UserDomainService`、`AssessmentQuestionDomainService`、`QrcodeDomainService`、`IntroduceImageDomainService`、`CompetitionDomainService` 共 7 个领域服务。

前端实际只使用了 2 个上传接口（avatar 和 work），其余 6 个的前端管理页面均未实现。

项目中已有"正确模式"：场地/设备管理接受 `imageFileId` 参数，文件上传和业务逻辑完全分离。

## Goals / Non-Goals

**Goals:**
- 文件上传接口仅负责文件存储（MinIO + tb_file），不涉及任何业务逻辑
- 各业务领域通过各自的接口关联 fileId
- 前端调用流程：先 upload → 得到 fileId → 调业务接口传入 fileId
- 竞赛图片简化为 logo + 封面各一张
- 所有相关测试同步更新

**Non-Goals:**
- 孤儿文件清理机制（后续独立处理）
- 文件下载接口变更（保持不变）
- 数据库表结构变更
- 新增前端管理页面（竞赛/二维码/介绍图片管理页面尚未实现）

## Decisions

### D1: 统一上传接口设计

**决策**: `POST /api/v1/file/upload` 接受 `file` + `type` (FileType) 参数

**替代方案**:
- 按类型保留多个上传路径（如 `/upload/avatar`, `/upload/work`）→ 违反简化目标
- 不限制 type 参数，自动推断 → 不可靠，不同业务场景文件类型可能相同

**理由**: 单一入口 + 显式类型参数最简洁。`type` 决定 MinIO 存储桶。

### D2: 未登录用户上传权限

**决策**: 接口使用 `AccessLevel.PUBLIC`，controller 内部校验：`type = AVATAR` 允许未登录，其他类型需认证。

**理由**: 报名页上传头像时用户未登录，这是唯一需要 PUBLIC 的场景。其他文件类型均需登录后操作。CSRF 白名单新增 `/api/v1/file/upload`。

### D3: 业务接口新增位置

**决策**: 业务接口添加到各自已有的 Controller 中，不新建 Controller。

| 业务操作 | 放置的 Controller | 方法 |
|---------|------------------|------|
| 用户头像 | `UserProfileController` | `PUT /api/v1/users/avatar` |
| 题目附件 | `AdminAssessmentQuestionController` | `PUT /api/v1/admin/assessment-questions/{id}/attachment` |
| 二维码 | `AdminQrcodeController` | `POST /api/v1/admin/qrcodes` |
| 介绍图片 | 新建 `AdminIntroduceImageController` | `POST /api/v1/admin/introduce-images` |
| 竞赛Logo | `AdminCompetitionController` | `PUT /api/v1/admin/competitions/{id}/logo` |
| 竞赛封面 | `AdminCompetitionController` | `PUT /api/v1/admin/competitions/{id}/cover` |

**理由**: 各业务接口归属各自领域 Controller，符合 DDD 分层。介绍图片管理目前没有 Admin Controller，需新建。

### D4: 竞赛封面设计

**决策**: 在 `tb_competition` 表新增 `cover_file_id` 字段（通过 Flyway 迁移），独立于 logo。

**理由**: 竞赛只需 logo + 封面各一张，不再需要 tb_introduce_image 的多图关联。但现有竞赛图片（tb_introduce_image 中 type=COMPETITION 的记录）仍需兼容展示。封面字段可为 null，公开接口优先使用封面，无封面则 fallback 到第一张 introduce image。

### D5: FileService 简化

**决策**: `FileService` 仅保留 `uploadFile(file, type)` 和 `downloadFile(fileId)` 方法。删除所有 `uploadXxx` 业务方法和对 7 个领域服务的依赖。

**理由**: 文件服务应该是纯粹的基础设施服务，不应感知业务领域。

### D6: 前端变更策略

**决策**: `file.service.ts` 提供 `upload(file: File, type: FileType)` 通用方法，替代 `uploadAvatar` 和 `uploadWork`。头像上传后需额外调用 `PUT /users/avatar` 更新关联。

**理由**: 与后端统一上传接口对应，减少方法数量。报名页头像流程不变（upload → 报名表传入 avatarId），个人头像页需增加一步调用。

## Risks / Trade-offs

- [孤儿文件] 上传后未调用业务接口关联会产生孤立文件 → 暂不处理，后续可加定时清理
- [Breaking Change] 所有 8 个旧上传接口删除 → 项目在开发环境，直接替换
- [报名头像流程] 报名页调用新 `upload` 接口后，仍然通过 `CreateEnrollmentRequestDTO.avatarId` 传入，流程不变
- [竞赛封面新增字段] 需要数据库迁移 → 新增 Flyway migration
