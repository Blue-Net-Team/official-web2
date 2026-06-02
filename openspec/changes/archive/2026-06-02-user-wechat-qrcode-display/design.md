## Context

数据库表 `tb_user.qrcode_id`、领域实体 `User.qrcodeId`、基础设施层 `UserRepositoryImpl.convertToVO()` 和 `MemberRepositoryImpl.convertToEntity()` 均已完整实现二维码数据链路（经 `tb_qrcode` 关联到 `tb_file` 获取预签名 URL）。`MemberDetailDTO`（后端 Java）和 `MemberResult` 也已包含 `wechatQrcode` 字段。

但数据在应用层和 API 层被截断：
- `UserInfoAppServiceImpl.getMyInfo()` 未将 `UserVO.wechatQrcode` 放入 `UserInfoResult`
- `UserInfoResult`、`UserInfo`（API DTO）无二维码字段
- `updateProfile` 全链路（Command → DTO → DomainService → Repository）不支持更新二维码
- 前端 `type.ts` 的 `UserInfo` 和 `MemberDetailDTO` 缺少二维码字段，所有 Profile 组件无二维码 UI

## Goals / Non-Goals

**Goals:**
- 已登录用户可在个人主页查看和更新自己的微信二维码
- 访客可在成员详情页查看任意团队成员的微信二维码
- 复用现有文件上传基础设施（预签名上传 + `FileType.QRCODE`）

**Non-Goals:**
- 不改数据库 schema（`qrcode_id` 已存在）
- 不修改二维码管理后台（admin/qrcode 是咨询群/考核群二维码，与个人二维码无关）
- 不做二维码生成（用户上传自己的微信二维码图片）
- 不增加新的文件类型或权限规则

## Decisions

### 1. 二维码展示位置：ProfileSidebar

**决策**：在个人主页和成员详情页的左侧 `ProfileSidebar` 组件中新增微信二维码展示区域，位于基本信息（学院/专业/年级）和方向信息之间。

**理由**：
- 二维码属于"个人名片"信息，与头像、姓名、学院等放在同一卡片中更符合用户心智模型
- `ProfileInfoDisplay`（右侧基本信息区）已有密集的文字字段，加入图片会打破排版
- `ProfileSidebar` 已是复用组件（个人主页 + 成员详情共用），一次改动两处受益

**替代方案**：放在 `ProfileInfoDisplay` 中 —— 被拒绝，因为右侧区域字段密集且不含图片类元素

### 2. 二维码上传：复用现有预签名上传流程，无需裁剪

**决策**：复用 `fileService.upload(file, 'QRCODE')` 预签名上传流程，前端不需要 `AvatarCropModal` 裁剪。

**理由**：
- 微信二维码是矩形图片，不需要圆形裁剪
- 现有 `FileType.QRCODE` 枚举和文件上传基础设施已完整支持
- 与头像上传保持一致的上传模式，降低心智负担

### 3. 更新方式：走 `PUT /api/v1/user/info` 统一更新，而非独立接口

**决策**：将 `qrcodeFileId` 作为 `UpdateProfileRequestDTO` 的可选字段，走现有的 `PUT /api/v1/user/info` 接口更新。

**理由**：
- 二维码是用户个人资料的一部分，与头像、昵称等属于同一聚合
- 避免新增独立接口带来的额外维护成本
- 前端编辑表单可一次性提交所有资料变更

**替代方案**：新增 `PUT /api/v1/users/qrcode` 独立接口 —— 被拒绝，属于过度设计

### 4. 编辑权限：所有人可编辑

**决策**：所有角色（包括 CANDIDATE）均可上传/修改自己的微信二维码。

**理由**：
- 二维码是个人联系方式，不涉及团队权限或敏感信息
- 现有 `updateProfile` 的权限校验（仅限制用户名/性别/学院/专业/方向）不适用于二维码

## Risks / Trade-offs

| 风险 | 缓解措施 |
|------|----------|
| 用户上传非二维码图片 | 前端提示"请上传微信二维码图片"，但不强制校验图片内容（避免过度复杂） |
| 旧二维码文件成为孤儿文件 | 用户更新二维码时旧文件留在文件表，由现有的 `orphan-file-cleanup` 定时任务清理 |
| 成员详情页二维码公开暴露 | `qrcode` 文件类型在 `CLAUDE.md` 中已定义为"公开可见"，符合现有安全策略 |
| 前端类型与后端 DTO 不同步 | 后端 `MemberDetailDTO` 已有 `wechatQrcode`，前端 `type.ts` 需补全 |

## Migration Plan

无需迁移。数据库字段和文件存储链路已就绪，仅需代码层打通。

## Open Questions

-（无）
