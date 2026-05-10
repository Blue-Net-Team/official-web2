## ADDED Requirements

### Requirement: 判题服务支持阿里云 OSS 作为对象存储后端
当 `storage.provider=aliyun-oss` 时，判题服务 SHALL 使用阿里云 OSS 存储和读取判题资产。

#### Scenario: 使用阿里云 OSS 读取判题资产
- **WHEN** 判题流程需要加载测试用例或标准答案
- **THEN** 系统从阿里云 OSS 判题专用 bucket 中读取对应 objectKey 的内容

#### Scenario: 使用阿里云 OSS 写入判题资产
- **WHEN** 测试数据生成流程创建新的测试用例
- **THEN** 系统将测试用例内容写入阿里云 OSS 判题专用 bucket，并设置正确的 Content-Type

#### Scenario: 使用阿里云 OSS 删除判题资产
- **WHEN** 测试数据重新生成或题目被删除
- **THEN** 系统从阿里云 OSS 判题专用 bucket 中删除对应 objectKey 的对象

### Requirement: 阿里云 OSS 判题资产存储实现 JudgeAssetStorage 接口
`AliyunOssJudgeAssetStorage` SHALL 完全实现 `JudgeAssetStorage` 接口定义的三个方法。

#### Scenario: 读取资产
- **WHEN** 调用 `get(objectKey)`
- **THEN** 返回该 objectKey 对应的文件字节内容

#### Scenario: 写入资产
- **WHEN** 调用 `put(objectKey, content, contentType)`
- **THEN** 将字节内容写入 OSS，并附带 Content-Type 和 Content-Length 元数据

#### Scenario: 删除资产
- **WHEN** 调用 `delete(objectKey)`
- **THEN** 从 OSS 中删除该 objectKey 对应的对象

### Requirement: 阿里云 OSS 客户端配置条件化加载
阿里云 OSS 客户端 SHALL 仅在 `storage.provider=aliyun-oss` 时初始化，避免与 MinIO 客户端冲突。

#### Scenario: provider 为 aliyun-oss
- **WHEN** `storage.provider` 设置为 `aliyun-oss`
- **THEN** Spring 容器创建 `OSS` Bean 和 `AliyunOssJudgeAssetStorage` Bean

#### Scenario: provider 为 minio
- **WHEN** `storage.provider` 设置为 `minio`
- **THEN** Spring 容器不创建阿里云 OSS 相关 Bean，仅加载 `MinioJudgeAssetStorage`
