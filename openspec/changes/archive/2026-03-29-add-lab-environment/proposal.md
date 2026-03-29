## Why

官网需要一个独立的实验室环境展示页面（`/lab-environment`），向访客展示团队的实验场地和先进设备。当前 `IntroduceImage` 表结构过于简单（仅有 description 字段），无法满足场地（标题+副标题+描述）和设备（标题+品牌+描述）的结构化展示需求。

## What Changes

### 后端
- 新增 `tb_venue` 表存储场地信息（标题、副标题、描述、图片）
- 新增 `tb_equipment` 表存储设备信息（标题、品牌、描述、图片）
- 新增场地管理 API（CRUD + 图片上传）
- 新增设备管理 API（CRUD + 图片上传）
- **BREAKING** 移除 `ImageType` 枚举中的 `LABORATORY` 和 `EQUIPMENT` 值

### 前端
- 新增 `/lab-environment` 路由页面
- 实现场地展示区（2x2 卡片布局）
- 实现设备展示区（2x3 卡片布局）
- Hero 区域使用固定文本（硬编码）

## Capabilities

### New Capabilities

- `backend-venue-management`: 场地管理能力，包括场地实体的 CRUD 操作、图片上传、排序管理
- `backend-equipment-management`: 设备管理能力，包括设备实体的 CRUD 操作、图片上传、排序管理
- `frontend-lab-environment-page`: 实验室环境展示页面，包含场地展示区和设备展示区

### Modified Capabilities

- `backend-introduce-image-management`: 移除 `LABORATORY` 和 `EQUIPMENT` 类型的支持，这些类型将由新的场地和设备表替代

## Impact

### 数据库
- 新增 `tb_venue` 表
- 新增 `tb_equipment` 表
- 需要手动处理现有的 `LABORATORY` 和 `EQUIPMENT` 类型数据迁移（如有）

### 后端
- 新增 `Venue` 和 `Equipment` 实体、VO、DTO
- 新增 `VenueService` 和 `EquipmentService`
- 新增 `VenueDomainService` 和 `EquipmentDomainService`
- 新增 `VenueController`（公开）和 `AdminVenueController`（管理）
- 新增 `EquipmentController`（公开）和 `AdminEquipmentController`（管理）
- 修改 `ImageType` 枚举

### 前端
- 新增 `/lab-environment` 页面
- 新增场地和设备的 API 服务
- 管理平台新增场地和设备管理功能

### API 端点
- `GET /api/v1/venues` - 获取场地列表（公开）
- `GET /api/v1/equipments` - 获取设备列表（公开）
- `POST /api/v1/admin/venues` - 创建场地
- `PUT /api/v1/admin/venues/{id}` - 更新场地
- `DELETE /api/v1/admin/venues/{id}` - 删除场地
- `POST /api/v1/admin/equipments` - 创建设备
- `PUT /api/v1/admin/equipments/{id}` - 更新设备
- `DELETE /api/v1/admin/equipments/{id}` - 删除设备
