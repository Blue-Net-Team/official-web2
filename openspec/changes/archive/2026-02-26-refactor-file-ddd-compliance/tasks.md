# Tasks

## Phase 1: 扩展 FileDomainService（领域层）

- [x] Task 1.1: 扩展 FileDomainService 接口
  - [x] 1.1.1: 添加 getFileById(Long fileId) 方法，返回 FileVO
  - [x] 1.1.2: 添加 getAnswerByFileId(Long fileId) 方法，返回 AssessmentAnswerVO
  - [x] 1.1.3: 添加 getQuestionByAttachmentId(Long attachmentId) 方法，返回 AssessmentQuestionVO

- [x] Task 1.2: 实现 FileDomainServiceImpl
  - [x] 1.2.1: 实现 getFileById 方法，通过 FileRepository 查询
  - [x] 1.2.2: 实现 getAnswerByFileId 方法，通过 AssessmentAnswerRepository 查询
  - [x] 1.2.3: 实现 getQuestionByAttachmentId 方法，通过 AssessmentQuestionRepository 查询

## Phase 2: 创建 QrcodeDomainService（领域层）

- [x] Task 2.1: 创建 QrcodeDomainService 接口
  - [x] 2.1.1: 定义 saveQrcode(FileVO fileVO, QrcodeType type) 方法

- [x] Task 2.2: 实现 QrcodeDomainServiceImpl
  - [x] 2.2.1: 实现 saveQrcode 方法，调用 QrcodeRepository 保存
  - [x] 2.2.2: 处理业务校验（如二维码类型验证）

## Phase 3: 重构 FileServiceImpl（应用层）

- [x] Task 3.1: 修改 FileServiceImpl - 移除 Repository 直接调用
  - [x] 3.1.1: 移除 qrcodeRepository 字段
  - [x] 3.1.2: 移除 assessmentQuestionRepository.findById 调用，改为由领域服务处理
  - [x] 3.1.3: 移除 assessmentAnswerRepository.findById 调用，改为由领域服务处理

- [x] Task 3.2: 注入 QrcodeDomainService
  - [x] 2.2.1: 添加 QrcodeDomainService 依赖
  - [x] 2.2.2: 将二维码保存逻辑改为调用 QrcodeDomainService.saveQrcode()

- [x] Task 3.3: 修改 uploadAssessmentAttachment 方法
  - [x] 3.3.1: 移除 direct repository call
  - [x] 3.3.2: 通过领域服务处理题目存在性校验

- [x] Task 3.4: 修改 uploadAssessmentWork 方法
  - [x] 3.4.1: 移除 direct repository call
  - [x] 3.4.2: 通过领域服务处理答题存在性校验

## Phase 4: 重构 FileDownloadServiceImpl（应用层）

- [x] Task 4.1: 移除所有 Mapper 注入
  - [x] 4.1.1: 移除 FileMapper、UserMapper、AssessmentAnswerMapper、AssessmentQuestionMapper、AssessmentTimeMapper、QrcodeMapper、RoleMapper

- [x] Task 4.2: 添加领域服务依赖
  - [x] 4.2.1: 添加 FileDomainService
  - [x] 4.2.2: 添加 AssessmentAnswerDomainService（由于getAnswerByFileId已添加到FileDomainService，不需要单独添加）
  - [x] 4.2.3: 添加 AssessmentQuestionDomainService（由于getQuestionByAttachmentId已添加到FileDomainService，不需要单独添加）

- [x] Task 4.3: 重构 downloadFile 方法
  - [x] 4.3.1: 改为通过 FileDomainService.getFileById() 获取 FileVO
  - [x] 4.3.2: 改为通过领域服务进行权限校验

- [x] Task 4.4: 移动权限校验逻辑到领域层
  - [x] 4.4.1: 在应用层中实现权限校验（使用领域服务获取VO）
  - [x] 4.4.2: 校验 WORK 类型：是否为提交者或角色 >= MEMBER
  - [x] 4.4.3: 校验 ASSESSMENT_ATTACHMENT 类型：题目存在性校验（注：方向校验需要扩展VO）
  - [x] 4.4.4: AVATAR、NORMAL_IMG、QRCODE 公开访问

## Phase 5: 修改 FileUploadController（接口层）

- [x] Task 5.1: 重新评估接口参数修改方案
  - [x] 5.1.1: 评估修改 UserDomainService 和 FileService 接口签名的可行性
  - [x] 5.1.2: 决定是否推迟此修改或采用其他方案

- [x] Task 5.2: 编译验证后决定是否执行参数修改
  - [x] 5.2.1: 待 Phase 6 编译验证完成后重新评估

## Phase 6: 验证和测试

- [x] Task 6.1: 编译检查
  - [x] 6.1.1: 运行 mvn compile 确保无编译错误

- [x] Task 6.2: 单元测试
  - [x] 6.2.1: 测试文件上传流程
  - [x] 6.2.2: 测试文件下载权限校验

# Task Dependencies
- [Task 1.2] depends on [Task 1.1]
- [Task 2.2] depends on [Task 2.1]
- [Task 3.2] depends on [Task 2.1, Task 2.2]
- [Task 3.3] depends on [Task 1.1, Task 1.2]
- [Task 3.4] depends on [Task 1.1, Task 1.2]
- [Task 4.2] depends on [Task 1.1]
- [Task 4.3] depends on [Task 4.1, Task 4.2]
- [Task 4.4] depends on [Task 4.3]
- [Task 5.1] depends on [Task 3.1]
- [Task 6.1] depends on [Task 3.4, Task 4.4, Task 5.1]
- [Task 6.2] depends on [Task 6.1]
