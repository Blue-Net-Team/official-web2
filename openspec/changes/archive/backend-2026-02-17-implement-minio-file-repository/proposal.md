## Why

应用需要统一的文件存储解决方案来支持文件上传、下载和管理功能。根据 application.yml 中已配置的 MinIO 配置，需要实现 FileRepository 接口以提供基于 MinIO 的文件存储能力，支持多 bucket 管理和灵活的文件操作。

## What Changes

- 创建 `MinioFileRepository` 实现类，实现 `FileRepository` 接口
- 创建 MinIO 配置属性类，从 application.yml 读取配置（endpoint、port、accessKey、secretKey、useSSL）
- 添加 minio-java 依赖到 pom.xml
- 创建 MinIO 客户端配置类
- 支持多 bucket 管理，bucket 名称从 FileType 枚举自动获取
- 实现文件保存和加载功能
- **改进**: 移除 application.yml 中的 bucket-names 配置，直接从 FileType 枚举获取 bucket 名称，减少配置冗余

## Capabilities

### New Capabilities
- `minio-file-storage`: MinIO 文件存储实现，提供文件上传、下载、管理功能，支持多 bucket 配置

### Modified Capabilities
- 无

## Impact

- 新增 MinIO 相关依赖（minio-java SDK，已在 pom.xml 中声明）
- 新增基础设施层代码（config、repository 实现）
- 需要配置 MinIO 服务器连接信息（endpoint、port、accessKey、secretKey）
- 支持 5 个预定义 bucket：avatar、normal-img、assessment-attachment、qrcode、work（从 FileType 枚举自动获取）
- **配置变更**: application.yml 中的 `minio.bucket-names` 配置项将被移除
