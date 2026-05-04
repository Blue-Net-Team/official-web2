# Tasks

## Phase 1: 创建 AssessmentAnswer 和 AssessmentQuestion 的 Repository 和 DomainService

- [x] Task 1.1: 创建 AssessmentAnswerRepository 接口
  - [x] 1.1.1: 定义 updateFileId(answerId, fileId, submitTime) 方法
  - [x] 1.1.2: 定义 findById(answerId) 方法用于权限检查

- [x] Task 1.2: 创建 AssessmentQuestionRepository 接口
  - [x] 1.2.1: 定义 updateAttachmentId(questionId, fileId) 方法
  - [x] 1.2.2: 定义 findById(questionId) 方法用于权限检查

- [x] Task 1.3: 创建 AssessmentAnswerDomainService 接口
  - [x] 1.3.1: 定义 updateWorkFile(answerId, fileVO) 方法

- [x] Task 1.4: 创建 AssessmentQuestionDomainService 接口
  - [x] 1.4.1: 定义 updateAttachment(questionId, fileVO) 方法

- [x] Task 1.5: 实现 AssessmentAnswerRepositoryImpl
  - [x] 1.5.1: 使用 AssessmentAnswerMapper 实现 updateFileId 方法
  - [x] 1.5.2: 使用 AssessmentAnswerMapper 实现 findById 方法

- [x] Task 1.6: 实现 AssessmentQuestionRepositoryImpl
  - [x] 1.6.1: 使用 AssessmentQuestionMapper 实现 updateAttachmentId 方法
  - [x] 1.6.2: 使用 AssessmentQuestionMapper 实现 findById 方法

- [x] Task 1.7: 实现 AssessmentAnswerDomainServiceImpl
  - [x] 1.7.1: 调用 AssessmentAnswerRepository 更新 file_id 和 submit_time

- [x] Task 1.8: 实现 AssessmentQuestionDomainServiceImpl
  - [x] 1.8.1: 调用 AssessmentQuestionRepository 更新 attachment_id

- [x] Task 1.9: 在 UserRepository 中添加更新二维码的方法
  - [x] 1.9.1: 添加 updateQrcode(userId, qrcodeVO) 方法（如果需要）

## Phase 2: 在 FileService 中添加其他文件类型的上传方法

- [x] Task 2.1: 在 FileService 接口中添加 uploadAssessmentAttachment 方法
  - [x] 2.1.1: 定义方法签名，接收 questionId 和 MultipartFile

- [x] Task 2.2: 在 FileService 接口中添加 uploadAssessmentWork 方法
  - [x] 2.2.1: 定义方法签名，接收 answerId 和 MultipartFile

- [x] Task 2.3: 在 FileService 接口中添加 uploadQrcode 方法
  - [x] 2.3.1: 定义方法签名，接收 qrcodeType 和 MultipartFile

- [x] Task 2.4: 在 FileServiceImpl 中实现 uploadAssessmentAttachment
  - [x] 2.4.1: 保存文件到 MinIO
  - [x] 2.4.2: 调用 AssessmentQuestionDomainService 更新 attachment_id

- [x] Task 2.5: 在 FileServiceImpl 中实现 uploadAssessmentWork
  - [x] 2.5.1: 保存文件到 MinIO
  - [x] 2.5.2: 调用 AssessmentAnswerDomainService 更新 file_id 和 submit_time

- [x] Task 2.6: 在 FileServiceImpl 中实现 uploadQrcode
  - [x] 2.6.1: 保存文件到 MinIO
  - [x] 2.6.2: 根据二维码类型更新 User 表或创建 QRCode 表记录

## Phase 3: 在 FileUploadController 中实现其他上传接口

- [x] Task 3.1: 实现 uploadAssessmentAttachment 接口
  - [x] 3.1.1: 接收 questionId 参数和文件
  - [x] 3.1.2: 调用 FileService.uploadAssessmentAttachment
  - [x] 3.1.3: 返回文件信息

- [x] Task 3.2: 实现 uploadAssessmentWork 接口
  - [x] 3.2.1: 接收 answerId 参数和文件
  - [x] 3.2.2: 调用 FileService.uploadAssessmentWork
  - [x] 3.2.3: 返回文件信息

- [x] Task 3.3: 实现 uploadSelfQrcode 接口
  - [x] 3.3.1: 接收文件
  - [x] 3.3.2: 调用 FileService.uploadQrcode
  - [x] 3.3.3: 返回文件信息

- [x] Task 3.4: 实现 uploadGroupQrcode 接口
  - [x] 3.4.1: 接收文件
  - [x] 3.4.2: 调用 FileService.uploadQrcode
  - [x] 3.4.3: 返回文件信息

## Phase 4: 实现文件下载 Controller 和权限校验

- [x] Task 4.1: 创建 FileDownloadService 接口
  - [x] 4.1.1: 定义 download(fileId, currentUser) 方法

- [x] Task 4.2: 实现 FileDownloadServiceImpl
  - [x] 4.2.1: 实现权限校验逻辑
  - [x] 4.2.2: WORK 类型：校验当前用户是否是提交者或角色 >= MEMBER
  - [x] 4.2.3: ASSESSMENT_ATTACHMENT 类型：校验用户方向是否匹配考题方向
  - [x] 4.2.4: AVATAR 类型：根据关联表决定权限
  - [x] 4.2.5: NORMAL_IMG/QRCODE 类型：公开访问
  - [x] 4.2.6: 设置适当的 Content-Type 和 Content-Disposition 头

- [x] Task 4.3: 实现 FileDownloadController
  - [x] 4.3.1: 创建 GET /api/v1/file/download/{fileId} 接口
  - [x] 4.3.2: 调用 FileDownloadService 下载文件
  - [x] 4.3.3: 处理文件不存在场景（404）
  - [x] 4.3.4: 处理权限拒绝场景（403）
  - [x] 4.3.5: 支持大文件流式下载

## Phase 5: 测试各类型文件上传下载流程

- [x] Task 5.1: 编写单元测试
  - [x] 5.1.1: 测试考题附件上传更新 AssessmentQuestion.attachment_id
  - [x] 5.1.2: 测试考题作品上传更新 AssessmentAnswer.file_id
  - [x] 5.1.3: 测试二维码上传更新相关表
  - [x] 5.1.4: 测试文件下载权限校验

- [x] Task 5.2: 编写集成测试
  - [x] 5.2.1: 测试完整的上传流程
  - [x] 5.2.2: 测试不同权限场景的文件下载

- [x] Task 5.3: 运行完整回归测试套件

# Task Dependencies
- [Task 2.4] depends on [Task 1.8]
- [Task 2.5] depends on [Task 1.7]
- [Task 3.1] depends on [Task 2.4]
- [Task 3.2] depends on [Task 2.5]
- [Task 3.3] depends on [Task 2.6]
- [Task 3.4] depends on [Task 2.6]
- [Task 4.2] depends on [Task 4.1]
- [Task 4.3] depends on [Task 4.2]
- [Task 5.1] depends on [Task 3.4, Task 4.3]
- [Task 5.2] depends on [Task 5.1]
- [Task 5.3] depends on [Task 5.2]
