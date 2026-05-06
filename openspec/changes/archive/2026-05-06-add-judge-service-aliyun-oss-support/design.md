## Context

判题服务（`src/judge-service`）使用 `JudgeAssetStorage` 接口抽象判题资产的存储操作，当前仅有 `MinioJudgeAssetStorage` 实现。主应用（`src/backend`）已通过 `AliyunOssObjectStorage` 和 `AliyunOssConfig` 完成阿里云 OSS 适配，判题服务需要复用同一 SDK 和配置模式补齐该能力。

`judge-service` 的 `pom.xml` 已引入 `aliyun-sdk-oss:3.18.3`，`application.yml` 已预留 `storage.aliyun-oss` 配置项，`ObjectStorageProperties` 已定义 `AliyunOss` record，因此无需新增依赖或配置结构。

## Goals / Non-Goals

**Goals:**
- 当 `storage.provider=aliyun-oss` 时，判题服务能正常启动并完成判题资产的读取、写入、删除
- 保持与 `MinioJudgeAssetStorage` 相同的行为契约（接口层面无差异）
- 提供对应的 Mockito 单元测试

**Non-Goals:**
- 不修改主应用的阿里云 OSS 实现
- 不支持断点续传、分片上传等大文件高级特性（判题资产为文本/代码，通常 KB 级别）
- 不引入 STS 临时凭证或 RAM 角色等高级安全特性

## Decisions

**1. 独立配置类而非复用主应用 Bean**
- 判题服务是独立进程，与主应用不共享 Spring 上下文，因此需要自建 `OSS` Bean
- 复用 `ObjectStorageProperties` 中的 `aliyunOss` 字段，保持配置一致性

**2. `AliyunOssJudgeAssetStorage` 直接操作 `OSS` 客户端**
- 不引入额外的 Repository 或 Service 层，因为 `JudgeAssetStorage` 本身就是基础设施层抽象
- 参考 `MinioJudgeAssetStorage` 的异常处理方式：捕获 SDK 异常后统一包装为 `RuntimeException`

**3. 使用 `ObjectMetadata` 设置 Content-Type 和 Content-Length**
- 阿里云 OSS `putObject(String, String, InputStream)` 不自动推断 MIME 类型
- 通过 `ObjectMetadata` 显式设置 `Content-Type` 和 `Content-Length`，确保文件下载时响应头正确

**4. 判题专用 bucket 隔离**
- 复用 `JudgeStorageProperties.bucket()` 作为目标 bucket，与主应用业务文件 bucket 隔离
- 不在 `AliyunOssJudgeAssetStorage` 中自动创建 bucket（阿里云 OSS bucket 创建涉及地域、权限等控制台配置，不适合代码自动创建）

## Risks / Trade-offs

- **[Risk] 阿里云 OSS `doesBucketExist` 性能差** → **Mitigation**: 不在判题资产存储中调用 `doesBucketExist`，假设 bucket 已预创建。健康检查由运维在部署时验证。
- **[Risk] 判题资产体积超出内存** → **Mitigation**: 判题资产（测试用例、标准答案）通常为 KB 级别文本，当前 `byte[]` 模式足够。若未来支持大文件，需改为流式处理。

## Migration Plan

无需迁移。此变更仅新增条件 Bean，不影响现有 MinIO 部署。切换方式为修改环境变量 `STORAGE_PROVIDER=aliyun-oss` 并填写 `ALIYUN_OSS_ENDPOINT`/`ALIYUN_OSS_AK`/`ALIYUN_OSS_SK`。
