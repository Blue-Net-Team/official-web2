## 1. 后端：统一文件上传接口

### Task 1: 重构 FileService 为纯文件上传

#### 测试边界
- 输入条件：MultipartFile + FileType 枚举
- 前置状态：MinIO 可用，tb_file 表存在
- 后置状态：文件存储到 MinIO 对应桶，tb_file 新增记录，返回 FileInfo

#### 测试用例
| 用例ID | 场景 | 输入 | 期望输出/行为 | 异常 |
|--------|------|------|---------------|------|
| TC-101 | 上传 AVATAR 类型 | file + AVATAR | 文件存入 avatar 桶，返回 FileInfo | - |
| TC-102 | 上传 WORK 类型 | file + WORK | 文件存入 work 桶，返回 FileInfo | - |
| TC-103 | 上传 NORMAL_IMG 类型 | file + NORMAL_IMG | 文件存入 normal-img 桶，返回 FileInfo | - |
| TC-104 | 上传 QRCODE 类型 | file + QRCODE | 文件存入 qrcode 桶，返回 FileInfo | - |
| TC-105 | 上传 ASSESSMENT_ATTACHMENT 类型 | file + ASSESSMENT_ATTACHMENT | 文件存入 assessment-attachment 桶，返回 FileInfo | - |
| TC-106 | FileService 无业务依赖 | 检查依赖注入 | 不包含 UserDomainService 等 7 个领域服务 | - |
| TC-107 | 非 AVATAR 类型未登录被拒绝 | 未登录 + type=WORK | - | 401 |
| TC-108 | type 参数缺失 | file + 无 type | - | 400 |

#### 涉及源文件
- `api/controller/v1/file/FileUploadController.java` — 删除 8 个旧接口，新增统一 `POST /api/v1/file/upload`
- `application/service/FileService.java` — 删除所有 uploadXxx 方法声明，新增 `uploadFile(file, type)`
- `application/service/impl/FileServiceImpl.java` — 删除所有业务逻辑和 7 个领域服务依赖，仅保留 `uploadFile`
- `domain/service/FileDomainService.java` — 确认接口不变
- `domain/service/impl/FileDomainServiceImpl.java` — 确认 saveFile 仅处理文件存储
- `domain/model/vo/FileVO.java` — 确认不变
- `domain/model/entity/File.java` — 确认不变
- `api/dto/file/FileInfo.java` — 确认不变

#### 涉及测试文件
- `application/service/impl/FileServiceImplTest.java` — **重写**：删除所有旧 uploadXxx 测试，新增 uploadFile 各类型测试 + 无业务依赖断言
- `api/controller/v1/file/FileUploadControllerIntegrationTest.java` — **重写**：删除旧 8 接口测试，新增统一上传接口测试
- `api/controller/v1/file/FileUploadDownloadIntegrationTest.java` — **更新**：上传部分改用新接口
- `infrastructure/repository/impl/MinioFileRepositoryTest.java` — 确认不变（底层存储逻辑不变）

#### 实现步骤（严格按顺序）
- [x] 1.1 编写 FileService 重构测试（红灯阶段）：测试 uploadFile(file, type) 各类型 + 未登录拒绝 + 无业务依赖
- [x] 1.2 领域层：确认 FileDomainService/FileDomainServiceImpl 的 saveFile 无需改动
- [x] 1.3 应用层：重构 FileService/FileServiceImpl，删除所有 uploadXxx 方法和 7 个领域服务依赖，新增 uploadFile(file, type)
- [x] 1.4 控制层：重构 FileUploadController，删除 8 个旧接口，新增统一 `POST /api/v1/file/upload`
- [x] 1.5 运行测试（绿灯阶段）
- [x] 1.6 重构优化

---

## 2. 后端：新增用户头像更新接口

### Task 2: UserProfileController 新增 PUT /api/v1/users/avatar

#### 测试边界
- 输入条件：已登录用户 + fileId (Long)
- 前置状态：用户存在，文件存在且类型为 AVATAR
- 后置状态：tb_user.avatar_id 更新为 fileId

#### 测试用例
| 用例ID | 场景 | 输入 | 期望输出/行为 | 异常 |
|--------|------|------|---------------|------|
| TC-201 | 成功更新头像 | fileId=100, 文件类型=AVATAR | tb_user.avatar_id=100，返回 200 | - |
| TC-202 | 文件不存在 | fileId=9999 | - | 404 DataNotFound |
| TC-203 | 文件类型不匹配 | fileId=100, 文件类型=WORK | - | 400 BadRequest |
| TC-204 | 未登录用户 | 无认证 | - | 401 |

#### 涉及源文件
- `api/controller/v1/user/UserProfileController.java` — 新增 PUT /api/v1/users/avatar
- `application/service/UserInfoService.java` — 新增 updateAvatar 方法
- `application/service/impl/UserInfoServiceImpl.java` — 实现 updateAvatar（校验文件类型 + 调用领域服务）
- `domain/service/UserDomainService.java` — 已有 updateUserAvatar，确认可用
- `domain/service/impl/UserDomainServiceImpl.java` — 已有实现，确认可用
- `domain/repository/UserRepository.java` — 已有 updateAvatar 重载，确认可用
- `infrastructure/repository/impl/UserRepositoryImpl.java` — 已有实现，确认可用

#### 涉及测试文件
- `application/service/impl/UserInfoServiceImplTest.java` — **新增** updateAvatar 测试用例
- `domain/service/impl/UserDomainServiceImplTest.java` — 确认已有 updateUserAvatar 测试覆盖

