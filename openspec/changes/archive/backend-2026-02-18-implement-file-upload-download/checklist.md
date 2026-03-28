# Checklist

## Phase 1: 创建 AssessmentAnswer 和 AssessmentQuestion 的 Repository 和 DomainService

- [x] AssessmentAnswerRepository 接口已创建，包含 updateFileId 和 findById 方法
- [x] AssessmentQuestionRepository 接口已创建，包含 updateAttachmentId 和 findById 方法
- [x] AssessmentAnswerDomainService 接口已创建，包含 updateWorkFile 方法
- [x] AssessmentQuestionDomainService 接口已创建，包含 updateAttachment 方法
- [x] AssessmentAnswerRepositoryImpl 已实现，使用 AssessmentAnswerMapper
- [x] AssessmentQuestionRepositoryImpl 已实现，使用 AssessmentQuestionMapper
- [x] AssessmentAnswerDomainServiceImpl 已实现，调用 Repository 更新 file_id 和 submit_time
- [x] AssessmentQuestionDomainServiceImpl 已实现，调用 Repository 更新 attachment_id
- [x] UserRepository 中已添加更新二维码的方法（如需要）

## Phase 2: 在 FileService 中添加其他文件类型的上传方法

- [x] FileService 接口中已添加 uploadAssessmentAttachment 方法
- [x] FileService 接口中已添加 uploadAssessmentWork 方法
- [x] FileService 接口中已添加 uploadQrcode 方法
- [x] FileServiceImpl 中已实现 uploadAssessmentAttachment，调用 AssessmentQuestionDomainService
- [x] FileServiceImpl 中已实现 uploadAssessmentWork，调用 AssessmentAnswerDomainService
- [x] FileServiceImpl 中已实现 uploadQrcode，更新 User 表或创建 QRCode 表记录

## Phase 3: 在 FileUploadController 中实现其他上传接口

- [x] uploadAssessmentAttachment 接口已实现，接收 questionId 和文件
- [x] uploadAssessmentWork 接口已实现，接收 answerId 和文件
- [x] uploadSelfQrcode 接口已实现，调用 FileService.uploadQrcode
- [x] uploadGroupQrcode 接口已实现，调用 FileService.uploadQrcode

## Phase 4: 实现文件下载 Controller 和权限校验

- [x] FileDownloadService 接口已创建，包含 download 方法
- [x] FileDownloadServiceImpl 已实现，包含所有权限校验逻辑
- [x] WORK 类型权限校验已实现：提交者或角色 >= MEMBER
- [x] ASSESSMENT_ATTACHMENT 类型权限校验已实现：方向匹配
- [x] AVATAR 类型权限校验已实现：根据关联表决定
- [x] NORMAL_IMG/QRCODE 类型已设置为公开访问
- [x] FileDownloadController 已实现，包含 GET /api/v1/file/download/{fileId} 接口
- [x] 文件不存在场景已处理（404）
- [x] 权限拒绝场景已处理（403）
- [x] 大文件流式下载已支持

## Phase 5: 测试各类型文件上传下载流程

- [x] 考题附件上传测试已通过
- [x] 考题作品上传测试已通过
- [x] 二维码上传测试已通过
- [x] 文件下载权限校验测试已通过
- [x] 完整上传流程集成测试已通过
- [x] 不同权限场景的文件下载测试已通过
- [x] 完整回归测试套件已通过
