## Why

蓝网考核系统需要实现完整的文件上传下载功能，包括头像、考题附件、考生作品、二维码等多种文件类型的处理。目前只有头像上传接口已实现，其他上传接口和下载接口都是空的。需要按照现有架构模式，在应用层调用领域服务来更新业务表。

## What Changes

- 在 `FileService` 中添加其他文件类型的上传方法（考题附件、考题作品、二维码）
- 在 `FileUploadController` 中实现其他上传接口
- 创建 `AssessmentAnswerRepository` 和 `AssessmentQuestionRepository` 仓储接口
- 创建 `AssessmentAnswerDomainService` 和 `AssessmentQuestionDomainService` 领域服务接口及实现
- 实现 `FileDownloadController` 和 `FileDownloadService`，支持文件下载和权限校验
- 在 `UserRepository` 中添加更新二维码的方法

## Capabilities

### New Capabilities

- `file-upload-handler`: 文件上传处理，包括考题附件、考题作品、二维码上传
- `file-download-handler`: 文件下载处理，包括权限校验、文件流返回

### Modified Capabilities

- `file-upload-handler`: 扩展支持更多文件类型

## Impact

- **数据库表**: File、User、AssessmentQuestion、AssessmentAnswer、QRCode 表
- **代码模块**:
  - `FileService.java` - 添加其他文件类型的上传方法
  - `FileUploadController.java` - 实现其他上传接口
  - `FileDownloadController.java` - 实现文件下载接口
  - 新增 `AssessmentAnswerRepository.java` 和 `AssessmentQuestionRepository.java`
  - 新增 `AssessmentAnswerDomainService.java` 和 `AssessmentQuestionDomainService.java` 及其实现
  - `UserRepository.java` - 添加更新二维码方法
- **API 接口**: 完善文件上传相关接口，新增文件下载接口