#### 实现步骤（严格按顺序）
- [x] 2.1 编写用户头像更新测试（红灯阶段）
- [x] 2.2 应用层：UserInfoService 新增 updateAvatar 方法（校验文件存在 + 类型为 AVATAR + 调用领域服务）
- [x] 2.3 控制层：UserProfileController 新增 PUT /api/v1/user/avatar 接口
- [x] 2.4 运行测试（绿灯阶段）
- [x] 2.5 重构优化

---

## 3. 后端：新增考核题目附件更新接口

### Task 3: AdminAssessmentQuestionController 新增 PUT attachment

#### 测试边界
- 输入条件：管理员 + questionId + fileId
- 前置状态：题目存在，文件存在且类型为 ASSESSMENT_ATTACHMENT
- 后置状态：tb_assessment_question.attachment_id 更新为 fileId

#### 测试用例
| 用例ID | 场景 | 输入 | 期望输出/行为 | 异常 |
|--------|------|------|---------------|------|
| TC-301 | 成功更新附件 | questionId=1, fileId=100 | attachment_id 更新，返回 200 | - |
| TC-302 | 题目不存在 | questionId=9999 | - | 404 DataNotFound |
| TC-303 | 文件不存在 | fileId=9999 | - | 404 DataNotFound |
| TC-304 | 文件类型不匹配 | fileId=100, 类型非 ASSESSMENT_ATTACHMENT | - | 400 BadRequest |

#### 涉及源文件
- `api/controller/v1/admin/AdminAssessmentQuestionController.java` — 新增 PUT attachment
- `application/service/AssessmentQuestionService.java` — 新增 updateAttachment 方法
- `application/service/impl/AssessmentQuestionServiceImpl.java` — 实现 updateAttachment
- `domain/service/AssessmentQuestionDomainService.java` — 已有 updateAttachment
- `domain/service/impl/AssessmentQuestionDomainServiceImpl.java` — 已有实现
- `domain/repository/AssessmentQuestionRepository.java` — 已有 updateAttachment
- `infrastructure/repository/impl/AssessmentQuestionRepositoryImpl.java` — 已有实现

#### 涉及测试文件
- `infrastructure/repository/impl/AssessmentQuestionRepositoryImplTest.java` — 确认已有覆盖

#### 实现步骤（严格按顺序）
- [x] 3.1 编写考核题目附件更新测试（红灯阶段）
- [x] 3.2 应用层：AssessmentQuestionService 新增 updateAttachment 方法（校验文件类型 + 调用领域服务）
- [x] 3.3 控制层：AdminAssessmentQuestionController 新增 PUT /api/v1/admin/assessment-questions/{id}/attachment
- [x] 3.4 运行测试（绿灯阶段）
- [x] 3.5 重构优化

---

## 4. 后端：答案提交接口补充校验

### Task 4: AssessmentAnswerService 补充方向匹配和 fileId 校验

当前答案提交接口（POST/PUT `/api/v1/assessment-answers`）缺少方向匹配校验和 fileId 校验，这些校验目前在 `uploadWork` 上传接口中。重构后上传变成纯存储，校验必须移到答案提交接口。

#### 测试边界
- 输入条件：已登录用户 + questionId + fileId（可选）
- 前置状态：题目存在，考核时间未过期
- 后置状态：仅当用户方向匹配且 fileId 有效时才创建/更新答案

#### 测试用例
| 用例ID | 场景 | 输入 | 期望输出/行为 | 异常 |
|--------|------|------|---------------|------|
| TC-401 | 正常提交答案（方向匹配 + 有效 fileId） | questionId=1, fileId=100 | 创建答案记录 | - |
| TC-402 | 方向不匹配 | 用户方向=FRONTEND, 题目考核方向=BACKEND | - | 403 "方向不匹配" |
| TC-403 | fileId 对应文件不存在 | fileId=9999 | - | 400 "文件不存在" |
| TC-404 | fileId 类型不是 WORK | fileId=100, 文件类型=AVATAR | - | 400 "文件类型不匹配" |
| TC-405 | fileId 为 null 但 content 有值 | fileId=null, content="答案内容" | 正常创建（非文件题） | - |
| TC-406 | 更新答案时 fileId 校验 | fileId=200, 文件类型非 WORK | - | 400 "文件类型不匹配" |

#### 涉及源文件
- `application/service/impl/AssessmentAnswerServiceImpl.java` — 在 createAnswer/updateAnswer 中增加方向匹配 + fileId 校验
- `domain/service/AssessmentQuestionDomainService.java` — 确认可返回题目所属考核的方向信息
- `domain/service/impl/AssessmentQuestionDomainServiceImpl.java` — 确认可获取考核时间中的方向

#### 涉及测试文件
- `application/service/impl/AssessmentAnswerServiceImplTest.java` — **新增** 方向匹配、fileId 存在性、fileId 类型校验测试用例
- `domain/service/impl/AssessmentAnswerDomainServiceImplTest.java` — 确认已有覆盖

#### 实现步骤（严格按顺序）
- [x] 4.1 编写答案提交校验测试（红灯阶段）：方向匹配、fileId 存在性、fileId 类型校验
- [x] 4.2 领域层：确认 AssessmentQuestionDomainService 可返回题目的考核方向信息
- [x] 4.3 应用层：AssessmentAnswerServiceImpl 在 createAnswer/updateAnswer 中增加方向匹配校验
- [x] 4.4 应用层：AssessmentAnswerServiceImpl 在 createAnswer/updateAnswer 中增加 fileId 存在性和类型校验
- [x] 4.5 运行测试（绿灯阶段）
- [x] 4.6 重构优化

