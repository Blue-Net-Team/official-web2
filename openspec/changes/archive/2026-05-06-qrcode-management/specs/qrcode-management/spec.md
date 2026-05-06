## ADDED Requirements

### Requirement: 管理员获取咨询群二维码列表
系统 SHALL 提供管理接口获取咨询群二维码列表。

#### Scenario: 管理员获取咨询群二维码列表
- **WHEN** 管理员调用 `GET /api/v1/admin/qrcodes/consultation`
- **THEN** 系统返回所有咨询群二维码列表
- **AND** 响应格式为 `ResponseMessage<List<ConsultationQrcodeDTO>>`
- **AND** 每个 DTO 包含 `id` 和 `fileId`

### Requirement: 管理员更新咨询群二维码
系统 SHALL 提供管理接口更新咨询群二维码。

#### Scenario: 管理员更新咨询群二维码图片
- **WHEN** 管理员调用 `PUT /api/v1/admin/qrcodes/consultation/{id}` 更新图片
- **THEN** 系统更新二维码关联的文件
- **AND** 删除旧文件记录和 MinIO 对象
- **AND** 返回成功响应

#### Scenario: 更新不存在的二维码
- **WHEN** 管理员更新不存在的二维码 ID
- **THEN** 系统返回 404 Not Found

### Requirement: 管理员获取考核群二维码列表
系统 SHALL 提供管理接口获取考核群二维码列表。

#### Scenario: 管理员获取所有考核群二维码
- **WHEN** 管理员调用 `GET /api/v1/admin/qrcodes/assessment` 不带筛选参数
- **THEN** 系统返回所有考核群二维码列表
- **AND** 响应格式为 `ResponseMessage<List<AssessmentQrcodeDTO>>`

#### Scenario: 管理员按方向筛选考核群二维码
- **WHEN** 管理员调用 `GET /api/v1/admin/qrcodes/assessment?direction=COMPUTER_VISION`
- **THEN** 系统返回该方向的所有考核群二维码
- **AND** 不包含其他方向的二维码

#### Scenario: 管理员按轮次筛选考核群二维码
- **WHEN** 管理员调用 `GET /api/v1/admin/qrcodes/assessment?epoch=1`
- **THEN** 系统返回该轮次的所有考核群二维码
- **AND** 包含共用的二维码（is_shared=true）

#### Scenario: 管理员按方向和轮次筛选
- **WHEN** 管理员调用 `GET /api/v1/admin/qrcodes/assessment?direction=COMPUTER_VISION&epoch=1`
- **THEN** 系统返回精确匹配的二维码
- **AND** 包含共用的二维码（is_shared=true 且 epoch 匹配）

### Requirement: 管理员创建考核群二维码
系统 SHALL 提供管理接口创建考核群二维码。

#### Scenario: 管理员创建非共用的考核群二维码
- **WHEN** 管理员调用 `POST /api/v1/admin/qrcodes/assessment` 上传图片
- **AND** 请求包含 `fileId`、`direction`、`epoch`，`isShared=false`
- **THEN** 系统创建考核群二维码记录
- **AND** 返回成功响应

#### Scenario: 管理员创建共用的考核群二维码
- **WHEN** 管理员调用 `POST /api/v1/admin/qrcodes/assessment` 上传图片
- **AND** 请求包含 `fileId`、`epoch`，`isShared=true`，`direction` 为空
- **THEN** 系统创建共用的考核群二维码记录
- **AND** `direction` 字段为 NULL

#### Scenario: 文件类型不匹配
- **WHEN** 管理员上传的文件类型不是 QRCODE
- **THEN** 系统返回 400 Bad Request

### Requirement: 管理员更新考核群二维码
系统 SHALL 提供管理接口更新考核群二维码。

#### Scenario: 管理员更新考核群二维码信息
- **WHEN** 管理员调用 `PUT /api/v1/admin/qrcodes/assessment/{id}` 更新信息
- **THEN** 系统更新二维码的方向、轮次、共用标识或图片
- **AND** 返回成功响应

#### Scenario: 更新为共用时清空方向
- **WHEN** 管理员将 `isShared` 设为 `true`
- **THEN** 系统自动将 `direction` 设为 NULL

### Requirement: 管理员删除考核群二维码
系统 SHALL 提供管理接口删除考核群二维码。

#### Scenario: 管理员删除考核群二维码
- **WHEN** 管理员调用 `DELETE /api/v1/admin/qrcodes/assessment/{id}`
- **THEN** 系统删除 `tb_qrcode` 记录
- **AND** 删除关联的文件记录和 MinIO 对象

### Requirement: 前端二维码管理页面
系统 SHALL 提供管理后台二维码管理页面。

#### Scenario: 访问二维码管理页面
- **WHEN** 管理员访问 `/admin/qrcode`
- **THEN** 系统显示二维码管理页面
- **AND** 默认显示咨询群二维码 Tab

#### Scenario: 切换到考核群二维码 Tab
- **WHEN** 管理员点击"考核群二维码"Tab
- **THEN** 系统显示考核群二维码列表
- **AND** 显示方向和轮次筛选器

#### Scenario: 上传二维码
- **WHEN** 管理员点击"上传二维码"按钮
- **THEN** 系统显示上传 Drawer
- **AND** 根据当前 Tab 显示对应的表单字段

#### Scenario: 编辑二维码
- **WHEN** 管理员点击"编辑"按钮
- **THEN** 系统显示编辑 Drawer
- **AND** 预填充当前二维码信息

#### Scenario: 删除二维码
- **WHEN** 管理员点击"删除"按钮
- **THEN** 系统显示确认对话框
- **AND** 确认后删除二维码
