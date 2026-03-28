## Why

当前文件上传下载模块的实现不符合DDD（领域驱动设计）规范，主要问题包括：
1. 应用层（Application Service）直接注入并使用了MyBatis-Plus的Mapper（如FileMapper、UserMapper等），这属于基础设施层细节
2. 应用层直接调用Repository接口进行数据库操作，应该通过领域服务完成
3. 应用层处理了业务校验逻辑（如题目是否存在），这些应该在领域层
4. 接口层传递了领域值对象（UserVO）给应用层，应该只传递userId

## What Changes

### 重构 FileServiceImpl（应用层）
- 移除所有Mapper的直接注入
- 移除所有Repository的直接调用
- 改为调用领域服务完成业务逻辑

### 重构 FileDownloadServiceImpl（应用层）
- 移除所有Mapper的直接注入（FileMapper、UserMapper、AssessmentAnswerMapper等）
- 改为调用领域服务获取VO和进行权限校验

### 扩展 FileDomainService（领域层）
- 添加 getFileById 方法
- 添加 getAnswerByFileId 方法
- 添加 getQuestionByAttachmentId 方法

### 创建 QrcodeDomainService（领域层）
- 处理二维码的保存和查询业务逻辑

### 修改 FileUploadController（接口层）
- 将 UserVO 改为只传递 userId

## Capabilities

### Refactored Capabilities

- `file-upload-handler`: 重构为符合DDD规范的文件上传处理
- `file-download-handler`: 重构为符合DDD规范的文件下载处理

## Impact

- **代码模块**:
  - `FileServiceImpl.java` - 移除Mapper和Repository调用，改为调用领域服务
  - `FileDownloadServiceImpl.java` - 移除Mapper注入，改为调用领域服务
  - `FileDomainService.java` - 扩展领域服务方法
  - 新增 `QrcodeDomainService.java` - 创建二维码领域服务
  - `FileUploadController.java` - 修改参数传递方式
- **架构改进**:
  - 应用层不再直接操作数据库和Entity
  - 所有业务逻辑下沉到领域层
  - 接口层与领域层解耦