---

## 5. 后端：重构二维码管理接口

### Task 5: AdminQrcodeController + QrcodeService 重构为接受 fileId

#### 测试边界
- 输入条件：管理员 + fileId + qrcodeType
- 前置状态：文件存在且类型为 QRCODE
- 后置状态：tb_qrcode 新增记录关联 fileId

#### 测试用例
| 用例ID | 场景 | 输入 | 期望输出/行为 | 异常 |
|--------|------|------|---------------|------|
| TC-501 | 成功创建二维码记录 | fileId=100, type=CONSULTATION | tb_qrcode 新增记录，返回 200 | - |
| TC-502 | 文件不存在 | fileId=9999 | - | 404 DataNotFound |
| TC-503 | 文件类型不匹配 | fileId=100, 类型非 QRCODE | - | 400 BadRequest |

#### 涉及源文件
- `api/controller/v1/admin/AdminQrcodeController.java` — POST 接口改为接受 fileId + type（不再接受 MultipartFile）
- `application/service/QrcodeService.java` — uploadConsultationQrcode(MultipartFile) 改为 createQrcode(fileId, type)
- `application/service/impl/QrcodeServiceImpl.java` — **重构**：移除对 FileService 的依赖，改为接受 fileId，校验文件存在+类型后调用 QrcodeDomainService
- `domain/service/QrcodeDomainService.java` — 确认 saveQrcode 可接受 fileId 参数
- `domain/service/impl/QrcodeDomainServiceImpl.java` — 确认实现

#### 涉及测试文件
- `api/controller/v1/qrcode/QrcodeControllerIntegrationTest.java` — **更新**：改为先上传文件再调用创建二维码接口
- `domain/service/impl/QrcodeDomainServiceImplTest.java` — 确认已有覆盖
- `application/service/impl/CompetitionServiceImplTest.java` — 确认无影响（QrcodeService 独立）

#### 实现步骤（严格按顺序）
- [x] 5.1 编写二维码创建重构测试（红灯阶段）
- [x] 5.2 应用层：QrcodeService 重构，uploadConsultationQrcode 改为 createQrcode(fileId)，移除 FileService 依赖
- [x] 5.3 控制层：AdminQrcodeController POST 接口改为接受 fileId
- [x] 5.4 运行测试（绿灯阶段）
- [x] 5.5 重构优化

---

## 6. 后端：新增介绍图片管理接口

### Task 6: 新建 AdminIntroduceImageController

#### 测试边界
- 输入条件：管理员 + fileId + description
- 前置状态：文件存在且类型为 NORMAL_IMG
- 后置状态：tb_introduce_image 新增记录

#### 测试用例
| 用例ID | 场景 | 输入 | 期望输出/行为 | 异常 |
|--------|------|------|---------------|------|
| TC-601 | 成功创建介绍图片 | fileId=100, description="..." | tb_introduce_image 新增记录，返回 200 | - |
| TC-602 | 文件不存在 | fileId=9999 | - | 404 DataNotFound |
| TC-603 | 文件类型不匹配 | fileId=100, 类型非 NORMAL_IMG | - | 400 BadRequest |
| TC-604 | 成功删除介绍图片 | id=1 | 删除记录 + 文件，返回 200 | - |
| TC-605 | 删除不存在的记录 | id=9999 | - | 404 DataNotFound |

#### 涉及源文件
- `api/controller/v1/admin/AdminIntroduceImageController.java` — **新建**（POST + DELETE）
- `application/service/IntroduceImageService.java` — 新增 createIntroduceImage / deleteIntroduceImage 方法
- `application/service/impl/IntroduceImageServiceImpl.java` — 实现（校验文件 + 调用领域服务）
- `application/converter/IntroduceImageConverter.java` — 确认可用
- `domain/service/IntroduceImageDomainService.java` — 已有 addIntroduceImage，确认可接受 fileId
- `domain/service/impl/IntroduceImageDomainServiceImpl.java` — 已有实现

#### 涉及测试文件
- `api/controller/v1/introduce/IntroduceImageControllerTest.java` — **更新**：新增管理接口测试
- `domain/service/impl/IntroduceImageDomainServiceImplTest.java` — 确认已有覆盖
- `application/service/impl/IntroduceImageServiceImplTest.java` — 确认已有覆盖

#### 实现步骤（严格按顺序）
- [x] 6.1 编写介绍图片管理测试（红灯阶段）
- [x] 6.2 应用层：IntroduceImageService 新增 createIntroduceImage / deleteIntroduceImage 方法
- [x] 6.3 控制层：新建 AdminIntroduceImageController（POST + DELETE）
- [x] 6.4 运行测试（绿灯阶段）
- [x] 6.5 重构优化

---

## 7. 后端：竞赛 Logo/封面更新接口 + 数据库迁移 + 清理旧图片接口

### Task 7: 竞赛接口重构 + Flyway 迁移

竞赛简化为 logo + 封面各一张。移除通过 tb_introduce_image 多图关联的方式，改为 tb_competition 表直接存储 logo_file_id + cover_file_id。同时移除 ImageType 枚举（仅剩 COMPETITION，已无用），介绍图片系统不再区分类型。

