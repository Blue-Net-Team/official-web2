## ADDED Requirements

### Requirement: 获取咨询群列表
系统 SHALL 提供公开接口获取咨询群二维码列表。

#### Scenario: 公开获取咨询群列表
- **WHEN** 未登录用户访问 `GET /api/v1/qrcodes/consultation`
- **THEN** 系统返回所有咨询群二维码列表
- **AND** 响应格式为 `ResponseMessage<List<ConsultationQrcodeDTO>>`
- **AND** 每个 DTO 包含 `id` 和 `fileId`

### Requirement: 上传咨询群二维码
系统 SHALL 提供管理接口上传咨询群二维码。

#### Scenario: 管理员上传咨询群二维码
- **WHEN** 管理员调用 `POST /api/v1/admin/qrcodes/consultation` 上传图片
- **THEN** 系统保存二维码文件
- **AND** 创建 `tb_qrcode` 记录（type=CONSULTATION）
- **AND** 返回文件信息

#### Scenario: 非管理员尝试上传
- **WHEN** 非管理员用户调用上传接口
- **THEN** 系统返回 403 Forbidden

### Requirement: 删除咨询群二维码
系统 SHALL 提供管理接口删除咨询群二维码。

#### Scenario: 管理员删除咨询群二维码
- **WHEN** 管理员调用 `DELETE /api/v1/admin/qrcodes/consultation/{id}`
- **THEN** 系统删除 `tb_qrcode` 记录
- **AND** 删除关联的文件记录
- **AND** 删除 MinIO 中的文件

#### Scenario: 删除不存在的二维码
- **WHEN** 管理员删除不存在的二维码 ID
- **THEN** 系统返回 404 Not Found

### Requirement: 数据库结构扩展
系统 SHALL 扩展 `tb_qrcode` 表支持咨询群和考核群。

#### Scenario: 表结构包含扩展字段
- **WHEN** 系统运行数据库迁移
- **THEN** `tb_qrcode` 表包含以下字段：
  - `epoch` INT NULL（考核轮次）
  - `direction` VARCHAR(50) NULL（方向）
  - `is_shared` BOOLEAN DEFAULT FALSE（是否共用）

### Requirement: 二维码类型枚举扩展
系统 SHALL 扩展 `QrcodeType` 枚举支持咨询群和考核群。

#### Scenario: 枚举包含新类型
- **WHEN** 系统启动
- **THEN** `QrcodeType` 枚举包含：
  - `USER`：用户微信二维码
  - `CONSULTATION`：咨询群二维码
  - `ASSESSMENT`：考核群二维码
