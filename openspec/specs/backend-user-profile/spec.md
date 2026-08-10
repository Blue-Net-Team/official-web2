## ADDED Requirements

### Requirement: 用户头像更新接口
系统 SHALL 提供接口 `PUT /api/v1/users/avatar`，允许已登录用户通过 fileId 更新头像。

#### Scenario: 成功更新头像
- **WHEN** 已登录用户 PUT `/api/v1/users/avatar` body=`{fileId: 123}`
- **THEN** 系统 SHALL 校验 fileId 对应的文件存在且类型为 AVATAR
- **AND** 系统 SHALL 更新 tb_user.avatar_id 为 fileId
- **AND** 返回 200 + 更新后的用户信息

#### Scenario: 文件不存在
- **WHEN** PUT `/api/v1/users/avatar` body=`{fileId: 9999}`
- **THEN** 返回 404 错误，提示文件不存在

#### Scenario: 文件类型不匹配
- **WHEN** PUT `/api/v1/users/avatar` body=`{fileId: 123}` 但文件类型不是 AVATAR
- **THEN** 返回 400 错误，提示文件类型不匹配

#### Scenario: 未登录用户
- **WHEN** 未登录用户 PUT `/api/v1/users/avatar`
- **THEN** 返回 401 Unauthorized

### Requirement: 用户信息包含微信二维码
系统 SHALL 在 `GET /api/v1/user/info` 响应中返回当前登录用户的微信二维码 URL，并同时返回用户的内推码。

#### Scenario: 用户已设置微信二维码
- **WHEN** 已登录用户调用 `GET /api/v1/user/info`
- **THEN** 响应 SHALL 包含 `wechatQrcode` 字段，值为预签名文件 URL
- **AND** 响应 SHALL 包含 `internalReferralCode` 字段，值为用户内推码

#### Scenario: 用户未设置微信二维码
- **WHEN** 已登录用户调用 `GET /api/v1/user/info` 且该用户 `qrcode_id` 为空
- **THEN** 响应 SHALL 包含 `wechatQrcode` 字段，值为 `null`
- **AND** 响应 SHALL 包含 `internalReferralCode` 字段，值为用户内推码

#### Scenario: 未登录用户获取用户信息
- **WHEN** 未登录用户调用 `GET /api/v1/user/info`
- **THEN** 返回 401 Unauthorized

### Requirement: 用户资料响应包含内推码
系统 SHALL 在 `GET /api/v1/user/info` 响应中返回当前登录用户的 `internalReferralCode`。

#### Scenario: 成员获取内推码
- **WHEN** 已登录成员调用 `GET /api/v1/user/info`
- **THEN** 响应 SHALL 包含 `internalReferralCode` 字段
- **AND** 字段值 SHALL 与 `tb_user.internal_referral_code` 一致

#### Scenario: 未设置内推码的用户
- **WHEN** 已登录用户调用 `GET /api/v1/user/info` 但该用户 `internal_referral_code` 为空
- **THEN** 响应 SHALL 包含 `internalReferralCode` 字段，值为 `null` 或空字符串

#### Scenario: 未登录用户
- **WHEN** 未登录用户调用 `GET /api/v1/user/info`
- **THEN** 返回 401 Unauthorized

### Requirement: 用户可更新微信二维码
系统 SHALL 允许已登录用户通过 `PUT /api/v1/user/info` 更新微信二维码文件标识。

#### Scenario: 成功更新微信二维码
- **WHEN** 已登录用户 PUT `/api/v1/user/info` body 包含 `qrcodeFileId: 123`
- **THEN** 系统 SHALL 校验 fileId 对应的文件存在且类型为 `QRCODE`
- **AND** 系统 SHALL 更新 `tb_user.qrcode_id` 为指定 fileId（通过 `tb_qrcode` 关联表）
- **AND** 返回 200 成功响应

#### Scenario: 文件不存在
- **WHEN** 已登录用户 PUT `/api/v1/user/info` body 包含 `qrcodeFileId: 9999`
- **THEN** 返回 404 错误，提示文件不存在

#### Scenario: 文件类型不匹配
- **WHEN** 已登录用户 PUT `/api/v1/user/info` body 包含 `qrcodeFileId: 123` 但文件类型不是 `QRCODE`
- **THEN** 返回 400 错误，提示文件类型不匹配

#### Scenario: 未提供 qrcodeFileId
- **WHEN** 已登录用户 PUT `/api/v1/user/info` body 不包含 `qrcodeFileId`
- **THEN** 系统 SHALL 不修改用户的二维码设置，其他字段正常更新

#### Scenario: 清空微信二维码
- **WHEN** 已登录用户 PUT `/api/v1/user/info` body 包含 `qrcodeFileId: null`
- **THEN** 系统 SHALL 将 `tb_user.qrcode_id` 设为 `null`
- **AND** 返回 200 成功响应