#### 测试用例
| 用例ID | 场景 | 输入 | 期望输出/行为 | 异常 |
|--------|------|------|---------------|------|
| TC-701 | 成功更新 Logo | competitionId=1, fileId=100 | logo_file_id 更新，返回 200 | - |
| TC-702 | 成功更新封面 | competitionId=1, fileId=200 | cover_file_id 更新，返回 200 | - |
| TC-703 | 竞赛不存在 | competitionId=9999 | - | 404 DataNotFound |
| TC-704 | 文件不存在 | fileId=9999 | - | 404 DataNotFound |
| TC-705 | 文件类型不匹配 | fileId=100, 类型非 NORMAL_IMG | - | 400 BadRequest |
| TC-706 | 公开竞赛列表返回 coverFileId | GET /competitions | coverFileId 可为 null | - |
| TC-707 | coverFileId 为 null 时 fallback | coverFileId=null, 有历史 introduce_image | introduceImageFileId 填充 | - |

#### 涉及源文件
- **Flyway 迁移** — 新增 `V{n}__add_competition_cover_file_id.sql`，ALTER TABLE tb_competition ADD cover_file_id BIGINT；新增 `V{n+1}__remove_introduce_image_type.sql`，ALTER TABLE tb_introduce_image DROP COLUMN type
- `domain/model/entity/Competition.java` — 新增 coverFileId 字段
- `domain/model/vo/CompetitionVO.java` — 新增 coverFileId 字段
- `domain/model/vo/CompetitionBriefVO.java` — 新增 coverFileId 字段
- `domain/model/enumerate/ImageType.java` — **删除**
- `domain/model/entity/IntroduceImage.java` — 删除 `type` 字段
- `domain/model/vo/IntroduceImageVO.java` — 删除 `type` 字段
- `api/dto/introduce/IntroduceImageDTO.java` — 删除 `type` 字段
- `domain/service/CompetitionDomainService.java` — 新增 updateCover 方法
- `domain/service/impl/CompetitionDomainServiceImpl.java` — 实现 updateCover
- `domain/service/IntroduceImageDomainService.java` — 移除 `ImageType` 参数，addIntroduceImage 改为 `(Long fileId, String description)`
- `domain/service/impl/IntroduceImageDomainServiceImpl.java` — 移除所有 `ImageType` 引用；删除 `getCompetitionImages`/`countCompetitionImages`/`addCompetitionImage`/`removeCompetitionImage`（竞赛不再通过 introduce_image 管理图片）
- `domain/repository/IntroduceImageRepository.java` — 移除 `findByType`/`findByTypeAndCompetitionId`/`countByTypeAndCompetitionId`（竞赛相关查询不再需要）
- `infrastructure/repository/impl/IntroduceImageRepositoryImpl.java` — 移除对应实现
- `infrastructure/repository/mapper/IntroduceImageMapper.java` — 移除按 type 查询的 SQL
- `domain/repository/CompetitionRepository.java` — 确认可更新 coverFileId
- `infrastructure/repository/impl/CompetitionRepositoryImpl.java` — 实现更新
- `application/service/CompetitionService.java` — 新增 updateLogo、updateCover 方法；删除 addCompetitionImage/deleteCompetitionImage
- `application/service/impl/CompetitionServiceImpl.java` — 实现：校验文件类型 + 调用领域服务；删除多图逻辑（addCompetitionImage 中 20 张限制等）
- `application/service/IntroduceImageService.java` — getIntroduceImages 移除 ImageType 参数，改为无参数返回全部
- `application/service/impl/IntroduceImageServiceImpl.java` — 同步移除 ImageType 参数
- `application/converter/CompetitionConverter.java` — 映射 coverFileId，实现 coverFileId fallback 到第一个 introduce_image；删除 convertToImageDTO 方法；convertToDetailDTO 删除 images 参数改为 coverFileId；convertToResponseDTO 的 introduceImageFileId 替换为 coverFileId
- `api/controller/v1/introduce/IntroduceImageController.java` — GET 移除 `type` 必填参数
- `api/controller/v1/admin/AdminCompetitionController.java` — 新增 PUT logo + PUT cover 接口；**删除** POST images + DELETE images/{imageId} 旧接口
- `api/dto/competition/CompetitionBriefDTO.java` — 新增 coverFileId 字段
- `api/dto/competition/CompetitionDetailDTO.java` — 新增 coverFileId 字段，**删除** `images`（`List<CompetitionImageDTO>`）多图字段
- `api/dto/competition/CompetitionResponseDTO.java` — `introduceImageFileId` **替换**为 `coverFileId`
- `api/dto/competition/CompetitionImageDTO.java` — **删除**
- `api/dto/competition/AddCompetitionImageRequestDTO.java` — **删除**
- `api/dto/competition/ResponseMessageCompetitionImage.java` — **删除**

