## Context

当前应用已有 `FileRepository` 接口定义在 `com.bluenet.web.domain.repository` 包中，定义了文件存储的基本操作（保存和加载）。application.yml 中已配置 MinIO 服务器连接信息，包括 endpoint、port、accessKey、secretKey。

FileType 枚举定义了 5 种文件类型：AVATAR、NORMAL_IMG、ASSESSMENT_ATTACHMENT、WORK、QRCODE，每种类型对应一个 MinIO bucket。Bucket 名称直接从 FileType 枚举的 value 字段获取，无需在配置文件中重复定义。

## Goals / Non-Goals

**Goals:**
- 实现 `FileRepository` 接口，提供基于 MinIO 的文件存储功能
- 创建配置属性类从 application.yml 读取 MinIO 配置
- 创建 MinIO 客户端配置类，初始化并注入 MinioClient
- 支持多 bucket 管理，根据 FileType 自动选择对应 bucket
- 确保 bucket 在应用启动时自动创建（如果不存在）

**Non-Goals:**
- 不实现文件删除功能（超出当前接口范围）
- 不实现文件列表查询功能
- 不修改现有 FileRepository 接口定义
- 不添加额外的文件元数据管理

## Decisions

### 1. 使用 MinIO Java SDK 8.5.4
- **选择**: 使用 `io.minio:minio:8.5.4` SDK
- **理由**: 该版本稳定，与 Spring Boot 3.x 兼容，且已在 pom.xml 中声明
- **替代方案**: 使用 Amazon S3 SDK（过度设计，增加复杂度）

### 2. 配置类位置
- **选择**: 配置属性类放在 `com.bluenet.web.infrastructure.config.properties` 包下
- **理由**: 与现有 `JwtProperties` 配置模式保持一致，符合项目结构
- **替代方案**: 放在 `config` 根包下（可能与其他配置类混淆）

### 3. Repository 实现类位置
- **选择**: 实现类放在 `com.bluenet.web.infrastructure.repository.impl` 包下
- **理由**: 遵循 DDD 分层架构，基础设施层实现领域层接口
- **替代方案**: 放在 `domain.repository` 包下（违反依赖原则）

### 4. Bucket 自动创建策略
- **选择**: 在应用启动时检查并自动创建 bucket
- **理由**: 确保运行时 bucket 一定存在，避免运行时错误
- **替代方案**: 手动创建（增加运维成本，易出错）

### 5. 文件名生成策略
- **选择**: 使用原始文件名，由调用方负责生成唯一文件名
- **理由**: 保持接口简单，调用方更清楚文件命名需求
- **替代方案**: 自动生成 UUID 文件名（可能不符合业务需求）

### 6. Bucket 名称来源
- **选择**: Bucket 名称直接从 FileType 枚举的 value 字段获取
- **理由**: 减少配置冗余，避免配置与代码不一致的问题；FileType 枚举已包含 bucket 名称信息
- **替代方案**: 在 application.yml 中配置 bucket-names 列表（需要维护两份相同的映射关系）

## Risks / Trade-offs

- **[风险] MinIO 服务器连接失败** → 启动时检查连接，失败则阻止应用启动
- **[风险] Bucket 权限配置错误** → 使用配置的 accessKey/secretKey 需要拥有 bucket 创建和文件操作权限
- **[风险] 大文件上传内存溢出** → 使用 InputStream 而非 byte[]，支持流式传输
- **[权衡] 无文件删除功能** → 当前版本不提供删除功能，如需删除需后续扩展

## Migration Plan

1. 确保 MinIO 服务器已部署并可访问
2. 配置环境变量：MINIO_ENDPOINT, MINIO_PORT, MINIO_AK, MINIO_SK
3. 应用启动时会自动创建所需 bucket
4. 验证：调用文件上传/下载接口测试功能

## Open Questions

- 是否需要支持文件访问权限控制（私有/公开）？
- 是否需要添加文件大小限制？（当前 application.yml 已配置 5MB）
