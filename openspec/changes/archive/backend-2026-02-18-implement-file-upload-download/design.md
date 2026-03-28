## Context

当前系统已实现文件上传的基础框架：
- 头像上传接口 `updateUserAvatar` 已实现，通过 `FileServiceImpl` 调用 `UserDomainService` 更新 User 表的 avatar_id 字段
- 文件存储使用 MinIO OSS 实现
- File 表已定义，存储文件元信息（name, type, url）
- 需要实现其他文件类型的上传和下载功能

文件类型定义在 FileType 枚举中：
- `AVATAR`: 头像（用户头像、报名头像）- 已实现
- `NORMAL_IMG`: 普通图片（介绍图片等）
- `ASSESSMENT_ATTACHMENT`: 考题附件
- `WORK`: 考生作品/答案文件
- `QRCODE`: 二维码

## Goals / Non-Goals

**Goals:**
- 实现考题附件上传功能，上传后更新 AssessmentQuestion 表的 attachment_id 字段
- 实现考题作品上传功能，上传后更新 AssessmentAnswer 表的 file_id 和 submit_time 字段
- 实现二维码上传功能，上传后更新 User 表的 wechat_qrcode 字段或创建 QRCode 表记录
- 实现文件下载接口，支持不同类型文件的下载和权限验证
- 所有上传功能遵循现有架构模式：Controller -> Application Service -> Domain Service -> Repository

**Non-Goals:**
- 不修改现有文件存储机制（MinIO）
- 不修改现有数据库表结构
- 不实现文件内容解析（如图片压缩、PDF处理等）
- 不使用事件监听器方式（FileSaveEventListener）

## Decisions

### 1. 不使用事件监听器，直接在应用层调用领域服务
**决策**: 移除 FileSaveEventListener，在 FileServiceImpl 中直接调用对应的 DomainService 更新业务表。

**理由**:
- 代码更直观，逻辑更清晰
- 减少异步带来的复杂性
- 与现有头像上传实现保持一致

### 2. 按文件类型创建对应的领域服务
**决策**: 为 AssessmentAnswer 和 AssessmentQuestion 创建独立的 DomainService 和 Repository。

**理由**:
- 遵循 DDD 分层架构
- 便于后续扩展和维护
- 与 UserDomainService 的设计保持一致

### 3. 文件权限校验策略
**决策**: 根据文件类型走不同的权限校验链：
- `WORK`: 校验当前用户是否是提交者 或 角色 >= MEMBER
- `ASSESSMENT_ATTACHMENT`: 校验用户方向是否匹配考题方向
- `AVATAR`: 公开或需登录（根据关联表决定）
- `NORMAL_IMG` / `QRCODE`: 公开访问

**理由**:
- 灵活应对不同文件类型的权限需求
- 与现有架构文档中的文件访问流程图保持一致

### 4. 事务处理
**决策**: 应用层 Service 方法使用 `@Transactional`，确保文件保存和业务表更新在同一事务中。

**理由**:
- 避免文件已保存但业务表更新失败导致的数据不一致
- 便于错误回滚

## Risks / Trade-offs

| Risk | Mitigation |
|------|------------|
| 事务超时：大文件上传可能导致事务长时间持有 | 文件实际上传 OSS 应在事务外进行，事务内只更新元数据 |
| 并发问题：同一业务ID多次上传文件 | 使用数据库唯一约束或乐观锁处理 |
| 旧文件残留：更新关联后旧文件记录未清理 | 添加定时任务清理无关联的孤儿文件 |

## Migration Plan

1. **Phase 1**: 创建 AssessmentAnswer 和 AssessmentQuestion 的 Repository 和 DomainService
2. **Phase 2**: 在 FileService 中添加其他文件类型的上传方法
3. **Phase 3**: 在 FileUploadController 中实现其他上传接口
4. **Phase 4**: 实现文件下载 Controller 和权限校验
5. **Phase 5**: 测试各类型文件上传下载流程

**回滚策略**: 代码回滚到上一版本，数据库无 schema 变更。

## Open Questions

<!-- 无遗留问题 -->