#### 涉及测试文件
- `api/controller/v1/admin/AdminCompetitionControllerIntegrationTest.java` — **重写**：删除多图测试，新增 logo/cover 更新测试
- `api/controller/v1/competition/CompetitionControllerIntegrationTest.java` — **更新**：验证 coverFileId 返回
- `api/controller/v1/competition/CompetitionListIntegrationTest.java` — **更新**：验证列表 DTO 包含 coverFileId
- `api/controller/v1/competition/CompetitionControllerTest.java` — **更新**：断言从 `introduceImageFileId` 改为 `coverFileId`，`images` 改为 `coverFileId`
- `application/converter/CompetitionConverterTest.java` — **更新**：convertToBriefDTO 增加 coverFileId 断言，convertToDetailDTO 删除 images 断言改为 coverFileId，删除 convertToImageDTO 测试
- `domain/service/impl/CompetitionDomainServiceImplTest.java` — **新增** updateCover 测试
- `application/service/impl/CompetitionServiceImplTest.java` — **更新**：新增 updateLogo/updateCover 测试，删除多图相关测试
- `domain/service/impl/IntroduceImageDomainServiceImplTest.java` — **更新**：移除 ImageType 引用，删除竞赛相关测试（getCompetitionImages 等）
- `application/service/impl/IntroduceImageServiceImplTest.java` — **更新**：移除 ImageType 参数
- `api/controller/v1/introduce/IntroduceImageControllerTest.java` — **更新**：GET 接口不再需要 type 参数
- `infrastructure/repository/mapper/EnumMappingTest.java` — **更新**：移除 ImageType 枚举映射测试
- `infrastructure/repository/mapper/EntityCrudTest.java` — **更新**：移除 IntroduceImage 的 ImageType 字段测试
- `infrastructure/config/converter/EnumConverterTest.java` — **更新**：移除 ImageType 转换器测试

#### 实现步骤（严格按顺序）
- [x] 7.1 编写竞赛 Logo/封面更新 + ImageType 移除测试（红灯阶段）
- [x] 7.2 基础设施层：新增 Flyway 迁移，tb_competition 添加 cover_file_id 字段；tb_introduce_image 删除 type 列
- [x] 7.3 领域层：Competition 实体新增 coverFileId 字段，CompetitionVO/CompetitionBriefVO 同步
- [x] 7.4 领域层：删除 ImageType 枚举；IntroduceImage 实体/VO 删除 type 字段
- [x] 7.5 领域层：CompetitionDomainService 新增 updateCover 方法
- [x] 7.6 领域层：IntroduceImageDomainService/Repository 移除 ImageType 参数和竞赛相关方法（getCompetitionImages 等）
- [x] 7.7 应用层：CompetitionService 新增 updateLogo、updateCover 方法（校验文件类型）；删除 addCompetitionImage/deleteCompetitionImage
- [x] 7.8 应用层：IntroduceImageService 移除 ImageType 参数
- [x] 7.9 应用层：CompetitionConverter 映射 coverFileId，删除 convertToImageDTO，convertToDetailDTO 删除 images 改为 coverFileId
- [x] 7.10 控制层：AdminCompetitionController 新增 PUT logo + PUT cover；删除 POST images + DELETE images/{imageId}
- [x] 7.11 控制层：IntroduceImageController GET 移除 type 必填参数
- [x] 7.12 更新所有竞赛 DTO（新增 coverFileId，删除 images，替换 introduceImageFileId）
- [x] 7.13 删除死代码：CompetitionImageDTO、AddCompetitionImageRequestDTO、ResponseMessageCompetitionImage
- [x] 7.14 更新所有受影响测试
- [x] 7.15 运行测试（绿灯阶段）
- [x] 7.16 重构优化

---

## 8. 后端：更新 CSRF 白名单 + 全量旧代码清理

### Task 8: 安全配置更新和旧代码彻底删除

此任务在所有其他后端任务完成后执行，确保所有旧上传相关代码被彻底清除。

#### 测试用例
| 用例ID | 场景 | 输入 | 期望输出/行为 | 异常 |
|--------|------|------|---------------|------|
| TC-801 | 新上传路径免 CSRF | POST /api/v1/file/upload 无 CSRF token | 请求正常处理 | - |
| TC-802 | 旧路径已不存在 | POST /api/v1/file/upload/avatar | - | 404 |
| TC-803 | FileService 无旧方法 | 检查 FileService 接口 | 不包含 uploadXxx 方法 | - |
| TC-804 | QrcodeService 无 FileService 依赖 | 检查 QrcodeServiceImpl | 不注入 FileService | - |
| TC-805 | CompetitionService 无 uploadCompetitionImage | 检查 CompetitionServiceImpl | 不包含文件上传混合逻辑 | - |

#### 涉及源文件
- `infrastructure/security/csrf/CsrfTokenFilter.java` — PUBLIC_PATHS 中 `/api/v1/file/upload/avatar` → `/api/v1/file/upload`
- `application/service/FileService.java` — **最终确认**所有旧方法声明已删除
- `application/service/impl/FileServiceImpl.java` — **最终确认**所有旧方法和依赖已删除
- `api/controller/v1/file/FileUploadController.java` — **最终确认**仅保留统一上传接口

#### 涉及测试文件（逐一检查并更新）
- `application/service/impl/FileServiceImplTest.java` — **最终确认**旧测试已删除
- `api/controller/v1/file/FileUploadControllerIntegrationTest.java` — **最终确认**旧接口测试已删除
- `api/controller/v1/file/FileUploadDownloadIntegrationTest.java` — **最终确认**上传使用新接口

#### 无需修改的测试文件（经深度验证排除）
以下测试文件均通过 `fileMapper.insert()` 创建文件、不调用上传接口，或不涉及文件上传：
- Venue/Equipment/CompetitionList/CompetitionController 集成测试 — 直接 mapper 插入
- EnumMappingTest/EntityCrudTest — 仅枚举/实体验证
- User/Enroll/Competition/IntroduceImage/Qrcode 领域服务测试 — 已与 FileService 解耦
- Competition/Enroll/IntroduceImage 应用服务测试 — 不依赖 FileService
- MinioFileRepository/MockFile/MockQrcode/AssessmentQuestion/Member/AssessmentTime 等测试 — 不涉及上传

