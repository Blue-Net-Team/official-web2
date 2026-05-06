## 1. 配置层

- [x] 1.1 创建 `AliyunOssJudgeConfig.java`，按 `storage.provider=aliyun-oss` 条件初始化 `OSS` Bean，使用 `ObjectStorageProperties.AliyunOss` 中的 endpoint、accessKeyId、accessKeySecret，并设置 `destroyMethod = "shutdown"`

## 2. 存储实现层

- [x] 2.1 创建 `AliyunOssJudgeAssetStorage.java`，实现 `JudgeAssetStorage` 接口
- [x] 2.2 实现 `get(objectKey)`：使用 `ossClient.getObject(bucket, objectKey)` 读取内容并返回 `byte[]`
- [x] 2.3 实现 `put(objectKey, content, contentType)`：使用 `ObjectMetadata` 设置 `Content-Type` 和 `Content-Length`，调用 `ossClient.putObject`
- [x] 2.4 实现 `delete(objectKey)`：调用 `ossClient.deleteObject`
- [x] 2.5 添加 `@ConditionalOnExpression("'${storage.provider:minio}' == 'aliyun-oss'")` 和 `@ConditionalOnBean(OSS.class)` 注解，确保仅在阿里云 OSS provider 时加载
- [x] 2.6 统一异常处理：捕获 `OSSException` 和 `ClientException`，包装为 `RuntimeException` 抛出

## 3. 测试层

- [x] 3.1 创建 `AliyunOssJudgeAssetStorageTest.java`，使用 Mockito 模拟 `OSS` 客户端
- [x] 3.2 编写 `get` 方法测试：验证 `getObject` 被正确调用，返回内容匹配
- [x] 3.3 编写 `put` 方法测试：验证 `putObject` 被正确调用，`ObjectMetadata` 中 Content-Type 和 Content-Length 正确
- [x] 3.4 编写 `delete` 方法测试：验证 `deleteObject` 被正确调用

## 4. 验证

- [x] 4.1 编译判题服务：`cd src/judge-service && ./mvnw compile`
- [x] 4.2 运行单元测试：`./mvnw test -Dtest=AliyunOssJudgeAssetStorageTest`
- [x] 4.3 确认 MinIO provider 下判题服务仍能正常编译启动（无回归）
