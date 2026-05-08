## 1. 基础设施与数据模型

- [x] 1.1 修改 `application.yml`：`spring.servlet.multipart.max-file-size` 和 `max-request-size` 提升至 500MB（兼容旧接口过渡期内的大文件）
- [x] 1.2 `tb_file` 表新增 `status` 字段（枚举：PENDING, ACTIVE, REJECTED），创建 Flyway 迁移脚本
- [x] 1.3 `File` 实体、`FileDO`、`FileVO` 新增 `status` 字段及对应转换逻辑
- [x] 1.4 `FileUploadController` 的 `POST /api/v1/file/upload` 标记 `@Deprecated`，保留向后兼容

## 2. ObjectStorage 接口扩展

- [x] 2.1 `ObjectStorage` 接口新增 `getPresignedUploadUrl(FileType, String, String, long, Duration)` 方法声明
- [x] 2.2 `ObjectStorage` 接口新增 `getPresignedDownloadUrl(FileType, String, Duration)` 方法声明
- [x] 2.3 `MinioObjectStorage` 实现两个预签名方法，使用 `MinioClient.getPresignedObjectUrl`
- [x] 2.4 `AliyunOssObjectStorage` 实现两个预签名方法，使用 `OSS.generatePresignedUrl`

## 3. 预签名上传接口（后端）

- [x] 3.1 创建 `PrepareUploadCommand` / `PrepareUploadResult` DTO
- [x] 3.2 创建 `ConfirmUploadCommand` / `ConfirmUploadResult` DTO
- [x] 3.3 `FileUploadController` 新增 `POST /api/v1/file/prepare-upload`，权限校验（AVATAR/NORMAL_IMG 允许匿名，其余需登录）后调用 DomainService
- [x] 3.4 `FileUploadController` 新增 `POST /api/v1/file/confirm-upload`，校验 callbackToken，调用 DomainService 确认上传
- [x] 3.5 callbackToken 使用 JWT 实现：含 fileId、预期 MD5 hash、过期时间

## 4. 下载接口改造

- [x] 4.1 `FileDownloadController` 改造：权限校验通过后，生成预签名 GET URL，返回 302 Redirect
- [x] 4.2 批量下载 ZIP 改造：`FileAppService.downloadBatch()` 改为流式输出到 `ServletOutputStream`，移除 `ByteArrayOutputStream`

## 5. 上传确认与校验

- [x] 5.1 `FileDomainServiceImpl` 新增 `prepareUpload` 方法：生成随机文件名、插入 PENDING 记录、调用 `ObjectStorage.getPresignedUploadUrl`
- [x] 5.2 `FileDomainServiceImpl` 新增 `confirmUpload` 方法：校验 JWT token、向 OSS HEAD 请求获取 ETag、比对 MD5、更新状态为 ACTIVE/REJECTED
- [x] 5.3 校验失败时调用 `ObjectStorage.delete` 清理 OSS 对象
- [x] 5.4 （可选强校验）增加文件魔数检查（jpeg/png/pdf 头字节）

## 6. 安全与审计

- [x] 6.1 预签名 URL 生成时绑定 Content-Type 和 objectKey（随机 UUID）
- [x] 6.2 预签名 URL 过期时间配置化（默认 PUT 15min / GET 10min）
- [ ] 6.3 上传确认失败时记录审计日志（IP、userId、预期 hash、实际 hash）—— 暂不实现，现有审计体系需额外扩展
- [x] 6.4 匿名 prepare 接口限流：同一 IP 短时间内高频调用触发限流

## 7. 测试

- [x] 7.1 `MinioObjectStorageTest` 补充预签名 URL 生成测试
- [x] 7.2 `AliyunOssObjectStorageTest` 补充预签名 URL 生成测试（Mockito）
- [x] 7.3 预签名上传流程集成测试：prepare → mock OSS HEAD → confirm
- [x] 7.4 批量下载 ZIP 流式输出测试：验证内存占用不随文件大小增长
- [x] 7.5 匿名上传权限测试：未登录只能 prepare AVATAR/NORMAL_IMG

## 8. 验证与部署

- [x] 8.1 编译主应用：`mvn clean compile -pl src/backend -am`
- [x] 8.2 运行新增单元测试和集成测试
- [x] 8.3 确认旧接口 `POST /api/v1/file/upload` 仍可用（向后兼容）
- [x] 8.4 提供 Nginx 配置建议文档：`client_max_body_size`（可收紧）、`proxy_read_timeout` 等参数
- [x] 8.5 提供前端对接文档：三步流程（prepare → 直传 OSS → confirm）