#### 实现步骤（严格按顺序）
- [x] 8.1 编写 CSRF 白名单更新测试（红灯阶段）
- [x] 8.2 基础设施层：CsrfTokenFilter PUBLIC_PATHS 更新为 /api/v1/file/upload
- [x] 8.3 全局搜索确认无代码引用旧上传路径（`/file/upload/avatar`、`/file/upload/assessment/work` 等）
- [x] 8.4 全局搜索确认无代码引用 FileService 旧方法（`uploadAvatar`、`uploadWork`、`uploadQrcode` 等）
- [x] 8.5 全局搜索确认 QrcodeServiceImpl 不再依赖 FileService
- [x] 8.6 逐一检查并修复上述所有测试文件
- [x] 8.7 运行全量测试 `mvn test`（绿灯阶段）
- [x] 8.8 代码格式化 `mvn spotless:apply`

---

## 9. 前端：API 层和页面更新

### Task 9: 前端 file.service.ts + 页面适配 + 类型定义更新

#### 测试边界
- 输入条件：前端调用文件上传
- 前置状态：前端使用旧的 uploadAvatar / uploadWork 方法
- 后置状态：前端使用统一 upload(file, type) 方法 + 各业务接口

#### 测试用例
| 用例ID | 场景 | 输入 | 期望输出/行为 | 异常 |
|--------|------|------|---------------|------|
| TC-901 | 报名页头像上传 | file + type=AVATAR | 调用统一上传，返回 fileId | - |
| TC-902 | 个人头像上传+关联 | file + type=AVATAR | 调用统一上传 → 调用 PUT /users/avatar | - |
| TC-903 | 考核作品上传 | file + type=WORK | 调用统一上传，不再传 questionId | - |
| TC-904 | 竞赛卡片封面展示 | coverFileId 存在 | 使用 coverFileId 下载 | - |
| TC-905 | 竞赛卡片封面 fallback | coverFileId=null, introduceImageFileId 存在 | 使用 introduceImageFileId 下载 | - |

#### 涉及前端文件

**API 服务层（必须修改）：**
- `src/apis/services/file.service.ts` — uploadAvatar → `upload(file, type)`，uploadWork → `upload(file, type)`；新增 `updateAvatar(fileId)`；下载方法不变

**类型定义（必须修改）：**
- `src/apis/schema/type.ts` — CompetitionBriefDTO 新增 `coverFileId: number | null`
- `src/apis/schema/enumerate.ts` — FileType 枚举不变（已包含所有类型）

**页面（必须修改）：**
- `src/app/(public)/(other)/enroll/page.tsx` — AvatarUpload 调用新 `upload(file, "AVATAR")`
- `src/components/Profile/ProfileSidebar/index.tsx` — 上传后额外调用 `updateAvatar(fileId)`
- `src/app/(public)/(other)/assessment/[timeId]/questions/[questionId]/page.tsx` — Upload.Dragger 调用新 `upload(file, "WORK")`，不再传 questionId

**展示组件（必须修改 - 竞赛封面 fallback）：**
- `src/components/CompetitionCard/index.tsx` — `introduceImageFileId` 改为优先使用 `coverFileId`，fallback 到 `introduceImageFileId`

**展示组件（无需修改 - 仅使用 fileId 下载，逻辑不变）：**
- `src/components/PublicNavbar/index.tsx` — avatarFileId 下载不变
- `src/components/Members/MemberCard/MemberCard.tsx` — avatarFileId 下载不变
- `src/components/Home/Competitions/CompetitionCard/index.tsx` — logoFileId 下载不变
- `src/components/Achievements/AchievementCard/index.tsx` — competitionLogoFileId 下载不变
- `src/components/Admin/EnrollManagement/EnrollmentCard/index.tsx` — avatarFileId 下载不变
- `src/components/Admin/EnrollManagement/EnrollmentDrawer/index.tsx` — avatarFileId 下载不变
- `src/components/Enroll/ConsultationQrcode/index.tsx` — fileId 下载不变
- `src/app/(public)/(other)/lab-environment/page.tsx` — imageFileId 下载不变
- `src/app/(public)/(other)/members/[id]/page.tsx` — avatarFileId 下载不变

**辅助组件（无需修改）：**
- `src/components/Profile/AvatarCropModal/index.tsx` — 仅负责裁剪，不涉及上传
- `src/apis/services/qrcode.service.ts` — 仅读取 fileId，不涉及上传

#### 实现步骤（严格按顺序）
- [x] 9.1 重构 `file.service.ts`：uploadAvatar/uploadWork → `upload(file, type)`；新增 `updateAvatar(fileId)`
- [x] 9.2 更新报名页 `enroll/page.tsx`：AvatarUpload 调用新 `upload(file, "AVATAR")`
- [x] 9.3 更新个人头像 `ProfileSidebar/index.tsx`：上传后调用 `updateAvatar(fileId)`
- [x] 9.4 更新考核页 `assessment/.../page.tsx`：Upload.Dragger 调用新 `upload(file, "WORK")`，不再传 questionId
- [x] 9.5 更新类型定义 `type.ts`：CompetitionBriefDTO 新增 `coverFileId` 字段
- [x] 9.6 更新 `CompetitionCard/index.tsx`：优先使用 coverFileId，fallback 到 introduceImageFileId
- [x] 9.7 前端构建验证：`pnpm build` 无报错
- [x] 9.8 前端 lint 验证：`pnpm lint` 无报错

