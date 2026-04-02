## Why

报名页面需要展示咨询群二维码，方便新生扫码加入咨询。咨询群数量动态变化，需要通过后端管理。同时为后续考核群二维码功能预留扩展性（按轮次、方向划分，权限控制）。

## What Changes

### 数据库变更
- 扩展 `tb_qrcode` 表，添加 `epoch`（考核轮次）、`direction`（方向）、`is_shared`（是否共用）字段
- 修改 `QrcodeType` 枚举：从 `USER/GROUP` 扩展为 `USER/CONSULTATION/ASSESSMENT`

### 后端变更
- 新增公开接口 `GET /api/v1/qrcodes/consultation` 获取咨询群列表
- 新增管理接口用于咨询群 CRUD（上传、删除、列表）
- 新增 `QrcodeController` 控制器
- 扩展 `QrcodeDomainService` 领域服务

### 前端变更
- 在 enroll 页面添加咨询群展示组件
- 组件展示方式：列表展示群名，鼠标悬浮显示二维码

## Capabilities

### New Capabilities
- `consultation-qrcode`: 咨询群二维码管理功能，包含公开获取接口和管理接口

### Modified Capabilities
- `frontend-enroll-page`: 报名页面新增咨询群二维码展示区域

## Impact

### 后端
- `QrcodeType.java` - 枚举扩展
- `Qrcode.java` - 实体字段扩展
- `QrcodeDomainService.java` - 领域服务扩展
- `QrcodeController.java` - 新增控制器
- Flyway 迁移脚本 - 数据库结构变更

### 前端
- `src/app/(public)/(other)/enroll/page.tsx` - 添加咨询群组件
- 新增 `ConsultationQrcode` 组件
- 新增 API 调用服务

### 数据库
- `tb_qrcode` 表结构变更（新增3个可空字段）
