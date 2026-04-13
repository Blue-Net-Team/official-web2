## Why

当前文件上传接口（8个）将文件存储与业务逻辑耦合在一起。`FileServiceImpl` 依赖了 7 个领域服务，违反了单一职责原则。每次新增文件相关的业务需求都需要修改文件上传服务。需要将文件上传解耦为纯粹的文件存储操作，业务关联由各自领域的接口负责。

## What Changes

- **BREAKING** 统一文件上传接口：将 8 个业务混合上传接口合并为 `POST /api/v1/file/upload`，仅负责文件存储（MinIO + tb_file），接受 `type` 参数指定文件类型
- **BREAKING** 删除所有业务混合上传接口（avatar/assessment/qrcode/introduce-image/competition）
- 新增用户头像更新接口：`PUT /api/v1/users/avatar`，接受 fileId
- 新增考核题目附件更新接口：`PUT /api/v1/admin/assessment-questions/{id}/attachment`，接受 fileId
- 答案提交接口补充校验：`POST/PUT /api/v1/assessment-answers` 新增方向匹配校验和 fileId 类型校验（原来在上传接口中的校验）
- 重构二维码管理接口：`POST /api/v1/admin/qrcodes` 接受 fileId + type，替代混合上传，QrcodeServiceImpl 移除 FileService 依赖
- 新增介绍图片管理接口：`POST /api/v1/admin/introduce-images` 接受 fileId + type + description + DELETE
- 新增竞赛 Logo 更新接口：`PUT /api/v1/admin/competitions/{id}/logo` 接受 fileId
- 新增竞赛封面更新接口：`PUT /api/v1/admin/competitions/{id}/cover` 接受 fileId
- **BREAKING** 删除竞赛多图接口（POST/DELETE images），改为仅 logo + 封面各一张，tb_competition 新增 cover_file_id 字段；删除 CompetitionImageDTO、AddCompetitionImageRequestDTO、ResponseMessageCompetitionImage 多图相关 DTO
- 移除 ImageType 枚举（仅剩 COMPETITION，竞赛改用 cover_file_id 后无用），介绍图片系统不再区分类型；tb_introduce_image 删除 type 列；IntroduceImageController GET 移除 type 必填参数
- 删除死代码：`ResponseMessageFileInfo.java`
- 更新前端 `file.service.ts`：统一上传方法 `upload(file, type)`
- 更新前端报名页、个人头像页、考核答题页调用新接口
- 更新前端 CompetitionCard 组件：优先使用 coverFileId，fallback 到 introduceImageFileId
- 更新 CSRF 白名单：`/api/v1/file/upload` 替换 `/api/v1/file/upload/avatar`
- 涉及 37 个后端源文件、19 个需更新的后端测试文件、6 个前端文件的全面重构

## Capabilities

### New Capabilities
- `unified-file-upload`: 统一文件上传接口，纯粹的文件存储操作（上传到 MinIO + 记录 tb_file），不涉及任何业务逻辑

### Modified Capabilities
- `backend-file-upload-handler`: 从 8 个业务混合接口重构为纯文件上传，删除所有业务逻辑依赖
- `backend-file-download-handler`: 无功能变更，但需更新相关测试
- `backend-user-profile`: 新增用户头像更新接口（通过 fileId）
- `backend-competition-management`: 新增 logo/封面更新接口，移除多图片接口，新增 cover_file_id 字段
- `backend-introduce-image-management`: 新增管理接口（创建/删除介绍图片记录）
- `backend-csrf-protection`: 更新 CSRF 白名单路径
- `backend-evaluation-system`: 答案提交接口补充方向匹配和 fileId 校验（从上传接口迁入）

## Impact

- **后端 API**：所有文件上传接口变更（breaking change），新增多个业务关联接口
- **后端服务层**：`FileService`/`FileServiceImpl` 大幅简化，移除 7 个领域服务依赖
- **前端 API 层**：`file.service.ts` 方法签名变更
- **前端页面**：报名页、个人头像页、考核答题页需更新调用方式
- **测试**：需更新所有文件上传相关测试用例
- **数据库**：tb_competition 新增 cover_file_id 字段（Flyway 迁移）