---

## 10. 全量验证

### Task 10: 最终集成验证

#### 实现步骤（严格按顺序）
- [x] 10.1 后端全量测试：`mvn test`
- [x] 10.2 前端构建验证：`pnpm build`
- [x] 10.3 代码格式化：`mvn spotless:apply`
- [x] 10.4 全局搜索确认无残留旧上传代码引用

---

## 附录：完整受影响文件清单

### 后端源文件（按层分组）

**API 层（Controller + DTO）：**
1. `api/controller/v1/file/FileUploadController.java` — 重构
2. `api/controller/v1/admin/AdminQrcodeController.java` — 重构
3. `api/controller/v1/admin/AdminCompetitionController.java` — 新增 logo/cover，删除多图接口
4. `api/controller/v1/admin/AdminIntroduceImageController.java` — **新建**
5. `api/controller/v1/user/UserProfileController.java` — 新增 PUT avatar
6. `api/controller/v1/introduce/IntroduceImageController.java` — GET 移除 type 参数
7. `api/dto/file/FileInfo.java` — 确认不变
8. `api/dto/competition/CompetitionBriefDTO.java` — 新增 coverFileId
9. `api/dto/competition/CompetitionDetailDTO.java` — 新增 coverFileId，删除 images 字段
10. `api/dto/competition/CompetitionResponseDTO.java` — introduceImageFileId 替换为 coverFileId
11. `api/dto/competition/CompetitionImageDTO.java` — **删除**
12. `api/dto/competition/AddCompetitionImageRequestDTO.java` — **删除**
13. `api/dto/competition/ResponseMessageCompetitionImage.java` — **删除**
14. `api/dto/introduce/IntroduceImageDTO.java` — 删除 type 字段

**应用层（Service）：**
15. `application/service/FileService.java` — 重构
16. `application/service/impl/FileServiceImpl.java` — 重构
17. `application/service/QrcodeService.java` — 重构
18. `application/service/impl/QrcodeServiceImpl.java` — 重构（移除 FileService 依赖）
19. `application/service/CompetitionService.java` — 新增 updateLogo/updateCover，删除 addCompetitionImage
20. `application/service/impl/CompetitionServiceImpl.java` — 新增 updateCover，删除多图逻辑
21. `application/service/IntroduceImageService.java` — 新增 create/delete，移除 ImageType 参数
22. `application/service/impl/IntroduceImageServiceImpl.java` — 实现，移除 ImageType 参数
23. `application/service/UserInfoService.java` — 新增 updateAvatar
24. `application/service/impl/UserInfoServiceImpl.java` — 实现
25. `application/service/AssessmentQuestionService.java` — 新增 updateAttachment
26. `application/service/impl/AssessmentQuestionServiceImpl.java` — 实现
27. `application/service/impl/AssessmentAnswerServiceImpl.java` — 新增方向匹配 + fileId 校验
28. `application/converter/CompetitionConverter.java` — 映射 coverFileId，删除 convertToImageDTO
29. `application/converter/IntroduceImageConverter.java` — 确认可用

**领域层（Domain）：**
30. `domain/model/entity/Competition.java` — 新增 coverFileId
31. `domain/model/entity/IntroduceImage.java` — 删除 type 字段
32. `domain/model/vo/CompetitionVO.java` — 新增 coverFileId
33. `domain/model/vo/CompetitionBriefVO.java` — 新增 coverFileId
34. `domain/model/vo/IntroduceImageVO.java` — 删除 type 字段
35. `domain/model/enumerate/ImageType.java` — **删除**
36. `domain/service/CompetitionDomainService.java` — 新增 updateCover
37. `domain/service/impl/CompetitionDomainServiceImpl.java` — 实现 updateCover
38. `domain/service/IntroduceImageDomainService.java` — 移除 ImageType 参数，删除竞赛相关方法
39. `domain/service/impl/IntroduceImageDomainServiceImpl.java` — 移除 ImageType 引用，删除竞赛相关方法
40. `domain/repository/IntroduceImageRepository.java` — 移除按 type 查询的方法

**基础设施层（Infrastructure）：**
41. `infrastructure/repository/impl/IntroduceImageRepositoryImpl.java` — 移除按 type 查询实现
42. `infrastructure/repository/mapper/IntroduceImageMapper.java` — 移除按 type 查询的 SQL
43. `infrastructure/repository/impl/CompetitionRepositoryImpl.java` — 实现 coverFileId 更新
44. `infrastructure/security/csrf/CsrfTokenFilter.java` — 更新白名单
45. Flyway 迁移脚本 — 新增 cover_file_id 字段 + 删除 introduce_image.type 列

### 后端测试文件（需检查）

**专用上传测试（重写）：**
1. `application/service/impl/FileServiceImplTest.java` — 删除所有旧 uploadXxx 测试，新增 uploadFile 测试
2. `api/controller/v1/file/FileUploadControllerIntegrationTest.java` — 删除旧 8 接口测试，新增统一上传测试
3. `api/controller/v1/file/FileUploadDownloadIntegrationTest.java` — 更新上传调用为新接口

