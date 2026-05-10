## Why

判题服务（`src/judge-service`）目前仅支持 MinIO 作为对象存储后端，缺少阿里云 OSS 适配。当部署环境使用阿里云 OSS（`storage.provider=aliyun-oss`）时，判题服务因无法找到 `JudgeAssetStorage` 实现而启动失败，导致算法题判题功能不可用。

## What Changes

- 新增阿里云 OSS 客户端配置类 `AliyunOssJudgeConfig`，按 `storage.provider=aliyun-oss` 条件初始化 `OSS` Bean
- 新增 `AliyunOssJudgeAssetStorage` 实现 `JudgeAssetStorage` 接口，支持判题资产的读取、写入和删除
- 新增单元测试 `AliyunOssJudgeAssetStorageTest`，使用 Mockito 覆盖三种操作
- 判题服务 `pom.xml` 已包含 `aliyun-sdk-oss` 依赖，无需新增依赖

## Capabilities

### New Capabilities
- `judge-service-aliyun-oss-storage`: 判题服务阿里云 OSS 对象存储适配，提供判题资产（测试用例、标准答案等）的读写删能力

### Modified Capabilities
- （无现有 spec 需要修改）

## Impact

- **代码范围**：仅影响 `src/judge-service` 的 `infrastructure.config` 和 `infrastructure.storage` 包
- **配置影响**：`application.yml` 中已预留 `storage.aliyun-oss` 配置，无需新增配置项
- **API 影响**：无外部 API 变更
- **兼容性**：与现有 MinIO 实现互斥切换，通过 `storage.provider` 控制，不影响已有 MinIO 部署
