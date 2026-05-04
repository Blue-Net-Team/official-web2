# Checklist

## Phase 1: 扩展 FileDomainService（领域层）

- [ ] FileDomainService 接口已扩展
  - [ ] getFileById(Long fileId) 方法已添加
  - [ ] getAnswerByFileId(Long fileId) 方法已添加
  - [ ] getQuestionByAttachmentId(Long attachmentId) 方法已添加

- [ ] FileDomainServiceImpl 已实现
  - [ ] getFileById 方法通过 FileRepository 查询
  - [ ] getAnswerByFileId 方法通过 AssessmentAnswerRepository 查询
  - [ ] getQuestionByAttachmentId 方法通过 AssessmentQuestionRepository 查询

## Phase 2: 创建 QrcodeDomainService（领域层）

- [ ] QrcodeDomainService 接口已创建
  - [ ] saveQrcode(FileVO, QrcodeType) 方法已定义

- [ ] QrcodeDomainServiceImpl 已实现
  - [ ] saveQrcode 方法调用 QrcodeRepository 保存
  - [ ] 二维码类型验证已实现

## Phase 3: 重构 FileServiceImpl（应用层）

- [ ] FileServiceImpl 已重构
  - [ ] qrcodeRepository 字段已移除
  - [ ] assessmentQuestionRepository.findById 调用已移除
  - [ ] assessmentAnswerRepository.findById 调用已移除

- [ ] QrcodeDomainService 已注入
  - [ ] QrcodeDomainService 依赖已添加
  - [ ] uploadQrcode 方法改为调用 QrcodeDomainService.saveQrcode()

- [ ] 上传方法已重构
  - [ ] uploadAssessmentAttachment 通过领域服务处理
  - [ ] uploadAssessmentWork 通过领域服务处理

## Phase 4: 重构 FileDownloadServiceImpl（应用层）

- [ ] FileDownloadServiceImpl 已重构
  - [ ] 所有 Mapper 注入已移除（FileMapper、UserMapper等）
  - [ ] FileDomainService 已注入
  - [ ] AssessmentAnswerDomainService 已注入
  - [ ] AssessmentQuestionDomainService 已注入

- [ ] downloadFile 方法已重构
  - [ ] 改为通过 FileDomainService.getFileById() 获取 FileVO
  - [ ] 改为通过领域服务进行权限校验

- [ ] 权限校验已移到领域层
  - [ ] WORK 类型权限校验：是否为提交者或角色 >= MEMBER
  - [ ] ASSESSMENT_ATTACHMENT 类型权限校验：方向是否匹配
  - [ ] AVATAR、NORMAL_IMG、QRCODE 公开访问

## Phase 5: 修改 FileUploadController（接口层）

- [ ] 接口层参数已修改
  - [ ] updateUserAvatar 改为只传递 userId
  - [ ] 其他上传方法参数已优化（如适用）

## Phase 6: 验证和测试

- [ ] 编译检查已通过
  - [ ] mvn compile 成功，无编译错误

- [ ] 单元测试已通过
  - [ ] 文件上传流程测试通过
  - [ ] 文件下载权限校验测试通过