**需更新的测试：**
4. `api/controller/v1/qrcode/QrcodeControllerIntegrationTest.java` — QR 码接口改为 fileId 模式
5. `api/controller/v1/admin/AdminCompetitionControllerIntegrationTest.java` — 新增 logo/cover 测试，删除多图测试（20张限制等）
6. `api/controller/v1/competition/CompetitionControllerIntegrationTest.java` — 验证 coverFileId 返回
7. `api/controller/v1/competition/CompetitionListIntegrationTest.java` — 验证列表 DTO 包含 coverFileId
8. `domain/service/impl/CompetitionDomainServiceImplTest.java` — 新增 updateCover 测试
9. `application/service/impl/CompetitionServiceImplTest.java` — 新增 updateLogo/updateCover 测试，删除多图测试
10. `application/service/impl/AssessmentAnswerServiceImplTest.java` — 新增方向匹配 + fileId 校验测试
11. `application/service/impl/UserInfoServiceImplTest.java` — 新增 updateAvatar 测试
12. `application/converter/CompetitionConverterTest.java` — 更新 coverFileId 断言，删除 convertToImageDTO 测试，convertToDetailDTO 改为 coverFileId
13. `api/controller/v1/competition/CompetitionControllerTest.java` — introduceImageFileId 断言改为 coverFileId，images 断言改为 coverFileId
14. `domain/service/impl/IntroduceImageDomainServiceImplTest.java` — 移除 ImageType 引用，删除竞赛相关测试
15. `application/service/impl/IntroduceImageServiceImplTest.java` — 移除 ImageType 参数
16. `api/controller/v1/introduce/IntroduceImageControllerTest.java` — GET 接口不再需要 type 参数
17. `infrastructure/repository/mapper/EnumMappingTest.java` — 移除 ImageType 枚举映射测试
18. `infrastructure/repository/mapper/EntityCrudTest.java` — 移除 IntroduceImage 的 ImageType 字段测试
19. `infrastructure/config/converter/EnumConverterTest.java` — 移除 ImageType 转换器测试

**确认无需修改的测试（经深度验证排除）：**
- `api/controller/v1/venue/VenueControllerIntegrationTest.java` — 通过 fileMapper.insert() 创建文件，不调用上传接口
- `api/controller/v1/equipment/EquipmentControllerIntegrationTest.java` — 同上
- `api/controller/v1/introduce/IntroduceImageControllerIntegrationTest.java` — 仅 GET 测试，不涉及上传（注：ImageType 移除后 type 参数变化已在 Task 7 中覆盖）
- `domain/service/impl/UserDomainServiceImplTest.java` — 已与 FileService 解耦
- `domain/service/impl/EnrollDomainServiceImplTest.java` — 已与 FileService 解耦
- `domain/service/impl/QrcodeDomainServiceImplTest.java` — 领域服务层不依赖 FileService
- `domain/service/impl/AssessmentAnswerDomainServiceImplTest.java` — 领域服务层不涉及校验逻辑
- `application/service/impl/EnrollServiceImplTest.java` — 不依赖 FileService
- `infrastructure/repository/impl/MinioFileRepositoryTest.java` — 底层存储不变
- `infrastructure/repository/impl/AssessmentQuestionRepositoryImplTest.java` — 底层不变
- `infrastructure/repository/impl/MockFileRepository.java` — Mock 不变
- `infrastructure/repository/impl/MockQrcodeRepository.java` — Mock 不变
- `domain/service/impl/MemberDomainServiceImplTest.java` — 不涉及文件
- `domain/model/vo/QuestionContentJsonTest.java` — 不涉及上传
- `application/service/impl/AssessmentTimeServiceImplTest.java` — 不涉及上传

### 清理项（随 Task 7 一起执行）
- `api/dto/file/ResponseMessageFileInfo.java` — **死代码**，从未被使用，应删除
- `api/dto/competition/CompetitionImageDTO.java` — 多图模式 DTO，删除
- `api/dto/competition/AddCompetitionImageRequestDTO.java` — 仅被 addCompetitionImage 使用，删除
- `api/dto/competition/ResponseMessageCompetitionImage.java` — 仅被 addCompetitionImage 使用，删除
- `domain/model/enumerate/ImageType.java` — 仅剩 COMPETITION，竞赛改用 cover_file_id 后无用，删除

### 前端文件（20 个）

**必须修改（7 个）：**
1. `src/apis/services/file.service.ts`
2. `src/apis/schema/type.ts`
3. `src/app/(public)/(other)/enroll/page.tsx`
4. `src/components/Profile/ProfileSidebar/index.tsx`
5. `src/app/(public)/(other)/assessment/[timeId]/questions/[questionId]/page.tsx`
6. `src/components/CompetitionCard/index.tsx`

**无需修改（14 个）- 仅使用 fileId 下载或纯展示：**
7. `src/apis/services/qrcode.service.ts`
8. `src/apis/schema/assessment.dto.ts`
9. `src/apis/schema/enumerate.ts`
10. `src/components/Profile/AvatarCropModal/index.tsx`
11. `src/components/PublicNavbar/index.tsx`
12. `src/components/Members/MemberCard/MemberCard.tsx`
13. `src/components/Home/Competitions/CompetitionCard/index.tsx`
14. `src/components/Achievements/AchievementCard/index.tsx`
15. `src/components/Admin/EnrollManagement/EnrollmentCard/index.tsx`
16. `src/components/Admin/EnrollManagement/EnrollmentDrawer/index.tsx`
17. `src/components/Enroll/ConsultationQrcode/index.tsx`
18. `src/app/(public)/(other)/lab-environment/page.tsx`
19. `src/app/(public)/(other)/members/[id]/page.tsx`
20. `src/components/Profile/index.ts`
